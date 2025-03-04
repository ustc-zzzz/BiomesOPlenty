package biomesoplenty.common.biome.end;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.common.biome.EndBiomeBOP;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BushConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.EndCityConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.surfacebuilders.CompositeSurfaceBuilder;

public class EndPlainsBiome extends EndBiomeBOP
{
	public EndPlainsBiome()
	{
        super((new Biome.BiomeBuilder()).surfaceBuilder(new CompositeSurfaceBuilder<>(DEFAULT_SURFACE_BUILDER, END_STONE_SURFACE)).precipitation(Biome.RainType.NONE).category(Biome.Category.THEEND).depth(0.1F).scale(0.2F).temperature(0.5F).downfall(0.5F).waterColor(4159204).waterFogColor(329011).parent((String)null));
	    
        this.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, createCompositeFeature(Feature.END_CITY, new EndCityConfig(), PASSTHROUGH, IPlacementConfig.NO_PLACEMENT_CONFIG));
		
        this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, createCompositeFeature(Feature.BUSH, new BushConfig(BOPBlocks.spectral_fern), TWICE_SURFACE, new FrequencyConfig(400)));
        
        this.addSpawn(EnumCreatureType.MONSTER, new Biome.SpawnListEntry(EntityType.ENDERMAN, 10, 4, 4));
	}
}
