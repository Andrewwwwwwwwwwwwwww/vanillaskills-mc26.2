#!/usr/bin/env node
/*
 * Validate the things that silently break the CLIENT but never fail a build.
 *
 *   node tools/check-assets.js
 *
 * Two real bugs motivated this, both shipped to players unnoticed:
 *
 *   1. PLACEHOLDER TEXTURES IN THE JAR. The 2.0 art drop landed in resourcepack/ only and was never
 *      copied back into src/main/resources/. Nine items (the three shard items and six crates) kept
 *      their 155-byte magenta placeholder inside the mod. Servers looked fine — the pushed pack
 *      covers them — so it only showed in SINGLE PLAYER, where no pack is pushed and the jar is the
 *      only source of art. Every mod texture must be byte-identical to the pack's copy.
 *
 *   2. DUPLICATE LANG KEYS. A second "vanillaskills.points.tasks" was added for a new screen entry
 *      while the old one still existed. Gson parses the language file strictly, so ONE duplicate
 *      throws and the WHOLE file fails to load — every string in every language silently falls back
 *      to its hardcoded English. It logged one line and was otherwise invisible.
 *
 * Neither is a compile error, and neither is caught by check-parity.js (which compares editions
 * against each other — and a bug copied to all six editions is "in parity").
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
// Compare the WHOLE shared asset tree, not just textures/. The first version of this check only
// walked textures/ and therefore missed that models/ had drifted exactly the same way: the crates
// were `item/generated` (a flat sprite) in the jar and `block/cube_all` (a cube) in the pack, so a
// crate looked different in single player than on a server. Any subtree common to both must match.
const MOD_ASSETS = path.join(ROOT, 'src/main/resources/assets/vanillaskills');
const PACK_ASSETS = path.join(ROOT, 'resourcepack/assets/vanillaskills');
// lang/ is the one deliberate exception: tools/build-pack.sh regenerates the pack's copy from the
// mod's at build time, so a difference here is staleness in the built zip, not a source-tree fault.
const SHARED_SKIP = new Set(['lang']);
const LANG = path.join(MOD_ASSETS, 'lang');

let problems = 0;
const fail = (msg) => { console.error('  ✗ ' + msg); problems++; };

function walk(dir, base = dir, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, base, out);
    else out.push(path.relative(base, p).replace(/\\/g, '/'));
  }
  return out;
}

// --- 1. shared assets must match the pack, byte for byte ------------------------------------
console.log('Shared assets: mod jar vs pushed pack');
if (!fs.existsSync(PACK_ASSETS)) {
  fail('resourcepack assets not found at ' + PACK_ASSETS);
} else {
  const shared = (root) =>
    fs.readdirSync(root, { withFileTypes: true })
      .filter((e) => e.isDirectory() && !SHARED_SKIP.has(e.name))
      .map((e) => e.name);

  for (const sub of shared(MOD_ASSETS)) {
    const modDir = path.join(MOD_ASSETS, sub);
    const packDir = path.join(PACK_ASSETS, sub);
    if (!fs.existsSync(packDir)) continue; // mod-only subtree (e.g. items/ definitions) — fine
    for (const rel of walk(modDir)) {
      const a = path.join(modDir, rel);
      const b = path.join(packDir, rel);
      if (!fs.existsSync(b)) { fail(`pack is missing ${sub}/${rel} (mod has it)`); continue; }
      if (!fs.readFileSync(a).equals(fs.readFileSync(b))) {
        fail(`${sub}/${rel} differs — single player shows the mod's copy, servers show the pack's`);
      }
    }
    for (const rel of walk(packDir)) {
      if (!fs.existsSync(path.join(modDir, rel))) fail(`mod is missing ${sub}/${rel} (pack has it)`);
    }
  }
}

// --- 2. language files: no duplicate keys, and the same key set everywhere -------------------
console.log('Language files: duplicate keys and key parity');
const langFiles = fs.existsSync(LANG) ? fs.readdirSync(LANG).filter((f) => f.endsWith('.json')) : [];
const keySets = {};
for (const f of langFiles) {
  const raw = fs.readFileSync(path.join(LANG, f), 'utf8');
  const keys = [...raw.matchAll(/^\s*"([^"]+)"\s*:/gm)].map((m) => m[1]);
  const seen = new Set(), dupes = new Set();
  for (const k of keys) (seen.has(k) ? dupes : seen).add(k);
  // Gson throws on the first duplicate and the entire file fails to load, so this is fatal.
  for (const d of dupes) fail(`${f}: duplicate key "${d}" — Gson refuses the whole file`);
  keySets[f] = seen;
}
const [reference, ...others] = Object.keys(keySets);
for (const other of others) {
  for (const k of keySets[reference]) {
    if (!keySets[other].has(k)) fail(`${other} is missing key "${k}" (present in ${reference})`);
  }
  for (const k of keySets[other]) {
    if (!keySets[reference].has(k)) fail(`${reference} is missing key "${k}" (present in ${other})`);
  }
}

console.log(problems === 0 ? '\nAssets OK.' : `\n${problems} problem(s) found.`);
process.exit(problems === 0 ? 0 : 1);
