package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Builds the skill tree from the datapacks.
 *
 * <p>The tree is content, not save data: lanes come from {@code vanillaskills/skill_category} and nodes from
 * {@code vanillaskills/skill_node}, so a pack can add, retune or replace any part of it without code. What
 * stays per-world is only the players' progress through it.
 *
 * <p>Worlds from before this carried the tree in {@code skilltree.json}; that file is rescued into a world
 * datapack once and then retired, so in-game customisations are not silently lost.
 */
public class SkillTreeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SkillTree tree = new SkillTree();

    public SkillTree tree() {
        return tree;
    }

    private static Path path() {
        Path dir = VanillaSkills.worldDir();
        return dir == null ? null : dir.resolve("skilltree.json");
    }

    /**
     * Build the tree from the datapacks.
     *
     * <p>The datapack is authoritative. A per-world {@code skilltree.json} is no longer read as the tree —
     * it is only inspected once, to rescue customisations from worlds that predate this (see
     * {@link #migrateLegacyTree}).
     *
     * <p><b>Player progress is unaffected by this change.</b> Unlocked skills are stored as a set of node
     * <i>ids</i>, and the ids are unchanged, so every existing unlock still resolves. That is the entire
     * reason this migration is safe to do at all.
     *
     * <p>Falls back to the built-in generated tree if the datapack somehow yields nothing — a server with no
     * skill tree at all would be far worse than one running the defaults.
     */
    public void load() {
        migrateLegacyTree();

        if (io.github.andrewwwwwwwwwwwwwww.vanillaskills.data.VsContent.hasSkillTree()) {
            tree = fromDatapack();
        } else {
            VanillaSkills.LOGGER.warn(
                    "No skill nodes found in any datapack — falling back to the built-in tree. "
                            + "Is the mod's bundled datapack enabled?");
            tree = defaultTree();
        }
        tree.index();
        applyEconomy(tree, economyP);
        VanillaSkills.LOGGER.info("Loaded skill tree '{}' with {} nodes from {}",
                tree.title, tree.size(),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.data.VsContent.hasSkillTree()
                        ? "datapack" : "built-in default");
    }

    /** Assemble the runtime tree from the loaded datapack definitions. */
    private static SkillTree fromDatapack() {
        SkillTree t = new SkillTree();
        t.title = io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.SKILL_TREE_TITLE;
        t.rows = io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.SKILL_TREE_ROWS;
        for (var def : io.github.andrewwwwwwwwwwwwwww.vanillaskills.data.VsContent.skillCategories()) {
            t.categories.add(def.toCategory());
        }
        for (var def : io.github.andrewwwwwwwwwwwwwww.vanillaskills.data.VsContent.skillNodes()) {
            t.nodes.add(def.toNode());
        }
        return t;
    }

    /**
     * Rescue a pre-datapack world's customised tree, once.
     *
     * <p>Before 2.0 the tree lived in {@code skilltree.json} and ops could edit it in game. Now that the
     * datapack owns it, that file would simply stop being read — silently discarding those edits. Instead it
     * is converted into a world datapack that overrides the built-ins, so a customised tree keeps working and
     * becomes editable the same way any other pack content is.
     *
     * <p>The original is renamed rather than deleted, so nothing is destroyed even if the conversion is
     * imperfect. Runs at most once: it is skipped entirely once the marker file exists.
     */
    private void migrateLegacyTree() {
        // Needs a world on disk to read from and write to. load() also runs from the datapack reload
        // listener, which fires once before the server exists at all.
        if (VanillaSkills.server == null) return;
        Path legacy = path();
        if (legacy == null || !Files.exists(legacy)) return;

        Path dir = VanillaSkills.worldDir();
        Path marker = dir.resolve("skilltree.migrated");
        if (Files.exists(marker)) return;

        try {
            SkillTree old = GSON.fromJson(Files.readString(legacy), SkillTree.class);
            if (old == null || old.nodes == null || old.nodes.isEmpty()) {
                Files.move(legacy, marker);
                return;
            }
            old.index();

            Path packRoot = VanillaSkills.server.getWorldPath(
                            net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("datapacks").resolve("vanillaskills-legacy-tree");
            Path content = packRoot.resolve("data").resolve("vanillaskills").resolve("vanillaskills");
            Files.createDirectories(content.resolve("skill_category"));
            Files.createDirectories(content.resolve("skill_node"));

            Files.writeString(packRoot.resolve("pack.mcmeta"), """
                    {
                      "pack": {
                        "description": "VanillaSkills - skill tree migrated from this world's skilltree.json",
                        "pack_format": 107
                      }
                    }
                    """);

            // "replace": true, because this world's tree is meant to BE the tree, not to add to the defaults.
            StringBuilder cats = new StringBuilder("{\n  \"replace\": true,\n  \"values\": [\n");
            for (int i = 0; i < old.categories.size(); i++) {
                SkillCategory c = old.categories.get(i);
                cats.append("    ").append(GSON.toJson(c));
                if (i < old.categories.size() - 1) cats.append(',');
                cats.append('\n');
            }
            cats.append("  ]\n}\n");
            Files.writeString(content.resolve("skill_category").resolve("migrated.json"), cats.toString());

            StringBuilder nodes = new StringBuilder("{\n  \"replace\": true,\n  \"values\": [\n");
            for (int i = 0; i < old.nodes.size(); i++) {
                nodes.append("    ").append(GSON.toJson(legacyNodeAsDef(old.nodes.get(i))));
                if (i < old.nodes.size() - 1) nodes.append(',');
                nodes.append('\n');
            }
            nodes.append("  ]\n}\n");
            Files.writeString(content.resolve("skill_node").resolve("migrated.json"), nodes.toString());

            Files.move(legacy, marker);
            VanillaSkills.LOGGER.info(
                    "Migrated this world's skilltree.json into a datapack at world/datapacks/"
                            + "vanillaskills-legacy-tree ({} lanes, {} nodes). The original was renamed to "
                            + "skilltree.migrated. Run /reload or restart for it to take effect; delete the "
                            + "pack to fall back to the built-in tree.",
                    old.categories.size(), old.nodes.size());
        } catch (Exception e) {
            // Never fatal: a failed rescue must not stop the server, and the original file is still there.
            VanillaSkills.LOGGER.error("Could not migrate skilltree.json to a datapack — the built-in tree "
                    + "will be used and your old file has been left untouched", e);
        }
    }

    /** The datapack shape of a legacy node: same fields, with {@code cost} carried across as {@code weight}. */
    private static io.github.andrewwwwwwwwwwwwwww.vanillaskills.data.SkillNodeDef legacyNodeAsDef(SkillNode n) {
        var def = new io.github.andrewwwwwwwwwwwwwww.vanillaskills.data.SkillNodeDef();
        def.id = n.id;
        def.title = n.title;
        def.description = n.description;
        def.icon = n.icon;
        def.weight = Math.max(1, n.cost);
        def.minEarned = n.minEarned;
        def.currency = n.currency;
        def.requires = n.requires;
        def.category = n.category;
        def.slot = n.slot;
        def.effects = n.effects;
        return def;
    }


    /** Re-index and persist after an edit. */

    /**
     * Regenerate the tree from the built-in default, backing up the existing skilltree.json first.
     * <p>When {@code preserve} is true (the normal {@code /skill regen}), an op's existing tree is kept
     * concrete — every current lane/node is left exactly as-is and only brand-new lanes/nodes from the
     * built-in default are appended. When false ({@code /skill regen fresh}), the tree is fully reset to
     * the built-in default (discarding op customizations).
     *
     * @return the backup file path, or null if there was no existing file (or the backup failed).
     */

    // ---- Default starter tree (5 lanes: Health, Speed, Mining, Luck, Damage) ----

    // Every lane uses one centred layout (see gridSlots): rows of up to five, each centred, starting on
    // row 1 directly under the lane header and clear of the bottom buttons. Named aliases below keep the
    // lane definitions readable.
    private static final int[] TIER_SLOTS = gridSlots(3);     // one centred row of 3
    private static final int[] TIER_SLOTS_5 = gridSlots(5);   // one centred row of 5
    private static final int[] FIVE_AND_FIVE = gridSlots(10); // two centred rows of 5 (5x2)
    // Quest-Shard cost ramp for the 10-tier Armorsmith/Toolsmith ladders (cheap early, steep at the top).
    private static final int[] TIER_COSTS = {1, 2, 3, 5, 8, 12, 20, 35, 60, 100};
    // Lane icons on the lane-select screen. Indexed by laneIndex. Two zones:
    //   SKILLS (Skill Shards) — row 1 (10-16): Vitality, Fleet, Warrior, Guardian, Reach, Evasion, Mountaineer
    //                           row 2 (20-24): Prospector, Fortune, Aquatic, Cultivator, Brewmaster
    //   CRAFTING (Quest Shards) — row 4: Armorsmith (39), Recipes (40), Toolsmith (41)
    //   Night Vision capstone — bottom-centre (49), between the Points/Stats buttons.
    // laneIndex:                  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14
    // lane:                    health spd min luck dmg grd rch mtn aqu arm tool brw eva cul nv
    private static final int[] CATEGORY_SLOTS = {10, 11, 20, 21, 12, 13, 14, 16, 22, 39, 41, 24, 15, 23, 40};

    private static SkillTree defaultTree() {
        SkillTree t = new SkillTree();
        t.version = 1;
        t.title = "Skills";
        t.rows = 6;

        // Vitality: +2 hearts (4 HP) per node over 10 nodes (+40 HP). End-loaded cost ramp.
        addScalingLane(t, "health", "Vitality", "minecraft:golden_apple", 0,
                "minecraft:max_health", "add_value", 4.0, gridSlots(10),
                new int[]{2, 2, 3, 4, 5, 7, 10, 14, 23, 30},
                n -> "+2 hearts — +" + (4 * n) + " HP total");

        // Fleet Foot: +2% movement speed per node, up to +30% (15 nodes).
        addScalingLane(t, "speed", "Fleet Foot", "minecraft:feather", 1,
                "minecraft:movement_speed", "add_multiplied_base", 0.02, gridSlots(15),
                new int[]{1, 1, 1, 2, 2, 3, 3, 4, 5, 6, 8, 11, 15, 18, 20},
                n -> "+2% movement speed — +" + (2 * n) + "% total");

        // Prospector: 5 nodes totalling +12 mining efficiency, enough that an Efficiency V diamond
        // pickaxe (speed 34) clears the 45-speed instamine threshold for stone — i.e. Haste II speeds.
        addLaneNodes(t, "mining", "Prospector", "minecraft:diamond_pickaxe", 2,
                TIER_SLOTS_5, new int[]{3, 6, 9, 14, 18},
                new SkillEffect[][]{
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 2.0)},
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 2.0)},
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 2.0)},
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 3.0)},
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 3.0)}
                },
                new String[]{"+2 mining efficiency", "+2 (4 total)", "+2 (6 total)", "+3 (9 total)",
                        "+3 (12 total) — instamine stone with an Efficiency V diamond pickaxe"});

        // Fortune Finder: +0.5 luck per node, up to +5 (10 nodes).
        addScalingLane(t, "luck", "Fortune Finder", "minecraft:rabbit_foot", 3,
                "minecraft:luck", "add_value", 0.5, FIVE_AND_FIVE,
                new int[]{1, 1, 2, 2, 3, 4, 5, 7, 11, 14},
                n -> "+0.5 luck — +" + fmt(0.5 * n) + " total");

        // Warrior: each node adds +0.5 FLAT attack damage AND +3% WEAPON damage. The percentage uses
        // add_multiplied_total, so it scales with the total damage of whatever you're holding — a real
        // weapon benefits far more than a bare fist, which keeps swords ahead (a flat-only bonus made
        // the fast-swinging fist out-DPS slow weapons). Maxes at +5 flat and +30%. End-loaded ramp.
        {
            int n = 10;
            SkillEffect[][] warrior = new SkillEffect[n][];
            String[] warriorDesc = new String[n];
            for (int i = 0; i < n; i++) {
                warrior[i] = new SkillEffect[]{
                        SkillEffect.attribute("minecraft:attack_damage", "add_value", 0.5),
                        SkillEffect.attribute("minecraft:attack_damage", "add_multiplied_total", 0.03)};
                int lvl = i + 1;
                warriorDesc[i] = "+0.5 dmg & +3% weapon damage — +" + fmt(0.5 * lvl) + " flat, +" + (3 * lvl) + "%";
            }
            addLaneNodes(t, "damage", "Warrior", "minecraft:iron_sword", 4, gridSlots(10),
                    new int[]{2, 2, 3, 4, 5, 7, 10, 14, 23, 30}, warrior, warriorDesc);
        }

        // Guardian: +1 armor per node, up to +10 (10 nodes). End-loaded — maxing is a real investment.
        addScalingLane(t, "guardian", "Guardian", "minecraft:iron_chestplate", 5,
                "minecraft:armor", "add_value", 1.0, FIVE_AND_FIVE,
                new int[]{2, 2, 3, 4, 5, 7, 10, 14, 23, 30},
                n -> "+1 armor — +" + n + " total");

        // Reach: +0.5 block & entity interaction range per node, up to +2.5 (5 nodes). Pricey — extra
        // reach is very strong, and the ramp climbs hard.
        addScalingLaneMulti(t, "reach", "Reach", "minecraft:spyglass", 6, TIER_SLOTS_5,
                new int[]{5, 12, 22, 34, 42},
                new String[]{"minecraft:block_interaction_range", "minecraft:entity_interaction_range"},
                "add_value", 0.5,
                n -> "+0.5 block & entity reach — +" + fmt(0.5 * n) + " total");

        // Mountaineer: pricier than the other short lanes — auto-stepping full blocks is strong QoL.
        addLaneNodes(t, "mountaineer", "Mountaineer", "minecraft:ladder", 7,
                TIER_SLOTS, new int[]{4, 8, 13},
                new SkillEffect[][]{
                        {SkillEffect.attribute("minecraft:step_height", "add_value", 0.2)},
                        {SkillEffect.attribute("minecraft:step_height", "add_value", 0.2)},
                        {SkillEffect.attribute("minecraft:step_height", "add_value", 0.1)}
                },
                new String[]{"Step up taller ledges (sneak to walk normally)", "Step up a full block",
                        "Step up 1.1 blocks — /skill toggle stepup"});

        // Aquatic: 6 nodes spreading two underwater perks — breath and swim speed.
        // Costs ramp up steeply so reaching full underwater capability is a real investment.
        addLaneNodes(t, "aquatic", "Aquatic", "minecraft:heart_of_the_sea", 8,
                gridSlots(6), new int[]{3, 5, 7, 9, 12, 15},
                new SkillEffect[][]{
                        {SkillEffect.attribute("minecraft:oxygen_bonus", "add_value", 1.0)},
                        {SkillEffect.attribute("minecraft:oxygen_bonus", "add_value", 1.0)},
                        {SkillEffect.attribute("minecraft:oxygen_bonus", "add_value", 1.0)},
                        // Swim speed is the mod's own, graduated across these three nodes — see SwimSpeed.
                        // water_movement_efficiency is the other half and only governs walking on the seabed;
                        // it is capped at 1.0, which these three reach together.
                        {SkillEffect.attribute("minecraft:water_movement_efficiency", "add_value", 0.34),
                                SkillEffect.flag("swim_speed_1")},
                        {SkillEffect.attribute("minecraft:water_movement_efficiency", "add_value", 0.33),
                                SkillEffect.flag("swim_speed_2")},
                        {SkillEffect.attribute("minecraft:water_movement_efficiency", "add_value", 0.33),
                                SkillEffect.flag("swim_speed_3")}
                },
                new String[]{"+1 breath", "+1 breath (+2)", "+1 breath (+3)",
                        "Swim faster — and it stacks with Depth Strider",
                        "Swim faster still, and walk through water at land speed",
                        "Fastest swimming the lane offers, and full water movement"});

        // Armorsmith: a 10-tier ladder (paid in QUEST SHARDS) that gates crafting each armour tier,
        // climbing Hardwood → Copper → Gold → Rose Gold → Iron → Steel → Diamond → Crystalline →
        // Netherite → Dragon. Strict chain; pricier toward the top.
        addLaneNodes(t, "armorsmith", "Armorsmith", "minecraft:smithing_table", 9, FIVE_AND_FIVE,
                TIER_COSTS,
                flagEffects("craft_armor_hardwood", "craft_armor_copper", "craft_armor_gold",
                        "craft_armor_rose_gold", "craft_armor_iron", "craft_armor_steel",
                        "craft_armor_diamond", "craft_armor_crystal", "craft_armor_netherite",
                        "craft_armor_dragon"),
                new String[]{"Craft Hardwood armor", "Craft Copper armor", "Craft Gold armor",
                        "Craft Rose Gold armor", "Craft Iron armor", "Craft Steel armor",
                        "Craft Diamond armor", "Craft Crystalline armor", "Craft Netherite armor",
                        "Craft Dragon armor"});
        markQuestTierLane(t, "armorsmith", new String[]{
                "minecraft:leather_chestplate", "minecraft:copper_chestplate", "minecraft:golden_chestplate",
                "minecraft:golden_chestplate", "minecraft:iron_chestplate", "minecraft:iron_chestplate",
                "minecraft:diamond_chestplate", "minecraft:diamond_chestplate", "minecraft:netherite_chestplate",
                "minecraft:netherite_chestplate"});

        // Toolsmith: the same 10-tier Quest-Shard ladder, gating each tool tier.
        addLaneNodes(t, "toolsmith", "Toolsmith", "minecraft:anvil", 10, FIVE_AND_FIVE,
                TIER_COSTS,
                flagEffects("craft_tool_hardwood", "craft_tool_copper", "craft_tool_gold",
                        "craft_tool_rose_gold", "craft_tool_iron", "craft_tool_steel",
                        "craft_tool_diamond", "craft_tool_crystal", "craft_tool_netherite",
                        "craft_tool_dragon"),
                new String[]{"Craft Hardwood tools", "Craft Copper tools", "Craft Gold tools",
                        "Craft Rose Gold tools", "Craft Iron tools", "Craft Steel tools",
                        "Craft Diamond tools", "Craft Crystalline tools", "Craft Netherite tools",
                        "Craft Dragon tools"});
        markQuestTierLane(t, "toolsmith", new String[]{
                "minecraft:stone_pickaxe", "minecraft:copper_pickaxe", "minecraft:golden_pickaxe",
                "minecraft:golden_pickaxe", "minecraft:iron_pickaxe", "minecraft:iron_pickaxe",
                "minecraft:diamond_pickaxe", "minecraft:diamond_pickaxe", "minecraft:netherite_pickaxe",
                "minecraft:netherite_pickaxe"});

        // Brewmaster: each node extends beneficial potion durations by a further +10% (up to +50%).
        addLaneNodes(t, "brewmaster", "Brewmaster", "minecraft:brewing_stand", 11, TIER_SLOTS_5,
                new int[]{8, 14, 20, 26, 32},
                flagEffects("long_potions_1", "long_potions_2", "long_potions_3", "long_potions_4", "long_potions_5"),
                new String[]{"Beneficial potions +10% duration", "+20% total", "+30% total", "+40% total",
                        "+50% total"});

        // Evasion: +2% chance to completely dodge incoming arrow damage per node, up to 20%.
        // Expensive — fully dodging 1-in-5 arrows is a strong defensive perk.
        addLaneNodes(t, "evasion", "Evasion", "minecraft:arrow", 12,
                FIVE_AND_FIVE, new int[]{2, 2, 3, 4, 5, 7, 10, 14, 23, 30},
                flagEffects("arrow_dodge_1", "arrow_dodge_2", "arrow_dodge_3", "arrow_dodge_4", "arrow_dodge_5",
                        "arrow_dodge_6", "arrow_dodge_7", "arrow_dodge_8", "arrow_dodge_9", "arrow_dodge_10"),
                new String[]{"+2% chance to dodge arrows", "4% total", "6% total", "8% total", "10% total",
                        "12% total", "14% total", "16% total", "18% total", "20% total"});

        // Cultivator: each node adds +20% chance for bonus crops when harvesting mature crops (→100%).
        addLaneNodes(t, "cultivator", "Cultivator", "minecraft:wheat", 13,
                TIER_SLOTS_5, new int[]{3, 6, 9, 14, 18},
                flagEffects("cultivator_1", "cultivator_2", "cultivator_3", "cultivator_4", "cultivator_5"),
                new String[]{"+20% chance for bonus crops", "40% chance", "60% chance", "80% chance",
                        "100% chance (always bonus)"});

        // Night Vision: a single capstone node granting permanent night vision.
        addLaneNodes(t, "nightvision", "Night Vision", "minecraft:golden_carrot", 14,
                new int[]{22}, new int[]{75},
                new SkillEffect[][]{ { SkillEffect.status("minecraft:night_vision", 0) } },
                new String[]{"Permanent Night Vision"});

        // Movable pseudo-lanes (no nodes): Recipes opens the recipe book (under Armorsmith), Guide opens
        // the /skill guide book (under Toolsmith), Bounty Board opens the quest screen (under Night Vision).
        t.categories.add(new SkillCategory("recipes", "Recipes", "minecraft:crafting_table", 48));
        t.categories.add(new SkillCategory("guide", "Guide", "minecraft:written_book", 50));
        t.categories.add(new SkillCategory("quests", "Bounty Board", "minecraft:clock", 49));

        applyEconomy(t, economyP);
        return t;
    }

    /** P = total earnable Skill Shards, set at server start so the default tree can be priced against it. */
    public static int economyP = 0;

    /**
     * Price the tree against the total earnable points (P): Night Vision costs a flat 75 and is gated
     * behind earning P/3, while every other node is scaled so the whole tree (including NV) sums to
     * exactly P — so doing every advancement affords the entire tree once.
     */
    private static void applyEconomy(SkillTree t, int P) {
        if (P <= 80) return; // not computed yet (e.g. before server start) — leave hand-tuned costs
        final int nvCost = 75;

        // ⚠ Scales from the AUTHORED WEIGHT each time, never from the previous cost.
        //
        // This used to read and write n.cost, which meant the result was only correct on the first run: a
        // second /reload scaled already-scaled values and the whole economy drifted downward. Weights are
        // now the fixed input and cost is derived output, so this is idempotent however often it runs.
        //
        // Night Vision is fixed (75); Armorsmith/Toolsmith are funded by Quest Shards rather than Skill
        // Shards, so they sit outside the Skill-Shard budget. Everything else scales so the Skill-Shard tree
        // (core lanes + NV) sums to exactly P — complete every advancement and you can afford it all once.
        int baseSum = 0;
        for (SkillNode n : t.nodes) {
            if (isFixedOrQuestLane(n)) continue;
            baseSum += Math.max(1, n.weight);
        }
        if (baseSum <= 0) return;
        double f = (double) (P - nvCost) / baseSum;
        for (SkillNode n : t.nodes) {
            if ("nightvision".equals(n.category)) {
                n.cost = nvCost;
                n.minEarned = Math.round(P / 3.0f);
            } else if (!isFixedOrQuestLane(n)) {
                n.cost = Math.max(1, (int) Math.round(Math.max(1, n.weight) * f));
            } else {
                n.cost = Math.max(1, n.weight);   // quest-funded lanes: weight IS the price
            }
        }
    }

    /**
     * Total authored weight of the nodes the economy actually scales — the divisor {@link #applyEconomy}
     * prices against. Exposed so {@link RemovedNodes} can reconstruct what a since-deleted node used to
     * cost: add its weight back to this and the old scale factor falls out.
     */
    public static int scalableWeight(SkillTree t) {
        int sum = 0;
        for (SkillNode n : t.nodes) {
            if (isFixedOrQuestLane(n)) continue;
            sum += Math.max(1, n.weight);
        }
        return sum;
    }

    /** Night Vision fixed price, held outside the scaled budget. */
    public static final int NIGHT_VISION_COST = 75;

    private static boolean isFixedOrQuestLane(SkillNode n) {
        return "nightvision".equals(n.category) || "armorsmith".equals(n.category) || "toolsmith".equals(n.category);
    }

    /** A many-node lane where each node adds {@code perNode} of one attribute, with an explicit cost ramp. */
    private static void addScalingLane(SkillTree t, String key, String name, String icon, int laneIndex,
                                       String attribute, String operation, double perNode, int[] slots, int[] costs,
                                       java.util.function.IntFunction<String> describe) {
        int count = slots.length;
        SkillEffect[][] effects = new SkillEffect[count][];
        String[] descriptions = new String[count];
        for (int i = 0; i < count; i++) {
            effects[i] = new SkillEffect[]{SkillEffect.attribute(attribute, operation, perNode)};
            descriptions[i] = describe.apply(i + 1);
        }
        addLaneNodes(t, key, name, icon, laneIndex, slots, costs, effects, descriptions);
    }

    /** {@link #addScalingLaneMulti} with an explicit (hand-authored, end-loaded) cost ramp. */
    private static void addScalingLaneMulti(SkillTree t, String key, String name, String icon, int laneIndex,
                                            int[] slots, int[] costs, String[] attributes, String operation, double perNode,
                                            java.util.function.IntFunction<String> describe) {
        int count = slots.length;
        SkillEffect[][] effects = new SkillEffect[count][];
        String[] descriptions = new String[count];
        for (int i = 0; i < count; i++) {
            SkillEffect[] node = new SkillEffect[attributes.length];
            for (int a = 0; a < attributes.length; a++) node[a] = SkillEffect.attribute(attributes[a], operation, perNode);
            effects[i] = node;
            descriptions[i] = describe.apply(i + 1);
        }
        addLaneNodes(t, key, name, icon, laneIndex, slots, costs, effects, descriptions);
    }

    /**
     * Lays {@code count} node slots in a tidy CENTRED block: rows of up to five, each row centred
     * horizontally, starting on row 2 — row 0 holds the lane header and row 1 is deliberately left
     * EMPTY as a visual separator so players don't mistake the header for a clickable skill. Stays
     * clear of the bottom-row Back/Points/Stats buttons. 9 nodes form a 3x3; 10 → 5x2; 15 → 5x3;
     * 1–5 → a single centred row. So every lane reads as an even, centred grid under the header.
     */
    private static int[] gridSlots(int count) {
        int width = switch (count) {        // nodes per row, chosen so the lane forms a tidy block
            case 9 -> 3;                    // 3x3
            default -> Math.min(5, Math.max(1, count)); // 1-5 -> one centred row; 10 -> 5x2; 15 -> 5x3
        };
        int startCol = (9 - width) / 2;     // centre each row (width 5 -> cols 2-6, width 3 -> cols 3-5)
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            int row = 2 + i / width;        // row 2 down, leaving row 1 as a gap under the header
            int col = startCol + i % width;
            slots[i] = row * 9 + col;
        }
        return slots;
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    /** Roman numeral for any positive count (lane node titles can exceed VI). */
    private static String roman(int n) {
        if (n <= 0) return String.valueOf(n);
        int[] values = {100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (n >= values[i]) { sb.append(symbols[i]); n -= values[i]; }
        }
        return sb.toString();
    }

    /** Mark a tier lane's nodes as Quest-Shard currency and give each node its tier's item icon. */
    private static void markQuestTierLane(SkillTree t, String category, String[] icons) {
        for (SkillNode n : t.nodes) {
            if (!category.equals(n.category)) continue;
            n.currency = "quest";
            try {
                int idx = Integer.parseInt(n.id.substring(category.length() + 1)) - 1;
                if (idx >= 0 && idx < icons.length) n.icon = icons[idx];
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static SkillEffect[][] flagEffects(String... flags) {
        SkillEffect[][] effects = new SkillEffect[flags.length][];
        for (int i = 0; i < flags.length; i++) effects[i] = new SkillEffect[]{SkillEffect.flag(flags[i])};
        return effects;
    }

    /** A lane with one node per entry, using the given slots/costs/effects (with a prerequisite chain). */
    private static void addLaneNodes(SkillTree t, String key, String name, String icon, int laneIndex,
                                     int[] slots, int[] costs, SkillEffect[][] effectsPerNode, String[] descriptions) {
        t.categories.add(new SkillCategory(key, name, icon, CATEGORY_SLOTS[laneIndex]));
        for (int i = 0; i < slots.length; i++) {
            SkillNode node = new SkillNode(key + "_" + (i + 1), name + " " + roman(i + 1), key, slots[i], costs[i], icon);
            node.description.add(descriptions[i]);
            for (SkillEffect effect : effectsPerNode[i]) node.effects.add(effect);
            if (i > 0) node.requires.add(key + "_" + i);
            t.nodes.add(node);
        }
    }
}
