package com.hellmod.hell.portal;

import com.hellmod.hell.ModBlocks;
import com.hellmod.hell.block.HellPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** 반대편 차원에서 기존 지옥 포탈을 찾거나, 없으면 새로 만든다. */
public final class HellPortalForcer {
    private static final int SEARCH_RADIUS = 16;

    private HellPortalForcer() {}

    public static Optional<BlockPos> findExisting(ServerLevel level, BlockPos origin) {
        int minY = level.getMinBuildHeight();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                for (int y = minY; y <= HellPortalShape.MAX_Y; y++) {
                    m.set(origin.getX() + dx, y, origin.getZ() + dz);
                    if (level.getBlockState(m).is(ModBlocks.HELL_PORTAL.get())) {
                        double dist = m.distSqr(origin);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = m.immutable();
                        }
                    }
                }
            }
        }
        if (best == null) {
            return Optional.empty();
        }
        while (level.getBlockState(best.below()).is(ModBlocks.HELL_PORTAL.get())) {
            best = best.below();
        }
        return Optional.of(best);
    }

    /** 내부 2x3 포탈(X축)을 만들고 내부 왼쪽 아래 좌표를 반환 */
    public static BlockPos create(ServerLevel level, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();
        int y = Mth.clamp(origin.getY(), level.getMinBuildHeight() + 6, HellPortalShape.MAX_Y);

        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 공간 비우기 + 바닥
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(x + dx, y - 1, z + dz), obsidian, 3);
                for (int dy = 0; dy <= 4; dy++) {
                    level.setBlock(new BlockPos(x + dx, y + dy, z + dz), air, 3);
                }
            }
        }
        // 틀
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                boolean edge = dx == -1 || dx == 2 || dy == -1 || dy == 3;
                if (edge) {
                    level.setBlock(new BlockPos(x + dx, y + dy, z), obsidian, 3);
                }
            }
        }
        // 포탈
        BlockState portal = ModBlocks.HELL_PORTAL.get().defaultBlockState()
                .setValue(HellPortalBlock.AXIS, net.minecraft.core.Direction.Axis.X);
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                level.setBlock(new BlockPos(x + dx, y + dy, z), portal, 18);
            }
        }
        return new BlockPos(x, y, z);
    }
}
