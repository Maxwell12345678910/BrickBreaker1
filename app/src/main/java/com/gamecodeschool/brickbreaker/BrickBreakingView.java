package com.gamecodeschool.brickbreaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class BrickBreakingView extends SurfaceView implements Runnable{
    private final float BALL_SPEED = 15f; // How many pixels to travel diagonally
                                          // each time the screen gets updated.
                                          // e.g. With 15f, 15 pixels right, and 15 pixels up.
                                          // must be <= BRICK_THICKNESS
    final static int BRICK_THICKNESS = 40;
    private int PADDLE_WIDTH; // The constructor determines this based on screen width
    private Brick paddle; // The paddle
    final private float PCBPY = 5f/6f; // Paddle Control Button Position Y: 5/6th down the screen length
    final private float PFBPY = 4f/5f; // Play Field Bottom Position Y: 4/5th down the screen length
    final private int PFTPYAbs = 200; // Play Field Top Position Y Absolute
    private Brick bottomLeftBrick; // The brick that appears at bottom left when the paddle is there
    private Brick bottomRightBrick; // The brick that appears at the bottom right when the paddle is there
    private RectF playFieldCoords; // The play field coordinates including the top, side, bottom bricks
                                     // The constructor determines this based on the screen size
                                     // and the constant parameters above
    private RectF entireScreenCoords; // The entire screen coordinates
    private ArrayList<Brick> mBrickList = new ArrayList<>(); // list of all bricks
    private SurfaceHolder mOurHolder;
    private Canvas mCanvas;
    private Paint mPaint;
    private int mScreenX;
    private int mScreenY;
    private Thread mThread = null;
    private volatile boolean mViewActive;
    private Bitmap leftButtonBitmap = BitmapFactory.decodeResource
            (getResources(), R.drawable.left_arrow); // left_arrow.png must be in /res/drawable
    private Bitmap rightButtonBitmap = BitmapFactory.decodeResource
            (getResources(), R.drawable.right_arrow);
    private Bitmap upButtonBitmap = BitmapFactory.decodeResource
            (getResources(), R.drawable.up_arrow);
    private Bitmap downButtonBitmap = BitmapFactory.decodeResource
            (getResources(), R.drawable.down_arrow);
    private final float GAP_BETWEEN_BUTTONS = 0f; // gap between left and right buttons.
                                                  // Range: 0.0 to 1.0. Fix this to 0f.
    private RectF mLeftButtonCoords;
    private RectF mRightButtonCoords;
    private RectF mUpButtonCoords;
    private RectF mDownButtonCoords;
    private volatile boolean rightButtonDown = false;
    private volatile boolean leftButtonDown = false;
    private volatile boolean upButtonDown = false;
    private volatile boolean downButtonDown = false;
    public BrickBreakingView(Context context, int x, int y) {
        super(context);

        mScreenX = x;
        mScreenY = y;
        PADDLE_WIDTH = mScreenX/4; // set the paddle with to be 1/4 of screen width
        entireScreenCoords = new RectF(0,0,mScreenX-1,mScreenY-1);
        float playFieldBottomPositionY = mScreenY*PFBPY;
        playFieldCoords = new RectF(0,PFTPYAbs,mScreenX-1,
                (int)(playFieldBottomPositionY)-1);

        mOurHolder = getHolder();
        mPaint = new Paint();

        // Initialize the four buttons
        int leftButtonRunLength = mScreenX/2-leftButtonBitmap.getWidth();
        int rightButtonRunLength = mScreenX/2-rightButtonBitmap.getWidth();
        int leftRight = mScreenX/2-1-(int)(GAP_BETWEEN_BUTTONS*((float) leftButtonRunLength));
        int rightLeft = mScreenX/2+(int)(GAP_BETWEEN_BUTTONS*((float) rightButtonRunLength));
        int buttonPositionY = (int) (mScreenY* PCBPY);
        mLeftButtonCoords = new RectF(leftRight-leftButtonBitmap.getWidth()+1, buttonPositionY,
                        leftRight, buttonPositionY+leftButtonBitmap.getHeight()-1);
        mRightButtonCoords = new RectF(rightLeft, buttonPositionY,
                rightLeft+rightButtonBitmap.getWidth()-1,
                buttonPositionY+rightButtonBitmap.getHeight()-1);

        int upButtonRunLength = mScreenX/2-upButtonBitmap.getWidth();
        int downButtonRunLength = mScreenX/2-downButtonBitmap.getWidth();
        int upRight = mScreenX/2-1-(int)((1f-GAP_BETWEEN_BUTTONS)*((float) upButtonRunLength));
        int downLeft = mScreenX/2+(int)((1f-GAP_BETWEEN_BUTTONS)*((float) downButtonRunLength));
        mUpButtonCoords = new RectF(upRight-upButtonBitmap.getWidth()+1, buttonPositionY,
                upRight, buttonPositionY+upButtonBitmap.getHeight()-1);
        mDownButtonCoords = new RectF(downLeft, buttonPositionY,
                downLeft+downButtonBitmap.getWidth()-1,
                buttonPositionY+downButtonBitmap.getHeight()-1);

        Brick topBrick = new Brick(BRICK_THICKNESS,PFTPYAbs,
                mScreenX-BRICK_THICKNESS-1,PFTPYAbs+BRICK_THICKNESS-1,
                true, false, 0);
        Brick topLeftBrick = new Brick(0,PFTPYAbs,
                BRICK_THICKNESS-1,PFTPYAbs+BRICK_THICKNESS-1,
                true, true, 0);
        Brick topRightBrick = new Brick(mScreenX-BRICK_THICKNESS, PFTPYAbs,
                mScreenX-1,PFTPYAbs+BRICK_THICKNESS-1,
                true, true, 0);
        Brick leftSideBrick = new Brick(0,PFTPYAbs+BRICK_THICKNESS,
                BRICK_THICKNESS-1,(int)(playFieldBottomPositionY)-BRICK_THICKNESS-1,
                false,true, 0);
        Brick rightSideBrick = new Brick(mScreenX-BRICK_THICKNESS,PFTPYAbs+BRICK_THICKNESS,
                mScreenX-1,(int)(playFieldBottomPositionY)-BRICK_THICKNESS-1,
                false, true, 0);
        /*
        Brick bottomBrick = new Brick(BRICK_THICKNESS,(int)(playFieldBottomPositionY)-BRICK_THICKNESS,
                mScreenX-BRICK_THICKNESS-1,(int)(playFieldBottomPositionY)-1,
                true, false, 0, idFX2, mSP);
         */
        bottomLeftBrick = new Brick(0,(int)(playFieldBottomPositionY)-BRICK_THICKNESS,
                BRICK_THICKNESS-1,(int)(playFieldBottomPositionY)-1,
                true, true, 0);
        bottomRightBrick = new Brick(mScreenX-BRICK_THICKNESS, (int)(playFieldBottomPositionY)-BRICK_THICKNESS,
                mScreenX-1,(int)(playFieldBottomPositionY)-1,
                true, true, 0);

        mBrickList.add(topBrick);
        mBrickList.add(topLeftBrick);
        mBrickList.add(topRightBrick);
        mBrickList.add(leftSideBrick);
        mBrickList.add(rightSideBrick);
        /*
        mBrickList.add(bottomBrick);
        */
        mBrickList.add(bottomLeftBrick);
        mBrickList.add(bottomRightBrick);


        paddle = new Brick(BRICK_THICKNESS,(int)(playFieldBottomPositionY)-BRICK_THICKNESS,
                BRICK_THICKNESS+PADDLE_WIDTH-1,(int)(playFieldBottomPositionY)-1,
                true, false, 0,
                BRICK_THICKNESS,(playFieldCoords.top+ playFieldCoords.bottom)/2f,
                mScreenX-BRICK_THICKNESS-1,(int)(playFieldBottomPositionY)-1);
        mBrickList.add(paddle);
    }
    public void update(){
        if(leftButtonDown){
            movePaddleLeft(1f);
        } else if(rightButtonDown){
            movePaddleRight(1f);
        } else if(upButtonDown){
            movePaddleUp(1f);
        } else if(downButtonDown){
            movePaddleDown(1f);
        }
        bottomLeftBrick.setInPlay(paddle.atLeftBottomLimit());
        bottomRightBrick.setInPlay(paddle.atRightBottomLimit());
    }
    private void draw() {
        if (mOurHolder.getSurface().isValid()) {
            mCanvas = mOurHolder.lockCanvas();

            mCanvas.drawColor(Color.argb(255, 0, 0, 0));
            mPaint.setColor(Color.argb(255, 255, 255, 255));

            for(Brick b: mBrickList){
                b.draw(mCanvas,mPaint);
            }

            mCanvas.drawBitmap(leftButtonBitmap,null, mLeftButtonCoords,mPaint);
            mCanvas.drawBitmap(rightButtonBitmap,null, mRightButtonCoords,mPaint);
            mCanvas.drawBitmap(upButtonBitmap,null, mUpButtonCoords,mPaint);
            mCanvas.drawBitmap(downButtonBitmap,null, mDownButtonCoords,mPaint);

            mOurHolder.unlockCanvasAndPost(mCanvas);
        }
    }
    @Override
    public void run() {
        while (mViewActive) {
            update();
            draw();
        }
    }
    void movePaddleLeft(float times){
        paddle.offset(-BALL_SPEED *times, 0f);
    }
    void movePaddleRight(float times){
        paddle.offset(BALL_SPEED *times, 0f);
    }
    void movePaddleUp(float times){
        paddle.offset(0f, -BALL_SPEED *times);
    }
    void movePaddleDown(float times){
        paddle.offset(0f, BALL_SPEED *times);
    }
    public void pause() {
        mViewActive = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }
    public void resume() {
        mViewActive = true;
        mThread = new Thread(this);
        mThread.start();
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // User moved a finger while touching screen
        int maskedAction = motionEvent.getAction() & MotionEvent.ACTION_MASK;
        float motionPosX = motionEvent.getX();
        float motionPosY = motionEvent.getY();
        if(maskedAction == MotionEvent.ACTION_DOWN){
            if(mRightButtonCoords.contains(motionPosX,motionPosY)){
                rightButtonDown = true;
                movePaddleRight(1f);
            } else if(mLeftButtonCoords.contains(motionPosX,motionPosY)){
                leftButtonDown = true;
                movePaddleLeft(1f);
            }else if(mUpButtonCoords.contains(motionPosX,motionPosY)){
                upButtonDown = true;
                movePaddleUp(1f);
            } else if(mDownButtonCoords.contains(motionPosX,motionPosY)){
                downButtonDown = true;
                movePaddleDown(1f);
            }
        } else if(maskedAction == MotionEvent.ACTION_UP || maskedAction == MotionEvent.ACTION_CANCEL){
            if(mRightButtonCoords.contains(motionPosX,motionPosY)){
                rightButtonDown = false;
            } else if(mLeftButtonCoords.contains(motionPosX,motionPosY)){
                leftButtonDown = false;
            } else if(mUpButtonCoords.contains(motionPosX,motionPosY)){
                upButtonDown = false;
            } else if(mDownButtonCoords.contains(motionPosX,motionPosY)){
                downButtonDown = false;
            } else { // must be action cancel. Consider both buttons to be up now.
                rightButtonDown = false;
                leftButtonDown = false;
                upButtonDown = false;
                downButtonDown = false;
            }
        }
        return true;
    }
}
