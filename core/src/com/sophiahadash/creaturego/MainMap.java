package com.sophiahadash.creaturego;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class MainMap extends ApplicationAdapter {
    final int WIDTH = 480;
    final int HEIGHT = 800;

    public RequestHandler requestHandler;

    public MainMap(RequestHandler mapHandler) {
        this.requestHandler = mapHandler;

    }

    TextButtonStyle txButtonStyle;
    LabelStyle lbStyle;
    BitmapFont font;

    Stage stage;
    GoogleMapActor mapActor;

    TextButton btShowMap;
    TextButton btHideMap;

    Label lbPosition;

    private CameraHandler camHandle;

    @Override
    public void create() {
        camHandle = new CameraHandler(this);

        stage = new Stage(new StretchViewport(WIDTH, HEIGHT));

        font = new BitmapFont();

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui.txt"));
        txButtonStyle = new TextButtonStyle(new NinePatchDrawable(atlas.createPatch("bt")), new NinePatchDrawable(atlas.createPatch("btDown")), null,
                font);
        lbStyle = new LabelStyle(font, Color.WHITE);

        /*Label lbl = new Label("testtest", lbStyle);
        lbl.setPosition(10, 10);
        stage.addActor(lbl);*/

        mapActor = new GoogleMapActor(requestHandler);
        mapActor.setSize(WIDTH, HEIGHT);
        mapActor.setPosition(0, 0);
        mapActor.showMap(stage);

        stage.addActor(mapActor);

        Gdx.input.setInputProcessor(new GestureDetector(camHandle));

        requestHandler.updateCamera();
    }

    public CameraHandler getCamHandle() {
        return camHandle;
    }

    public void setPosition(float lat, float lng) {
        //lbPosition.setText("Latitude: " + lat + ", Longitude: " + lng);
    }

    @Override
    public void render() {
        stage.act();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        stage.draw();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

}
