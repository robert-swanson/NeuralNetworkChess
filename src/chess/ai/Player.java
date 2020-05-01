package chess.ai;

import chess.board.Board;
import chess.board.Move;

public interface Player {
    public Move getBestMove();
}
