package com.xiaoshi2022.kamen_rider_weapon_craft.world.gen;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class HelheimTeleporter implements ITeleporter {

    public PortalInfo getPortalInfo(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, double x, double y, double z, float yaw, Function<Boolean, Entity> repositionEntity) {
        Vec3 destination = new Vec3(0, 64, 0); // 目标位置
        return new PortalInfo(destination, Vec3.ZERO, yaw, 0);
    }
}