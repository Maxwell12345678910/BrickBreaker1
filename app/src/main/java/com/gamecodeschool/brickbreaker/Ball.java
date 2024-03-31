package com.gamecodeschool.brickbreaker;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Ball {
    private float posX, posY; // Ball position
    private float increaseX, increaseY; // Ball movement increments, must be
    private float radius; // Ball radius

    public Ball(float posX, float posY, float increaseX, float dy, float radius) {
        this.posX = posX;
        this.posY = posY;
        this.increaseX = increaseX;
        this.increaseY = dy;
        this.radius = radius;
    }

    public void update() {
        // Update ball position based on dx and dy
        posX += increaseX;
        posY += increaseY;
    }

    public void draw(Canvas canvas, Paint paint) {
        // Draw the ball on the canvas
        canvas.drawCircle(posX, posY, radius, paint);
    }

    // Getter and setter methods for position, velocity, and radius
    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getIncreaseX() {
        return increaseX;
    }

    public void setIncreaseX(float increaseX) {
        this.increaseX = increaseX;
    }

    public float getIncreaseY() {
        return increaseY;
    }

    public void setIncreaseY(float increaseY) {
        this.increaseY = increaseY;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }


    //returns a rectangle that represents the bounds of the ball itself
    public RectF getBounds() {
        return new RectF(posX - radius, posY - radius, posX + radius, posY + radius);
    }
}
