/*******************************************************************************
 * Copyright 2014-2019, the Biomes O' Plenty Team
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/
package biomesoplenty.common.world.gen.feature;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;

public class BogSurfaceBuilder implements ISurfaceBuilder<SurfaceBuilderConfig>
{
   @Override
   public void buildSurface(Random random, IChunk chunkIn, Biome biomeIn, int x, int z, int startHeight, double noise, IBlockState defaultBlock, IBlockState defaultFluid, int seaLevel, long seed, SurfaceBuilderConfig config)
   {
      double d0 = Biome.INFO_NOISE.getValue((double)x * 0.25D, (double)z * 0.25D);
      if (d0 > 0.1D)
      {
         int i = x & 15;
         int j = z & 15;
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         BlockPos.MutableBlockPos blockposdown$mutableblockpos = new BlockPos.MutableBlockPos();
         BlockPos.MutableBlockPos blockposup$mutableblockpos = new BlockPos.MutableBlockPos();

         for(int k = startHeight; k >= 0; --k)
         {
            blockpos$mutableblockpos.setPos(i, k, j);
            if (!chunkIn.getBlockState(blockpos$mutableblockpos).isAir())
            {
               if (k == 62)
               {
	               if (chunkIn.getBlockState(blockpos$mutableblockpos).getBlock() != defaultFluid.getBlock())
	               {
	                  chunkIn.setBlockState(blockpos$mutableblockpos, defaultFluid, false);
	               }
	               else
	               {
	            	   blockposup$mutableblockpos.setPos(i, k+1, j);
	            	   blockposdown$mutableblockpos.setPos(i, k-1, j);
	            	   if (chunkIn.getBlockState(blockposdown$mutableblockpos).getBlock() != defaultFluid.getBlock())
	            	   {
	            		   chunkIn.setBlockState(blockpos$mutableblockpos, Blocks.GRASS_BLOCK.getDefaultState(), false);
	            	   }
	               }
               }
               break;
            }
         }
      }

      Biome.DEFAULT_SURFACE_BUILDER.buildSurface(random, chunkIn, biomeIn, x, z, startHeight, noise, defaultBlock, defaultFluid, seaLevel, seed, config);
   }
}