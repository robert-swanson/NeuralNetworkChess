package chess.ai;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import chess.board.Board;
import chess.board.Move;
import chess.board.Point;
import chess.board.State;
import chess.pieces.Bishop;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import neuralnetwork.BoardEvaluation;

/**
 * Maneges the logic for the chess AI
 */
public class AI {

	Board board;

	// Identity
	public Strategy stratagy;
	final boolean player;

	// State
	SimpleBooleanProperty allowance;
	ArrayList<Integer> bannedMoves;
	boolean halt;

	int avBranchTotal;
	int avBranchCount;
	final int progressDepth = 2;
	SimpleDoubleProperty progress;
	Long totalNodes;
	
	public double confidence;
	public boolean thinking = false;
	public int[][] keys;
	HashMap<Integer, Double>[] transpositionTable;
	Move[][] killerH;
	Move best;
	
	
	//NODE
	public PrintWriter logger;
	public Node parent;
	
	boolean idDone;

	
	public AI(Board board, boolean player, SimpleBooleanProperty a, SimpleDoubleProperty p) {
		try {
			logger = new PrintWriter("ChessTree.txt");
		} catch (FileNotFoundException e1) {
			System.err.println("Could'nt create logger");
			e1.printStackTrace();
		}
		halt = false;
		allowance = a;
		allowance.addListener(e -> respondToKill());
		this.board = board;
		this.player = player;
		stratagy = new Strategy();
		bannedMoves = new ArrayList<>();
		progress = p;
		
		resetKHTT();
		
		keys = new int[64][16];
		for(int pos = 0; pos < 64; pos++){
			for(int i = 0; i < 16; i++){
				keys[pos][i] = ThreadLocalRandom.current().nextInt();
			}
		}
	}
	@SuppressWarnings("unchecked")
	public void resetKHTT(){
		transpositionTable = new HashMap[stratagy.getDepth() +1];
		killerH = new Move[stratagy.getDepth() +1][2];
		for(int d = 0; d <= stratagy.getTranspositionTableDepth() && d <= stratagy.getDepth(); d++){
			transpositionTable[d] = new HashMap<>();
		}
	}
	/**
	 * Determines the legal moves a player can make
	 * @param player
	 * The player
	 * @param board
	 * The board
	 * @return
	 * An arrayList containing all the legal moves
	 */
	public static ArrayList<Move> getLegalMoves(boolean player, Board board){
		ArrayList<Move> moves = new ArrayList<>();
		for(Point p: board.getPieces(player).keySet()){
			moves.addAll(board.getPiece(p).getMoves(board, p));
		}
		board.addCastleMoves(moves, player);
		board.removeCheckMoves(moves);
		return moves;
	}
	
	public Move forceMove(){
		if(board.getIsAIPlayer()){
			System.out.println("To SLOW MR. AI");
		}
		ArrayList<Move> moves = getMoves(player);
		board.removeCastleOutOfCheck(moves, player);
		board.removeCheckMoves(moves);
		return moves.get((int)(Math.random()*moves.size())*0);
	}
	
