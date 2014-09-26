package eureka.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameData;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eureka.api.EurekaInfo;
import eureka.api.EurekaRegistry;
import eureka.core.Logger;
/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Eureka is distributed under the terms of GNU GPL v3.0
 * Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public class FileReader {
	public static File mainfolder, catagoryFolder, keyFolder;
	protected static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static void setMainfolder (File folder){
		mainfolder = folder;
	}

	public static void readFiles() {
		try {
			createFolderIfNeeded(mainfolder);
			catagoryFolder = new File(mainfolder, "Categories");
			keyFolder = new File(mainfolder, "Keys");
			createFolderIfNeeded(catagoryFolder);
			createFolderIfNeeded(keyFolder);
			eurekaCategory();


			for (File file : catagoryFolder.listFiles(new FileFilter()))
				readCategory(file);

			//ChapterEntry entry = new ChapterEntry("basicDuster", "BCA", "buildcraftadditions", "basicDusterBlock", "block", 30, new String[]{"test"}, new String[]{"test2"}, new String[]{"test3", "test4"}, new int[]{2}, "buildcraftadditions", "basicDusterBlock", "block", "crafting", "ds", "testing", new String[]{});
			//File test = new File(keyFolder, "Duster.json");
			//Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(test)));
			//writer.write(gson.toJson(entry));
			//writer.close();

			for (File file: keyFolder.listFiles(new FileFilter())){
				readKey(file);
			}
		} catch (Throwable e) {
			Logger.error("Something went wrong while reading the Eureka JSON files, please report this including following stacktrace: ");
			e.printStackTrace();
		}
	}

	public static void createFolderIfNeeded(File folder) {
		try {
			if (!Files.exists(folder.toPath()))
				Files.createDirectory(folder.toPath());
		} catch (Throwable e) {
			Logger.error("Something went wrong while checking or creating the Eureka folders");
			e.printStackTrace();
		}
	}

	public static void eurekaCategory(){
		try {
			File file = new File(catagoryFolder, "Eureka.json");
			if (!Files.exists(file.toPath())){
				CategoryEntry category = new CategoryEntry("Eureka", "eureka", "engineeringDiary", "item");
				Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
				writer.write(gson.toJson(category));
				writer.close();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static void readCategory(File file){
		try {
			CategoryEntry category = gson.fromJson(new java.io.FileReader(file), CategoryEntry.class);
			if (category.displaystackType == null || category.displaystackName == null || category.displaystackModID == null || category.name == null) {
				Logger.error("Error while reading catagory file " + file.toString());
				return;
			}
			ItemStack stack = null;
			if (category.displaystackType.toLowerCase().equals("block"))
				stack = new ItemStack(getBlockFromRegistry(category.displaystackModID, category.displaystackName));
			else if (category.displaystackType.toLowerCase().equals("item"))
				stack = new ItemStack(getItemFromRegistry(category.displaystackModID, category.displaystackName));
			if (stack == null) {
				Logger.error("Unable to get the displaystack from the GameRegistry for category" + category.name + "check type, modid and the name used to register it");
			}
			EurekaRegistry.registerCategory(category.name, stack);
		} catch (Throwable e){
			e.printStackTrace();
		}
	}

	private static void readKey(File file){
		try {
			ChapterEntry chapter = gson.fromJson(new java.io.FileReader(file), ChapterEntry.class);
			if (chapter.name == null ||chapter.category == null || chapter.displaystackModID == null || chapter.displaystackType == null || chapter.displaystackName == null || chapter.dropsAmount == null || chapter.dropsModIDs == null || chapter.dropsStackName == null || chapter.dropsStackType == null || chapter.linkedObjectModID == null || chapter.linkedObjectStackName == null || chapter.linkedObjectStackType == null || chapter.progressType == null) {
				Logger.error("Error while reading key file " + file.toString() + " : unable to load all required variables");
				return;
			}
			ItemStack displaystack = null;
			if (chapter.displaystackType.toLowerCase().equals("block"))
				displaystack = new ItemStack(getBlockFromRegistry(chapter.displaystackModID, chapter.displaystackName));
			else if (chapter.displaystackType.toLowerCase().equals("item"))
				displaystack = new ItemStack(getItemFromRegistry(chapter.displaystackModID, chapter.displaystackName));
			if (displaystack == null){
				Logger.error("Error while reading key file" + file.toString() + ": error while obtaining display stack, please check type, modid and name");
				return;
			}
			if (chapter.dropsStackType.length + chapter.dropsAmount.length + chapter.dropsModIDs.length + chapter.dropsStackName.length != (chapter.dropsStackType.length * 4)){
				Logger.error("Error while reading key file" + file.toString() +  ": drop array sizes don't match");
				return;
			}
			ItemStack[] drops = new ItemStack[chapter.dropsStackType.length];
			for (int teller = 0; teller < chapter.dropsStackType.length; teller++){
				ItemStack tempstack = null;
				if (chapter.dropsStackType[teller].toLowerCase().equals("block"))
					tempstack = new ItemStack(getBlockFromRegistry(chapter.dropsModIDs[teller], chapter.dropsStackName[teller]));
				else if (chapter.dropsStackType[teller].toLowerCase().equals("item"))
					tempstack = new ItemStack(getItemFromRegistry(chapter.dropsModIDs[teller], chapter.dropsStackName[teller]));
				if (tempstack == null){
					Logger.error("Error while reading key file" + file.toString() +  ": Unable to assemble itemstack " + teller);
					continue;
				}
				tempstack.stackSize = chapter.dropsAmount[teller];
				drops[teller] = tempstack.copy();
			}
			EurekaRegistry.register(new EurekaInfo(chapter.name, chapter.category, chapter.maxProgress, displaystack, chapter.requiredResearch));
			EurekaRegistry.registerDrops(chapter.name, drops);

			if (chapter.linkedObjectStackType.toLowerCase().equals("block")){
				Block tempblock = getBlockFromRegistry(chapter.linkedObjectModID, chapter.linkedObjectStackName);
				if (tempblock == null){
					Logger.error("Error while reading key file" + file.toString() + ": unable to locate linked block");
					return;
				}
				EurekaRegistry.bindToKey(tempblock, chapter.name);
			} else if (chapter.linkedObjectStackType.toLowerCase().equals("item")){
				Item tempitem = getItemFromRegistry(chapter.linkedObjectModID, chapter.linkedObjectStackName);
				if (tempitem == null){
					Logger.error("Error while reading key file" + file.toString() + ": unable to locate linked item");
					return;
				}
				EurekaRegistry.bindToKey(tempitem, chapter.name);
			}

		} catch (Throwable e){
			e.printStackTrace();
		}
	}

	private static Item getItemFromRegistry(String modID, String name){
		return GameData.getItemRegistry().getObject(modID + ":" + name);
	}

	private static Block getBlockFromRegistry(String modID, String name){
		return GameData.getBlockRegistry().getObject(modID + ":" + name);
	}
}
