package com.hellmod.hell.portal;

import com.hellmod.hell.ModBlocks;
import com.hellmod.hell.block.HellPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** 흑요석 틀(내부 2x3 ~ 21x21) 탐지. 네더 포탈과 같은 형태. */
public record HellPortalShape(Direction.Axis axis, BlockPos bottomLeft, int width, int height) {
    /** 포탈 내부 맨 아래 칸의 Y가 이 값 이하여야 열 수 있음 */
    public static final int MAX_Y = 0;

    private static final int MIN_WIDTH = 2;
    private static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    private static final int MAX_HEIGHT = 21;

    public static Optional<HellPortalShape> findAny(LevelAccessor level, BlockPos start) {
        return find(level, start, Direction.Axis.X).or(() -> find(level, start, Direction.Axis.Z));
    }

    public static Optional<HellPortalShape> find(LevelAccessor level, BlockPos start, Direction.Axis axis) {
        if (!isEmpty(level, start)) {
            return Optional.empty();
        }
        Direction along = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction back = along.getOpposite();

        // 맨 아래로
        BlockPos pos = start;
        int guard = 0;
        while (guard++ <= MAX_HEIGHT && isEmpty(level, pos.below())) {
            pos = pos.below();
        }
        if (!isFrame(level, pos.below())) {
            return Optional.empty();
        }

        // 맨 왼쪽으로
        guard = 0;
        while (guard++ <= MAX_WIDTH && isEmpty(level, pos.relative(back))
                && isFrame(level, pos.relative(back).below())) {
            pos = pos.relative(back);
        }
        if (!isFrame(level, pos.relative(back))) {
            return Optional.empty();
        }

        // 너비
        int width = 0;
        while (width <= MAX_WIDTH && isEmpty(level, pos.relative(along, width))
                && isFrame(level, pos.relative(along, width).below())) {
            width++;
        }
        if (width < MIN_WIDTH || width > MAX_WIDTH || !isFrame(level, pos.relative(along, width))) {
            return Optional.empty();
        }

        // 높이
        int height = 0;
        while (true) {
            if (height > MAX_HEIGHT) {
                return Optional.empty();
            }
            boolean allFrame = true;
            boolean allEmpty = true;
            for (int w = 0; w < width; w++) {
                BlockPos p = pos.relative(along, w).above(height);
                if (!isFrame(level, p)) allFrame = false;
                if (!isEmpty(level, p)) allEmpty = false;
            }
            if (allFrame) {
                break; // 윗변 도달
            }
            if (!allEmpty) {
                return Optional.empty();
            }
            if (!isFrame(level, pos.relative(back).above(height))
                    || !isFrame(level, pos.relative(along, width).above(height))) {
                return Optional.empty();
            }
            height++;
        }
        if (height < MIN_HEIGHT) {
            return Optional.empty();
        }
        return Optional.of(new HellPortalShape(axis, pos, width, height));
    }

    public boolean isValidHeight() {
        return bottomLeft.getY() <= MAX_Y;
    }

    public void createPortal(LevelAccessor level) {
        Direction along = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockState state = ModBlocks.HELL_PORTAL.get().defaultBlockState().setValue(HellPortalBlock.AXIS, axis);
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                level.setBlock(bottomLeft.relative(along, w).above(h), state, 18);
            }
        }
    }

    private static boolean isEmpty(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }

    private static boolean isFrame(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.OBSIDIAN);
    }
}
