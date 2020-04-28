package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public class Pawn extends Piece
{
	public Pawn(boolean c, Point pos)
	{
		super(1, c, pos);
	}


	public ArrayList<Move> getMoves(Board board, Point pos)
	{
		boolean up = board.rules.topPlayer != this.color;
		ArrayList<Move> moves = new ArrayList<Move>();
		Point jump = pos.getNewPoint(2, (up ? 0 : 4));
		Boolean jumpO = board.getWhoOccupiesAt(jump);
		
		Point capLeft = pos.getNewPoint(1, (up ? 7 : 5));
		Boolean capLeftO = board.getWhoOccupiesAt(capLeft);
		
		Point capRight = pos.getNewPoint(1, (up ? 1 : 3));
		Boolean capRightO = board.getWhoOccupiesAt(capRight);
		
		Point forward = pos.getNewPoint(1, (up ? 0 : 4));
		Boolean forwardO = board.getWhoOccupiesAt(forward);
		
		
		if(capLeftO != null && capLeftO == !this.color && capLeft.isInBoard()){
			Move cl = new Move(pos, capLeft,board);
			addSwitch(cl, moves);
			}
		if(capRightO != null && capRightO == !this.color && capRight.isInBoard()){
			Move cr = new Move(pos, capRight, board);
			addSwitch(cr, moves);
			}
		if(forwardO == null && forward.isInBoard()){
			Move m = new Move(pos, forward,board);
			addSwitch(m, moves);
			if((this.moves <= 0) && jumpO == null)
				moves.add(new Move(pos, jump, board));
		}
		board.setCaptures(moves);
		return moves;
	}
	private void addSwitch(Move m, ArrayList<Move> moves){
		if(m.to.y == 0 || m.to.y == 7){
			Move q = new Move(m);
			Move k  = new Move(m);
			q.changedTo = new Queen(m.me, m.to);
			k.changedTo = new Knight(m.me, m.to);
			moves.add(q);
			moves.add(k);
		}
		else
			moves.add(m);
	}
}
