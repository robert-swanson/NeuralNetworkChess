package chess.pieces;

import java.util.ArrayList;

import chess.board.Board;
import chess.board.Move;
import chess.board.Point;

public class Knight extends Piece
{
	public Knight(boolean c, Point pos)
	{
		super(3, c, pos);
	}

	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		boolean me = board.getWhoOccupiesAt(pos);
		ArrayList<Move> moves = new ArrayList<Move>();
		ArrayList<Point> points = new ArrayList<>();
		for(int d = 0; d < 8; d+= 2){
			Point diag = pos.getNewPoint(2, d);
			if(d == 0 || d == 4){
				points.add(diag.getNewPoint(1, 6));
				points.add(diag.getNewPoint(1, 2));
			}
			else{
				points.add(diag.getNewPoint(1, 0));
				points.add(diag.getNewPoint(1, 4));
			}
		}
		
		for(Point p: points){
			if(p.isInBoard() && board.getWhoOccupiesAt(p) == null){
				moves.add(new Move(pos, p, board));
			}
			else if(p.isInBoard()){
				if(board.getWhoOccupiesAt(p) == !me)
					moves.add(new Move(pos, p, board));
			}
		}
		board.setCaptures(moves);
		return moves;
	}
}
