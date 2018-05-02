package com.sophiahadash.creaturego;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import android.app.FragmentTransaction;
import android.location.Location;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sophiahadash.creaturego.CameraHandler;
import com.sophiahadash.creaturego.MainMap;
import com.sophiahadash.creaturego.RequestHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AndroidLauncher extends AndroidApplication implements RequestHandler, OnMapReadyCallback, LocationListener, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMapFragment.onReady,
        GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveCanceledListener, GoogleMap.OnCameraIdleListener {

    //The LibGDX Application
    protected MainMap gdxApp;

    //Stores the game-frame and the map-frame
    FrameLayout flGame;
    FrameLayout flMap;

    //Map
    GoogleMap map;
    private GoogleMapFragment mMapFragment;
    private static final String MAP_FRAGMENT_TAG = "map";

    //Camera for the map
    CameraPosition camPos;
    CameraHandler camHandle;

    //Google Location Provider
    FusedLocationProviderClient locationClient;

    //Store last known current location
    Location lastKnownLocation;
    private Vector2 lastKnownScreenLocation = Vector2.Zero;

    @Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        //create the LibGDX Game
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.r = cfg.g = cfg.b = cfg.a = 8;

        gdxApp = new MainMap(this);
        View gameView = initializeForView(gdxApp, cfg);

        if (graphics.getView() instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) graphics.getView();
            // force alpha channel - I'm not sure we need this as the GL surface is already using alpha channel
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setZOrderOnTop(true);
        }

        //Add the game to it's FrameLayout
        flGame = (FrameLayout) findViewById(R.id.flGame);
        flGame.addView(gameView);

        //Find the map FrameLayout
        flMap = (FrameLayout) findViewById(R.id.flMap);

        //Create (or find an existing) GoogleMapFragment
        mMapFragment = (GoogleMapFragment) getFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mMapFragment == null) {
            mMapFragment = (GoogleMapFragment) GoogleMapFragment.newInstance();

            //replaces the default flMap Fragment with the GoogleMapFragment
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(flMap.getId(), mMapFragment, MAP_FRAGMENT_TAG);
            fragmentTransaction.commit();

        }

        //Start fetching the map from Google's server. Callback is called when ready.
        mMapFragment.getMapAsync(this);

        //get the camera handler
        camHandle = gdxApp.getCamHandle();

        //Set the location provided client
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        //Request the last known location
        getInitialLocation();

        flGame.bringToFront();
        //locationClient.requestLocationUpdates()
	}

	private void getInitialLocation(){
        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },123 );
        }else {
            Task<Location> task = locationClient.getLastLocation();
            task.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    lastKnownLocation = task.getResult();
                    onLocationChanged(lastKnownLocation);
                }
            });
        }
    }

	//Can be called from the game-code. Sets the map to: visible
    @Override
    public void addMap(final int x, final int y, final int width, final int height) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
                lp.setMargins(x, 0, 0, y);
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                flMap.setLayoutParams(lp);
                flMap.setVisibility(View.VISIBLE);

            }
        });
    }

    //Can be called from the game-code. Sets the map to: invisible
    @Override
    public void removeMap() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flMap.setVisibility(View.GONE);
            }
        });
    }

    Circle targetCircle;
    @Override
    public void updateCamera(){
        if (camHandle == null){
            camHandle = gdxApp.getCamHandle();
            if (camHandle == null){
                return;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CameraHandler.CameraSettings s = camHandle.transformCamera(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                CameraPosition camPos =
                        new CameraPosition.Builder().target(new LatLng(s.targetLatitude, s.targetLongitude))
                                .zoom(s.zoom)
                                .bearing(s.bearing)
                                .tilt(s.tilt)
                                .build();

                map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));

                storeScreenLocation();

                if (targetCircle == null) {
                    targetCircle = map.addCircle(new CircleOptions()
                            .center(new LatLng(s.targetLatitude, s.targetLongitude))
                            .radius(.4)
                            .strokeWidth(.1f)
                            .strokeColor(Color.BLACK)
                            .fillColor(Color.BLACK));
                }else{
                    targetCircle.setCenter(new LatLng(s.targetLatitude, s.targetLongitude));
                }
            }
        });
    }

    @Override
    public Vector2 getScreenCoordsOfCharacter(){
        if (lastKnownScreenLocation.equals(Vector2.Zero)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    storeScreenLocation();
                }
            });
        }
        return lastKnownScreenLocation;
    }

    //Called when the map is received from google and ready
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        if (map != null) {
            if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },123 );
            }
            storeScreenLocation();

            //will do this manually
            map.setMyLocationEnabled(false);
            map.setBuildingsEnabled(false);
            map.setIndoorEnabled(false);

            //Add the map styling
            FileHandle handle = Gdx.files.internal("JSON/map_style.json");
            String mapStyleJson = handle.readString();

            MapStyleOptions mapStyle = new MapStyleOptions(mapStyleJson);
            map.setMapStyle(mapStyle);

            map.setOnCameraIdleListener(this);
            map.setOnCameraMoveStartedListener(this);
            map.setOnCameraMoveListener(this);
            map.setOnCameraMoveCanceledListener(this);

            //hide default UI overlay
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setIndoorLevelPickerEnabled(false);
            map.getUiSettings().setMapToolbarEnabled(false);


            //disable all gestures
            map.getUiSettings().setAllGesturesEnabled(false);

        }

    }

    @Override
    public void onCameraMoveStarted(int reason) {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraIdle() {

    }

    Circle circle;
    @Override
    public void onLocationChanged(Location location) {
        lastKnownLocation = location;

        if (map != null) {
            if (circle == null) {
                circle = map.addCircle(new CircleOptions()
                        .center(new LatLng(location.getLatitude(), location.getLongitude()))
                        .radius(.4)
                        .strokeWidth(.1f)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.BLUE));
            }else {
                circle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            updateCamera();
            Gdx.app.log(getResources().getString(R.string.log_tag), "Location changed!");
            storeScreenLocation();
        }

        gdxApp.setPosition((float) location.getLatitude(), (float) location.getLongitude());
    }

    private void storeScreenLocation(){
        if (lastKnownLocation != null && map!= null) {
            Point p = map.getProjection().toScreenLocation(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
            lastKnownScreenLocation = new Vector2(p.x, p.y);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case 123:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getInitialLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
        }
    }

    @Override
    public void isReady(){

    }
}
