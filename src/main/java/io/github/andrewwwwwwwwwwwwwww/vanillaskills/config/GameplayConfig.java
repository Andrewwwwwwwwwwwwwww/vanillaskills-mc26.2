package io.github.andrewwwwwwwwwwwwwww.vanillaskills.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.QuestShop;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Quests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Optional gameplay/pacing config, stored PER-WORLD at &lt;world&gt;/vanillaskills/gameplay.json so each
 * world (and each server) can have its own settings. Edit the file (or use the Mod Menu screen in a
 * loaded singleplayer world) and it applies on load / {@code /skill reload}, no cheats required.
 */
public class GameplayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // --- Live values published to the rest of the mod on load() ---

    /** Read by {@code ItemEnchantmentsMutableMixin}. false (default) = Mending is stripped everywhere. */
    public static volatile boolean MENDING_ENABLED = false;

    /** Read by the experience mixins. false (default) = experience is removed from the game entirely:
     *  nothing drops or grants XP, no orbs spawn, and Bottles o' Enchanting stop appearing in loot
     *  and villager trades. Set true to restore vanilla experience. */
    public static volatile boolean EXPERIENCE_ENABLED = false;

    // The pushed pack must carry the language files: vanilla clients have no mod jar, so their gear
    // and item NAMES come only from this pack's lang. A stale pack (pre-localization) makes every
    // custom item name fall back to English. Bump BOTH the URL and the SHA-1 whenever the pack changes.
    // Rebuild both with tools/build-pack.sh, which zips the pack and patches the hash into all four
    // editions at once - they have to be produced together or the client rejects the download.
    //
    // The 1.7.6 pack this replaces was actively harmful under 2.0: it overrode 57 vanilla items
    // (gold_ingot, diamond, iron_ingot, the tool and armour sets) using the pre-2.0 custom_model_data
    // approach, which both broke those vanilla textures and outranked any locally-installed 2.0 pack.
    private static final String DEFAULT_RP_URL =
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v2.0.1/VanillaSkills-TexturePack.zip";
    private static final String DEFAULT_RP_SHA1 = "f3f17dc0bccda7e214fe70f144b25b3946874e07";
    /** The public wiki page the Guide icon links to. */
    private static final String DEFAULT_GUIDE_URL =
            "https://andrewwwwwwwwwwwwwww.github.io/modhub/mods/vanillaskills/";

    /** When true, the server force-pushes the VanillaSkills texture pack to every joining client
     *  (so vanilla clients see the custom gear with no server.properties setup). Read on player join. */
    public static volatile boolean PUSH_RESOURCE_PACK = true;
    public static volatile String RESOURCE_PACK_URL = DEFAULT_RP_URL;
    public static volatile String RESOURCE_PACK_SHA1 = DEFAULT_RP_SHA1;
    /** Wiki URL for the Guide icon; blank = open the built-in book instead. See {@code guideUrl}. */
    public static volatile String GUIDE_URL = DEFAULT_GUIDE_URL;
    /** Milliseconds after login during which a completed advancement is worth 0. See the persisted field. */
    public static volatile long ADVANCEMENT_LOGIN_GRACE_MS = 5000L;
    /** Read by {@code CraftingGate}: when false, TOOL crafting has no skill requirement and the
     *  Toolsmith lane is hidden from the skill tree. */
    public static volatile boolean TOOL_REQS_ENABLED = true;
    /** Read by {@code CraftingGate}: when false, ARMOR crafting has no skill requirement and the
     *  Armorsmith lane is hidden from the skill tree. */
    public static volatile boolean ARMOR_REQS_ENABLED = true;
    /** Read by {@code DeepslateGate}: false = anyone can mine deepslate (no Steel-pick requirement). */
    public static volatile boolean DEEPSLATE_GATE = true;
    /** Read by {@code FortuneBoost}: false = Fortune IV/V behave like vanilla (no extra base drops). */
    public static volatile boolean FORTUNE_BOOST = true;
    /** Chance of one extra base drop per Fortune level above III. 1.0 = the old guaranteed behaviour. */
    public static volatile float FORTUNE_BONUS_CHANCE = 0.75f;

    // --- Gear balance (see GearTuning for the per-tier numbers) ---
    /** Fraction of incoming melee damage a full Crystalline set reflects. 0 disables the reflect. */
    public static volatile float CRYSTAL_REFLECT_FRACTION = 0.25f;
    /** Grant Strength and Resistance while a full Crystalline set is worn. */
    public static volatile boolean CRYSTAL_SET_EFFECTS = true;
    /** Amplifier for those effects: 0 = level I. */
    public static volatile int CRYSTAL_SET_AMPLIFIER = 0;
    /** Grant Fire Resistance while a full Rose Gold set is worn. */
    public static volatile boolean ROSE_GOLD_FIRE_RESISTANCE = true;
    /** Dragon set dash: launch velocity, downward bias, and cooldown in ticks. 0 speed disables the dash. */
    public static volatile double DRAGON_DASH_SPEED = 1.6;
    public static volatile double DRAGON_DASH_DOWN_BIAS = 0.3;
    public static volatile long DRAGON_DASH_COOLDOWN_TICKS = 60L;
    /** Steel Shield: durability, movement penalty while held, and damage dealt to melee attackers. */
    public static volatile int STEEL_SHIELD_DURABILITY = 2400;
    public static volatile double STEEL_SHIELD_SLOWDOWN = -0.10;
    public static volatile float STEEL_SHIELD_THORNS = 2.0f;
    /** Bring a player's existing VanillaSkills gear onto the current tier numbers as they log in.
     *  Without this, retuning gear balance only affects pieces crafted afterwards. */
    public static volatile boolean GEAR_RESTAMP = true;
    /** Read by {@code Feats}: false = the whole Feats system is off (no tab, no auto-awards). */
    public static volatile boolean FEATS_ENABLED = true;
    /** Skill tree screen title and row count (the tree itself is datapack-owned). */
    public static volatile String SKILL_TREE_TITLE = "Skills";
    public static volatile int SKILL_TREE_ROWS = 6;
    /** Show a horse's speed / jump / health in its inventory screen title. */
    public static volatile boolean HORSE_STATS = true;
    /** Show the player's banked Skill Shards as the number on the experience bar. */
    public static volatile boolean SHARDS_IN_XP_BAR = true;
    /** Copper helmet emits light when worn. ⚠ CLIENT-SIDE rendering — only players with the mod see it. */
    public static volatile boolean COPPER_HELMET_LIGHT = true;
    public static volatile int COPPER_HELMET_LIGHT_LEVEL = 12;

    /** Infusing Table — replaces the enchanting table now that experience is gone. */
    public static volatile boolean INFUSING_ENABLED = true;
    public static volatile int INFUSING_COST_PER_LEVEL = 2;
    /** Leave enchantments the held item cannot take out of the Infusing Table entirely, instead of
     *  listing them greyed out after the ones that fit. */
    public static volatile boolean INFUSING_HIDE_INCOMPATIBLE = false;
    /** "skill" or "quest" — which shard the table charges. */
    public static volatile String INFUSING_CURRENCY = "skill";
    /** Enchantments whose BOOK is burned when infused, as "<enchantment>:<minLevel>". */
    public static volatile java.util.List<String> INFUSING_CONSUMED_BOOKS = java.util.List.of("minecraft:fortune:4");

    /** Nether roof rule — see the persisted fields for meaning. */
    public static volatile boolean NETHER_ROOF_DAMAGE = true;
    public static volatile int NETHER_ROOF_Y = 128;
    public static volatile float NETHER_ROOF_DAMAGE_AMOUNT = 4.0f;
    /** Read by {@code PlayerSkillManager} on first join: false = new players skip the starter board. */
    public static volatile boolean STARTER_QUESTS = true;
    /** Read by {@code QuestBoard}: how many quests are dealt per rotation. */
    public static volatile int QUESTS_PER_ROTATION = 6;
    /** Read by {@code QuestShop}: how many offers appear per shop rotation. */
    public static volatile int SHOP_SLOTS = 8;
    /** Anvil pricing in Skill Shards — see the persisted fields for the reasoning. */
    public static volatile boolean ANVIL_MATERIAL_PRICING = true;
    public static volatile int ANVIL_REPAIR_PER_MATERIAL = 1;
    public static volatile int ANVIL_ENCHANT_PER_LEVEL = 1;
    public static volatile int ANVIL_RENAME_COST = 0;
    /** Flat shards for a same-item combine that restores durability (the sacrifice is consumed for it). */
    public static volatile int ANVIL_COMBINE_REPAIR_COST = 2;
    /** Read by {@code AnvilMenuMixin}: true = restore the vanilla 40-level "Too Expensive" cap. */
    public static volatile boolean ANVIL_TOO_EXPENSIVE_CAP = false;
    /** Read by {@code AnvilMenuMixin}: true restores letting an enchanted book be applied straight to a
     *  tool/armor piece in the anvil. Default false: the Infusing Table is the only path for that now.
     *  Book + book combining is never affected by this. */
    public static volatile boolean ANVIL_BOOKS_ON_ITEMS = false;
    /** Read by {@code AnvilMenuMixin}: flat cost to fully repair Dragon gear with a Dragon Ingot.
     *  Charged in Skill Shards while experience is removed, in levels when it is restored. */
    public static volatile int DRAGON_REPAIR_COST = 2;
    // --- Skill Shard blocks (read by ShardBlocks / ShardBank) ---
    /** How far a Stable Skill Shard Block's aura reaches from its centre. 3 = a 7x7x7 cube. */
    public static volatile int SHARD_AURA_RADIUS = 3;
    /** Damage dealt to each hostile mob inside the aura, per pulse. */
    public static volatile float SHARD_AURA_DAMAGE = 3.0f;
    /** Ticks between aura pulses. 20 = once a second. */
    public static volatile int SHARD_AURA_INTERVAL = 20;
    /** How many Stable blocks can be merged into one placed block (each merge widens the radius). */
    public static volatile int SHARD_MAX_MERGE = 4;
    /** Multiplier applied to a beacon's range when it stands on a Stable Skill Shard Block base. */
    public static volatile int SHARD_BEACON_RANGE_MULT = 3;
    /** Extra amplifier levels a beacon grants on a Stable base ("doubled" effect = +1 level). */
    public static volatile int SHARD_BEACON_AMPLIFIER_BONUS = 1;
    /** Skill Shards converted to items per confirmed click of the withdraw button. */
    public static volatile int SHARD_WITHDRAW_AMOUNT = 1;
    /** Require a Crystalline-or-better pickaxe to break shard blocks. */
    public static volatile boolean SHARD_MINING_GATE = true;
    /** Broken spawners drop an Unstable Skill Shard Block (they no longer drop experience). */
    public static volatile boolean SPAWNER_DROPS_SHARD_BLOCK = true;
    /** Loot weights for finding an Unstable Skill Shard. Weight vs "empty" gives the chance, so
     *  chest = 1/(1+60) ≈ 1.6% and barter = 1/(1+120) ≈ 0.8% at the defaults. 0 disables that source. */
    public static volatile int SHARD_CHEST_WEIGHT = 1;
    public static volatile int SHARD_CHEST_EMPTY_WEIGHT = 60;
    public static volatile int SHARD_BARTER_WEIGHT = 1;
    public static volatile int SHARD_BARTER_EMPTY_WEIGHT = 120;
    /** Weight of a crate against "empty" in the fishing table. 0 removes crates from fishing. */
    public static volatile int CRATE_FISHING_WEIGHT = 1;
    public static volatile int CRATE_FISHING_EMPTY_WEIGHT = 40;
    /** Opening reel: false pays out instantly with no animation. */
    public static volatile boolean CRATE_REEL_ENABLED = true;
    /** Roughly how long the reel runs before it may settle, in ticks. */
    public static volatile int CRATE_REEL_TICKS = 60;
    /** Radius of the ring of items, in blocks. */
    public static volatile double CRATE_REEL_RADIUS = 1.35;
    /** How far in front of the player the ring is centred, in blocks. */
    public static volatile double CRATE_REEL_DISTANCE = 1.4;
    /** How many distinct items the ring shows. */
    public static volatile int CRATE_REEL_POOL = 8;
    /** Size of each item on the ring; 1.0 is a full block. */
    public static volatile double CRATE_REEL_SCALE = 0.28;
    /** Gap between reel cells, as a multiple of item scale. */
    public static volatile double CRATE_REEL_SPACING = 1.6;
    /** Ticks the finished result is held on screen before the loot is handed over. */
    public static volatile int CRATE_REEL_HOLD = 30;
    /** Height bands the generated ore is recognised in. Keep in step with the placed-feature JSONs in
     *  {@code data/vanillaskills/worldgen/placed_feature/} — those control where it actually spawns. */
    public static volatile int SHARD_ORE_OVERWORLD_MIN_Y = -10;
    public static volatile int SHARD_ORE_OVERWORLD_MAX_Y = 10;
    public static volatile int SHARD_ORE_NETHER_MAX_Y = 15;
    /** Unstable Skill Shards dropped by one generated ore block. */
    public static volatile int SHARD_ORE_DROP = 1;
    /** Chance (0–1) that mining/placing a block or harvesting a crop shakes an Unstable Skill Shard
     *  loose, and the per-player cooldown after a payout during which nothing rolls. 0 chance disables. */
    public static volatile double TASK_SHARD_CHANCE = 0.002;
    public static volatile int TASK_SHARD_COOLDOWN_SECONDS = 240;
    /** Chance multiplier added per point of the luck attribute (Fortune Finder, Luck potions):
     *  effective chance = base × (1 + luck × this). 0.1 = +50% odds at a maxed Fortune Finder lane. */
    public static volatile double TASK_SHARD_LUCK_BONUS = 0.1;
    /** Give the vanilla items that ship with no repair material one that fits (trident ← prismarine,
     *  bow/crossbow/rod ← string, flint-and-steel/shears ← iron, brush ← copper). */
    public static volatile boolean VANILLA_REPAIR_MATERIALS = true;
    /** Movement speed added per Aquatic swim node while in water (0.2 = +20% each, +60% for all three). */
    public static volatile double AQUATIC_SWIM_SPEED = 0.2;
    /** Bone meal on a spore blossom grows another on a ceiling nearby, making them renewable. */
    public static volatile boolean BONEMEAL_SPORE_BLOSSOMS = true;
    /** Loot `quality` on the crate catch: ~+10% relative crate odds per point of fishing luck
     *  (Luck of the Sea level + the luck attribute). 0 makes the crate rate ignore luck. */
    public static volatile int CRATE_FISHING_LUCK_QUALITY = 1;
    /** Dragon Scales dropped when a player kills the Ender Dragon, and on the world's FIRST player kill. */
    public static volatile int DRAGON_SCALE_DROP = 8;
    public static volatile int DRAGON_SCALE_FIRST_KILL_DROP = 32;

    /** Read by {@code QuestBoard} when re-rolling: ms between bounty rotations. */
    public static volatile long BOUNTY_REFRESH_MS = 5L * 3_600_000L;
    /** Read by {@code QuestShop}: ms between shop rotations. */
    public static volatile long SHOP_REFRESH_MS = 24L * 3_600_000L;
    // QuestShop.CONVERT_RATIO and Quests.GRADUATE_AT are pushed directly on load().

    // --- Persisted fields (gameplay.json) ---

    /** When true, Mending is available as normal; when false, the mod removes it everywhere. */
    public boolean mendingEnabled = false;
    /** When true, experience works as in vanilla. When false (default) experience is removed entirely —
     *  no XP from blocks, mobs, spawners, furnaces, breeding, fishing or trades, no orbs, and no
     *  Bottles o' Enchanting in chest loot or villager trades. Anvil costs are paid in Skill Shards. */
    public boolean experienceEnabled = false;
    /** Hours between bounty-board rotations (default 5). */
    public int bountyRefreshHours = 5;
    /** Hours between Quest Shop rotations (default 24). */
    public int shopRefreshHours = 24;
    /** Quest Shards needed per 1 Skill Shard at the converter (default 3). */
    public int convertRatio = 3;
    /** LEGACY (pre-1.2.0): graduation is now "complete every fixed starter quest" — this value is
     *  ignored; kept so old gameplay.json files still parse. */
    public int graduateAt = 15;
    /** Skill-gate TOOL crafting behind the Toolsmith lane (default true). Set false to let anyone
     *  craft any tool tier — the Toolsmith lane disappears from the skill tree. */
    public boolean toolCraftingRequirements = true;
    /** Skill-gate ARMOR crafting behind the Armorsmith lane (default true). Set false to let anyone
     *  craft any armor tier — the Armorsmith lane disappears from the skill tree. */
    public boolean armorCraftingRequirements = true;
    /** Require a Steel-tier or better pickaxe to mine deepslate (default true). Set false to disable. */
    public boolean deepslateGate = true;
    /** Fortune IV/V grant extra base ore drops (default true). Set false for vanilla fortune behaviour. */
    public boolean fortuneBoost = true;
    /** Chance of one extra base drop per Fortune level above III (default 0.5, so Fortune V averages one
     *  extra drop rather than two). 1.0 restores the old guaranteed behaviour; 0 disables the bonus drops
     *  while leaving the Ancient Debris chance intact. */
    public float fortuneBonusChance = 0.75f;
    /** Enable the Feats system — one-time achievement rewards. 2.0: back ON by default. It was switched
     *  off long ago as too strong, but the shard economy is far wider now and 188 Quest Shards of built,
     *  finished content was sitting unreachable. */
    public boolean feats = true;
    /** Title shown on the skill tree screen (default "Skills"). The lanes and nodes themselves are datapack
     *  content — see {@code data/<ns>/vanillaskills/skill_category/} and {@code skill_node/}. */
    public String skillTreeTitle = "Skills";
    /** Rows in the skill tree GUI, 1-6 (default 6). */
    public int skillTreeRows = 6;
    /** Report a horse's real speed, jump height and health when you open its inventory (default true).
     *  Server-side, so it works on vanilla clients. */
    public boolean horseStats = true;
    /** Show banked Skill Shards as the level number on the experience bar (default true).
     *  Experience is removed in 2.0, so the bar is otherwise dead space. DISPLAY ONLY — a cosmetic packet;
     *  the server side stays at level 0. It also stops the anvil greying out its result, because vanilla's
     *  client-side affordability check finally sees a number it can compare against. */
    public boolean shardsInXpBar = true;
    /** A worn Copper Helmet lights the area around you (default true). ⚠ This is CLIENT-SIDE rendering:
     *  only players who have the mod installed see it, and it lights nothing for anyone else. */
    public boolean copperHelmetLight = true;
    /** Brightness of that light, 0-15 (default 12, just under a torch's 14). */
    public int copperHelmetLightLevel = 12;
    /** Right-clicking an enchanting table opens the Infusing Table (default true). Set false to leave the
     *  vanilla enchanting screen in place — though with experience removed it cannot be paid for. */
    public boolean infusingEnabled = true;
    /** Shards charged per level of the enchantment being applied (default 1, so Efficiency IV costs 4). */
    public int infusingCostPerLevel = 1;
    /** Which currency the Infusing Table charges: "skill" or "quest". Books are never consumed either way. */
    public String infusingCurrency = "skill";
    /** Hide enchantments the held item cannot take, rather than listing them greyed out below the ones
     *  that fit (default false — greyed out, so players can still see what their shelves hold). */
    public boolean infusingHideIncompatible = false;
    /** Enchantments whose shelved BOOK is consumed when infused, each "&lt;enchantment&gt;:&lt;minLevel&gt;".
     *  Books are normally permanent, which is the point of the table, so this is the deliberate exception.
     *  Fortune IV/V are minted rather than enchanted, and a reusable source would devalue every other route
     *  to them. Empty the list to make every book permanent again. */
    public java.util.List<String> infusingConsumedBooks = java.util.List.of("minecraft:fortune:4");
    /** Damage players who get on top of the Nether's bedrock roof, the same way the world border does.
     *  Set false to allow roof travel. */
    public boolean netherRoofDamage = true;
    /** Y level at or above which the Nether roof rule applies (default 128 — the roof itself is 127). */
    public int netherRoofY = 128;
    /** Damage per pulse to a player above the Nether roof. */
    public float netherRoofDamageAmount = 4.0f;
    /** New players start on the fixed starter board (default true). Set false to send new players
     *  straight to the rotating board (players already mid-starter finish theirs normally). */
    public boolean starterQuests = true;
    /** How many quests the rotating board deals per rotation (default 6). */
    public int questsPerRotation = 6;
    /** How many offers the Quest Shop shows per rotation (default 8). */
    public int questShopSlots = 8;
    /**
     * Price anvil work by what it consumes rather than by vanilla's level formula.
     *
     * <p>Vanilla's anvil cost is built for experience, which regrows. Skill Shards do not — an advancement
     * pays once, and the whole world holds a fixed number. Charging vanilla's levels 1:1 in shards therefore
     * bills a <b>repeatable</b> sink to a <b>finite</b> budget: every repair is taken out of the skill tree,
     * and a heavy anvil user eventually cannot pay at all. A late-game combine can cost 39 of roughly 2,100.
     *
     * <p>With this on, the price is what you put in:
     * <ul>
     *   <li>repairing with materials costs {@code anvilRepairCostPerMaterial} per material consumed —
     *       four diamonds into a pickaxe is four shards;</li>
     *   <li>merging enchantments costs {@code anvilEnchantCostPerLevel} per enchantment level on the
     *       sacrificed item, so the price tracks what you are actually feeding in;</li>
     *   <li>a plain rename costs {@code anvilRenameCost}.</li>
     * </ul>
     *
     * <p>Prior-work penalty stops applying, which is the point: it is what made an item progressively
     * unrepairable. Set false to go back to vanilla's level cost charged 1:1 in shards.
     */
    public boolean anvilMaterialPricing = true;
    public int anvilRepairCostPerMaterial = 1;
    public int anvilEnchantCostPerLevel = 1;
    public int anvilRenameCost = 0;
    /** Flat shards for a same-item combine that restores durability (default 2). The durability comes
     *  from consuming a whole second item, which per-material pricing otherwise never sees. */
    public int anvilCombineRepairCost = 2;
    /** Restore vanilla's 40-level "Too Expensive" anvil cap (default false = no cap, costs still scale). */
    public boolean anvilTooExpensiveCap = false;
    /** Allow an enchanted book to be applied directly to a tool/armor piece in the anvil (default false).
     *  The Infusing Table replaced this path in 2.0; set true to restore the vanilla behaviour. Combining
     *  two enchanted books together in the anvil is never affected by this: that still works, and is still
     *  priced in Skill Shards like every other anvil operation. */
    public boolean anvilBooksOnItems = false;
    /**
     * Flat cost to fully repair a Dragon tool or armour piece with one Dragon Scale or Ingot.
     *
     * <p>One material restores the piece completely however damaged it was, so this is a flat fee rather
     * than a per-material price like the rest of the anvil. It was 20 back when an ordinary combine cost
     * 30-40 levels; now that {@link #anvilMaterialPricing} puts everything else at one shard per material,
     * 2 keeps Dragon the dearest repair in the game without being out of step with it.
     *
     * <p>Paid in Skill Shards while {@link #experienceEnabled} is false, in levels when it is true.
     */
    public int dragonRepairCost = 2;
    /** Stable Skill Shard Block aura reach from its centre, in blocks (default 3 = a 7x7x7 cube). */
    public int shardAuraRadius = 3;
    /** Damage each hostile mob inside the aura takes per pulse (default 4.0 = two hearts). */
    public float shardAuraDamage = 4.0f;
    /** Ticks between aura pulses (default 20 = once a second). Raise to soften, lower to sharpen. */
    public int shardAuraIntervalTicks = 20;
    /** How many Stable blocks can be merged into one placed block (default 4). Each merge widens the aura. */
    public int shardMaxMerge = 4;
    /** Beacon range multiplier when built on a Stable Skill Shard Block base (default 3 = 50 -> 150 blocks). */
    public int shardBeaconRangeMultiplier = 3;
    /** Extra beacon amplifier levels on a Stable base (default 1, i.e. the chosen effect at double strength). */
    public int shardBeaconAmplifierBonus = 1;
    /** Skill Shards withdrawn as items per confirmed click of the withdraw button (default 1). */
    public int shardWithdrawAmount = 1;
    /** Require a Crystalline-or-better pickaxe to break shard blocks (default true). A plain diamond
     *  pickaxe does NOT qualify — Crystalline sits above diamond in this mod's tier ladder. */
    public boolean shardMiningGate = true;
    /** Broken spawners drop an Unstable Skill Shard Block, replacing the experience they used to give
     *  (default true). */
    public boolean spawnerDropsShardBlock = true;
    /** Chance of an Unstable Skill Shard in a structure chest, as weight vs emptyWeight.
     *  Defaults give 1/(1+60) ≈ 1.6% per chest. Set shardChestWeight to 0 to remove chest shards. */
    public int shardChestWeight = 1;
    public int shardChestEmptyWeight = 60;
    /** Same, for piglin bartering. Defaults give 1/(1+120) ≈ 0.8% per trade. */
    public int shardBarterWeight = 1;
    public int shardBarterEmptyWeight = 120;
    /** Chance of hooking a crate, as weight vs emptyWeight. Defaults give 1/(1+40) ≈ 2.4% per catch, on
     *  top of the normal catch. Set crateFishingWeight to 0 to stop crates being fished up at all.
     *  <b>Which</b> crate you get, and where, is decided by the {@code vanillaskills:crate_fishing} loot
     *  table rather than here — that is a datapack file, so it can be rewritten without a config change. */
    public int crateFishingWeight = 1;
    public int crateFishingEmptyWeight = 40;
    /** Show the opening reel when a crate is opened (default true). The reward is rolled the moment the
     *  crate is opened either way - the reel only reveals it, and is granted in full even if you log out
     *  mid-spin. Set false to pay out instantly. */
    public boolean crateReelEnabled = true;
    /** Roughly how long the reel spins before it is allowed to settle, in ticks (default 60 = 3s). */
    public int crateReelTicks = 60;
    /** Radius of the ring of items in blocks (default 1.35). */
    public double crateReelRadius = 1.35;
    /** How far in front of the player the ring is centred, in blocks (default 2.0). */
    public double crateReelDistance = 1.4;
    /** How many distinct possibilities the ring shows (default 8). */
    public int crateReelPool = 8;
    /** Size of each item on the ring, where 1.0 is a full block (default 0.45). */
    public double crateReelScale = 0.28;
    /** Gap between reel cells, as a multiple of item scale (default 1.6). */
    public double crateReelSpacing = 1.6;
    /** Ticks the finished result is held before the loot is handed over (default 30 = 1.5s). */
    public int crateReelHold = 30;
    /** Height bands in which naturally generated Skill Shard ore is recognised. These must match the
     *  placed-feature JSONs under {@code data/vanillaskills/worldgen/placed_feature/}, which is where the
     *  spawn rate itself lives (worldgen is baked at chunk generation, so a runtime toggle would lie). */
    public int shardOreOverworldMinY = -10;
    public int shardOreOverworldMaxY = 10;
    public int shardOreNetherMaxY = 15;
    /** Unstable Skill Shards dropped by one generated ore block (default 1). */
    public int shardOreDrop = 1;
    /** Chance (0–1) that ordinary work — mining or placing a block, harvesting a crop — drops an
     *  Unstable Skill Shard (default 0.002, one action in 500). Set 0 to disable the mechanic. */
    public double taskShardChance = 0.002;
    /** After a task shard drops, how long that player earns no more from tasks (default 240 s). */
    public int taskShardCooldownSeconds = 240;
    /** Extra task-shard odds per point of luck (Fortune Finder / Luck potions), as a multiplier:
     *  effective chance = taskShardChance × (1 + luck × this). Default 0.1. 0 ignores luck. */
    public double taskShardLuckBonus = 0.1;
    /** Give the vanilla items with no repair material one that fits: trident ← prismarine,
     *  bow/crossbow/fishing rod ← string, flint-and-steel/shears ← iron ingot, brush ← copper ingot. */
    public boolean vanillaRepairMaterials = true;
    /** How much faster each Aquatic swim node makes you in water, as a fraction of your normal speed.
     *  Three nodes at the default is +60%, which a dolphin would still comfortably beat. 0 turns the
     *  lane's swim speed off and leaves only its seabed walking. */
    public double aquaticSwimSpeed = 0.2;
    /** Bone meal on a spore blossom grows another on a nearby ceiling — the one lush-cave plant vanilla
     *  gives no way to make more of. */
    public boolean bonemealSporeBlossoms = true;
    /** How much fishing luck (Luck of the Sea + the luck attribute) raises the crate catch rate:
     *  roughly +10% relative odds per point at 1 (default). 0 makes the rate ignore luck. */
    public int crateFishingLuckQuality = 1;
    /** Dragon Scales a player gets for killing the Ender Dragon (default 8). Only PLAYER kills drop. */
    public int dragonScaleDrop = 8;
    /** Dragon Scales for the world's very first player kill of the dragon (default 32). One time only. */
    public int dragonScaleFirstKillDrop = 32;
    /** Per-tier gear balance. See {@link GearTuning} for the shape and the tier ids. */
    public GearTuning gear = null; // filled with the shipped table on first write

    /** Fraction of melee damage a full Crystalline set reflects at the attacker (default 0.25). */
    public float crystalReflectFraction = 0.25f;
    /** Grant Strength + Resistance while a full Crystalline set is worn (default true). */
    public boolean crystalSetEffects = true;
    /** Level of those effects, as an amplifier: 0 = I, 1 = II (default 0). */
    public int crystalSetAmplifier = 0;
    /** Grant Fire Resistance while a full Rose Gold set is worn (default true). */
    public boolean roseGoldFireResistance = true;
    /** Dragon set dash launch speed (default 1.6). Set 0 to disable the dash entirely. */
    public double dragonDashSpeed = 1.6;
    /** How much the dash pulls you downward, so it reads as a swoop rather than a jump (default 0.3). */
    public double dragonDashDownBias = 0.3;
    /** Ticks between dashes (default 60 = 3s). */
    public long dragonDashCooldownTicks = 60L;
    /** Steel Shield durability (default 2400; a vanilla shield is 336). */
    public int steelShieldDurability = 2400;
    /** Movement penalty while the Steel Shield is held, as a fraction of walk speed (default -0.10). */
    public double steelShieldSlowdown = -0.10;
    /** Damage the Steel Shield deals back to a melee attacker (default 2.0). */
    public float steelShieldThorns = 2.0f;
    /** Update existing gear to the current tier numbers when its owner logs in (default true).
     *  Only durability and attribute modifiers are rewritten — enchantments, damage and anvil renames
     *  are left alone. Turn off to freeze old gear at whatever stats it was crafted with. */
    public boolean gearRestamp = true;

    /** Auto-push the VanillaSkills texture pack to joining clients (required). Default on. */
    public boolean serverResourcePack = true;
    /** Texture-pack download URL the server pushes (default = the GitHub release asset). */
    public String resourcePackUrl = DEFAULT_RP_URL;
    /** SHA-1 of that pack (lets clients cache it; update alongside the URL if you change the pack). */
    public String resourcePackSha1 = DEFAULT_RP_SHA1;
    /**
     * Where the Guide icon sends players. A URL opens the wiki as a clickable chat link; blank falls back
     * to the built-in written book, which is the offline answer and the one that still works if this page
     * ever moves. Point it at your own docs if you run a modified pack.
     */
    public String guideUrl = DEFAULT_GUIDE_URL;
    /**
     * How long after logging in an advancement completing is treated as "already had it" and pays nothing.
     *
     * <p>This is what makes it safe to add another mod's namespace to {@code countedNamespaces} in
     * points.json. A newly added mod completes its root advancement, and anything keyed on inventory or
     * statistics the player already satisfies, the instant they next log in — without this those all pay out
     * at once for no play. They are still recorded, so they never pay later either.
     *
     * <p>Set to 0 to disable and pay for everything.
     */
    public long advancementLoginGraceMs = 5000L;

    /**
     * Written into gameplay.json as {@code "#option-guide"} so anyone reading the file knows what each
     * option does without opening the source. JSON has no comments; an extra key is the next best thing.
     * Regenerated on every save, so hand-edits to it are harmless and stale text never survives.
     */
    @com.google.gson.annotations.SerializedName("#option-guide")
    public java.util.Map<String, String> optionGuide = buildOptionGuide();

    static java.util.Map<String, String> buildOptionGuide() {
        java.util.Map<String, String> g = new java.util.LinkedHashMap<>();
        g.put("mendingEnabled", "true = Mending exists as normal; false (default) = the mod strips Mending everywhere.");
        g.put("experienceEnabled", "true = vanilla XP returns; false (default) = XP is removed and anvils/infusing charge Skill Shards.");
        g.put("bountyRefreshHours", "Hours between bounty-board rotations.");
        g.put("shopRefreshHours", "Hours between Quest Shop rotations.");
        g.put("convertRatio", "Quest Shards needed to buy 1 Skill Shard at the shop converter.");
        g.put("graduateAt", "LEGACY, ignored - graduation is now 'finish every starter quest'.");
        g.put("toolCraftingRequirements", "Gate tool crafting behind the Toolsmith lane; false removes the gate and hides the lane.");
        g.put("armorCraftingRequirements", "Gate armor crafting behind the Armorsmith lane; false removes the gate and hides the lane.");
        g.put("deepslateGate", "Deepslate needs a Steel-tier-or-better pickaxe; false = vanilla rules.");
        g.put("fortuneBoost", "Fortune IV/V grant extra base ore drops; false = vanilla Fortune.");
        g.put("fortuneBonusChance", "Chance (0-1) of one extra base drop per Fortune level above III.");
        g.put("feats", "Enable Feats - one-time achievement rewards paid in Quest Shards.");
        g.put("skillTreeTitle", "Title shown on the skill tree screen.");
        g.put("skillTreeRows", "Rows in the skill tree GUI (1-6).");
        g.put("horseStats", "Show a horse's real speed, jump and health in its inventory screen.");
        g.put("shardsInXpBar", "Show banked Skill Shards on the XP bar (cosmetic packet; the server stays level 0).");
        g.put("copperHelmetLight", "A worn Copper Helmet lights the area - CLIENT-SIDE, modded clients only.");
        g.put("copperHelmetLightLevel", "Brightness of that light, 0-15.");
        g.put("infusingEnabled", "Right-clicking an enchanting table opens the Infusing Table; false keeps the vanilla screen.");
        g.put("infusingCostPerLevel", "Skill Shards per level of the enchantment being applied.");
        g.put("infusingCurrency", "'skill' or 'quest' - which shard the Infusing Table charges.");
        g.put("infusingHideIncompatible", "Hide enchantments the held item cannot take, instead of listing them greyed out after the ones that fit.");
        g.put("infusingConsumedBooks", "Enchantments whose shelved book IS consumed, each '<enchantment>:<minLevel>'. Empty = every book permanent.");
        g.put("netherRoofDamage", "Hurt players on the Nether roof, like the world border does.");
        g.put("netherRoofY", "Y level at or above which the roof rule applies.");
        g.put("netherRoofDamageAmount", "Damage per pulse above the roof.");
        g.put("starterQuests", "New players begin on the fixed starter board; false = straight to the rotating board.");
        g.put("questsPerRotation", "Quests the rotating board deals per rotation (1-6).");
        g.put("questShopSlots", "Offers the Quest Shop shows per rotation.");
        g.put("anvilMaterialPricing", "Price anvil work by what it consumes; false = vanilla's level formula charged in shards.");
        g.put("anvilRepairCostPerMaterial", "Shards per repair material consumed.");
        g.put("anvilEnchantCostPerLevel", "Shards per enchantment level on the sacrificed item in a combine.");
        g.put("anvilRenameCost", "Flat shards for a plain rename (0 = free).");
        g.put("anvilCombineRepairCost", "Flat shards when a same-item combine restores durability.");
        g.put("anvilTooExpensiveCap", "Restore vanilla's 40-level 'Too Expensive' cap.");
        g.put("anvilBooksOnItems", "Allow enchanted books directly onto gear at the anvil (the Infusing Table's job since 2.0).");
        g.put("dragonRepairCost", "Flat shards for the one-scale full repair of a Dragon piece.");
        g.put("shardAuraRadius", "Stable Skill Shard Block aura reach, in blocks.");
        g.put("shardAuraDamage", "Damage per pulse to hostiles inside the aura.");
        g.put("shardAuraIntervalTicks", "Ticks between aura pulses (20 = once a second).");
        g.put("shardMaxMerge", "How many Stable blocks can merge into one.");
        g.put("shardBeaconRangeMultiplier", "Beacon range multiplier on a Stable Skill Shard Block base.");
        g.put("shardBeaconAmplifierBonus", "Extra beacon amplifier levels on a Stable base.");
        g.put("shardWithdrawAmount", "Shards withdrawn as items per click of the withdraw button.");
        g.put("shardMiningGate", "Shard blocks need a Crystalline-or-better pickaxe (plain diamond does NOT qualify).");
        g.put("spawnerDropsShardBlock", "Broken spawners drop an Unstable Skill Shard Block.");
        g.put("shardChestWeight", "Structure-chest shard odds, weight vs shardChestEmptyWeight (1v60 = ~1.6% per chest; 0 disables).");
        g.put("shardChestEmptyWeight", "The 'nothing' side of the structure-chest roll.");
        g.put("shardBarterWeight", "Piglin-barter shard odds, weight vs shardBarterEmptyWeight (1v120 = ~0.8%; 0 disables).");
        g.put("shardBarterEmptyWeight", "The 'nothing' side of the barter roll.");
        g.put("crateFishingWeight", "Crate catch odds, weight vs crateFishingEmptyWeight (1v40 = ~2.4% per catch; 0 disables). WHICH crate is datapack loot.");
        g.put("crateFishingEmptyWeight", "The 'no crate' side of the fishing roll.");
        g.put("crateReelEnabled", "Show the slot-machine reel on crate open (the reward is identical either way).");
        g.put("crateReelTicks", "How long the reel spins, in ticks.");
        g.put("crateReelRadius", "Radius of the reel ring, in blocks.");
        g.put("crateReelDistance", "How far in front of the player the ring floats.");
        g.put("crateReelPool", "Distinct possibilities the ring shows.");
        g.put("crateReelScale", "Item size on the ring (1.0 = a full block).");
        g.put("crateReelSpacing", "Gap between reel cells, as a multiple of item scale.");
        g.put("crateReelHold", "Ticks the winning item is held before payout.");
        g.put("shardOreOverworldMinY", "Bottom of the Overworld band where generated shard ore is recognised - spawn RATE lives in the worldgen datapack.");
        g.put("shardOreOverworldMaxY", "Top of that Overworld band.");
        g.put("shardOreNetherMaxY", "Nether ceiling for recognised shard ore.");
        g.put("shardOreDrop", "Unstable Skill Shards dropped per generated ore block.");
        g.put("taskShardChance", "Chance (0-1) that mining/placing/harvesting drops a shard (0.002 = 1 in 500; 0 disables).");
        g.put("taskShardCooldownSeconds", "After a task shard drops, seconds before that player can earn another.");
        g.put("taskShardLuckBonus", "Task-shard odds bonus per point of luck: chance x (1 + luck x this).");
        g.put("vanillaRepairMaterials", "Give repair-less vanilla items a repair material (trident<-prismarine, bow/crossbow/rod<-string, flint-and-steel/shears<-iron, brush<-copper).");
        g.put("aquaticSwimSpeed", "Movement speed added per Aquatic swim node while in water (0.2 = +20% each).");
        g.put("bonemealSporeBlossoms", "Bone meal on a spore blossom grows another on a ceiling nearby, so they are renewable.");
        g.put("crateFishingLuckQuality", "Crate-rate bonus per point of fishing luck (~+10% relative per point at 1; 0 ignores luck).");
        g.put("dragonScaleDrop", "Dragon Scales per player dragon kill.");
        g.put("dragonScaleFirstKillDrop", "Scales for the world's FIRST player kill, one time.");
        g.put("gear", "Per-tier gear balance - armor, toughness, speed, durability, tool stats. Rewrites reach existing gear at login (see gearRestamp).");
        g.put("crystalReflectFraction", "Fraction of melee damage a full Crystalline set reflects.");
        g.put("crystalSetEffects", "Full Crystalline set grants Strength + Resistance.");
        g.put("crystalSetAmplifier", "Those effects' amplifier: 0 = level I.");
        g.put("roseGoldFireResistance", "Full Rose Gold set grants Fire Resistance.");
        g.put("dragonDashSpeed", "Dragon set dash speed (0 disables the dash).");
        g.put("dragonDashDownBias", "Downward pull on the dash so it reads as a swoop.");
        g.put("dragonDashCooldownTicks", "Ticks between dashes.");
        g.put("steelShieldDurability", "Steel Shield durability (a vanilla shield is 336).");
        g.put("steelShieldSlowdown", "Movement penalty while holding the Steel Shield, as a fraction of walk speed.");
        g.put("steelShieldThorns", "Damage the Steel Shield returns to melee attackers.");
        g.put("gearRestamp", "Bring existing gear onto the current tier numbers at login (enchants, damage and renames untouched).");
        g.put("serverResourcePack", "Push the VanillaSkills texture pack to joining clients.");
        g.put("resourcePackUrl", "Pack download URL pushed to clients.");
        g.put("resourcePackSha1", "SHA-1 of that pack, for client caching - update it alongside the URL.");
        g.put("guideUrl", "Where the Guide icon sends players; blank = the built-in written book.");
        g.put("advancementLoginGraceMs", "Ms after login during which advancements completing pay nothing (anti-windfall when adding a mod's namespace). 0 disables.");
        return g;
    }

    private static Path path() {
        Path dir = VanillaSkills.worldDir();
        return dir == null ? null : dir.resolve("gameplay.json");
    }

    /** Load gameplay.json from the current world (writing a default file if absent) and publish its values. */
    public static GameplayConfig load() {
        Path path = path();
        GameplayConfig cfg = new GameplayConfig();
        if (path != null) {
            try {
                if (Files.exists(path)) {
                    String raw = Files.readString(path);
                    GameplayConfig loaded = GSON.fromJson(raw, GameplayConfig.class);
                    if (loaded != null) {
                        cfg = loaded;
                        cfg.optionGuide = buildOptionGuide(); // never let stale on-disk help text win
                        boolean rewrite = cfg.migrateStalePack(); // servers pinned to a superseded pack
                        rewrite |= cfg.migrateSupersededDefaults(); // defaults that changed between releases
                        // A file written by an older version has none of the options added since. They work
                        // — Gson leaves them at their defaults — but an option nobody can see in the file is
                        // an option nobody can change, so write the full set back with existing values kept.
                        rewrite |= cfg.missingOptions(raw);
                        if (rewrite) cfg.save();
                    }
                } else {
                    cfg.save();
                }
            } catch (Exception e) {
                VanillaSkills.LOGGER.error("Failed to load gameplay.json, using defaults", e);
                cfg = new GameplayConfig();
            }
        }
        cfg.apply();
        return cfg;
    }

    /**
     * Defaults that changed between releases follow their default to the new value. A stored value
     * equal to the OLD default is almost certainly not a deliberate choice — it is what the mod wrote
     * for the admin — so it upgrades, exactly like the superseded pack URLs below. A value that
     * matches neither old nor new default is a real decision and is never touched.
     */
    private boolean migrateSupersededDefaults() {
        boolean changed = false;
        // 2.1.3 took the default 3 -> 2; 2.1.10 took it to 1, so a level-N enchantment costs N shards.
        if (infusingCostPerLevel == 3 || infusingCostPerLevel == 2) { infusingCostPerLevel = 1; changed = true; }
        return changed;
    }

    /** URLs of packs we've shipped as the default before; a config pinned to one of these predates the
     *  localized pack, so its custom item names show English. Auto-upgraded to the current default. */
    private static final java.util.Set<String> SUPERSEDED_RP_URLS = java.util.Set.of(
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v2.0.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v2.0.0-beta.1/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.7.6/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.7.5/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.7.4/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.7.3/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.7.2/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.7.1/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.7.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.6.5/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.6.4/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.6.3/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.6.2/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.6.1/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.6.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.5.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.4.6/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.4.5/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.4.4/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.4.3/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.4.2/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.4.1/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.4.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.3.4/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.3.3/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.3.2/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.3.1/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.3.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.2.9/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.2.8/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.2.7/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.2.6/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.2.5/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.2.2/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.2.1/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.2.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.1.1/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.1.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.0.9/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.0.8/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.0.7/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.0.6/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.0.5/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.0.4/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.0.3/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v1.0.0/VanillaSkills-TexturePack.zip",
            "https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/releases/download/v0.19.0/VanillaSkills-TexturePack.zip");

    /** If this config still points at a superseded default pack, move it to the current default.
     *  Only touches the exact old-default URLs — a hand-set custom URL is left alone. Returns true if changed. */
    private boolean migrateStalePack() {
        if (resourcePackUrl != null && SUPERSEDED_RP_URLS.contains(resourcePackUrl.trim())) {
            resourcePackUrl = DEFAULT_RP_URL;
            resourcePackSha1 = DEFAULT_RP_SHA1;
            return true;
        }
        return false;
    }

    /** Publish this config's values to the live flags / consumers (clamped to sane minimums). */
    public void apply() {
        MENDING_ENABLED = mendingEnabled;
        EXPERIENCE_ENABLED = experienceEnabled;
        TOOL_REQS_ENABLED = toolCraftingRequirements;
        ARMOR_REQS_ENABLED = armorCraftingRequirements;
        DEEPSLATE_GATE = deepslateGate;
        FORTUNE_BOOST = fortuneBoost;
        FORTUNE_BONUS_CHANCE = Math.max(0.0f, Math.min(1.0f, fortuneBonusChance));
        // Gear: fill the block in on first load so the written file documents the whole table, then push it.
        if (gear == null) gear = GearTuning.defaults();
        gear.apply();
        CRYSTAL_REFLECT_FRACTION = Math.max(0.0f, Math.min(1.0f, crystalReflectFraction));
        CRYSTAL_SET_EFFECTS = crystalSetEffects;
        CRYSTAL_SET_AMPLIFIER = Math.max(0, Math.min(4, crystalSetAmplifier));
        ROSE_GOLD_FIRE_RESISTANCE = roseGoldFireResistance;
        DRAGON_DASH_SPEED = Math.max(0.0, dragonDashSpeed);
        DRAGON_DASH_DOWN_BIAS = dragonDashDownBias;
        DRAGON_DASH_COOLDOWN_TICKS = Math.max(1L, dragonDashCooldownTicks);
        STEEL_SHIELD_DURABILITY = Math.max(1, steelShieldDurability);
        STEEL_SHIELD_SLOWDOWN = Math.min(0.0, steelShieldSlowdown);
        STEEL_SHIELD_THORNS = Math.max(0.0f, steelShieldThorns);
        GEAR_RESTAMP = gearRestamp;
        FEATS_ENABLED = feats;
        SKILL_TREE_TITLE = (skillTreeTitle == null || skillTreeTitle.isBlank()) ? "Skills" : skillTreeTitle;
        SKILL_TREE_ROWS = Math.max(1, Math.min(6, skillTreeRows));
        STARTER_QUESTS = starterQuests;
        QUESTS_PER_ROTATION = Math.max(1, Math.min(6, questsPerRotation));  // 6 quest slots in the GUI
        SHOP_SLOTS = Math.max(1, Math.min(45, questShopSlots));
        ANVIL_MATERIAL_PRICING = anvilMaterialPricing;
        ANVIL_REPAIR_PER_MATERIAL = Math.max(0, anvilRepairCostPerMaterial);
        ANVIL_ENCHANT_PER_LEVEL = Math.max(0, anvilEnchantCostPerLevel);
        ANVIL_RENAME_COST = Math.max(0, anvilRenameCost);
        ANVIL_COMBINE_REPAIR_COST = Math.max(0, anvilCombineRepairCost);
        ANVIL_TOO_EXPENSIVE_CAP = anvilTooExpensiveCap;
        ANVIL_BOOKS_ON_ITEMS = anvilBooksOnItems;
        DRAGON_REPAIR_COST = Math.max(0, dragonRepairCost);
        // Shard blocks. Radius is capped at 16 so a mis-set value cannot turn every placed block into a
        // server-wide entity sweep, and the interval floors at 1 tick to keep the modulo safe.
        SHARD_AURA_RADIUS = Math.max(1, Math.min(16, shardAuraRadius));
        SHARD_AURA_DAMAGE = Math.max(0.0f, shardAuraDamage);
        SHARD_AURA_INTERVAL = Math.max(1, shardAuraIntervalTicks);
        SHARD_MAX_MERGE = Math.max(1, Math.min(16, shardMaxMerge));
        SHARD_BEACON_RANGE_MULT = Math.max(1, shardBeaconRangeMultiplier);
        SHARD_BEACON_AMPLIFIER_BONUS = Math.max(0, shardBeaconAmplifierBonus);
        SHARD_WITHDRAW_AMOUNT = Math.max(1, shardWithdrawAmount);
        SHARD_MINING_GATE = shardMiningGate;
        SPAWNER_DROPS_SHARD_BLOCK = spawnerDropsShardBlock;
        SHARD_CHEST_WEIGHT = Math.max(0, shardChestWeight);
        SHARD_CHEST_EMPTY_WEIGHT = Math.max(1, shardChestEmptyWeight);
        SHARD_BARTER_WEIGHT = Math.max(0, shardBarterWeight);
        SHARD_BARTER_EMPTY_WEIGHT = Math.max(1, shardBarterEmptyWeight);
        CRATE_FISHING_WEIGHT = Math.max(0, crateFishingWeight);
        CRATE_FISHING_EMPTY_WEIGHT = Math.max(1, crateFishingEmptyWeight);
        CRATE_REEL_ENABLED = crateReelEnabled;
        CRATE_REEL_TICKS = Math.max(20, crateReelTicks);
        CRATE_REEL_RADIUS = Math.max(0.5, Math.min(4.0, crateReelRadius));
        CRATE_REEL_DISTANCE = Math.max(1.0, Math.min(6.0, crateReelDistance));
        CRATE_REEL_POOL = Math.max(3, Math.min(16, crateReelPool));
        CRATE_REEL_SCALE = Math.max(0.1, Math.min(2.0, crateReelScale));
        CRATE_REEL_SPACING = Math.max(0.5, Math.min(5.0, crateReelSpacing));
        CRATE_REEL_HOLD = Math.max(0, crateReelHold);
        SHARD_ORE_OVERWORLD_MIN_Y = shardOreOverworldMinY;
        SHARD_ORE_OVERWORLD_MAX_Y = Math.max(shardOreOverworldMinY, shardOreOverworldMaxY);
        SHARD_ORE_NETHER_MAX_Y = shardOreNetherMaxY;
        SHARD_ORE_DROP = Math.max(1, shardOreDrop);
        TASK_SHARD_CHANCE = Math.max(0.0, Math.min(1.0, taskShardChance));
        TASK_SHARD_COOLDOWN_SECONDS = Math.max(0, taskShardCooldownSeconds);
        TASK_SHARD_LUCK_BONUS = Math.max(0.0, taskShardLuckBonus);
        VANILLA_REPAIR_MATERIALS = vanillaRepairMaterials;
        AQUATIC_SWIM_SPEED = Math.max(0.0, aquaticSwimSpeed);
        BONEMEAL_SPORE_BLOSSOMS = bonemealSporeBlossoms;
        CRATE_FISHING_LUCK_QUALITY = Math.max(0, crateFishingLuckQuality);
        DRAGON_SCALE_DROP = Math.max(0, dragonScaleDrop);
        DRAGON_SCALE_FIRST_KILL_DROP = Math.max(0, dragonScaleFirstKillDrop);
        HORSE_STATS = horseStats;
        SHARDS_IN_XP_BAR = shardsInXpBar;
        COPPER_HELMET_LIGHT = copperHelmetLight;
        COPPER_HELMET_LIGHT_LEVEL = Math.max(0, Math.min(15, copperHelmetLightLevel));
        INFUSING_ENABLED = infusingEnabled;
        INFUSING_COST_PER_LEVEL = Math.max(0, infusingCostPerLevel);
        INFUSING_HIDE_INCOMPATIBLE = infusingHideIncompatible;
        INFUSING_CONSUMED_BOOKS = infusingConsumedBooks == null ? java.util.List.of() : java.util.List.copyOf(infusingConsumedBooks);
        INFUSING_CURRENCY = "quest".equalsIgnoreCase(infusingCurrency) ? "quest" : "skill";
        NETHER_ROOF_DAMAGE = netherRoofDamage;
        NETHER_ROOF_Y = netherRoofY;
        NETHER_ROOF_DAMAGE_AMOUNT = Math.max(0.0f, netherRoofDamageAmount);
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.invalidate(); // re-read lang files on reload
        BOUNTY_REFRESH_MS = Math.max(1, bountyRefreshHours) * 3_600_000L;
        SHOP_REFRESH_MS = Math.max(1, shopRefreshHours) * 3_600_000L;
        QuestShop.CONVERT_RATIO = Math.max(1, convertRatio);
        Quests.GRADUATE_AT = Math.max(1, graduateAt);
        PUSH_RESOURCE_PACK = serverResourcePack;
        RESOURCE_PACK_URL = resourcePackUrl == null ? "" : resourcePackUrl;
        RESOURCE_PACK_SHA1 = resourcePackSha1 == null ? "" : resourcePackSha1;
        GUIDE_URL = guideUrl == null ? "" : guideUrl.trim();
        ADVANCEMENT_LOGIN_GRACE_MS = Math.max(0L, advancementLoginGraceMs);
    }

    public void save() {
        Path path = path();
        if (path == null) return; // no world loaded
        // Fill the gear table in before writing, so a freshly-created gameplay.json documents every tier's
        // numbers instead of an unhelpful "gear": null. save() runs before apply() on a brand-new world.
        if (gear == null) gear = GearTuning.defaults();
        optionGuide = buildOptionGuide(); // the written guide always matches this build's options
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this));
        } catch (IOException e) {
            VanillaSkills.LOGGER.error("Failed to save gameplay.json", e);
        }
    }

    /**
     * True if the file on disk is missing any option this version defines.
     *
     * <p>Compared at the top level only. That is enough — a nested object such as {@code gear} is written
     * whole or not at all — and it keeps the check cheap and predictable.
     */
    private boolean missingOptions(String raw) {
        try {
            com.google.gson.JsonObject onDisk = GSON.fromJson(raw, com.google.gson.JsonObject.class);
            if (onDisk == null) return false;
            com.google.gson.JsonObject full = GSON.toJsonTree(this).getAsJsonObject();
            for (String key : full.keySet()) {
                if (!onDisk.has(key)) return true;
            }
        } catch (Exception e) {
            return false; // unreadable JSON is the load path's problem, not this one's
        }
        return false;
    }
}
