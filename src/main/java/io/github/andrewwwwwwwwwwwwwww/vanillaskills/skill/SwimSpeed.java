package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The Aquatic lane's swim speed.
 *
 * <p>It used to be Dolphin's Grace. That turned out to be a switch rather than a dial: Minecraft asks
 * {@code hasEffect(DOLPHINS_GRACE)} and, if so, changes the drag on your movement in water from 0.8 to 0.96 —
 * the level is never read. So the first of the three swim nodes already handed out the whole thing, the two
 * above it added nothing to swimming, and what it handed out was a permanent dolphin: drag that low multiplies
 * your top speed in water several times over.
 *
 * <p>So the drag is left alone and the lane pushes instead. Each node adds to how hard you swim, which raises
 * your speed in proportion rather than compounding, and it only applies while you are actually in water. Three
 * nodes is a real gain that a dolphin would still overtake, the nodes finally differ from one another, and the
 * size of it is a number in the config rather than a constant in Minecraft.
 *
 * <p>Movement speed is an attribute, so it reaches a vanilla client on its own — which matters, because this
 * mod has to work for players who have installed nothing. Changing the drag directly would have meant changing
 * it on the server only, and the client would have gone on predicting vanilla speed and fought us for it.
 */
public final class SwimSpeed {
    private SwimSpeed() {}

    /** Stable id, so re-applying updates the one modifier instead of stacking another. */
    private static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath("vanillaskills", "skill.swim_speed");

    /** Applies or removes the in-water bonus for every online player. */
    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            update(player);
        }
    }

    private static void update(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        int level = CraftingGate.swimSpeedLevel(player);
        boolean wanted = level > 0 && player.isInWater() && GameplayConfig.AQUATIC_SWIM_SPEED > 0.0;
        AttributeModifier current = speed.getModifier(MODIFIER_ID);

        if (!wanted) {
            if (current != null) speed.removeModifier(MODIFIER_ID);
            return;
        }

        double amount = level * GameplayConfig.AQUATIC_SWIM_SPEED;
        if (current != null && current.amount() == amount) return;
        speed.addOrUpdateTransientModifier(new AttributeModifier(
                MODIFIER_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    /** Drops the bonus when a player leaves, so nothing is left behind on the way out. */
    public static void forget(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(MODIFIER_ID) != null) speed.removeModifier(MODIFIER_ID);
    }
}
