import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

import static javafx.animation.PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Creates a JavaFX application that models a "Catch the Mouse" game.
 *
 * The user is displayed a game board on which a mouse sprite is moving in random directions.
 * A point is added to the score if the user is able to click the sprite.
 *
 * @author  Denis Cokanovic
 * @since   04.02.2021
 */
public class App extends Application {
    private final Random rand = new Random();

    private final double MOUSE_WIDTH = 50.0;
    private final double MOUSE_HEIGHT = 50.0;
    private final double ANIMATION_SPEED = 1000.0;

    private AnchorPane paneBoard;
    private Label labelScore;
    private Rectangle rectMouse;

    private int score;
    private long startTime;

    @Override
    public void start(Stage stage) {
        /* Setup nodes */
        rectMouse = new Rectangle(MOUSE_WIDTH, MOUSE_HEIGHT, new ImagePattern(new Image(getClass().getResourceAsStream("mouse.png"))));
        labelScore = new Label();
        labelScore.setText(String.valueOf(score));
        labelScore.setPrefHeight(50.0);
        labelScore.setTextFill(Paint.valueOf("341948"));
        labelScore.setStyle("-fx-font-family: Consolas, sans-serif; -fx-font-size: 40px; -fx-font-weight: bold");

        HBox menu = new HBox(new Region(), labelScore, new Region());
        menu.setBackground(new Background(new BackgroundFill(Paint.valueOf("F4B9B8"), null, null)));
        menu.setBorder(new Border(new BorderStroke(null, null, Paint.valueOf("887BB0"), null, null, null, BorderStrokeStyle.SOLID, null, null, BorderStroke.THIN, null)));
        menu.setPrefHeight(50.0);
        menu.setAlignment(CENTER);

        paneBoard = new AnchorPane(rectMouse);
        paneBoard.setBackground(new Background(new BackgroundFill(Paint.valueOf("FFF4BD"), null, null)));
        VBox.setVgrow(paneBoard, ALWAYS);

        /* Setup root node */
        VBox root = new VBox(menu, paneBoard);
        root.setFillWidth(true);

        /* Setup the stage & scene */
        stage.setScene(new Scene(root, 600.0, 600.0));  // Hard-coded window size
        stage.setResizable(false);
        stage.setTitle("Catch the Mouse");
        stage.show();

        /* Initialize the game loop */
        awakenMouse(ANIMATION_SPEED);
        startTime = System.currentTimeMillis(); // Used to track elapsed time from game start

        /* Start thread that checks if 30 seconds have elapsed */
        Thread thread = new Thread(() -> {
            while(true) {
                if (System.currentTimeMillis() - startTime >= 30000) {
                    System.out.println("You got a score of " + score + " in 30 seconds!");

                    break;
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    private void awakenMouse(double duration) {
        double xCurrent = rectMouse.getTranslateX() + rectMouse.getWidth() / 2;
        double yCurrent = rectMouse.getTranslateY() + rectMouse.getHeight() / 2;
        double xNew = generateX();
        double yNew = generateY();

        /* Setup the movement animation */
        PathTransition transition = new PathTransition();

        rectMouse.setOnMouseClicked(e -> {
            transition.stop();

            labelScore.setText(String.valueOf(++score));

            awakenMouse(1.0);   // Teleport the mouse to a new position if it's been clicked
        });

        transition.setDuration(Duration.millis(duration));
        transition.setNode(rectMouse);
        transition.setPath(new Path(new MoveTo(xCurrent, yCurrent), new LineTo(xNew, yNew)));
        transition.setOrientation(ORTHOGONAL_TO_TANGENT);   // Mouse rotates in relation to the direction it's moving
        transition.setOnFinished(e -> awakenMouse(ANIMATION_SPEED));
        transition.play();
    }

    /*
    * The following two methods generate a random x-coordinate and y-coordinate respectively
    * such that the mouse node can be repositioned and not be clipped on the edge of the window.
    */

    private double generateX() {
        double x = Math.min(rand.nextInt((int) Math.floor(paneBoard.getWidth() + 1)),
                paneBoard.getWidth() - rectMouse.getWidth() / 2);

        return Math.max(x, rectMouse.getWidth() / 2);
    }

    private double generateY() {
        double y = Math.min(rand.nextInt((int) Math.floor(paneBoard.getHeight() + 1)),
                paneBoard.getHeight() - rectMouse.getHeight() / 2);

        return Math.max(y, rectMouse.getHeight() / 2);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
