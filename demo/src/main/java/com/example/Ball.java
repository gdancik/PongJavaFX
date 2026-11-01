package com.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The ball and its motion on the court
 */
public final class Ball {

    
    // radius of the ball
    private static final double RADIUS = 9;

    // initial speed of the ball
    private static final double INITIAL_SPEED = 360;

    // current (x,y) position
    private double x;
    private double y;

    // current (x,y) velocity
    private double velocityX;
    private double velocityY;

    /**
     * Resets the ball
     * @param centerX x position
     * @param centerY y position
     */
    public void reset(double centerX, double centerY) {
        x = centerX;
        y = centerY;

        // full speed on x-axis; up to 35% speed (up/down) on y-axis
        velocityX =  INITIAL_SPEED;
        velocityY = (Math.random() * 2 - 1) * INITIAL_SPEED * 0.35;
    }

    /**
     * Move based on elapsed time
     * @param elapsed
     */
    public void move(double elapsed, double height) {
        x += velocityX * elapsed;
        y += velocityY * elapsed;

        // bounce vertically        
        if (y - RADIUS <= 0) {
            y = RADIUS;
            bounceVertically();
        } else if (y + RADIUS >= height) {
            y = height - RADIUS;
            bounceVertically();
        }
        
    }

    // vertical bounce negates y-axis velocity
    public void bounceVertically() {
        velocityY = -velocityY;
    }

    // 
    public void bounceOffPlayer(double playerCenterY, double playerHeight) {

        // set impact to be between +/- 1, based on offset from the center
        // of the paddle
        double impact = (y - playerCenterY) / (playerHeight / 2);

        // calculate speed
        double speed = Math.min(Math.hypot(velocityX, velocityY) * 1.055, 760);
        
        // convert impact to radians
        double angle = impact * Math.toRadians(58);

        // update velocities
        velocityX = Math.abs(speed * Math.cos(angle));
        velocityY = speed * Math.sin(angle);
    }

    // horizontal bounce negates x-axis velocity
    public void bounceOffWall() {
        velocityX = -Math.abs(velocityX);
    }

    // update x position so ball is just right of player 
    // (prevents ball from going through the paddle)
    public void placeJustRightOfPlayer(double playerRightEdge) {
        x = playerRightEdge + RADIUS + 0.5;
    }

    // draws the paddle
    public void render(GraphicsContext graphics) {
        graphics.setFill(Color.WHITE);
        graphics.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return RADIUS;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}