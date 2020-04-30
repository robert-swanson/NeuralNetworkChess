package application;

import chess.board.RuleSet;
import chess.ai.Strategy;
import chess.board.RuleSet.GameMode;
import chess.board.Board;
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
	VBox strategy = new VBox();
	boolean mustRestartOnClose = false;

	RuleSet rules;
	Strategy blackStrategy;
	Strategy whiteStrategy;

	public SettingsView(Board board){
		window = new Stage();
		rules = board.rules;
		blackStrategy = board.black.stratagy;
		whiteStrategy = board.white.stratagy;
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
		window.setHeight(rules.getMode() == GameMode.pvp ? 320 : 645);
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
		gameMode.setValue(rules.getMode().toString());
		
		gameMode.valueProperty().addListener(e -> {
			System.out.println("Changed Gamemode to:" + gameMode.getValue());
			String m = gameMode.getValue();
			if(m.equals("Player vs Player")){
				rules.setMode(RuleSet.GameMode.pvp);
				window.setHeight(320);
			}
			else if(m.equals("Player vs Computer")){
				rules.setMode(RuleSet.GameMode.pvc);
				window.setHeight(645);

			}
			else{
				rules.setMode(RuleSet.GameMode.cvc);
				window.setHeight(645);
			}
			layout.getChildren().remove(strategy);
			initStratagyView(rules.getMode());
			layout.getChildren().add(layout.getChildren().size()-1, strategy);
		});		initStratagyView(rules.getMode());
		//Rules
		//Debug
		CheckBox debug = new CheckBox("Debug Mode");
		debug.setSelected(rules.isDebug());
		debug.selectedProperty().addListener(e -> {
			rules.setDebug(debug.isSelected());
		});
		//Undo
		CheckBox undo = new CheckBox("Allow Undo");
		undo.setSelected(rules.isUndo());
		undo.selectedProperty().addListener(e -> {
			rules.setUndo(undo.isSelected());
			mustRestartOnClose = true;
		});
		
		//CastleTCheck
		CheckBox throughCheck = new CheckBox("Can't Castle Through Check");
		throughCheck.setSelected(rules.isCantCastleThroughCheck());
		throughCheck.selectedProperty().addListener(e -> {
			rules.setCantCastleThroughCheck(throughCheck.isSelected());
		});

		//CastleACheck
		CheckBox afterCheck = new CheckBox("Can't Castle After Check");
		afterCheck.setSelected(rules.isCantCastleAfterCheck());
		afterCheck.selectedProperty().addListener(e -> {
			rules.setCantCastleAfterCheck(afterCheck.isSelected());
		});

		//Top Player
		HBox topPlayer = new HBox();
		Label tpl = new Label("Top Player");
		ObservableList<String> players = FXCollections.observableArrayList(
				"White", "Black");
		ComboBox<String> tp = new ComboBox<>(players);
		tp.setValue(rules.isTopPlayer() ? "White" : "Black");
		topPlayer.getChildren().addAll(tpl,tp);
		App.SetUpHBox(topPlayer);
		tp.valueProperty().addListener(e -> {
			mustRestartOnClose = true;
			if(tp.getValue().equals("Black"))
				rules.setTopPlayer(false);
			else
				rules.setTopPlayer(true);
		});

		HBox timeLimit = new HBox();
		Label tl1 = new Label("Time Limit");
		Label tl2 = new Label("m");
		Label tl3 = new Label("s");
		ObservableList<String> timeLimitOptions = FXCollections.observableArrayList(
				"Off", "Total", "Turn");
		ComboBox<String> tl = new ComboBox<>(timeLimitOptions);
		String limit = "";
		switch (rules.getTimeLimit()) {
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
		tlM.setText(""+ rules.getTimeLimit().minutes);
		TextField tlS = new TextField();
		tlS.setText(""+ rules.getTimeLimit().seconds);
		tl.valueProperty().addListener(e -> {
			String v = tl.getValue();
			tlS.setDisable(v.equals("Off"));
			tlM.setDisable(v.equals("Off"));

			if(v.equals("Off"))
				rules.setTimeLimit(RuleSet.TimeLimit.off);
			else if(v.equals("Total"))
				rules.setTimeLimit(RuleSet.TimeLimit.total);
			else if(v.equals("Turn"))
				rules.setTimeLimit(RuleSet.TimeLimit.turn);
			tlS.setText(rules.getTimeLimit().seconds+"");
			tlM.setText(rules.getTimeLimit().minutes+"");
		});
		tlM.textProperty().addListener(e -> {
			String v = tlM.getText();
			if(v.matches("\\d+"))
				rules.getTimeLimit().minutes = Integer.parseInt(v);
			else if(v.length() > 0)
				tlM.setText(""+ rules.getTimeLimit().minutes);
		});
		tlS.textProperty().addListener(e -> {
			String v = tlS.getText();
			if(v.matches("\\d+"))
				rules.getTimeLimit().seconds = Integer.parseInt(v);
			else if(v.length() > 0)
				tlS.setText(""+ rules.getTimeLimit().seconds);
		});
		tlS.setDisable(rules.getTimeLimit() == RuleSet.TimeLimit.off);
		tlM.setDisable(rules.getTimeLimit() == RuleSet.TimeLimit.off);
		tlS.setMaxWidth(40);
		tlM.setMaxWidth(30);
		App.SetUpHBox(timeLimit);
		App.SetUpHBox(topPlayer);
		
		Separator sep = new  Separator(Orientation.HORIZONTAL);
		
		timeLimit.getChildren().addAll(tl1,tl, tlM, tl2, tlS, tl3);

		layout.getChildren().addAll(debug, undo, throughCheck, afterCheck, topPlayer, sep, gameMode, strategy, ok);
		App.SetMargins(layout);

		Scene s = new Scene(layout);
		window.setScene(s);
		window.centerOnScreen();
		window.showAndWait();
		return mustRestartOnClose;
	}

	private void initStratagyView(RuleSet.GameMode mode){
		strategy = new VBox();
		App.SetUpVBox(strategy);

		if(mode == RuleSet.GameMode.pvp)
			return;

		//Player Picker
		ObservableList<String> players = FXCollections.observableArrayList(
				"White", "Black");
		ComboBox<String> cPlayer = new ComboBox<>(players);
		cPlayer.setValue(rules.isComputerPlayer() ? "White" : "Black");
		Label l = new Label(RuleSet.GameMode.cvc == mode ? "" : "Computer Player");
		HBox player = new HBox();
		player.getChildren().addAll(l, cPlayer);
		App.SetUpHBox(player);
		cPlayer.valueProperty().addListener(e -> {
			rules.setComputerPlayer(cPlayer.getValue().equals("White"));
			strategy.getChildren().clear();
			strategy.getChildren().add(player);
			initAISpecifics(strategy, cPlayer);
		});
		strategy.getChildren().add(player);
		initAISpecifics(strategy, cPlayer);
	}
	
	private void initAISpecifics(VBox layout, ComboBox<String> cPlayer){
				//Depth
				HBox depth = new HBox();
				Label dl = new Label("Depth");
				TextField d = new TextField();
				d.setText(""+getStrat(cPlayer.getValue()).getDepth());
				depth.getChildren().addAll(dl,d);
				App.SetUpHBox(depth);
				App.SetUpTextField(d);
				
				//Check Depth
				HBox cDepth = new HBox();
				Label cdl = new Label("Check Depth");
				TextField cd = new TextField();
				cd.setText(""+getStrat(cPlayer.getValue()).getCheckDepth());
				cDepth.getChildren().addAll(cdl,cd);
				App.SetUpHBox(cDepth);
				App.SetUpTextField(cd);
				

				//AlphaBeta
				CheckBox alphaBeta = new CheckBox("AlphaBeta");
				alphaBeta.setSelected(getStrat(cPlayer.getValue()).isAlphaBeta());
				
				//Random Element
				CheckBox random = new CheckBox("Add Random Element");
				random.setSelected(getStrat(cPlayer.getValue()).isAddRand());
				
				//Nodes
				CheckBox nodes = new CheckBox("Nodes");
				nodes.setSelected(getStrat(cPlayer.getValue()).isNodes());
				
				//Prevent Cycles
				CheckBox cycles = new CheckBox("Prevent Cycles");
				cycles.setSelected(getStrat(cPlayer.getValue()).isPreventCycles());

				//Transposition Table
				HBox ttt = new HBox();
				CheckBox tt = new CheckBox("Transposition Table");
				TextField ttd = new TextField();
				ttd.disableProperty().bind(tt.selectedProperty().not());
				ttd.setPromptText("Depth");
				ttt.getChildren().addAll(tt,ttd);
				tt.setSelected(getStrat(cPlayer.getValue()).isTranspositionTable());
				ttd.setText(getStrat(cPlayer.getValue()).getTranspositionTableDepth() +"");
				App.SetUpHBox(ttt);
				App.SetUpTextField(ttd);
				

				//Killer Heuristic
				HBox kH = new HBox();
				CheckBox killerHeuristic = new CheckBox("Killer Heuristic");
				TextField kHDepth = new TextField();
				kHDepth.disableProperty().bind(killerHeuristic.selectedProperty().not());
				kHDepth.setPromptText("Depth");
				kH.getChildren().addAll(killerHeuristic,kHDepth);
				killerHeuristic.setSelected(getStrat(cPlayer.getValue()).isKillerHeuristic());
				kHDepth.setText(getStrat(cPlayer.getValue()).getKillerHeuristicDepth() +"");
				App.SetUpHBox(kH);
				App.SetUpTextField(kHDepth);

				//Iterative Deepening
				HBox iD = new HBox();
				CheckBox iterativeDeepening = new CheckBox("Iterative Deepening");
				TextField iDDepth = new TextField();
				iDDepth.disableProperty().bind(iterativeDeepening.selectedProperty().not());
				iDDepth.setPromptText("Depth");
				iD.getChildren().addAll(iterativeDeepening, iDDepth);
				iterativeDeepening.setSelected(getStrat(cPlayer.getValue()).isIterativeDeepening());
				iDDepth.setText(getStrat(cPlayer.getValue()).getIterativedeepeningDepth() +"");
				App.SetUpHBox(iD);
				App.SetUpTextField(iDDepth);
				
				cPlayer.valueProperty().addListener(e -> {
					editListeners(getStrat(cPlayer.getValue()), d, cd, alphaBeta, random, nodes, cycles, tt, ttd, killerHeuristic, kHDepth, iterativeDeepening, iDDepth);
				});
				editListeners(getStrat(cPlayer.getValue()), d, cd, alphaBeta, random, nodes, cycles, tt, ttd, killerHeuristic, kHDepth, iterativeDeepening, iDDepth);

				strategy.getChildren().addAll(depth, cDepth, alphaBeta, random, nodes, cycles, ttt, kH, iD);
	}

	
	private void editListeners(Strategy strat,
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
					strat.setDepth(d);
					if(strat.getTranspositionTableDepth() > d) {
						strat.setTranspositionTableDepth(d);
						ttd.setText(d+"");
					}
					if(strat.getIterativedeepeningDepth() > d) {
						strat.setIterativedeepeningDepth(d);
						iDDepth.setText(d+"");
					}
					if(strat.getKillerHeuristicDepth() > d) {
						strat.setKillerHeuristicDepth(d);
						kHDepth.setText(d+"");
					}
					if(strat.getCheckDepth() > d) {
						strat.setCheckDepth(d);
						cDepth.setText(d+"");
					}
				}
				else if(depth.getText().length() > 0)
					depth.setText(""+ strat.getDepth());
				}
			else if(depth.getText().length() > 0)
				depth.setText(strat.getDepth() +"");
		});
		
		cDepth.textProperty().addListener(e -> {
			if(cDepth.getText().matches("\\d+")){
				int cd = Integer.parseInt(cDepth.getText());
				if(cd <= strat.getDepth())
					strat.setCheckDepth(cd);
				else
					cDepth.setText(strat.getDepth() +"");
				
			}
			else if(cDepth.getText().length() > 0)
				cDepth.setText(""+ strat.getCheckDepth());
			
		});

		alphaBeta.selectedProperty().addListener(e -> {
			strat.setAlphaBeta(alphaBeta.isSelected());
		});

		rand.selectedProperty().addListener(e -> {
			strat.setAddRand(rand.isSelected());
		});
		
		nodes.selectedProperty().addListener(e -> {
			strat.setNodes(nodes.isSelected());
		});
		
		cycles.selectedProperty().addListener(e -> {
			strat.setPreventCycles(cycles.isSelected());
		});

		transpositionTable.selectedProperty().addListener(e -> {
			strat.setTranspositionTable(transpositionTable.isSelected());
		});
		
		ttd.textProperty().addListener(e -> {
			if(ttd.getText().matches("\\d+")){
				int td = Integer.parseInt(ttd.getText());
				if(td <= strat.getDepth())
					strat.setTranspositionTableDepth(td);
				else
					ttd.setText(strat.getDepth() +"");
				
			}
			else if(ttd.getText().length() > 0)
				ttd.setText(""+ strat.getTranspositionTableDepth());
		});

		killerHeuristic.selectedProperty().addListener(e -> {
			strat.setKillerHeuristic(killerHeuristic.isSelected());
		});

		kHDepth.textProperty().addListener(e -> {
			if(kHDepth.getText().matches("\\d+")){
				int kh = Integer.parseInt(kHDepth.getText());
				if(kh <= strat.getDepth())
					strat.setKillerHeuristicDepth(kh);
				else
					kHDepth.setText(strat.getDepth() +"");
			}
			else if(kHDepth.getText().length() > 0)
				kHDepth.setText(""+ strat.getKillerHeuristicDepth());
		});

		IterativeDeepening.selectedProperty().addListener(e -> {
			strat.setIterativeDeepening(IterativeDeepening.isSelected());
		});

		iDDepth.textProperty().addListener(e -> {
			if(iDDepth.getText().matches("\\d+")){
				int id = Integer.parseInt(iDDepth.getText());
				if(id <= strat.getDepth())
					strat.setIterativedeepeningDepth(id);
				else
					iDDepth.setText(strat.getDepth() +"");
			}
			else if(iDDepth.getText().length() > 0)
				iDDepth.setText(""+ strat.getIterativedeepeningDepth());
		});

	}

	private Strategy getStrat(String player){
		if(player.equals("White"))
			return whiteStrategy;
		return blackStrategy;

	}
	}
