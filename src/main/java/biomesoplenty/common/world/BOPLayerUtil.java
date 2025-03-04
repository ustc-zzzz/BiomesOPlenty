/*******************************************************************************
 * Copyright 2014-2019, the Biomes O' Plenty Team
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/
package biomesoplenty.common.world;

import biomesoplenty.common.world.layer.*;
import biomesoplenty.common.world.layer.traits.LazyAreaLayerContextBOP;
import com.google.common.collect.ImmutableList;
import net.minecraft.init.Biomes;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.IContextExtended;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraft.world.gen.area.IArea;
import net.minecraft.world.gen.area.IAreaFactory;
import net.minecraft.world.gen.area.LazyArea;
import net.minecraft.world.gen.layer.*;

import java.util.function.LongFunction;

public class BOPLayerUtil
{
    public static final int WARM_OCEAN = IRegistry.BIOME.getId(Biomes.WARM_OCEAN);
    public static final int LUKEWARM_OCEAN = IRegistry.BIOME.getId(Biomes.LUKEWARM_OCEAN);
    public static final int OCEAN = IRegistry.BIOME.getId(Biomes.OCEAN);
    public static final int COLD_OCEAN = IRegistry.BIOME.getId(Biomes.COLD_OCEAN);
    public static final int FROZEN_OCEAN = IRegistry.BIOME.getId(Biomes.FROZEN_OCEAN);
    public static final int DEEP_WARM_OCEAN = IRegistry.BIOME.getId(Biomes.DEEP_WARM_OCEAN);
    public static final int DEEP_LUKEWARM_OCEAN = IRegistry.BIOME.getId(Biomes.DEEP_LUKEWARM_OCEAN);
    public static final int DEEP_OCEAN = IRegistry.BIOME.getId(Biomes.DEEP_OCEAN);
    public static final int DEEP_COLD_OCEAN = IRegistry.BIOME.getId(Biomes.DEEP_COLD_OCEAN);
    public static final int DEEP_FROZEN_OCEAN = IRegistry.BIOME.getId(Biomes.DEEP_FROZEN_OCEAN);

    public static <T extends IArea, C extends IContextExtended<T>> IAreaFactory<T> createInitialLandAndSeaFactory(LongFunction<C> contextFactory)
    {
        // NOTE: Normally AddSnow, CoolWarm, HeatIce and Special GenLayers occur here, but we handle those ourselves
        IAreaFactory<T> factory = GenLayerIsland.INSTANCE.apply(contextFactory.apply(1L));
        factory = GenLayerZoom.FUZZY.apply(contextFactory.apply(2000L), factory);
        factory = GenLayerAddIsland.INSTANCE.apply(contextFactory.apply(1L), factory);
        factory = GenLayerZoom.NORMAL.apply(contextFactory.apply(2001L), factory);
        factory = GenLayerAddIsland.INSTANCE.apply(contextFactory.apply(2L), factory);
        factory = GenLayerAddIsland.INSTANCE.apply(contextFactory.apply(50L), factory);
        factory = GenLayerAddIsland.INSTANCE.apply(contextFactory.apply(70L), factory);
        factory = GenLayerRemoveTooMuchOcean.INSTANCE.apply(contextFactory.apply(2L), factory);
        //factory = GenLayerAddSnow.INSTANCE.<T>apply((IContextExtended)contextFactory.apply(2L), factory);
        factory = GenLayerAddIsland.INSTANCE.apply(contextFactory.apply(3L), factory);
        //factory = GenLayerEdge.CoolWarm.INSTANCE.apply(contextFactory.apply(2L), factory);
        //factory = GenLayerEdge.HeatIce.INSTANCE.apply(contextFactory.apply(2L), factory);
        //factory = GenLayerEdge.Special.INSTANCE.apply(contextFactory.apply(3L), factory);
        factory = GenLayerZoom.NORMAL.apply(contextFactory.apply(2002L), factory);
        factory = GenLayerZoom.NORMAL.apply(contextFactory.apply(2003L), factory);
        factory = GenLayerAddIsland.INSTANCE.apply(contextFactory.apply(4L), factory);
        return factory;
    }

    // superimpose hot and cold regions an a land and sea layer
    public static <T extends IArea, C extends IContextExtended<T>> IAreaFactory<T> createClimateFactory(LongFunction<C> contextFactory, BOPWorldSettings settings)
    {
        IAreaFactory<T> temperatureFactory;

        switch (settings.tempScheme)
        {
            case LATITUDE: default:
                temperatureFactory = GenLayerTemperatureLatitude.INSTANCE.apply(contextFactory.apply(2L));
                break;
            case SMALL_ZONES:
                temperatureFactory = GenLayerTemperatureNoise.SMALL_ZONES.apply(contextFactory.apply(3L));
                break;
            case MEDIUM_ZONES:
                temperatureFactory = GenLayerTemperatureNoise.MEDIUM_ZONES.apply(contextFactory.apply(4L));
                break;
            case LARGE_ZONES:
                temperatureFactory = GenLayerTemperatureNoise.LARGE_ZONES.apply(contextFactory.apply(5L));
                break;
            case RANDOM:
                temperatureFactory = GenLayerTemperatureRandom.INSTANCE.apply(contextFactory.apply(6L));
                break;
        }

        IAreaFactory<T> rainfallFactory;
        switch(settings.rainScheme)
        {
            case SMALL_ZONES:
                rainfallFactory = GenLayerRainfallNoise.SMALL_ZONES.apply(contextFactory.apply(7L));
                break;
            case MEDIUM_ZONES: default:
                rainfallFactory = GenLayerRainfallNoise.MEDIUM_ZONES.apply(contextFactory.apply(8L));
                break;
            case LARGE_ZONES:
                rainfallFactory = GenLayerRainfallNoise.LARGE_ZONES.apply(contextFactory.apply(9L));
                break;
            case RANDOM:
                rainfallFactory = GenLayerRainfallRandom.INSTANCE.apply(contextFactory.apply(10L));
                break;
        }

        return GenLayerClimate.INSTANCE.apply(contextFactory.apply(103L), temperatureFactory, rainfallFactory);
    }

    public static <T extends IArea, C extends IContextExtended<T>> IAreaFactory<T> createBiomeFactory(IAreaFactory<T> landSeaAreaFactory, IAreaFactory<T> climateAreaFactory, LongFunction<C> contextFactory)
    {
        IAreaFactory<T> biomeFactory = GenLayerBiomeBOP.INSTANCE.apply(contextFactory.apply(200L), landSeaAreaFactory, climateAreaFactory);
        biomeFactory = LayerUtil.repeat(1000L, GenLayerZoom.NORMAL, biomeFactory, 2, contextFactory);
        biomeFactory = GenLayerBiomeEdgeBOP.INSTANCE.apply(contextFactory.apply(1000L), biomeFactory);
        return biomeFactory;
    }

    public static <T extends IArea, C extends IContextExtended<T>> ImmutableList<IAreaFactory<T>> createAreaFactories(WorldType worldType, OverworldGenSettings settings, LongFunction<C> contextFactory)
    {
        // Create the initial land and sea layer. Is also responsible for adding deep oceans
        // and mushroom islands
        IAreaFactory<T> landSeaFactory = createInitialLandAndSeaFactory(contextFactory);

        // Determines positions for all of the new ocean subbiomes added in 1.13
        IAreaFactory<T> oceanBiomeFactory = OceanLayer.INSTANCE.apply(contextFactory.apply(2L));
        oceanBiomeFactory = LayerUtil.repeat(2001L, GenLayerZoom.NORMAL, oceanBiomeFactory, 6, contextFactory);

        int biomeSize = 4;
        int riverSize = biomeSize;
        if (settings != null) {
            biomeSize = settings.getBiomeSize();
            riverSize = settings.getRiverSize();
        }

        biomeSize = LayerUtil.getModdedBiomeSize(worldType, biomeSize);

        // Create the climates
        IAreaFactory<T> climateFactory = createClimateFactory(contextFactory, new BOPWorldSettings());

        // Add islands and deep oceans
        landSeaFactory = GenLayerAddMushroomIsland.INSTANCE.apply(contextFactory.apply(5L), landSeaFactory);
        landSeaFactory = GenLayerLargeIsland.INSTANCE.apply(contextFactory.apply(5L), landSeaFactory, climateFactory);
        landSeaFactory = GenLayerDeepOcean.INSTANCE.apply(contextFactory.apply(4L), landSeaFactory);

        // Allocate the biomes
        IAreaFactory<T> biomesFactory = createBiomeFactory(landSeaFactory, climateFactory, contextFactory);

        // Fork off a new branch as a seed for rivers and sub biomes
        IAreaFactory<T> riverAndSubBiomesInitFactory = GenLayerRiverInit.INSTANCE.apply(contextFactory.apply(100L), landSeaFactory);
        riverAndSubBiomesInitFactory = LayerUtil.repeat(1000L, GenLayerZoom.NORMAL, riverAndSubBiomesInitFactory, 2, contextFactory);
        biomesFactory = GenLayerSubBiome.INSTANCE.apply(contextFactory.apply(1000L), biomesFactory, riverAndSubBiomesInitFactory);

        // Develop the rivers branch
        IAreaFactory<T> riversInitFactory = LayerUtil.repeat(1000L, GenLayerZoom.NORMAL, riverAndSubBiomesInitFactory, riverSize, contextFactory);
        riversInitFactory = GenLayerRiver.INSTANCE.apply(contextFactory.apply(1L), riversInitFactory);
        riversInitFactory = GenLayerSmooth.INSTANCE.apply(contextFactory.apply(1000L), riversInitFactory);

        // Mix in rare biomes into biomes branch
        biomesFactory = GenLayerRareBiome.INSTANCE.apply(contextFactory.apply(1001L), biomesFactory);

        // Zoom more based on the biome size
        for (int i = 0; i < biomeSize; ++i)
        {
            biomesFactory = GenLayerZoom.NORMAL.apply(contextFactory.apply((long)(1000 + i)), biomesFactory);
            if (i == 0) biomesFactory = GenLayerAddIsland.INSTANCE.apply(contextFactory.apply(3L), biomesFactory);
            if (i == 1 || biomeSize == 1) biomesFactory = GenLayerShoreBOP.INSTANCE.apply(contextFactory.apply(1000L), biomesFactory);
        }

        biomesFactory = GenLayerSmooth.INSTANCE.apply(contextFactory.apply(1000L), biomesFactory);

        // Mix rivers into the biomes branch
        biomesFactory = GenLayerRiverMixBOP.INSTANCE.apply(contextFactory.apply(100L), biomesFactory, riversInitFactory);

        climateFactory = LayerUtil.repeat(2001L, GenLayerZoom.NORMAL, climateFactory, 6, contextFactory);
        biomesFactory = GenLayerMixOceansBOP.INSTANCE.apply(contextFactory.apply(100L), biomesFactory, oceanBiomeFactory, climateFactory);

        // Finish biomes with Voroni zoom
        IAreaFactory<T> voroniZoomBiomesFactory = GenLayerVoronoiZoom.INSTANCE.apply(contextFactory.apply(10L), biomesFactory);

        return ImmutableList.of(biomesFactory, voroniZoomBiomesFactory, biomesFactory);
    }

    public static GenLayer[] createGenLayers(long seed, WorldType worldType, OverworldGenSettings settings)
    {
        int[] layerCount = new int[1]; // Do this as an array to enable incrementing it in the lambda
        ImmutableList<IAreaFactory<LazyArea>> factoryList = createAreaFactories(worldType, settings, (seedModifier) ->
        {
            ++layerCount[0];
            return new LazyAreaLayerContextBOP(1, layerCount[0], seed, seedModifier);
        });
        GenLayer biomesLayer = new GenLayer(factoryList.get(0));
        GenLayer voroniZoomBiomesLayer = new GenLayer(factoryList.get(1));
        GenLayer biomesLayer2 = new GenLayer(factoryList.get(2));
        return new GenLayer[]{biomesLayer, voroniZoomBiomesLayer, biomesLayer2};
    }

    public static boolean isOcean(int biomeIn)
    {
        return biomeIn == WARM_OCEAN || biomeIn == LUKEWARM_OCEAN || biomeIn == OCEAN || biomeIn == COLD_OCEAN || biomeIn == FROZEN_OCEAN || biomeIn == DEEP_WARM_OCEAN || biomeIn == DEEP_LUKEWARM_OCEAN || biomeIn == DEEP_OCEAN || biomeIn == DEEP_COLD_OCEAN || biomeIn == DEEP_FROZEN_OCEAN;
    }

    public static boolean isShallowOcean(int biomeIn)
    {
        return biomeIn == WARM_OCEAN || biomeIn == LUKEWARM_OCEAN || biomeIn == OCEAN || biomeIn == COLD_OCEAN || biomeIn == FROZEN_OCEAN;
    }
}
