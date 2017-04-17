package com.elytradev.glasshearts.client;

import java.util.Random;

import net.minecraft.client.gui.Gui;

public class GuiParticle {

	public double prevPosX;
	public double prevPosY;

	public double posX;
	public double posY;

	public double motionX;
	public double motionY;

	public boolean expired;

	public int age;
	public int maxAge;
	public float gravity;

	public Random rand = new Random();
	
	public float red = 1;
	public float green = 1;
	public float blue = 1;

	public GuiParticle(double x, double y) {
		posX = x;
		posY = y;

		prevPosX = x;
		prevPosY = y;

		maxAge = (int) (4 / (rand.nextFloat() * 0.9f + 0.1f));
		age = 0;
		
		motionX = rand.nextFloat()-0.5f;
		motionY = rand.nextFloat()-0.5f;

		gravity = 12;
	}

	public void setExpired() {
		expired = true;
	}

	public void update() {
		prevPosX = posX;
		prevPosY = posY;

		if (age++ >= maxAge) {
			setExpired();
		}

		motionY += 0.04 * gravity;
		motionX *= 0.98;
		motionY *= 0.98;

		posX += motionX;
		posY += motionY;
	}

	public void render(float partialTicks) {
		double interpPosX = prevPosX + (posX - prevPosX) * partialTicks;
		double interpPosY = prevPosY + (posY - prevPosY) * partialTicks;

		Gui.drawRect((int) interpPosX - 2, (int) interpPosY - 2, (int) interpPosX + 2, (int) interpPosY + 2, -1);
	}

}
