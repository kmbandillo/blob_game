package blobgame;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage){
		GameStage game = new GameStage();
		game.setStage(stage);
	}

}
