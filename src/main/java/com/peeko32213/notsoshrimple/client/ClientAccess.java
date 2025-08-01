package com.peeko32213.notsoshrimple.client;

//import com.peeko32213.notsoshrimple.common.entity.EntityToxicWater;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("resource")
public class ClientAccess {
    //Part of the legacy packets infrastructure
    /*public static boolean sendDatatoPiss(Vec3 startPosFromShrimp, Vec3 deltaPosFromShrimp, int timerFromShrimp, int id) {
        final Entity toxicWater = Minecraft.getInstance().level.getEntity(id);
        if (toxicWater instanceof final EntityToxicWater urine) {
            urine.startPos = startPosFromShrimp;
            urine.deltaPos = deltaPosFromShrimp;
            urine.normalDeltaPos = deltaPosFromShrimp.normalize();
            return true;
        }

        return false;
    }*/
}