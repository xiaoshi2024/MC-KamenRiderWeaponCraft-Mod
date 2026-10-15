package com.xiaoshi2022.kamenriderweaponcraft.register;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.SonicArrowItem;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegister {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KamenRiderWeaponCraft.MODID);

    // 使用 Heiseisword 类中定义的 Tier，移除重复定义
    // 注意：Heiseisword 的构造函数现在接受 Tier 和 Properties
    // 但为了简单，直接使用无参构造，因为它已经定义了内部 Tier
    public static final DeferredItem<Heiseisword> HEISEISWORD = ITEMS.register("heiseisword",
            Heiseisword::new);  // 使用无参构造

    // ==================== 音速弓 ====================
    public static final DeferredItem<SonicArrowItem> SONICARROW = ITEMS.register("sonicarrow",
            SonicArrowItem::new);

    // 锁种不再内置，改由软联动使用假面骑士工艺（kamenridercraft）的锁种，见 compat/KRCCompat

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}