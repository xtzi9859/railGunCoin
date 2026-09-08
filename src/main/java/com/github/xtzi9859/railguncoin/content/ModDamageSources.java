package com.github.xtzi9859.railguncoin.content;

import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public final class ModDamageSources {
    public static final ResourceKey<DamageType> COIN_IMPACT = key("coin_impact");
    public static final ResourceKey<DamageType> MISFIRE = key("misfire");

    private ModDamageSources() {
    }

    public static DamageSource coinImpact(Level level, Entity projectile, @Nullable Entity owner) {
        return new DamageSource(holder(level, COIN_IMPACT), projectile, owner);
    }

    public static DamageSource misfire(Level level, Entity player, ItemStack railgun) {
        Component customName = railgun.get(DataComponents.CUSTOM_NAME);
        return new MisfireDamageSource(
                holder(level, MISFIRE),
                player,
                customName == null ? null : customName.copy()
        );
    }

    private static Holder.Reference<DamageType> holder(Level level, ResourceKey<DamageType> key) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, RailgunCoinMod.id(path));
    }

    private static final class MisfireDamageSource extends DamageSource {
        @Nullable
        private final Component railgunName;

        private MisfireDamageSource(
                Holder<DamageType> type,
                Entity player,
                @Nullable Component railgunName
        ) {
            super(type, player);
            this.railgunName = railgunName;
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity victim) {
            if (railgunName != null) {
                return Component.translatable(
                        "death.attack.railguncoin.misfire.named",
                        victim.getDisplayName(),
                        railgunName
                );
            }

            return  Component.translatable(
                    "death.attack.railguncoin.misfire",
                    victim.getDisplayName()
            );
        }
    }
}
