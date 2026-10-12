package org.BsXinQin.kinswathe.component;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class BodyDeathReasonComponent implements AutoSyncedComponent {

    public static final ComponentKey<BodyDeathReasonComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "body"), BodyDeathReasonComponent.class);

    public Identifier deathReason = GameConstants.DeathReasons.GENERIC;
    public PlayerBodyEntity playerBodyEntity;

    public BodyDeathReasonComponent(@NotNull PlayerBodyEntity playerBodyEntity) {this.playerBodyEntity = playerBodyEntity;}

    public void sync() {
        KEY.sync(this.playerBodyEntity);
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putString("deathReason", deathReason.toString());
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.deathReason = Identifier.of(tag.getString("deathReason"));
    }
}
