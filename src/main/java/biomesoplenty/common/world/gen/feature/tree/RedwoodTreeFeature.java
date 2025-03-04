/*******************************************************************************
 * Copyright 2014-2019, the Biomes O' Plenty Team
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/
package biomesoplenty.common.world.gen.feature.tree;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.common.util.biome.GeneratorUtil;
import biomesoplenty.common.util.block.IBlockPosQuery;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

import java.util.Random;
import java.util.Set;

public class RedwoodTreeFeature extends TreeFeatureBase
{
    public static class Builder extends BuilderBase<Builder, RedwoodTreeFeature>
    {
        protected int trunkWidth;

        public Builder trunkWidth(int a) {this.trunkWidth = a; return this;}

        public Builder()
        {
            this.minHeight = 10;
            this.maxHeight = 30;
            this.log = BOPBlocks.redwood_log.getDefaultState();
            this.leaves = BOPBlocks.redwood_leaves.getDefaultState();
            this.vine = Blocks.VINE.getDefaultState();
            this.trunkWidth = 1;
        }

        @Override
        public RedwoodTreeFeature create()
        {
            return new RedwoodTreeFeature(this.updateNeighbours, this.placeOn, this.replace, this.log, this.leaves, this.altLeaves, this.vine, this.hanging, this.trunkFruit, this.minHeight, this.maxHeight, this.trunkWidth);
        }

    }

    private int trunkWidth = 1;

    protected RedwoodTreeFeature(boolean notify, IBlockPosQuery placeOn, IBlockPosQuery replace, IBlockState log, IBlockState leaves, IBlockState altLeaves, IBlockState vine, IBlockState hanging, IBlockState trunkFruit, int minHeight, int maxHeight, int trunkWidth)
    {
        super(notify, placeOn, replace, log, leaves, altLeaves, vine, hanging, trunkFruit, minHeight, maxHeight);
        this.trunkWidth = trunkWidth;
    }

    public boolean checkSpace(IWorld world, BlockPos pos, int baseHeight, int height)
    {
        for (int y = 0; y <= height; y++)
        {

            int trunkWidth = (this.trunkWidth * (height - y) / height) + 1;
            int trunkStart = MathHelper.ceil(0.25D - trunkWidth / 2.0D);
            int trunkEnd = MathHelper.floor(0.25D + trunkWidth / 2.0D);

            // require 3x3 for the leaves, 1x1 for the trunk
            int start = (y <= baseHeight ? trunkStart : trunkStart - 1);
            int end = (y <= baseHeight ? trunkEnd : trunkEnd + 1);

            for (int x = start; x <= end; x++)
            {
                for (int z = start; z <= end; z++)
                {
                    BlockPos pos1 = pos.add(x, y, z);
                    // note, there may be a sapling on the first layer - make sure this.replace matches it!
                    if (pos1.getY() >= 255 || !this.replace.matches(world, pos1))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // generates a layer of leafs
    public void generateLeafLayer(IWorld world, Random rand, BlockPos pos, int leavesRadius, int trunkStart, int trunkEnd)
    {
        int start = trunkStart - leavesRadius;
        int end = trunkEnd + leavesRadius;

        for (int x = start; x <= end; x++)
        {
            for (int z = start; z <= end; z++)
            {
                // skip corners
                if ((leavesRadius > 0 ) && (x == start || x == end) && (z == start || z == end)) {continue;}
                int distFromTrunk = (x < 0 ? trunkStart - x : x - trunkEnd) + (z < 0 ? trunkStart - z : z - trunkEnd);

                // set leaves as long as it's not too far from the trunk to survive
                if (distFromTrunk < 4 || (distFromTrunk == 4 && rand.nextInt(2) == 0))
                {
                    this.setLeaves(world, pos.add(x, 0, z));
                }
            }
        }
    }

    public void generateBranch(Set<BlockPos> changedBlocks, IWorld world, Random rand, BlockPos pos, EnumFacing direction, int length)
    {
        EnumFacing.Axis axis = direction.getAxis();
        EnumFacing sideways = direction.rotateY();
        for (int i = 1; i <= length; i++)
        {
            BlockPos pos1 = pos.offset(direction, i);
            int r = (i == 1 || i == length) ? 1 : 2;
            for (int j = -r; j <= r; j++)
            {
                if (i < length || rand.nextInt(2) == 0)
                {
                    this.setLeaves(world, pos1.offset(sideways, j));
                }
            }
            if (length - i > 2)
            {
                this.setLeaves(world, pos1.up());
                this.setLeaves(world, pos1.up().offset(sideways, -1));
                this.setLeaves(world, pos1.up().offset(sideways, 1));
                this.setLog(changedBlocks, world, pos1, axis);
            }
        }
    }


    @Override
    protected boolean place(Set<BlockPos> changedBlocks, IWorld world, Random random, BlockPos startPos)
    {
        // Move down until we reach the ground
        while (startPos.getY() > 1 && world.isAirBlock(startPos) || world.getBlockState(startPos).getMaterial() == Material.LEAVES) {startPos = startPos.down();}

        for (int x = 0; x <= this.trunkWidth - 1; x++)
        {
            for (int z = 0; z <= this.trunkWidth - 1; z++)
            {
		        if (!this.placeOn.matches(world, startPos.add(x, 0, z)))
		        {
		            // Abandon if we can't place the tree on this block
		            return false;
		        }
            }
        }

        // Choose heights
        int height = GeneratorUtil.nextIntBetween(random, this.minHeight, this.maxHeight);
        int baseHeight = GeneratorUtil.nextIntBetween(random, (int)(height * 0.6F), (int)(height * 0.4F));
        int leavesHeight = height - baseHeight;
        if (leavesHeight < 3) {return false;}

        if (!this.checkSpace(world, startPos.up(), baseHeight, height))
        {
            // Abandon if there isn't enough room
            return false;
        }

        // Start at the top of the tree
        BlockPos pos = startPos.up(height);

        // Leaves at the top
        this.setLeaves(world, pos);
        pos.down();

        // Add layers of leaves
        for (int i = 0; i < leavesHeight; i++)
        {

            int trunkWidth = (this.trunkWidth * i / height) + 1;
            int trunkStart = MathHelper.ceil(0.25D - trunkWidth / 2.0D);
            int trunkEnd = MathHelper.floor(0.25D + trunkWidth / 2.0D);


            int radius = Math.min(Math.min((i + 2) / 4, 2 + (leavesHeight - i)), 4);
            if (radius == 0)
            {
                this.setLeaves(world, pos);
            }
            else if (radius < 2)
            {
                this.generateLeafLayer(world, random, pos, radius, trunkStart, trunkEnd);
            }
            else
            {
	            this.generateBranch(changedBlocks, world, random, pos.add(trunkStart, 0, trunkStart), EnumFacing.NORTH, radius);
	            this.generateBranch(changedBlocks, world, random, pos.add(trunkEnd, 0, trunkStart), EnumFacing.EAST, radius);
	            this.generateBranch(changedBlocks, world, random, pos.add(trunkEnd, 0, trunkEnd), EnumFacing.SOUTH, radius);
	            this.generateBranch(changedBlocks, world, random, pos.add(trunkStart, 0, trunkEnd), EnumFacing.WEST, radius);
            }
            pos = pos.down();
        }

        // Generate the trunk
        for (int y = 0; y < height - 1; y++)
        {
            int trunkWidth = (this.trunkWidth * (height - y) / height) + 1;
            int trunkStart = MathHelper.ceil(0.25D - trunkWidth / 2.0D);
            int trunkEnd = MathHelper.floor(0.25D + trunkWidth / 2.0D);

            if (trunkWidth < 1 || trunkWidth > this.trunkWidth)
            {
                trunkStart = 0;
                trunkEnd = 0;
            }

            for (int x = trunkStart; x <= trunkEnd; x++)
            {
                for (int z = trunkStart; z <= trunkEnd; z++)
                {
                    this.setLog(changedBlocks, world, startPos.add(x, y, z));
                }
            }
        }

        return true;
    }
}
