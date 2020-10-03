package chess.board;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import chess.ai.AI;
import chess.ai.Strategy;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import neuralnetwork.BoardEvaluation;

/**
 * Manages the black and white pieces
 */
public class Board {
/**
	 * Describes a piece on the board
	 */
	//TODO: Make getters and setters for members
	public HashMap<Point, Piece> whitePieces;
	public HashMap<Point, Piece> blackPieces;

	public AI black;
	public AI white;
	
	King whiteKing;
	King blackKing;

	public RuleSet rules;
	public Boolean allowance;

	public Stack<Move> history;
	public State gameState;
	public boolean winning;

	public boolean turn;
	
	private long startTime;
	
	public long avTotal = 0;
	public long avCount = 0;
	
	SimpleDoubleProperty whiteTime;
	SimpleDoubleProperty blackTime;

	LinkedList<BoardEvaluation> evaluationBuffer;

	/**
	 * Initializes the pieces on the board according to what player is on the top
 	 * @param allowance
	 * @param p
	 */
	public Board(SimpleBooleanProperty allowance, SimpleDoubleProperty p) {
		evaluationBuffer = new LinkedList<>();
		black = new AI(this, false, allowance, p);
		white = new AI(this, true, allowance, p);
		rules = new RuleSet();
		setUpBoard();
		gameState = State.INPROGRESS;
		winning = false;
		whiteTime = new SimpleDoubleProperty(rules.timeLimit.toSeconds());
		blackTime = new SimpleDoubleProperty(rules.timeLimit.toSeconds());

	}
	
	public Board(SimpleBooleanProperty allowance, String importString, SimpleDoubleProperty p){
		this(allowance, p);
		String[] input = importString.split("_");
		if(input.length != 8 || !input[0].equals("$$$") || ! input[7].equals("$$$")){
			System.out.println("Import Failed");
			return;
		}
		//Pieces
		whitePieces = new HashMap<>();
		blackPieces = new HashMap<>();
		String[] pieces = input[1].split("-");
		for(String piece: pieces){
			Piece n = Piece.importPiece(piece);
			whitePieces.put(n.position, n);
			if(n instanceof King)
				whiteKing = (King)n;
		}
		pieces = input[2].split("-");
		for(String piece: pieces){
			Piece n = Piece.importPiece(piece);
			blackPieces.put(n.position, n);
			if(n instanceof King)
				blackKing = (King)n;
		}
		
		//Turn
		turn = input[3].equals("White");
		
		//Rules
		rules = new RuleSet(input[4]);
		
		//Stratagy
		white.stratagy = new Strategy(input[5]);
		black.stratagy = new Strategy(input[6]);

		// Board Evaluation
		evaluationBuffer = new LinkedList<>();
	}
	
	public void setUpBoard(){
		white.resetKHTT();
		black.resetKHTT();
		boolean topPlayer = rules.isTopPlayer();
		gameState = State.INPROGRESS;
		whitePieces  = new HashMap<>();
		blackPieces  = new HashMap<>();
		turn = true;

		history = new Stack<>();
		evaluationBuffer.clear();

		//Pawns
		Point p;
		for(int x = 0; x < 8; x++){
			p = new Point(x, (topPlayer ? 1 : 6));
			whitePieces.put(p, new Pawn(true,p));
			p = new Point(x, (topPlayer ? 6 : 1));
			blackPieces.put(p, new Pawn(false,p));
		}

		int whiteY = topPlayer ? 0 : 7;
		int blackY = topPlayer ? 7 : 0;

		p = new Point(0, whiteY);
		whitePieces.put(p, new Rook(true, p));
		p = new Point(1, whiteY);
		whitePieces.put(p, new Knight(true, p));
		p = new Point(2, whiteY);
		whitePieces.put(p, new Bishop(true, p));
		if(!topPlayer){
			p = new Point(3, whiteY);
			whitePieces.put(p, new Queen(true, p));
			p = new Point(4, whiteY);
			whiteKing = new King(true, p);
			whitePieces.put(p, whiteKing);
		}
		else{
			p = new Point(3, whiteY);
			whiteKing = new King(true, p);
			whitePieces.put(p, whiteKing);
			p = new Point(4, whiteY);
			whitePieces.put(p, new Queen(true, p));
		}
		p = new Point(5, whiteY);
		whitePieces.put(p, new Bishop(true, p));
		p = new Point(6, whiteY);
		whitePieces.put(p, new Knight(true, p));
		p = new Point(7, whiteY);
		whitePieces.put(p, new Rook(true, p));
		
		p = new Point(0, blackY);
		blackPieces.put(p, new Rook(false, p));
		p = new Point(1, blackY);
		blackPieces.put(p, new Knight(false, p));
		p = new Point(2, blackY);
		blackPieces.put(p, new Bishop(false, p));
		if(!topPlayer){
			p = new Point(3, blackY);
			blackPieces.put(p, new Queen(false, p));
			p = new Point(4, blackY);
			blackKing = new King(false, p);
			blackPieces.put(p, blackKing);
		}
		else{
			p = new Point(3, blackY);
			blackKing = new King(false, p);
			blackPieces.put(p, blackKing);
			p = new Point(4, blackY);
			blackPieces.put(p, new Queen(false, p));
		}
		p = new Point(5, blackY);
		blackPieces.put(p, new Bishop(false, p));
		p = new Point(6, blackY);
		blackPieces.put(p, new Knight(false, p));
		p = new Point(7, blackY);
		blackPieces.put(p, new Rook(false, p));
	}
	

