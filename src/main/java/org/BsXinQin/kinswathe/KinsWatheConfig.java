package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.game.GameConstants;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class KinsWatheConfig {
    /// 配置文件设置
    public static ConfigClassHandler<KinsWatheConfig> HANDLER = ConfigClassHandler.createBuilder(KinsWatheConfig.class).id(Identifier.of(KinsWathe.MOD_ID, "config")).serializer(config -> GsonConfigSerializerBuilder.create(config).setPath(FabricLoader.getInstance().getConfigDir().resolve(KinsWathe.MOD_ID + ".json5")).setJson5(true).build()).build();

    @SerialEntry(comment = "Starting cooldown and game safe time (in seconds).")
    public int StartingCooldown = GameConstants.getInTicks(0,30) / 20;

    @SerialEntry(comment = "Whether to enable staminabar.")
    public boolean EnableStaminaBar = true;

    @SerialEntry(comment = "Whether to enable jump when not in game.")
    public boolean EnableJumpNotInGame = false;

    @SerialEntry(comment = "Whether to enable safe time when game starts.")
    public boolean EnableStartSafeTime = false;

    @SerialEntry(comment = "Whether to enable auto join voice chat group for spectators.")
    public boolean EnableAutoJoinVoiceChat = true;

    @SerialEntry(comment = "Whether to enable better Blackout visual effect.")
    public boolean EnableBetterBlackout = true;

    @SerialEntry(comment = "Whether to enable instinct when in Psycho Mode.")
    public boolean EnableAutoPsychoInstinct = true;

    /// 关于Wathe修改
    @SerialEntry(comment = "\n\n[Wathe] Modify (default settings are original settings of Wathe):")
    public boolean EnableWatheModify = false;

    @SerialEntry(comment = "Initial civilians income.")
    public int InitialCivilianIncome = 0;

    @SerialEntry(comment = "Initial neutrals income.")
    public int InitialNeutralIncome = 0;

    @SerialEntry(comment = "Initial killers income.")
    public int InitialKillerIncome = 100;

    @SerialEntry(comment = "Increase money when kill player.")
    public int IncreaseMoneyWhenKill = 100;

    @SerialEntry(comment = "Whether Revolver drop after killer kills civilian.")
    public boolean PreventKillerDropRevolver = false;

    /// 关于KinsWathe修改
    @SerialEntry(comment = "\n\n[Kin's Wathe] Modify:\nBodymaker: modify cooldown of ability and whether to fake role of body.")
    public int BodymakerAbilityCooldown = GameConstants.getInTicks(1,30) / 20;
    @SerialEntry
    public boolean BodymakerAbilityFakeRole = true;

    @SerialEntry(comment = "Drugmaker: modify generation player limit, get coins when someone was poisoned and price of Poison Injector and Blowgun.")
    public int DrugmakerPlayerLimit = 10;
    @SerialEntry
    public int DrugmakerGetCoins = 50;
    @SerialEntry
    public int DrugmakerPoisonInjectorPrice = 100;
    @SerialEntry
    public int DrugmakerBlowgunPrice = 175;

    @SerialEntry(comment = "Judge: modify price and cooldown of ability.")
    public int JudgeAbilityPrice = 300;
    @SerialEntry
    public int JudgeAbilityGlowing = GameConstants.getInTicks(1,30) / 20;
    @SerialEntry
    public int JudgeAbilityCooldown = GameConstants.getInTicks(3,0) / 20;

    @SerialEntry(comment = "Kidnapper: modify price of Knockout Drug and get additional coins when kill dazed player.")
    public int KidnapperKnockoutDrugPrice = 75;
    @SerialEntry
    public int KidnapperGetAdditionalCoins = 100;

    @SerialEntry(comment = "Licensed Villain: modify generation player limit and price of Revolver.")
    public int LicensedVillainPlayerLimit = 10;
    @SerialEntry
    public int LicensedVillainRevolverPrice = 300;

    @SerialEntry(comment = "Robot: modify duration and cooldown of ability.")
    public int RobotAbilityDuration = GameConstants.getInTicks(0,10) / 20;
    @SerialEntry
    public int RobotAbilityCooldown = GameConstants.getInTicks(1,30) / 20;

    @SerialEntry(comment = "Technician: modify price of props and Capture Device parameter.")
    public int TechnicianWrenchPrice = 100;
    @SerialEntry
    public int TechnicianCaptureDevicePrice = 100;
    @SerialEntry
    public int TechnicianPowerRestorationPrice = 300;
    @SerialEntry
    public int TechnicianCaptureDeviceStunTime = GameConstants.getInTicks(0,5) / 20;
    @SerialEntry
    public int TechnicianCaptureDeviceLifetimeSeconds = GameConstants.getInTicks(3,0) / 20;

    @SerialEntry(comment = "Violator: whether to allow Violator to retain disable/enable state after a server restart.")
    public boolean ViolatorEnabled = false;

    /// 关于NoellesRoles修改
    @SerialEntry(comment = "\n\n[Noelle's Roles] Modify (default settings are original settings of Noelle's Roles):")
    public boolean EnableNoellesRolesModify = false;

    @SerialEntry(comment = "Conductor: whether to enable instinct of seeing dropped items.")
    public boolean ConductorInstinctModify = false;

    @SerialEntry(comment = "Coroner: whether to enable instinct of seeing player bodies.")
    public boolean CoronerInstinctModify = false;

    @SerialEntry(comment = "Jester: whether to enable prevent attack killer in Psycho Mode.")
    public boolean JesterAttackKillerModify = false;
}
