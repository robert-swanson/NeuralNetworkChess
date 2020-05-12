package application;

import chess.ai.Tester;
import chess.board.RuleSet.GameMode;
import chess.ai.AI;
import chess.board.Board;
import chess.board.Move;
import chess.board.Point;
import chess.board.State;
import chess.pieces.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Manages the graphics and user actions
 * @author Robert Swanson
 */
public class App extends Application {


	//Constants
	final double Animation_Duration = .2; 
	final int stop = -1;

	//Gameplay
	double step;
	Board board;
	SimpleBooleanProperty allowance;
	ArrayList<Transition> animations;
	SimpleDoubleProperty progress;
	Move aisMove;
	Status AIStatus;
	CheckBox castleWL;
	CheckBox castleWR;
	CheckBox castleBL;
	CheckBox castleBR;

	ArrayList<Integer> histHash = new ArrayList<>();

	Tester tester;

	//UI
	BorderPane masterLayout;
	StackPane layout;
	Canvas canvas;
	BorderPane timerPanel;
	Stage window;
	Messages messages;
	HashMap<Point, ImageView> whiteIcons;
	HashMap<Point, ImageView> blackIcons;
	HBox buttons;
	Label topTime;
	Label botTime;
	SimpleIntegerProperty turnTimeD;
	StackPane clock;
	ImageView hand;
	RotateTransition timer;
	Timeline tl;
	Label turnTime;
	ImageView pauseTime;
	boolean paused = false;
	ImageView coverPause;
	ImageView stopStart;
	Line turnIndicator;	
	Point selected;
	boolean editing = false;
	Piece draggedPiece = null;
	ImageView draggedIcon = null;
	double[] draggedCoords = new double[2];
	double[] mouseCoords = new double[2];

	TrainingView trainingView;

	public static void main(String[] args){
		launch(args);
	}
	@Override
	public void stop() throws Exception {
		allowance.set(false);
		System.out.println("Terminating Program");
		super.stop();
	}

	@Override
	/**
	 * Sets up the primary stage with the board in starting position
	 */
	public void start(Stage primaryStage) throws Exception {
		allowance = new SimpleBooleanProperty(true);
		progress = new SimpleDoubleProperty(0.0);
		tester = new Tester(progress,allowance);
		AIStatus = Status.RUNNING;
		animations = new ArrayList<>();
		board = new Board(allowance, progress);
		board = new Board(allowance, progress);
		whiteIcons = new HashMap<>();
		blackIcons = new HashMap<>();

		//Graphics
		Label mess = new Label();
		mess.setFont(new Font("Ubuntu Mono", 24));
		HBox.setMargin(mess, new Insets(10,10,0,10));

		//Top
		VBox top = new VBox(10);
		HBox messBox = new HBox(mess);
		HBox progressBar = new HBox(10);

		//Progress Bar
		ProgressBar pBar = new ProgressBar();
		VBox.setMargin(progressBar, new Insets(10,10,0,10));
		pBar.prefWidthProperty().bind(top.widthProperty());
		top.setAlignment(Pos.CENTER);
		pBar.progressProperty().bind(progress);

		//Stop Button
		stopStart = new ImageView(getClass().getResource("../resources/images/stop.png").toString());
		stopStart.setPreserveRatio(true);
		stopStart.setVisible(true);
		stopStart.setOnMouseClicked(e-> {
			allowance.set(!allowance.get());
		});
		stopStart.setFitHeight(19);
		progressBar.getChildren().addAll(pBar, stopStart);


		top.getChildren().addAll(progressBar,messBox);

		messBox.setAlignment(Pos.CENTER);
		messages = new Messages(mess);

		double stageX = 0.0;
		double stageY = 0.0;
		for(Screen screen : Screen.getScreens()){
			Rectangle2D bounds = screen.getBounds();
			if(bounds.getWidth() == 1280.0 && bounds.getHeight() == 800){
				stageX = bounds.getMinX()+bounds.getWidth();
				stageY = bounds.getMinY()+bounds.getHeight();
			}
		}
		window = primaryStage;
		window.setX(stageX);
		window.setY(stageY);
		window.setTitle("Chess");
		window.setWidth(800);
		window.setHeight(750);
		window.setMinHeight(550);
		window.setMinWidth(450);

		try {
			//			URL iconURL = App.class.getResource("Black_King.png");
			//			java.awt.Image image = new ImageIcon(iconURL).getImage();
			//			com.apple.eawt.Application.getApplication().setDockIconImage(image);
		} catch (Exception e) {
			// Won't work on Windows or Linux.
		}

		//Initialize

		initButtonBar();
		trainingView = new TrainingView();

		//Stack Pane
		layout = new StackPane();
		canvas = new Canvas(100,100);

		window.widthProperty().addListener(e -> resize());
		window.heightProperty().addListener(e -> resize());

		step = canvas.getWidth()/8;

		//Initialize
		initiateBoard();
		layout.getChildren().add(canvas);
		initiatePieces();


		//Master Layout
		masterLayout = new BorderPane();
		masterLayout.setCenter(layout);
		masterLayout.setBottom(buttons);
		masterLayout.setTop(top);

		//		initiateTimerPanel(window.getHeight()-300);
		masterLayout.setLeft(timerPanel);

		Scene board = new Scene(masterLayout);
		board.setFill(Color.LIGHTGREY);
		window.setScene(board);

		//Mouse Handler
		canvas.setOnMouseClicked(e -> {
			click(e.getSceneX(), e.getSceneY(),null);
		});



		//Show the Window
		window.show();

		resize();
		setupAnimation(Animation_Duration,0);
		window.centerOnScreen();
	}

