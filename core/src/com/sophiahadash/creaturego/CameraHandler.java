package com.sophiahadash.creaturego;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public class CameraHandler{
    public class CameraSettings {
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

    final private float zoomMin = 19f;
    final private float zoomMax = 19.9f;

    final private float kmPerDegree = 111.32f;
    final private float distanceCharCenterMin = 10f; // in meters
    final private float distanceCharCenterMax = 30f;



    private MainMap game;

    public CameraHandler(MainMap g) {
        game = g;
    }

    public CameraSettings transformCamera(double latitude, double longitude) {
        CameraSettings s = new CameraSettings();

        float degreePerM = 1 / (kmPerDegree * 1000);
        float degreePerMLongitude = 1 / (kmPerDegree*(float)Math.cos(Math.toRadians(latitude)) * 1000);
        float distance = interpolate(distanceCharCenterMin, distanceCharCenterMax, dimZoom);
        s.targetLatitude = latitude + distance * degreePerM * Math.cos(Math.toRadians(dimRotate));
        s.targetLongitude = longitude + distance * degreePerMLongitude * Math.sin(Math.toRadians(dimRotate));
        s.zoom = interpolate(zoomMin, zoomMax, dimZoom);
        s.tilt = interpolate(tiltMin, tiltMax, dimZoom);
        s.bearing = dimRotate;

        return s;
    }

    private float interpolate(float min, float max, float alpha) {
        return (max - min) * alpha + min;
    }

    public float getDimZoom() {
        return dimZoom;
    }
    public void setDimZoom(float zoom){
        dimZoom = zoom;
    }

    public float getDimRotate() {
        return dimRotate;
    }
    public void setDimRotate(float rotate){
        dimRotate = rotate;
        while (dimRotate >= 360f){
            dimRotate -= 360f;
        }
    }

    public Vector2 getScreenCoordsCharacter(){
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        return new Vector2(w/2, h*(.415f-(float)Math.sin(dimZoom)*.2f));
    }
}
