# VanillaSkills Changelog

## [2.4.0] - 2026-09-15

### Changed
- **The Aquatic lane's swim speed is the mod's own, and no longer Dolphin's Grace.** Minecraft reads that
  effect as a yes/no — `hasEffect`, not the level — so the first of the three swim nodes was already handing
  out the whole of it, the two above it added nothing to swimming, and what it handed out was a permanent
  dolphin: water drag dropped from 0.8 to 0.96, which multiplies your top speed in water several times over.
  - Each of the three nodes now adds to how hard you swim, while you are in water. Three of them together is a
    real gain that an actual dolphin would still comfortably beat, and the nodes finally differ from one
    another.
  - How much is `aquaticSwimSpeed` in `gameplay.json`, +20% per node by default. Set it to 0 to leave the
    lane with only its seabed walking.
  - It is an attribute rather than an effect, so it still reaches a vanilla client on its own.
  - Nothing to do on an existing world: the old effect simply lapses within twenty seconds of the update,
    because nothing renews it any more.
- **The stats screen tells the truth about that lane.** It was labelling water-movement-efficiency as "Swim
  Speed", which is the attribute that governs walking along the seabed. Swim speed and wading are separate
  lines now, and each shows what it actually is.

## [2.3.0] - 2026-09-15

### Added
- **Netherite Scrap repairs netherite gear.** An ingot is four scrap and four gold, and the gold does nothing
  for the repair, so mending a netherite tool was the most expensive repair in the game. Scrap works now, and
  the ingot still does. The Dragon tier is untouched: it is built on netherite items but repairs with Dragon
  Ingots, and scrap has no business there.
- **Bone meal spreads spore blossoms.** They are the one lush-cave plant vanilla gives you no way to make more
  of — they grow nowhere and spread to nothing, so the only supply is what the world generated. Bone meal on one
  now grows another on a ceiling within a few blocks, which makes a lush cave something to tend rather than
  strip. If there is nowhere for one to hang, the bone meal is not spent. Turn it off with
  `bonemealSporeBlossoms`.

### Changed
- **Updated to Minecraft 26.3.** Fabric loader 0.19.3 → 0.19.5, Fabric API 0.152.1+26.2 → 0.160.5+26.3,
  `minecraft` dependency `~26.2` → `~26.3`. The 26.2 and 26.1.2 builds carry on alongside it.

### Fixed
- **The Dragon set no longer fires you across the water.** Its dive-dash triggers on holding sneak while off
  the ground — and while swimming you are never on the ground, and sneak is how you sink. So wearing the full
  set and holding sneak underwater launched you at dash speed, once per cooldown, for as long as you held it.
  The dash is a dive through the air, as the armour says, and it stays out of water and lava now.

## [2.1.16] - 2026-08-28

### Changed
- Aquatic lane rebalanced. Shards spent on any node the rebalance dropped are refunded automatically on the next join — priced from the economy that was in force when they were bought, so the figure is right whatever a server total earnable happens to be.

## [2.1.15] - 2026-08-28

### Changed
- **The Infusing Table pages instead of truncating.** It has 21 slots and simply stopped drawing after the 21st offer, so anything past that was unreachable — no scrolling, no sign it existed. Previous/Next buttons flank Confirm when there is more than one page, and selections are held per enchantment, so picking something on one page and paying on another works as expected.
- **Enchantments the held item can take are listed first.** The order is worked out against whatever you are holding the moment the screen opens, so what fits is on page one and in shelf order, with the rest behind it.
- **An applicable enchantment now shows as an Enchanted Book, an inapplicable one as a plain Book.** Colour alone was carrying that distinction; the icon carries it now, with the selection glint on top, so the three states — cannot use, can use, picked — read at a glance.
- New option `infusingHideIncompatible`: leave enchantments the item cannot take out of the list entirely rather than greying them out. Off by default, since seeing what your shelves hold is usually worth the clutter.

## [2.1.14] - 2026-08-28

### Fixed
- **Crates were flat sprites in single player and cubes on servers; the shard blocks were slightly oversized.** The same jar-versus-pack drift that 2.1.13 fixed for textures also existed in the models — eight files where the mod carried `item/generated` (a flat 2D sprite) while the pack carried `block/cube_all` (a proper cube). The mod now ships the pack's models, so a crate looks the same wherever you play.

### Changed
- `tools/check-assets.js` now compares the **whole** shared asset tree rather than only `textures/`, which is why the model drift above went unnoticed when the tool was written. `lang/` stays excluded, since the pack's copy is regenerated at build time.

## [2.1.13] - 2026-08-28

### Fixed
- **Skill Shards, the shard blocks and six of the seven crates showed the purple-and-black missing-texture square in single player.** The 2.0 art drop went into the pushed texture pack but was never copied back into the mod's own resources, so the jar still carried 155-byte placeholders for those nine items. Servers hid it — every client gets the pack on join — but single player pushes no pack, leaving the jar as the only source of art. The real textures now ship inside the mod as well.
- **The language file failed to load entirely, in every language.** A second `vanillaskills.points.tasks` key was added for the Hard Work entry while the original (the advancement frame type) still existed. Gson rejects a duplicate key by throwing on the whole file, so every string fell back to its hardcoded English and Traditional Chinese did not work at all. The new entry is now `vanillaskills.points.hardwork`.

### Added
- `tools/check-assets.js` — fails the build ritual on either of the above: any mod texture that differs from the pack's copy, any duplicate language key, and any key present in one language but not another. Both bugs were invisible to `check-parity.js`, which compares the four editions against each other and so cannot see a fault copied into all of them.

## [2.1.12] - 2026-08-27

### Added
- **The recipe book opens on a contents page.** Every custom recipe is listed as its finished item in one clickable grid — click an item to see its crafting grid, then go back to the list or straight to the skill tree from the same row of buttons. Thirty-odd recipes previously had to be reached by clicking "Next" until the right one appeared.

### Fixed
- **The Skill Shard count on the XP bar is now self-healing.** The readout was only re-sent when the balance changed, so any moment the client silently dropped its copy left the bar blank indefinitely. Two releases tried to enumerate those moments (respawn, dimension change) and both missed cases. The count is now simply re-sent twice a second — a dozen bytes per player — so it repairs itself within half a second no matter what caused the loss.

## [2.1.11] - 2026-08-27

### Fixed
- **The shard readout really survives dimension changes now.** 2.1.10's fix pushed the count immediately on the change — and lost a race: vanilla marks its own experience state stale on the entity rebuild and re-sends the player's real level (0) a tick later, wiping the pushed value. The hooks now clear the send-cache instead, so the regular reconcile re-sends the count safely *after* vanilla's zero. Worst case the bar is blank for half a second instead of forever.

## [2.1.10] - 2026-08-27

### Changed
- **Infusing now costs exactly the level: 1 shard for a level-I enchantment, 2 for II, 5 for V** (`infusingCostPerLevel` drops from 2 to 1). With every other price in the economy now material-shaped, per-level was the last place a flat multiplier made things lumpy. Existing worlds on a stored default migrate automatically.

### Fixed
- **The shard count on the XP bar no longer vanishes on a dimension change (or respawn).** The client silently resets its XP display when the player entity is rebuilt, but the server's send-only-on-change reconcile believed the client was current — so the readout stayed blank until the balance next moved. Portal trips and respawns now force a refresh.

## [2.1.9] - 2026-08-26

### Added
- **The config files now explain themselves.** `gameplay.json` and `points.json` gain a `#option-guide` block with a one-line description of every option, written by the mod into its own per-world files (JSON has no comments, so an extra key is the next best thing). It regenerates on every save, so it always matches the installed version, and editing it is harmless. Existing files pick it up automatically on first load.

## [2.1.8] - 2026-08-26

### Changed
- **Luck of the Sea now reels in a few more crates.** Each point of fishing luck — Luck of the Sea's level plus the luck attribute, so the Fortune Finder lane counts — raises the crate catch rate by roughly +10% relative: at defaults, 2.4% base → ~3.2% with Luck of the Sea III → ~4.3% with a maxed Fortune Finder on top. Unboxing deliberately keeps its own job: it upgrades *which* crate bites (the Diamond bonus draw), not how often. New option `crateFishingLuckQuality` (0 makes the rate ignore luck). The Earning Skill Shards screen now spells out both roles.

## [2.1.7] - 2026-08-26

### Changed
- **Updating actually updates the prices now.** The self-upgrading configs only ever added missing options — a stored value was always respected, so existing worlds silently kept the old infusing cost and advancement payouts forever. Stored values that exactly match a superseded default now follow the default to its new value (infusing 3 → 2/level; advancement payouts task 1/2 → 5, goal 5/12 → 20, challenge 20/45 → 50), the same policy the superseded texture-pack URLs already use. A hand-tuned value that matches no old default is never touched.
- **Repricing reaches balances automatically.** When payouts rise, every player now gets one automatic recalc on their next join — no more `/skill recalc <player>` per player. Spent shards are preserved, exactly as with the manual command.

## [2.1.6] - 2026-08-26

### Fixed
- **Repairing by combine is no longer free when the spare is unenchanted.** The material-based pricing charged a combine only for the enchantment levels on the sacrificed item — so feeding an enchanted fishing rod a plain spare restored its durability for nothing, while the mirrored arrangement charged per level. A same-item combine that restores durability now costs a flat fee (default **2 shards**, new option `anvilCombineRepairCost`), on top of any enchantment levels on the sacrifice. Material repairs are unaffected — those already price per unit.

## [2.1.5] - 2026-08-26

### Changed
- **The crate ladder now climbs itself.** Every crate has a small chance of an Unboxing book, tiered to the crate: **Wooden → Unboxing I (3%)**, **Copper → Unboxing II (3%)**, **Iron (5%) / Diamond (7%) / Frozen / Lush / Desert (4%) → Unboxing III**. The bottom of the ladder previously offered no way up it — without Unboxing, wooden crates stayed the only crates most players ever saw.
- **A crate can never drop two identical Unboxing books.** Iron and Diamond used to roll their Unboxing entry inside the multi-roll loot pool (at generous odds — Diamond handed out doubles regularly); every Unboxing book now sits in its own single-roll pool, so it is at most one per crate, at deliberately small odds. All of it is datapack loot, retunable like any other crate entry.

## [2.1.4] - 2026-08-26

### Added
- **The repair-less vanilla items get repair materials**: Trident ← Prismarine, Bow / Crossbow / Fishing Rod ← String, Flint and Steel / Shears ← Iron Ingot, Brush ← Copper Ingot. Vanilla ships all of these combine-only — a trident could only ever be mended with a second trident. The `repairable` component is stamped per stack (so vanilla clients preview the repair correctly), and repairs price as usual: 1 shard per material. The two "on a stick" rods stay combine-only, deliberately. Toggle: `vanillaRepairMaterials`.

