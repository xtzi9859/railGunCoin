package jp.example.railguncoin.content;

import jp.example.railguncoin.RailgunCoinMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class ModDamageSources {
    public static final ResourceKey<DamageType> COIN_IMPACT = key("coin_impact");
    public static final ResourceKey<DamageType> MISFIRE = key("misfire");

    private ModDamageSources() {
    }

    public static DamageSource coinImpact(Level level, Entity projectile, @Nullable Entity owner) {
        return new DamageSource(holder(level, COIN_IMPACT), projectile, owner);
    }

    public static DamageSource misfire(Level level, Entity player) {
        return new DamageSource(holder(level, MISFIRE), player);
    }

    private static Holder.Reference<DamageType> holder(Level level, ResourceKey<DamageType> key) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, RailgunCoinMod.id(path));
    }
}
