package com.github.xtzi9859.railguncoin;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import com.github.xtzi9859.railguncoin.content.CoinProjectileProperties;
import com.github.xtzi9859.railguncoin.registry.ModEntityTypes;
import com.github.xtzi9859.railguncoin.registry.ModItems;
import com.github.xtzi9859.railguncoin.registry.ModParticles;
import com.github.xtzi9859.railguncoin.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(RailgunCoinMod.MOD_ID)
public final class RailgunCoinMod {
    public static final String MOD_ID = "railguncoin";

    public RailgunCoinMod(IEventBus modBus) {
        ModItems.REGISTER.register(modBus);
        ModEntityTypes.REGISTER.register(modBus);
        ModParticles.REGISTER.register(modBus);
        ModSounds.REGISTER.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(ModItems::addToCreativeTab);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> RailgunHandler.registerProjectile(
                () -> net.minecraft.world.item.crafting.Ingredient.of(ModItems.SILVER_COIN.get()),
                CoinProjectileProperties.INSTANCE
        ));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
