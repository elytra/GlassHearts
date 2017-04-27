/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017:
 * 	William Thompson (unascribed),
 * 	Isaac Ellingson (Falkreon),
 * 	Jamie Mansfield (jamierocks),
 * 	and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.glasshearts;

import java.util.ArrayList;
import java.util.Collection;
import com.google.common.base.Supplier;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

public class NBTHelper {
	
	/**
	 * Convert the given Collection of INBTSerializable objects into an
	 * NBTTagList.
	 */
	public <T extends INBTSerializable<U>,
			U extends NBTBase> NBTTagList serialize(Collection<T> in) {
		NBTTagList out = new NBTTagList();
		for (T t : in) {
			out.appendTag(t.serializeNBT());
		}
		return out;
	}
	
	/**
	 * Convert the given NBTTagList into an ArrayList of INBTSerializable
	 * objects, using freshly-created objects from the passed constructor.
	 * <p>
	 * Example: {@code NBTHelper.deserialize(MyObject::new, myNBTList)}
	 */
	public <T extends INBTSerializable<U>,
			U extends NBTBase> ArrayList<T> deserialize(Supplier<T> constructor, NBTTagList in) {
		return deserialize(constructor, in, ArrayList::new);
	}
	
	/**
	 * Convert the given NBTTagList into a Collection of INBTSerializable
	 * objects, using freshly-created objects from the passed constructor, and
	 * a freshly-created collection from the passed collection constructor. 
	 * <p>
	 * Example: {@code NBTHelper.deserialize(MyObject::new, myNBTList, ArrayList::new)}
	 */
	public <T extends INBTSerializable<U>,
			U extends NBTBase,
			C extends Collection<T>> C deserialize(Supplier<T> constructor, NBTTagList in, Supplier<C> collectionConstructor) {
		return deserializeInto(constructor, in, collectionConstructor.get());
	}
	
	/**
	 * Add INBTSerializable objects from the given NBTTagList into a
	 * pre-existing Collection of the correct type, using freshly-created
	 * objects from the passed constructor.
	 * <p>
	 * This method does not clear the passed collection, that's up to you if you
	 * want to do it. 
	 * <p>
	 * Example: {@code NBTHelper.deserialize(MyObject::new, myNBTList, myList)}
	 */
	public <T extends INBTSerializable<U>,
			U extends NBTBase,
			C extends Collection<T>> C deserializeInto(Supplier<T> constructor, NBTTagList in, C collection) {
		for (int i = 0; i < in.tagCount(); i++) {
			T t = constructor.get();
			t.deserializeNBT((U)in.get(i));
			collection.add(t);
		}
		return collection;
	}
	
}
