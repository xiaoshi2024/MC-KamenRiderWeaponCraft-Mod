package com.xiaoshi2022.kamenriderweaponcraft.client.render.thunder;

/**
 * 雷电效果配置参数
 */
public class ThunderConfig {
    // ========== 基础参数 ==========
    public static float THUNDER_BASE_WIDTH = 0.08f;
    public static float THUNDER_CORE_WIDTH = 0.035f;
    public static int THUNDER_LIFETIME = 20;

    // ========== 颜色参数 ==========
    public static int COLOR_MAIN_R = 180;
    public static int COLOR_MAIN_G = 220;
    public static int COLOR_MAIN_B = 255;

    public static int COLOR_CORE_R = 255;
    public static int COLOR_CORE_G = 255;
    public static int COLOR_CORE_B = 255;

    public static int COLOR_NEAR_R = 200;
    public static int COLOR_NEAR_G = 230;
    public static int COLOR_NEAR_B = 255;

    public static int COLOR_MID_R = 130;
    public static int COLOR_MID_G = 180;
    public static int COLOR_MID_B = 255;

    public static int COLOR_FAR_R = 80;
    public static int COLOR_FAR_G = 130;
    public static int COLOR_FAR_B = 220;

    // ========== 辅助方法 ==========
    public static int getColorByDistance(float distance) {
        float ratio = Math.min(1.0f, distance / 8.0f);
        if (ratio < 0.3f) {
            return rgbToInt(COLOR_NEAR_R, COLOR_NEAR_G, COLOR_NEAR_B);
        } else if (ratio < 0.7f) {
            return rgbToInt(COLOR_MID_R, COLOR_MID_G, COLOR_MID_B);
        } else {
            return rgbToInt(COLOR_FAR_R, COLOR_FAR_G, COLOR_FAR_B);
        }
    }

    public static int getMainColor() {
        return rgbToInt(COLOR_MAIN_R, COLOR_MAIN_G, COLOR_MAIN_B);
    }

    public static int getCoreColor() {
        return rgbToInt(COLOR_CORE_R, COLOR_CORE_G, COLOR_CORE_B);
    }

    public static int rgbToInt(int r, int g, int b) {
        return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
