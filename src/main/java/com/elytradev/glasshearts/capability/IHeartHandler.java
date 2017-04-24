package com.elytradev.glasshearts.capability;

import com.elytradev.glasshearts.logic.HeartContainer;

import net.minecraft.util.DamageSource;

public interface IHeartHandler extends Iterable<HeartContainer> {
	/**
	 * Damage this IHealthHandler for {@code amount} <b>hearts</b>. This differs
	 * from vanilla, which uses half-hearts.
	 * @param amount The amount of damage to apply, in <b>hearts</b>.
	 * @param src The source of the damage
	 * @return The amount of damage that was applied
	 */
	float damage(float amount, DamageSource src);
	/**
	 * Heal this IHealthHandler for {@code amount} <b>hearts</b>. This differs
	 * from vanilla, which uses half-hearts.
	 * @param amount The amount of healing to apply, in <b>hearts</b>.
	 * @return The amount of healing that was applied
	 */
	float heal(float amount);
	
	/**
	 * @return the amount of containers currently in this IHealthHandler
	 */
	int getContainers();
	
	/**
	 * Get the HeartContainer at the given index. Does not return a copy;
	 * <b>do not modify the return value without first copying it</b>.
	 * @param index the index of the container to retrieve
	 * @return the HeartContainer in this slot, never null
	 * @throws IndexOutOfBoundsException if {@code index} is &lt; 0 or &gt;=
	 * 		{@code size()}
	 */
	HeartContainer getContainer(int index);
	/**
	 * Get a copy of the HeartContainer at the given index. As it is a copy,
	 * the return value can be safely modified.
	 * @param index the index of the container to retrieve
	 * @return a copy of the HeartContainer in this slot, never null
	 * @throws IndexOutOfBoundsException if {@code index} is &lt; 0 or &gt;=
	 * 		{@code size()}
	 */
	HeartContainer copyContainer(int index);
	/**
	 * Get and remove the HeartContainer at the given index. Does not return
	 * a copy, though since the container has been removed it is safe to modify
	 * it.
	 * @param index the index of the container to retrieve
	 * @return the HeartContainer in this slot, never null
	 * @throws IndexOutOfBoundsException if {@code index} is &lt; 0 or &gt;=
	 * 		{@code size()}
	 * @throws UnsupportedOperationException if this IHealthHandler cannot be
	 * 		modified
	 */
	HeartContainer popContainer(int index);
	
	
	/**
	 * Set the HeartContainer at the given index.
	 * @param index the index of the container to replace
	 * @param container the HeartContainer to put in this slot
	 * @throws IndexOutOfBoundsException if {@code index} is &lt; 0 or &gt;=
	 * 		{@code size()}
	 * @throws UnsupportedOperationException if this IHealthHandler cannot be
	 * 		modified
	 */
	void setContainer(int index, HeartContainer container);
	
	
	/**
	 * Remove the HeartContainer at the given index.
	 * @param index the index of the container to remove
	 * @throws IndexOutOfBoundsException if {@code index} is &lt; 0 or &gt;=
	 * 		{@code size()}
	 * @throws UnsupportedOperationException if this IHealthHandler cannot be
	 * 		modified
	 */
	void removeContainer(int index);
	/**
	 * Add a new HeartContainer to this IHealthHandler, expanding its size by
	 * one.
	 * @param container the HeartContainer to add to the end
	 * @throws UnsupportedOperationException if this IHealthHandler cannot be
	 * 		modified
	 */
	void addContainer(HeartContainer container);
	/**
	 * Add a new HeartContainer at the specified index to this IHealthHandler,
	 * expanding its size by one and shifting all containers after it by one.
	 * @param index the index at which to insert the container
	 * @param container the HeartContainer to add to the given index
	 * @throws UnsupportedOperationException if this IHealthHandler cannot be
	 * 		modified
	 */
	void addContainer(int index, HeartContainer container);
	
	
	/**
	 * Remove all containers from this IHealthHandler.
	 */
	void clear();
	
	/**
	 * @return The total amount of health stored in this handler, in hearts.
	 */
	float totalHealth();
}