	/**
	 * Determines the best move to make
	 * @return
	 * The best move
	 */
	public Move getBestMove(){
		thinking = true;
		progress.set(0.0);						//PROGRESS
		board.startTimer();

		boolean ab = stratagy.isAlphaBeta();
		boolean tt = stratagy.isTranspositionTable() & stratagy.getTranspositionTableDepth() >= 0;
		boolean kh = tt && stratagy.isKillerHeuristic() && stratagy.getKillerHeuristicDepth() >= 0;
		boolean id = tt && stratagy.isIterativeDeepening() && stratagy.getIterativedeepeningDepth() < stratagy.getDepth();
		
		resetKHTT();
		if(id)
			iterativeGetBestMove();
		
		parent = new Node(true);
		boolean me = board.turn;
		
		ArrayList<Move> moves = getMoves(me);	//Moves
		if(0 <= stratagy.getCheckDepth())
			board.removeCheckMoves(moves);

		if(id)
			sortMoves(moves, 0);
		if(kh){
			sortByKH(moves, 0);
		}
		
		if(stratagy.isPreventCycles()){				//Prevent Cycles
			updateBanned(4, 2);
			for(Move m: moves.toArray(new Move[moves.size()]))
				if(bannedMoves.contains(m.hashCode()) && moves.size() > 1){
					moves.remove(m);
					System.out.println("Broke Cycle");
				}
		}
		
		if(moves.size() == 0){
			thinking = false;
			return null;	
		}
		
		if(stratagy.getDepth() == 0){				//Random Moves
			randomize(moves);
			thinking = false;
			return moves.get(0);
		}
		
		double alpha = -1000;
		double beta = 1000;
		best = moves.get(0);

		int index = 0;
		double step = 1.0/moves.size();			//PROGRESS
		for(Move m: moves){
			Node child = new Node(false, m);	//NODE
			m.doMove();
			
			boolean didUseTT = false;
			int hash = board.hashCode(keys);
			if(tt && !id){
				Double s = transpositionGet(hash, 0);
				if(s != null){
					m.score = s;
					didUseTT = true;
				}
			}
			if(!didUseTT)
				m.score = minimax(alpha, beta, me, 1, stratagy.getDepth(), child, step);

//			board.addEvaluationToBuffer((me ? m.score : -m.score), stratagy.getDepth()-1, stratagy.scoringNetwork != null ? "NN" : "Minimax");
			
			m.undoMove();
			best = setBest(best, m, true, true);
			if(tt && !didUseTT)
				tranpositionAdd(0, hash, m.score);
			
			child.score = m.score;				//NODE
			parent.children.add(child);			//NODE
				
			index++;
			if(ab){
				putKH(m, 0);
				if(alpha < m.score)
					alpha = m.score;
				if(alpha > beta){
					progress.set(progress.get() + (moves.size()-index) * step); //PROGRESS
					thinking = false;
					board.addEvaluationToBuffer((me ? best.score : -best.score), stratagy.getDepth(), stratagy.scoringNetwork != null ? "NN" : "Minimax");
					return best;
				}
			}
		}		
		confidence = best.score;
		progress.set(0.0);
		board.endTimer();
//		board.printTimer("Move " + board.history.size(), confidence);
		thinking = false;
		board.addEvaluationToBuffer((me ? best.score : -best.score), stratagy.getDepth(), stratagy.scoringNetwork != null ? "NN" : "Minimax");
		return best;
	}
	
