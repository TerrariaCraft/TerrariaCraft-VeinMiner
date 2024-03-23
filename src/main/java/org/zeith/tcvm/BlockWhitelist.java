package org.zeith.tcvm;

import com.zeitheron.hammercore.utils.base.SideLocal;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.zeith.tcvm.init.TagsTCVM;
import org.zeith.terraria.api.tags.*;
import org.zeith.terraria.common.content.blocks.base.BlockFallingTC;
import org.zeith.terraria.common.content.blocks.simple.*;

import java.util.*;

import static org.zeith.tcvm.init.TagsTCVM.Blocks.*;

public class BlockWhitelist
{
	public static final SideLocal<Set<Block>> CFG_WHITELISTED_BLOCKS = SideLocal.initializeForBoth(Collections::emptySet);
	public static final SideLocal<Set<Block>> CFG_BLOCKLISTED_BLOCKS = SideLocal.initializeForBoth(Collections::emptySet);
	
	public static void setupDefaults()
	{
		for(Block block : ForgeRegistries.BLOCKS)
		{
			if(block instanceof BlockSilt || block instanceof BlockSlush)
				StaticallyLinkedTagRegistry.bind(VEIN_MINE_ALLOWLIST, block);
			if(block instanceof BlockFallingTC || block instanceof BlockFalling)
				StaticallyLinkedTagRegistry.bind(FALLING_BLOCKS, block);
		}
	}
	
	public static boolean sortPositionsVertically(World world, IBlockState state)
	{
		return TagHelper.test(FALLING_BLOCKS, world, state);
	}
	
	public static boolean isVeinMinable(World world, IBlockState state)
	{
		Block b = state.getBlock();
		
		if(TagHelper.test(VEIN_MINE_BLOCKLIST, world, b)
		   || CFG_BLOCKLISTED_BLOCKS.get().contains(b))
			return false;
		
		return TagHelper.test(VEIN_MINE_ALLOWLIST, world, b)
			   || CFG_WHITELISTED_BLOCKS.get().contains(b);
	}
}