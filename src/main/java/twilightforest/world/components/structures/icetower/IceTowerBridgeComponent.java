package twilightforest.world.components.structures.icetower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.world.components.structures.TFStructureComponent;
import twilightforest.world.components.structures.TFStructureComponentOld;


public class IceTowerBridgeComponent extends TFStructureComponentOld {

	private final int length;

	public IceTowerBridgeComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(TFStructurePieceTypes.TFITBri.get(), nbt);
		this.length = nbt.getInt("bridgeLength");
	}

	@SuppressWarnings("this-escape")
	public IceTowerBridgeComponent(int index, int x, int y, int z, int length, Direction direction) {
		super(TFStructurePieceTypes.TFITBri.get(), index, x, y, z);
		this.length = length;
		this.setOrientation(direction);

		this.boundingBox = BoundingBoxUtils.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, length, 6, 5, direction, false);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tagCompound) {
		super.addAdditionalSaveData(ctx, tagCompound);
		tagCompound.putInt("bridgeLength", this.length);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
		if (parent != null && parent instanceof TFStructureComponent tfStructureComponent) {
			this.deco = tfStructureComponent.deco;
		}
	}

	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		generateAirBox(world, sbb, 0, 1, 0, length, 5, 4);

		// make floor/ceiling
		generateBox(world, sbb, 0, 0, 0, length, 0, 4, deco.blockState, deco.blockState, false);
		generateBox(world, sbb, 0, 6, 0, length, 6, 4, deco.blockState, deco.blockState, false);

		// pillars
		for (int x = 2; x < length; x += 3) {
			generateBox(world, sbb, x, 1, 0, x, 5, 0, deco.pillarState, deco.pillarState, false);
			generateBox(world, sbb, x, 1, 4, x, 5, 4, deco.pillarState, deco.pillarState, false);
		}
	}
}