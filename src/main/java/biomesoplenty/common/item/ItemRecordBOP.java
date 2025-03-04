/*******************************************************************************
 * Copyright 2014-2019, the Biomes O' Plenty Team
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/
package biomesoplenty.common.item;

import biomesoplenty.common.util.inventory.ItemGroupBOP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;

public class ItemRecordBOP extends ItemRecord
{
    public ItemRecordBOP(SoundEvent record)
    {
        super(0, record, new Item.Properties().group(ItemGroupBOP.instance));
    }
}
