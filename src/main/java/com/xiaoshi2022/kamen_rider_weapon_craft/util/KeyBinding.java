package com.xiaoshi2022.kamen_rider_weapon_craft.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY_kamen_rider_weapon_craft = "KEY.kamen_rider_weapon_craft.kamen_rider_weapon_craft";
    public static final String KEY_CHANGE_OVER = "KEY.kamen_rider_weapon_craft.change_over";
    public static final String KEY_OPEN_LOCKSEED_GUI = "KEY.kamen_rider_weapon_craft.open_lockseed_gui";

    public static final KeyMapping CHANGE_KEY = new KeyMapping(KEY_CHANGE_OVER, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, KEY_CATEGORY_kamen_rider_weapon_craft);
    public static final KeyMapping OPEN_LOCKSEED = new KeyMapping(KEY_OPEN_LOCKSEED_GUI, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, KEY_CATEGORY_kamen_rider_weapon_craft);

}
