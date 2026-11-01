package com.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The player's paddle
 */
public final class Player {
    private static final double WIDTH = 13;
    private static final double HEIGHT = 104;
    private static final double SPEED = 440;
    private static final double X = 34;
    private static final Color COLOR = Color.web("#67f5c5");

    private double y;

    // reset paddle, position to middle of the screen
    public void reset(double courtHeight) {
        y = Math.max(0, (courtHeight - HEIGHT) / 2);
    }

    // move the paddle up or down
    public void update(double elapsed, double courtHeight, boolean moveUp, boolean moveDown) {
        if (moveUp) {
            y -= SPEED * elapsed;
        }
        if (moveDown) {
            y += SPEED * elapsed;
        }

        // update vertical position, max and min prevent the paddle from
        // going off the screen
        y = Math.max(0, Math.min(Math.max(0, courtHeight - HEIGHT), y));
    }

    // draw the paddle
    public void render(GraphicsContext graphics) {
        graphics.setFill(COLOR);
        graphics.fillRoundRect(X, y, WIDTH, HEIGHT, 9, 9);
    }

    /**
     * Returns true if the paddle hits the ball
     * @param ballX the x position of the ball
     * @param ballY the y position of the ball
     * @param radius the radius of the ball
     * @return true if the player hits the ball
     */
    public boolean hits(double ballX, double ballY, double radius) {
        return ballX - radius <= X + WIDTH  
                && ballX + radius >= X
                && ballY + radius >= y
                && ballY - radius <= y + HEIGHT;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getCenterY() {
        return y + HEIGHT / 2;
    }

    public double getRightEdge() {
        return X + WIDTH;
    }

    public double getHeight() {
        return HEIGHT;
    }

}