package chess;

import application.App;
import chess.AI.Stratagy;
import chess.Board.RuleSet.GameMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsView{
	Stage window;
	VBox layout = new VBox();
	VBox stratagy = new VBox();
	boolean mustRestartOnClose = false;

	Board.RuleSet rules;
	AI.Stratagy blackStratagy;
	AI.Stratagy whiteStratagy;

	public SettingsView(Board board){
		window = new Stage();
		rules = board.rules;
		blackStratagy = board.black.stratagy;
		whiteStratagy = board.white.stratagy;
		App.SetUpVBox(layout);
	}

	public boolean display(){
		//TODO Finish Initiating Settings into interface
		//TODO When done settings, input window dimensions for each possibilty
		//Window Init
		
		double stageX = 0.0;
		double stageY = 0.0;
//		for(Screen screen : Screen.getScreens()){
//			Rectangle2D bounds = screen.getBounds();
//			if(bounds.getWidth() == 1280.0 && bounds.getHeight() == 800){
//				stageX = bounds.getMinX()+bounds.getWidth();
//				stageY = bounds.getMinY()+bounds.getHeight();
//				}
//		}
		window.setX(stageX);
		window.setY(stageY);
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Settings");
		window.setMinWidth(300);
		window.setMinHeight(80+layout.getChildren().size()*40);
		window.setHeight(rules.mode == GameMode.pvp ? 320 : 645);
		window.setResizable(false);
		

		//OK Button
		Button ok = new Button("OK");
		BorderPane.setAlignment(ok, Pos.CENTER);
		ok.setAlignment(Pos.CENTER);
		ok.setOnAction(e -> {
			window.close();
		});


		//Gamemode
		ObservableList<String> modes = FXCollections.observableArrayList(
				"Player vs Player", 
				"Player vs Computer", 
				"Computer vs Computer");
		ComboBox<String> gameMode = new ComboBox<>(modes);
		gameMode.setValue(rules.mode.toString());
		
		gameMode.valueProperty().addListener(e -> {
			System.out.println("Changed Gamemode to:" + gameMode.getValue());
			String m = gameMode.getValue();
			if(m.equals("Player vs Player")){
				rules.mode = Board.RuleSet.GameMode.pvp;
				window.setHeight(320);
			}
			else if(m.equals("Player vs Computer")){
				rules.mode = Board.RuleSet.GameMode.pvc;
				window.setHeight(645);

			}
			else{
				rules.mode = Board.RuleSet.GameMode.cvc;
				window.setHeight(645);
			}
			layout.getChildren().remove(stratagy);
			initStratagyView(rules.mode);
			layout.getChildren().add(layout.getChildren().size()-1, stratagy);
		});		initStratagyView(rules.mode);
		//Rules
		//Debug
		CheckBox debug = new CheckBox("Debug Mode");
		debug.setSelected(rules.debug);
		debug.selectedProperty().addListener(e -> {
			rules.debug = debug.isSelected();
		});
		//Undo
		CheckBox undo = new CheckBox("Allow Undo");
		undo.setSelected(rules.undo);
		undo.selectedProperty().addListener(e -> {
			rules.undo = undo.isSelected();
			mustRestartOnClose = true;
		});
		
		//CastleTCheck
		CheckBox throughCheck = new CheckBox("Can't Castle Through Check");
		throughCheck.setSelected(rules.cantCastleThroughCheck);
		throughCheck.selectedProperty().addListener(e -> {
			rules.cantCastleThroughCheck = throughCheck.isSelected();
		});

		//CastleACheck
		CheckBox afterCheck = new CheckBox("Can't Castle After Check");
		afterCheck.setSelected(rules.cantCastleAfterCheck);
		afterCheck.selectedProperty().addListener(e -> {
			rules.cantCastleAfterCheck = afterCheck.isSelected();
		});

		//Top Player
		HBox topPlayer = new HBox();
		Label tpl = new Label("Top Player");
		ObservableList<String> players = FXCollections.observableArrayList(
				"White", "Black");
		ComboBox<String> tp = new ComboBox<>(players);
		tp.setValue(rules.topPlayer ? "White" : "Black");
		topPlayer.getChildren().addAll(tpl,tp);
		App.SetUpHBox(topPlayer);
		tp.valueProperty().addListener(e -> {
			mustRestartOnClose = true;
			if(tp.getValue().equals("Black"))
				rules.topPlayer = false;
			else
				rules.topPlayer = true;
		});

		HBox timeLimit = new HBox();
		Label tl1 = new Label("Time Limit");
		Label tl2 = new Label("m");
		Label tl3 = new Label("s");
		ObservableList<String> timeLimitOptions = FXCollections.observableArrayList(
				"Off", "Total", "Turn");
		ComboBox<String> tl = new ComboBox<>(timeLimitOptions);
		String limit = "";
		switch (rules.timeLimit) {
		case off:
			limit = "Off";
			break;
		case total:
			limit = "Total";
			break;
		case turn:
			limit = "Turn";
			break;
		default:
			limit = "Other";
		}
		tl.setValue(limit);
		TextField tlM = new TextField();
		tlM.setText(""+rules.timeLimit.minutes);
		TextField tlS = new TextField();
		tlS.setText(""+rules.timeLimit.seconds);
		tl.valueProperty().addListener(e -> {
			String v = tl.getValue();
			tlS.setDisable(v.equals("Off"));
			tlM.setDisable(v.equals("Off"));

			if(v.equals("Off"))
				rules.timeLimit = Board.RuleSet.TimeLimit.off;
			else if(v.equals("Total"))
				rules.timeLimit = Board.RuleSet.TimeLimit.total;
			else if(v.equals("Turn"))
				rules.timeLimit = Board.RuleSet.TimeLimit.turn;
			tlS.setText(rules.timeLimit.seconds+"");
			tlM.setText(rules.timeLimit.minutes+""); 
		});
		tlM.textProperty().addListener(e -> {
			String v = tlM.getText();
			if(v.matches("\\d+"))
				rules.timeLimit.minutes = Integer.parseInt(v);
			else if(v.length() > 0)
				tlM.setText(""+rules.timeLimit.minutes);
		});
		tlS.textProperty().addListener(e -> {
			String v = tlS.getText();
			if(v.matches("\\d+"))
				rules.timeLimit.seconds = Integer.parseInt(v);
			else if(v.length() > 0)
				tlS.setText(""+rules.timeLimit.seconds);
		});
		tlS.setDisable(rules.timeLimit == Board.RuleSet.TimeLimit.off);
		tlM.setDisable(rules.timeLimit == Board.RuleSet.TimeLimit.off);
		tlS.setMaxWidth(40);
		tlM.setMaxWidth(30);
		App.SetUpHBox(timeLimit);
		App.SetUpHBox(topPlayer);
		
		Separator sep = new  Separator(Orientation.HORIZONTAL);
		
		timeLimit.getChildren().addAll(tl1,tl, tlM, tl2, tlS, tl3);

		layout.getChildren().addAll(debug, undo, throughCheck, afterCheck, topPlayer, sep, gameMode, stratagy, ok);
		App.SetMargins(layout);

		Scene s = new Scene(layout);
		window.setScene(s);
		window.centerOnScreen();
		window.showAndWait();
		return mustRestartOnClose;
	}

	private void initStratagyView(Board.RuleSet.GameMode mode){
		stratagy = new VBox();
		App.SetUpVBox(stratagy);

		if(mode == Board.RuleSet.GameMode.pvp)
			return;

		//Player Picker
		ObservableList<String> players = FXCollections.observableArrayList(
				"White", "Black");
		ComboBox<String> cPlayer = new ComboBox<>(players);
		cPlayer.setValue(rules.computerPlayer ? "White" : "Black");
		Label l = new Label(Board.RuleSet.GameMode.cvc == mode ? "" : "Computer Player");
		HBox player = new HBox();
		player.getChildren().addAll(l, cPlayer);
		App.SetUpHBox(player);
		cPlayer.valueProperty().addListener(e -> {
			rules.computerPlayer = cPlayer.getValue().equals("White");
			stratagy.getChildren().clear();
			stratagy.getChildren().add(player);
			initAISpecifics(stratagy, cPlayer);
		});
		stratagy.getChildren().add(player);
		initAISpecifics(stratagy, cPlayer);
	}
	
	private void initAISpecifics(VBox layout, ComboBox<String> cPlayer){
				//Depth
				HBox depth = new HBox();
				Label dl = new Label("Depth");
				TextField d = new TextField();
				d.setText(""+getStrat(cPlayer.getValue()).depth);
				depth.getChildren().addAll(dl,d);
				App.SetUpHBox(depth);
				App.SetUpTextField(d);
				
				//Check Depth
				HBox cDepth = new HBox();
				Label cdl = new Label("Check Depth");
				TextField cd = new TextField();
				cd.setText(""+getStrat(cPlayer.getValue()).checkDepth);
				cDepth.getChildren().addAll(cdl,cd);
				App.SetUpHBox(cDepth);
				App.SetUpTextField(cd);
				

				//AlphaBeta
				CheckBox alphaBeta = new CheckBox("AlphaBeta");
				alphaBeta.setSelected(getStrat(cPlayer.getValue()).alphaBeta);
				
				//Random Element
				CheckBox random = new CheckBox("Add Random Element");
				random.setSelected(getStrat(cPlayer.getValue()).addRand);
				
				//Nodes
				CheckBox nodes = new CheckBox("Nodes");
				nodes.setSelected(getStrat(cPlayer.getValue()).nodes);
				
				//Prevent Cycles
				CheckBox cycles = new CheckBox("Prevent Cycles");
				cycles.setSelected(getStrat(cPlayer.getValue()).preventCycles);

				//Transposition Table
				HBox ttt = new HBox();
				CheckBox tt = new CheckBox("Transposition Table");
				TextField ttd = new TextField();
				ttd.disableProperty().bind(tt.selectedProperty().not());
				ttd.setPromptText("Depth");
				ttt.getChildren().addAll(tt,ttd);
				tt.setSelected(getStrat(cPlayer.getValue()).transpositionTable);
				ttd.setText(getStrat(cPlayer.getValue()).transpositionTableDepth+"");
				App.SetUpHBox(ttt);
				App.SetUpTextField(ttd);
				

				//Killer Heuristic
				HBox kH = new HBox();
				CheckBox killerHeuristic = new CheckBox("Killer Heuristic");
				TextField kHDepth = new TextField();
				kHDepth.disableProperty().bind(killerHeuristic.selectedProperty().not());
				kHDepth.setPromptText("Depth");
				kH.getChildren().addAll(killerHeuristic,kHDepth);
				killerHeuristic.setSelected(getStrat(cPlayer.getValue()).killerHeuristic);
				kHDepth.setText(getStrat(cPlayer.getValue()).killerHeuristicDepth+"");
				App.SetUpHBox(kH);
				App.SetUpTextField(kHDepth);

				//Iterative Deepening
				HBox iD = new HBox();
				CheckBox iterativeDeepening = new CheckBox("Iterative Deepening");
				TextField iDDepth = new TextField();
				iDDepth.disableProperty().bind(iterativeDeepening.selectedProperty().not());
				iDDepth.setPromptText("Depth");
				iD.getChildren().addAll(iterativeDeepening, iDDepth);
				iterativeDeepening.setSelected(getStrat(cPlayer.getValue()).iterativeDeepening);
				iDDepth.setText(getStrat(cPlayer.getValue()).iterativedeepeningDepth+"");
				App.SetUpHBox(iD);
				App.SetUpTextField(iDDepth);
				
				cPlayer.valueProperty().addListener(e -> {
					editListeners(getStrat(cPlayer.getValue()), d, cd, alphaBeta, random, nodes, cycles, tt, ttd, killerHeuristic, kHDepth, iterativeDeepening, iDDepth);
				});
				editListeners(getStrat(cPlayer.getValue()), d, cd, alphaBeta, random, nodes, cycles, tt, ttd, killerHeuristic, kHDepth, iterativeDeepening, iDDepth);

				stratagy.getChildren().addAll(depth, cDepth, alphaBeta, random, nodes, cycles, ttt, kH, iD);
	}

	
	private void editListeners(Stratagy strat, 
			TextField depth, 
			TextField cDepth, 
			CheckBox alphaBeta, 
			CheckBox rand,
			CheckBox nodes,
			CheckBox cycles,
			CheckBox transpositionTable,
			TextField ttd,
			CheckBox killerHeuristic, 
			TextField kHDepth, 
			CheckBox IterativeDeepening, 
			TextField iDDepth){

		depth.textProperty().addListener(e -> {
			if(depth.getText().matches("\\d+")){
				int d = Integer.parseInt(depth.getText());
				if(d >= 0) {
					strat.depth = d;
					if(strat.transpositionTableDepth > d) {
						strat.transpositionTableDepth = d;
						ttd.setText(d+"");
					}
					if(strat.iterativedeepeningDepth > d) {
						strat.iterativedeepeningDepth = d;
						iDDepth.setText(d+"");
					}
					if(strat.killerHeuristicDepth > d) {
						strat.killerHeuristicDepth = d;
						kHDepth.setText(d+"");
					}
					if(strat.checkDepth > d) {
						strat.checkDepth = d;
						cDepth.setText(d+"");
					}
				}
				else if(depth.getText().length() > 0)
					depth.setText(""+strat.depth);	
				}
			else if(depth.getText().length() > 0)
				depth.setText(strat.depth+"");
		});
		
		cDepth.textProperty().addListener(e -> {
			if(cDepth.getText().matches("\\d+")){
				int cd = Integer.parseInt(cDepth.getText());
				if(cd <= strat.depth)
					strat.checkDepth = cd;
				else
					cDepth.setText(strat.depth+"");
				
			}
			else if(cDepth.getText().length() > 0)
				cDepth.setText(""+strat.checkDepth);
			
		});

		alphaBeta.selectedProperty().addListener(e -> {
			strat.alphaBeta = alphaBeta.isSelected();
		});

		rand.selectedProperty().addListener(e -> {
			strat.addRand = rand.isSelected();
		});
		
		nodes.selectedProperty().addListener(e -> {
			strat.nodes = nodes.isSelected();
		});
		
		cycles.selectedProperty().addListener(e -> {
			strat.preventCycles = cycles.isSelected();
		});

		transpositionTable.selectedProperty().addListener(e -> {
			strat.transpositionTable = transpositionTable.isSelected();
		});
		
		ttd.textProperty().addListener(e -> {
			if(ttd.getText().matches("\\d+")){
				int td = Integer.parseInt(ttd.getText());
				if(td <= strat.depth)
					strat.transpositionTableDepth = td;
				else
					ttd.setText(strat.depth+"");
				
			}
			else if(ttd.getText().length() > 0)
				ttd.setText(""+strat.transpositionTableDepth);
		});

		killerHeuristic.selectedProperty().addListener(e -> {
			strat.killerHeuristic = killerHeuristic.isSelected();
		});

		kHDepth.textProperty().addListener(e -> {
			if(kHDepth.getText().matches("\\d+")){
				int kh = Integer.parseInt(kHDepth.getText());
				if(kh <= strat.depth)
					strat.killerHeuristicDepth = kh;
				else
					kHDepth.setText(strat.depth+"");
			}
			else if(kHDepth.getText().length() > 0)
				kHDepth.setText(""+strat.killerHeuristicDepth);
		});

		IterativeDeepening.selectedProperty().addListener(e -> {
			strat.iterativeDeepening = IterativeDeepening.isSelected();
		});

		iDDepth.textProperty().addListener(e -> {
			if(iDDepth.getText().matches("\\d+")){
				int id = Integer.parseInt(iDDepth.getText());
				if(id <= strat.depth)
					strat.iterativedeepeningDepth = id;
				else
					iDDepth.setText(strat.depth+"");
			}
			else if(iDDepth.getText().length() > 0)
				iDDepth.setText(""+strat.iterativedeepeningDepth);
		});

	}

	private Stratagy getStrat(String player){
		if(player.equals("White"))
			return whiteStratagy;
		return blackStratagy;

	}
	}
