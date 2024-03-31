package com.gamecodeschool.brickbreaker;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;

import java.io.IOException;

public class BrickBreakingActivity extends Activity {

    private BrickBreakingView mBrickBreakingView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Display display = getWindowManager()
                .getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mBrickBreakingView = new BrickBreakingView(this, size.x, size.y);
        setContentView(mBrickBreakingView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBrickBreakingView.resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mBrickBreakingView.pause();
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
            mBrickBreakingView.movePaddleLeft(2f);
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
            mBrickBreakingView.movePaddleRight(2f);
        } else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            mBrickBreakingView.movePaddleUp(2f);
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            mBrickBreakingView.movePaddleDown(2f);
        }
        return super.dispatchKeyEvent(event);
    }
}