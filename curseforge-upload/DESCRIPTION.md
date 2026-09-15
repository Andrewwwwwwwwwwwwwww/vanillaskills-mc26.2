# VanillaSkills

**Experience is gone. Skill Shards took its place.**

VanillaSkills replaces vanilla's progression with one of its own. Experience orbs no longer drop, levels no longer matter, and the XP bar shows your banked **Skill Shards** instead. You spend them on a fifteen-lane skill tree, at the anvil, and at the Infusing Table that replaces enchanting.

It installs on the **server only**. Your players connect with an unmodified client and see everything — the custom gear, the crates, the retextured blocks — because the server pushes a resource pack on join. Nobody installs anything by hand.

---

## Skill Shards are a real item

Withdraw shards from the skill tree as **Unstable Skill Shards** and right-click one to bank it again, so they can be traded, stored, or handed to another player.

Nine compress into an **Unstable Skill Shard Block** — which is reinforced deepslate, taken over outright. Ancient cities generate obsidian in its place, so the only reinforced deepslate in the world is yours. It also spawns as ore deep underground in all three dimensions, and only a Netherite, Crystalline or Dragon pickaxe can harvest it.

Ring one with redstone and tinted glass for a **Stable Skill Shard Block**: it damages hostile mobs around it, merges with its neighbours to widen that area, works as a beacon base with triple range, and shrugs off explosions.

## Where shards come from

Advancements are the main source — each counts once and can never be farmed. Beyond that: shard ore, structure chests, piglin bartering, broken spawners, crates, and a wandering trader who now **buys** raw materials from you and pays in shards.

Other mods can feed the economy too. Add a namespace to `countedNamespaces` and its advancements pay out at the usual rate; anything that completes during login is recorded but worth nothing, so adding a mod mid-world never hands out a windfall.

## Five gear tiers past netherite

**Hardwood · Rose Gold · Steel · Crystalline · Dragon** — each with its own armour, tools and set bonus.

Rose Gold shrugs off every negative status effect and keeps piglins neutral. Crystalline reflects a quarter of the melee damage you take and grants Strength and Resistance. Dragon is immune to fire, lava and dragon's breath, and lets you dive-dash by sneaking in midair — then fuse a Dragon chestplate with an Elytra on an anvil for an armoured glider.

Every tier's armour, toughness, knockback, movement penalty, durability, attack damage and mining speed is tunable from one config block, and existing gear is brought onto retuned numbers as its owner logs in.

## The Infusing Table

The enchanting table, rebuilt for a world without experience. It reads the enchanted books shelved in the **chiseled bookshelves** around it and offers exactly those — several at once if you want — for a price in shards. You see what you're getting before you pay, and the books are never consumed. Build the library once.

## Crates

Fish one up as a bonus catch. A Wooden → Copper → Iron → Diamond rarity ladder plus **Frozen**, **Lush** and **Desert** variants decided by the biome you're fishing in, and a slot-machine reel that spins in front of you before it pays out. **Unboxing**, a fishing-rod enchantment, raises your crate rate — and turns up in crates itself.

## Bounty Board and Quest Shop

New players work a fixed starter board, then graduate to a shared board that rerolls on a timer. Bounties pay **Quest Shards**, which buy the gear-unlock lanes and a rotating shop of boosts — including enchanted books, capped low and priced high, so combining two cheap ones at an anvil is the real route to a high level.

## Fortune IV and V

Find a Fortune Upgrade template in an ancient city or a mineshaft, combine two Fortune III books with it, and again for V. Each level above III adds a chance at an extra base drop, not a guaranteed one.

---

## Everything is a datapack

The skill tree, quests, shop stock, crates and feats all live in datapack files using the vanilla tag format. A pack can add, reprice, replace or remove any of it the same way it would extend a tag — no code, no fork. The mod ships its own content exactly this way.

Numbers that aren't content live in a per-world `gameplay.json` you can reload live with `/skill reload`.

## Requirements

- Minecraft **26.3**, **26.2** or **26.1.2**
- **Fabric** (with Fabric API) or **NeoForge**
- Install on the **server**. A client install is optional; vanilla clients are fully supported.

## Translations

English and Traditional Chinese, complete. Every quest, crate, feat, shop offer, skill lane and node description is translatable.

---

**Upgrading from 1.7.x?** 2.0 is a breaking release — back up your world and test on a copy first. Gear, skill points and bounty progress all carry over, but experience is removed, Steel moves from the anvil to the furnace, and two vanilla blocks change meaning. Full notes are in the changelog.

**Documentation:** https://andrewwwwwwwwwwwwwww.github.io/modhub/mods/vanillaskills/
