package com.example;

import java.util.Set;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/** Game rules, court rendering, and the objects that make up a Pong board. */
public final class Board {
    private final Player player = new Player();
    private final Ball ball = new Ball();
    private final Wall wall = new Wall();
    private double width;
    private double height;
    private int rally;
    private boolean gameOver;
    private boolean initialized;

    /**
     * Resets the board
     * @param courtWidth the width of the court
     * @param courtHeight the height of the court
     */
    public void reset(double courtWidth, double courtHeight) {
        setSize(courtWidth, courtHeight);        
        rally = 0;
        gameOver = false;
        player.reset(height);
        ball.reset(Math.min(width / 2, wall.getX() / 2), height / 2);
    }

    /**
     * Update the board
     * @param elapsed the elapsed time
     * @param courtWidth the width of the court
     * @param courtHeight the height of the court
     * @param keysDown set consisting of what keys have been pressed
     */
    public void update(double elapsed, double courtWidth, double courtHeight, Set<KeyCode> keysDown) {
        setSize(courtWidth, courtHeight);
        if (gameOver || elapsed <= 0) {
            return;
        }

        // update player position (moveUp for w/up arrow and moveDown for S/down arrow)
        player.update(elapsed, height,
                keysDown.contains(KeyCode.W) || keysDown.contains(KeyCode.UP),
                keysDown.contains(KeyCode.S) || keysDown.contains(KeyCode.DOWN));

        // move the ball
        ball.move(elapsed, height);

        // check if the ball hits the wall
        double radius = ball.getRadius();    
        if (wall.hits(ball)) {
            ball.setX(wall.getX() - radius);
            ball.bounceOffWall();
        }

        // check if the player hits the ball
        if (ball.getVelocityX() < 0 && player.hits(ball.getX(), ball.getY(), radius)) {
            ball.placeJustRightOfPlayer(player.getRightEdge());
            ball.bounceOffPlayer(player.getCenterY(), player.getHeight());
            rally++;
        } else if (ball.getX() + radius < 0) {
            gameOver = true;
        }

    }

    // render the board
    public void render(GraphicsContext graphics, double courtWidth, double courtHeight) {
        setSize(courtWidth, courtHeight);
        
        graphics.setFill(Color.web("#09131d"));
        graphics.fillRect(0, 0, width, height);

        graphics.setStroke(Color.web("#29404c"));
        graphics.setLineWidth(2);
        graphics.setLineDashes(8, 12);        
        graphics.setLineDashes(null);

        wall.render(graphics);
        player.render(graphics);
        ball.render(graphics);
    }

    public int getRally() {
        return rally;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Set board size, forcing a minimum of 40 width x 1 height
     * The size is only updated if this is the first time the method
     * is called or if the court has been re-sized.
     * @param courtWidth the court width
     * @param courtHeight the court height
     */
    private void setSize(double courtWidth, double courtHeight) {
        
        double newWidth = Math.max(40, courtWidth);
        double newHeight = Math.max(1, courtHeight);
        if (!initialized || width <= 1 || height <= 1) {
            width = newWidth;
            height = newHeight;
            wall.setHeight(height);
            wall.setX(width - 30);
            player.reset(height);
            ball.reset(Math.min(width / 2, wall.getX() / 2), height / 2);
            initialized = true;
        } else if (newWidth != width || newHeight != height) {
            double xScale = newWidth / width;
            double yScale = newHeight / height;
            width = newWidth;
            height = newHeight;
            wall.setHeight(height);
            wall.setX(width - 30);
            player.setY(player.getY() * yScale);
            ball.setPosition(ball.getX() * xScale, ball.getY() * yScale);
        }
    }
}