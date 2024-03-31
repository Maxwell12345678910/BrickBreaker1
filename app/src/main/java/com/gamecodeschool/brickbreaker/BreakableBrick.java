package com.gamecodeschool.brickbreaker;

import android.graphics.Color;
import android.graphics.RectF;

public class BreakableBrick {

    private RectF position;
    private int color;
    private boolean breakable;
    private boolean hit;

    // Constants for brick colors
    public static final int COLOR_GREEN = Color.GREEN;
    public static final int COLOR_YELLOW = Color.YELLOW;
    public static final int COLOR_RED = Color.RED;

    // Constructor
    public BreakableBrick(RectF position, int color, boolean breakable) {
        this.position = position;
        this.color = color;
        this.breakable = breakable;
        this.hit = false;
    }

    // Getter methods
    public RectF getPosition() {
        return position;
    }

    public int getColor() {
        return color;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public boolean isHit() {
        return hit;
    }

    // Method to handle brick hit
    public void hitBrick() {
        if (breakable && !hit) {
            // If the brick is breakable and hasn't been hit before
            // Change its color and mark it as hit
            color = COLOR_YELLOW;
            hit = true;
        } else if (!breakable) {
            // If the brick is not breakable, change its color to the next level
            if (color == COLOR_YELLOW) {
                color = COLOR_GREEN;
            } else if (color == COLOR_RED) {
                color = COLOR_YELLOW;
            }
        }
    }
}


//if a brick is hit change its color- also decrement hitsLeft.
//if the brick is green, remove it from the array that draws the bricks to the screen ,