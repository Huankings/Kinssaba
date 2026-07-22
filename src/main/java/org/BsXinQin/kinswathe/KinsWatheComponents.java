package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.component.*;
import org.BsXinQin.kinswathe.roles.kidnapper.KidnapperComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class KinsWatheComponents implements EntityComponentInitializer, WorldComponentInitializer {

    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(PlayerEntity.class, PlayerEffectComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerEffectComponent::new);
        registry.beginRegistration(PlayerEntity.class, AbilityPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(AbilityPlayerComponent::new);
        registry.beginRegistration(PlayerBodyEntity.class, BodyDeathReasonComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(BodyDeathReasonComponent::new);
        registry.beginRegistration(PlayerEntity.class, KidnapperComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(KidnapperComponent::new);
        registry.beginRegistration(PlayerEntity.class, TechnicianComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(TechnicianComponent::new);
    }

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(GameSafeComponent.KEY, GameSafeComponent::new);
        registry.register(ConfigWorldComponent.KEY, ConfigWorldComponent::new);
    }
}