	private void showBoard(){
		layout.getChildren().remove(coverPause);
		for(Point p : board.blackPieces.keySet())
			blackIcons.get(p).setVisible(true);
		for(Point p : board.whitePieces.keySet())
			whiteIcons.get(p).setVisible(true);
	}
	/**
	 * Draws the board on the canvas
	 */
	private void initiateBoard(){
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		boolean startBlack = false;
		for(int y = 0; y < 8; y++){
			boolean black = startBlack;
			for(int x = 0; x < 8; x++){
				Color color = black ? Color.BROWN : Color.WHITE;
				gc.setFill(color);
				gc.fillRect(step*x, step*y, step, step);
				black = !black;
			}
			startBlack = !startBlack;		
		}
		if(selected != null)
			select(selected);

		double h = canvas.getHeight();
		double y = board.turn == board.rules.isTopPlayer() ?  0: h;
		gc.setStroke(board.getIsAIPlayer() ? Color.RED : Color.BLUE);
		gc.setLineWidth(step*.1);
		gc.strokeLine(0, y, h, y);
	}

	/**
	 * Called when the window is resized. It fits the canvas and reinstansiates the pieces to fit
	 */
	private void resize(){
		double size = Double.min(window.getHeight()-150, window.getWidth()-150);

		canvas.setWidth(size);
		canvas.setHeight(size);
		step = size/8;
		initiateBoard();
		for(ImageView icon: blackIcons.values()){
			icon.setFitWidth(step);
		}
		for(ImageView icon: whiteIcons.values()){
			icon.setFitWidth(step);
		}
		setupAnimation(.0001, 0);
	}

	/**
	 * Creates and adds an icon for every piece on the board
	 */
	private void initiatePieces(){
		layout.getChildren().removeAll(blackIcons.values());
		layout.getChildren().removeAll(whiteIcons.values());
		blackIcons.clear();
		whiteIcons.clear();

		for(Point point: board.blackPieces.keySet()){
			Piece piece = board.blackPieces.get(point);
			ImageView icon = initPiece(point, false, piece);

			blackIcons.put(point, icon);
			layout.getChildren().add(icon);
		}
		for(Point point: board.whitePieces.keySet()){
			Piece piece = board.whitePieces.get(point);
			ImageView icon = initPiece(point, true, piece);
			whiteIcons.put(point, icon);
			layout.getChildren().add(icon);
		}		
	}

