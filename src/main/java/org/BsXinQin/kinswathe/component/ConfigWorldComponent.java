package org.BsXinQin.kinswathe.component;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class ConfigWorldComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<ConfigWorldComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "config"), ConfigWorldComponent.class);

    private final World world;
    public void sync() {KEY.sync(this.world);}
    public ConfigWorldComponent(@NotNull World world) {this.world = world;}
    @Override public void serverTick() {this.sync();}

    /// 具体配置内容
    //全局配置
    public int StartingCooldown = GameConstants.getInTicks(0,30) / 20;
    public boolean EnableJumpNotInGame = false;
    public boolean EnableStartSafeTime = false;
    public boolean EnableBetterBlackout = true;
    public boolean EnableAutoPsychoInstinct = true;
    //关于KinsWathe修改
    public boolean BodymakerAbilityFakeRole = true;
    public int JudgeAbilityPrice = 300;
    public int LicensedVillainRevolverPrice = 300;
    public int TechnicianWrenchPrice = 100;
    public int TechnicianCaptureDevicePrice = 100;
    public int TechnicianPowerRestorationPrice = 300;
    //关于NoellesRoles修改
    public boolean EnableNoellesRolesModify = false;
    public boolean ConductorInstinctModify = false;
    public boolean CoronerInstinctModify = false;

    /// 同步服务端客户端配置
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        //全局配置
        StartingCooldown = KinsWatheConfig.HANDLER.instance().StartingCooldown; tag.putInt("StartingCooldown", this.StartingCooldown);
        EnableJumpNotInGame = KinsWatheConfig.HANDLER.instance().EnableJumpNotInGame; tag.putBoolean("EnableJumpNotInGame", this.EnableJumpNotInGame);
        EnableStartSafeTime = KinsWatheConfig.HANDLER.instance().EnableStartSafeTime; tag.putBoolean("EnableStartSafeTime", this.EnableStartSafeTime);
        EnableBetterBlackout = KinsWatheConfig.HANDLER.instance().EnableBetterBlackout; tag.putBoolean("EnableBetterBlackout", this.EnableBetterBlackout);
        EnableAutoPsychoInstinct = KinsWatheConfig.HANDLER.instance().EnableAutoPsychoInstinct; tag.putBoolean("EnableAutoPsychoInstinct", this.EnableAutoPsychoInstinct);
        //关于KinsWathe修改
        BodymakerAbilityFakeRole = KinsWatheConfig.HANDLER.instance().BodymakerAbilityFakeRole; tag.putBoolean("BodymakerAbilityFakeRole", this.BodymakerAbilityFakeRole);
        JudgeAbilityPrice = KinsWatheConfig.HANDLER.instance().JudgeAbilityPrice; tag.putInt("JudgeAbilityPrice", this.JudgeAbilityPrice);
        LicensedVillainRevolverPrice = KinsWatheConfig.HANDLER.instance().LicensedVillainRevolverPrice; tag.putInt("LicensedVillainRevolverPrice", this.LicensedVillainRevolverPrice);
        TechnicianWrenchPrice = KinsWatheConfig.HANDLER.instance().TechnicianWrenchPrice; tag.putInt("TechnicianWrenchPrice", this.TechnicianWrenchPrice);
        TechnicianCaptureDevicePrice = KinsWatheConfig.HANDLER.instance().TechnicianCaptureDevicePrice; tag.putInt("TechnicianCaptureDevicePrice", this.TechnicianCaptureDevicePrice);
        TechnicianPowerRestorationPrice = KinsWatheConfig.HANDLER.instance().TechnicianPowerRestorationPrice; tag.putInt("TechnicianPowerRestorationPrice", this.TechnicianPowerRestorationPrice);
        //关于NoellesRoles修改
        EnableNoellesRolesModify = KinsWatheConfig.HANDLER.instance().EnableNoellesRolesModify; tag.putBoolean("EnableNoellesRolesModify", this.EnableNoellesRolesModify);
        ConductorInstinctModify = KinsWatheConfig.HANDLER.instance().ConductorInstinctModify; tag.putBoolean("ConductorInstinctModify", this.ConductorInstinctModify);
        CoronerInstinctModify = KinsWatheConfig.HANDLER.instance().CoronerInstinctModify; tag.putBoolean("CoronerInstinctModify", this.CoronerInstinctModify);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        //全局配置
        if (tag.contains("StartingCooldown"))   this.StartingCooldown = tag.getInt("StartingCooldown");
        if (tag.contains("EnableJumpNotInGame"))   this.EnableJumpNotInGame = tag.getBoolean("EnableJumpNotInGame");
        if (tag.contains("EnableStartSafeTime"))   this.EnableStartSafeTime = tag.getBoolean("EnableStartSafeTime");
        if (tag.contains("EnableBetterBlackout"))   this.EnableBetterBlackout = tag.getBoolean("EnableBetterBlackout");
        if (tag.contains("EnableAutoPsychoInstinct"))   this.EnableAutoPsychoInstinct = tag.getBoolean("EnableAutoPsychoInstinct");
        //关于KinsWathe修改
        if (tag.contains("BodymakerAbilityFakeRole"))   this.BodymakerAbilityFakeRole = tag.getBoolean("BodymakerAbilityFakeRole");
        if (tag.contains("JudgeAbilityPrice"))   this.JudgeAbilityPrice = tag.getInt("JudgeAbilityPrice");
        if (tag.contains("LicensedVillainRevolverPrice"))   this.LicensedVillainRevolverPrice = tag.getInt("LicensedVillainRevolverPrice");
        if (tag.contains("TechnicianWrenchPrice"))   this.TechnicianWrenchPrice = tag.getInt("TechnicianWrenchPrice");
        if (tag.contains("TechnicianCaptureDevicePrice"))   this.TechnicianCaptureDevicePrice = tag.getInt("TechnicianCaptureDevicePrice");
        if (tag.contains("TechnicianPowerRestorationPrice"))   this.TechnicianPowerRestorationPrice = tag.getInt("TechnicianPowerRestorationPrice");
        //关于NoellesRoles修改
        if (tag.contains("EnableNoellesRolesModify"))   this.EnableNoellesRolesModify = tag.getBoolean("EnableNoellesRolesModify");
        if (tag.contains("ConductorInstinctModify"))   this.ConductorInstinctModify = tag.getBoolean("ConductorInstinctModify");
        if (tag.contains("CoronerInstinctModify"))   this.CoronerInstinctModify = tag.getBoolean("CoronerInstinctModify");
    }
}
