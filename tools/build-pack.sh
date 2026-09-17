#!/usr/bin/env bash
#
# Rebuild the pushed texture pack and wire its hash into all six editions.
#
#   ./tools/build-pack.sh [release-tag]
#
# The server pushes this zip to every joining client, and the client REFUSES it if the SHA-1 in
# GameplayConfig does not match the file byte for byte. So the zip and the hash have to be produced
# together, every time — doing it by hand is how a pack ends up rejected in the wild.
#
# Uses the JDK `jar` tool, never PowerShell Compress-Archive: Compress-Archive writes backslash path
# separators into the zip entries and Minecraft cannot read the result.
#
# Run this after ANY change under resourcepack/ — new art, a lang update, a new item model.

set -euo pipefail

HERE="$(cd "$(dirname "$0")/.." && pwd)"
PACK_DIR="$HERE/resourcepack"
OUT="$HERE/curseforge-upload/VanillaSkills-TexturePack.zip"
TAG="${1:-}"

JAR="${JAVA_HOME:+$JAVA_HOME/bin/}jar"

if [ ! -f "$PACK_DIR/pack.mcmeta" ]; then
    echo "error: $PACK_DIR/pack.mcmeta not found" >&2
    exit 1
fi

# Keep the pack's lang copies identical to the mod's. A vanilla client has no mod jar, so the NAMES of
# every custom item come from the pack — a stale copy ships raw translation keys to players.
cp "$HERE/src/main/resources/assets/vanillaskills/lang/"*.json \
   "$PACK_DIR/assets/vanillaskills/lang/"

mkdir -p "$(dirname "$OUT")"
rm -f "$OUT"
# -C so pack.mcmeta lands at the zip ROOT, which is what Minecraft requires.
# List the pack's parts explicitly rather than zipping the whole directory: `.` also swept up
# README.txt and _PROGRESS.md, which are notes to ourselves and were being shipped to every player
# who joined. Minecraft ignores them, but they have no business in a pushed pack.
(cd "$PACK_DIR" && "$JAR" --create --file "$OUT" --no-manifest pack.mcmeta pack.png assets)

SHA1=$(sha1sum "$OUT" | cut -d' ' -f1)
SIZE=$(wc -c < "$OUT")

echo "built  $OUT"
echo "size   $SIZE bytes"
echo "sha1   $SHA1"

# Patch the hash (and tag) into every edition so they cannot drift apart.
#
# Done in Node, not perl: `perl -pi` reads bytes as latin-1 and so truncates every multi-byte character
# in the file it rewrites. GameplayConfig's javadoc is full of em dashes, and the mangled result still
# compiles, so the damage is easy to ship without noticing.
node "$HERE/tools/patch-pack-refs.js" "$SHA1" ${TAG:+"$TAG"}

echo
echo "Next: upload $OUT to the GitHub release${TAG:+ $TAG}, then rebuild all six editions."
