package chess.pieces;

import java.util.ArrayList;

import chess.board.Board;
import chess.board.Move;
import chess.board.Point;

public class Queen extends Piece
{
	public Queen(boolean c, Point pos)
	{
		super(9, c, pos);
	}

	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		boolean me = board.getWhoOccupiesAt(pos);
		ArrayList<Move> moves = new ArrayList<Move>();
		for(int dir = 0; dir <= 7; dir += 1){
			for(int dis = 1; dis <= 7; dis++){
				Point p = pos.getNewPoint(dis, dir);
				if(p.isInBoard() && board.getWhoOccupiesAt(p) == null){
					moves.add(new Move(pos, p, board));
				}
				else if(p.isInBoard()){
					if(board.getWhoOccupiesAt(p) == !me)
						moves.add(new Move(pos, p, board));
					break;
				}
				else
					break;
			}
		}
		board.setCaptures(moves);
		return moves;
	}
}
