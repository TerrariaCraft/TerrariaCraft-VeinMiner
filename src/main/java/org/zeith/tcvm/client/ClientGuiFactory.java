package org.zeith.tcvm.client;

import com.zeitheron.hammercore.cfg.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.zeith.tcvm.TCVeinMiner;
import org.zeith.tcvm.cfg.VeinMiningConfigs;

import java.util.Set;

public class ClientGuiFactory
		extends GuiConfigFactory
{
	@Override
	public void initialize(Minecraft minecraftInstance)
	{
		TCVeinMiner.LOG.info("Created {} Gui Config Factory!", TCVeinMiner.MOD_NAME);
	}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
	{
		return null;
	}
	
	@Override
	public boolean hasConfigGui()
	{
		return true;
	}
	
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen)
	{
		return new HCConfigGui(parentScreen, VeinMiningConfigs.cfg, TCVeinMiner.MOD_ID);
	}
}