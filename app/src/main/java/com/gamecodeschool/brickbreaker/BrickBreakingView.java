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

    String hitsLeftStr="072";
    int hitsLeft=72;
    public long timer= 0;
    long elapsedTimeSeconds = 0;
    String elapsedTimeString = "0:00:00";

    public ArrayList<BreakableBrick> breakableBricks = new ArrayList<>(); //List of BREAKABLE gifts

    private boolean gameStarted = false;

    private Ball ball;
    private int BALL_RADIUS = 20;
    private final float BALL_SPEED = 15f; // How many pixels to travel diagonally // each time the screen gets updated.
    // e.g. With 15f, 15 pixels right, and 15 pixels up.
    // must be <= BRICK_THICKNESS

    final static int BRICK_THICKNESS = 40;
    private int PADDLE_WIDTH; // The constructor determines this based on screen width
    private Brick paddle; // The paddle
    final private float PCBPY = 5f/6f; // Paddle Control Button Position Y: 5/6th down the screen length
    final private float PFBPY = 4f/5f; // Play Field Bottom Position Y: 4/5th down the screen length
    final private int PFTPYAbs = 150; // Play Field Top Position Y Absolute
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

    //CONSTRUCTOR , STUFF THAT SHOULD BE DONE FOR FIRST GAME LAUNCH SHOULD BE TRIGGERED HERE
    public BrickBreakingView(Context context, int x, int y) {
        super(context);

        ball = new Ball(mScreenX / 2, mScreenY / 2, 5f, -5f, BALL_RADIUS);


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

        initBreakBricks();

    }

    private void initBreakBricks() {
        final int BRICK_WIDTH = 70;
        final int BRICK_HEIGHT = 40;
        final int BRICK_GAP = 5;

        // Determine the position of the first row of bricks
        int startX = 88;  // Adjusted to 80 pixels to the right
        int startY = 300; // Adjusted to 300 pixels further down vertically

        // Create breakable bricks for each row
        for (int row = 0; row < 3; row++) { //iterates through each row

            if(row>=1) //between the 1st and 2nd row we add 40 px of space (a bricks height worth)
                startY+=40;


            for (int col = 0; col < 12; col++) { //iterates 12 times for BreakableBrick item in each row
                // Calculate position of the current brick
                int left = startX + col * (BRICK_WIDTH + BRICK_GAP);
                int top = startY + row * (BRICK_HEIGHT + BRICK_GAP);
                int right = left + BRICK_WIDTH;
                int bottom = top + BRICK_HEIGHT;

                // Create breakable brick and add it to the list
                RectF position = new RectF(left, top, right, bottom);

                int color;//set the color of the brick based on its row value
                if(row == 0)
                    color = Color.RED;
                else if(row == 1)
                        color = Color.YELLOW;
                else color = Color.GREEN;


                BreakableBrick brick = new BreakableBrick(position, color,true);
                breakableBricks.add(brick);
            }
        }
    }

    public void update(){

        if (!gameStarted) {
            return; // Don't update anything until the game starts
        }

        ball.update();


        if(leftButtonDown){
            movePaddleLeft(1f);
        } else if(rightButtonDown){
            movePaddleRight(1f);
        } else if(upButtonDown){
            movePaddleUp(1f);
        } else if(downButtonDown){
            movePaddleDown(1f);
        }


        checkBallPaddleCollision();
        checkBallHitBrick();
        checkBallHitWall();


        bottomLeftBrick.setInPlay(paddle.atLeftBottomLimit());
        bottomRightBrick.setInPlay(paddle.atRightBottomLimit());

        // Calculate elapsed time in seconds
        elapsedTimeSeconds = (System.currentTimeMillis() - timer) / 1000;

        // Convert elapsed time to a formatted string (HH:MM:SS)
        elapsedTimeString = String.format("%01d:%02d:%02d", elapsedTimeSeconds / 3600, (elapsedTimeSeconds % 3600) / 60, elapsedTimeSeconds % 60);

        // Log the formatted string
        Log.d("Timer", "Elapsed Time: " + elapsedTimeString);

    }



    private void checkBallPaddleCollision() {
        RectF ballBounds = ball.getBounds(); // Get the bounds of the ball
        RectF paddleBounds = paddle.getPosition(); // Get the bounds of the paddle

        // Check if the ball intersects with the paddle
        if (RectF.intersects(ballBounds, paddleBounds)) {
            // Implement collision response here
            // For example, reverse the vertical direction of the ball
            ball.setIncreaseY(-Math.abs(ball.getIncreaseY()));
        }

    }




    private void checkBallHitWall() {

        // Left wall
        if (ball.getPosX() - ball.getRadius() <= (playFieldCoords.left+40)) {
            ball.setIncreaseX(Math.abs(ball.getIncreaseX())); // Reverse X direction

        }
        // Right wall
        else if (ball.getPosX() + ball.getRadius() >= (playFieldCoords.right-40)) {
            ball.setIncreaseX(-Math.abs(ball.getIncreaseX())); // Reverse X direction

        }

        // Top wall
        if (ball.getPosY() - ball.getRadius() <= (playFieldCoords.top+40)) {
            ball.setIncreaseY(Math.abs(ball.getIncreaseY())); // Reverse Y direction

        }


        // (missed the paddle)
        else if (ball.getPosY() + ball.getRadius() >= playFieldCoords.bottom) { // BALL FELL OFF THE SCREEN
            // Handle game over or reset ball position
            // For now, let's reset the ball position to the center
            ball.setPosX(mScreenX / 2);
            ball.setPosY(mScreenY / 2);
            // You might want to set increaseX and increaseY to appropriate initial values


        }

    }