	/**
	 * Determines if a move is valid and if so, changes the board accordingly
	 * @param m move
	 * @return
	 */
	public boolean move(Move m){
//		ArrayList<Move> moves = new ArrayList<>();
//		moves.add(m);
//		setCaptures(moves);
		boolean rv = m.doMove();
		AI.updateGameState(this,null);
		return rv;
	}
	
	public Piece edit(Piece piece, Point p) {
		if(p == null) {
			if(piece.isWhite()) {
				whitePieces.remove(piece.position);
			}
			else {
				blackPieces.remove(piece.position);
			}
			return null;
		}
		Piece rv = null;
		if(whitePieces.containsKey(p)) {
			rv = whitePieces.get(p);
		}else if(blackPieces.containsKey(p)) {
			rv = blackPieces.get(p);
		}
		if(piece.isWhite()) {
			whitePieces.put(p, piece);
			if(whitePieces.get(piece.position) == piece)
				whitePieces.remove(piece.position);
		}else {
			blackPieces.put(p, piece);
			if(blackPieces.get(piece.position) == piece)
				blackPieces.remove(piece.position);
		}
		piece.position = p;
		return rv;
		
	}
	
	/**
	 * Determines if a particular piece could possibly be the given position and modifies the pieces moves assuming no returns
	 * @param piece
	 * @param pos
	 * @return null if position is invalid else piece with correct moves
	 */
	
	public Piece validPos(Piece piece, Point pos) {
		boolean top = rules.isTopPlayer() == piece.isWhite();
		if(piece instanceof Pawn) {
				if(pos.y == 0 || pos.y == 7) 
					return null;
				else if(!(pos.y == 1 && top || !top && pos.y == 6)) 
					piece.moves = (piece.moves > 0 ? piece.moves : 1);
					
		}else if(piece instanceof King) {
			piece.moves = (top && pos.y == 0 || !top && pos.y == 7) && pos.x == 4 ? piece.moves : (piece.moves > 0 ? piece.moves : 1);
		}else if(piece instanceof Rook) {
			piece.moves = (top && pos.y == 0 || !top && pos.y == 7) && (pos.x == 0 || pos.x == 7) ? piece.moves : (piece.moves > 0 ? piece.moves : 1);
		}
		return piece;
	}
	
	public Move undo(){
		Move rv = history.peek();
		rv.undoMove();
		return rv;
	}

