package io.github.andrewwwwwwwwwwwwwww.vanillaskills;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.command.SkillCommands;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.PointsConfig;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorCraftingRecipe;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonScale;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonSet;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.RoseGoldSet;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.loot.FortuneTemplateLoot;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.FortuneTemplateRecipe;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.FortuneUpgradeRecipe;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.PlayerSkillData;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.PlayerSkillManager;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillEffects;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillTree;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillTreeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaSkills implements ModInitializer {
    public static final String MOD_ID = "vanillaskills";
    public static final Logger LOGGER = LoggerFactory.getLogger("VanillaSkills");
    /** Stable id for our server-pushed texture pack, so the client de-duplicates re-pushes. */
    private static final java.util.UUID RESOURCE_PACK_ID = java.util.UUID.fromString("5b6c9a10-7e2d-4c3a-9f11-a1b2c3d4e5f6");

    public static MinecraftServer server;

    /** Per-world vanillaskills data/config directory (inside the world save), or null if no world is loaded. */
    public static java.nio.file.Path worldDir() {
        return server == null ? null
                : server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("vanillaskills");
    }
    public static final SkillTreeManager TREE = new SkillTreeManager();
    public static final PlayerSkillManager PLAYERS = new PlayerSkillManager();
    public static final io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.QuestBoard QUESTS =
            new io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.QuestBoard();
    public static final io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.BountyBoards BOARDS =
            new io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.BountyBoards();
    public static final io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBlocks SHARDS =
            new io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBlocks();
    public static final io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.WorldState STATE =
            new io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.WorldState();

    private static final int ROSE_GOLD_INTERVAL = 10;
    private static final int STATUS_REFRESH_INTERVAL = 40;
    private static final int DRAGON_SCALE_DROP = 8;
    private static final int QUEST_ROTATION_INTERVAL = 200; // check the bounty timer every ~10s
    private static final int ELYTRA_FORGE_INTERVAL = 20; // scan items on anvils/grindstones once a second

    private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> placedFeature(String path) {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,
                Identifier.fromNamespaceAndPath(MOD_ID, path));
    }

    private int tickCounter = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("VanillaSkills initializing");

        // Datapack-driven content. Registered during init rather than at SERVER_STARTED because the
        // listener has to be in place before the FIRST datapack load — which happens while the server
        // is still being constructed, i.e. before SERVER_STARTED assigns `server`. Nothing reached
        // from here may touch world state or worldDir().
        // This registration is the one edition-specific part; NeoForge uses AddReloadListenerEvent.
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(MOD_ID, "content");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        io.github.andrewwwwwwwwwwwwwww.vanillaskills.data.VsContent.reload(manager);

                        // Rebuild the tree so a pack edit takes effect on /reload rather than only on
                        // restart. Guarded on `server`: this listener also runs during the FIRST datapack
                        // load, while the server is still being constructed — at that point SERVER_STARTED
                        // has not run, worldDir() is null, and TREE.load() would have nothing to migrate
                        // into. SERVER_STARTED does the initial build instead.
                        if (server != null) {
                            TREE.load();
                            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                                PLAYERS.applyAll(player);
                            }
                        }
                    }
                });

        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "fortune_upgrade"),
                FortuneUpgradeRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "fortune_template"),
                FortuneTemplateRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "tool_crafting"),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolCraftingRecipe.SERIALIZER);
        FortuneTemplateLoot.register();
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.loot.ShardLoot.register();
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.loot.CrateLoot.register();

        // Skill Shard ore generation.
        //
        // The feature itself is plain datapack JSON under data/vanillaskills/worldgen/, shared byte-for-byte
        // by every edition. Only this biome injection is loader-specific: vanilla has no way for a datapack to
        // add a feature to an existing biome without replacing that biome outright, which is exactly why
        // worldgen packs either rewrite biomes wholesale or depend on a library. Replacing vanilla biomes here
        // would fight every other worldgen pack the server runs, so we inject instead.
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
                placedFeature("skill_shard_ore_overworld"));
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInTheNether(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
                placedFeature("skill_shard_ore_nether"));
        // The End: outer islands only. Excluding minecraft:the_end keeps the ore off the spawn island.
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(
                        net.minecraft.world.level.biome.Biomes.END_HIGHLANDS,
                        net.minecraft.world.level.biome.Biomes.END_MIDLANDS,
                        net.minecraft.world.level.biome.Biomes.END_BARRENS,
                        net.minecraft.world.level.biome.Biomes.SMALL_END_ISLANDS),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
                placedFeature("skill_shard_ore_end"));

        io.github.andrewwwwwwwwwwwwwww.vanillaskills.creative.VanillaSkillsItemGroup.register();

        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "armor_crafting"),
                ArmorCraftingRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "dragon_ingot"),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonIngotRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "dragon_template_dup"),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.DragonTemplateRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "shard_crafting"),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardCraftingRecipe.SERIALIZER);
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.loot.DragonTemplateLoot.register();

        ServerLifecycleEvents.SERVER_STARTED.register(srv -> {
            server = srv;
            PLAYERS.setPointsConfig(PointsConfig.load());
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.load();
            // Compute total earnable points (P) before building the tree so the default tree can be
            // priced against it (whole tree = P, Night Vision gated at P/3).
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillTreeManager.economyP = PLAYERS.computeTotalEarnable();
            TREE.load();
            QUESTS.load();
            BOARDS.load();
            SHARDS.load();
            STATE.load();
            // Display entities persist, so a crash mid-spin would otherwise leave a ring of items hanging
            // in the world with nothing tracking it.
            for (ServerLevel lvl : srv.getAllLevels()) {
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.crate.CrateReel.sweep(lvl);
            }
            // Redraw every tracked Skill Shard block. Their overlays are entities written into the world,
            // so one built by an older version keeps whatever size and model it was spawned with — 2.0.0
            // moved the anti-z-fighting oversize off the model and onto the entity, and an un-redrawn
            // overlay flickers against the vanilla block underneath. Doubles as the repair for overlays
            // lost to a chunk-delete or an entity wipe. Only touches loaded chunks; the rest catch up as
            // they load.
            int redrawn = SHARDS.refreshAll(srv);
            if (redrawn > 0) LOGGER.info("Redrew {} Skill Shard block overlay(s)", redrawn);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(srv -> {
            // Settle first: a crate is consumed the moment it is opened, so a reel still spinning at
            // shutdown is holding loot that has already been paid for.
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.crate.CrateReel.finishAll(srv);
            PLAYERS.saveAllAndClear();
            QUESTS.save();
            BOARDS.save();
            SHARDS.save();
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.TaskShards.clear();
        });

        // Force the custom texture pack onto every joining client, so vanilla clients see the gear
        // with zero server.properties setup. Configurable in gameplay.json; on by default.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, srv) -> {
            if (!io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.PUSH_RESOURCE_PACK) return;
            // Skip pure single-player: the host has every texture in the mod jar already, including the
            // assets/minecraft/** overrides the block takeover needs. Keep those bundled — without them a
            // single-player world shows plain reinforced deepslate and lodestone with their vanilla names.
            // Still push on LAN-opened worlds and dedicated servers, where vanilla clients can join.
            if (srv.isSingleplayer() && !srv.isPublished()) return;
            String url = io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.RESOURCE_PACK_URL;
            if (url == null || url.isEmpty()) return;
            handler.send(new ClientboundResourcePackPushPacket(
                    RESOURCE_PACK_ID, url,
                    io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.RESOURCE_PACK_SHA1,
                    true,
                    java.util.Optional.of(Component.translatableWithFallback("vanillaskills.resourcepack.prompt", "VanillaSkills+ needs this pack to show the custom gear."))));
        });

        // Right-clicks that need to beat vanilla to the punch: opening the Infusing Table, and merging one
        // Stable block into another. Placement itself is vanilla's — the blocks ARE reinforced deepslate and
        // lodestone, so a BlockItem places them correctly on its own; ShardBlockPlaceMixin records the
        // position afterwards.
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel level)) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            // Right-clicking an enchanting table opens the Infusing Table instead. The vanilla screen still
            // exists but cannot be paid for now that experience is gone, so this replaces it outright.
            if (io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.INFUSING_ENABLED
                    && level.getBlockState(hit.getBlockPos()).is(net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE)
                    && !sp.isSecondaryUseActive()) {
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.infuse.InfusingMenu.open(sp, hit.getBlockPos());
                return net.minecraft.world.InteractionResult.SUCCESS;
            }

            // Bone meal on a spore blossom spreads it; vanilla does nothing at all here.
            if (io.github.andrewwwwwwwwwwwwwww.vanillaskills.world.SporeBlossoms.tryBoneMeal(
                    level, hit.getBlockPos(), sp, sp.getItemInHand(hand))) {
                return net.minecraft.world.InteractionResult.SUCCESS;
            }

            ItemStack held = player.getItemInHand(hand);
            boolean stable = io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardItems.isStableBlock(held);
            boolean unstable = io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardItems.isUnstableBlock(held);
            if (!stable && !unstable) return net.minecraft.world.InteractionResult.PASS;

            // Merge: a Stable block clicked onto an already-placed Stable block widens its aura instead
            // of placing a second one. Sneaking opts out and places normally — the same convention vanilla
            // uses to bypass a block's use action, and the only way to build two Stable blocks side by side.
            if (stable && !sp.isSecondaryUseActive() && SHARDS.kindAt(level, hit.getBlockPos())
                    == io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBlocks.Kind.STABLE) {
                if (SHARDS.merge(level, hit.getBlockPos())) {
                    if (!sp.hasInfiniteMaterials()) held.shrink(1);
                    sp.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(
                            sp, "vanillaskills.msg.shard_block_merged", "Merged — area of effect widened (%d/%d).",
                            SHARDS.mergeCountAt(level, hit.getBlockPos()),
                            io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBlocks.maxMerge())));
                } else {
                    sp.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(
                            sp, "vanillaskills.msg.shard_block_full", "This block is already fully merged."))
                            .withStyle(net.minecraft.ChatFormatting.RED));
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }

            // Placement is left entirely to vanilla BlockItem#place, and ShardBlockPlaceMixin records the
            // position afterwards. This used to be hand-rolled here, which meant re-implementing every
            // vanilla placement rule by hand and getting them wrong: it ignored that right-clicking an
            // interactable block should OPEN it rather than place against it, so aiming at a crafting table
            // with a shard block in hand buried the table instead of opening it. Deferring fixes that and
            // every other nuance (replaceability, collision, sounds, offhand) at once.
            return net.minecraft.world.InteractionResult.PASS;
        });

        // Breaking a shard block: we take over entirely, so vanilla does not also drop a plain amethyst block.
        //
        // The two kinds behave differently below Crystalline, which is deliberate:
        //   Unstable — breaks like ordinary ore mined with the wrong tool: it is destroyed and drops nothing,
        //              silently. Losing the block IS the feedback; a message on top would just nag.
        //   Stable   — refuses to break at all and says why. It is expensive and its aura is infrastructure,
        //              so destroying one by accident with a diamond pick would be a genuinely bad surprise.
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
            if (!(world instanceof ServerLevel level)) return true;
            var kind = SHARDS.kindAt(level, pos);
            if (kind == null) return true;

            if (!io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBlocks.canMine(player)) {
                if (kind == io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBlocks.Kind.STABLE) {
                    if (player instanceof ServerPlayer sp) {
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(
                                Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(sp,
                                        "vanillaskills.msg.shard_block_tool",
                                        "You need a Crystalline or better pickaxe to mine this."))
                                        .withStyle(net.minecraft.ChatFormatting.RED)));
                    }
                    return false; // not broken at all — the block stays and keeps its record
                }
                SHARDS.onBroken(level, pos, false); // Unstable: destroyed, nothing dropped
                return false;
            }
            SHARDS.onBroken(level, pos, !player.hasInfiniteMaterials());
            return false; // handled — cancel the vanilla break
        });

        // Naturally generated Skill Shard ore: drops shards, behind the same Crystalline-or-better gate.
        // Below that tier it breaks and drops nothing, exactly like the Unstable block and like vanilla ore
        // mined with too weak a pickaxe. No message — only the Stable block nags.
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
            if (!(world instanceof ServerLevel level)) return true;
            if (!io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardOre.isOre(level, pos, state)) return true;
            if (!io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBlocks.canMine(player)) {
                level.removeBlock(pos, false);
                return false;
            }
            if (!player.hasInfiniteMaterials()) {
                ItemStack drop = io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardItems.unstableShard();
                drop.setCount(io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.SHARD_ORE_DROP);
                net.minecraft.world.level.block.Block.popResource(level, pos, drop);
            }
            level.removeBlock(pos, false);
            return false; // handled — vanilla's empty drop table would otherwise give nothing
        });

        // Spawners drop an Unstable Skill Shard Block instead of the experience they used to give.
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
            if (!io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.SPAWNER_DROPS_SHARD_BLOCK) return;
            if (!(world instanceof ServerLevel level) || player.hasInfiniteMaterials()) return;
            if (!state.is(net.minecraft.world.level.block.Blocks.SPAWNER)) return;
            net.minecraft.world.level.block.Block.popResource(level, pos,
                    io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardItems.unstableBlock());
        });

        // Right-click a held Unstable Skill Shard to bank the whole stack again, or a crate to open it.
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            ItemStack held = player.getItemInHand(hand);
            if (io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBank.deposit(sp, held)
                    || io.github.andrewwwwwwwwwwwwwww.vanillaskills.crate.Crates.open(sp, held)) {
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        // Right-click a bounty board's floating-text interaction entity to open the quest GUI.
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == net.minecraft.world.InteractionHand.MAIN_HAND
                    && player instanceof ServerPlayer sp
                    && entity instanceof net.minecraft.world.entity.Interaction
                    && entity.entityTags().contains(
                            io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.BountyBoards.TAG)) {
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui.QuestMenu.open(sp);
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        // Bounty board: track kills toward active quests, and boss kills toward Feats.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getEntity() instanceof ServerPlayer killer) {
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Quests.onKill(killer, entity);
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Feats.onKill(killer, entity);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof EnderDragon) || !(entity.level() instanceof ServerLevel level)) return;
            // PLAYER kills only. THP can kill the dragon itself as part of how its End fight starts up, and
            // that must not quietly hand out scales — least of all the one-time first-kill bonus.
            if (!(source.getEntity() instanceof ServerPlayer)) return;
            int count = STATE.claimFirstDragonKill()
                    ? io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.DRAGON_SCALE_FIRST_KILL_DROP
                    : io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.DRAGON_SCALE_DROP;
            if (count <= 0) return;
            ItemStack scales = DragonScale.create();
            scales.setCount(count);
            ItemEntity drop = new ItemEntity(level, entity.getX(), entity.getY() + 1.0, entity.getZ(), scales);
            level.addFreshEntity(drop);
        });

        // Deepslate gate: only a Steel-tier-or-better pickaxe can break deepslate & its ores.
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.DeepslateGate.canBreak(player, state));

        // Fortune IV/V ore boost: one guaranteed extra base drop roll per level above III.
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerLevel level && player instanceof ServerPlayer sp) {
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.FortuneBoost.onBreak(level, sp, pos, state);
            }
            return true;
        });

        // Cultivator skill: bonus crops when harvesting a mature crop. Each Cultivator level rolls an
        // independent ~50% chance for one extra crop, so the bonus scales clearly with level — at max
        // (5) you average ~2.5 extra per crop, and at level 1 ~0.5.
        // Runs on BEFORE (block still present) so the melon/pumpkin natural-growth check can see the
        // attached stem — it reverts the instant the fruit is removed. Always returns true (never cancels).
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel level) || !(player instanceof ServerPlayer sp)) return true;
            net.minecraft.world.item.Item product =
                    io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Farming.matureCropProduct(level, pos, state);
            if (product == null) return true;
            int farmLevel = io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.CraftingGate.farmingLevel(sp);
            if (farmLevel <= 0) return true;
            int bonus = 0;
            for (int i = 0; i < farmLevel; i++) {
                if (sp.getRandom().nextFloat() < 0.5f) bonus++;
            }
            bonus = Math.min(bonus, io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Farming.bonusCap(state));
            if (bonus > 0) {
                net.minecraft.world.level.block.Block.popResource(level, pos, new ItemStack(product, bonus));
            }
            return true;
        });

        // Hard work pays: every block actually broken (mining, digging, harvesting a crop) rolls the
        // rare task-shard chance. AFTER only fires for breaks that went through, so anything a BEFORE
        // handler cancelled — gated deepslate, shard blocks — never rolls. Placement rolls the same
        // chance from TaskShardPlaceMixin, and TaskShards itself enforces the per-player cooldown.
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerLevel level && player instanceof ServerPlayer sp) {
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.TaskShards.roll(level, sp, pos);
            }
        });

        // Hardwood swords & axes inflict a little poison on hit.
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (blocked || damageTaken <= 0.0f) return;
            if (!(source.getEntity() instanceof ServerPlayer attacker)) return;
            ItemStack weapon = attacker.getMainHandItem();
            if (!io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers.has(weapon,
                    io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTiers.HARDWOOD.markerKey)) return;
            if (!(weapon.is(net.minecraft.world.item.Items.STONE_SWORD)
                    || weapon.is(net.minecraft.world.item.Items.STONE_AXE))) return;
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0)); // Poison I, 2s
        });

        ServerPlayerEvents.JOIN.register(player -> {
            PLAYERS.onJoin(player);
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.LegacyGear.sweep(player); // repoint pre-2.0 gear models
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.RepairMaterials.sweep(player);
            // Our data recipes appear in the book only once the matching skill is unlocked.
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.RecipeUnlocks.sync(player);
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBar.push(player, true);
            // Anything a crate owed them from a reel they logged out of.
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.crate.CrateReel.PendingRewards.deliver(player);
        });
        ServerPlayerEvents.LEAVE.register(player -> {
            PLAYERS.onLeave(player);
            DragonSet.onPlayerLeave(player.getUUID());
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.StepHeight.onLeave(player.getUUID());
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBar.forget(player);
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SwimSpeed.forget(player);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PLAYERS.applyAll(newPlayer);
            // Vanilla fills the fresh body to its BASE max (20) before our max-health modifiers are
            // reapplied, so a Vitality player came back at a fraction of their bar. A death respawn
            // means full health — but only a death respawn: alive=true is an end-portal return, where
            // topping up would be a free heal.
            if (!alive) newPlayer.setHealth(newPlayer.getMaxHealth());
            // The rebuilt client resets its XP display, so the shard readout went blank. An immediate
            // forced push LOSES here: vanilla marks its own lastSentExp stale on the rebuild and re-sends
            // the player's REAL experience (level 0) on the next tick, wiping anything we sent first.
            // Clearing our cache instead makes the ~10-tick reconcile re-send AFTER vanilla's zero.
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBar.forget(newPlayer);
        });

        // Same client-side reset happens on a portal trip (nether or otherwise) without any respawn
        // event firing. Same race, same fix: clear the cache and let the reconcile outlast vanilla's zero.
        net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL
                .register((player, origin, destination) ->
                        io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBar.forget(player));

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SkillCommands.register(dispatcher);
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.command.HelpCommand.register(dispatcher);
            var questsNode = dispatcher.register(net.minecraft.commands.Commands.literal("quests")
                    .executes(ctx -> {
                        io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui.QuestMenu.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    })
                    .then(net.minecraft.commands.Commands.literal("board")
                            .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                            .executes(ctx -> { BOARDS.place(ctx.getSource().getPlayerOrException()); return 1; })
                            .then(net.minecraft.commands.Commands.literal("remove")
                                    .executes(ctx -> { BOARDS.removeNear(ctx.getSource().getPlayerOrException()); return 1; }))
                            .then(net.minecraft.commands.Commands.literal("refresh")
                                    .executes(ctx -> {
                                        int n = BOARDS.refreshAll(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                "Re-rendered " + n + " bounty board" + (n == 1 ? "" : "s") + "."), true);
                                        return 1;
                                    })))
                    .then(net.minecraft.commands.Commands.literal("reroll")
                            .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                            .executes(ctx -> {
                                QUESTS.forceReroll();
                                ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                        "Bounties re-rolled."), true);
                                return 1;
                            }))
                    .then(net.minecraft.commands.Commands.literal("graduate")
                            .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                            .then(net.minecraft.commands.Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                    .executes(ctx -> {
                                        ServerPlayer t = net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "player");
                                        io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Quests.forceGraduate(t);
                                        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                t.getName().getString() + " graduated to the main bounty board."), true);
                                        return 1;
                                    })))
                    .then(net.minecraft.commands.Commands.literal("starter")
                            .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                            .then(net.minecraft.commands.Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                    .executes(ctx -> {
                                        ServerPlayer t = net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "player");
                                        io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Quests.resetToStarter(t);
                                        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                t.getName().getString() + " sent back to the starter board."), true);
                                        return 1;
                                    }))));
            dispatcher.register(net.minecraft.commands.Commands.literal("bounty").redirect(questsNode));
        });
    }

    private void onServerTick(MinecraftServer srv) {
        tickCounter++;
        DragonSet.tick(srv);
        // Every tick: suppress the Mountaineer step-up bonus while sneaking / toggled off (safety).
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.StepHeight.tick(srv, TREE.tree());
        // Every tick: the Aquatic lane's swim bonus goes on entering water and comes off leaving it.
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SwimSpeed.tick(srv);
        // Throttled (every ~2s, internally): discovery/dimension Feats + STAT-quest baselines.
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Feats.serverTick(srv);
        if (tickCounter % ELYTRA_FORGE_INTERVAL == 0) {
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonElytraForge.tick(srv);
        }
        if (tickCounter % ROSE_GOLD_INTERVAL == 0) {
            RoseGoldSet.tick(srv);
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.CrystalSet.tick(srv);
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorSetTooltips.tick(srv);
        }
        if (tickCounter % STATUS_REFRESH_INTERVAL == 0) {
            SkillTree tree = TREE.tree();
            for (ServerPlayer player : srv.getPlayerList().getPlayers()) {
                PlayerSkillData data = PLAYERS.get(player.getUUID());
                SkillEffects.refreshStatusEffects(player, data, tree);
                // Catches pre-2.0 gear picked up from a chest after the join sweep. Costs one
                // component lookup per slot once a world has been migrated.
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.LegacyGear.sweep(player);
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.RepairMaterials.sweep(player);
                // Reveal recipes for ingredients picked up since the last check. Only awarding on join
                // meant finding your first shard showed nothing until you relogged.
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.RecipeUnlocks.sync(player);
            }
        }
        if (tickCounter % QUEST_ROTATION_INTERVAL == 0) {
            QUESTS.tick(srv);
        }
        if (tickCounter % io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.BountyBoards.REFRESH_INTERVAL == 0) {
            BOARDS.tick(srv, tickCounter);
        }
        SHARDS.tick(srv, tickCounter); // self-throttling; harms hostiles inside a Stable block's area
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.NetherRoof.tick(srv, tickCounter); // self-throttling
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.shard.ShardBar.tick(srv, tickCounter); // self-throttling
        // Every tick: the reel paces its own steps, and returns immediately when nothing is spinning.
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.crate.CrateReel.tick(srv);
    }
}
