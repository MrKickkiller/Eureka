package eureka.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


import eureka.Eureka;
import eureka.network.MessageEngineeringDiary;
import eureka.network.PacketHandler;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Eureka is distributed under the terms of LGPLv3
 * Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public class ItemEngineeringDiary extends ItemBook {
	public static IIcon icon;

	public ItemEngineeringDiary() {
		super();
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int damage) {
		return icon;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			PacketHandler.instance.sendToAllAround(new MessageEngineeringDiary(player), new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 1));
			player.openGui(Eureka.instance, 1, world, (int) player.posX, (int) player.posY, (int) player.posZ);
		}
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister register) {
		icon = register.registerIcon("eureka:engineeringDiary");
	}
}
