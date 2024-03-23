package org.zeith.tcvm.init;

import net.minecraft.block.Block;
import org.zeith.tcvm.TCVeinMiner;
import org.zeith.terraria.api.tags.*;
import org.zeith.terraria.init.TagsTC;

import java.util.Arrays;

public class TagsTCVM
{
	public static void init()
	{
		Blocks.init();
	}
	
	public interface Blocks
	{
		TagKey<Block> VEIN_MINE_BLOCKLIST = tag("vm_blocklist");
		TagKey<Block> VEIN_MINE_ALLOWLIST = tag("vm_allowlist");
		TagKey<Block> FALLING_BLOCKS = tag("falling_blocks");
		
		static void init()
		{
			StaticallyLinkedTagRegistry.include(VEIN_MINE_ALLOWLIST, Arrays.asList(TagsTC.Blocks.ORES));
		}
		
		static TagKey<Block> tag(String key)
		{
			return TagTypeRegistry.BLOCKS.key(TCVeinMiner.id(key));
		}
	}
}