	/**
	 * A recursive method that determines the score of a particular move
	 * @param alpha
	 * The minimum value for move to be acceptable
	 * @param beta
	 * The maximum value for move to be acceptable
	 * @return
	 * The number score of the move
	 */
	private double minimax(double alpha, double beta, boolean me, int depth, int maxDepth, Node parent, double whole){		//NODE/PROGRESS
		boolean maximizer = me == board.turn;
		boolean aB = stratagy.isAlphaBeta();
		boolean tT = stratagy.isTranspositionTable() && depth <= stratagy.getTranspositionTableDepth();
		boolean iD = tT && stratagy.isIterativeDeepening();
		boolean kH = tT && stratagy.isKillerHeuristic() && depth <= stratagy.getKillerHeuristicDepth();
		ArrayList<Move> moves = getMoves(board.turn);
		
		
		if(depth <= stratagy.getCheckDepth())
			board.removeCheckMoves(moves);
			
		double step = whole/moves.size();			//PROGRESS
//		if(iD)
//			step = Math.pow(32, depth)/totalNodes;
		if(depth == maxDepth || moves.isEmpty()){	//Depth
			double score = score(me, null, moves);
			return score;
		}
		if(iD && depth != maxDepth)
			sortMoves(moves, depth);
		if(kH){
			sortByKH(moves, depth);
		}
		Move best = moves.get(0);
		int index = 0;
		for(Move m: moves){
			if(!allowance.get())
				return  best.score;
			Node child = new Node(!maximizer, m);						//NODE
			m.doMove();
			int hash = board.hashCode(keys);
			boolean didUseTT = false;
			if(tT && !iD){												//TT
				Double s = transpositionGet(hash, depth);
				if(s != null){
					m.score = s;	
					didUseTT = true;
				}
			}															//Normal MiniMax
			if(!didUseTT){
				m.score = minimax(alpha, beta, me, depth+1, maxDepth, child, step);
			}
			if(tT && !didUseTT)
				tranpositionAdd(depth, hash, m.score);

//			board.addEvaluationToBuffer((me ? m.score : -m.score), stratagy.getDepth()-depth, stratagy.scoringNetwork != null ? "NN" : "Minimax");

			m.undoMove();

			if(stratagy.isNodes()){
				child.score = m.score;									//NODE
				parent.children.add(child);								//NODE	
			}
			best = setBest(best, m, maximizer, false);
			if((maximizer && m.score > best.score)||(!maximizer && m.score < best.score))
				best = m;
			if(aB && maximizer && alpha < m.score)
				alpha = m.score;
			else if(aB && !maximizer && beta > m.score)
				beta = m.score;
			if(aB && (alpha > beta || (alpha == beta && !(alpha == best.score)))){
				if(kH){
					putKH(m, depth);
				}
				if(depth <= progressDepth){
					progress.set(progress.get() + (moves.size()-index) * step); //PROGRESS
				}
				best = m;
				return m.score;
			}
			index++;
		}
		if(depth == progressDepth){
			progress.set(progress.get()+whole);						//PROGRESS
		}
		return best.score;
	}
	@SuppressWarnings("unchecked")
	private void iterativeGetBestMove(){
		totalNodes = (long) 0;
		for(int d = stratagy.getIterativedeepeningDepth(); d <= stratagy.getDepth(); d++)
			totalNodes += (int)Math.pow(32, d);
		transpositionTable = new HashMap[stratagy.getDepth()];
		for(int i = 0; i < transpositionTable.length; i++)
			transpositionTable[i] = new HashMap<>();
		best = null;
		Node parent = new Node(true);
		for(int d = stratagy.getIterativedeepeningDepth(); d < stratagy.getDepth(); d++){
			double step = Math.pow(32, d)/totalNodes;
			minimax(-1000, 1000, board.turn, 0, d, parent, step);
		}
	}
	private void sortMoves(ArrayList<Move> moves, int depth){
		boolean maximizer = depth % 2 == 0;
		for(int i = 0; i < moves.size(); i++){
			Move m = moves.get(i);
			m.doMove();
			int hash = board.hashCode(keys);
			m.undoMove();
			Double score = transpositionTable[depth].get(hash);
			if(score != null){
				m.score = score;
				m.maximizer = maximizer;
			}
			else{
				return;	
			}
		}
		for(int i = 1; i < moves.size(); i++){
			int t = i;
			while(t > 0 && moves.get(t).score < moves.get(t-1).score){
				moves.add(t-1, moves.remove(t));
				t--;
			}
		}
	}
	private Move setBest(Move a, Move b, boolean maximizer, boolean top){
		if(maximizer){
			if(b.score > a.score){
				progressScore(b);
				return b;
			}
		}
		else{
			if(b.score < a.score){
				progressScore(b);
				return b;
			}
		}
		if(top && b.score ==  a.score){
			boolean aCheck = a.putsPlayerInCheck(!a.me);
			boolean bCheck = b.putsPlayerInCheck(!b.me);
			if(!aCheck && bCheck){
				progressScore(b);
				return b;
			}
			else if(aCheck && bCheck){
				if(scorePieceChecking(b.piece) > scorePieceChecking(a.piece)){
					progressScore(b);
					return b;
				}
					
			}
			progressScore(b);
			if(a.progressScore < b.progressScore){
				progressScore(b);
				return b;
			}
			else if(stratagy.addRand && Math.random()>.5){
				progressScore(b);
				return b;
			}
		}
		return a;
	}
	private void updateBanned(int maxLength, int maxRep){
		bannedMoves = new ArrayList<>();
		ArrayList<Integer> history = new ArrayList<>();
		for(Move m: board.history)
			history.add(m.hashCode());
		for(int l = 4; l <= maxLength; l += 2){
			if(history.size() < l * maxRep)
				continue;
			int[] pattern = new int[l];
			boolean patternBroken = false;
			for(int rep = 1; rep <= maxRep && !patternBroken; rep++){
				int[] pat = new int[l];
				for(int i = l-1; i >= 0 && !patternBroken; i--){
					int last = history.size()-1;
					int prevPat = (rep-1)*l;
					int fin = last - prevPat - (l-1) + i;
					pat[i] = history.get(fin);
				}
				if(rep == 1)
					pattern = pat;
				else if(!Arrays.equals(pat, pattern))
					patternBroken = true;
				else if(rep == maxRep){
					bannedMoves.add(pattern[0]);
				}
			}
		}
	}

/*	private Move getMaxMove(ArrayList<Move> moves){
		if(moves.size() == 0)
			return null;
		boolean me = moves.get(0).me;
		HashMap<Double, ArrayList<Move>> moveMap = new HashMap<>();
		Double highest = new Double(-1000);
		Double lowest = new Double(1000);
		for(Move m: moves){
			Double key = m.score;
			if(!moveMap.containsKey(key))
				moveMap.put(key, new ArrayList<>());
			moveMap.get(key).add(m);
			if(key > highest)
				highest = key;
			if(key < lowest)
				lowest = key;
		}
		while(highest >= lowest){
			Double key = (Double)highest;
			if(moveMap.containsKey(key)){
				if(stratagy.addRand)
					randomize(moveMap.get(key));
				for(Move m: moveMap.get(key)){
					if(m == null){
						moves.remove(m);
						continue;
					}
					if(!m.putsPlayerInCheck(me))
						return m;
					else
						moves.remove(m);
				}
			}
			highest = fixFloatIssues(highest - .01);
		}
		return null;
	}
	
	private Move getMinMove(ArrayList<Move> moves){
		if(moves.size() == 0)
			return null;
		boolean me = moves.get(0).me;
		HashMap<Double, ArrayList<Move>> moveMap = new HashMap<>();
		Double highest = new Double(-1000);
		Double lowest = new Double(1000);
		for(Move m: moves){
			Double key = m.score;
			if(!moveMap.containsKey(key))
				moveMap.put(key, new ArrayList<>());
			moveMap.get(key).add(m);
			if(key > highest)
				highest = key;
			if(key < lowest)
				lowest = key;
		}
		while(lowest <= highest){
			Double key = (Double)highest;
			if(moveMap.containsKey(key)){
				if(stratagy.addRand)
					randomize(moveMap.get(key));
				for(Move m: moveMap.get(key)){
					if(m == null){
						moves.remove(m);
						continue;
					}
					if(!m.putsPlayerInCheck(me))
						return m;
					else
						moves.remove(m);
				}
			}
			lowest = fixFloatIssues(lowest + .1);
		}
		return null;
	}
*/	
	private void randomize(ArrayList<Move> moves){
		for(int i = 0; i < moves.size(); i++){
			int rand = (int)(Math.random() * moves.size());
			Move temp = moves.get(i);
			moves.set(i, moves.get(rand));
			moves.set(rand, temp);
		}
	}
	
