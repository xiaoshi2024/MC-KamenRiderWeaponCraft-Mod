package com.xiaoshi2022.kamen_rider_weapon_craft;

import com.xiaoshi2022.kamen_rider_weapon_craft.component.ridermodComponents;
import com.xiaoshi2022.kamen_rider_weapon_craft.event.EntityDeathEventListener;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItemGroups;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kamen_Rider_Weapon_Craft implements ModInitializer {
    public static final String MOD_ID = "kamen_rider_weapon_craft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // 1. 先初始化组件
        ridermodComponents.initialize();

        // 2. 再初始化物品（这会创建HEISEI_SWORD实例）
        ModItems.initialize();

        // 3. 然后初始化物品组（这会使用HEISEI_SWORD）
        ModItemGroups.initialize();

        ModEntityTypes.initialize();

        // 注意：KeyBindings.registerKeyBindings() 现在在客户端初始化器中调用

        // 注册实体死亡事件监听器
        EntityDeathEventListener.register();

        // 3. 初始化网络处理器
        NetworkHandler.register();

        // 4. 最后初始化音效
        ModSounds.initialize();
        
        // 5. 注册实体死亡事件监听器
        EntityDeathEventListener.register();

        LOGGER.info("Hello Fabric world! All components initialized in correct order.");
    }
}