	private HBox initButtonBar(){
		HBox buttons = new HBox(10);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(10,0,10,0));
		if(!editing) {
			//Buttons

			Button tree = new Button("Tree");
			tree.setOnAction(e -> {
				if(board.turn)
					board.black.parent.print();
				else
					board.white.parent.print();
				try {
					Desktop.getDesktop().open(new File("ChessTree.txt"));
				} catch (IOException e1) {
					System.err.println("No File Named ChessTree.txt");
					e1.printStackTrace();
				}
			});

			Button reanimate = new Button("Show Last");
			reanimate.setOnAction(e -> {
				Move last = board.history.peek();
				undo(.001);
				move(last);
			});

			Button reset = new Button("Restart");
			reset.setOnAction(e -> reset());

			Button sync = new Button("Sync");
			sync.setOnAction(e -> {
				initiatePieces();
				resize();
				setupAnimation(.0001, 0);
			});

			Button edit = new Button("Edit");
			edit.setOnAction(e -> {
				if(board.rules.getMode() == GameMode.pvc && board.getIsAIPlayer())
					messages.message("Please wait until the AI has moved before editing", Duration.seconds(5));
				else if(board.rules.getMode() == GameMode.cvc && AIStatus != Status.RUNNING)
					messages.message("Please pause the AIs before editing", Duration.seconds(5));
				else {
					editing = true;
					messages.message("Click pieces to move them", Duration.seconds(10));
					initButtonBar();
				}
			});

			Button print = new Button("Print");
			print.setOnAction(e -> {
				board.print();
			});


			Button undo = new Button("Undo");
			undo.setOnAction(e -> {
				switch(board.rules.getMode()){
				case cvc:
					if(AIStatus == Status.PAUSED)
						undo(Animation_Duration);
					break;
				case pvc:
					undo(Animation_Duration);
					undo(Animation_Duration);
					break;
				case pvp:
					undo(Animation_Duration);
					break;
				default:
					break;
				}
				initiateBoard();
			});

			ImageView sButton = new ImageView(getClass().getResource("../resources/images/settings.png").toString());
			sButton.setPreserveRatio(true);
			sButton.setVisible(true);
			sButton.setOnMouseClicked(e-> {
				SettingsView s = new SettingsView(board);
				if(s.display())  
					reset();
				initButtonBar();
				showBoard();
				if(board.getIsAIPlayer() && AIStatus == Status.RUNNING)
					new AIMove(board).start();
			});


			Button stepT = new Button("Step");
			stepT.setOnAction(e -> {
				if(board.getIsAIPlayer())
					new AIMove(board).start();
			});

			Button pausePlay = new Button(AIStatus == Status.PAUSED ? "Play" : "Pause");
			pausePlay.setOnAction(e -> {
				if(board.rules.getMode() == GameMode.cvc){
					if(AIStatus == Status.PAUSED){
						pausePlay.setText("Pause");
						AIStatus = Status.RUNNING;
						new AIMove(board).start();
					}
					else{
						pausePlay.setText("Play");
						AIStatus = Status.PAUSED;
						allowance.set(false);
					}
				}
			});

			//		Button copy = new Button("Copy");
			//		copy.setOnAction(e -> {
			//			ClipboardContent content = new ClipboardContent();
			//			content.putString(board.exportToString());
			//			Clipboard.getSystemClipboard().setContent(content);
			//			messages.message("Copied", Duration.seconds(1));
			//		});
			Button hist = new Button("History");
			hist.setOnAction(e -> {
				ClipboardContent content = new ClipboardContent();
				content.putString(board.history.toString());
				Clipboard.getSystemClipboard().setContent(content);
				messages.message(String.format("Copied History to Clipboard, Size: %d",board.history.size()), Duration.seconds(1));
			});

			Button load = new Button("Load");
			load.setOnAction(e -> {
				String content = Clipboard.getSystemClipboard().getString();
				board = new Board(allowance, content, progress);
				initiatePieces();
				resize();
				setupAnimation(.0001, 0);
			});


			Button test = new Button("Test");
			test.setOnAction(e -> {
				if(test.getText().equals("Test")){
					test.setText("Stop");
					tester.start();
					try {
						Desktop.getDesktop().open(new File("Learner.txt"));
					} catch (IOException e1) {
						System.err.println("No File Named ChessTree.txt");
						e1.printStackTrace();
					}
				}
				else{
					allowance.set(false);
					test.setText("Test");
				}

			});
			sButton.fitHeightProperty().set(20);;

			Button training = new Button("Training");
			training.setOnAction(e -> {
				trainingView.display();
			});


			buttons.getChildren().addAll(sButton, reset, hist, edit);
			if(board.rules.isUndo())
				buttons.getChildren().addAll(undo);
			if(board.rules.getMode() != GameMode.cvc)
				buttons.getChildren().addAll(reanimate);
			else{
				buttons.getChildren().addAll(pausePlay);
				buttons.getChildren().addAll(stepT);	
			}
			if(board.rules.isDebug()){
				Separator sep = new Separator(Orientation.VERTICAL);
				buttons.getChildren().addAll(sep, print, tree, sync, test, training);
			}
			//		this.buttons.getChildren().clear();
			//		this.buttons.getChildren().addAll(buttons.getChildren());


		}
		else {
			Button done = new Button("Done");
			done.setOnAction(e -> {
				System.out.println("Done Editing");
				editing = false;
				for(Point p : board.blackPieces.keySet())
					blackIcons.get(p).setVisible(true);
				for(Point p : board.whitePieces.keySet())
					whiteIcons.get(p).setVisible(true);
				initButtonBar();
				board.print();
			});

			Button delete = new Button("Delete");
			delete.setDisable(true);
			delete.setOnAction(e -> {
				System.out.println("Delete");
				if(draggedIcon != null) {
					layout.getChildren().remove(draggedIcon);	
					draggedIcon.setOnMouseMoved(null);
					canvas.setOnMouseMoved(null);
					draggedIcon = null;
					setDoneDisable(false);
				}
				if(draggedPiece != null) {
					board.edit(draggedPiece, null);
					draggedPiece = null;
				}
			});

			Button turn = new Button((board.turn ? "White" : "Black") + "'s turn");
			turn.setOnAction(e -> {
				board.turn = !board.turn;
				turn.setText((board.turn ? "White" : "Black") + "'s turn");
				initiateBoard();
			});
			
			Button wQueen = new Button();
			wQueen.setGraphic(getImageView("Queen", true));
			wQueen.setOnAction(e -> {
				Piece n = new Queen(true, null);
				addPiece(n, wQueen.getLayoutX(), wQueen.getLayoutY());
			});

			Button wRook = new Button();
			wRook.setGraphic(getImageView("Rook", true));
			wRook.setOnAction(e -> {
				Piece n = new Rook(true, null);
				addPiece(n, wRook.getLayoutX(), wRook.getLayoutY());
			});

			Button wBishop = new Button();
			wBishop.setGraphic(getImageView("Bishop", true));
			wBishop.setOnAction(e -> {
				Piece n = new Bishop(true, null);
				addPiece(n, wBishop.getLayoutX(), wBishop.getLayoutY());
			});

			Button wKnight = new Button();
			wKnight.setGraphic(getImageView("Knight", true));
			wKnight.setOnAction(e -> {
				Piece n = new Knight(true, null);
				addPiece(n, wKnight.getLayoutX(), wKnight.getLayoutY());
			});

			Button wPawn = new Button();
			wPawn.setGraphic(getImageView("Pawn", true));
			wPawn.setOnAction(e -> {
				Piece n = new Pawn(true, null);
				addPiece(n, wPawn.getLayoutX(), wPawn.getLayoutY());
			});

			Button bQueen = new Button();
			bQueen.setGraphic(getImageView("Queen", false));
			bQueen.setOnAction(e -> {
				Piece n = new Queen(false, null);
				addPiece(n, bQueen.getLayoutX(), bQueen.getLayoutY());
			});

			Button bRook = new Button();
			bRook.setGraphic(getImageView("Rook", false));
			bRook.setOnAction(e -> {
				Piece n = new Rook(false, null);
				addPiece(n, bRook.getLayoutX(), bRook.getLayoutY());
			});

			Button bBishop = new Button();
			bBishop.setGraphic(getImageView("Bishop", false));
			bBishop.setOnAction(e -> {
				Piece n = new Bishop(false, null);
				addPiece(n, bBishop.getLayoutX(), bBishop.getLayoutY());
			});

			Button bKnight = new Button();
			bKnight.setGraphic(getImageView("Knight", false));
			bKnight.setOnAction(e -> {
				Piece n = new Knight(false, null);
				addPiece(n, bKnight.getLayoutX(), bKnight.getLayoutY());
			});

			Button bPawn = new Button();
			bPawn.setGraphic(getImageView("Pawn", false));
			bPawn.setOnAction(e -> {
				Piece n = new Pawn(false, null);
				addPiece(n, bPawn.getLayoutX(), bPawn.getLayoutY());
			});

			castleWL = new CheckBox("Castle WL");
			castleWR = new CheckBox("Castle WR");
			castleBL = new CheckBox("Castle BL");
			castleBR = new CheckBox("Castle BR");

			castleWL.setOnAction(e -> {
				setPieceCastle(true, true, castleWL.isSelected());
			});
			castleWR.setOnAction(e -> {
				setPieceCastle(false, true, castleWR.isSelected());
			});
			castleBL.setOnAction(e -> {
				setPieceCastle(true, false, castleBL.isSelected());
			});
			castleBR.setOnAction(e -> setPieceCastle(false, false, castleBR.isSelected()));

			updateCastleCheckBoxes();

			buttons.getChildren().addAll(done,delete,turn,wQueen,wRook,wBishop,wKnight,wPawn,bQueen,bRook,bBishop,bKnight,bPawn,castleWL,castleWR,castleBL,castleBR);


		}


