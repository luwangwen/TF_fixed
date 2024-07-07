package twilightforest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import twilightforest.block.CloudBlock;
import twilightforest.block.WroughtIronFenceBlock;
import twilightforest.client.FoliageColorHandler;
import twilightforest.client.TFClientSetup;
import twilightforest.config.TFConfig;
import twilightforest.entity.TFPart;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFDimension;
import twilightforest.init.TFItems;
import twilightforest.init.custom.ChunkBlanketProcessors;
import twilightforest.item.ArcticArmorItem;
import twilightforest.item.mapdata.TFMagicMapData;
import twilightforest.network.UpdateTFMultipartPacket;
import twilightforest.util.WorldUtil;
import twilightforest.world.components.structures.CustomDensitySource;
import twilightforest.world.components.structures.util.CustomStructureData;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"JavadocReference", "unused", "RedundantSuppression", "deprecation"})
public class ASMHooks {

	/**
	 * {@link twilightforest.asm.transformers.map.RenderMapDecorationsTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.client.gui.MapRenderer.MapInstance#draw(PoseStack, MultiBufferSource, boolean, int)}
	 */
	public static int renderMapDecorations(int o, MapItemSavedData data, PoseStack stack, MultiBufferSource buffer, int light) {
		if (data instanceof TFMagicMapData mapData) {
			for (TFMagicMapData.TFMapDecoration decoration : mapData.tfDecorations.values()) {
				decoration.render(o, stack, buffer, light);
				o++;
			}
		}
		return o;
	}

	private static boolean isOurMap(ItemStack stack) {
		return stack.is(TFItems.FILLED_MAGIC_MAP.get()) || stack.is(TFItems.FILLED_MAZE_MAP.get()) || stack.is(TFItems.FILLED_ORE_MAP.get());
	}

	/**
	 * {@link twilightforest.asm.transformers.map.ShouldMapRenderInArmTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.client.renderer.ItemInHandRenderer#renderArmWithItem(AbstractClientPlayer, float, float, InteractionHand, float, ItemStack, float, PoseStack, MultiBufferSource, int)}<br/>
	 * Targets: {@link net.minecraft.world.item.Items#FILLED_MAP} and {@link net.minecraft.world.item.ItemStack#is(Item)}
	 */
	public static boolean shouldMapRenderInArm(boolean o, ItemStack stack) {
		return o || isOurMap(stack);
	}

	/**
	 * {@link twilightforest.asm.transformers.map.ResolveMapDataForRenderTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.client.renderer.ItemInHandRenderer#renderMap(PoseStack, MultiBufferSource, int, ItemStack)}
	 */
	@Nullable
	public static MapItemSavedData resolveMapDataForRender(@Nullable MapItemSavedData o, ItemStack stack, @Nullable Level level) {
		return isOurMap(stack) && level != null ? MapItem.getSavedData(stack, level) : o;
	}

