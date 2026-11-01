package com.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A vertical wall that spans the playable court.
 */ 
public final class Wall {
    private static final double DEFAULT_X = 400;

    private double x = DEFAULT_X;
    private double height;

    public void setHeight(double height) {
        this.height = Math.max(0, height);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = Math.max(0, x);
    }

    public double getHeight() {
        return height;
    }

    public boolean hits(Ball ball) {
        double radius = ball.getRadius();
        return ball.getVelocityX() > 0 && ball.getX() + radius >= x;
    }

    // render the wall
    public void render(GraphicsContext graphics) {
        graphics.setStroke(Color.web("#ff826e"));
        graphics.setLineWidth(4);
        graphics.strokeLine(x, 0, x, height);
    }
}