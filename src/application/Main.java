package application;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

class Bird extends ImageView {
	private double velocity = 0;
	private final double GRAVITY = 0.2;
	private final double JUMP_STRENGTH = -5;
	private boolean isJumping = false;

	public Bird(Image image) {
		super(image);
		setFitWidth(30);
		setFitHeight(30);
		setLayoutX(100);
		setLayoutY(200);
	}

	public void jump() {
		isJumping = true;
		velocity = JUMP_STRENGTH;

		RotateTransition rotateTransition = new RotateTransition(Duration.seconds(0.3), this);
		rotateTransition.setToAngle(-40);
		rotateTransition.setInterpolator(Interpolator.EASE_OUT);
		rotateTransition.play();
	}

	public void update() {
		if (!isJumping) {
			RotateTransition rotateTransition = new RotateTransition(Duration.seconds(0.3), this);
			rotateTransition.setToAngle(50);
			rotateTransition.setInterpolator(Interpolator.EASE_OUT);
			rotateTransition.play();
		}

		velocity += GRAVITY;
		setLayoutY(getLayoutY() + velocity);
		setLayoutY(Math.max(getLayoutY(), 0));
	}

	public void setJumping(boolean jumping) {
		isJumping = jumping;
	}

	public boolean isJumping() {
		return isJumping;
	}
}

class Pipe extends ImageView {

	public int pipeX = 360;
	public int pipeY = 0;
	int pipeWidth = 64;
	int pipeHeiht = 512;
	public int velocityX = -2;
	public int velocacityY = 0;
	boolean passed = false;

	Pipe(Image img) {
		super(img);
		setFitHeight(pipeHeiht);
		setFitWidth(pipeWidth);
		setLayoutX(pipeX);
		setLayoutY(pipeY);
	}

	public void update() {
		setLayoutX(getLayoutX() + velocityX);
	}
}

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	ArrayList<Pipe> pipes;
	private Bird bird;
	private ImageView backgroundImg;

	int boardWidth = 360;
	int boardHeight = 640;

	AnimationTimer placePipeTimer;

	boolean gameOver = false;
	double score = 0;

	Label lblscore;
	Label lblnotify;

	Long placePipeTick = (long) 1200000000;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Pane root = new Pane();
		Scene scene = new Scene(root, boardWidth, boardHeight);
		scene.getStylesheets().add("application/application.css");

		lblscore = new Label();
		lblscore.getStyleClass().add("lblscore");
		String s = "Score: " + score;
		lblscore.setText(s);

		lblnotify = new Label();
		lblnotify.getStyleClass().add("lblnotify");
		lblnotify.setLayoutY(boardHeight / 2 - 50);
		lblnotify.setLayoutX(boardWidth / 2 - 120);
		// Load Images

		backgroundImg = new ImageView(new Image(getClass().getResourceAsStream("flappybirdbg.png")));
		backgroundImg.setFitWidth(boardWidth);
		backgroundImg.setFitHeight(boardHeight);

		bird = new Bird(new Image(getClass().getResourceAsStream("birds.png")));
		root.getChildren().addAll(lblscore, lblnotify);
		root.getChildren().addAll(backgroundImg, bird);
		pipes = new ArrayList<>();

		backgroundImg.toBack();

		scene.setOnKeyReleased(ev -> {
			if (ev.getCode() == KeyCode.SPACE) {
				bird.setJumping(false);
			}
		});

		AnimationTimer gameloop = new AnimationTimer() {
			@Override
			public void handle(long arg0) {
				handleGameLoop();
				String s = "Score: " + score;
				lblscore.setText(s);
				if (gameOver) {
					this.stop();
					placePipeTimer.stop();
				}
			}
		};

		scene.setOnKeyPressed(ev -> {
			if (ev.getCode() == KeyCode.SPACE && !bird.isJumping()) {
				bird.jump();
			}
			// reset game
			if (gameOver) {
				resetGame(root);
				gameloop.start();
				placePipeTimer.start();
			}
		});
		placePipeTimer = new AnimationTimer() {
			long lastTime = 0;

			@Override
			public void handle(long now) {
				if (now - lastTime >= placePipeTick) { // 5_000_000_00 nanoseconds = 0.5 seconds
					placePipe(root);
					lastTime = now;
				}
			}
		};
		placePipeTimer.start();
		gameloop.start();
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public void placePipe(Pane root) {
		int randomPipeY = (int) (0 - 512 / 4 - Math.random() * (512 / 2)); // pipeY - pipeHeight / 4 - Math.random() *
																			// (pipeHeight / 2
		int openingSpace = boardHeight / 4;

		Pipe topPipe = new Pipe(new Image(getClass().getResourceAsStream("toppipe.png")));
		topPipe.setLayoutY(randomPipeY);
		pipes.add(topPipe);

		Pipe bottomPipe = new Pipe(new Image(getClass().getResourceAsStream("bottompipe.png")));
		bottomPipe.setLayoutY(topPipe.getLayoutY() + topPipe.getFitHeight() + openingSpace);
		pipes.add(bottomPipe);

		root.getChildren().addAll(topPipe, bottomPipe);
	}

	public void handleGameLoop() {

		bird.update();

		Iterator<Pipe> iterator = pipes.iterator();
		while (iterator.hasNext()) {
			Pipe pipe = iterator.next();
			pipe.update();
			if (!pipe.passed && bird.getLayoutX() > pipe.getLayoutX() + pipe.getFitWidth()) {
				score += 0.5;
				pipe.passed = true;
				System.out.println(score);
			}
			if (collision(bird, pipe)) {
				gameOver = true;
				String notify = "Game Over! Ur score: " + score + "\n Press SPACE to play again";
				lblnotify.setText(notify);
				lblnotify.toFront();
			}
			if (bird.getLayoutY() > boardHeight) {
				gameOver = true;
				String notify = "Game Over! Ur score: " + score + "\nPress SPACE to play again!!!";
				lblnotify.setText(notify);
				lblnotify.toFront();
			}
			if (score > 10) {
				placePipeTick = (long) 900000000;
			}
			if (pipe.getLayoutX() < -70) {
				iterator.remove();
			}
		}
	}

	boolean collision(Bird a, Pipe b) {
		return a.getLayoutX() < b.getLayoutX() + b.getFitWidth() && a.getLayoutX() + a.getFitWidth() > b.getLayoutX()
				&& a.getLayoutY() < b.getLayoutY() + b.getFitHeight()
				&& a.getLayoutY() + a.getFitHeight() > b.getLayoutY();
	}

	public void resetGame(Pane root) {
		pipes.clear();
		root.getChildren().clear();
		root.getChildren().addAll(backgroundImg, bird);
		root.getChildren().addAll(lblscore, lblnotify);
		lblnotify.setText("");
		backgroundImg.toBack();
		gameOver = false;
		score = 0;
		bird.setLayoutY(200);
	}

}
