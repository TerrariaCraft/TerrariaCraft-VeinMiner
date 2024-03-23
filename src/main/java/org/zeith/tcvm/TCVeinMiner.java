package org.zeith.tcvm;

import com.zeitheron.hammercore.internal.variables.VariableManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.*;
import org.zeith.tcvm.cfg.VeinMiningConfigs;
import org.zeith.tcvm.init.TagsTCVM;
import org.zeith.tcvm.proxy.CommonProxy;
import org.zeith.terraria.api.mod.ITerrariaMod;
import org.zeith.terraria.common.data.player.KeyMapTC;

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
	
	@SidedProxy(serverSide = "org.zeith.tcvm.proxy.CommonProxy", clientSide = "org.zeith.tcvm.proxy.ClientProxy")
	public static CommonProxy proxy;
	
	public static final KeyMapTC.KeyButtonTC VEIN_MINE = new KeyMapTC.KeyButtonTC(new ResourceLocation(MOD_ID, "vein_mine"), (data, state) ->
	{
	});
	
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
		TagsTCVM.init();
	}
	
	@EventHandler
	public void preInit(FMLPostInitializationEvent event)
	{
		BlockWhitelist.setupDefaults();
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
}
