package com.xiaoshi2022.kamenriderweaponcraft.rider;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.decade.DecadeRiderRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.den_o.DenOTrainRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.drive.DriveRiderRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.faiz.FaizEmptySetRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.fourze.FourzeRocketRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.gaim.GaimLockSeedRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.hibiki.HibikiDrumEffectRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kiva.KivaBatRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kuuga.KuugaRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ooo.OOOGeoEntityRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.w.WTornadoRenderer;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.wizard.WizardRiderRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID, value = Dist.CLIENT)
public class RiderEffectEntityRegistry {

    @SubscribeEvent
    public static void onEntityRenderersRegister(EntityRenderersEvent.RegisterRenderers event) {
        // 注册所有骑士特效实体渲染器
        event.registerEntityRenderer(EntityRegister.DECADE_RIDER.get(), DecadeRiderRenderer::new);
        event.registerEntityRenderer(EntityRegister.KUUGA_RIDER.get(), KuugaRenderer::new);
        event.registerEntityRenderer(EntityRegister.WIZARD_RIDER.get(), WizardRiderRenderer::new);
        event.registerEntityRenderer(EntityRegister.DRIVE_RIDER.get(), DriveRiderRenderer::new);
        event.registerEntityRenderer(EntityRegister.DEN_O_TRAIN.get(), DenOTrainRenderer::new);
        event.registerEntityRenderer(EntityRegister.FOURZE_ROCKET.get(), FourzeRocketRenderer::new);
        event.registerEntityRenderer(EntityRegister.GAIM_LOCK_SEED.get(), GaimLockSeedRenderer::new);
        event.registerEntityRenderer(EntityRegister.HIBIKI_DRUM_EFFECT.get(), HibikiDrumEffectRenderer::new);
        event.registerEntityRenderer(EntityRegister.KIVA_BAT_EFFECT.get(), KivaBatRenderer::new);
        event.registerEntityRenderer(EntityRegister.OOO_GEO_EFFECT.get(), OOOGeoEntityRenderer::new);
        event.registerEntityRenderer(EntityRegister.W_TORNADO.get(), WTornadoRenderer::new);
        event.registerEntityRenderer(EntityRegister.FAIZ_EMPTY_SET.get(), FaizEmptySetRenderer::new);
    }
}