	private void tranpositionAdd(int depth, int hash, double value){
		HashMap<Integer, Double> map = transpositionTable[depth];
		if(map.size()>100000){
			map.remove(map.keySet().iterator().next());
		}
		if(map.size() > 100001)
			System.out.println("BIG");
		map.put(hash, value);
	}
	private Double transpositionGet(int hash, int depth){
		for(int d = 1; d < stratagy.getTranspositionTableDepth(); d++){
			Double n = transpositionTable[d].get(hash);
			if(n != null){
				if(depth < d){
					transpositionTable[depth].put(hash, n);
					transpositionTable[d].remove(hash);
				}
				return n;
			}
		}
		return null;
	}
	
	private void sortByKH(ArrayList<Move> moves, int depth){
		if(killerH[depth].length == 2 && moves.contains(killerH[depth][1])){
			int b = moves.indexOf(killerH[depth][1]);
			if(b > -1){
				moves.add(0,moves.remove(b));
			}
		}
		if(killerH[depth].length > 0){
			int a = moves.indexOf(killerH[depth][0]);
			if(a > -1){
				moves.add(0,moves.remove(a));
			}
		}
	}
	
	private void putKH(Move m, int depth){
		Move[] kh = killerH[depth];
		if(kh[0] == null || kh.length == 1 && kh[0] == m){
			kh[0] = m;
		}
		else if(kh[1] == null){
			kh[1] = m;
		}
		else{
			if(kh[0].equals(m)){
			}
			else if(kh[1].equals(m)){
				kh[1] = kh[0];
				kh[0] = m;
			}
			else{
				kh[1] = m;
			}
		}
	}
	
	
	/**
	 * Determines all the legal moves a particular player can make on the current board state
	 * @param me the color of the current player
	 * @return
	 * An arrayList containing all the legal moves
	 */
	private ArrayList<Move> getMoves(boolean me){
		HashMap<Point, Piece> pieces = board.getPieces(me);
		ArrayList<Move> moves = new ArrayList<>();
		board.removeCastleOutOfCheck(moves, me);
		for(Point point: pieces.keySet().toArray(new Point[pieces.size()])){
			Piece piece = board.getPiece(point, me);
			moves.addAll(piece.getMoves(board, point));
		}
		return moves;
	}
	
