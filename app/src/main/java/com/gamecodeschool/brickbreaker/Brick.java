package com.gamecodeschool.brickbreaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.SoundPool;

import java.util.Random;

public class Brick {
    private RectF position;
    private RectF limits;
    private boolean horizontalReflection;
    private boolean verticalReflection;
    public int lifeLeft; // ranges from 0 to 3, 0 means unbreakable brick.
    // if lifeLeft becomes 0 after decrementing, brick is broken, and must set inPlay to false.
    public boolean inPlay;
    public Brick(float left, float top, float right, float bottom, boolean horizontalReflection,
                 boolean verticalReflection, int lifeLeft){
        position = new RectF(left, top, right, bottom);
        limits = new RectF(left,top,right,bottom);
        this.horizontalReflection = horizontalReflection;
        this.verticalReflection = verticalReflection;
        this.lifeLeft = lifeLeft;
        inPlay = true;
    }
    public Brick(float left, float top, float right, float bottom, boolean horizontalReflection,
                 boolean verticalReflection, int lifeLeft,
                 float leftLimit, float topLimit, float rightLimit, float bottomLimit){
        this(left, top, right, bottom, horizontalReflection, verticalReflection,lifeLeft);
        limits.set(leftLimit,topLimit,rightLimit,bottomLimit);
    }
    public RectF getPosition(){
        return position;
    }
    public void draw(Canvas canvas, Paint paint){
        if( inPlay ){
            canvas.drawRect(position, paint);
        }
    }
    public void offset(float dx, float dy){
        position.offset(dx,dy);
        float adjustDX=0f, adjustDY=0f;
        if(position.left<limits.left){
            adjustDX = limits.left-position.left;
        } else if(position.right>limits.right){
            adjustDX = limits.right-position.right;
        }
        if(position.top<limits.top) {
            adjustDY = (limits.top - position.top);
        }else if(position.bottom>limits.bottom){
            adjustDY = (limits.bottom-position.bottom);
        }
        position.offset(adjustDX,adjustDY);
    }
    public void setInPlay(boolean enabled){
        inPlay = enabled;
    }
    public boolean atLeftBottomLimit(){
        return (position.left==limits.left && position.bottom==limits.bottom);
    }
    public boolean atRightBottomLimit(){
        return (position.right==limits.right && position.bottom==limits.bottom);
    }
}
