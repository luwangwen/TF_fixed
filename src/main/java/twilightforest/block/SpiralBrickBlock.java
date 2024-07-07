package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import twilightforest.enums.Diagonals;

public class SpiralBrickBlock extends Block implements SimpleWaterloggedBlock {

	public static final EnumProperty<Diagonals> DIAGONAL = EnumProperty.create("diagonal", Diagonals.class);
	public static final EnumProperty<Direction.Axis> AXIS_FACING = EnumProperty.create("axis", Direction.Axis.class);

	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	@SuppressWarnings("this-escape")
	public SpiralBrickBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any().setValue(DIAGONAL, Diagonals.BOTTOM_RIGHT).setValue(AXIS_FACING, Direction.Axis.X).setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AXIS_FACING, DIAGONAL, WATERLOGGED);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.isSecondaryUseActive()) {
			//if sneaking, place on the y axis with glazed terracotta logic
			return this.defaultBlockState()
				.setValue(AXIS_FACING, Direction.Axis.Y)
				.setValue(DIAGONAL, convertVerticalDirectionToDiagonal(context.getHorizontalDirection()))
				.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
		} else {
			//otherwise, place on the x and z with stair logic
			return this.defaultBlockState()
				.setValue(AXIS_FACING, context.getHorizontalDirection().getAxis())
				.setValue(DIAGONAL, getHorizontalDiagonalFromPlayerPlacement(context.getPlayer(), context.getHorizontalDirection(), context.getClickLocation().y - (double) context.getClickedPos().getY() > 0.5D))
				.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
		}
	}

	private static Diagonals convertVerticalDirectionToDiagonal(Direction facing) {
		return switch (facing) {
			default -> Diagonals.TOP_RIGHT;
			case SOUTH -> Diagonals.BOTTOM_LEFT;
			case EAST -> Diagonals.TOP_LEFT;
			case WEST -> Diagonals.BOTTOM_RIGHT;
		};
	}

	private static Diagonals getHorizontalDiagonalFromPlayerPlacement(LivingEntity placer, Direction facing, boolean upper) {
		return switch (facing) {
			case NORTH, EAST -> Diagonals.getDiagonalFromUpDownLeftRight(placer.getDirection() == facing, upper);
			case SOUTH, WEST -> Diagonals.getDiagonalFromUpDownLeftRight(placer.getDirection() != facing, upper);
			default -> Diagonals.BOTTOM_RIGHT;
		};
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor accessor, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) accessor.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(accessor));
		return super.updateShape(state, facing, facingState, accessor, currentPos, facingPos);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		Direction.Axis axis = state.getValue(AXIS_FACING);
		if (rot == Rotation.CLOCKWISE_180 || (axis == Direction.Axis.X && rot == Rotation.CLOCKWISE_90) || (axis == Direction.Axis.Z && rot == Rotation.COUNTERCLOCKWISE_90))
			state = state.setValue(DIAGONAL, Diagonals.mirror(state.getValue(DIAGONAL), Mirror.LEFT_RIGHT));

		switch (rot) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> {
				return switch (state.getValue(AXIS_FACING)) {
					case X -> state.setValue(AXIS_FACING, Direction.Axis.Z);
					case Z -> state.setValue(AXIS_FACING, Direction.Axis.X);
					default -> state;
				};
			}
			default -> {
				return state;
			}
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(DIAGONAL, Diagonals.mirrorOn(state.getValue(AXIS_FACING), state.getValue(DIAGONAL), mirror));
	}
}