	private int scorePieceChecking(Piece p){
		if(p instanceof Queen)
			return 3;
		if(p instanceof Rook)
			return 2;
		if(p instanceof Bishop)
			return 1;
		return 0;
	}

	public double objectiveScoreBoardState(){
		switch(board.gameState){
			case BLACKWON:
				return Integer.MIN_VALUE+1;
			case WHITEWON:
				return Integer.MAX_VALUE;
			case STALEMATE:
				return 0;
			default:
		}

		if (stratagy.scoringNetwork != null){
			return stratagy.scoringNetwork.classify(new BoardEvaluation(board));
		} else {
			double score = 0;
			for(Piece p: board.getPieces(true).values()){
				Point pos = p.position;
				int dist = (p.isWhite() == board.rules.isTopPlayer()) ? pos.y-1 : 6 - pos.y;
				score = fixFloatIssues(score + p.getValue(dist));
			}
			for(Piece p: board.getPieces(false).values()){
				int dist = (p.isWhite() == board.rules.isTopPlayer()) ? p.position.y-1 : 6 - p.position.y;
				score =  fixFloatIssues(score - p.getValue(dist));
			}
			return score;
		}
	}
	
	/**
	 * Evaluates the current board according to the piece values
	 * @return
	 * The total score in the perspective of the AI
	 */
	public double score(boolean maximizingPlayer, Move m, ArrayList<Move> moves){
		updateGameState(board, moves);

		switch(board.gameState){
		case BLACKWON:
			return maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		case WHITEWON:
			return maximizingPlayer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		case STALEMATE:
			return 0;
		default:
		}

		if (stratagy.scoringNetwork != null){
			return stratagy.scoringNetwork.classify(new BoardEvaluation(board));
		} else {
			double score = 0;
			for(Piece p: board.getPieces(maximizingPlayer).values()){
				Point pos = p.position;
				int dist = (p.isWhite() == board.rules.isTopPlayer()) ? pos.y-1 : 6 - pos.y;
				score = fixFloatIssues(score + p.getValue(dist));
			}
			if(m != null)
				m.progressScore = score;
			for(Piece p: board.getPieces(!maximizingPlayer).values().toArray(new Piece[board.getPieces(!maximizingPlayer).size()])){
				int dist = (p.isWhite() == board.rules.isTopPlayer()) ? p.position.y-1 : 6 - p.position.y;
				score =  fixFloatIssues(score - p.getValue(dist));
			}
			if(m != null)
				m.score = score;

			return score;
		}
	}
	
