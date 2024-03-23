package org.zeith.tcvm.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.zeith.tcvm.*;
import org.zeith.terraria.common.data.world.harvest.BlockHarvestManager;

import static org.zeith.tcvm.TCVeinMiner.MOD_ID;

public class ClientProxy
		extends CommonProxy
{
	public static final KeyBinding VEIN_MINE = new KeyBinding("key." + MOD_ID + ".vein_mine", KeyConflictContext.IN_GAME, Keyboard.KEY_GRAVE, "key.category." + MOD_ID);
	
	@Override
	public void setup()
	{
		ClientRegistry.registerKeyBinding(VEIN_MINE);
		TCVeinMiner.VEIN_MINE.bind(VEIN_MINE);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void renderHud(RenderGameOverlayEvent.Post e)
	{
		if(e.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS
		   && VEIN_MINE.isKeyDown())
		{
			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution sr = new ScaledResolution(mc);
			int sw = sr.getScaledWidth();
			int sh = sr.getScaledHeight();
			
			if(mc.objectMouseOver != null
			   && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK
			   && mc.objectMouseOver.getBlockPos() != null
			   && BlockHarvestManager.getCLProgress(mc.objectMouseOver.getBlockPos()) > 0
			   && mc.gameSettings.keyBindAttack.isKeyDown()
			   && mc.world != null
			   && BlockWhitelist.isVeinMinable(mc.world, mc.world.getBlockState(mc.objectMouseOver.getBlockPos())))
			{
				String txt = I18n.format("info." + MOD_ID + ".vein_mining");
				
				FontRenderer font = mc.fontRenderer;
				int w = font.getStringWidth(txt);
				font.drawString(txt, (sw - w) / 2, sh / 2 + font.FONT_HEIGHT, 0xFFFFFF, false);
			}
			
		}
	}
}