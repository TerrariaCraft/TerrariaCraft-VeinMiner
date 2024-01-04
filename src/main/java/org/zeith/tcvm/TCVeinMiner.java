package org.zeith.tcvm;

import com.zeitheron.hammercore.internal.variables.VariableManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.*;
import org.zeith.tcvm.cfg.VeinMiningConfigs;
import org.zeith.tcvm.proxy.CommonProxy;
import org.zeith.terraria.api.events.*;
import org.zeith.terraria.api.mod.ITerrariaMod;
import org.zeith.terraria.common.data.player.*;
import org.zeith.terraria.common.data.world.harvest.BlockHarvestManager;
import org.zeith.terraria.utils.ScheduledProcess;
import org.zeith.terraria.utils.forge.DeferredRegistries;

import java.util.*;

@Mod(
		modid = TCVeinMiner.MOD_ID,
		name = TCVeinMiner.MOD_NAME,
		version = TCVeinMiner.MOD_VERSION,
		dependencies = "required-after:terraria",
		guiFactory = "org.zeith.tcvm.client.ClientGuiFactory",
		updateJSON = "https://api.modrinth.com/updates/SPvYoM1y/forge_updates.json",
		certificateFingerprint = "9f5e2a811a8332a842b34f6967b7db0ac4f24856"
)
public class TCVeinMiner
		implements ITerrariaMod
{
	public static final String MOD_ID = "tcvm";
	public static final String MOD_NAME = "TerrariaCraft VeinMiner";
	public static final String MOD_VERSION = "@VERSION@";
	public static final Logger LOG = LogManager.getLogger();
	
	public final DeferredRegistries registries = new DeferredRegistries(this);
	
	@SidedProxy(serverSide = "org.zeith.tcvm.proxy.CommonProxy", clientSide = "org.zeith.tcvm.proxy.ClientProxy")
	public static CommonProxy proxy;
	
	public static final KeyMapTC.KeyButtonTC VEIN_MINE = new KeyMapTC.KeyButtonTC(new ResourceLocation(MOD_ID, "vein_mine"), (data, state) ->
	{
	});
	
	public TCVeinMiner()
	{
		// Used to add custom recipes
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@EventHandler
	public void construction(FMLConstructionEvent event)
	{
		// This is very recommended for common addon setup.
		ITerrariaMod.super.constructionEvent(event);
		VariableManager.registerVariable(VeinMiningConfigs.CONFIG);
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.setup();
	}
	
	@EventHandler
	public void preInit(FMLPostInitializationEvent event)
	{
		BlockWhitelist.setupDefaults();
	}
	
	@Override
	public DeferredRegistries getRegistries()
	{
		return registries;
	}
	
	@SubscribeEvent
	public void breakBlock(TerrariaBlockBreakEvent e)
	{
		if(!(e.getEntityPlayer() instanceof EntityPlayerMP)) return;
		EntityPlayerMP player = (EntityPlayerMP) e.getEntityPlayer();
		if(BlockWhitelist.isVeinMinable(e.getState()))
		{
			PlayerDataTC pd = PlayerDataTC.get(player);
			if(pd.keyboard.isPressed(VEIN_MINE)
			   && (!player.getEntityData().getBoolean("TCVM_VeinMineBusy")
				   || player.world.getTotalWorldTime() - player.getEntityData().getLong("TCVM_LastVeinMine") > VeinMiningConfigs.DIG_DELAY * 2))
			{
				World world = e.getWorld();
				BlockPos origin = e.getPos();
				IBlockState match = e.getState();
				
				player.getEntityData().setBoolean("TCVM_VeinMineBusy", true);
				LOG.info("Start vein mining for {}!", player);
				
				int maxDelay = 0;
				List<BlockPos> positions = new ArrayList<>();
				positions.add(e.getPos());
				for(int i = 0; i < positions.size(); i++)
				{
					BlockPos cur = positions.get(i);
					for(BlockPos off : VeinMiningConfigs.allRelatives(cur))
					{
						if(!positions.contains(off) && world.getBlockState(off).equals(match))
						{
							positions.add(off);
							
							if(!origin.equals(off))
							{
								int ticks = 1 + Math.round((float) (VeinMiningConfigs.DIG_DELAY * Math.sqrt(origin.distanceSq(off))));
								
								maxDelay = Math.max(maxDelay, ticks);
								ScheduledProcess.schedule(ticks, () ->
								{
									if(world.getBlockState(off).equals(match))
									{
										BlockHarvestManager.harvestBlockByPlayer(player, world, off, true);
										player.getEntityData().setLong("TCVM_LastVeinMine", player.world.getTotalWorldTime());
									}
								});
							}
						}
					}
					if(positions.size() > VeinMiningConfigs.MAX_VEIN_SIZE)
					{
						break;
					}
				}
				
				ScheduledProcess.schedule(1 + maxDelay, () ->
				{
					player.getEntityData().setBoolean("TCVM_VeinMineBusy", false);
					LOG.info("Stop vein mining for {}!", player);
				});
			}
		}
	}
}
