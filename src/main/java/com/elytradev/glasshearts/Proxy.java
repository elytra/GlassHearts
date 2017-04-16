package com.elytradev.glasshearts;

import com.elytradev.glasshearts.tile.TileEntityGlassHeart;

public interface Proxy {
	void onPreInit();
	void onPostInit();
	void onLoad(TileEntityGlassHeart te);
}
