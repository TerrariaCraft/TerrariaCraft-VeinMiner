package org.zeith.tcvm.cfg;

import com.google.common.collect.AbstractIterator;
import com.zeitheron.hammercore.cfg.*;
import com.zeitheron.hammercore.cfg.fields.*;
import com.zeitheron.hammercore.internal.variables.types.VariableCompoundNBT;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.Sys;
import org.zeith.tcvm.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

@HCModConfigurations(modid = TCVeinMiner.MOD_ID)
public class VeinMiningConfigs
		implements IConfigReloadListener
{
	private static String[] allowedBlocks, deniedBlocks;
	
	@ModConfigPropertyInt(name = "Max Vein Size", category = "Tweaks", defaultValue = 256, min = 0, max = 2048, comment = "How many blocks may be mined by one vein mining operation?")
	public static int MAX_VEIN_SIZE = 256;
	
	@ModConfigPropertyFloat(name = "Dig Delay", category = "Tweaks", defaultValue = 2, min = 0, max = 100, comment = "How many ticks should pass for every block distance?")
	public static float DIG_DELAY = 2;
	
	@ModConfigPropertyBool(name = "Diagonal", category = "Tweaks", defaultValue = false, comment = "Should vein mining look diagonally? (27 blocks around center vs 6)")
	public static boolean DIAGONAL = false;
	
	private static long lastRefresh = -1L;
	
	public static Iterable<BlockPos> allRelatives(BlockPos pos)
	{
		return DIAGONAL
			   ? BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, 1, 1))
			   : () -> new AbstractIterator<BlockPos>()
			   {
				   final Iterator<EnumFacing> facings = Arrays.stream(EnumFacing.VALUES).iterator();
				   
				   @Override
				   protected BlockPos computeNext()
				   {
					   if(facings.hasNext())
						   return pos.offset(facings.next());
					   return endOfData();
				   }
			   };
	}
	
	public static final VariableCompoundNBT CONFIG = new VariableCompoundNBT(new ResourceLocation(TCVeinMiner.MOD_ID, "config"))
	{
		long localLastRefresh = -1L;
		
		@Override
		public void readFromNBT(NBTTagCompound nbt)
		{
			BlockWhitelist.CFG_WHITELISTED_BLOCKS.set(fromNbt(nbt.getTagList("AllowedBlocks", Constants.NBT.TAG_STRING)));
			BlockWhitelist.CFG_BLOCKLISTED_BLOCKS.set(fromNbt(nbt.getTagList("DeniedBlocks", Constants.NBT.TAG_STRING)));
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt)
		{
			nbt = new NBTTagCompound();
			nbt.setTag("AllowedBlocks", toNbt(allowedBlocks));
			nbt.setTag("DeniedBlocks", toNbt(deniedBlocks));
			return nbt;
		}
		
		@Override
		public boolean hasChanged()
		{
			return localLastRefresh != lastRefresh;
		}
		
		@Override
		public void setNotChanged()
		{
			localLastRefresh = lastRefresh;
		}
	};
	
	public static Configuration cfg;
	
	@Override
	public void reloadCustom(Configuration cfgs)
	{
		cfg = cfgs;
		
		allowedBlocks = cfgs.getStringList("Block-Allowlist", "Blocks", new String[0], "What blocks should be allowed to be vein-mined?");
		deniedBlocks = cfgs.getStringList("Block-Denylist", "Blocks", new String[0], "What blocks should be denied to be vein-mined?");
		
		BlockWhitelist.CFG_WHITELISTED_BLOCKS.set(Side.SERVER, fromNbt(toNbt(allowedBlocks)));
		BlockWhitelist.CFG_BLOCKLISTED_BLOCKS.set(Side.SERVER, fromNbt(toNbt(deniedBlocks)));
		
		lastRefresh = System.currentTimeMillis();
	}
	
	private static Set<Block> fromNbt(NBTTagList lst)
	{
		return IntStream.range(0, lst.tagCount())
				.mapToObj(lst::getStringTagAt)
				.map(ResourceLocation::new)
				.map(ForgeRegistries.BLOCKS::getValue)
				.filter(b -> b != null && b != Blocks.AIR)
				.collect(Collectors.toSet());
	}
	
	private static NBTTagList toNbt(String[] tags)
	{
		NBTTagList lst = new NBTTagList();
		for(String tag : tags)
			lst.appendTag(new NBTTagString(tag));
		return lst;
	}
}