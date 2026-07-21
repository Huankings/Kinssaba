package org.BsXinQin.kinswathe.record;

import dev.doctor4t.wathe.record.GameRecordEvent;
import dev.doctor4t.wathe.record.GameRecordManager;
import dev.doctor4t.wathe.record.replay.ReplayGenerator;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static dev.doctor4t.wathe.record.replay.ReplayRegistry.*;

public final class KinsWatheReplayFormatters {
    private KinsWatheReplayFormatters() {
    }

    private static Identifier id(String path) {
        return Identifier.of(KinsWathe.MOD_ID, path);
    }

    public static void register() {
        registerSkillFormatter(id("cleaner_clear_items"), KinsWatheReplayFormatters::formatCleanerClearItems);
        registerSkillFormatter(id("hunter_refresh"), KinsWatheReplayFormatters::formatHunterRefresh);
        registerSkillFormatter(id("robot_night_vision"), KinsWatheReplayFormatters::formatRobotNightVision);
        registerSkillFormatter(id("kidnapper_release"), KinsWatheReplayFormatters::formatKidnapperRelease);

        registerItemUseFormatter(id("knockout_drug"), KinsWatheReplayFormatters::formatKnockoutDrugUse);
        registerItemUseFormatter(id("poison_injector"), KinsWatheReplayFormatters::formatPoisonInjectorUse);
        registerItemUseFormatter(id("sulfuric_acid_barrel"), KinsWatheReplayFormatters::formatSulfuricAcidBarrelUse);

        registerItemHitFormatter(id("blowgun"), KinsWatheReplayFormatters::formatBlowgunHit);

        registerGlobalEventFormatter(id("robot_night_vision_end"), KinsWatheReplayFormatters::formatRobotNightVisionEnd);
        registerGlobalEventFormatter(id("robot_poison_immune"), KinsWatheReplayFormatters::formatRobotPoisonImmune);
        registerGlobalEventFormatter(id("robot_bed_poison_immune"), KinsWatheReplayFormatters::formatRobotBedPoisonImmune);
    }

    private static @Nullable Text actorText(GameRecordEvent event, GameRecordManager.MatchRecord match) {
        UUID actorUuid = event.data().containsUuid("actor") ? event.data().getUuid("actor") : null;
        return actorUuid == null ? null : ReplayGenerator.formatPlayerName(actorUuid, ReplayGenerator.getPlayerInfoCache(match));
    }

    private static @Nullable Text targetText(GameRecordEvent event, GameRecordManager.MatchRecord match) {
        UUID targetUuid = event.data().containsUuid("target") ? event.data().getUuid("target") : null;
        return targetUuid == null ? null : ReplayGenerator.formatPlayerName(targetUuid, ReplayGenerator.getPlayerInfoCache(match));
    }

    private static @Nullable Text playerText(@Nullable UUID uuid, GameRecordManager.MatchRecord match) {
        return uuid == null ? null : ReplayGenerator.formatPlayerName(uuid, ReplayGenerator.getPlayerInfoCache(match));
    }

    private static @Nullable UUID uuid(NbtCompound data, String key) {
        return data.containsUuid(key) ? data.getUuid(key) : null;
    }

    private static @Nullable Text formatCleanerClearItems(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        if (actor == null) {
            return null;
        }
        return Text.translatable("replay.skill_use.kinswathe.cleaner", actor, event.data().getInt("price"));
    }

    private static @Nullable Text formatHunterRefresh(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        if (actor == null) {
            return null;
        }
        return Text.translatable("replay.skill_use.kinswathe.hunter", actor, event.data().getInt("price"));
    }

    private static @Nullable Text formatRobotNightVision(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        return actor == null ? null : Text.translatable("replay.skill_use.kinswathe.robot", actor);
    }

    private static @Nullable Text formatKidnapperRelease(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        if (actor == null) {
            return null;
        }
        Text target = targetText(event, match);
        if (target != null) {
            return Text.translatable("replay.skill_use.kinswathe.kidnapper.release", actor, target);
        }
        return Text.translatable("replay.skill_use.kinswathe.kidnapper.release_end", actor);
    }

    private static @Nullable Text formatKnockoutDrugUse(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        Text target = targetText(event, match);
        if (actor == null || target == null) {
            return null;
        }
        if (event.data().getBoolean("robot_failed")) {
            return Text.translatable("replay.item_use.kinswathe.knockout_drug.failed_robot", target, actor);
        }
        return Text.translatable("replay.item_use.kinswathe.knockout_drug", actor, target);
    }

    private static @Nullable Text formatPoisonInjectorUse(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        Text target = targetText(event, match);
        if (actor == null || target == null) {
            return null;
        }
        if (event.data().getBoolean("robot_failed")) {
            return Text.translatable("replay.item_use.kinswathe.poison_injector.failed_robot", target, actor);
        }
        return Text.translatable("replay.item_use.kinswathe.poison_injector", actor, target);
    }

    private static @Nullable Text formatSulfuricAcidBarrelUse(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        if (actor == null || !event.data().containsUuid("body_owner")) {
            return null;
        }
        Text corpseOwner = playerText(event.data().getUuid("body_owner"), match);
        return corpseOwner == null ? null : Text.translatable("replay.item_use.kinswathe.sulfuric_acid_barrel", actor, corpseOwner);
    }

    private static @Nullable Text formatBlowgunHit(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        Text target = targetText(event, match);
        if (actor == null || target == null) {
            return null;
        }
        if (event.data().getBoolean("robot_failed")) {
            return Text.translatable("replay.item_hit.kinswathe.blowgun.failed_robot", target, actor);
        }
        return Text.translatable("replay.item_hit.kinswathe.blowgun", actor, target);
    }

    private static @Nullable Text formatRobotNightVisionEnd(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        return actor == null ? null : Text.translatable("replay.global.kinswathe.robot_night_vision_end", actor);
    }

    private static @Nullable Text formatRobotPoisonImmune(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        if (actor == null) {
            return null;
        }
        Text item = ReplayGenerator.resolveItemName(event.data(), world);
        UUID poisoner = uuid(event.data(), "poisoner");
        Text poisonerText = playerText(poisoner, match);
        if (poisonerText == null) {
            return null;
        }
        return Text.translatable("replay.global.kinswathe.robot.poison_immune", actor, item, poisonerText);
    }

    private static @Nullable Text formatRobotBedPoisonImmune(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        Text actor = actorText(event, match);
        if (actor == null) {
            return null;
        }
        Text poisonerText = playerText(uuid(event.data(), "poisoner"), match);
        return poisonerText == null ? null : Text.translatable("replay.global.kinswathe.robot.bed_poison_immune", actor, poisonerText);
    }

}
