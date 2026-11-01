package com.hellmod.hell.block;

import com.hellmod.hell.HellMod;
import com.hellmod.hell.portal.HellPortalForcer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** 네더 포탈과 같은 구조의 지옥 포탈 블록 */
public class HellPortalBlock extends Block implements Portal {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public HellPortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis changed = direction.getAxis();
        boolean relevant = changed == Direction.Axis.Y || changed == state.getValue(AXIS);
        if (relevant && !neighborState.is(this) && !neighborState.is(Blocks.OBSIDIAN)) {
            return Blocks.AIR.defaultBlockState(); // 틀이 부서지면 포탈도 사라짐
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        if (entity instanceof Player player) {
            return Math.max(0, level.getGameRules().getInt(player.getAbilities().invulnerable
                    ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY
                    : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        }
        return 0;
    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        var targetKey = level.dimension() == HellMod.HELL_LEVEL ? Level.OVERWORLD : HellMod.HELL_LEVEL;
        ServerLevel target = level.getServer().getLevel(targetKey);
        if (target == null) {
            return null;
        }

        Vec3 arrive;
        var existing = HellPortalForcer.findExisting(target, pos);
        if (existing.isPresent()) {
            BlockPos p = existing.get();
            Direction.Axis axis = target.getBlockState(p).getValue(AXIS);
            Direction front = axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
            arrive = Vec3.atBottomCenterOf(p.relative(front));
        } else {
            BlockPos p = HellPortalForcer.create(target, pos);
            arrive = new Vec3(p.getX() + 1.0, p.getY(), p.getZ() + 1.5);
        }

        return new DimensionTransition(target, arrive, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                DimensionTransition.PLAY_PORTAL_SOUND);
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}
