package jp.example.railguncoin.registry;

import jp.example.railguncoin.RailgunCoinMod;
import jp.example.railguncoin.content.CoinProjectileEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(
            BuiltInRegistries.ENTITY_TYPE, RailgunCoinMod.MOD_ID
    );

    public static final DeferredHolder<EntityType<?>, EntityType<CoinProjectileEntity>> COIN_PROJECTILE =
            REGISTER.register("coin_projectile", () -> EntityType.Builder
                    .<CoinProjectileEntity>of(CoinProjectileEntity::new, MobCategory.MISC)
                    .sized(0.125F, 0.125F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(RailgunCoinMod.MOD_ID + ":coin_projectile"));

    private ModEntityTypes() {
    }
}
