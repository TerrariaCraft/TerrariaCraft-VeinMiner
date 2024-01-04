package org.zeith.tcvm;

import com.zeitheron.hammercore.utils.base.SideLocal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.zeith.terraria.common.content.blocks.simple.*;

import java.util.*;

public class BlockWhitelist
{
	public static final SideLocal<Set<Block>> CFG_WHITELISTED_BLOCKS = SideLocal.initializeForBoth(Collections::emptySet);
	public static final SideLocal<Set<Block>> CFG_BLOCKLISTED_BLOCKS = SideLocal.initializeForBoth(Collections::emptySet);
	
	public static final Set<Block> WHITELISTED_BLOCKS = new HashSet<>();
	public static final Set<IBlockState> WHITELISTED_STATES = new HashSet<>();
	
	public static void setupDefaults()
	{
		for(Block block : ForgeRegistries.BLOCKS)
		{
			if(block instanceof BlockOre)
				WHITELISTED_BLOCKS.add(block);
			if(block instanceof BlockSilt)
				WHITELISTED_BLOCKS.add(block);
			if(block instanceof BlockSlush)
				WHITELISTED_BLOCKS.add(block);
			if(block instanceof BlockGemOre)
				WHITELISTED_BLOCKS.add(block);
		}
	}
	
	public static boolean isVeinMinable(IBlockState state)
	{
		Block b = state.getBlock();
		if(CFG_BLOCKLISTED_BLOCKS.get().contains(b))
			return false;
		return WHITELISTED_STATES.contains(state)
			   || WHITELISTED_BLOCKS.contains(b)
			   || CFG_WHITELISTED_BLOCKS.get().contains(b);
	}
}