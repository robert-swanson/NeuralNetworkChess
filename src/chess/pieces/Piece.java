package chess.pieces;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Point;

public abstract class Piece
{
	protected int value;
	protected boolean color;
//	public boolean hasMoved;
	public int moves;
	public Point position;

	public static Piece importPiece(String importString){
		Piece me;
		Boolean color;
		Point pos;
		String[] data = importString.split(":");
		if(data.length != 4)
			return null;
		
		//Position
		if(data[1].matches("\\(\\d, \\d\\)")){
			char x = data[1].charAt(1);
			char y = data[1].charAt(4);
			pos = new Point(Integer.parseInt(x+""),Integer.parseInt(""+y));
		}
		else
			return null;
		
		//Color
		if(data[2].equals("Black"))
			color = false;
		else if(data[2].equals("White"))
			color = true;
		else
			return null;
		
		//Type
		if(data[0].matches("\\d")){
			switch(Integer.parseInt(data[0])){
			case 1:
				me = new Pawn(color, pos);
				break;
			case 2:
				me = new Rook(color, pos);
				break;
			case 3:
				me = new Knight(color, pos);
				break;
			case 4:
				me = new Bishop(color, pos);
				break;
			case 5:
				me = new King(color, pos);
				break;
			case 6:
				me = new Queen(color, pos);
				break;
			default:
				System.out.println("Unkown Piece ID");
				return null;
			}
		}
		else
			return null;
		
		if(data[3].matches("\\d+"))
			me.moves = Integer.parseInt(data[3]);
		return me;
	}
	public Piece(int v, boolean c, Point pos)
	{
		value = v;
		color = c;
		moves = 0;
		position = pos;
	}

	public boolean isWhite()
	{
		return color;
	}

	public double getValue(int dist)
	{
		double rv = value;
		if(this instanceof Rook){
			return moves > 0 ? 4.9 : 5;
		}
		if(this instanceof King){
			return moves > 0 ? 99.9 : 100;
		}
		if(this instanceof Pawn){
			switch(dist){
			case 5:
				return 2;
			case 4:
				return 1.5;
			case 3:
				return 1.25;
			case 2:
				return 1.2;
			case 1:
				return 1.1;
			default:
				return 1;
			}
		}
			
		return rv;
	}
	
/*	protected void checkOptions(ArrayList<Move> moves, Board board){ //Removes moves where own piece already occupies
		for(Move move: moves){
			Boolean from = board.getWhoOccupiesAt(move.from);
			Boolean to = board.getWhoOccupiesAt(move.to);
			if(from.equals(null) || from.equals(to))
				moves.remove(move);
		}
	}	*/
	@Override
	public String toString() {
		if(this instanceof Bishop)
			return "Bishop";
		else if(this instanceof King)
			return "King";
		else if(this instanceof Knight)
			return "Knight";
		else if(this instanceof Pawn)
			return "Pawn";
		else if(this instanceof Queen)
			return "Queen";
		else if(this instanceof Rook)
			return "Rook";
		else
			return "Unkown Piece";
	}
	public char toChar(){
		if(this instanceof Knight)
			return color ? 'N' : 'n';
		if(color)
			return (Character.toUpperCase(toString().charAt(0)));
		else
			return (Character.toLowerCase(toString().charAt(0)));
	}
	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(this.toString());
	}
	public abstract ArrayList<Move> getMoves(Board board, Point pos);
	
	public int getPieceHash(){
		if(this instanceof Pawn && moves == 0)
			return (color ? 14 : 15);
		if(this instanceof Rook && moves == 0)
			return (color ? 12 : 13);
		int rv = getPieceID()-1;
		if(!color)
			rv += 6;
		return rv;
	}
	public int getPieceID(){
		if(this instanceof Pawn)
			return 1;
		if(this instanceof Rook)
			return 2;
		if(this instanceof Knight)
			return 3;
		if(this instanceof Bishop)
			return 4;
		if(this instanceof King)
			return 5;
		if(this instanceof Queen)
			return 6;
		else
			return 0;
	}
	
	public String export(){
		return String.format("%d:%s:%s:%d", getPieceID(), position, color ? "White" : "Black", moves);
	}

}
