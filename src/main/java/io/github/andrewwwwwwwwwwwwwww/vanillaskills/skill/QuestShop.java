package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Quest Shop: a daily-rotating catalog of boost items bought with Quest Shards (or Skill Shards
 * at the {@link #CONVERT_RATIO} ratio). The daily selection is derived deterministically from the
 * UTC day number, so it is stable for the whole day and needs no persistence.
 */
public final class QuestShop {
    private QuestShop() {}

    /** Quest Shards per 1 Skill Shard (one-way); also the rate when paying for items in Skill Shards.
     *  Default 3 — set live from gameplay.json by {@link GameplayConfig}. */
    public static int CONVERT_RATIO = 3;
    /** How many rotating offers are shown each day. */
    public static int dailyCount() { return io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.SHOP_SLOTS; }

    /** A single granted stack within an offer. */
    /**
     * One stack an offer hands over.
     *
     * <p>{@code enchantment} is normally null. When set, the granted item is enchanted with it at
     * {@code enchantLevel} — which is how the shop sells enchanted books without needing a separate offer
     * type. It is resolved against the live registry at purchase time rather than stored as a Holder,
     * because enchantments are a datapack registry and {@code vanillaskills:unboxing} does not exist until
     * the pack has loaded.
     */
    public record Grant(String itemId, int count, String enchantment, int enchantLevel) {
        public Grant(String itemId, int count) {
            this(itemId, count, null, 0);
        }
    }

    /**
     * One shop offer: one or more stacks granted for a Quest-Shard price.
     *
     * @param weight relative likelihood of being stocked on a given day; {@link #defaultWeight} derives
     *               one from the price when a pack does not set it.
     */
    public record ShopOffer(String key, String label, List<Grant> grants, int price, int weight,
                            int skillPriceOverride) {
        public Grant icon() { return grants.get(0); }
        /**
         * Skill-Shard cost when paying with Skill Shards.
         *
         * <p>Normally the Quest-Shard price converted at {@link #CONVERT_RATIO} and rounded up, so the two
         * ways to pay cost the same. An offer may author its own instead, and the enchanted books do:
         * charging three times as many Quest Shards for stock that Quest Shards exist to buy made the
         * shop's own currency the worst way to shop.
         */
        public int skillPrice() {
            if (skillPriceOverride > 0) return skillPriceOverride;
            return Math.max(1, (price + CONVERT_RATIO - 1) / CONVERT_RATIO);
        }
        /** Display name: explicit label, or the icon item's name with its count. */
        public String displayName() {
            if (label != null) return label;
            Grant g = icon();
            String name = new ItemStack(Quests.item(g.itemId())).getHoverName().getString();
            return g.count() > 1 ? g.count() + "× " + name : name;
        }
    }

    /**
     * The full catalogue, as loaded from the datapacks. Empty until the first datapack load.
     *
     * <p>Lives in {@code data/<namespace>/vanillaskills/shop_offer/}; the mod ships its own as a
     * bundled pack, so a server can reprice, remove or add stock without a code change.
     */
    public static List<ShopOffer> catalog() {
        return io.github.andrewwwwwwwwwwwwwww.vanillaskills.data.VsContent.shopOffers();
    }

    /**
     * Selection weight for an offer that does not declare one — cheaper commons appear more often than
     * premiums, so a 55-shard enchanted book stays a rare sighting rather than crowding out the staples.
     */
    public static int defaultWeight(int price) {
        if (price <= 2) return 6;
        if (price <= 4) return 4;
        if (price <= 7) return 3;
        if (price <= 11) return 2;
        return 1;
    }

    /** The UTC day number used as the rotation seed. */
    public static long currentDay() {
        return System.currentTimeMillis() / GameplayConfig.SHOP_REFRESH_MS; // rotation period
    }

    /** Milliseconds until the next daily rotation (UTC midnight). */
    public static long msUntilRotation() {
        long period = GameplayConfig.SHOP_REFRESH_MS;
        return period - (System.currentTimeMillis() % period);
    }

    /** Today's rotating offers (weighted, distinct, stable for the whole UTC day). */
    public static List<ShopOffer> dailyOffers() {
        Random rng = new Random(currentDay() * 0x9E3779B97F4A7C15L);
        List<ShopOffer> pool = new ArrayList<>(catalog());
        List<ShopOffer> chosen = new ArrayList<>();
        int count = Math.min(dailyCount(), pool.size());
        for (int n = 0; n < count; n++) {
            int total = 0;
            for (ShopOffer o : pool) total += Math.max(1, o.weight());
            int r = rng.nextInt(total);
            int idx = 0;
            for (int i = 0; i < pool.size(); i++) {
                r -= Math.max(1, pool.get(i).weight());
                if (r < 0) { idx = i; break; }
            }
            chosen.add(pool.remove(idx));
        }
        return chosen;
    }

    /** Attempt to buy an offer with the chosen currency; messages the player and returns success. */
    public static boolean purchase(ServerPlayer player, ShopOffer offer, boolean paySkillShards) {
        if (paySkillShards) {
            int cost = offer.skillPrice();
            if (!VanillaSkills.PLAYERS.spendSkillShards(player, cost)) {
                player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,"vanillaskills.msg.need_skill","Not enough Skill Shards (need %d).", cost))
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        } else {
            if (!VanillaSkills.PLAYERS.spendQuestShards(player, offer.price())) {
                player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,"vanillaskills.msg.need_quest","Not enough Quest Shards (need %d).", offer.price()))
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        }
        for (Grant g : offer.grants()) {
            ItemStack stack = new ItemStack(Quests.item(g.itemId()), g.count());
            applyEnchantment(player, stack, g);
            player.getInventory().placeItemBackInInventory(stack);
        }
        String paid = paySkillShards
                ? io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,
                        "vanillaskills.msg.amount_skill", "%d Skill Shards", offer.skillPrice())
                : io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,
                        "vanillaskills.msg.amount_quest", "%d Quest Shards", offer.price());
        player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,"vanillaskills.msg.purchased","Purchased %s for %s.", offer.displayName(), paid))
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    /**
     * Stamp a granted stack with its offer's enchantment, if it has one.
     *
     * <p>An {@code enchanted_book} carries its enchantment in {@code stored_enchantments} rather than
     * {@code enchantments}, and {@code EnchantmentHelper.updateEnchantments} already routes to the right one
     * based on the item — so this works for both a book and, if ever wanted, a directly-enchanted tool.
     *
     * <p>Silently leaves the stack plain if the enchantment cannot be resolved. A shop offer must never be
     * able to fail a purchase the player has already paid for.
     */
    private static void applyEnchantment(ServerPlayer player, ItemStack stack, Grant grant) {
        if (grant.enchantment() == null) return;
        var server = player.level().getServer();
        if (server == null) return;
        var id = net.minecraft.resources.Identifier.tryParse(grant.enchantment());
        if (id == null) return;
        var registry = server.registryAccess()
                .lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).orElse(null);
        if (registry == null) return;
        var holder = registry.get(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.ENCHANTMENT, id)).orElse(null);
        if (holder == null) return;
        net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(stack,
                mutable -> mutable.set(holder, Math.max(1, grant.enchantLevel())));
    }

    /** Convert Quest Shards → 1 Skill Shard at the 3:1 ratio; messages the player. */
    public static boolean convertOne(ServerPlayer player) {
        if (!VanillaSkills.PLAYERS.convertToSkillShards(player, 1)) {
            player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,"vanillaskills.msg.need_quest","Not enough Quest Shards (need %d).", CONVERT_RATIO))
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,
                "vanillaskills.msg.converted", "Converted %d Quest Shards → 1 Skill Shard.", CONVERT_RATIO))
                .withStyle(ChatFormatting.GREEN));
        return true;
    }
}
