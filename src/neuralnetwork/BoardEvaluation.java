package neuralnetwork;

import chess.board.Board;
import neuralnetwork.NN;

import java.util.Arrays;

public class BoardEvaluation {
    long[] board;
    double minimaxScore;
    int depth;
    int gameOutcome;
    String player;

    public BoardEvaluation(Board board) {
        this.board = board.getCondensedBoardInputLayer();
    }

    public BoardEvaluation(long[] board, double minimaxScore, int depth, String player){
        this.board = board;
        this.minimaxScore = minimaxScore;
        this.depth = depth;
        this.player = player;
    }

    public BoardEvaluation(String inputString) {
        String[] input = inputString.split("[ ,]+");
        try{
            player = input[0];
            gameOutcome = Integer.parseInt(input[1]);
            minimaxScore = Double.parseDouble(input[2]);
            depth = Integer.parseInt(input[3]);
            board = new long[12];
            for(int i = 0; i < 12; i++){
                board[i] = Long.parseLong(input[4+i]);
            }
        } catch (Exception e){
            System.err.printf("Unable to read board evaluation line: %s\n", e.getMessage());
        }

    }

    public double[] getExpandedBoardInputLayer() {
        assert board.length == 12;
        double[] rv = new double[NN.BOARD_LAYER_SIZE];

        for (int pieceHashIndex = 0; pieceHashIndex < 12; pieceHashIndex++) {
            for (int loc = 0; loc < 64; loc++) {
                if ((board[pieceHashIndex] & (1 << (long)(loc))) != 0) {
                    rv[loc] = 1.0;
                }
            }
        }
        return rv;
    }

    public String getSaveString(int gameOutcome){
        StringBuilder line = new StringBuilder(String.format("%7s, %1d, %20f, %2d", player, gameOutcome, minimaxScore, depth));
        for(long l: board) line.append(String.format(", %20d",l));
        return line.toString()+"\n";
    }

    public double getLabel(NN.LabelingMethod method) {
        return switch (method) {
            case GameOutcome -> (gameOutcome == -1) ? 0 : ((gameOutcome == 1) ? 1 : .5);
            case StandardScore -> NN.sigmoid(minimaxScore);
        };
    }

}
