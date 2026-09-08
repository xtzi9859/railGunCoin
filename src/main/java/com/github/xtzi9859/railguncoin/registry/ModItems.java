package com.github.xtzi9859.railguncoin.registry;

import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(RailgunCoinMod.MOD_ID);

    public static final DeferredItem<Item> SILVER_COIN = REGISTER.registerSimpleItem(
            "silver_coin", new Item.Properties().stacksTo(64)
    );

    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(SILVER_COIN);
        }
    }

    private ModItems() {
    }
}
