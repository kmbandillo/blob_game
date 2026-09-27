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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameStage {
	public static final int WINDOW_HEIGHT = 800;
	public static final int WINDOW_WIDTH = 800;
	public static final int MAP_WIDTH = 2400;
	public static final int MAP_HEIGHT = 2400;
	private Scene scene;
	private Scene splashScene;
	private Scene instructionsScene;
	private Scene aboutScene;
	private Stage stage;
	private Group root;
	private Canvas canvas;
	private GraphicsContext gc;
	private GameTimer gametimer;
	private Canvas scoreCanvas;
	private Button gameMenuBtn;
	private Button gameOverMenuBtn;
	private Button continueBtn;

	private static final String BTN_STYLE_ACTIVE =
		"-fx-background-radius: 50; " +
		"-fx-background-color: #f5f5dc; " +
		"-fx-text-fill: black; " +
		"-fx-font-size: 20px; " +
		"-fx-font-weight: bold; " +
		"-fx-border-radius: 50; " +
		"-fx-border-color: orange, black; " +
		"-fx-border-width: 3px, 1px; " +
		"-fx-cursor: hand;";

	private static final String BTN_STYLE_DISABLED =
		"-fx-background-radius: 50; " +
		"-fx-background-color: #e0e0d0; " +
		"-fx-text-fill: #888888; " +
		"-fx-font-size: 20px; " +
		"-fx-font-weight: bold; " +
		"-fx-border-radius: 50; " +
		"-fx-border-color: #aaaaaa, #888888; " +
		"-fx-border-width: 2px, 1px; " +
		"-fx-opacity: 0.55; " +
		"-fx-cursor: default;";

	//the class constructor
	public GameStage() {
	    this.root = new Group();
	    this.scene = new Scene(root, GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT);
	    this.canvas = new Canvas(GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT);
	    this.gc = this.canvas.getGraphicsContext2D();

	    // In-game Menu button
	    this.gameMenuBtn = new Button("Menu");
	    this.gameMenuBtn.setPrefSize(78, 32);
	    this.gameMenuBtn.setLayoutX(712);
	    this.gameMenuBtn.setLayoutY(6);
	    this.gameMenuBtn.setFocusTraversable(false);
	    this.gameMenuBtn.setStyle(
	        "-fx-background-radius: 20; " +
	        "-fx-background-color: #f5f5dc; " +
	        "-fx-text-fill: black; " +
	        "-fx-font-size: 13px; " +
	        "-fx-font-weight: bold; " +
	        "-fx-border-radius: 20; " +
	        "-fx-border-color: orange, black; " +
	        "-fx-border-width: 2px, 1px; " +
	        "-fx-cursor: hand;"
	    );
	    this.gameMenuBtn.setOnAction(e -> returnToMenu());

	    // Game Over Back to Menu button
	    this.gameOverMenuBtn = new Button("Back to Menu");
	    this.gameOverMenuBtn.setPrefSize(220, 50);
	    this.gameOverMenuBtn.setLayoutX(GameStage.WINDOW_WIDTH / 2.0 - 110);
	    this.gameOverMenuBtn.setLayoutY(540);
	    this.gameOverMenuBtn.setFocusTraversable(false);
	    this.gameOverMenuBtn.setStyle(
	        "-fx-background-radius: 50; " +
	        "-fx-background-color: #f5f5dc; " +
	        "-fx-text-fill: black; " +
	        "-fx-font-size: 18px; " +
	        "-fx-font-weight: bold; " +
	        "-fx-border-radius: 50; " +
	        "-fx-border-color: orange, black; " +
	        "-fx-border-width: 3px, 1px; " +
	        "-fx-cursor: hand;"
	    );
	    this.gameOverMenuBtn.setOnAction(e -> returnToMenu());

	    this.hideGameButtons();
	    this.root.getChildren().addAll(this.canvas, this.gameMenuBtn, this.gameOverMenuBtn);
	}


	//method to add the stage elements
	public void setStage(Stage stage) {
		this.stage = stage;
		this.initSplash(stage);
		this.initInstructions(stage);
		this.initAbout(stage);
		this.stage.setTitle("Blob.io");
		this.stage.setScene(this.splashScene);
		stage.setResizable(false);
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

	    this.continueBtn = new Button("Continue");
	    Button b1 = new Button("New Game");
	    Button b2 = new Button("Instructions");
	    Button b3 = new Button("About");

	    this.continueBtn.setPrefSize(200, 48);
	    b1.setPrefSize(200, 48);
	    b2.setPrefSize(200, 48);
	    b3.setPrefSize(200, 48);

	    this.continueBtn.setFocusTraversable(false);
	    b1.setFocusTraversable(false);
	    b2.setFocusTraversable(false);
	    b3.setFocusTraversable(false);

	    b1.setStyle(BTN_STYLE_ACTIVE);
	    b2.setStyle(BTN_STYLE_ACTIVE);
	    b3.setStyle(BTN_STYLE_ACTIVE);

	    this.continueBtn.setOnAction(e -> continueGame());

	    b1.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	            setGame(stage);		// changes the scene into the game scene
	        }
	    });

	    b2.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	            stage.setScene(instructionsScene);
	        }
	    });

	    b3.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	            stage.setScene(aboutScene);
	        }
	    });

	    vbox.getChildren().add(this.continueBtn);
	    vbox.getChildren().add(b1);
	    vbox.getChildren().add(b2);
	    vbox.getChildren().add(b3);

	    this.updateMenuButtons();

	    return vbox;
	}

	private void initInstructions(Stage stage) {
		StackPane root = new StackPane();
		ImageView bgView = new ImageView(new Image("images/extras_bg.png"));
		bgView.setFitWidth(GameStage.WINDOW_WIDTH);
		bgView.setFitHeight(GameStage.WINDOW_HEIGHT);

		VBox cardContent = new VBox();
		cardContent.setAlignment(Pos.CENTER);
		cardContent.setMaxWidth(460);
		cardContent.setMaxHeight(500);
		cardContent.setSpacing(6);
		cardContent.setPadding(new Insets(6, 16, 6, 16));

		Label title = new Label("HOW TO PLAY");
		title.setFont(Font.font("Century Gothic", FontWeight.BOLD, 28));
		title.setTextFill(Color.rgb(45, 38, 35));

		VBox rulesBox = new VBox(5);
		rulesBox.setAlignment(Pos.CENTER_LEFT);
		rulesBox.setPadding(new Insets(2, 6, 2, 6));

		rulesBox.getChildren().add(createInstructionItem("• CONTROLS", "Move your blob using W, A, S, D or Arrow keys."));

		// Food & Power-ups Section
		Label foodHeader = new Label("• FOOD & POWER-UPS");
		foodHeader.setFont(Font.font("Century Gothic", FontWeight.BOLD, 12));
		foodHeader.setTextFill(Color.rgb(45, 38, 35));

		VBox foodList = new VBox(3);
		foodList.setPadding(new Insets(2, 0, 2, 8));
		foodList.getChildren().add(createFoodItem(new Image("images/cookie.png"), "Cookie (Regular Food)", "Eat cookies scattered around the map to grow (+10 size each)."));
		foodList.getChildren().add(createFoodItem(new Image("images/hotchoco.png"), "Hot Choco (Speed Power-up)", "Gives Speed Boost! Doubles your movement speed for 5 seconds."));
		foodList.getChildren().add(createFoodItem(new Image("images/peanut.png"), "Peanut (Immunity Power-up)", "Gives Immunity! Makes you invulnerable to larger enemies for 5 seconds."));

		VBox foodSection = new VBox(2, foodHeader, foodList);
		rulesBox.getChildren().add(foodSection);

		rulesBox.getChildren().add(createInstructionItem("• HUNT & SURVIVE", "Absorb smaller blobs to grow. Evade larger enemies!"));
		rulesBox.getChildren().add(createInstructionItem("• SPLIT BLOB (SPACE)", "Press Spacebar to split (size ≥ 80). Overlapping blobs re-absorb."));

		Button backBtn = new Button("Back to Menu");
		backBtn.setPrefSize(180, 38);
		backBtn.setFocusTraversable(false);
		backBtn.setStyle(
			"-fx-background-radius: 50; " +
			"-fx-background-color: #f5f5dc; " +
			"-fx-text-fill: black; " +
			"-fx-font-size: 16px; " +
			"-fx-font-weight: bold; " +
			"-fx-border-radius: 50; " +
			"-fx-border-color: orange, black; " +
			"-fx-border-width: 3px, 1px; " +
			"-fx-cursor: hand;"
		);
		backBtn.setOnAction(e -> {
			this.updateMenuButtons();
			this.stage.setScene(this.splashScene);
		});

		cardContent.getChildren().addAll(title, rulesBox, backBtn);
		root.getChildren().addAll(bgView, cardContent);
		this.instructionsScene = new Scene(root, GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT);
	}

	private VBox createInstructionItem(String header, String desc) {
		VBox item = new VBox(1);
		Label headLbl = new Label(header);
		headLbl.setFont(Font.font("Century Gothic", FontWeight.BOLD, 12));
		headLbl.setTextFill(Color.rgb(45, 38, 35));

		Label descLbl = new Label(desc);
		descLbl.setFont(Font.font("Century Gothic", FontWeight.NORMAL, 11));
		descLbl.setTextFill(Color.rgb(70, 64, 60));
		descLbl.setWrapText(true);
		descLbl.setMaxWidth(420);

		item.getChildren().addAll(headLbl, descLbl);
		return item;
	}

	private HBox createFoodItem(Image icon, String name, String desc) {
		HBox row = new HBox(8);
		row.setAlignment(Pos.CENTER_LEFT);

		ImageView iconView = new ImageView(icon);
		iconView.setFitWidth(22);
		iconView.setFitHeight(22);
		iconView.setPreserveRatio(true);

		VBox textCol = new VBox(0);
		Label nameLbl = new Label(name);
		nameLbl.setFont(Font.font("Century Gothic", FontWeight.BOLD, 11));
		nameLbl.setTextFill(Color.rgb(45, 38, 35));

		Label descLbl = new Label(desc);
		descLbl.setFont(Font.font("Century Gothic", FontWeight.NORMAL, 11));
		descLbl.setTextFill(Color.rgb(70, 64, 60));
		descLbl.setWrapText(true);
		descLbl.setMaxWidth(390);

		textCol.getChildren().addAll(nameLbl, descLbl);
		row.getChildren().addAll(iconView, textCol);
		return row;
	}

	private void initAbout(Stage stage) {
		StackPane root = new StackPane();
		ImageView bgView = new ImageView(new Image("images/extras_bg.png"));
		bgView.setFitWidth(GameStage.WINDOW_WIDTH);
		bgView.setFitHeight(GameStage.WINDOW_HEIGHT);

		VBox cardContent = new VBox();
		cardContent.setAlignment(Pos.CENTER);
		cardContent.setMaxWidth(440);
		cardContent.setMaxHeight(480);
		cardContent.setSpacing(14);
		cardContent.setPadding(new Insets(10, 20, 10, 20));

		Label title = new Label("ABOUT");
		title.setFont(Font.font("Century Gothic", FontWeight.BOLD, 34));
		title.setTextFill(Color.rgb(45, 38, 35));

		VBox infoBox = new VBox(8);
		infoBox.setAlignment(Pos.CENTER);

		Label nameLbl = new Label("BLOB GAME");
		nameLbl.setFont(Font.font("Century Gothic", FontWeight.BOLD, 20));
		nameLbl.setTextFill(Color.rgb(45, 38, 35));

		Label devLbl = new Label("Developer: Kimberly Bandillo\nGithub: kmbandillo");
		devLbl.setFont(Font.font("Century Gothic", FontWeight.NORMAL, 14));
		devLbl.setTextFill(Color.rgb(70, 64, 60));
		devLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

		Label refLbl = new Label("References:\n• CMSC22 Base Code\n• Original Assets in src/images");
		refLbl.setFont(Font.font("Century Gothic", FontWeight.NORMAL, 13));
		refLbl.setTextFill(Color.rgb(90, 84, 80));
		refLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

		infoBox.getChildren().addAll(nameLbl, devLbl, refLbl);

		Button backBtn = new Button("Back to Menu");
		backBtn.setPrefSize(200, 46);
		backBtn.setFocusTraversable(false);
		backBtn.setStyle(
			"-fx-background-radius: 50; " +
			"-fx-background-color: #f5f5dc; " +
			"-fx-text-fill: black; " +
			"-fx-font-size: 18px; " +
			"-fx-font-weight: bold; " +
			"-fx-border-radius: 50; " +
			"-fx-border-color: orange, black; " +
			"-fx-border-width: 3px, 1px; " +
			"-fx-cursor: hand;"
		);
		backBtn.setOnAction(e -> {
			this.updateMenuButtons();
			this.stage.setScene(this.splashScene);
		});

		cardContent.getChildren().addAll(title, infoBox, backBtn);
		root.getChildren().addAll(bgView, cardContent);
		this.aboutScene = new Scene(root, GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT);
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


	public void returnToMenu() {
		if (this.gametimer != null) {
			this.gametimer.pauseGame();
		}
		this.hideGameButtons();
		this.updateMenuButtons();
		this.stage.setScene(this.splashScene);
	}

	public void continueGame() {
		if (this.gametimer != null && this.gametimer.canContinue()) {
			this.stage.setScene(this.scene);
			this.showGameplayButton();
			this.gametimer.resumeGame();
		} else {
			this.setGame(this.stage);
		}
	}

	public void updateMenuButtons() {
		boolean canCont = this.gametimer != null && this.gametimer.canContinue();
		if (this.continueBtn != null) {
			this.continueBtn.setDisable(!canCont);
			this.continueBtn.setStyle(canCont ? BTN_STYLE_ACTIVE : BTN_STYLE_DISABLED);
		}
	}

	public void showGameplayButton() {
		this.gameMenuBtn.setVisible(true);
		this.gameOverMenuBtn.setVisible(false);
	}

	public void showGameOverButton() {
		this.gameMenuBtn.setVisible(false);
		this.gameOverMenuBtn.setVisible(true);
		this.updateMenuButtons();
	}

	public void hideGameButtons() {
		this.gameMenuBtn.setVisible(false);
		this.gameOverMenuBtn.setVisible(false);
	}

    void setGame(Stage stage) {
    	this.stage = stage;

		if (this.gametimer != null) {
			this.gametimer.stop();
		}

		this.gc.clearRect(0, 0, GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT);
		this.gametimer = new GameTimer(this.gc, this.scene, this.stage, this);

        if (!this.root.getChildren().contains(canvas)) {
            this.root.getChildren().add(canvas);
        }

		this.showGameplayButton();
        stage.setScene(this.scene);

        this.gametimer.start();			// this internally calls the handle() method of our GameTimer
        this.updateMenuButtons();
	}
}

