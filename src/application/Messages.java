package application;

import chess.board.State;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Handles displaying game messages to the user
 * @author Robert Swanson
 */
public class Messages{
    private Label label;
    private Timeline tl;
    private Duration duration;
    private FadeTransition fade;
    private State gameState;

    public Messages(Label l) {
        label = l;
        fade = new FadeTransition();
        updateTimeLine(Duration.ZERO);
    }

    public void invalidMove(){
        message("Invalid Move", Duration.seconds(3));
    }

    public void notYourTurn(){
        message("It's not your turn",Duration.seconds(3));
    }

    public void gameOver(){
        if(gameState == State.BLACKWON)
            message("Black Won!", Duration.seconds(10));
        else if(gameState == State.WHITEWON)
            message("White Won!", Duration.seconds(10));
        else if(gameState == State.STALEMATE)
            message("Stalemate!",Duration.seconds(10));
        else
            message("Game Over ERROR", Duration.seconds(3));
    }

    public void message(String message, Duration d){
        tl.stop();
        fade.stop();
        updateTimeLine(d);
        label.setOpacity(1);
        label.setText(message);
        tl.play();
    }

    public void clear(){
        label.setText("");
        tl.stop();
        fade.stop();
    }

    public void setGameState(State gameState) {
        this.gameState = gameState;
    }

    private void updateTimeLine(Duration d){
        duration = d;
        tl = new Timeline(new KeyFrame(
                d,
                ae -> fade()));
    }

    private void fade(){
        if(duration == Duration.INDEFINITE)
            return;
        fade = new FadeTransition();
        fade.setNode(label);
        fade.setDuration(Duration.seconds(1));
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        label.setOpacity(1);
        fade.setOnFinished(e -> label.setText(""));
        fade.play();
    }
}
