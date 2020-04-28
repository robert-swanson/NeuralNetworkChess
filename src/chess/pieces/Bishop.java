package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Bishop extends Piece
{
	public Bishop(boolean c, Point pos)
	{
		super(3,c, pos);
	}

	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		boolean me = board.getWhoOccupiesAt(pos);
		ArrayList<Move> moves = new ArrayList<Move>();
		for(int dir = 1; dir <= 7; dir += 2){
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
