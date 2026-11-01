package com.hellmod.hell;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(HellMod.MODID)
public class HellMod {
    public static final String MODID = "hell";

    /** 지옥 차원 키 (data/hell/dimension/hell.json) */
    public static final ResourceKey<Level> HELL_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(MODID, "hell"));

    public HellMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
    }
}
