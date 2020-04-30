package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Switcher {
	Stage window;
	boolean answer;
	public Switcher() {
		window = new Stage();
		answer = false;
	}
	public boolean display(){
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Switch Piece");
		window.setHeight(120);
		window.setWidth(200);
		window.setResizable(false);
		
		BorderPane pane = new BorderPane();
		pane.setCenter(new Label("What piece would you like\nto replace this pawn with?"));
		HBox buttons = new HBox(10);
		Button q = new Button("Queen");
		Button k = new Button("Knight");
		q.setOnAction(e -> {
			answer = true;
			window.close();
		});
		k.setOnAction(e -> {
			answer = false;
			window.close();
		});
		buttons.getChildren().addAll(q, k);
		buttons.setAlignment(Pos.CENTER);
		pane.setBottom(buttons);
		BorderPane.setMargin(buttons, new Insets(10,10,10,10));
		
		Scene s = new Scene(pane);
		window.setScene(s);
		window.showAndWait();
		return answer;
	}
}
