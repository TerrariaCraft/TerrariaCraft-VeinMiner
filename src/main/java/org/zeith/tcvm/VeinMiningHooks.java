package org.zeith.tcvm;

import com.zeitheron.hammercore.utils.base.Cast;
import lombok.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.zeith.tcvm.cfg.VeinMiningConfigs;
import org.zeith.terraria.api.blocks.IHarvestPrevention;
import org.zeith.terraria.api.events.TerrariaBlockBreakEvent;
import org.zeith.terraria.api.items.functional.ITool;
import org.zeith.terraria.common.data.player.PlayerDataTC;
import org.zeith.terraria.common.data.world.harvest.BlockHarvestManager;
import org.zeith.terraria.utils.ScheduledProcess;

import java.util.*;
import java.util.stream.*;

@Mod.EventBusSubscriber
public class VeinMiningHooks
{
	public static List<MineablePos> gatherAllPositions(World world, BlockPos originPos, IBlockState match)
	{
		boolean fallingBlocks = BlockWhitelist.sortPositionsVertically(world, match);
		
		val origin = originPos.toImmutable();
		List<BlockPos> positions = new ArrayList<>();
		positions.add(origin);
		for(int i = 0; i < positions.size(); i++)
		{
			BlockPos cur = positions.get(i);
			for(BlockPos off : VeinMiningConfigs.allRelatives(cur))
				if(!positions.contains(off) && world.getBlockState(off).equals(match))
					positions.add(off);
			if(positions.size() > VeinMiningConfigs.MAX_VEIN_SIZE)
				break;
		}
		
		if(fallingBlocks)
		{
			Map<BlockColumn, SortedColumnData> columns = new HashMap<>();
			
			for(int i = 1; i < positions.size(); i++)
			{
				val off = positions.get(i);
				columns.computeIfAbsent(new BlockColumn(off), SortedColumnData::new).add(off);
			}
			
			val columnsOrdered = columns.values()
					.stream()
					.sorted(Comparator.comparingDouble(bc -> bc.column.distanceSq(origin)))
					.collect(Collectors.toList());
			
			List<MineablePos> lst = new ArrayList<>();
			
			float tileDelay = 0;
			for(val col : columnsOrdered)
			{
				for(val pos : col.positions)
				{
					int ticks = Math.round(tileDelay + (col.maxY - pos.getY() + 1) * VeinMiningConfigs.DIG_DELAY);
					lst.add(new MineablePos(pos, ticks));
				}
				tileDelay += VeinMiningConfigs.DIG_DELAY * col.positions.size();
			}
			
			return lst;
		}
		
		return positions.stream()
				.skip(1L)
				.map(off -> new MineablePos(off, 1 + Math.round((float) (VeinMiningConfigs.DIG_DELAY * Math.sqrt(origin.distanceSq(off))))))
				.collect(Collectors.toList());
	}
	
	@SubscribeEvent
	public static void breakBlock(TerrariaBlockBreakEvent e)
	{
		if(!(e.getEntityPlayer() instanceof EntityPlayerMP)) return;
		EntityPlayerMP player = (EntityPlayerMP) e.getEntityPlayer();
		if(BlockWhitelist.isVeinMinable(player.world, e.getState()))
		{
			PlayerDataTC pd = PlayerDataTC.get(player);
			if(pd.keyboard.isPressed(TCVeinMiner.VEIN_MINE)
			   && (!player.getEntityData().getBoolean("TCVM_VeinMineBusy")
				   || player.world.getTotalWorldTime() - player.getEntityData().getLong("TCVM_LastVeinMine") > VeinMiningConfigs.DIG_DELAY * 2))
			{
				World world = e.getWorld();
				BlockPos origin = e.getPos();
				IBlockState match = e.getState();
				
				player.getEntityData().setBoolean("TCVM_VeinMineBusy", true);
				TCVeinMiner.LOG.info("Start vein mining for {}!", player);
				
				int maxDelay = 0;
				
				val positions = gatherAllPositions(world, origin, match);
				for(MineablePos position : positions)
				{
					val off = position.getPos();
					int ticks = position.getDelay();
					
					maxDelay = Math.max(maxDelay, ticks);
					ScheduledProcess.schedule(ticks, () ->
					{
						if(world.getBlockState(off).equals(match))
						{
							if(IHarvestPrevention.canBeHarvested(world, off))
							{
								BlockHarvestManager.harvestBlockByPlayer(player, world, off, true);
							}
							player.getEntityData().setLong("TCVM_LastVeinMine", player.world.getTotalWorldTime());
						}
					});
				}
				
				ScheduledProcess.schedule(1 + maxDelay, () ->
				{
					player.getEntityData().setBoolean("TCVM_VeinMineBusy", false);
					TCVeinMiner.LOG.info("Stop vein mining for {}!", player);
				});
				
				int cooldown = Cast.optionally(pd.getSelectedItem().getItem(), ITool.class).map(ITool::getUseTime).orElse(0);
				int md = (maxDelay + 1) * 3;
				if(cooldown < md) pd.startCustomSwingAndCooldown((maxDelay + 1) * 3 + 30);
			}
		}
	}
	
	@Data
	public static class MineablePos
	{
		public final BlockPos pos;
		public final int delay;
	}
	
	@Data
	public static class BlockColumn
	{
		public final int x;
		public final int z;
		
		public BlockColumn(BlockPos pos)
		{
			this(pos.getX(), pos.getZ());
		}
		
		public BlockColumn(int x, int z)
		{
			this.x = x;
			this.z = z;
		}
		
		public double distanceSq(BlockPos pos)
		{
			return pos.distanceSq(x, pos.getY(), z);
		}
	}
	
	public static class SortedColumnData
	{
		public final BlockColumn column;
		public final List<BlockPos> positions = new ArrayList<>();
		public int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		
		public SortedColumnData(BlockColumn column)
		{
			this.column = column;
		}
		
		public void add(BlockPos pos)
		{
			minY = Math.min(minY, pos.getY());
			maxY = Math.max(maxY, pos.getY());
			positions.add(pos.toImmutable());
		}
	}
}