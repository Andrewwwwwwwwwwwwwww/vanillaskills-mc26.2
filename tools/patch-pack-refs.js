#!/usr/bin/env node
//
// Write the pack's SHA-1 (and optionally its release tag) into every edition's GameplayConfig.
//
//   node tools/patch-pack-refs.js <sha1> [tag]
//
// Node rather than perl on purpose. `perl -pi` reads bytes as latin-1, so it truncates every multi-byte
// character in the file it rewrites — GameplayConfig is full of em dashes in its javadoc, and they come
// out as mojibake that still compiles. This reads and writes UTF-8 explicitly.

const fs = require('fs');
const path = require('path');

const [sha1, tag] = process.argv.slice(2);
if (!sha1 || !/^[0-9a-f]{40}$/.test(sha1)) {
  console.error('usage: patch-pack-refs.js <40-char-sha1> [tag]');
  process.exit(1);
}

const here = path.resolve(__dirname, '..');
// Every edition, listed from the projects root rather than relative to whichever copy of this
// script is running. The old relative list was written when there were four editions and only ever
// reached the MC version it sat in plus 26.1.2, so adding 26.2 and 26.3 left two editions holding a
// stale hash — and a stale hash is a pack the client refuses, not one that merely looks wrong.
const root = path.resolve(here, '../..');
const EDITIONS = [
  path.join(root, '26.3/vanillaskills'),
  path.join(root, '26.3/vanillaskills-neoforge'),
  path.join(root, '26.2/vanillaskills'),
  path.join(root, '26.2/vanillaskills-neoforge'),
  path.join(root, '26.1.2/vanillaskills'),
  path.join(root, '26.1.2/vanillaskills-neoforge'),
];

let patched = 0;
for (const ed of EDITIONS) {
  const cfg = path.join(ed, 'src/main/java/io/github/andrewwwwwwwwwwwwwww/vanillaskills/config/GameplayConfig.java');
  if (!fs.existsSync(cfg)) { console.log('skip (no config): ' + ed); continue; }

  let s = fs.readFileSync(cfg, 'utf8');
  const before = s;

  s = s.replace(/(DEFAULT_RP_SHA1 = ")[0-9a-f]{40}(")/, '$1' + sha1 + '$2');

  if (tag) {
    // ONLY the DEFAULT_RP_URL line. The same URL shape appears in SUPERSEDED_RP_URLS, which is the
    // historical record of packs already shipped — rewriting those flattens them onto the current tag and
    // silently turns the auto-upgrade for servers pinned to an old pack into a no-op.
    s = s.replace(
      /(DEFAULT_RP_URL =\s*\n\s*"[^"]*releases\/download\/)[^/"]+(\/VanillaSkills-TexturePack\.zip")/,
      '$1' + tag + '$2');
  }

  if (s === before) { console.log('unchanged: ' + path.basename(ed)); continue; }
  fs.writeFileSync(cfg, s, 'utf8');
  console.log('patched ' + path.basename(path.dirname(ed)) + '/' + path.basename(ed));
  patched++;
}

if (patched === 0) {
  console.error('error: nothing was patched');
  process.exit(1);
}
