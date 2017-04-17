package com.elytradev.glasshearts.client;

import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleAttune extends ParticleRedstone {

	private Entity seek;
	
	public ParticleAttune(Entity seek, World world, double x, double y, double z, float scale, float r, float g, float b) {
		super(world, x, y, z, scale, r, g, b);
		this.seek = seek;
		particleMaxAge *= 4;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (particleAge > 20) {
			Vec3d diff = seek.getPositionVector().addVector(0, seek.height/2, 0).subtract(posX, posY, posZ).normalize().scale(0.4);
			
			motionX = diff.xCoord;
			motionY = diff.yCoord;
			motionZ = diff.zCoord;
			
			if (seek.getDistanceSq(posX, posY-seek.height/2, posZ) < 0.1) {
				setExpired();
			}
		} else {
			motionX *= 0.8;
			motionY *= 0.8;
			motionZ *= 0.8;
		}
	}

	public void setVelocity(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}

}
