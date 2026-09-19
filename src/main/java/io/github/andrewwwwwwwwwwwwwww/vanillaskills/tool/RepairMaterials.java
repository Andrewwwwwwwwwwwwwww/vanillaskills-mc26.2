package io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Repairable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gives the vanilla items that ship with no repair material one that fits, so they stop being
 * combine-or-nothing at the anvil. Vanilla leaves nine damageable items with no {@code repairable}
 * component at all — a trident can only ever be mended with a second trident — which under
 * material-based anvil pricing makes them the only gear that cannot take the cheap repair path.
 *
 * <p>The component is stamped onto the <b>stack</b> (like the custom tiers' repair rules) rather than
 * onto the item's defaults, because stack components are synced: a vanilla client's anvil screen
 * previews the repair correctly. Stamping happens in the same player sweep that migrates legacy gear,
 * so anything held, looted or crafted is covered within a tick.
 *
 * <p>The two "on a stick" rods are deliberately left combine-only. An already-present component —
 * ours from an earlier sweep, or one a datapack added — is never overwritten. Toggle with
 * {@code vanillaRepairMaterials}; turning it off stops stamping but does not strip stacks already
 * stamped.
 */
public final class RepairMaterials {
    private RepairMaterials() {}

    private static final Map<Item, Item> MATERIALS = Map.of(
            Items.TRIDENT, Items.PRISMARINE_SHARD,
            Items.BOW, Items.STRING,
            Items.CROSSBOW, Items.STRING,
            Items.FISHING_ROD, Items.STRING,
            Items.FLINT_AND_STEEL, Items.IRON_INGOT,
            Items.SHEARS, Items.IRON_INGOT,
            Items.BRUSH, Items.COPPER_INGOT);

    /**
     * Vanilla netherite gear, which repairs with an ingot and nothing else.
     *
     * <p>An ingot is four scrap and four gold, so repairing with one is the most expensive way to mend
     * anything in the game — and the gold in it does nothing for the repair. Scrap is added alongside the
     * ingot rather than replacing it, so both work.
     */
    private static final Set<Item> NETHERITE_GEAR = Set.of(
            Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE,
            Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.NETHERITE_SPEAR,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

    /** Stamps every matching item in the player's inventory and ender chest. */
    public static void sweep(ServerPlayer player) {
        if (!GameplayConfig.VANILLA_REPAIR_MATERIALS) return;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) stamp(inv.getItem(i));
        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            stamp(player.getEnderChestInventory().getItem(i));
        }
    }

    private static void stamp(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (addNetheriteScrap(stack)) return;
        Item material = MATERIALS.get(stack.getItem());
        if (material == null) return;
        if (stack.has(DataComponents.REPAIRABLE)) {
            retireOldMaterial(stack);
            return;
        }
        stack.set(DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(List.of(material.builtInRegistryHolder()))));
    }

    /**
     * Materials this mod once stamped and has since thought better of, mapped to what replaces them.
     *
     * <p>A stamped component is never revisited, so a trident stamped before this correction would keep
     * asking for a prismarine <i>block</i> — four shards — for ever. A stack whose repair material is
     * exactly the one we used to write is ours to correct; anything else, including a datapack's or a
     * player's own, is left alone.
     */
    private static final Map<Item, Item> SUPERSEDED = Map.of(Items.PRISMARINE, Items.PRISMARINE_SHARD);

    private static void retireOldMaterial(ItemStack stack) {
        Repairable current = stack.get(DataComponents.REPAIRABLE);
        if (current == null) return;
        List<net.minecraft.core.Holder<Item>> items = new java.util.ArrayList<>();
        for (net.minecraft.core.Holder<Item> holder : current.items()) items.add(holder);
        if (items.size() != 1) return;                       // only the single-material stamps are ours
        Item replacement = SUPERSEDED.get(items.get(0).value());
        if (replacement == null) return;
        stack.set(DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(List.of(replacement.builtInRegistryHolder()))));
    }

    /**
     * Adds Netherite Scrap to a piece of vanilla netherite gear's repair materials.
     *
     * @return true if this stack is netherite gear, whether or not anything needed changing
     */
    private static boolean addNetheriteScrap(ItemStack stack) {
        if (!NETHERITE_GEAR.contains(stack.getItem())) return false;
        // The mod's own Dragon tier is built on netherite items but repairs with Dragon Ingots. Its stacks
        // carry the tier's marker, and leaving them alone keeps scrap out of a tier it was never meant for.
        for (io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTier tier
                : io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTiers.TIERS) {
            if (io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers.has(stack, tier.markerKey)) {
                return true;
            }
        }
        for (io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTier tier
                : io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTiers.TIERS) {
            if (tier.isWorn(stack)) return true;
        }

        Repairable current = stack.get(DataComponents.REPAIRABLE);
        List<net.minecraft.core.Holder<Item>> materials = new java.util.ArrayList<>();
        if (current != null) {
            for (net.minecraft.core.Holder<Item> holder : current.items()) {
                if (holder.value() == Items.NETHERITE_SCRAP) return true; // already done
                materials.add(holder);
            }
        } else {
            materials.add(Items.NETHERITE_INGOT.builtInRegistryHolder());
        }
        materials.add(Items.NETHERITE_SCRAP.builtInRegistryHolder());
        stack.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(materials)));
        return true;
    }
}