### Fixed
- **A free anvil result can now actually be taken.** Vanilla treats cost 0 as "no operation" and refuses the pickup — so the free rename that 2.0.2 introduced showed a result that could not be removed from the anvil.
- **Renaming VanillaSkills gear sticks.** The legacy-gear sweep demoted any custom name off marked gear, which silently reverted a player's anvil rename moments after it was paid for. Only the mod's own legacy branding (text identical to the item's intrinsic name) is demoted now.
- **The Satchel keeps its name for life.** It was named through `item_name`, which placement throws away — so a placed Satchel became a plain brown shulker box and never got its name back. It is now named through `custom_name` (styled identically, and shown as the container title too), which vanilla carries through placing, the block entity, and mining. A Satchel crafted before this fix can be re-branded with a plain anvil rename, which is free.

## [2.1.3] - 2026-08-26

### Changed
- **Infusing gets cheaper: 2 shards per enchantment level, down from 3.** A level V book drops 15 → 10, a full sword loadout ~42 → ~28. The books were already permanent, so this was the only recurring cost on enchanting, and it priced a full kit at more than a whole run of goal advancements. Existing worlds keep whatever `infusingCostPerLevel` their gameplay.json stores — set it to 2 (and `/skill reload`) to adopt the new rate.

## [2.1.2] - 2026-08-26

### Fixed
- **Respawning restores your whole health bar.** Vanilla fills the fresh body to its base 20 HP before the Vitality lane's max-health modifiers are re-applied, so a player with extra hearts came back at a fraction of their bar. A death respawn now comes back at full health; returning through the End portal still keeps the health you left with.

## [2.1.1] - 2026-08-24

### Changed
- **Advancements pay more.** With enchanting and the anvil both charging Skill Shards, the advancement rates were too stingy: tasks now pay **5** (was 2), goals **20** (was 12) and challenges **50** (was 45). New worlds get the new numbers automatically; an existing world keeps its `points.json` — raise `valueTask` / `valueGoal` / `valueChallenge` there and run `/skill recalc <player>` to re-credit everyone at the new rates.
- **Luck raises the task-shard odds.** The 2.1.0 work-trickle now reads your luck attribute: each point (Fortune Finder gives +0.5 per node, Luck potions count too) multiplies the base chance by another +10%, so a maxed Fortune Finder lane rolls at +50% odds. Unluck genuinely worsens the roll. New option `taskShardLuckBonus` (0 ignores luck).

## [2.1.0] - 2026-08-24

### Added
- **Hard work pays.** Mining or placing a block and harvesting crops (berries and glow berries included) each carry a small chance — 0.2% by default — of shaking an Unstable Skill Shard loose on the spot. A payout starts a per-player cooldown (4 minutes by default) during which nothing further drops, so the trickle rewards ordinary play without ever becoming a farm. Creative mode earns nothing. New gameplay.json options: `taskShardChance` (0 disables the mechanic) and `taskShardCooldownSeconds`. The source is listed on the Earning Skill Shards screen.

## [2.0.2] - 2026-08-23

### Changed
- **Anvils now price by what the operation actually consumes, not vanilla's level formula.** Vanilla's cost curve was built around experience, which regrows; Skill Shards do not — a world holds a fixed number, so a late-game combine could eat 39 shards and the prior-work penalty doubled on every visit until a piece was unrepairable. The cost is now one shard per repair material and one per enchantment level on the sacrificed item, with a flat (default free) fee for a plain rename. New gameplay.json options: `anvilMaterialPricing`, `anvilRepairCostPerMaterial`, `anvilEnchantCostPerLevel` and `anvilRenameCost`; clearing the first restores vanilla's numbers.
- **Dragon scale repair drops from 20 shards to 2.** One scale still restores a piece completely, so it stays a flat fee — 20 only made sense when an ordinary combine cost 30–40.

## [2.0.1] - 2026-08-22

### Added
- **Satchel** — a chest surrounded by leather makes a brown shulker box named Satchel, so portable storage no longer waits on a trip to the End.

### Fixed
- **Crates handed out a broken Unstable Skill Shard Block.** Its loot table still built the item on an amethyst block, left behind when 2.0 took over reinforced deepslate. The block looked right and did nothing — no aura, no merging, and it would not craft back into shards. Spawner drops were unaffected.
- **Pre-2.0 gear that had lost its marker stayed broken forever.** Such a piece kept the old `custom_model_data`, whose supporting pack overrides 2.0 removed, so it rendered as a plain vanilla sword or helmet. Gear is now identified by the model id it claims and re-stamped in full — marker, model, name, repairability and the worn-armour asset — with enchantments, damage and anvil renames untouched.
- **Gear inside shulker boxes was never migrated.** The sweep only looked at loose inventory and the ender chest, which is exactly where spare kit is not kept.
- Every superseded texture pack now auto-upgrades. The list named six of the 47 released packs, so a server pinned to any of the other 41 kept pushing a pre-2.0 pack that overrides 57 vanilla items.

## [2.0.0] - 2026-08-20
The datapack-era rework. Experience is gone, the Skill Shard is a real item, and almost all of the mod's content now lives in datapack files instead of Java constants — so a server can rewrite the skill tree, the quests, the shop and the crates without touching the code.

**This is a breaking release.** Commands were removed, data formats changed, and the pushed texture pack was replaced. Back up your world before updating.

### Added
- **Skill Shards are physical.** Withdraw banked shards as **Unstable Skill Shards** from the skill tree, right-click one to bank it again. Nine compress into an **Unstable Skill Shard Block**, which also generates naturally in all three dimensions and can only be mined with a Netherite, Crystalline or Dragon pickaxe — anything less shatters it for nothing.
- **Stable Skill Shard Blocks** damage nearby hostile mobs, merge with adjacent blocks to widen that aura, work as a beacon base, and are immune to explosions.
- **Crates**, fished out of the water: Wooden, Copper, Iron and Diamond as a rarity ladder, plus **Frozen**, **Lush** and **Desert** biome variants. Opening one spins a slot-machine reel in front of you before it pays out.
- **Unboxing**, a datapack fishing-rod enchantment that raises your crate rate — and is itself found in crates and sold in the shop.
- The **Infusing Table** replaces the enchanting table. It reads enchanted books out of nearby **chiseled bookshelves**, offers exactly what those books hold, lets you pick several at once, and charges Skill Shards. Books are kept, not consumed (except Fortune IV+).
- **Enchanted books in the Quest Shop**, capped at level I–II and priced well above the rest of the catalogue. Buy two and combine them at an anvil.
- **Datapack content types**: `skill_category`, `skill_node`, `quest`, `shop_offer`, `feat` and `crate`. Files follow the vanilla tag format, so a pack adds, reprices, replaces or removes entries the same way it would extend a tag.
- **Gear balance is configurable.** A `gear` block in gameplay.json holds every tier's armour, toughness, knockback, movement modifier and durability, plus tool durability, attack damage/speed and pickaxe mining. Existing gear is brought onto retuned numbers as its owner logs in.
- Horse speed, jump height and health are shown when you open a horse's inventory.
- Your banked Skill Shards are displayed on the experience bar, which experience no longer uses.
- The recipe book fills the crafting grid for custom recipes.
- **The wandering trader buys from you**, paying in Skill Shards for iron, netherite scrap, crops and blocks. Offers keep vanilla's no-restock behaviour, and are priced to beat handing the same items in as a quest.
- **Iron blocks smelt into Steel in a blast furnace** as well as a furnace, at the usual double speed.
- The **Guide** icon opens the wiki as a clickable chat link. Clear `guideUrl` in gameplay.json to go back to the in-game book.
- **Skill Shard ore** and every other physical shard source are listed on the Earning Skill Shards screen, with rates read live from the config.

### Changed
- **Experience is removed from the game.** No orbs, no XP from mobs, mining, smelting, breeding or furnaces, and Bottles o' Enchanting no longer appear in loot or villager trades. Anvils charge **Skill Shards** instead of levels.
- **Anvils no longer put enchanted books onto tools and armour** — that is the Infusing Table's job now. Combining two books in an anvil still works, and still costs Skill Shards.
- **Steel armour** now costs a flat **-10% movement speed** on the full set (was -4%), to pay for its 18 armour. **Hardwood** rises to **+16%** (was +10%).
- **Crystalline** grants Strength and Resistance I on the full set, on top of its 25% melee reflect.
- **Rose Gold** gains fire resistance and can mine gold and iron. **Copper** tools can mine gold.
- **Recipes**: Steel Ingots are smelted (1 iron block to 3) rather than forged; the Steel Shield is forged in an anvil from a shield and a steel ingot; Crystallized Diamond takes Unstable Skill Shards; the Dragon Ingot needs 4 scales instead of 8; Fortune V needs a Stable Skill Shard Block top and bottom.
- **The Ender Dragon only drops scales to a player kill**, with a one-time bonus for the world's first, so a scripted startup kill cannot consume it.
- The skill tree is built from the datapack; `skilltree.json` is no longer authored in-game and an existing one is migrated into a datapack on first load.
- Quest progress and the bounty board are keyed by stable quest ids rather than list positions, so quests can be reordered or replaced without disturbing anyone's progress.
- Custom items identify themselves with `item_model` and `item_name` components instead of `custom_model_data`, which also stops custom gear showing a floating nameplate in an item frame.
- The **Unstable Skill Shard** is a written book and the shard blocks are **reinforced deepslate** and **lodestone**, taken over outright rather than drawn as overlays. Reinforced deepslate no longer generates in ancient cities, and lodestone's recipe is gone; both are obtained only through VanillaSkills.
- `gameplay.json` gains any options added since it was written, instead of silently leaving new settings out of the file.

### Removed
- `/skill open`, `/skill guide`, `/skill editor`, `/skill layout`, `/skill edit …` and `/skill regen`.
- `/skill points` is now **`/skill skillshards`**.

### Fixed
- The anvil screen said "Enchantment Cost" while charging Skill Shards.
- The Quest Shop showed offer names as a literal `%dx %s`.
- Compressing an Unstable Skill Shard Block back into nine shards did not craft.
- Shift-right-clicking a Stable Skill Shard Block onto another merged them instead of placing.
- Creepers were immune to the Stable Skill Shard Block's aura.
- The pushed texture pack now ships the same language files as the mod, so vanilla clients see translated item names rather than raw translation keys. A build step keeps the two copies in step.

### Translation
- English and Traditional Chinese are both complete at **710 keys**. Every quest, crate, feat, shop offer, skill lane and skill-node description is translatable, and node titles translate through their lane name.

## [1.7.6] - 2026-07-24
### Changed
- New **Crystallized Diamond** texture.

## [1.7.5] - 2026-07-22
### Added
- **Traditional Chinese (zh_tw) is now complete** — the 14 advancement titles and descriptions are translated, bringing zh_tw to full parity with English (612 keys). Thanks to caprese502 for the translation.
### Changed
- Bundled texture pack refreshed with the completed zh_tw language file, so vanilla clients receive the translated advancement and item names.

## [1.7.4] - 2026-07-21
### Added
- The **advancement screen** is now translatable — all 14 VanillaSkills advancement titles and descriptions use translation keys (English fallback). This was the last untranslated surface. en_us.json is now 612 keys.

## [1.7.3] - 2026-07-21
### Added
- The last hardcoded item text is now translatable: the Steel-Infused Shield name and lore, the Fortune and Dragon upgrade template lore, and the "+ Elytra" fusion label. Traditional Chinese (zh_tw) translations for these were provided by **caprese502** (Discord).
- Quest Shop offer names now show each item in the player's own language (they were built from a server-resolved English name before).

### Fixed
- The auto-pushed resource pack now includes these new strings, so vanilla clients see the translated item lore too.

## [1.7.2] - 2026-07-21
### Fixed
- **Custom item and gear names now translate for vanilla clients.** The server was auto-pushing an old resource pack (from before localization) that carried no language files, so on clients without the mod jar every custom item name fell back to English. The default pushed pack now points at the current localized pack, and servers still pinned to the old pack are auto-upgraded on load.

## [1.7.1] - 2026-07-21
### Added
- **Traditional Chinese (zh_tw)** translation — a complete 573/573 translation contributed by **caprese502** (Discord). Thank you!

### Fixed
- Two message keys took different arguments than translators would reasonably expect: `msg.mastered` now passes the Dragon Ingot count as `%d` instead of hardcoding it, and `msg.unlocked_one` now takes the same three arguments as `msg.unlocked_many` so the two can be translated identically.

## [1.7.0] - 2026-07-20
### Added
- **Full translation coverage.** Everything a player can see is now translatable: all tiered gear names (every Hardwood/Rose Gold/Steel/Crystalline/Dragon armour piece and tool), the alloy ingots, Dragon Ingot/Scale, upgrade templates, armour set-bonus tooltips, the bounty-board hologram, every remaining chat message, `/help`, and the Mod Menu config screen. `en_us.json` grows from 420 to 568 keys and now also ships inside the resource pack.

### Changed
- Item names and lore are baked into the item, so they use client-resolved translation keys **with English fallbacks** — translated for anyone with the resource pack, plain English (never raw keys) for everyone else.

## [1.6.5] - 2026-07-20
### Changed
- Moved the Quest Shop button on the Bounty Board back to the bottom-center, where it sat before 1.4.0.

## [1.6.4] - 2026-07-20
### Added
- Completed translation coverage: the "Earning Skill Shards" page, the "Your Stats" page (including attribute names and the Aquatic summary), every skill node description (`vanillaskills.node.<id>.desc`, overriding the per-world tree text), and the remaining unlock/refund/purchase chat messages are now all translatable. en_us.json grows from 256 to 420 keys.

## [1.6.3] - 2026-07-19
### Added
- Fortune V now has a flat **0.5% chance to drop a second Ancient Debris** when mining it (vanilla Fortune does nothing for debris). A rare bonus, not a reliable multiplier; requires the correct tool, not in creative, and respects the `fortuneBoost` config toggle.

## [1.6.2] - 2026-07-19
### Changed
- Warrior lane reworked: each tier now grants +0.5 flat AND +3% weapon damage (up to +5 and +30%) instead of +1 flat. The percentage scales with the weapon you hold, so swords stay ahead of an unarmed fist. Stacks with Sharpness as before.
- Aquatic swim-speed nodes now grant Dolphin's Grace, which actually speeds up swimming and stacks with the Depth Strider enchantment (the old water-movement bonus only affected walking on the seabed and was capped).

## [1.6.1] - 2026-07-07
### Added
- Completed the translation template: recipe names, all skill-lane header blurbs, and every Guide book page are now in en_us.json, and node titles auto-translate from their lane name (no per-tier keys needed). Added TRANSLATING.md. Community translations hot-reload with /skill reload.

## [1.6.0] - 2026-07-07
### Added
- **Language file support.** Menus, quests, feats and messages are now translated server-side per player, from that player's client language. Bundled en_us.json is the template; drop community translations at <world>/vanillaskills/lang/<locale>.json (server-only) or send them in to bundle. Falls back to English for any missing key.
- **Many new gameplay.json toggles:** toolCraftingRequirements / armorCraftingRequirements (disable = ungate crafting AND hide that lane), deepslateGate, fortuneBoost, questsPerRotation, questShopSlots, anvilTooExpensiveCap, dragonRepairCost, starterQuests (new players skip the starter board), and feats.
### Changed
- **Feats are now OFF by default** (removed as too strong). Re-enable with feats=true in gameplay.json if you want them back.

## [1.5.0] - 2026-07-18
### Added
- **Language support.** The mod now translates its UI per player, server-side, using the language each client reports. Bundled defaults live in the jar (assets/vanillaskills/lang/en_us.json, 180+ keys covering the Bounty Board, Feats tab, quests and feats); server owners can drop community translations (e.g. ru_ru.json) into <world>/vanillaskills/lang/ - no jar editing. Missing keys safely fall back to English. More screens (skill tree, shop, recipe book) get keys next update.
- **Config toggles for crafting requirements.** gameplay.json now has toolCraftingRequirements and armorCraftingRequirements (both default true). Set one false and that entire gate is off: anyone can craft any tier of that class, and the Toolsmith/Armorsmith lane disappears from the skill tree (ops still see it in edit mode; full-tree completion adjusts so it stays reachable).

## [1.4.6] - 2026-07-07
### Changed
- **Removed the anvil's "Too Expensive" cap for every operation.** Costs still scale exactly like vanilla (prior-work penalty untouched) - they just never block the result, however high they climb. You need the levels, you pay the levels. (Vanilla clients may still see the red "Too Expensive" label above 40 - cosmetic only; the result is there and takeable.) The Dragon flat repair (1 ingot + 20 levels) is separate and unchanged.

## [1.4.5] - 2026-07-07
### Changed
- **Dragon gear anvil repair is now a flat rate:** put a Dragon tool or armor piece in an anvil with 1 Dragon Ingot and it FULLY repairs for exactly 20 levels. No prior-work scaling, no "Too Expensive" cap, exactly one ingot consumed per repair.

## [1.4.4] - 2026-07-07
### Changed
- Fortune IV and V are now real upgrades on ores. Vanilla only nudges the average per level (diamond ore: III ~2.2x, IV ~2.5x, V ~2.8x), so each level above III now also grants one guaranteed extra base drop (IV = +1, V = +2). Diamond ore now averages ~3.5x at IV and ~4.8x at V. Requires the correct tool; applies to all standard ores.

## [1.4.3] - 2026-07-06
### Fixed
- Stat quests (walk/swim/jump) now actually track progress. The stat read passed a freshly-parsed identifier that did not match Minecraft's identity-keyed stat map, so it always returned 0 and progress never moved. It now resolves to the registered stat object first. (Pairs with the 1.4.2 completion ping.)

## [1.4.2] - 2026-07-06
### Changed
- Stat quests (walk/swim/jump) now ping you in chat the moment you complete one ("Bounty ready to claim ... /quests"). They always tallied in the background, but there was no feedback while doing it — now there is. Open the board to see live progress, or just keep going and wait for the ping.

## [1.4.1] - 2026-07-06
### Fixed
- The Fortune Upgrade and Dragon Upgrade templates found in chest loot now show their proper custom texture and description. They were missing the model + lore components, so a chest-found one looked like the plain base item (echo shard / netherite template). The recipe-book/crafted versions were already correct.

## [1.4.0] - 2026-07-06
### Changed
- The rotating bounty board now deals **6 quests per rotation** (was 3), shown in a 3-3 grid below the clock. Still random with no duplicates in a rotation.
- Moved the **Quest Shop** button to the top-left of the board.
- **Rebalanced rewards** for the bigger board: rotating quests -2 Quest Shards each (min 1), starter quests -1 each (min 1).

## [1.3.4] - 2026-07-06
### Changed
- Skill-branch node layouts now leave an empty separator row between the branch header and the skills (row 1), so the header reads clearly as a description rather than a clickable skill. Still centered. Ops: run /skill regen fresh to apply.

## [1.3.3] - 2026-07-06
### Changed
- Feats tab reflowed to a 5-5-1 centered layout for a more even look.
- Skill-branch node layouts unified onto one centered grid (rows of up to five, centered, starting right under the branch header) so every branch looks even and centered with no gap under the description. Ops: run /skill regen fresh once to apply the new layout to an existing world (player unlocks are kept).
- Gather 32 Emeralds now rewards 5 Quest Shards (was 10).
### Fixed
- Structure-discovery Feats now only trigger when you are actually inside the structure. They previously fired from far away because the check matched the structure's oversized bounding box instead of its rooms.
- Fortune Finder no longer places blank (un-enchanted) enchanted books as bonus loot.

## [1.3.2] - 2026-07-06
### Added
- Opening a skill branch now shows a header at the top with the branch icon, its name, and a hover description of what that skill actually does, plus your unlock progress.

## [1.3.1] - 2026-07-06
### Changed
- Moved the Feats button to the top-right of the Bounty Board, and laid the Feats tab out in a tidy 4-4-3 grid (bosses & the End on top, then overworld discoveries, then nether/end discoveries).

## [1.3.0] - 2026-07-06
### Added
- **Repeatable stat quests** on the bounty board: Travel 5,000 blocks, Swim 1,500 blocks, and Jump 800 times. Progress counts from the moment the quest appears (baseline snapshot per rotation), so it is repeatable each window and veterans do not auto-complete.
- **Feats tab** on the Bounty Board: a checklist of one-time achievements that auto-award Quest Shards. Includes boss kills (Ender Dragon, Wither, Warden), entering The End, and discovering structures (Ancient City, Woodland Mansion, Trial Chamber, Ocean Monument, Bastion, Nether Fortress, End City). Fully server-side; never repeats once earned.

## [1.2.9] - 2026-07-06
### Changed
- Rebalanced Quest Shard rewards across the starter and rotating boards. Highlights: Unlock 10 Skills 5 -> 10; Gather 10 Diamonds 9 -> 15 and now a rarer quest; the emerald quest is now Gather 32 Emeralds for 10; Ghast Tears 8 -> 16; Witches 6 -> 12; plus tweaks to leather, pumpkins, cocoa, nether wart, chorus, quartz, amethyst, copper, raw iron, obsidian, lapis, honey, tropical fish, nautilus shells, and the 50-mob quest. Starter full-completion payout is now 50 QS.

## [1.2.8] - 2026-07-05
### Added
- 20 new bounty-board quests: Cultivator crops (pumpkins, melon slices, sugar cane, sweet berries, cocoa beans, nether wart, chorus fruit), Nether materials (blaze rods, quartz, ghast tears, magma cream, ancient debris), Ocean materials (prismarine shards, nautilus shells, kelp, ink sacs), and raw mining drops (raw iron, deepslate, obsidian, lapis lazuli).

## [1.2.7] - 2026-07-05
### Changed
- **Crystallized Diamond recipe** reshaped: amethyst shards fill the top & bottom rows, diamonds flank the amethyst block in the middle (AAA / DBD / AAA). Now 6 shards + 2 diamonds + 1 amethyst block (was 4 + 4 + 1); still yields 2.

## [1.2.6] - 2026-07-04
### Changed
- **Dragon tool durability raised 2500 -> 3500** (~72% more than netherite, up from ~23%).

## [1.2.5] - 2026-07-04
### Added
- **The skill tree's Recipes book now starts with "Hardwood"** — shows how to make the base
  material (4 logs -> Wood blocks) and what it is for (craft tools/armor in their normal shape with
  Wood blocks instead of planks). Recipe entries can now carry a short "what it is for" description.

## [1.2.4] - 2026-07-04
### Fixed
- **Fixed a Cultivator dup exploit.** You could place a melon, pumpkin, sugar cane, or cactus and
  break it for a net-positive bonus (infinite items). Now:
  - **Melon & pumpkin** only give a bonus when they actually GREW (an attached stem is facing the
    fruit) — placed ones give nothing.
  - **Sugar cane & cactus** no longer give a Cultivator bonus at all (placing is identical to
    harvesting, with no reliable placed-vs-grown signal; they are also bulk auto-farm crops).
  - Wheat, carrots, potatoes, beetroot, nether wart, cocoa, chorus, and the berries are unchanged
    (already un-dupable: they need a growth stage, or the harvestable block cannot be placed).

## [1.2.3] - 2026-07-04
### Fixed
- **Fixed the lag introduced in 1.2.2.** The step-up sneak-suppression reacted to every flicker of
  the sneak flag, which could churn the step-height attribute every tick (a sync packet per tick →
  stutter). It is now debounced — the attribute changes at most once per real sneak/stand
  transition — so the safety behaviour is unchanged but the per-tick cost is gone.

## [1.2.2] - 2026-07-04
### Changed
- **Step-up (Mountaineer) is now suppressed while sneaking** — hold shift and you step like vanilla,
  so you can carefully edge up to ledges/lava instead of auto-stepping into danger.
- **Toggle commands unified under `/skill toggle <skill>`:** `/skill toggle nightvision` (was
  `/skill nightvision`) and the new **`/skill toggle stepup`** to disable the step-up bonus entirely.

## [1.2.1] - 2026-07-04
### Changed
- **Lowered every starter-quest reward by 1 Quest Shard** (total onboarding payout 62 -> 47).

## [1.2.0] - 2026-07-04
### Changed
- **Starter quests reworked: 15 fixed quests, always available.** New players now see all 15
  starter quests at once (no rotation, complete in any order, each once): sticks, cobblestone,
  coal, bread, leather, the copper->iron->gold->diamond mining ladder, zombies/skeletons/creepers,
  bones, string, and finally **Unlock 10 Skills**. Finishing all 15 graduates you to the shared
  rotating board. The starter GUI is now a full 4-row board showing live progress on everything.
- **Players who were mid-starter are reset to 0 starter quests done** (fresh start on the new
  board; earned shards are kept). Graduated players are unaffected.
### Added
- **`/skill nightvision` — toggle an unlocked Night Vision capstone on or off.** Per player,
  persists across logins. Turning it off removes the effect instantly; turning it on re-applies it
  instantly. Players who have not unlocked the capstone get a friendly error.
### Fixed
- **Grid repair-combining no longer destroys custom gear.** Combining two damaged custom
  tools/armor in a crafting grid produced a fresh vanilla item (marker, name, and look stripped).
  The grid combine now simply does not accept marked gear - use the anvil to merge or repair it.
### Removed
- **The redundant /skills alias** - /skill is the command.

_(1.1.2 was withdrawn — its Wind Burst "fix" addressed a non-bug: Spawn Manager cancels all
explosions at spawn, which includes Wind Burst launches. Wind Burst works normally outside the
spawn-protection radius.)_

## [1.1.1] - 2026-07-03
### Added
- **7 new bounty quests:** Gather 64 Carrots (4), 4 Honey Bottles (6), 24 Amethyst Shards (5),
  32 String (4); Slay 15 Slimes (5), 10 Pillagers (6), and 8 Guardians (8, post-graduation).
### Changed
- **Fishing quests appear about half as often.** All four fish quests dropped to half weight —
  they were one activity with four full-weight pool entries, so roughly every other starter board
  had one. With the new quests diluting the pool too, fishing lands at ~8% of quest slots.

## [1.1.0] - 2026-07-03
### Added
- **Fortune Finder now improves ALL naturally generated loot.** The Luck attribute (+0.5 per
  Fortune Finder node, +5 maxed) grants bonus items from natural containers: **+1 bonus item per
  2.5 luck** (+2 when maxed), rolled from the container's own loot table and placed in empty
  slots. Covers chests, barrels, chest minecarts, chest boats, trial-chamber corridor loot — and
  **trial-chamber vaults** eject bonus reward items at the same rate. Fishing luck unchanged
  (still applies). Bonus items can include the Fortune/Dragon upgrade templates.

## [1.0.9] - 2026-07-03
### Changed
- **Cultivator now also rewards sweet berries and glow berries.** Both are right-click harvests
  (no block-break event), so they are hooked at the harvest interaction itself: picking a berry
  bush (age 2+) or a berry-bearing cave vine rolls the same per-level 50% bonus, dropping extra
  sweet/glow berries. Works identically on Fabric and NeoForge (shared mixins).

## [1.0.8] - 2026-07-03
### Changed
- **Cultivator rewards more crops.** The bonus-harvest roll now also triggers on **cocoa (mature)**,
  **melon** (bonus slices), **pumpkin** (capped at **+2** pumpkins per break), **sugar cane**,
  **cactus**, and **chorus plant** (bonus chorus fruit) — in addition to the existing
  wheat/carrots/potatoes/beetroot/nether wart. Immature crops still give nothing.

## [1.0.7] - 2026-06-28

### Changed
- **Resource-pack auto-push now skips solo single-player.** A solo player already has the textures
  bundled in the jar, so there's no reason to prompt them. The server still pushes the pack on
  **LAN-opened worlds and dedicated servers** (where vanilla clients can join), so nothing changes
  there — playing alone just no longer shows a redundant "accept pack" prompt.

## [1.0.6] - 2026-06-28

### Added
- **Server auto-pushes the texture pack to joining clients** (on by default). The server now sends the
  VanillaSkills texture pack to every player on join (required), so **vanilla clients see the custom gear
  with no `server.properties` setup**. Configurable per-world in `gameplay.json` (`serverResourcePack`,
  `resourcePackUrl`, `resourcePackSha1`); set `serverResourcePack: false` to disable (e.g. if you deliver
  the pack via `server.properties` yourself). Modded/modpack clients already have the textures bundled —
  this is for vanilla clients.

## [1.0.5] - 2026-06-28

### Added
- **`/skill mending on|off`** (ops, permission level 2) — toggle Mending for the current world without
  hand-editing files. Saves to the world's `gameplay.json` and prompts to **restart the world/server**
  for it to take effect (existing villager trades don't change — reroll librarians for new offers). Fixes
  the gap where server admins had no in-game way to enable Mending (the Mod Menu screen is
  client/singleplayer-only) and the config lives inside the world save.

## [1.0.4] - 2026-06-28

### Added
- **Optional "Disable Narrator" toggle** (Mod Menu config → "Narrator"; **off by default**). On Windows,
  Minecraft's narrator backend makes a slow native text-to-speech call **on the render thread every time a
  screen opens** (chest, crafting, furnace, even the options menu) — stalling it for tens of milliseconds.
  The vanilla "Off" narrator setting only silences speech; it doesn't stop that call. Enabling this stubs
  the narrator entirely for noticeably smoother screen-opening. **Off by default** so accessibility users
  and vanilla behaviour are completely unaffected unless a player opts in. Client-side only; stored in
  `config/vanillaskills-client.json`. No gameplay changes.

## [1.0.3] - 2026-06-26

### Fixed
- **Custom-tier armor (Crystalline, Rose Gold, Dragon) can now be trimmed in the smithing table.**
  1.0.2 restored trims for *vanilla* armor, but our custom tiers still produced a red X. The cause was
  upstream of the trim code: our armor is a marked vanilla item, and the mod deliberately hides every
  marked item from vanilla recipe ingredients (so e.g. a Steel ingot can't satisfy the vanilla iron-block
  recipe). That same block made the trim recipe's base-ingredient check reject our armor, so no recipe
  matched and the result cleared. Trims now apply to custom armor through a dedicated handler that matches
  on the template + material and trims the marked base directly — without unblocking the marker globally
  (which would have allowed broken Netherite "upgrades" of custom armor). Custom pieces keep their tier
  art on the inventory icon; the trim renders when worn.

## [1.0.2] - 2026-06-26

### Fixed
- **Armor trims actually work now** (1.0.1's attempt didn't take effect). Two real causes:
  - The smithing craft-gate kept clearing trim results — the 1.0.1 guard bound to the wrong
    `ItemStack.is` overload and never fired. It now compares the base and result item directly, so trims
    apply to any armor (vanilla or custom) while genuine tier upgrades (diamond → netherite) stay gated.
  - Our armor item-model overrides had replaced vanilla's trim-icon handling, so trimmed armor showed no
    trim in the inventory — including plain vanilla armor (a regression we'd introduced). The overrides now
    nest vanilla's `trim_material` model as the fallback, restoring inventory trim icons for all vanilla
    armor. (Custom-tier pieces still show their tier art on the icon; trims render when worn.)

## [1.0.1] - 2026-06-26

### Fixed
- **Armor trims wouldn't apply to gated-tier armor** (custom tiers like Crystalline, and vanilla
  iron/diamond/etc.). The smithing-table craft gate was clearing the result because a trimmed piece is
  still a gated tier. The gate now only blocks true tier *upgrades* (where the item type changes, e.g.
  diamond → netherite), so trimming any armor works and keeps the piece's tier, stats, and worn texture.

## [1.0.0] - 2026-06-26

First stable release.

- **Jars are now named with the Minecraft version** — e.g. `vanillaskills-1.0.0+mc26.2.jar`.
- Headline additions since the 0.19 series: **per-world** config + skill tree, an in-game **Mod Menu
  config screen**, **client keybinds** (`]` skill tree / `[` bounty board), configurable pacing/economy
  (refresh intervals, convert ratio, graduation count, Mending toggle), the reorganized skill-tree home
  with Guide/Bounty shortcuts, and a fully revised guide book.

See the 0.19.x entries below for the detailed history leading here.

## [0.19.24] - 2026-06-26

### Fixed
- **Guide book overhaul.** Long pages were overflowing the written-book page limit and getting cut off —
  split them so every page fits. Also fixed stale/incorrect recipe info:
  - **Steel** ingot is forged **iron + iron in an anvil** (was wrongly listed as "2 iron + 1 coal").
  - **Crystallized Diamond** is **4 diamonds + 4 amethyst shards + 1 amethyst block = 2** (was "+ 1
    amethyst block = 4").
  - **Hardwood** armor is crafted from **Wood blocks** (the all-bark kind, like Oak Wood) — clarified it's
    not logs or planks.
  - The graduation quest count and the converter ratio now reflect the live config values.

## [0.19.23] - 2026-06-26

### Changed
- **Settings and the skill tree are now stored PER-WORLD** instead of in the shared instance config
  folder. `gameplay.json`, `points.json`, and `skilltree.json` now live in the world save
  (`<world>/vanillaskills/`), alongside the already-per-world player progress and quest data. So every
  single-player world — and every server world — can have its own settings, economy, and tree layout.
- The Mod Menu config screen now edits the **currently loaded** world's settings; with no world open it
  shows a notice (settings are per-world, so there's nothing global to edit there).

Note: existing worlds generate fresh per-world config/tree files from the defaults on first load with this
version (player progress and quests are unaffected — those were already per-world).

## [0.19.22] - 2026-06-26

### Added
- **Four new pacing/economy settings in `gameplay.json`** (server-side, no cheats needed):
  - `bountyRefreshHours` — hours between bounty-board rotations (default 5).
  - `shopRefreshHours` — hours between Quest Shop rotations (default 24).
  - `convertRatio` — Quest Shards per 1 Skill Shard at the converter (default 3).
  - `graduateAt` — quests to finish on the starter board before joining the main board (default 15).
- **The Mod Menu config screen now exposes all of these** as click-to-cycle buttons, alongside the
  Mending toggle. In singleplayer changes apply on close; on a server the server's config governs and
  applies on load / `/skill reload`.

## [0.19.21] - 2026-06-26

### Added
- **Mod Menu config screen.** With Mod Menu installed client-side, the VanillaSkills entry now has a
  Config button that opens a settings screen — currently a Mending toggle. It edits the local
  `config/vanillaskills/gameplay.json`; in singleplayer the change applies immediately. (On a multiplayer
  server the server's config is authoritative, so this only changes your own copy.) Mod Menu is optional —
  the integration is a no-op if it isn't installed.

## [0.19.20] - 2026-06-26

### Changed
- **Bounty Board's skill-tree button is no longer worded as a "return".** Since the bounty board can be
  opened directly (via `/quests` or its keybind), the nether-star button now reads "Skill Tree" / "Open
  the skill tree" instead of "Back to Skills" / "Return to the skill tree".

## [0.19.19] - 2026-06-26

### Changed
- **Default keybinds changed to the bracket keys** (both unbound in vanilla, unlike the commonly-used
  B/V): **Open Skill Tree → `]`**, **Open Bounty Board → `[`**. Still rebindable under Options →
  Controls → VanillaSkills.

## [0.19.18] - 2026-06-26

### Changed
- **Skill-tree home screen reorganized.** Night Vision and the Recipes (crafting) button swapped places,
  and Recipes now sits under Armorsmith. Two new movable buttons join that row: a **Guide** book under
  Toolsmith (opens the `/skill guide` book) and a **Bounty Board** clock under Night Vision (opens the
  quest screen). Like Recipes, both are pseudo-lanes — ops can reposition them in `/skill layout`.
- **The Bounty Board now has a nether star in the bottom-left to return to the skill tree.**

Note: the new arrangement is the default layout — existing worlds keep their saved tree until
`/skill regen fresh` is run.

## [0.19.17] - 2026-06-25

### Changed
- **The shard counter on the skill screen now shows both currencies.** Hovering the counter (bottom-left)
  now lists your **Skill Shards** and **Quest Shards** together (titled "Your Shards"), instead of only
  Skill Shards.

## [0.19.16] - 2026-06-25

### Added
- **Client keybinds for the Skill Tree and Bounty Board** (for players who install the mod on their
  client). Two rebindable keys under Options → Controls → "VanillaSkills": **Open Skill Tree** (default
  **K**) and **Open Bounty Board** (default **B**). They run the server's `/skill` and `/quests`, and only
  fire when the connected server is actually running VanillaSkills, so on a vanilla server they do nothing
  (no "Unknown command"). Vanilla clients are unaffected — they just use the commands as before.

## [0.19.15] - 2026-06-25

### Fixed
- **Vanilla (undyed) leather armor showed white in the inventory.** Overriding the leather item models
  to add the Hardwood custom-model-data `select` dropped vanilla's dye `tints`, so the dyeable base layer
  rendered untinted (white). The fallback models for all four leather pieces now carry the leather dye
  tint (`minecraft:dye`, default `-6265536`), so undyed and dyed leather render correctly again. Worn
  armor was always fine (it uses the separate equipment texture). Fix applied to both the bundled jar
  assets and the standalone texture pack.

## [0.19.14] - 2026-06-23

### Added
- **Mending removal is now configurable.** New `config/vanillaskills/gameplay.json` with a
  `mendingEnabled` flag (default `false` = Mending removed, as before). Set it to `true` to leave
  Mending in the game. Applies live on `/skill reload` (no restart needed); the guide book's Mending
  line reflects the current setting.

## [0.19.13] - 2026-06-23

### Changed
- **"Skill Master" completion reward is now 4 Dragon Ingots (was 5).** Both the granted amount and all
  the wording (advancement description, the in-game "Skill tree mastered!" message) now say 4.

## [0.19.12] - 2026-06-23

### Fixed
- **Advancement-tree icons now show the custom gear textures instead of vanilla ones.** Each custom
  advancement icon was a plain base item (`iron_chestplate`, `netherite_chestplate`, `gold_ingot`, …),
  so it rendered with the vanilla texture. The armor-set, alloy, dragon-ingot/template, fortune-template,
  and steel-shield advancements now carry the matching `custom_model_data` hook (and the steel shield its
  banner-pattern components), so they display the real custom art. Generic icons (root book, Specialist
  XP bottle, armored elytra) are unchanged. Requires the textures to be present client-side (bundled in
  the mod jar, or via the standalone texture pack for vanilla clients).

## [0.19.11] - 2026-06-23

### Changed
- **Recipe book pages now show the crafting station at the top instead of a green book.** Normal
  recipes display a **crafting table**; the Steel Ingot page displays an **anvil** (since it's
  anvil-forged). Hover text is unchanged — the top icon still just names the recipe.
- **Steel Ingot recipe page no longer shows an anvil in the middle of the grid** — the anvil moved to
  the station icon at the top, so the grid reads simply as iron + iron.

## [0.19.10] - 2026-06-23

### Changed
- **GUI blank spots are now truly empty instead of glass panes.** The bounty board, Quest Shop,
  skill-info / player-info screens, and the recipe book previously filled their empty slots with
  light-gray (and black) stained-glass panes. Resource packs that reskin glass panes ruined that
  effect, so those slots are now left empty — exactly like the skill tree. Clicks are still fully
  intercepted, so items can never be placed into the blank slots.

## [0.19.9] - 2026-06-23

### Changed
- **Crystallized Diamond is now a shaped recipe** — 4 diamonds (corners), 4 amethyst shards (edges),
  1 amethyst block (center) → 2 Crystallized Diamonds.

## [0.19.8] - 2026-06-23

### Changed
- **Crystallized Diamond recipe now yields 2** (was 4) per craft (4 diamonds + 1 amethyst block).

## [0.19.7] - 2026-06-23

### Fixed
- **Steel-Infused Shield from the creative menu now renders steel** on multiplayer servers. The shield's
  banner pattern was pulled from `VanillaSkills.server`, which is null on a client connected to a remote
  server, so a creative-grabbed shield was built with no pattern (plain wooden). `SteelShield.create`
  now takes the registry access from the caller, and the creative tab passes the connection's registries.
  (Crafted shields were already fine — they're built server-side.)

## [0.19.6] - 2026-06-23

### Changed
- **Finalized the default skill-tree layout** for release: Night Vision moved to the bottom-centre
  (slot 49) and the Recipes lane to slot 40 (between Armorsmith and Toolsmith). New worlds/servers get
  this arrangement; existing trees keep theirs (run `/skill regen fresh` to adopt it).

## [0.19.5] - 2026-06-23

### Fixed
- **Armorsmith/Toolsmith lane icons now show the custom tier textures.** The VanillaSkills tiers
  (Hardwood, Rose Gold, Steel, Crystalline, Dragon) in those Quest-Shard ladders were showing the
  vanilla base armor/tool textures; their skill-tree nodes now use the real marked tier item so the
  resource pack retextures them. Vanilla tiers (Copper/Gold/Iron/Diamond/Netherite) stay vanilla.

## [0.19.4] - 2026-06-23

### Fixed
- **Steel-Infused Shield tooltip** no longer shows the raw `block.vanillaskills.banner.steel.white`
  line. The banner pattern (used only to render the shield as steel) is now hidden from the tooltip
  via the `tooltip_display` component; the shield still renders steel.

## [0.19.3] - 2026-06-23

### Fixed
- **Anvil steel forging now consumes exactly one iron from each slot.** It was consuming the entire
  left stack (vanilla's onTake clears the left input slot) because the consume-override bailed on an
  unreliable check of the taken result; it now keys off the iron-in-both-inputs state.

## [0.19.2] - 2026-06-23

### Changed
- **Steel is now forged in an anvil**, not crafted. Put a plain iron ingot in each of the anvil's two
  input slots to forge a Steel Ingot — **no experience cost** — consuming one iron from each slot per
  take (so a stack can be forged one at a time). The old crafting-table recipe (2 iron + 1 coal) is
  **removed**. The in-game Recipes book now shows the anvil method for steel.

## [0.19.0] - 2026-06-23

### Changed
- **Textures are now bundled in the mod jar.** The full custom texture set (all tier armor/tool/spear
  icons, worn-armor layers, ingots/materials, the steel shield banner texture, and the fortune
  template) ships inside the jar, so players who install the mod see the custom look automatically —
  no separate resource pack needed. The mod's bundled equipment JSONs now point at the custom
  `vanillaskills:<tier>` worn-armor textures instead of the base-material fallback.
- The standalone resource pack is still published separately for **vanilla clients on a server**
  (server-side install), where the pack is delivered to clients that don't have the mod.

## [0.18.2] - 2026-06-23

### Changed
- **Steel-Infused Shield now renders steel via a custom banner pattern** instead of a custom item
  model. The vanilla shield renderer draws bannered shields in full 3D (inventory, held, blocking),
  so the steel shield looks like a real shield everywhere while plain (un-bannered) shields stay
  wooden — both look correct. Adds `vanillaskills:steel` banner pattern (data) + its shield-atlas
  texture (resource pack `entity/shield/steel.png`). Dropped the custom_model_data + the pack's
  custom shield model/icon/override.

## [0.18.1] - 2026-06-19

### Changed
- **New mod icon** — a pixel-art sword ringed with sparkles (replaces the old skill-tree icon).
  Resource pack `pack.png` updated to match.

## [0.18.0] - 2026-06-16

### Changed
- **Updated to Minecraft 26.2.** Fabric loader 0.19.2 → 0.19.3, Fabric API 0.148.2+26.1.2 →
  0.152.1+26.2, `minecraft` dependency `~26.1` → `~26.2`. (Built against the new 26.2 mappings.)

## [0.17.6] - 2026-06-11

### Changed
- **Creative tab icon** is now the **Dragon Upgrade template** (was the Rose Gold Ingot). With the
  resource pack installed it shows the custom dragon_template art.

## [0.17.5] - 2026-06-09

### Changed
- **Tool durability made consistent with vanilla's order** (like the armor pass): Hardwood **200 → 160**
  (now between Stone 131 and Copper 190, no longer beating Copper), Rose Gold **250 → 220** (between
  Copper 190 and Iron 250, no longer tying Iron). Steel/Crystalline/Dragon already interpolated.
  Damage ladder unchanged (sword 5 / 5.5 / 6.5 / 7.5 / 9.5). Rose Gold keeps gold's top mining speed.

## [0.17.4] - 2026-06-09

### Changed
- **Hardwood armor weakened to sit below Copper** (following vanilla's actual power order rather than a
  strict tier list): **9 armor / ×9 durability** (was 11 / ×12), between Leather and Copper. Keeps its
  +10% movement set bonus.
- Rose Gold left as-is (gold-speed, higher durability, negative-effect-immunity set bonus — no extra
  buffs needed).

## [0.17.3] - 2026-06-09

### Changed
- **Steel armor toughness removed** (1 → 0). Toughness is now a premium stat reserved for **Crystalline
  and above** — Hardwood/Rose Gold/Steel have none.

## [0.17.2] - 2026-06-09

### Fixed (full gear ladder)
- Audited every custom tier against the real vanilla stats so each sits between its neighbours in the
  order Leather < Chain < Hardwood < Copper < Gold < Rose Gold < Iron < Steel < Diamond < Crystalline <
  Netherite < Dragon:
  - **Steel armor toughness 2 → 1** (was identical to Diamond's 2; now between Iron 0 and Diamond 2).
  - **Hardwood** armor durability ×11 → ×12, **Rose Gold** ×15 → ×13 (no longer iron-level), so durability
    climbs through the ladder.
  - **Tool damage:** Hardwood is stone-based (sword 5) and was out-damaging gold-based Rose Gold —
    Hardwood bonus 0.5→0, Rose Gold 1.0→1.5, giving an ascending sword ladder
    5.0 → 5.5 → 6.5 → 7.5 → 9.5 (Hardwood/Rose Gold/Steel/Crystalline/Dragon) between the vanilla tiers.

## [0.17.1] - 2026-06-09

### Fixed (high-tier gear stats)
- **Crystalline armor** no longer beats Netherite: its knockback resistance is now **0.05** (was 0.125,
  which exceeded Netherite/Dragon's 0.1). It now sits correctly between Diamond and Netherite
  (20 armor / 2.5 toughness / 0.05 kb).
- **Dragon armor** is now clearly the **top tier**: **+1 armor per piece (24 total vs 20)**, toughness
  **4** (was 3, identical to Netherite), and knockback **0.15** (was 0.1).
- **Tool damage ladder** cleaned up so each custom tier sits between the right vanilla tiers (sword
  damage): Hardwood 4.5 · Rose Gold 5 · Steel 6.5 · Crystalline 7.5 · Dragon 9.5 (vs Iron 6, Diamond 7,
  Netherite 8). Rose Gold +0.5→+1, Steel/Crystalline +1→+0.5.
  - Note: stats are stamped at craft time — re-craft existing gear to get the new numbers.

### Changed
- The **floating bounty board** no longer lists specific quests (those are now per-player) — it shows a
  generic "Right-click to view your bounties" prompt + the reset countdown, so starter-board players
  aren't confused by quests they can't see yet.

## [0.17.0] - 2026-06-09

### Changed (bounty board redesign)
- **Personal starter board → graduation.** The server-wide 150-hour noob timer is gone. New players now
  get their **own** random board of early-game quests (the non-lategame pool). After completing **15
  quests** they **graduate** to the shared **universal board**, which has **every quest** unlocked and
  is the prominent, persistent one. Both boards share the same 5-hour rotation.
- The quest GUI shows your **starter progress (X/15)** until you graduate, then the main board.
- Replaced `/quests noobtimer` with **`/quests graduate <player>`** and **`/quests starter <player>`**
  (ops) for moving players between the two boards.

## [0.16.12] - 2026-06-09

### Fixed
- **Back arrow restored in lane view.** Moving Skill Points to the bottom-left had collided with the
  Back button, hiding it (and making the Points slot act as Back). Back is now its own bottom-centre
  button; Points/Stats stay in the corners.

### Changed
- **Removed player skill refunds.** Skill unlocks are now permanent — there's no right-click refund, so
  players can't swap perks back and forth to dodge doing the advancements. (Ops can still `/skill reset
  <player>` for a full respec.)

## [0.16.11] - 2026-06-09

### Changed
- Recipe book: shifted the arrow and result one slot left so the recipe is centred with even (2-wide)
  margins on both sides.

## [0.16.10] - 2026-06-09

### Added
- **Fishing bounties** to promote fishing: Catch 16 Cod, Catch 16 Salmon, Catch 6 Tropical Fish, and
  Catch 4 Pufferfish (turned in at the board for Quest Shards).

## [0.16.9] - 2026-06-09

### Changed
- **Guide book refresh** (`/skill guide`): rewritten to cover the current systems — the two currencies
  (Skill Shards / Quest Shards), the bounty board + Quest Shop + converter, the noob window, the
  Quest-Shard crafting ladders that gate every tier, the deepslate gate, the corrected Brewmaster cap
  (+50%), and a pointer to the in-game Recipes book. Removed stale pages.

## [0.16.8] - 2026-06-09

### Added
- Restored the **✦ Skills ✦ nether-star header** at the top of the lane-select screen (without the
  old divider strip).
- **`/skill regenpoints`** (op) — resets `points.json` (the advancement point values) to the current
  built-in defaults, backing up the old file. Use this when the Skill-Shard info tab / totals still
  show old advancement values because an existing `points.json` was never updated.

## [0.16.7] - 2026-06-09

### Changed
- Recipe book order: Rose Gold Ingot → Steel Ingot → Crystallized Diamond → Steel Shield → Fortune
  Upgrade Template → Fortune IV Book → Fortune V Book → Dragon Ingot → Dragon Upgrade Template.

## [0.16.6] - 2026-06-09

### Added
- **In-game recipe book.** A movable **Recipes** lane (crafting-table icon) on the skill screen opens a
  paginated book of the mod's custom recipes — one 3x3 layout per page with the result: Dragon Ingot,
  Steel Shield, Dragon/Fortune upgrade templates, Fortune IV & V books, Rose Gold Ingot, Steel Ingot,
  Crystallized Diamond. Page through with Prev/Next.

### Changed
- **Skill screen layout:** Skill Points moved to the **bottom-left** corner, the Stats head to the
  **bottom-right** corner. **Toolsmith** now uses an **anvil** icon. Removed the purple divider strip
  through the middle of the lane-select screen (currency is still shown by the purple lane names).

## [0.16.5] - 2026-06-09

### Added
- **Your Stats: Aquatic summary** — a new entry showing how much of the Aquatic lane you've unlocked:
  **Breaths**, **Swim Speed**, and **Mine Speed** (computed from your unlocked Aquatic nodes).
- **`/help`** — lists the player-facing VanillaSkills commands; **`/help admin`** (op) lists those plus
  all the op/admin commands.

### Changed
- **`/skill regen` now preserves op changes.** Once an op customizes the tree (layout, edits), those
  are concrete: a normal `/skill regen` keeps the existing tree exactly and only appends brand-new
  lanes/nodes from the latest built-in default. **`/skill regen fresh`** does the old full reset to the
  built-in default (use this once to pull a big balance/structure update).

### Upgrade note
- To pick up the new sectioned layout + 10-tier Quest-Shard crafting lanes, run **`/skill regen fresh`
  once**. After that, your layout/edits survive a plain `/skill regen`.

## [0.16.4] - 2026-06-09

### Added
- **`/skill layout`** (op) — opens the lane-select screen in a drag-to-arrange mode: click a lane to
  pick it up, then click an empty spot to move it or another lane to swap them. Saves automatically.
  Lets you design the layout in-game instead of editing coordinates by hand.

### Changed
- **Sectioned lane-select screen.** The home screen is now framed and split into a **Skills (Skill
  Shards)** zone up top and a divider-lined **Crafting (Quest Shards)** strip below, with the
  Armorsmith/Toolsmith lane names tinted purple — so the two currencies read at a glance. Each lane
  tooltip now states which currency it uses.

## [0.16.3] - 2026-06-09

### Added
- **Freebie quest:** a rare "Daily Bonus" bounty worth **3 Quest Shards**, claimable instantly. The
  board now supports per-quest **weighting**, and the freebie is weighted to appear less often.
- **Early-game "noob" window:** for the first **150 hours of cumulative server-active time**, the
  harder quests stay hidden from the board — **Ender Pearls, Emeralds, Endermen, Blazes, Piglins,
  Witches, Drowned** (Diamonds stay available so players can still farm/sell them). After 150h the
  full pool unlocks (the board rerolls automatically when the window ends).
- **`/quests noobtimer`** shows time left in the early-game window; **`/quests noobtimer reset`** (op)
  resets it back to the full 150 hours and rerolls.

## [0.16.2] - 2026-06-09

### Added (Phase 3 — Quest-Shard crafting ladders)
- **Armorsmith & Toolsmith are now 10-tier ladders paid in Quest Shards** (not Skill Shards), climbing
  **Hardwood → Copper → Gold → Rose Gold → Iron → Steel → Diamond → Crystalline → Netherite → Dragon**.
  Each node shows its tier's item icon and a purple "Cost: X Quest Shards".
- **Crafting every gear tier is now gated** — including the **vanilla** tiers (copper/gold/iron/diamond/
  netherite). You can't craft a tier's armor or tools until you've bought its node; **wood & stone stay
  free**. Found/traded/looted gear of any tier still works — only crafting/smithing is gated.
  - Table-crafted tiers gate at the result slot; **netherite** (and Dragon) gate at the smithing table.
- Strict ladder (chain): left-click a tier to buy it + every tier below it. Pricier toward the top
  (Hardwood 1 → Dragon 100 QS per lane; ~246 per lane). Steel — which opens deepslate mining — lands
  at ~31 QS (≈ one good bounty session).
- Per-node **currency** support: skill nodes can now cost Skill Shards or Quest Shards; unlock, bulk-buy,
  and refund all use the right currency.

### Upgrade notes (ops)
- Run **`/skill regen`** to rebuild the tree with the new 10-tier Quest-Shard lanes.

## [0.16.1] - 2026-06-09

### Changed (skill-tree rebalance — Phase 2)
- **Advancement values widened:** task **2**, goal **12**, challenge/purple **45**, milestone overrides
  ~doubled. Normal advancements barely change; hard/purple ones pay much more. Total earnable (P) ≈ 1090.
- **End-loaded cost ramps** on every lane — early nodes are cheap, the final nodes ramp hard, so
  maxing one path costs about as much as several other lanes. (e.g. Guardian/Vitality/Warrior:
  `2,2,3,4,5,7,10,14,23,30`.)
- **Node-count changes:** Vitality → **10 nodes** (+2 hearts each, +40 HP), Warrior → **10 nodes**
  (+1 dmg each), Reach → **5 nodes** (+0.5 block & entity reach each, +2.5 total).
- **Lane cost targets** (relative; the tree auto-scales so its total = P): Vitality/Fleet/Warrior/
  Guardian ~100, Reach ~115, Aquatic ~125, Brewmaster ~100, Evasion ~100, Fortune/Prospector/
  Cultivator ~50, Mountaineer ~25, Night Vision 75.
- **Brewmaster** now caps at **+50%** potion duration (5 nodes × +10%).
- **Force tree = P:** the whole Skill-Shard tree (lanes + Night Vision) scales to exactly the total
  earnable advancement points, so doing every advancement affords it once. (Armorsmith/Toolsmith are
  excluded — they become Quest-Shard lanes in Phase 3; their costs here are interim.)

### Fixed
- **Cultivator** now scales reliably: each level rolls an independent ~50% chance for a bonus crop, so
  at max (5) you average ~+2.5 per crop instead of the old single capped roll that felt like level 1.

### Upgrade notes (ops)
- Run **`/skill regen`** to rebuild the tree with the new ramps/costs, then **`/skill recalc <player>`**
  for existing players to reprice earned Shards. The startup log prints the new total (P).

## [0.16.0] - 2026-06-09

### Changed
- **Bounty board:** removed the Nether Star and the background panel — it's now just clean floating
  text, with an added **"or use /quests"** line under the right-click prompt.
- **Cleaner GUI tooltips:** skill-tree lane/node icons and the info screens no longer show the icon
  item's intrinsic stats (e.g. "+6 armor"); only the custom name + description show.
- **Skill node tooltip:** now says **"Cost: X"** where X is the **actual total a click will charge**
  (the node plus any locked prerequisites below it), instead of the misleading "Cost to here".
- **Night Vision** lane is now **sealed on the home screen** ("🔒 Locked", can't be opened) until its
  earned-Shard gate is met — and the requirement number is **hidden**, so players can't bee-line to it.
- **Earning Skill Shards screen:** the goals entry now shows the **total Shards from VanillaSkills'
  own advancements**, and the old "recipe unlocks" entry is replaced with a **Daily Bounties** entry
  pointing at the board → Quest Shards → conversion.
- **Aquatic** lane reordered so the **three breath nodes come first**.

### Added
- Op commands **`/skill points <player> reset`** and **`/skill questshards <player> reset`** (set to 0).

## [0.15.3] - 2026-06-08

### Changed
- Bounty board tuning: text back to **1× scale**, and the Nether Star is **bigger (0.9×) and raised
  above the text** instead of sitting small in the middle of it.

## [0.15.2] - 2026-06-08

### Added
- **`/quests board refresh`** (op) — re-renders every placed bounty board in a loaded chunk (despawns
  the old entities and spawns fresh ones), so existing boards pick up visual updates without a manual
  remove/replace.

## [0.15.1] - 2026-06-08

### Changed
- **Bigger, livelier bounty board.** The floating board is now a proper notice board: a **1.5×-scaled
  title on a dark panel**, the **three active bounties with their Quest Shard rewards** listed, and a
  **live "⏳ New bounties in 4h 12m" countdown** that refreshes every few seconds. A **slowly-spinning
  Nether Star** floats above it. The right-click hitbox was enlarged to match.
  - Note: per-player progress/claims still live in the GUI (a hologram is a shared world entity, so it
    looks the same to everyone). Existing boards should be re-placed (`/quests board remove` then
    `/quests board`) to pick up the new look.

## [0.15.0] - 2026-06-08

### Added
- **Deepslate gate:** deepslate and its ore variants now require a **Steel-tier or better pickaxe**
  (Steel, Diamond, Crystalline, Netherite, Dragon) — iron and below can't break them. Forces unlocking
  the Toolsmith Steel node to mine the deep layer. (Creative bypasses.)
- **Bulk-buy & cascade refund in the skill tree:** **left-click** a node to buy it *and every locked
  prerequisite below it* in one purchase (if you can afford the whole chain); **right-click** an
  unlocked node to **refund it and everything above it that depends on it**, returning all the Shards.

### Changed
- **Advancement values are now difficulty-weighted.** Instead of a flat 5 per advancement, value is read
  from each advancement's frame: **task ≈ 1, goal ≈ 5, challenge (purple) ≈ 20**, with milestone
  overrides; root advancements give 0. This removes the "everything gives 5" feel and makes hard/purple
  advancements pay far more.
- **Night Vision** is now **locked until you've earned 1/3 of all possible advancement points**, then
  costs a flat **75**. The rest of the tree is **scaled so the whole tree (including NV) costs exactly
  the total earnable points** — doing every advancement affords the entire tree once.
- **Quest rewards roughly tripled and tiered by difficulty** (easy 4–5, medium 6–7, hard 8–9), so a full
  board of 3 bounties yields ~16–22 Quest Shards — about half the daily shop.
- **Dragon pickaxe gains +18 mining_efficiency**, so Efficiency V + Haste II + full Prospector now
  instamines deepslate (effective speed 91 ≥ the 90 needed).
- **GUI polish:** the Earning-Skill-Shards and Your-Stats screens are now framed and centered (no more
  corner-packed icons); the Stats screen also shows your Skill/Quest Shard balances and progress.

### Fixed
- **`/skill recalc` no longer wipes a player's 5 starting Skill Shards** — it now re-seeds the starting
  bonus before re-tallying advancements.

### Upgrade notes (ops)
- Run **`/skill regen`** once after updating to rebuild the tree with the new pricing + NV gate.
- Run **`/skill recalc <player>`** for existing players to reprice their earned Shards under the new
  difficulty weighting.

## [0.14.2] - 2026-06-08

### Changed
- **All set-bonus armor now shows a live "what you're missing" checklist when worn**, matching Rose
  Gold. While wearing any piece of the **Crystalline** or **Dragon** set, the tooltip shows the set
  count (n/4), a +/- list of which slots are filled, and whether the bonus is active — so you can see
  what you still need. Stored (un-worn) pieces revert to the static description. Generalised the old
  Rose Gold-only logic into a shared `ArmorSetTooltips` so every set tier behaves identically.
  (Hardwood and Steel have no full-set-gated bonus — their movement/toughness is per-piece — so they
  show no checklist.)

## [0.14.1] - 2026-06-08

### Added
- Op test/admin commands: **`/skill questshards <player> add|set <n>`** to grant or set Quest Shards,
  and **`/quests reroll`** to force a fresh set of bounties immediately.

## [0.14.0] - 2026-06-08

### Added
- **Two currencies, renamed for clarity.** Skill-tree points are now **Skill Shards**; quest bounties
  award a separate **Quest Shard** currency. Every "point(s)" label across the GUIs, commands, and
  guide book is updated. Existing balances carry over as Skill Shards; everyone starts with 0 Quest
  Shards.
- **Quest Shop** — a Shop button in the quest GUI opens a daily-rotating catalog (~105 entries,
  weighted so cheap commons appear most). Pay with **Quest Shards (left-click)** or **Skill Shards
  (right-click, auto-converted 3:1)**. A permanent **converter** slot turns 3 Quest Shards → 1 Skill
  Shard (one-way), so early-game Quest Shards buy boosts and late-game they help finish the skill tree.
- The shop selection rotates daily at UTC midnight, derived deterministically from the day (no extra
  save file).

### Changed
- **Bounty board is now holograms-style floating text** instead of a lectern. `/quests board` (op)
  spawns a native text-display ("✦ Bounty Board ✦") plus an invisible interaction entity that anyone
  can right-click to open the quest GUI. `/quests board remove` cleans up both entities.
- Quest GUI and Shop GUI use a clean framed layout (bordered, centered content, header with balances
  + timer, footer with navigation/converter) rather than loose corner icons.

### Internal
- Removed the now-unused `ArmorStandAccessor` mixin (the board no longer uses an armor stand).

## [0.13.0] - 2026-06-07

### Added
- **Physical bounty board**: ops can summon a real, interactable board with
  `/quests board` (alias `/bounty board`). It places a **lectern** where you're looking with a floating
  golden **"✦ Bounty Board ✦"** label above it. Anyone can right-click the board to open the quest GUI —
  no command needed. Remove the nearest board (within 6 blocks) with `/quests board remove`. Board
  positions persist (`world/vanillaskills/questboards.json`) and the floating label is an invisible,
  invulnerable marker armor stand that's cleaned up on removal.

### Internal
- Removed dead code: unused `AlloyRecipes` serializers (the rose gold / steel / crystallized-diamond
  recipes use vanilla `crafting_shapeless`) and their registrations, the orphaned `addLane`/`addLaneMulti`
  helpers in `SkillTreeManager`, and an unused `Map` import. No behavior change.

## [0.12.0] - 2026-06-06

### Added
- **Bounty board** (`/quests`, alias `/bounty`): 3 shared quests that re-roll every **5 hours**.
  Each is a gather-items (turn in at the board) or kill-mobs bounty worth a couple of **skill points**.
  Anyone can do each once per rotation; progress is tracked per player and resets each rotation. Kill
  progress counts toward active quests automatically; gather quests consume the items on claim. Board
  state persists (`world/vanillaskills/questboard.json`); a chat announcement fires on each re-roll.

## [0.11.1] - 2026-06-06

### Changed
- Night Vision capstone cost lowered to **150** (was 276).

## [0.11.0] - 2026-06-06

### Added
- **Night Vision** capstone lane (centred below the lane grid): one node granting **permanent Night
  Vision**, costing **276** — the surplus points a true completionist earns beyond the rest of the
  tree, so doing every advancement affords the whole tree *and* this. Status effects now refresh at
  20s (was 11s) so Night Vision never flashes.

## [0.10.0] - 2026-06-06

### Added
- **Retexturable worn armor.** Each armor tier now uses its own equipment asset
  (`vanillaskills:<tier>`) so resource packs can give each tier custom *worn* armor textures (not just
  inventory icons). Default equipment assets are bundled (worn armor still looks like its base
  material). NOTE: on a vanilla client with no resource pack, custom worn armor has no texture
  (invisible) — modpacks should ship the bundled/required resource pack.

## [0.9.3] - 2026-06-06

### Fixed
- **Skill Master never gave the 5 Dragon Ingots.** The reward was gated on the advancement's
  done-state — once the advancement got marked complete (or the ingots fell into a full inventory),
  the reward path was skipped forever. Now tracked by a flag in player data, so it reliably fires once
  on the unlock/login that completes the tree. (Completion = every node in the *current* tree; if you
  regenned and added lanes, those must be unlocked too.)

## [0.9.2] - 2026-06-06

### Fixed
- **Datapack advancements no longer give points.** Only `minecraft:` and `vanillaskills:` advancements
  are counted now — VanillaTweaks (and other datapacks) were granting points (e.g. +115). Run
  `/skill recalc <player>` to strip already-credited datapack points from existing players.
- **Advancement tab background** now renders: 26.1.2 expects a sprite path without `textures/`/`.png`,
  so it's `minecraft:block/iron_block` (tiled iron) instead of the old full texture path.

### Changed
- **Evasion** uses the centered 5+5 node layout (matching Reach/Guardian/Fortune).

## [0.9.1] - 2026-06-06

### Fixed
- **Losing bonus hearts on relog.** Vitality (and other) max-health is added via transient modifiers
  reapplied on join — but the player loads first, so vanilla clamped current health to the base 20
  before our modifiers existed. Now we record health on logout and restore it after the modifiers
  reapply, so a player who logged out with full 3 rows logs back in with them full.

## [0.9.0] - 2026-06-05

### Added
- **Hardwood swords & axes** now inflict **Poison I for 2s** on hit.

### Changed
- Advancement tab background is now a tiled **iron block** texture.
- Guide book: Steel recipe corrected to **2 iron + 1 coal**.
- Skill-tree layouts: **Fortune Finder, Guardian, Reach** now lay out as a centred **5 + 5** block;
  **Cultivator** as a centred vertical column.

### Fixed
- **Skill Master reward** now also re-checks on join, so players who completed the tree before the
  reward existed get their **5 Dragon Ingots** on next login.

## [0.8.0] - 2026-06-05

### Added
- **Skill Master** advancement (challenge): unlock every node in the skill tree → rewarded with
  **5 Dragon Ingots** (granted in-code) + points.
- **Steeled Defense** advancement: craft a Steel-Infused Shield.

### Changed
- **"Earning Points" screen** rewritten — was a wall of raw advancement IDs; now a clean curated
  summary (per-advancement, milestones, VanillaSkills goals, starting bonus, recipe note).
- **Mountaineer** is more expensive: node costs 5/10/15 (was 1/2/3).
- **Multi-node lanes** now lay out in a tidy centred 7-wide block instead of filling from the
  top-left corner of the chest.

## [0.7.0] - 2026-06-05

### Added
- **Custom mod advancements** (own tab, shown on vanilla clients) that also grant skill points:
  - **Metallurgist** — forge any custom alloy
  - **Hardwood / Rose Gold / Steel / Crystalline / Dragon Suit** — craft a full set of each tier
  - **Ancient Knowledge / Dragon's Legacy** — discover the Fortune / Dragon upgrade templates
  - **Dragon Forged** — forge a Dragon Ingot · **Winged Dragon** — fuse an Elytra onto a Dragon chestplate
  - **Specialist** — fully unlock any skill path (granted in-code on lane completion)
- Point values for all of the above added to `points.json` defaults (+~128 to the earnable total).

## [0.6.1] - 2026-06-05

### Changed
- **Points rebalance** so the full tree (~826 pts / 132 nodes) is reachable very late-game:
  `perAdvancement` 1 → **5**, `startingPoints` 0 → **5**, and **~24 progression-weighted milestone
  overrides** (nether/end/netherite/hard challenges). A near-completionist earns ~900-930.
- Existing servers: delete `config/vanillaskills/points.json` (or edit it) to pick up the new values,
  then `/skill reload` and `/skill recalc <player>` to re-tally already-earned advancements.

## [0.6.0] - 2026-06-05

### Added
- **Evasion** lane (10 nodes): +2% chance per node to completely dodge incoming **arrow** damage,
  up to **20%**. Costs ramp steeply (3→26) — it's a strong defensive perk. (`ArrowDodgeMixin` on
  `LivingEntity#hurtServer`, rolled per hit.)
- **Cultivator** lane (5 nodes): +20% chance per node for **bonus crops** when harvesting mature
  crops (wheat, carrots, potatoes, beetroot, nether wart) — up to a guaranteed bonus at 100%. Drops
  +1–2 extra of the crop's product on success.

### Note
- Run **`/skill regen`** to load the two new lanes (home page is now a 7×2 block).

## [0.5.3] - 2026-06-05

### Changed
- **Steel Ingot** now costs **2 iron + 1 coal** (was 1 iron + 1 coal).
- **Aquatic** node costs ramp up steeply (3→24) so reaching full underwater capability is a real
  point investment.

## [0.5.2] - 2026-06-05

### Fixed
- **Server crash on startup**: `addLaneNodes` used a fixed 6-entry numerals array for node titles, but
  the rescaled lanes have up to 20 nodes → `ArrayIndexOutOfBoundsException` while building the default
  tree (which crashed the server). Replaced with a Roman-numeral helper that handles any node count.

## [0.5.1] - 2026-06-04

### Fixed
- **Dragon Ingot recipe now actually crafts.** It used fragile position-based 3x3 matching;
  rewritten to the same shapeless item-counting style as the other custom alloy recipes
  (8 Dragon Scales + 1 plain Netherite Ingot, in any arrangement → 1 Dragon Ingot).

### Changed
- **Vitality** is now **+1 heart (2 HP) per node, 20 nodes** (still reaches 3 rows of hearts) — half
  the node count.
- **Rose Gold, Steel, and Crystallized Diamond** are now **data-driven recipes** with the marker baked
  into the result, so they **appear in the vanilla recipe book** (granted on join). The crafted items
  are identical (same marker), so armor/tool crafting and repair still recognize them.

### Notes
- Recipe-book display is only possible for recipes with plain vanilla ingredients. Dragon Ingot and the
  Dragon-template duplication can't (their ingredients are marked items, hidden from ingredient
  matching), and the dynamic armor/tool/Fortune recipes are "special" (computed), so none of those
  show in the book.

## [0.5.0] - 2026-06-04

### Fixed
- **Crystallized Diamond, Dragon Ingot, and Dragon-template duplication recipes now work** — they were
  missing their `data/vanillaskills/recipe/*.json` registration entries, so the RecipeManager never
  loaded them. Added the three JSONs.

### Changed
- **Skill-tree home page** reorganized into a tidy 6×2 block (was scattered).
- **Lane rescale** (longer, finer progressions; one node per step):
  - Vitality: +1 max health (½ heart)/node → 3 rows of hearts (40 nodes).
  - Fleet Foot: +2%/node → +30% (15). Fortune Finder: +0.5/node → +5 (10).
  - Warrior: +0.5 attack/node → +10 (20). Guardian: +1 armor/node → +10 (10).
  - Reach: +0.25 block & entity/node → +2.5 (10).
  - Aquatic: spread to 9 nodes (breath ×3, swim speed → full, underwater mining → full).
- Guide book rewritten into shorter pages so nothing is cut off (Dragon section split across pages).

### Note
- Run **`/skill regen`** to apply the reworked tree. Node costs are placeholders pending the points
  rework. Recipe-book display of custom recipes is still TODO (see below).

### Fixed
- **Boot crash**: `DragonSmithingMixin` tried to `@Shadow` the `player` field, which is declared on
  the superclass `ItemCombinerMenu` (Shadow only resolves fields on the target class). Replaced with
  an `ItemCombinerMenuAccessor` (`@Accessor`) to read the player. Compiled fine but crashed on launch.

## [0.4.1] - 2026-06-04

### Added
- A duplication recipe for the **Dragon Upgrade template** (output 2): chorus flowers around the
  existing template + a netherite ingot, with end rods flanking a shulker shell on the bottom row:
  `C T C / C N C / R S R`. (Vanilla template duplication can't dupe it, since the marked template is
  hidden from ingredient matching.)

## [0.4.0] - 2026-06-04

### Changed
- **Armorsmith** and **Toolsmith** are now **5-node lanes (one node per tier)**, costs scaling from
  10 (10/15/20/25/30). Each node unlocks crafting of that specific tier (per-tier flags
  `craft_armor_<tier>` / `craft_tool_<tier>`). The Dragon node also gates the Dragon smithing upgrade.
- **Brewmaster** is now a **5-node lane**; each node adds +20% beneficial-potion duration (up to
  +100%), costs scaling from 10.
- **Prospector** expanded to **5 nodes totalling +12 mining efficiency** — enough that an Efficiency V
  diamond pickaxe instamines stone (Haste II–equivalent speeds).

### Note
- Existing worlds: run **`/skill regen`** to pull in the reworked lanes (backs up the old tree).

### Added
- The combined (Elytra'd) Dragon chestplate now shows a **"+ Elytra"** line in its tooltip so players
  can tell it glides. The label is stripped when the chestplate is split back apart.

## [0.3.0] - 2026-06-04

### Changed
- **Elytra combine reworked to a drop-on-block mechanic** (modelled on the Vanilla Tweaks Armored
  Elytra datapack), replacing the unreliable anvil-GUI mixins. **Drop** a Dragon chestplate + an
  Elytra together **on top of an anvil block** to fuse them into one gliding chestplate; **drop** the
  combined chestplate **on a grindstone block** to split them back (enchants preserved). A 1/second
  server-side scan (`DragonElytraForge`) handles it — no menu mixins, fully vanilla-client safe.

### Removed
- `DragonElytraAnvilMixin`, `GrindstoneMenuMixin`, `GrindstoneResultSlotMixin`, and
  `DragonGrindstoneAccess` (the old anvil/grindstone GUI combine path).

## [0.2.3] - 2026-06-04

### Fixed
- The Dragon Upgrade template no longer shows netherite's "Applies to / Ingredients" tooltip lines.
  `DragonTemplateTooltipMixin` suppresses `SmithingTemplateItem.appendHoverText` for our marked
  template (its custom name + lore still render). Client-side: takes effect on clients that have the
  mod; a pure-vanilla client on a dedicated server still sees the netherite lines (tooltips render
  client-side, and the item must remain a real template so the smithing slot accepts it).

## [0.2.2] - 2026-06-04

### Fixed
- **Boot crash**: `PotionStackSizeMixin` targeted `getMaxStackSize` on `ItemStack`, but that method
  is a default on the `ItemInstance` interface (ItemStack doesn't declare it), so the injection
  found no target and crashed the game on startup. Now mixes into `ItemInstance` instead.

## [0.2.1] - 2026-06-04

### Added
- **`/skill regen`** (op): overwrites `config/vanillaskills/skilltree.json` with the built-in
  default tree (so servers pick up new lanes/nodes after a mod update), backing up the existing
  file to `skilltree.backup-<timestamp>.json` first, then reapplying effects to online players.

## [0.2.0] - 2026-06-04

### Added
- **Dragon armor overhaul**: Ender Dragon drops **Dragon Scales** (8/kill) → forge a **Dragon Ingot**
  (8 scales around a netherite ingot). Dragon **armor** is now upgraded from netherite in a
  **smithing table** with the **Dragon Upgrade Template** (End City treasure, ~4%) + a Dragon Ingot,
  preserving enchantments. Dragon **tools** are crafted from Dragon Ingots. Dragon armor is no longer
  table-craftable.
- Skill tree grew to **12 lanes**: Guardian (armor), Reach (interaction range), Mountaineer (step
  height), Aquatic (underwater), **Armorsmith**/**Toolsmith** (unlock crafting custom gear), and
  **Brewmaster** (+50% beneficial potion duration).
- **Skill-gated crafting**: custom armor/tools can't be taken from the crafting result until the
  matching skill is unlocked; ingots/materials are never gated.
- **Potions** stack to 16; Brewmaster extends beneficial, non-infinite potions ×1.5 (past the 8-min cap).

### Changed
- Hardwood armor movement bonus 8% → 10% at a full set.
- Dragon armor toughness 4.0 → 3.0 (the dive-dash was too strong with the higher value).

### Fixed
- **Elytra now combines onto the Dragon chestplate**: the anvil mixin injected at TAIL, which vanilla
  skips via an early return for an "invalid" armor+elytra combo. Now HEAD/cancellable, and it consumes
  the elytra (sets `repairItemCountCost`).

### Notes
- The Elytra anvil/grindstone and Dragon smithing menu interactions compile against verified game
  internals (SmithingMenu places items via RecipePropertySet, not Ingredient, so marked items fit
  the slots natively) but still warrant an in-game playtest. Custom items render as their vanilla
  bases until a resource pack adds textures.

## [0.1.2] - 2026-06-04

### Changed
- Spears now use the real vanilla spear items (`WOODEN/STONE/COPPER/IRON/GOLDEN/DIAMOND/NETHERITE_SPEAR`)
  instead of being faked with swords. The tiered spear keeps the real spear's reach and slower
  swing and just adds the normal per-tier damage/speed bonus.
- Halved the Steel-Infused Shield's thorns damage (4.0 → 2.0), so blocking a melee hit injures the
  attacker for 1 heart instead of 2.

## [0.1.1] - 2026-06-02

### Fixed
- Fixed a fatal server-boot crash: the grindstone Elytra-split mixin injected `onTake` on
  `GrindstoneMenu`, but that method lives on the result-slot inner class. Split the logic into a
  dedicated inner-class mixin so the server starts again.

## [0.1.0] - 2026-06-02

### Added
- Server-side skill tree and progression overhaul (works with vanilla clients).
- Per-player skill tree with a chest GUI and `/skill editor`; Mending removed from the game.
- New tool/weapon/armor tiers: Hardwood, Rose Gold, Steel, Crystalline (Diamond II), and Dragon
  (Netherite II), plus dragon-scale drops, fire immunity, a dive-dash, and an Elytra⇄chestplate
  anvil/grindstone combine.
- Craftable Fortune IV/V via a custom smithing template.
