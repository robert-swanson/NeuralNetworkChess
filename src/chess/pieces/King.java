package chess.pieces;

import java.util.ArrayList;

import chess.board.Board;
import chess.board.Move;
import chess.board.Point;

public class King extends Piece
{	
	public King(boolean c, Point pos)
	{
		super(100, c, pos);
	}
	

	//Still to do
	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		boolean maybeCastle = false;
		for (int i = 0; i < 8; i++)
		{
			Point to = pos.getNewPoint(1, i);
			Boolean who = board.getWhoOccupiesAt(to);
			if(to.isInBoard() && (who == null || who == !color)){
				moves.add(new Move(pos, to, board));
				int y = board.rules.isTopPlayer() == isWhite() ? 0 : 7;
				if(to.y == y && (to.x == 2 || to.x == 4))
					maybeCastle = true;
			}
		}
		if (maybeCastle)
			board.addCastleMoves(moves, isWhite());
		board.setCaptures(moves);
		return moves;
	}
}
