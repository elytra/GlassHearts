package com.elytradev.glasshearts.client.particle;

import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleRedstoneSeekEntity extends ParticleRedstone {

	private Entity seek;
	
	public ParticleRedstoneSeekEntity(Entity seek, World world, double x, double y, double z, float scale, float r, float g, float b) {
		super(world, x, y, z, scale, r, g, b);
		this.seek = seek;
		particleMaxAge *= 2;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		Vec3d diff = seek.getPositionVector().addVector(0, seek.height/2, 0).subtract(posX, posY, posZ);
		
		double targetX = diff.x;
		double targetY = diff.y;
		double targetZ = diff.z;
		double diffX = targetX-motionX;
		double diffY = targetY-motionY;
		double diffZ = targetZ-motionZ;
		motionX += (diffX/32);
		motionY += (diffY/32);
		motionZ += (diffZ/32);
		
		if (seek.getDistanceSq(posX, posY-seek.height/2, posZ) < 0.1) {
			setExpired();
		}
	}

	public void setVelocity(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}

}