	/**
	 * Returns the piece at a specific location, returns null if there is no piece there
	 * @param p
	 * The point we are looking at
	 */
	public Piece getPiece(Point p)
	{
		if(whitePieces.containsKey(p))
			return whitePieces.get(p);
		else if(blackPieces.containsKey(p))
			return blackPieces.get(p);
		else
			return null;
	}
	public void putPiece(Piece piece, Point pos){
		if(piece.isWhite())
			whitePieces.put(pos, piece);
		else
			blackPieces.put(pos, piece);
	}
	public Piece getPiece(Point p, boolean color){
		if(color)
			return whitePieces.get(p);
		else
			return blackPieces.get(p);
	}
	public SimpleDoubleProperty getTime(boolean top){
		if(top == rules.isTopPlayer())
			return whiteTime;
		else
			return blackTime;
	}
	public HashMap<Point, Piece> getPieces(boolean color){
		if(color)
			return whitePieces;
		return blackPieces;
	}
	public Piece removePiece(Point p, boolean color){
		if(color)
			return whitePieces.remove(p);
		else
			return blackPieces.remove(p);
	}
	public Boolean getWhoOccupiesAt(Point p){
		if(whitePieces.containsKey(p))
			return true;
		else if(blackPieces.containsKey(p))
			return false;
		else
			return null;
	}
//	public Point getPosition(Piece p){
//		if(p.isWhite()){
//			for(Point pos: whitePieces.keySet()){
//				if(whitePieces.get(pos).equals(p))
//					return pos;
//			}
//		}
//		else{
//			for(Point pos: blackPieces.keySet()){
//				if(blackPieces.get(pos).equals(p))
//					return pos;
//			}
//		}
//		return null;
//	}
	public King getKing(boolean color){
		if(color){
			return whiteKing;
		}
		else{
			return blackKing;
		}
	}
	public void setCaptures(ArrayList<Move> moves){
		for(Move m: moves){
			Boolean me = getWhoOccupiesAt(m.from);
			Boolean to = getWhoOccupiesAt(m.to);
			if(!(to == null) && to == !me)
				m.setCapture(getPiece(m.to));
		}
	}

	public boolean playerHasPieceAt(boolean player, Point pos){
		return getPiece(pos).isWhite() == player;
	}
	
	public void removeCheckMoves(ArrayList<Move> moves){
		Iterator<Move> itr = moves.iterator();
		while(itr.hasNext()){
			Move m = itr.next();
			if(m.putsPlayerInCheck(m.me))
				itr.remove();
		}
	}
	public void removeCastleOutOfCheck(ArrayList<Move> moves, boolean color){
		for(Move m: moves.toArray(new Move[moves.size()])){
			if(m.castlingMove && isInCheck(color)){
					moves.remove(m);
			}
		}
	}
	
	public void addCastleMoves(ArrayList<Move> moves, boolean color){
		int y = rules.isTopPlayer() ==color ? 0 : 7;
		King king = getKing(color);
		if(king.moves > 0 || !king.position.equals(new Point(4, y)))
			return;
		
		boolean validL = true;
		Piece left = getPiece(new Point(0, y), color);
		if(left == null || !(left instanceof Rook) || left.moves > 0)
			validL = false;
		for(int x = 3; x > 0 && validL; x--){
			if(getWhoOccupiesAt(new Point(x, y)) != null)
				validL = false;
			if(validL && rules.isCantCastleThroughCheck()){
				Move test = new Move(new Point(4, y), new Point(x, y), this);
				if(test.putsPlayerInCheck(test.me)){
					validL = false;
				}
			}
		}
		
		boolean validR = true;
		Piece right = getPiece(new Point(7, y), color);
		if(right == null || !(right instanceof Rook) || right.moves > 0)
			validR = false;
		for(int x = 5; x < 7 && validR; x++){
			if(getWhoOccupiesAt(new Point(x, y)) != null)
				validR = false;
			if(validR && rules.isCantCastleThroughCheck()){
				Move test = new Move(new Point(4, y), new Point(x, y), this);
				if(test.putsPlayerInCheck(test.me)){
					validR = false;
				}
			}
		}
		
		if(validL){
			Move l = new Move(new Point(4, y), new Point(2, y), this);
			l.castlingMove = true;
			moves.add(l);
		}
		if(validR){
			Move r = new Move(new Point(4, y), new Point(6, y), this);
			r.castlingMove = true;
			moves.add(r);
		}
	}
	
