package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WallPillarBlock extends ConnectableRotatedPillarBlock implements SimpleWaterloggedBlock {

	protected static final VoxelShape BASE_SHAPE = Block.box(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D);

	protected static final VoxelShape WEST_SHAPE = Block.box(0.0D, 2.0D, 2.0D, 2.0D, 14.0D, 14.0D);
	protected static final VoxelShape EAST_SHAPE = Block.box(14.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
	protected static final VoxelShape DOWN_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
	protected static final VoxelShape UP_SHAPE = Block.box(2.0D, 14.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	protected static final VoxelShape NORTH_SHAPE = Block.box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 2.0D);
	protected static final VoxelShape SOUTH_SHAPE = Block.box(2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D);

	private static final VoxelShape WEST_FLAT = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
	private static final VoxelShape EAST_FLAT = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape DOWN_FLAT = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
	private static final VoxelShape UP_FLAT = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape NORTH_FLAT = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
	private static final VoxelShape SOUTH_FLAT = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	@SuppressWarnings("this-escape")
	public WallPillarBlock(BlockBehaviour.Properties properties, double width, double height) {
		super(properties, width, height);
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
		VoxelShape shape = BASE_SHAPE;
		Direction.Axis axis = state.getValue(WallPillarBlock.AXIS);

		if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_SHAPE);
		else if (axis.equals(Direction.Axis.Y)) shape = Shapes.or(shape, DOWN_FLAT);
		if (state.getValue(UP)) shape = Shapes.or(shape, UP_SHAPE);
		else if (axis.equals(Direction.Axis.Y)) shape = Shapes.or(shape, UP_FLAT);
		if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SHAPE);
		else if (axis.equals(Direction.Axis.Z)) shape = Shapes.or(shape, NORTH_FLAT);
		if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SHAPE);
		else if (axis.equals(Direction.Axis.Z)) shape = Shapes.or(shape, SOUTH_FLAT);
		if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SHAPE);
		else if (axis.equals(Direction.Axis.X)) shape = Shapes.or(shape, WEST_FLAT);
		if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SHAPE);
		else if (axis.equals(Direction.Axis.X)) shape = Shapes.or(shape, EAST_FLAT);

		return shape;
	}

	@Override
	public boolean canConnectTo(Direction.Axis thisAxis, Direction facing, BlockState facingState, boolean solidSide) {
		return facingState.getBlock() instanceof WallPillarBlock && (facing.getAxis() == thisAxis || facing.getAxis() == facingState.getValue(AXIS));
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return super.getBlockSupportShape(pState, pLevel, pPos);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
		boolean flag = fluidstate.getType() == Fluids.WATER;
		return super.getStateForPlacement(context).setValue(WATERLOGGED, flag);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor accessor, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) {
			accessor.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(accessor));
		}

		return super.updateShape(state, facing, facingState, accessor, currentPos, facingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED);
	}
}