	/**
	 * Injection Point:<br>
	 * {@link net.minecraft.client.sounds.MusicManager#tick()}<br>
	 * [AFTER FIRST INVOKEVIRTUAL]
	 */
	@OnlyIn(Dist.CLIENT)
	public static Music music(Music music) {
		if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null && (music == Musics.CREATIVE || music == Musics.UNDER_WATER) && TFDimension.isTwilightWorldOnClient(Minecraft.getInstance().level))
			return Minecraft.getInstance().level.getBiomeManager().getNoiseBiomeAtPosition(Minecraft.getInstance().player.blockPosition()).value().getBackgroundMusic().orElse(Musics.GAME);
		return music;
	}


	/**
	 * Injection Point:<br>
	 * {@link net.minecraft.server.level.ServerEntity#sendDirtyEntityData}<br>
	 * [AFTER GETFIELD]
	 */
	public static Entity updateMultiparts(Entity entity) {
		if (entity.isMultipartEntity())
			PacketDistributor.sendToPlayersTrackingEntity(entity, new UpdateTFMultipartPacket(entity));
		return entity;
	}

	/**
	 * Injection Point:<br>
	 * {@link net.minecraft.client.renderer.entity.EntityRenderDispatcher#getRenderer(Entity)}<br>
	 * [BEFORE LAST ARETURN]
	 */
	@Nullable
	public static EntityRenderer<?> getMultipartRenderer(@Nullable EntityRenderer<?> renderer, Entity entity) {
		if (entity instanceof TFPart<?>)
			return TFClientSetup.BakedMultiPartRenderers.lookup(((TFPart<?>) entity).renderer());
		return renderer;
	}

	/**
	 * Injection Point:<br>
	 * {@link net.minecraft.client.renderer.LevelRenderer#renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)}<br>
	 * [AFTER {@link net.minecraft.client.multiplayer.ClientLevel#entitiesForRendering}]
	 */
	public static Iterable<Entity> renderMultiparts(Iterable<Entity> iter) {
		List<Entity> list = new ArrayList<>();
		iter.forEach(entity -> {
			list.add(entity);
			if (entity.isMultipartEntity() && entity.getParts() != null) {
				for (PartEntity<?> part : entity.getParts()) {
					if (part instanceof TFPart)
						list.add(part);
				}
			}
		});
		return list;
	}

	/**
	 * {@link twilightforest.asm.transformers.foliage.FoliageColorResolverTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.client.renderer.BiomeColors#FOLIAGE_COLOR_RESOLVER}
	 */
	public static int resolveFoliageColor(int o, Biome biome, double x, double z) {
		return FoliageColorHandler.get(o, biome, x, z);
	}

	/**
	 * {@link twilightforest.asm.transformers.conquered.StructureStartLoadStaticTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.world.level.levelgen.structure.StructureStart#loadStaticStart(StructurePieceSerializationContext, CompoundTag, long)}<br/>
	 * Targets: {@link net.minecraft.world.level.levelgen.structure.StructureStart#StructureStart(Structure, ChunkPos, int, PiecesContainer)}
	 */
	public static StructureStart loadStaticStart(StructureStart start, PiecesContainer piecesContainer, CompoundTag nbt) {
		if (start.getStructure() instanceof CustomStructureData s)
			return s.forDeserialization(start.getStructure(), start.getChunkPos(), start.getReferences(), piecesContainer, nbt);
		return start;
	}

	/**
	 * {@link twilightforest.asm.transformers.book.ModifyWrittenBookNameTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.world.item.WrittenBookItem#getName(net.minecraft.world.item.ItemStack)}
	 */
	public static Component modifyWrittenBookName(Component component, ItemStack stack) {
		if (stack.has(TFDataComponents.TRANSLATABLE_BOOK)) {
			return Component.translatable(component.getString());
		} else return component;
	}

	/**
	 * Injection Point:<br>
	 * {@link net.minecraft.world.level.block.MushroomBlock#canSurvive(BlockState, LevelReader, BlockPos)}  }<br>
	 * [AFTER INVOKEINTERFACE {@link LevelReader#getRawBrightness(BlockPos, int)}]
	 */
	public static int shroom(int o, LevelReader level, BlockPos pos) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0)
					continue;
				if (level.getBlockState(pos.offset(x, -1, z)).is(TFBlocks.TWILIGHT_PORTAL))
					return 0;
			}
		}
		return o;
	}

	/**
	 * {@link twilightforest.asm.transformers.cloud.IsRainingAtTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.world.level.Level#isRainingAt(BlockPos)}
	 */
	public static boolean isRainingAt(boolean isRaining, Level level, BlockPos pos) {
		if (!isRaining && TFConfig.commonCloudBlockPrecipitationDistance > 0) {
			LevelChunk chunk = level.getChunkAt(pos);
			for (int y = pos.getY(); y < pos.getY() + TFConfig.commonCloudBlockPrecipitationDistance; y++) {
				BlockPos newPos = pos.atY(y);
				BlockState state = chunk.getBlockState(newPos);
				if (state.getBlock() instanceof CloudBlock cloudBlock && cloudBlock.getCurrentPrecipitation(newPos, level, level.getRainLevel(1.0F)).getLeft() == Biome.Precipitation.RAIN) {
					return true;
				}
				if (Heightmap.Types.MOTION_BLOCKING.isOpaque().test(state)) {
					return false;
				}
			}
		}
		return isRaining;
	}

	/**
	 * {@link twilightforest.asm.transformers.lead.LeashFenceKnotSurvivesTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.world.entity.decoration.LeashFenceKnotEntity#survives()}
	 */
	public static boolean leashFenceKnotSurvives(boolean o, LeashFenceKnotEntity entity) {
		if (o)
			return true; // Short-circuit to avoid an unnecessary #getBlockState call
		BlockState fenceState = entity.level().getBlockState(entity.getPos());
		return fenceState.is(TFBlocks.WROUGHT_IRON_FENCE) && fenceState.getValue(WroughtIronFenceBlock.POST) != WroughtIronFenceBlock.PostState.NONE;
	}

	/**
	 * {@link twilightforest.asm.transformers.chunk.ChunkStatusTaskTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.world.level.chunk.status.ChunkStatusTasks#generateSurface}
	 */
	public static void chunkBlanketing(ChunkAccess chunkAccess, WorldGenRegion worldGenRegion) {
		ChunkBlanketProcessors.chunkBlanketing(chunkAccess, worldGenRegion);
	}

	/**
	 * structure_terraform.js: attach<br>
	 * Injection point:<br>
	 * {@link net.minecraft.world.level.levelgen.Beardifier#forStructuresInChunk(StructureManager, ChunkPos)}<br>
	 * [BEFORE ARETURN]
	 */
	public static ObjectListIterator<DensityFunction> gatherCustomTerrain(StructureManager structureManager, ChunkPos chunkPos) {
		ObjectArrayList<DensityFunction> customStructureTerraforms = new ObjectArrayList<>(10);

		for (StructureStart structureStart : structureManager.startsForStructure(chunkPos, s -> s instanceof CustomDensitySource))
			if (structureStart.getStructure() instanceof CustomDensitySource customDensitySource)
				customStructureTerraforms.add(customDensitySource.getStructureTerraformer(chunkPos, structureStart));

		return customStructureTerraforms.iterator();
	}

	/**
	 * structure_terraform.js: recompute<br>
	 * Injection point:<br>
	 * {@link net.minecraft.world.level.levelgen.Beardifier#compute(DensityFunction.FunctionContext)}<br>
	 * [BEFORE DRETURN]
	 */
	public static double getCustomDensity(double densityBefore, DensityFunction.FunctionContext context, ObjectListIterator<DensityFunction> customDensities) {
		double newDensity = 0;

		while (customDensities.hasNext()) {
			newDensity += customDensities.next().compute(context);
		}
		customDensities.back(Integer.MAX_VALUE);

		return densityBefore + newDensity;
	}

	/**
	 * Injection Point:<br>
	 * {@link net.minecraft.world.level.chunk.ChunkGenerator#findNearestMapStructure(ServerLevel, HolderSet, BlockPos, int, boolean)}<br>
	 * [BEFORE LAST ARETURN]
	 */
	@Nullable
	public static Pair<BlockPos, Holder<Structure>> findNearestMapLandmark(@Nullable Pair<BlockPos, Holder<Structure>> oldReturnable, ServerLevel level, HolderSet<Structure> targetStructures, BlockPos pos, int searchRadius, boolean skipKnownStructures) {
		Pair<BlockPos, Holder<Structure>> nearestLandmark = WorldUtil.findNearestMapLandmark(level, targetStructures, pos, searchRadius, skipKnownStructures);

		return nearestLandmark != null ? nearestLandmark : oldReturnable;
	}

	/**
	 * {@link twilightforest.asm.transformers.armor.CancelArmorRenderingTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#renderArmorPiece(PoseStack, MultiBufferSource, LivingEntity, EquipmentSlot, int, HumanoidModel)}
	 */
	public static boolean cancelArmorRendering(boolean o, ItemStack stack) {
		if (o && stack.get(TFDataComponents.EMPERORS_CLOTH) != null) {
			return false;
		}
		return o;
	}

	/**
	 * {@link twilightforest.asm.transformers.armor.ArmorVisibilityRenderingTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.world.entity.LivingEntity#getVisibilityPercent(Entity)}
	 */
	public static float modifyArmorVisibility(float o, LivingEntity entity) {
		return o - getShroudedArmorPercentage(entity);
	}

	private static float getShroudedArmorPercentage(LivingEntity entity) {
		Iterable<ItemStack> iterable = entity.getArmorSlots();
		int shroudedArmor = 0;
		int nonShroudedArmor = 0;

		for (ItemStack stack : iterable) {
			if (!stack.isEmpty() && stack.get(TFDataComponents.EMPERORS_CLOTH) != null) {
				shroudedArmor++;
			}

			nonShroudedArmor++;
		}

		return nonShroudedArmor > 0 && shroudedArmor > 0 ? (float) shroudedArmor / (float) nonShroudedArmor : 0.0F;
	}

	/**
	 * {@link twilightforest.asm.transformers.armor.ArmorColorRenderingTransformer}<p/>
	 *
	 * Injection Point:<br/>
	 * {@link net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#renderArmorPiece(PoseStack, MultiBufferSource, LivingEntity, EquipmentSlot, int, HumanoidModel)} <br/>
	 * Targets: {@link net.minecraft.world.item.component.DyedItemColor#getOrDefault(net.minecraft.world.item.ItemStack, int)}
	 */
	public static int armorColorRendering(int color, ArmorItem armorItem, ItemStack armorStack) {
		if (armorItem instanceof ArcticArmorItem) return DyedItemColor.getOrDefault(armorStack, ArcticArmorItem.DEFAULT_COLOR);
		return color;
	}
}
