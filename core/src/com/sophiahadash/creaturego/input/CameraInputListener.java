package com.sophiahadash.creaturego.input;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sophiahadash.creaturego.MainMap;
import com.sophiahadash.creaturego.Settings;

public class CameraInputListener implements InputProcessor{

    boolean dragging = false;
    Vector2 dragStart;
    float startAngle;

    private MainMap main;
    public CameraInputListener(MainMap main){
        this.main = main;
    }

    /** Called when a key was pressed
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed */
    @Override
    public boolean keyDown (int keycode){
        return false;
    }

    /** Called when a key was released
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed */
    @Override
    public boolean keyUp (int keycode){
        return false;
    }

    /** Called when a key was typed
     *
     * @param character The character
     * @return whether the input was processed */
    @Override
    public boolean keyTyped (char character){
        return false;
    }

    /** Called when the screen was touched or a mouse button was pressed. The button parameter will be {@link Input.Buttons#LEFT} on iOS.
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event.
     * @param button the button
     * @return whether the input was processed */
    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button){
        if (pointer == 0){
            dragStart = new Vector2(screenX, screenY);
            startAngle = main.getCamHandle().getDimRotate();
            dragging = true;
            Gdx.app.log(Settings.log_tag, "starting drag");
        }else{
            dragging = false;
        }
        return false;
    }

    /** Called when a finger was lifted or a mouse button was released. The button parameter will be {@link Input.Buttons#LEFT} on iOS.
     * @param pointer the pointer for the event.
     * @param button the button
     * @return whether the input was processed */
    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button){
        dragging = false;
        return false;
    }

    /** Called when a finger or the mouse was dragged.
     * @param pointer the pointer for the event.
     * @return whether the input was processed */
    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer){
        //if first and single finger
        if (pointer == 0 && dragging){
            Vector2 newPos = new Vector2(screenX, screenY);
            Vector2 character = new Vector2(main.requestHandler.getScreenCoordsOfCharacter());
            Vector2 charToStart = new Vector2(character).sub(dragStart);
            Vector2 charToNew = new Vector2(character).sub(newPos);
            Vector2 moveDir = new Vector2(charToStart).sub(charToNew);

            Vector3 start3 = new Vector3(charToStart.x, charToStart.y, 0);
            Vector3 moveDir3 = new Vector3(moveDir.x, moveDir.y, 0);
            Vector3 dir3 = moveDir3.crs(start3).nor();

            float cosAngle = charToNew.dot(charToStart)/(charToNew.len()*charToStart.len());
            float angle = (float)Math.toDegrees(Math.acos(cosAngle)) * dir3.z;
            dragStart = newPos;

            //Gdx.app.log(Settings.log_tag, "Pstart: "+dragStart.toString()+"   Pnew: "+newPos.toString()+"   Pchar: "+character.toString()+"   cos(angle): "+cosAngle+"   angle: "+angle);

            if (angle != Float.NaN) {

                main.getCamHandle().setDimRotate(main.getCamHandle().getDimRotate() + angle);
                main.requestHandler.updateCamera();
            }
        }
        return false;
    }

    /** Called when the mouse was moved without any buttons being pressed. Will not be called on iOS.
     * @return whether the input was processed */
    @Override
    public boolean mouseMoved (int screenX, int screenY){
        return false;
    }

    /** Called when the mouse wheel was scrolled. Will not be called on iOS.
     * @param amount the scroll amount, -1 or 1 depending on the direction the wheel was scrolled.
     * @return whether the input was processed. */
    @Override
    public boolean scrolled (int amount){
        return false;
    }
}