		if(this.buttons == null){
			this.buttons = buttons;
		}
		else{
			ObservableList<Node> nodes = this.buttons.getChildren();
			nodes.clear();
			nodes.addAll(buttons.getChildren());
		}
		return buttons;
	}
	
	private void setPieceCastle(boolean left, boolean color, boolean canCaslte) {
		Piece k = board.getPiece(new Point(board.rules.isTopPlayer() ? 3 : 4, color == board.rules.isTopPlayer() ? 0 : 7));
		Piece p = board.getPiece(new Point(left ? 0 : 7, board.rules.isTopPlayer() == color ? 0 : 7));
		if(p != null)
			p.moves = canCaslte ? 0 : Math.max(1, p.moves);
		if(canCaslte)
			k.moves = 0;
		updateCastleCheckBoxes();
	}
	

	private void updateCastleCheckBoxes() {
		boolean top = board.rules.isTopPlayer();
		int wy = top ? 0 : 7;
		int by = top ? 7 : 0;
		Piece whiteKing = top ? board.getPiece(new Point(3,0)) : board.getPiece(new Point(4,7));
		Piece whiteRookL = board.getPiece(new Point(0,wy));
		Piece whiteRookR = board.getPiece(new Point(7,wy));
		Piece blackKing = top ? board.getPiece(new Point(3,7)) : board.getPiece(new Point(4,0));
		Piece blackRookL = board.getPiece(new Point(0,by));
		Piece blackRookR = board.getPiece(new Point(7,by));
		
		boolean wl = whiteKing != null && whiteRookL != null;
		boolean wr = whiteKing != null && whiteRookR != null;
		boolean bl = blackKing != null && blackRookL != null;
		boolean br = blackKing != null && blackRookR != null;
		
		castleWL.setDisable(!wl);
		castleWR.setDisable(!wr);
		castleBL.setDisable(!bl);
		castleBR.setDisable(!br);
		
		castleWL.setSelected(wl && whiteKing.moves == 0 && whiteRookL.moves == 0);
		castleWR.setSelected(wr && whiteKing.moves == 0 && whiteRookR.moves == 0);
		castleBL.setSelected(bl && blackKing.moves == 0 && blackRookL.moves == 0);
		castleBR.setSelected(br && blackKing.moves == 0 && blackRookR.moves == 0);
	}
	
	private void addPiece(Piece p, double x, double y) {
		draggedIcon = initPiece(null, p.isWhite(), p);
		layout.getChildren().add(draggedIcon);
		draggedPiece = p;
		draggedCoords[0] = draggedIcon.translateXProperty().get();
		draggedCoords[1] = draggedIcon.translateYProperty().get();
		EventHandler<MouseEvent> handleMouseMove = e -> mouseMove(e.getSceneX(), e.getSceneY());
		draggedIcon.toFront();
		draggedIcon.setFitWidth(step);
		draggedIcon.setOnMouseMoved(handleMouseMove);

		canvas.setOnMouseMoved(handleMouseMove);
		setDoneDisable(true);
	}

	private ImageView getImageView(String s, boolean color) {
		String name =  "../resources/images/" + ( color ? "White_" : "Black_") + s + ".png";
		ImageView icon = new ImageView(getClass().getResource(name).toString());
		icon.setPreserveRatio(true);
		icon.setFitHeight(17);
		return icon;
	}

	private ImageView initPiece(Point p, boolean player, Piece piece){
		String color = "../resources/images/" + (player ? "White_" : "Black_") + piece.toString() + ".png";
		ImageView icon = new ImageView(getClass().getResource(color).toString());
		icon.setPreserveRatio(true);
		icon.setVisible(false);
		icon.setOnMouseClicked(e-> click(e.getSceneX(),e.getSceneY(),null));
		return icon;
	}

	/**
	 * Animates the pieces coming out from the center
	 * @param duration
	 * The duration of the animation
	 * @param delay
	 * The delay before the animation plays
	 */
	private void setupAnimation(double duration, double delay){
		ArrayList<Point> points = new ArrayList<>();
		points.addAll(board.blackPieces.keySet());
		points.addAll(board.whitePieces.keySet());
		int i = 0;

		for(Point p:points){
			ImageView icon;
			if(whiteIcons.containsKey(p)){
				icon = whiteIcons.get(p);
			}
			else if(blackIcons.containsKey(p)){
				icon = blackIcons.get(p);
			}
			else{
				System.err.printf("No icon at %s\n", p.toString());
				continue;
			}
			icon.setVisible(true);

			double[] t = getLayoutCoord(p);
			double diff = step/2;

			Line path = new Line(diff, diff, t[0], t[1]);
			PathTransition move = new PathTransition();
			move.setPath(path);
			move.setDuration(Duration.seconds(duration));
			move.setDelay(Duration.seconds(delay));
			move.setNode(icon);
			if(duration > .001 && ++i == points.size())
				move.setOnFinished(e -> {
					if(board.getIsAIPlayer() && AIStatus == Status.RUNNING)
						new AIMove(board).start();
				});
			move.play();
		}

	}

	/**
	 * Animates a move
	 * @param from
//	 * The point describing where the piece starts
	 * @param to
	 * The point describing where the piece starts
	 * @param duration
	 * The duration of the animation
	 */
	private Transition animateMove(Point from, Point to, double duration){
		ImageView icon;
		if(whiteIcons.containsKey(from)){
			icon = whiteIcons.remove(from);
			icon.setOnMouseClicked(e-> click(e.getSceneX(),e.getSceneY(),to));
			whiteIcons.put(to, icon);
		}
		else if(blackIcons.containsKey(from)){
			icon = blackIcons.remove(from);
			icon.setOnMouseClicked(e-> click(e.getSceneX(),e.getSceneY(),to));
			blackIcons.put(to, icon);
		}
		else{
			System.err.printf("Cannot Animate piece at %s because it isn't there. (Move: %s)\n",from.toString(), board.history.peek());
			return null;
		}
		double[] f = getLayoutCoord(from);
		double[] t = getLayoutCoord(to);
		Line path = new Line(f[0], f[1], t[0], t[1]);
		PathTransition move = new PathTransition();
		move.setPath(path);
		move.setDuration(Duration.seconds(duration));
		move.setNode(icon);

		animations.add(move);
		move.setOnFinished(e -> animations.remove(move));
		return move;
	}
	private Transition animateCapture(Point pos, double duration, boolean color){
		ImageView icon;
		if(color)
			icon = whiteIcons.get(pos);
		else
			icon = blackIcons.get(pos);
		if(icon == null){
			System.err.println("No Point At: " + pos);
			return null;
		}
		RotateTransition rotate = new RotateTransition(Duration.seconds(duration),icon);
		rotate.setByAngle(200);
		FadeTransition fade = new FadeTransition(Duration.seconds(duration), icon);
		fade.setFromValue(1.0);
		fade.setToValue(0.0);
		ParallelTransition capture = new ParallelTransition(rotate, fade);
		capture.setOnFinished(e -> {
			if(color)
				whiteIcons.remove(pos);
			else
				blackIcons.remove(pos);
			layout.getChildren().remove(icon);
			animations.remove(capture);
		});

		animations.add(capture);
		return capture;
	}
	private Transition unCapture(ImageView piece, double duration, Point pos){
		double[] gp = getLayoutCoord(pos);
		Line path = new Line(1, 1 , gp[0], gp[1]);
		PathTransition move = new PathTransition();
		move.setPath(path);
		move.setDuration(Duration.millis(10));
		move.setNode(piece);


		FadeTransition fade = new FadeTransition();
		fade.setNode(piece);
		fade.setFromValue(0.0);
		fade.setToValue(1.0);
		fade.setDuration(Duration.seconds(duration));

		SequentialTransition uncap = new SequentialTransition(move, fade);

		animations.add(uncap);
		uncap.setOnFinished(e -> animations.remove(uncap));
		return uncap;
	}

	private Transition animateSwitch(Point p, Piece to, double duration, boolean color){

		ImageView remove;
		if(color)
			remove = whiteIcons.remove(p);
		else
			remove = blackIcons.remove(p);
		if(remove == null){
			System.err.println("Can't Switch piece at " + p + " because the icon isn't there");
			return null;
		}
		layout.getChildren().remove(remove);
		FadeTransition fadeOut = new FadeTransition();
		fadeOut.setNode(remove);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		fadeOut.setDuration(Duration.seconds(duration));

		ImageView toIcon = initPiece(p, color, to);
		if(color)
			whiteIcons.put(p, toIcon);
		else
			blackIcons.put(p, toIcon);
		layout.getChildren().add(toIcon);
		resize();
		toIcon.setVisible(true);

		FadeTransition fadeIn = new FadeTransition();
		fadeIn.setNode(toIcon);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.setDuration(Duration.seconds(duration));

		//		SequentialTransition switchPiece = new SequentialTransition(fadeOut, fadeIn);
		ParallelTransition switchPiece = new ParallelTransition(fadeOut, fadeIn);

		animations.add(switchPiece);
		switchPiece.setOnFinished(e -> animations.remove(switchPiece));
		return switchPiece;
	}

	/**
	 * Gets the coordinate relative to the middle of the layout: (0,0) = Middle of window
	 * @param p
	 * The point to be converted
	 * @return
	 * An int[]{x,y} describing where the point is in relation to the layout
	 */
	private double[] getLayoutCoord(Point p){
		double start = 0 - step * 3;
		return new double[]{(start + step * p.x), (start + step * p.y)};
	}

	/**
	 * Gets the coordinate relative to the canvas: (0,0) = top left corner
	 * The point to be converted
	 * @return
	 * An int[]{x,y} describing where the point is in relation to the canvas
	 */
	private double[] getCanvasCoord(Point p){
		return new double[]{(step * p.x), (step * p.y)};
	}

	private double[] getCanvasCoord(double x, double y) {
		double offX = (window.widthProperty().get()-canvas.widthProperty().get())/2.0;
		double offY = (window.heightProperty().get()-canvas.heightProperty().get())/2.0;
		return new double[] {x - offX, y - offY};
	}

	/**
	 * Gets the point of a click on the canvas
	 * @param x
	 * x value of the click
	 * @param y
	 * y value of the click
	 * @return
	 * the position on the board clicked
	 */
	private Point getPoint(double x, double y){
		x /= step;
		y /= step;
		return new Point((int)x, (int)y);
	}



	/**
	 * Handles the click event at either the given point or canvas coordinates
	 * @param x
	 * Canvas x coordinate of click
	 * @param y
	 * Canvas y coordinate of click
	 * @param p
	 * Point clicked
	 */
	private void click(double x, double y, Point p){
		double[] coord = getCanvasCoord(x, y);
		Point clicked = getPoint(coord[0],coord[1]);
		if(!editing) {
			if(board.gameState != State.INPROGRESS){
				messages.gameOver();
				progress.set(0.0);
				return;
			}
			if(board.rules.getMode().toString().equals("Computer vs Computer")){
				System.out.println("CVC");
				return;	
			}

			if(selected == null){ //Initial selection
				if(!board.getIsAIPlayer() && playerHasPiece(clicked, board.turn))
					select(clicked);
				else if(playerHasPiece(clicked, !board.turn))
					messages.notYourTurn();
				else if(board.getIsAIPlayer())
					messages.message("You Cannot Move For The AI", Duration.seconds(3));
			}
			else{
				if(!playerHasPiece(clicked, board.turn) && !board.getIsAIPlayer() && playerHasPiece(selected, board.turn)){	//Move
					Move m = new Move(selected, clicked, board);
					if(m.piece instanceof Pawn && (m.to.y == 0 || m.to.y == 7)){
						if(new Switcher().display())	//Queen
							m.changedTo = new Queen(m.me, m.to);
						else							//Knight
							m.changedTo = new Knight(m.me, m.to);
					}

					move(m);
					deSelect();
				}
				else{										//Reselected
					deSelect();
					select(clicked);
				}
			}	
		}
		else if(clicked.isInBoard()) {
			if(draggedPiece != null) { //Put down piece
				if(board.validPos(draggedPiece, clicked) == null) {
					messages.message("Invalid position for "+draggedPiece.toString(), Duration.seconds(5));
					return;
				}
				draggedIcon.setVisible(true);
				draggedIcon.setOnMouseMoved(null);
				canvas.setOnMouseMoved(null);
				ImageView replaced =  null;
				if(whiteIcons.containsKey(clicked))
					replaced = whiteIcons.remove(clicked);
				else if(blackIcons.containsKey(clicked))
					replaced = blackIcons.remove(clicked);
				if(draggedPiece.isWhite()) {
					whiteIcons.put(clicked, draggedIcon != null ? draggedIcon : whiteIcons.remove(draggedPiece.position));	
				}else{
					blackIcons.put(clicked, draggedIcon != null ? draggedIcon : blackIcons.remove(draggedPiece.position));	
				}
				if(clicked.equals(draggedPiece.position))
					draggedPiece = null;
				else
					draggedPiece = board.edit(draggedPiece, clicked);

				double[] layCoord = getLayoutCoord(clicked);
				Line path = new Line(draggedIcon.translateXProperty().get()+draggedIcon.getFitWidth()/2.0, draggedIcon.translateYProperty().get()+draggedIcon.getFitWidth()/2.0, layCoord[0], layCoord[1]);
				PathTransition move = new PathTransition();
				move.setPath(path);
				move.setDuration(Duration.seconds(.25));
				move.setNode(draggedIcon);
				move.playFromStart();
				animations.add(move);
				move.setOnFinished(e -> animations.remove(move));
				updateCastleCheckBoxes();

				if(draggedPiece != null) { //Replace piece
					draggedIcon = replaced;
					draggedCoords = getCanvasCoord(draggedPiece.position);
					EventHandler<MouseEvent> handleMouseMove = e -> mouseMove(e.getSceneX(), e.getSceneY());
					if(draggedIcon != null) {
						draggedIcon.setOnMouseMoved(handleMouseMove);
						draggedIcon.toFront();
					}
					else {
						System.err.println("Piece is null");
						throw new NullPointerException();
					}
					canvas.setOnMouseMoved(handleMouseMove);
					setDoneDisable(true);
					if(draggedPiece instanceof King) {
						for(Node n: buttons.getChildren()) {
							if(n instanceof Button) {
								Button b = (Button)n;
								if(b.getText().equals("Delete"))
									b.setDisable(true);
							}
						}
					}
				}
				else {
					setDoneDisable(false);
				}
			}
			else if(board.getPiece(clicked) != null) { //Pick up piece
				draggedPiece = board.getPiece(clicked);
				draggedIcon = (draggedPiece.isWhite() ? whiteIcons.get(clicked) : blackIcons.get(clicked));
				draggedCoords = getCanvasCoord(draggedPiece.position);
				EventHandler<MouseEvent> handleMouseMove = e -> mouseMove(e.getSceneX(), e.getSceneY());
				draggedIcon.toFront();
				draggedIcon.setOnMouseMoved(handleMouseMove);
				canvas.setOnMouseMoved(handleMouseMove);
				setDoneDisable(true);
				if(draggedPiece instanceof King) {
					for(Node n: buttons.getChildren()) {
						if(n instanceof Button) {
							Button b = (Button)n;
							if(b.getText().equals("Delete"))
								b.setDisable(true);
						}
					}
				}
			}	
		}else {
			System.out.println("Delete");
			if(draggedIcon != null) {
				layout.getChildren().remove(draggedIcon);	
				draggedIcon.setOnMouseMoved(null);
				canvas.setOnMouseMoved(null);
				draggedIcon = null;
				setDoneDisable(false);
			}
			if(draggedPiece != null) {
				board.edit(draggedPiece, null);
				draggedPiece = null;
			}
		}
	}

	private void setDoneDisable(boolean value) {
		for(Node n: buttons.getChildren()) {
			if(n instanceof Button) {
				Button b = (Button)n;
				if(b.getText().equals("Delete"))
					b.setDisable(!value);
				else
					b.setDisable(value);
			}
		}
	}

	private void mouseMove(double x, double y) {
		double[] canvasCoord = getCanvasCoord(x, y);
		draggedIcon.setTranslateX(canvasCoord[0]-canvas.widthProperty().get()/2.0);
		draggedIcon.setTranslateY(canvasCoord[1]-canvas.heightProperty().get()/2.0);
		mouseCoords[0] = x;
		mouseCoords[1] = y;
		draggedIcon.setVisible(true);
	}


	private void move(Move m){
		if(m == null)
			System.out.println("Cant Move Null");
		if(playerHasPiece(m.from, !board.turn)){
			messages.notYourTurn();
			return;
		}
		else{
			Piece f = board.getPiece(m.from);
			ArrayList<Move> moves = f.getMoves(board, m.from);
			board.addCastleMoves(moves, m.me);
			board.removeCastleOutOfCheck(moves, m.me);
			int i = moves.indexOf(m);
			if(i >= 0 && !moves.get(i).putsPlayerInCheck(m.me)){
				m = moves.get(i);
				if(m.putsPlayerInCheck(!m.me)){
					messages.message("Check", Duration.seconds(3));
				}
				if(board.move(m)){	//Moves returns if capture piece
					if (m.me){
						animateCapture(m.to, Animation_Duration, false).play();
					}
					else
						animateCapture(m.to, Animation_Duration, true).play();
				}
				Transition move = animateMove(m.from, m.to, Animation_Duration);
				if(m.changedTo != null){	//Queen me
					move.setOnFinished(e -> {
						animations.remove(move);
						respondToFinishSwitchMove();
					});

				}
				else
					setOnAIMoveOnFinish(move);
				move.play();

				if(board.history.size() == stop)
					AIStatus = Status.PAUSED;
				//					else if(board.history.size() == stop+5)
				//						AIStatus = Status.RUNNING;


				if(m.castlingMove){
					boolean left = m.to.x < 3;
					int y = m.me == board.rules.isTopPlayer() ? 0 : 7;
					if(left){
						animateMove(new Point(0, y), new Point(3, y), Animation_Duration).play();
					}
					else{
						animateMove(new Point(7, y), new Point(5, y), Animation_Duration).play();
					}
				}
				initiateBoard();
				if(board.gameState != State.INPROGRESS){
					messages.setGameState(board.gameState);
					messages.gameOver();
					progress.set(0.0);
				}
				board.updateIcon();
				int temp = board.hashCode(board.white.keys);
				int index = histHash.indexOf(temp);
				if(index >= 0){
					messages.message(String.format("%s conceding to repetition",board.turn ? "White" : "Black"),Duration.seconds(2));
				}
				histHash.add(temp);
				if(!isAllWell()){
					System.out.println("Icons Disagree");
					AIStatus = Status.PAUSED;
				}
			}
		}
	}

	private void setOnAIMoveOnFinish(Transition t){
		t.setOnFinished(e -> {
			if(board.getIsAIPlayer() && board.gameState == State.INPROGRESS && allowance.get() && (AIStatus == Status.RUNNING || board.rules.getMode() == GameMode.pvc))
				new AIMove(board).start();
			animations.remove(t);
		});
	}
	private void respondToFinishSwitchMove(){
		Move mm = board.history.peek();
		Transition t = animateSwitch(mm.to, mm.changedTo, Animation_Duration, mm.me);
		setOnAIMoveOnFinish(t);
		t.play();
	}
	/**
	 * Draws a box on the board to show the point is selected
	 * @param p
	 */
	private void select(Point p){
		selected = p;
		double[] grid = getCanvasCoord(p);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setStroke(Color.YELLOW);
		double width = step/10;
		gc.setLineWidth(width);
		gc.strokeRect(grid[0]+width/2, grid[1]+width/2 ,step-width, step-width);
	}

	/**
	 * Clears any selected points
	 */
	private void deSelect(){
		selected = null;
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		initiateBoard();
	}

	/**
	 * Resets the board
	 */
	private void reset(){
		histHash.clear();
		board.avCount = 0;
		board.avTotal = 0;
		//		allowance.set(false);
		messages.clear();
		deSelect();
		board.setUpBoard();
		initiatePieces();
		resize();
		for(Transition p: animations)
			p.stop();
		animations.clear();
		allowance.set(true);
		setupAnimation(Animation_Duration,0);
	}

	private void startAllAnimations(){
		for(Transition trans: animations)
			if(!trans.getStatus().equals(Status.RUNNING))
				trans.play();
	}
	private void undo(double d){
		if(board.white.thinking || board.black.thinking){
			allowance.set(false);
			return;
		}
		if(board.history.isEmpty())
			System.out.println("Cant Undo");
		else{
			histHash.remove(histHash.size()-1);
			deSelect();
			Move m = board.history.peek();
			board.undo();
			if(m.changedTo != null){
				Transition t = animateSwitch(m.to, board.getPiece(m.from), d, m.me);
				t.setOnFinished(e -> {
					animations.remove(t);
					startAllAnimations();
				});
				t.play();
			}
			animateMove(m.to, m.from, d);
			if(m.getCapture() != null){
				Piece piece = m.getCapture();
				ImageView icon = initPiece(m.to, piece.isWhite(), piece);
				icon.setVisible(true);
				layout.getChildren().add(icon);
				if(piece.isWhite())
					whiteIcons.put(m.to, icon);
				else
					blackIcons.put(m.to, icon);
				icon.setFitWidth(step);
				unCapture(icon, d, m.to);
			}
			if(m.castlingMove){
				boolean left = m.to.x < 4;
				int y = m.me == board.rules.isTopPlayer() ? 0 : 7;
				if(left)
					animateMove(new Point(3, y), new Point(0, y), d);
				else
					animateMove(new Point(5, y), new Point(7, y), d);
			}
			if(m.changedTo == null)
				startAllAnimations();
		}
	}

	/**
	 * Determines if the given player has a piece at the given point
	 * @param p
	 * The point in question
	 * @param player
	 * The player
	 * @return
	 * returns true if the player has a piece there
	 */
	private boolean playerHasPiece(Point p, boolean player){
		if(player){
			return board.whitePieces.containsKey(p);
		}
		return board.blackPieces.containsKey(p);
	}



	@Override
	public String toString() {
		return board.toString();
	}
	public static void SetUpVBoxCentered(VBox layout){
		layout.setAlignment(Pos.CENTER);
		layout.setSpacing(10);
	}
	public static void SetUpHBoxCentered(HBox layout){
		layout.setAlignment(Pos.CENTER);
		layout.setSpacing(10);
	}
	public static void SetUpVBoxLeft(VBox layout){
		layout.setAlignment(Pos.CENTER_LEFT);
		layout.setSpacing(10);
	}
	public static void SetUpHBoxLeft(HBox layout){
		layout.setAlignment(Pos.CENTER_LEFT);
		layout.setSpacing(10);
	}
	public static void SetUpTextField(TextField text){
		text.setMaxWidth(60);
	}
	public static void SetMargins(VBox layout){

		VBox.setMargin(layout.getChildren().get(0), new Insets(10,0,0,0));
		VBox.setMargin(layout.getChildren().get(layout.getChildren().size()-1), new Insets(0,0,10,0));

	}
	public boolean isAllWell(){
		Stack<Piece> pieces = new Stack<>();
		pieces.addAll(board.whitePieces.values());
		pieces.addAll(board.blackPieces.values());
		while(!pieces.isEmpty()){
			Piece p = pieces.pop();
			if(p.isWhite()){
				if(!whiteIcons.containsKey(p.position))
					return false;
			}
			else{
				if(!blackIcons.containsKey(p.position))
					return false;
			}
		}
		return true;
	}

	class AIMove extends Thread{
		AI ai;
		boolean valid;
		public AIMove(Board board) {
			valid = board.getIsAIPlayer();
			if(board.turn)
				ai = board.white;
			else
				ai = board.black;
		}
		@Override
		public void run() {
			allowance.set(true);
			AI ai = board.getAI();
			Move move = null;
			try{
				move = ai.getBestMove();
			}catch (Exception e) {
				System.err.println(e);
				if(!allowance.get()){
					System.out.println("Calculation Interrupted By User");
					return;
				}
				System.out.println(board.black.stratagy.getTranspositionTableDepth());
				System.out.println("Calculation Failed");
				throw e;
			}
			if(move != null){
				aisMove = move;
				Timeline tl = new Timeline(new KeyFrame(Duration.millis(1),evt -> {
					move(aisMove);
				}));
				tl.play();
			}
			else{
				System.out.println("Null Move Returned");
			}
			allowance.set(true);
			progress.set(0.0);
		}
	}
}
