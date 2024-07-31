package org.zeith.tcvm.compat;

import lombok.SneakyThrows;
import net.minecraft.item.ItemStack;
import org.zeith.terraria.api.items.functional.ITool;
import org.zeith.terraria.common.data.player.PlayerDataTC;

import java.lang.reflect.Method;

public class IToolCompat
{
	private static final Method METHOD;
	
	static
	{
		Method newTCMethod = null, oldTCMethod = null;
		for(Method method : ITool.class.getDeclaredMethods())
		{
			if("getUseTime".equals(method.getName()))
			{
				if(method.getParameterCount() == 0)
				{
					oldTCMethod = method;
					break;
				} else if(method.getParameterCount() == 2)
				{
					newTCMethod = method;
					break;
				}
			}
		}
		if(newTCMethod != null) METHOD = newTCMethod;
		else METHOD = oldTCMethod;
	}
	
	@SneakyThrows
	public static int getUseTime(ITool tool, PlayerDataTC player, ItemStack stack)
	{
		if(METHOD.getParameterCount() == 0)
			return (int) METHOD.invoke(tool);
		return (int) METHOD.invoke(tool, player, stack);
	}
}