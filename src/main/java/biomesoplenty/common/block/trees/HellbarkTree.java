package biomesoplenty.common.block.trees;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.trees.AbstractTree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class HellbarkTree extends AbstractTree
{
   @Nullable
   @Override
   protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random)
   {
      return null;
   }
}