	public boolean isInCheck(boolean player){
		ArrayList<Point> points = new ArrayList<>();
		if(player)
			points.addAll(blackPieces.keySet());
		else
			points.addAll(whitePieces.keySet());
		for(Point p: points){
			Piece piece = getPiece(p);
			for(Move move: piece.getMoves(this, p)){
				if(move.capturedKing)
					return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		String out = "";
		for(int y = 0; y < 8; y++){
			for(int x = 0; x < 8; x++){
				Piece p = getPiece(new Point(x, y));
				if(p == null)
					out += ("•");
				else
					out += p.toChar();
			}
			out += "\n";
		}
		out += String.format("It is %s's turn\n",turn ? "White" : "Black");
		return out;
	}
	public void print(){
		System.out.println(this);
		System.out.printf("%d moves\n", history.size());
	}
	public AI getAI(){
		if(getIsAIPlayer())
			return turn ? white : black;
		System.err.println("Get AI ERROR");
		return null;
	}
	public boolean getIsAIPlayer(){
		if(rules.getMode() == RuleSet.GameMode.cvc)
			return true;
		if(rules.getMode() == RuleSet.GameMode.pvp)
			return false;
		if(rules.isComputerPlayer() == turn)
			return true;
		return false;
	}
	public void startTimer(){
		startTime = System.nanoTime();
		
	}
	public void endTimer(){
		avTotal += (System.nanoTime()-startTime);
		avCount++;
	}
	public void printTimer(String name, double score){
		System.out.printf("Task: %s, NanoSecs: %.2e, Board Score: %.2f\n", name, (double)(avTotal/avCount), score);
	}
	public void updateIcon(){
		
		boolean w;
		if(turn)
			w = black.score(false, null, null) < 0;
		else
			w = white.score(true, null, null) > 0;
		if(w != winning){
			winning = w;
			try {
//				String s = winning ? "White_King.png" : "Black_King.png";
//		        URL iconURL = App.class.getResource(s);
//		        java.awt.Image image = new ImageIcon(iconURL).getImage();
//		        com.apple.eawt.Application.getApplication().setDockIconImage(image);
		    } catch (Exception e) {
		        // Won't work on Windows or Linux.
		    }
		}
	}
	
	public String exportToString(){
		String rv = "$$$_";
		for(Piece p: whitePieces.values()){
			rv += p.export()+"-";
		}
		rv += "_";
		for(Piece p: blackPieces.values()){
			rv += p.export()+"-";
		}
		rv += "_";
		rv += turn ? "White" : "Black";
		rv += "_";
		rv += rules.export() + "_";
		rv += white.stratagy.export() + "_";
		rv += black.stratagy.export() + "_$$$";

		return rv;
	}
	
	public void doPositionCheck(){
		for(Point p: whitePieces.keySet()){
			Piece piece = whitePieces.get(p);
			if(!p.equals(piece.position))
				System.out.printf("%s at %s thinks it's at %s\n",piece,p,piece.position);
		}
		for(Point p: blackPieces.keySet()){
			Piece piece = blackPieces.get(p);
			if(!p.equals(piece.position))
				System.out.printf("%s at %s thinks it's at %s\n",piece,p,piece.position);
		}
		System.out.println("Check Complete");
	}
	public int hashCode(int[][] keys) {	//Zobrist Hash
		int hash = 0;
		for(Piece p : whitePieces.values()){
			int pos = p.position.x + p.position.y*8;
			int id = p.getPieceHash();
			hash ^= keys[pos][id];
		}
		for(Piece p : blackPieces.values()){
			int pos = p.position.x + p.position.y*8;
			int id = p.getPieceHash();
			hash ^= keys[pos][id];
		}
		if(turn)
			hash ^= 100;
		return hash;
	}

	public void addEvaluationToBuffer(double minimaxScore, int depthOfScore, String player) {
		evaluationBuffer.add(new BoardEvaluation(getCondensedBoardInputLayer(), minimaxScore, depthOfScore, player));
	}

	public void flushEvaluationBuffer(BufferedWriter out, int gameOutcome) {
		evaluationBuffer.forEach(e -> {
			try {
				out.write(e.getSaveString(gameOutcome));
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		});
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		evaluationBuffer.clear();
	}

	// returns 12 longs in array, one for each piece type, with a bit set for for every place in the board containing that piece
	public long[] getCondensedBoardInputLayer(){
		long[] rv = new long[12]; // w_pawns, w_rook, w_knight, w_bishop, w_queen, w_king, b_pawns, b_rook, b_knight, b_bishop, b_queen, b_king;

		for (int i = 0; i < 64; i++) {
			int x = i%8, y = i/8;
			Piece piece = getPiece(new Point(x, y));
			if (piece != null) {
				rv[getPieceOffsetForLayer(piece)] |= 1 << (long)(i);
			}
		}
		return rv;
	}


	// Takes in index from 0-63 get the piece at that place in the board and returns its hash value for NN purposes
	private int getPieceOffsetForLayer(Piece piece) {
		int offset = piece.isWhite() ? 0 : 6;

		if (piece instanceof Pawn) {
		}else if(piece instanceof Rook) {
			offset += 1;
		} else if (piece instanceof Knight) {
			offset += 2;
		} else if (piece instanceof Bishop) {
			offset += 3;
		} else if (piece instanceof Queen) {
			offset += 4;
		} else if (piece instanceof King) {
			offset += 5;
		} else {
			System.err.println("Unknown Piece: " + piece.getClass().toString());
		}
		return offset;
	}



}
