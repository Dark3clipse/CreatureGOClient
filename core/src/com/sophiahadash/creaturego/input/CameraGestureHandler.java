package com.sophiahadash.creaturego.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.sophiahadash.creaturego.MainMap;
import com.sophiahadash.creaturego.Settings;

public class CameraGestureHandler implements GestureDetector.GestureListener{

    private MainMap main;
    private boolean zooming = false;
    private float zoomStart;
    private final float zoomSpeed = 100f;

    public CameraGestureHandler(MainMap main){
        this.main = main;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float originalDistance, float currentDistance) {
        float df = currentDistance - originalDistance;//bigger -> zoom-in

        if (!zooming) {
            zooming = true;
            zoomStart = main.getCamHandle().getDimZoom();
        }
        main.getCamHandle().setDimZoom(Math.max(0f, Math.min(1f, zoomStart + df / zoomSpeed)));

        main.requestHandler.updateCamera();
        return true;
    }

    @Override
    public boolean pinch(Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
        return false;
    }

    @Override
    public void pinchStop() {
        Gdx.app.log(Settings.log_tag, "zoom stop");
        zooming = false;
    }
}