//    private void checkBallHitBrick2() {
//        RectF ballBounds = ball.getBounds();
//
//        for (BreakableBrick brick : breakableBricks) {
//            if (brick.isActive()) {
//                RectF brickBounds = brick.getPosition();
//
//                // Check if the ball intersects with the brick
//                if (RectF.intersects(ballBounds, brickBounds)) {
//                    // Determine which side of the brick the ball hits
//
//
//                    //ADD OR SUBTRACKS TO THE BOUNDS AT THE END OF THE LINE BY 5-10
//                    boolean hitTop = ballBounds.bottom >= brickBounds.top && ballBounds.bottom <= brickBounds.top;
//                    boolean hitBottom = ballBounds.top <= brickBounds.bottom && ballBounds.top >= brickBounds.bottom ;
//                    boolean hitLeft = ballBounds.right >= brickBounds.left && ballBounds.right <= brickBounds.left ;
//                    boolean hitRight = ballBounds.left <= brickBounds.right && ballBounds.left >= brickBounds.right;
//
//                    // Respond to the collision based on the side hit
//                    if (hitTop || hitBottom) {
//                        ball.setIncreaseY(-ball.getIncreaseY()); // Reverse Y direction
//                    }
//                    if (hitLeft || hitRight) {
//                        ball.setIncreaseX(-ball.getIncreaseX()); // Reverse X direction
//                    }
//
//                    // Deactivate the brick
//                    brick.setActive(false);
//                }
//            }
//        }
//    }

    private void checkBallHitBrick() {
        RectF ballBounds = ball.getBounds(); // Get the bounds of the ball

        // Iterate over each breakable brick
        for (BreakableBrick brick : breakableBricks) {
            // Check if the brick is active and intersects with the ball
            if (brick.isActive() && RectF.intersects(ballBounds, brick.getPosition())) { // Handle the collision, for example, mark the brick as inactive or change its color and change ball direction
                hitsLeft--;
                if(brick.getColor()==Color.GREEN)
                    brick.setActive(false);
                brick.hitBrick();
                // Change ball direction
                ball.setIncreaseX(-ball.getIncreaseX());
                ball.setIncreaseY(-ball.getIncreaseY());
                // break the loop- handle only one brick collision per frame
                break;
            }
        }
    }

    private void draw() {


        if (mOurHolder.getSurface().isValid()) {
            mCanvas = mOurHolder.lockCanvas();

            mCanvas.drawColor(Color.argb(255, 0, 0, 0)); // Black background

            // Draw the stats
            drawStats(mCanvas);

            // Draw the walls
            for (Brick b : mBrickList) {
                mPaint.setColor(Color.argb(255, 255, 255, 255)); // White
                b.draw(mCanvas, mPaint);
            }

            if(gameStarted) //we only want the bricks and ball drawn if the game has begun
            {// Draw the breakable bricks
                for (BreakableBrick breakable : breakableBricks) {
                    if (breakable.isActive()) {
                        mPaint.setColor(breakable.getColor());
                        mCanvas.drawRect(breakable.getPosition(), mPaint);
                    }
                }


                // Draw the ball
                mPaint.setColor(Color.argb(255, 255, 255, 255)); // White
                mCanvas.drawCircle(ball.getPosX(), ball.getPosY(), ball.getRadius(), mPaint);
            }

            // Draw the arrow buttons
            mCanvas.drawBitmap(leftButtonBitmap, null, mLeftButtonCoords, mPaint);
            mCanvas.drawBitmap(rightButtonBitmap, null, mRightButtonCoords, mPaint);
            mCanvas.drawBitmap(upButtonBitmap, null, mUpButtonCoords, mPaint);
            mCanvas.drawBitmap(downButtonBitmap, null, mDownButtonCoords, mPaint);

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
        if(!gameStarted)//begin the game if it hasnt been already
            startNewGame();
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

    private void startNewGame() {
        // Reset necessary game parameters
        // Load ball onto paddle
        ball.setPosX(paddle.getPosition().centerX());
        ball.setPosY(paddle.getPosition().top - ball.getRadius());
        ball.setIncreaseX(5f); // Set initial velocity
        ball.setIncreaseY(-5f);

        timer = System.currentTimeMillis();        //start the timer


        gameStarted=true;
    }

    private void drawStats(Canvas canvas) {
        Paint statsPaint = new Paint();
        statsPaint.setColor(Color.WHITE);
        statsPaint.setTextSize(130);
        int verticalMargin = 80;         // Calculate vertical margin or padding for the stats text layout

        if (hitsLeft<100) //append a 0 to the string if the hits left # is less than 3 digits
            hitsLeftStr = "0" + String.valueOf(hitsLeft);
        else hitsLeftStr = String.valueOf(hitsLeft);


        // Draw time elapsed
        canvas.drawText(elapsedTimeString, 20, 50 + verticalMargin, statsPaint);

        // Draw number of hits needed
        canvas.drawText(hitsLeftStr, mScreenX / 2 + 20, 50 + verticalMargin, statsPaint);

        // Draw number of balls left
        canvas.drawText("01", mScreenX - 200, 50 + verticalMargin, statsPaint);
    }



}

