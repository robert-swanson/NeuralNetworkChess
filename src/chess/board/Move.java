package chess.board;

import java.util.ArrayList;

import chess.pieces.King;
import chess.pieces.Piece;
import chess.pieces.Rook;

/**
 * Describes a move, the piece moving, and if a piece was captured
 */
public class Move implements Comparable<Move>
{
	Board board;
	public Piece piece;
	public Piece changedTo;
	public Point from;
	public Point to;
	public boolean me;
	
	public double score;
	public double progressScore;
	public boolean maximizer;

	private Piece capturedPiece;
	boolean capturedKing;
	Boolean checks;
	
	boolean firstMove;
	public boolean castlingMove;

	public Move(Move m){
		this.board = m.board;
		this.piece = m.piece;
		this.changedTo = m.changedTo;
		this.from = m.from;
		this.to = m.to;
		this.me = m.me;
		this.capturedKing = m.capturedKing;
		this.checks = m.checks;
		this.firstMove = m.firstMove;
		this.castlingMove = m.castlingMove;
		this.maximizer = m.maximizer;
		this.progressScore  = m.progressScore;
	}
	public Move(Point from, Point to){
		this(from, to, null);
	}
	public Move(Point from, Point to, Board board)
	{
		this.from = from;
		this.to = to;
		this.board = board;
		this.piece = board.getPiece(from);
		this.score = -9999;
		this.maximizer = true;
		if(this == null || piece == null){
			System.out.println("No Piece at " + from);
		}
		this.firstMove = !(piece.moves > 0);
		this.castlingMove = false;
		changedTo = null;
		me = piece.isWhite();
	}
	
	public Piece getCapture(){
		return capturedPiece;
	}
	public void setCapture(Piece cap){
		capturedPiece = cap;
		if(cap instanceof King){
			capturedKing = true;
		}
	}

	/**
	 * Checks if move can be made, and sets rules such as checkMated,
	 * invalidated castle, and captured piece
	 * 
	 * @return Boolean indicated whether the move can be made
	 */
	public boolean validateMove()
	{
		if (!from.isInBoard() || !to.isInBoard())
			return false;

		return false;
	}

	/**
	 * Does the move to the board
	 */
	public boolean doMove()
	{
		board.turn = !board.turn;
		Boolean wFrom = board.getWhoOccupiesAt(from);
		Boolean wTo = board.getWhoOccupiesAt(to);
		boolean captured = wTo != null && wTo == !me;
		if(wFrom != null && wFrom == me && (wTo == null || wTo == !me)){
			board.putPiece(board.removePiece(from, me), to);
			if(captured)
				board.removePiece(to, !me);
		}
		piece.position = to;
		if(board == null || to == null)
			System.out.println(this + " board is null");
		board.getPiece(to).moves++;
		if(castlingMove){
			boolean left = to.x < 3;
			int y = me == board.rules.isTopPlayer() ? 0 : 7;
			if(left){
				Rook rook = (Rook)board.removePiece(new Point(0, y), me);
				board.putPiece(rook, new Point(3, y));
				rook.moves++;
				rook.position = new Point(3, y);
			}
			else{
				Rook rook = (Rook)board.removePiece(new Point(7, y), me);
				board.putPiece(rook, new Point(5, y));
				rook.moves++;
				rook.position = new Point(5, y);
			}
		}
		else if(changedTo != null)
			board.putPiece(changedTo, to);
		if(checks != null && checks && board.rules.isCantCastleAfterCheck())
			board.getKing(!me).moves = 1;
		board.history.push(this);
		return captured;
	}

	/**
	 * Undoes the move to the board, and replaced captured pieces
	 */
	public Piece undoMove()
	{
		board.turn = !board.turn;
		piece.position = from;
		piece.moves--;
		if(me && board.whitePieces.containsKey(to)){
			board.whitePieces.put(from, board.whitePieces.remove(to));
		}
		else if(!me && board.blackPieces.containsKey(to)){
			board.blackPieces.put(from, board.blackPieces.remove(to));
		}
		else
			System.err.printf("no piece\n");
		if(capturedPiece != null)
			board.putPiece(capturedPiece, to);
		if(firstMove){
			board.getPiece(from).moves = 0;	
		}
		if(checks != null && checks)
			board.getKing(!me).moves = 0;
		if(castlingMove){
			boolean left = to.x < 4;
			int y = me == board.rules.isTopPlayer() ? 0 : 7;
			if(left){
				Rook rook = (Rook)board.removePiece(new Point(3, y), me);
				rook.position = new Point(0, y);
				rook.moves--;
				board.putPiece(rook, new Point(0, y));
			}
				
			else{
				Rook rook = (Rook)board.removePiece(new Point(5, y), me);
				rook.position = new Point(7, y);
				rook.moves--;
				board.putPiece(rook, new Point(7, y));
			}
		}
		if(changedTo != null)
			board.putPiece(piece, from);
		board.history.pop();
		return capturedPiece;
	}
	
	public boolean putsPlayerInCheck(boolean color){
		doMove();
		ArrayList<Point> points = new ArrayList<>();
		if(color)
			points.addAll(board.blackPieces.keySet());
		else
			points.addAll(board.whitePieces.keySet());
		for(Point p: points){
			Piece piece = board.getPiece(p);
			for(Move move: piece.getMoves(board, p)){
				if(move.capturedKing){
					undoMove();
					if(board.rules.isCantCastleAfterCheck() && !(board.getKing(!me).moves > 0))
						checks = true;
					return true;
				}
					
			}
		}
		undoMove();
		checks = false;
		return false;
	}

	@Override
	public String toString()
	{
		String rv = String.format("%s %s -> %s",piece, from.toString(), to.toString());
		if (capturedPiece != null)
		{
			rv += String.format(", Captured %s", capturedPiece.toString());
		}
		if (castlingMove)
			rv += ", Castling Move";
		return rv;
	}
	
	@Override
	public boolean equals(Object obj) {		//Ignores properties
		if(obj instanceof Move){
			Move m = (Move)obj;
			boolean t = changedTo != null;
			boolean i = m.changedTo != null;
			if(t && i)
				t = changedTo.equals(m.changedTo);
				
			return m.from.equals(from) && m.to.equals(to) && m.piece.equals(piece) && t == i;
		}
		return false;
	}
	
	
	@Override
	public int hashCode() {
		int rv = (capturedPiece == null) ? 0 : capturedPiece.getPieceID();
		rv += 10 * piece.getPieceID();
		rv += 100 * to.y;
		rv += 1000 * to.x;
		rv += 10000 * from.y;
		rv += 100000 * from.x;
		rv += 1000000 * (firstMove ? 1 : 0);
		rv *= me ? 1 : -1;
		return rv;
	}
	@Override
	public int compareTo(Move o) {
		if(score == o.score)
			return 0;
		return (int)(score - o.score) * (maximizer ? 1 : -1);
	}
}
