#!/usr/bin/env node
/*
 * Compare the six editions' Java sources and report anything that drifted.
 *
 *   node tools/check-parity.js
 *
 * The six editions are two axes, and each axis allows a different kind of difference:
 *
 *   VERSION  (26.2 vs 26.1.2, same loader)  — should be IDENTICAL apart from renamed MC symbols.
 *   LOADER   (Fabric vs NeoForge, same MC)  — a short list of entrypoint/registration files differ.
 *
 * Checking both axes catches the two mistakes that actually happen: changing one edition and
 * forgetting the others, and blanket-copying a file that was supposed to differ. Doing the latter to
 * ShardBlocks.java (26.2 has EntityTypes, 26.1.2 has EntityType) broke both 26.1.2 builds, which is
 * why this exists.
 */
const fs = require('fs');
const path = require('path');

const PROJECTS = 'C:/Users/plapo/projects/';
const SRC = '/src/main/java/io/github/andrewwwwwwwwwwwwwww/vanillaskills/';

// Files whose contents are tied to the mod loader. Skipped on the LOADER axis, still compared on the
// VERSION axis — a Fabric-only file must still match between 26.2 Fabric and 26.1.2 Fabric.
const LOADER_DIVERGENT = new Set([
  'VanillaSkills.java',              // Fabric initializer vs NeoForge @Mod + event bus
  'loot/ShardLoot.java',             // loot injection goes through each loader's own hook
  'loot/CrateLoot.java',
  'loot/DragonTemplateLoot.java',
  'loot/FortuneTemplateLoot.java',
  'creative/VanillaSkillsItemGroup.java', // item-group registration differs per loader
  'client/VanillaSkillsClient.java',
  'client/ClientConfig.java',
  'client/VanillaSkillsModMenu.java',     // Mod Menu is Fabric-only; absent on NeoForge
]);

// 26.1.2 renamed this class; without normalising, every file touching it would be flagged.
function normalise(s) {
  return s.replace(/\r\n/g, '\n').replace(/\bEntityTypes\b/g, 'EntityType').trimEnd();
}

function walk(dir, base, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, base, out);
    else if (e.name.endsWith('.java')) out.push(path.relative(base, p).replace(/\\/g, '/'));
  }
  return out;
}

let problems = 0;

function compare(label, refEd, edEd, skip) {
  const refDir = PROJECTS + refEd + SRC;
  const dir = PROJECTS + edEd + SRC;
  const refFiles = walk(refDir, refDir).sort();
  const files = new Set(walk(dir, dir));
  const drifted = [], missing = [], extra = [];

  for (const f of refFiles) {
    if (skip.has(f)) continue;
    if (!files.has(f)) { missing.push(f); continue; }
    if (normalise(fs.readFileSync(refDir + f, 'utf8')) !== normalise(fs.readFileSync(dir + f, 'utf8'))) {
      drifted.push(f);
    }
  }
  for (const f of files) if (!refFiles.includes(f) && !skip.has(f)) extra.push(f);

  const bad = drifted.length + missing.length + extra.length;
  problems += bad;
  console.log(`\n${label}\n  ${refEd}  ->  ${edEd}   ${bad === 0 ? 'in parity' : bad + ' problem(s)'}`);
  for (const f of missing) console.log('    MISSING  ' + f);
  for (const f of extra) console.log('    EXTRA    ' + f);
  for (const f of drifted) console.log('    DIFFERS  ' + f);
}

// VERSION axis: same loader, different MC. Nothing may differ.
compare('VERSION (Fabric)', '26.2/vanillaskills', '26.1.2/vanillaskills', new Set());
compare('VERSION (NeoForge)', '26.2/vanillaskills-neoforge', '26.1.2/vanillaskills-neoforge', new Set());
// LOADER axis: same MC, different loader. Only the entrypoint/registration files may differ.
compare('LOADER (26.2)', '26.2/vanillaskills', '26.2/vanillaskills-neoforge', LOADER_DIVERGENT);
// 26.3 is a port, so it is NOT compared to 26.2 on the VERSION axis — the MC symbols genuinely
// differ. Its two loaders must still agree with each other, and that is the check that would have
// caught the 26.3 fixes landing on Fabric alone.
compare('LOADER (26.3)', '26.3/vanillaskills', '26.3/vanillaskills-neoforge', LOADER_DIVERGENT);

console.log(problems === 0
  ? '\nAll six editions in parity.'
  : `\n${problems} problem(s) — propagate the change, or add the file to LOADER_DIVERGENT if it is meant to differ.`);
process.exit(problems === 0 ? 0 : 1);
