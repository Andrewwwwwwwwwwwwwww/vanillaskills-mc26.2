package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTier;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTiers;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTier;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTiers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Skill-gated crafting: VanillaSkills armor and tools can be crafted only once the player has
 * unlocked the matching skill (which grants a "flag" effect). The raw materials/ingots are never
 * gated — only the finished gear. Enforced via {@code CraftResultGateMixin} on the crafting result
 * slot's {@code mayPickup}, so the item shows but can't be taken until the skill is unlocked — with an
 * action-bar line naming the missing skill, so the refusal is never mistaken for a broken recipe.
 */
public final class CraftingGate {
    private CraftingGate() {}

    /** True if this crafted stack is gear whose per-tier craft skill isn't unlocked (custom OR vanilla).
     *  The tool/armor requirement systems can each be disabled entirely in gameplay.json. */
    public static boolean isLocked(Player player, ItemStack stack) {
        String flag = requiredFlag(stack);
        return flag != null && !hasFlag(player, flag);
    }

    /**
     * The skill flag this stack's tier demands, or null when it is not gated at all — either because it is
     * not gear, or because its requirement system is switched off in gameplay.json.
     *
     * <p>Split out of {@link #isLocked} so the refusal can say <i>which</i> skill is missing. A gate that
     * silently declines is indistinguishable from a broken recipe, which is exactly how it was read.
     */
    public static String requiredFlag(ItemStack stack) {
        if (stack.isEmpty()) return null;
        boolean armorReqs = io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.ARMOR_REQS_ENABLED;
        boolean toolReqs = io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.TOOL_REQS_ENABLED;
        // Custom tiers first (marker-based) — these reuse vanilla base items + a marker.
        for (ArmorTier tier : ArmorTiers.TIERS) {
            if (tier.isWorn(stack)) return armorReqs ? "craft_armor_" + tier.id : null;
        }
        for (ToolTier tier : ToolTiers.TIERS) {
            if (Markers.has(stack, tier.markerKey)) return toolReqs ? "craft_tool_" + tier.id : null;
        }
        // Vanilla tiers (no custom marker): copper/gold/iron/diamond/netherite armour & tools.
        String armorTier = VANILLA_ARMOR.get(stack.getItem());
        if (armorTier != null) return armorReqs ? "craft_armor_" + armorTier : null;
        String toolTier = VANILLA_TOOL.get(stack.getItem());
        if (toolTier != null) return toolReqs ? "craft_tool_" + toolTier : null;
        return null;
    }

    /** The skill-tree node that grants a flag, so a refusal can name it rather than the internal id. */
    public static SkillNode nodeForFlag(String flag) {
        SkillTree tree = VanillaSkills.TREE.tree();
        if (tree == null) return null;
        for (SkillNode node : tree.nodes) {
            for (SkillEffect effect : node.effects) {
                if ("flag".equals(effect.type) && flag.equals(effect.name)) return node;
            }
        }
        return null;
    }

