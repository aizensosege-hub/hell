package com.hellmod.hell.event;

import com.hellmod.hell.HellMod;
import com.hellmod.hell.portal.HellPortalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;

@EventBusSubscriber(modid = HellMod.MODID)
public class PortalEvents {

    /** 흑요석 틀 안쪽에서 메아리 수정으로 우클릭하면 포탈이 열린다. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.ECHO_SHARD)) return;
        if (event.getFace() == null) return;

        BlockPos clicked = event.getPos();
        if (!level.getBlockState(clicked).is(Blocks.OBSIDIAN)) return;
        if (level.dimension() != Level.OVERWORLD && level.dimension() != HellMod.HELL_LEVEL) return;

        BlockPos start = clicked.relative(event.getFace());
        Optional<HellPortalShape> shape = HellPortalShape.findAny(level, start);
        if (shape.isEmpty()) return;

        Player player = event.getEntity();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!shape.get().isValidHeight()) {
            player.displayClientMessage(Component.translatable("message.hell.portal_too_high"), true);
            return;
        }

        shape.get().createPortal(level);
        level.playSound(null, clicked, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 0.6F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
