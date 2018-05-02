package com.sophiahadash.creaturego;

import com.badlogic.gdx.math.Vector2;

public interface RequestHandler {

    public void addMap(final int x, final int y, final int width, final int height);

    public void removeMap();

    public void updateCamera();

    public Vector2 getScreenCoordsOfCharacter();

}