    /** The last server tick each player was told why a craft was refused. */
    private static final java.util.Map<java.util.UUID, Integer> LAST_NOTICE = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Explain a refused craft on the action bar — the same surface, and the same red, as the deepslate gate.
     *
     * <p>The overlay is the right place for this: it fades on its own after a couple of seconds, so a player
     * who keeps trying gets reminded rather than accumulating a column of identical chat lines. Every fresh
     * attempt re-sends it, which restarts the fade.
     *
     * <p>Throttled to one packet per tick, because a single click consults {@code mayPickup} more than once
     * and a shift-click loops over it. That is invisible to the player; it only collapses the burst.
     */
    public static void notifyLocked(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer sp)) return;
        String flag = requiredFlag(stack);
        if (flag == null) return;

        Integer last = LAST_NOTICE.get(sp.getUUID());
        if (last != null && last.intValue() == sp.tickCount) return;
        LAST_NOTICE.put(sp.getUUID(), sp.tickCount);

        SkillNode node = nodeForFlag(flag);
        String name = node == null ? flag
                : io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(
                        sp, "vanillaskills.node." + node.id, node.title);
        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(
                net.minecraft.network.chat.Component.literal(
                                io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(sp,
                                        "vanillaskills.msg.craft_locked",
                                        "You need %s to craft this. Open /skill to unlock it.", name))
                        .withStyle(net.minecraft.ChatFormatting.RED)));
    }

    /** True if this skill-tree lane is turned off by config (its crafting is ungated and it's hidden). */
    public static boolean laneDisabled(String categoryId) {
        if ("toolsmith".equals(categoryId))
            return !io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.TOOL_REQS_ENABLED;
        if ("armorsmith".equals(categoryId))
            return !io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.ARMOR_REQS_ENABLED;
        return false;
    }

    private static final java.util.Map<net.minecraft.world.item.Item, String> VANILLA_ARMOR = new java.util.HashMap<>();
    private static final java.util.Map<net.minecraft.world.item.Item, String> VANILLA_TOOL = new java.util.HashMap<>();
    static {
        armorTier("copper", net.minecraft.world.item.Items.COPPER_HELMET, net.minecraft.world.item.Items.COPPER_CHESTPLATE,
                net.minecraft.world.item.Items.COPPER_LEGGINGS, net.minecraft.world.item.Items.COPPER_BOOTS);
        armorTier("gold", net.minecraft.world.item.Items.GOLDEN_HELMET, net.minecraft.world.item.Items.GOLDEN_CHESTPLATE,
                net.minecraft.world.item.Items.GOLDEN_LEGGINGS, net.minecraft.world.item.Items.GOLDEN_BOOTS);
        armorTier("iron", net.minecraft.world.item.Items.IRON_HELMET, net.minecraft.world.item.Items.IRON_CHESTPLATE,
                net.minecraft.world.item.Items.IRON_LEGGINGS, net.minecraft.world.item.Items.IRON_BOOTS);
        armorTier("diamond", net.minecraft.world.item.Items.DIAMOND_HELMET, net.minecraft.world.item.Items.DIAMOND_CHESTPLATE,
                net.minecraft.world.item.Items.DIAMOND_LEGGINGS, net.minecraft.world.item.Items.DIAMOND_BOOTS);
        armorTier("netherite", net.minecraft.world.item.Items.NETHERITE_HELMET, net.minecraft.world.item.Items.NETHERITE_CHESTPLATE,
                net.minecraft.world.item.Items.NETHERITE_LEGGINGS, net.minecraft.world.item.Items.NETHERITE_BOOTS);
        toolTier("copper", net.minecraft.world.item.Items.COPPER_PICKAXE, net.minecraft.world.item.Items.COPPER_AXE,
                net.minecraft.world.item.Items.COPPER_SHOVEL, net.minecraft.world.item.Items.COPPER_HOE, net.minecraft.world.item.Items.COPPER_SWORD);
        toolTier("gold", net.minecraft.world.item.Items.GOLDEN_PICKAXE, net.minecraft.world.item.Items.GOLDEN_AXE,
                net.minecraft.world.item.Items.GOLDEN_SHOVEL, net.minecraft.world.item.Items.GOLDEN_HOE, net.minecraft.world.item.Items.GOLDEN_SWORD);
        toolTier("iron", net.minecraft.world.item.Items.IRON_PICKAXE, net.minecraft.world.item.Items.IRON_AXE,
                net.minecraft.world.item.Items.IRON_SHOVEL, net.minecraft.world.item.Items.IRON_HOE, net.minecraft.world.item.Items.IRON_SWORD);
        toolTier("diamond", net.minecraft.world.item.Items.DIAMOND_PICKAXE, net.minecraft.world.item.Items.DIAMOND_AXE,
                net.minecraft.world.item.Items.DIAMOND_SHOVEL, net.minecraft.world.item.Items.DIAMOND_HOE, net.minecraft.world.item.Items.DIAMOND_SWORD);
        toolTier("netherite", net.minecraft.world.item.Items.NETHERITE_PICKAXE, net.minecraft.world.item.Items.NETHERITE_AXE,
                net.minecraft.world.item.Items.NETHERITE_SHOVEL, net.minecraft.world.item.Items.NETHERITE_HOE, net.minecraft.world.item.Items.NETHERITE_SWORD);
    }

    private static void armorTier(String tier, net.minecraft.world.item.Item... items) {
        for (net.minecraft.world.item.Item i : items) VANILLA_ARMOR.put(i, tier);
    }

    private static void toolTier(String tier, net.minecraft.world.item.Item... items) {
        for (net.minecraft.world.item.Item i : items) VANILLA_TOOL.put(i, tier);
    }

    /** True if the player has unlocked the Armorsmith node that permits the Dragon smithing upgrade. */
    public static boolean canSmithDragon(Player player) {
        if (!io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.ARMOR_REQS_ENABLED) return true;
        return hasFlag(player, "craft_armor_dragon");
    }

    /** Beneficial-potion duration multiplier from the Brewmaster lane (1.0 = none, up to 1.5 = +50%). */
    public static float potionDurationMultiplier(Player player) {
        for (int level = 5; level >= 1; level--) {
            if (hasFlag(player, "long_potions_" + level)) return 1.0f + 0.1f * level;
        }
        return 1.0f;
    }

    /** Chance (0.0–0.2) to fully dodge incoming arrow damage, from the Evasion lane. */
    public static float arrowDodgeChance(Player player) {
        for (int level = 10; level >= 1; level--) {
            if (hasFlag(player, "arrow_dodge_" + level)) return 0.02f * level;
        }
        return 0.0f;
    }

    /** Bonus-crop tier (0–5) from the Cultivator lane; each tier = +20% bonus-crop chance. */
    public static int farmingLevel(Player player) {
        for (int level = 5; level >= 1; level--) {
            if (hasFlag(player, "cultivator_" + level)) return level;
        }
        return 0;
    }

    /**
     * How many Aquatic swim-speed nodes the player has (0-3); each one pushes them harder through water.
     *
     * <p>The lane used to hand out Dolphin's Grace, which Minecraft reads as a yes/no and which therefore
     * arrived whole at the first node — a permanent dolphin. This is a dial instead; see {@link SwimSpeed}.
     */
    public static int swimSpeedLevel(Player player) {
        for (int level = 3; level >= 1; level--) {
            if (hasFlag(player, "swim_speed_" + level)) return level;
        }
        return 0;
    }

    /** True if any of the player's unlocked skill nodes grants the given flag. */
    public static boolean hasFlag(Player player, String flag) {
        if (!(player instanceof ServerPlayer sp)) return false;
        PlayerSkillData data = VanillaSkills.PLAYERS.get(sp.getUUID());
        if (data == null) return false;
        SkillTree tree = VanillaSkills.TREE.tree();
        for (String id : data.unlocked) {
            SkillNode node = tree.byId(id);
            if (node == null) continue;
            for (SkillEffect effect : node.effects) {
                if ("flag".equals(effect.type) && flag.equals(effect.name)) return true;
            }
        }
        return false;
    }
}
