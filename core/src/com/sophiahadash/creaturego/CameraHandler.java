package com.sophiahadash.creaturego;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public class CameraHandler implements GestureDetector.GestureListener {
    public class CameraSettings{
        public double targetLatitude;
        public double targetLongitude;
        public float zoom;
        public float bearing;
        public float tilt;
    }

    private float dimZoom = .8f;
    private float dimRotate = 0f;

    final private float tiltMax = 70f;
    final private float tiltMin = 10f;

    final private float zoomMin = 18f;
    final private float zoomMax = 20f;

    final private float kmPerDegree = 111.32f;
    final private float distanceCharCenterMin = 10f; // in meters
    final private float distanceCharCenterMax = 30f;

    private boolean zooming = false;
    private float zoomStart;
    private final float zoomSpeed = 100f;

    private MainMap game;
    public CameraHandler(MainMap g){
        game = g;
    }

    public CameraSettings transformCamera(double latitude, double longitude){
        CameraSettings s = new CameraSettings();

        float degreePerM = 1 / (kmPerDegree * 1000);
        float distance = interpolate(distanceCharCenterMin, distanceCharCenterMax, dimZoom);
        s.targetLatitude = latitude + distance*degreePerM;
        s.targetLongitude = longitude;

        s.zoom = interpolate(zoomMin, zoomMax, dimZoom);
        s.bearing = 0;
        s.tilt = interpolate(tiltMin, tiltMax, dimZoom);

        return s;
    }

    private float interpolate(float min, float max, float alpha){
        return (max-min)*alpha+min;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        Gdx.app.log(Settings.log_tag, "touch down!");
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
    public boolean zoom (float originalDistance, float currentDistance){
        float df = currentDistance - originalDistance;//bigger -> zoom-in

        if (!zooming){
            zooming = true;
            zoomStart = dimZoom;
        }
        dimZoom = Math.max(0f, Math.min(1f, zoomStart + df/zoomSpeed ));

        game.requestHandler.updateCamera();

        Gdx.app.log(Settings.log_tag, "zooming");
        return true;
    }

    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){

        return false;
    }
    @Override
    public void pinchStop () {
        Gdx.app.log(Settings.log_tag, "zoom stop");
        zooming = false;
    }
}
