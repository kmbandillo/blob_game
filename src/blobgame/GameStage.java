package blobgame;


import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameStage {
	public static final int WINDOW_HEIGHT = 800;
	public static final int WINDOW_WIDTH = 800;
	private Scene scene;
	private Scene splashScene;
	private Stage stage;
	private Group root;
	private Canvas canvas;
	private GraphicsContext gc;
	private GameTimer gametimer;
	private Canvas scoreCanvas;

	//the class constructor
	public GameStage() {
	    this.root = new Group();
	    this.scene = new Scene(root, GameStage.WINDOW_WIDTH,GameStage.WINDOW_HEIGHT);
	    this.canvas = new Canvas(GameStage.WINDOW_WIDTH,GameStage.WINDOW_HEIGHT);
	    this.gc = this.canvas.getGraphicsContext2D();
	    Image bg = new Image("images/back.png");
	    ImageView view = new ImageView(bg);
	    root.getChildren().add(view);

	    //instantiate an animation timer
	    this.gametimer = new GameTimer(this.gc,this.scene, this.stage);
	}


	//method to add the stage elements
	public void setStage(Stage stage) {
		this.stage = stage;
		this.initSplash(stage);
		this.stage.setTitle("Blob.io");
		this.stage.setScene(this.splashScene);
		stage.setResizable(false);
		//invoke the start method of the animation timer
//		this.gametimer.start();

		this.stage.show();
	}

	private void initSplash(Stage stage) {
		StackPane root = new StackPane();
		ImageView imgView = this.createView();
        root.getChildren().addAll(imgView,this.createVBox());
        this.splashScene = new Scene(root);
	}

	private ImageView createView() {
	    // Create a pane for the splash screen
		Image bg = new Image("images/bg.gif");
        ImageView view = new ImageView();
        view.setImage(bg);

        return view;
	}

	private VBox createVBox() {
	    VBox vbox = new VBox();
	    vbox.setAlignment(Pos.CENTER);
	    vbox.setPadding(new Insets(10));
	    vbox.setSpacing(8);

	    Button b1 = new Button("New Game");
	    Button b2 = new Button("Instructions");
	    Button b3 = new Button("About");

	    b1.setPrefSize(200, 50);
	    b2.setPrefSize(200, 50);
	    b3.setPrefSize(200, 50);

	    b1.setStyle("-fx-background-radius: 50; -fx-background-color: #f5f5dc; -fx-text-fill: black; -fx-font-size: 20px; -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-radius: 50; -fx-border-color: orange, black; -fx-border-width: 3px, 1px;");
	    b2.setStyle("-fx-background-radius: 50; -fx-background-color: #f5f5dc; -fx-text-fill: black; -fx-font-size: 20px; -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-radius: 50; -fx-border-color: orange, black; -fx-border-width: 3px, 1px;");
	    b3.setStyle("-fx-background-radius: 50; -fx-background-color: #f5f5dc; -fx-text-fill: black; -fx-font-size: 20px; -fx-font-size: 20px; -fx-font-weight: bold; -fx-border-radius: 50; -fx-border-color: orange, black; -fx-border-width: 3px, 1px;");

	    vbox.getChildren().add(b1);
	    vbox.getChildren().add(b2);
	    vbox.getChildren().add(b3);

	      b1.setOnAction(new EventHandler<ActionEvent>() {
	          @Override
	          public void handle(ActionEvent e) {
	              setGame(stage);		// changes the scene into the game scene
	          }
	      });

	    b2.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	            showInfoWindow("Instructions",
	                "Move with WASD. Eat food and smaller blobs to grow. " +
	                "Avoid larger enemies, and use power-ups when they appear.");
	        }
	    });

	    b3.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	            showInfoWindow("About",
	                "Blob Game\n\n" +
	                "Developer: kmbandillo\n" +
	                "Email: kimmbandillo@gmail.com\n\n" +
	                "References:\n" +
	                "- CMSC22 base code\n" +
	                "- Project image assets in src/images");
	        }
	    });

	    return vbox;
	}

	private void showInfoWindow(String title, String message) {
	    Stage infoStage = new Stage();
	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));
	    Label label = new Label(message);
	    label.setWrapText(true);
	    label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    Button closeButton = new Button("Close");
	    closeButton.setOnAction(event -> infoStage.close());
	    layout.getChildren().addAll(label, closeButton);
	    Scene infoScene = new Scene(layout, 360, 240);
	    infoStage.setTitle(title);
	    infoStage.setScene(infoScene);
	    infoStage.setResizable(false);
	    infoStage.show();
	}

    public void setScene(Scene scene) {
        this.scene = scene;
    }
    public Scene getScene() {
        return this.scene;
    }


    void setGame(Stage stage) {
    	this.stage = stage;

        this.root.getChildren().add(canvas);
        stage.setScene(this.scene);

        this.gametimer.start();			// this internally calls the handle() method of our GameTimer

	}


}

