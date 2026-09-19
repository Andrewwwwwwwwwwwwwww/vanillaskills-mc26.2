package io.github.andrewwwwwwwwwwwwwww.vanillaskills.data;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.QuestShop;

import java.util.ArrayList;
import java.util.List;

/**
 * Datapack definition of one Quest Shop offer, read from
 * {@code data/&lt;namespace&gt;/vanillaskills/shop_offer/&lt;name&gt;.json}.
 *
 * <p>Single-item offers use the shorthand form, which also derives the id:
 * <pre>{@code
 * {"item": "minecraft:bread", "count": 8, "price": 1}
 * }</pre>
 *
 * <p>Multi-item offers list their stacks and need an explicit id and label:
 * <pre>{@code
 * {
 *   "id": "iron_set",
 *   "label": "Iron Armor Set",
 *   "price": 22,
 *   "grants": [
 *     {"item": "minecraft:iron_helmet"}, {"item": "minecraft:iron_chestplate"},
 *     {"item": "minecraft:iron_leggings"}, {"item": "minecraft:iron_boots"}
 *   ]
 * }
 * }</pre>
 *
 * <p>An enchanted book is a normal grant carrying an {@code enchantment}; it is resolved against the
 * live registry at purchase time, so a pack may sell an enchantment its own pack defines.
 *
 * <p>{@code price} is in Quest Shards. The Skill-Shard price is derived from it at the live conversion
 * ratio, so there is nothing to keep in sync. {@code label} is an English fallback — it is translated
 * per player under {@code vanillaskills.shop.<slug of label>}; offers without one show the granted
 * item's own (already translated) name.
 */
public class ShopOfferDef implements VsEntry {

    /** One granted stack. */
    public static class GrantDef {
        public String item;
        public int count = 1;
        /** Optional enchantment id to stamp on the granted stack (books use stored_enchantments). */
        public String enchantment;
        /** Level for {@link #enchantment}; ignored without one. */
        public int level = 1;
    }

    /** Stable offer key. Optional for the single-item shorthand, where it becomes {@code <item>x<count>}. */
    public String id;

    /** English fallback display name. Optional — omit to show the granted item's own name. */
    public String label;

    /** Cost in Quest Shards. */
    public int price = 1;

    /** Selection weight for the daily rotation. 0 (the default) derives it from {@link #price}. */
    public int weight = 0;

    /**
     * Cost in Skill Shards, when it should not simply be {@link #price} at the convert rate.
     *
     * <p>0, the default, derives it as before. Set it to the same number as {@code price} to charge the
     * same either way, which is what the enchanted books do.
     */
    public int skillPrice = 0;

    // ---- single-item shorthand (ignored when `grants` is present) ----
    public String item;
    public int count = 1;
    public String enchantment;
    public int level = 1;

    /** Explicit stacks. Takes precedence over the shorthand fields. */
    public List<GrantDef> grants;

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean normalize() {
        if (grants == null || grants.isEmpty()) {
            if (item == null || item.isBlank()) return false;
            GrantDef only = new GrantDef();
            only.item = item;
            only.count = count;
            only.enchantment = enchantment;
            only.level = level;
            grants = List.of(only);
            if (id == null || id.isBlank()) id = item + "x" + Math.max(1, count);
        }
        if (id == null || id.isBlank()) return false;
        for (GrantDef g : grants) {
            if (g == null || g.item == null || g.item.isBlank()) return false;
            if (g.count < 1) g.count = 1;
            if (g.enchantment != null && g.enchantment.isBlank()) g.enchantment = null;
            if (g.level < 1) g.level = 1;
        }
        if (label != null && label.isBlank()) label = null;
        if (price < 0) price = 0;
        if (weight <= 0) weight = QuestShop.defaultWeight(price);
        return true;
    }

    /** Convert to the runtime record. Only valid after {@link #normalize()}. */
    public QuestShop.ShopOffer toOffer() {
        List<QuestShop.Grant> out = new ArrayList<>(grants.size());
        for (GrantDef g : grants) {
            out.add(new QuestShop.Grant(g.item, g.count, g.enchantment, g.level));
        }
        return new QuestShop.ShopOffer(id, label, List.copyOf(out), price, weight, skillPrice);
    }
}