	public double progressScore(Move m){
		double score = 0;
		for(Piece p: board.getPieces(m.me).values()){
			int dist = (p.isWhite() == board.rules.isTopPlayer()) ? p.position.y-1 : 6 - p.position.y;
			score = fixFloatIssues(score + p.getValue(dist));
		}
		m.progressScore = score;
		return score;
	}
	
	public void respondToKill(){
		halt = !allowance.get();
	}
	public void reactivate(){
		halt = false;
	}
	public static void updateGameState(Board board, ArrayList<Move> moves){
		boolean turn = board.turn;
			if(moves == null){
				moves = new ArrayList<>();
				int s = board.getPieces(turn).size();
				Point[] points = board.getPieces(turn).keySet().toArray(new Point[s]);
				for(Point p: points){
					ArrayList<Move> piecesMoves = new ArrayList<>();
					Piece piece = board.getPiece(p, turn);
					piecesMoves.addAll(piece.getMoves(board, p));
					board.removeCheckMoves(piecesMoves);
					moves.addAll(piecesMoves);
					if(moves.size() > 0)
						break;
				}
			}
			if(moves.size() == 0){
				if(board.isInCheck(turn))
					board.gameState = turn ? State.BLACKWON : State.WHITEWON;
				else
					board.gameState = State.STALEMATE;
			}
			else
				board.gameState = State.INPROGRESS;
	}
	public static Double fixFloatIssues(Double n){
		return Double.parseDouble(String.format("%.2f", n));
	}
	@Override
	public String toString() {
		return board.toString();
	}
	public class Node{
		ArrayList<Node> children;
		double score = 0;
		String move = "";
		boolean maximizing;

		Node(boolean maximizing){
			this.maximizing = maximizing;
			children = new ArrayList<Node>();

		}
		Node(boolean maximizing, Move m){
			move = m.toString();
			this.maximizing = maximizing;
			children = new ArrayList<Node>();
		}
		@Override
		public String toString() {
			return String.format("Player: %s, Score: %(d", (maximizing)?"b":"w",score);
		}
		public void print(){
			try {
				logger = new PrintWriter("ChessTree.txt");
//				logger.println(System.currentTimeMillis());
			} catch (FileNotFoundException e) {
				System.out.println("Couldn't remake logger");
				e.printStackTrace();
			}
			print(0,-1,new ArrayList<Number>(0));
			logger.flush();
		}
		private void print(int indent,int max, ArrayList<Number> lasts){
			if(max >= 0 && indent > max) return;
			if(indent == 0) logger.printf("%s: Move Score: %(f\n", (maximizing ? "MAX" : "MIN"), score);
			if(children==null)
				return;
			int num = 0;
			for(Node child: children){
				boolean isLast = false;
				ArrayList<Number> nLasts = new ArrayList<Number>(lasts);
				if(children.size() == ++num){
					nLasts.add((Number)indent);
					isLast = true;
				}
				for(int j = 0; j < indent; j++){
					if(lasts.contains(j)){
						logger.print("      ");
					}
					else{
						logger.print("|     ");
					}
				}
				if(isLast)
					logger.print("L___");
				else
					logger.print("|---");
				for(int i = 0; i < 3-indent; i++){
					if(isLast)
						logger.print("_");
					else
						logger.print("-");
				}
				logger.printf("%s, Score: %.1f, Move: %s\n",child.maximizing ? "MAX" : "MIN", child.score, child.move); //Flipped because its the children
									
				child.print(indent+1,max,nLasts);
			}
		}
	}
}
