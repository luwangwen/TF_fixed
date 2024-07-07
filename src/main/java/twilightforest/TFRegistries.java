package twilightforest;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;
import twilightforest.entity.MagicPaintingVariant;
import twilightforest.entity.passive.DwarfRabbitVariant;
import twilightforest.entity.passive.TinyBirdVariant;
import twilightforest.util.Enforcement;
import twilightforest.util.Restriction;
import twilightforest.util.WoodPalette;
import twilightforest.world.components.chunkblanketing.ChunkBlanketProcessor;
import twilightforest.world.components.chunkblanketing.ChunkBlanketType;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerFactory;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerType;
import twilightforest.world.components.layer.BiomeDensitySource;
import twilightforest.world.components.structures.StructureSpeleothemConfig;

import java.util.Locale;

public class TFRegistries {

	public static final Registry<BiomeLayerType> BIOME_LAYER_TYPE = new RegistryBuilder<>(Keys.BIOME_LAYER_TYPE).create();
	public static final Registry<Enforcement> ENFORCEMENT = new RegistryBuilder<>(Keys.ENFORCEMENT).sync(true).create();
	public static final Registry<ChunkBlanketType> CHUNK_BLANKET_TYPES = new RegistryBuilder<>(Keys.CHUNK_BLANKET_TYPE).create();

	public static final class Keys {
		public static final String REGISTRY_NAMESPACE = "twilight";

		//Normal Registries
		public static final ResourceKey<Registry<BiomeLayerType>> BIOME_LAYER_TYPE = ResourceKey.createRegistryKey(namedRegistry("biome_layer_type"));
		public static final ResourceKey<Registry<Enforcement>> ENFORCEMENT = ResourceKey.createRegistryKey(TwilightForestMod.prefix("enforcement"));
		public static final ResourceKey<Registry<ChunkBlanketType>> CHUNK_BLANKET_TYPE = ResourceKey.createRegistryKey(TwilightForestMod.prefix("chunk_blanket_type"));

		//Datapack Registries
		public static final ResourceKey<Registry<BiomeLayerFactory>> BIOME_STACK = ResourceKey.createRegistryKey(namedRegistry("biome_layer_stack"));
		public static final ResourceKey<Registry<BiomeDensitySource>> BIOME_TERRAIN_DATA = ResourceKey.createRegistryKey(namedRegistry("biome_terrain_data"));
		public static final ResourceKey<Registry<DwarfRabbitVariant>> DWARF_RABBIT_VARIANT = ResourceKey.createRegistryKey(namedRegistry("dwarf_rabbit_variant"));
		public static final ResourceKey<Registry<MagicPaintingVariant>> MAGIC_PAINTINGS = ResourceKey.createRegistryKey(namedRegistry("magic_paintings"));
		public static final ResourceKey<Registry<Restriction>> RESTRICTIONS = ResourceKey.createRegistryKey(namedRegistry("restrictions"));
		public static final ResourceKey<Registry<StructureSpeleothemConfig>> STRUCTURE_SPELEOTHEM_SETTINGS = ResourceKey.createRegistryKey(namedRegistry("structure_speleothem_settings"));
		public static final ResourceKey<Registry<TinyBirdVariant>> TINY_BIRD_VARIANT = ResourceKey.createRegistryKey(namedRegistry("tiny_bird_variant"));
		public static final ResourceKey<Registry<WoodPalette>> WOOD_PALETTES = ResourceKey.createRegistryKey(namedRegistry("wood_palettes"));
		public static final ResourceKey<Registry<ChunkBlanketProcessor>> CHUNK_BLANKET_PROCESSORS = ResourceKey.createRegistryKey(namedRegistry("chunk_blanket_processors"));

		public static ResourceLocation namedRegistry(String name) {
			return ResourceLocation.fromNamespaceAndPath(REGISTRY_NAMESPACE, name.toLowerCase(Locale.ROOT));
		}
	}
}
