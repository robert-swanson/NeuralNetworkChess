package chess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Tester {
	private class Test implements Comparable<Test>{
		long runtime;

		int depth;
		int checkDepth;
		boolean transpositionTable;
		int transpositionTableDepth;
		boolean iterativeDeepening;
		int iterativeDeepeningDepth;
		boolean killerHeuristic;
		int killerHeuristicDepth;
		
		boolean compareByRunTime; 

		public Test(int d, int cD, boolean tT, int ttD, boolean iD, int iDD, boolean kH, int kHD){
			depth = d;
			checkDepth = cD;
			transpositionTable = tT;
			transpositionTableDepth = ttD;
			iterativeDeepening = iD;
			iterativeDeepeningDepth = iDD;
			killerHeuristic = kH;
			killerHeuristicDepth = kHD;
		}
		public Test(String s){
			String[] data = s.split(" +");
			runtime = Long.parseLong(data[15]);

			depth = Integer.parseInt(data[3]);
			checkDepth = Integer.parseInt(data[5]);
			transpositionTableDepth = Integer.parseInt(data[7]);
			iterativeDeepening = data[9].equals("yes");
			iterativeDeepeningDepth = Integer.parseInt(data[11]);
			killerHeuristicDepth = Integer.parseInt(data[13]);
			
			killerHeuristic = true;
			transpositionTable = true;
		}
		
		public Test run(){
			long start = System.nanoTime();
			for(int i = 0; i < 10; i++){
				board = testBoards[i];
				setUpBoard(board);
				board.white.getBestMove();
			}
			runtime = (long)((System.nanoTime()-start)/10);
			return this;
		}
		public void setUpBoard(Board board){
			board.white.stratagy.addRand = false;
			board.white.stratagy.alphaBeta = true;

			board.white.stratagy.depth = depth;
			board.white.stratagy.checkDepth = checkDepth;
			board.white.stratagy.transpositionTable = transpositionTable;
			board.white.stratagy.transpositionTableDepth = transpositionTableDepth;
			board.white.stratagy.iterativeDeepening = iterativeDeepening;
			board.white.stratagy.iterativedeepeningDepth = iterativeDeepeningDepth;
			board.white.stratagy.killerHeuristic = killerHeuristic;
			board.white.stratagy.killerHeuristicDepth = killerHeuristicDepth;
		}
		
		@Override
		public String toString() {
			return String.format("Time: %.2e Depth: %d CD: %d TTD: %d ID: %s IDD: %d KHD: %d CRT: %d", (double)runtime, depth, checkDepth, transpositionTableDepth, iterativeDeepening ? "yes" : "no ", iterativeDeepeningDepth, killerHeuristicDepth, runtime);
		}
		@Override
		public int compareTo(Test o) {
			if(compareByRunTime){
				int x = (depth-o.depth)  * 10 + runtime>o.runtime ? 1 : (runtime == o.runtime ? 0 : -1);
				return x;
			}
			return (depth * 1000000 + checkDepth * 100000 + (transpositionTable ? 10000 : 0) + (iterativeDeepening ? 1000 : 0) + iterativeDeepeningDepth * 100 + (killerHeuristic ? 10 : 0) + (killerHeuristicDepth)) - (o.depth * 1000000 + o.checkDepth * 100000 + (o.transpositionTable ? 10000 : 0) + (o.iterativeDeepening ? 1000 : 0) + o.iterativeDeepeningDepth * 100 + (o.killerHeuristic ? 10 : 0) + o.killerHeuristicDepth);
		}
		
	}

	PrintWriter logger;
	ArrayList<Test> tests;

	AI ai;
	Board board;
	SimpleBooleanProperty allowance;
	SimpleDoubleProperty progress;
	Board[] testBoards;
	
	int d = 1;
	int cd = 0;
	boolean tt = true;
	int ttd = 0;
	boolean id = false;
	int idd = 0;
	boolean kh = true;
	int khd = 0;
	
	final int Max_Depth = 3;

	public Tester(SimpleDoubleProperty d, SimpleBooleanProperty a) {
		tests = new ArrayList<>();
		allowance = a;
		progress = d;
		tests = new ArrayList<>();
		
		readFile();
		mergeData();
		initTestBoards();
		new RunTests(tests).writeFile();

		board = testBoards[0];
		ai = board.white;
		
		if(tests.size() > 0){
			Test last = tests.get(tests.size()-1);
			this.d = last.depth;
			cd = last.checkDepth;
			tt = last.transpositionTable;
			ttd = last.transpositionTableDepth;
			id = last.iterativeDeepening;
			idd = last.iterativeDeepeningDepth;
			kh = last.killerHeuristic;
			khd = last.killerHeuristicDepth;
		}
		
	}

	private void readFile(){
		File f = new File("Learner.txt");
		if(f.exists()){
			Scanner in;
			try {
				in = new Scanner(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			while(in.hasNextLine() && !in.nextLine().equals("---Tests---"));
			while(in.hasNextLine()){
				tests.add(new Test(in.nextLine()));
			}
			in.close();
			try {
				logger = new PrintWriter(f);
			} catch (FileNotFoundException e1) {
				System.err.println("Could not create logger");
				e1.printStackTrace();
			}
		}
		else
			try {
				logger = new PrintWriter(new File("Learner.txt"));
			} catch (FileNotFoundException e1) {
				System.err.println("Could not create logger");
				e1.printStackTrace();
			}
	}

	
	private void mergeData(){
		File f = new File("Learner 2.txt");
		if(f.exists()){
			Scanner in;
			try {
				in = new Scanner(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			while(in.hasNextLine() && !in.nextLine().equals("---Tests---"));
			while(in.hasNextLine()){
				tests.add(new Test(in.nextLine()));
			}
			in.close();
			try {
				logger = new PrintWriter(f);
			} catch (FileNotFoundException e1) {
				System.err.println("Could not create logger");
				e1.printStackTrace();
			}
			f.delete();
		}
	}

	public void start(){
		new RunTests(tests).start();	
	}
	
	private Test getNextTest(){
		if(kh && khd < d){
			khd++;
		}
//		else if(!kh){
//			khd = 0;
//			
//			kh = true;
//		}
		else if(idd < d && id){
//			kh = false;
			khd = 0;
			
			idd++;	
		}
		else if(!id && tt){
//			kh = false;
			khd = 0;
			idd = 0;
			
			id = true;
		}
		else if(ttd < d && tt){
//			kh = false;
			khd = 0;
			idd = 0;
			id = false;
			
			ttd++;
		}
//		else if(!tt){
//			kh = false;
//			khd = 0;
//			idd = 0;
//			id = false;
//			ttd = 0;
//			
//			tt = true;
//		}
		else if(cd < d){
//			kh = false;
			khd = 0;
			idd = 0;
			id = false;
			ttd = 0;
//			tt = false;
			
			cd++;
		}
		else if(d < Max_Depth){
//			kh = false;
			khd = 0;
			idd = 0;
			id = false;
			ttd = 0;
//			tt = false;
			cd = 0;
			
			d++;
		}
		else
			return null;
		return new Test(d, cd, tt, ttd, id, idd, kh, khd);
	}

	private void initTestBoards(){
		testBoards = new Board[10];
		testBoards[0] = new Board(allowance, progress);
		testBoards[1] = new Board(allowance, "$$$_4:(3, 3):White:3-1:(7, 6):White:0-2:(0, 7):White:0-3:(1, 7):White:0-5:(3, 7):White:0-6:(4, 7):White:0-3:(6, 7):White:0-1:(4, 4):White:1-2:(7, 7):White:0-1:(6, 4):White:1-1:(3, 5):White:1-1:(5, 5):White:1-1:(0, 6):White:0-1:(1, 6):White:0-1:(2, 6):White:0-_1:(2, 3):Black:1-2:(0, 0):Black:0-3:(1, 0):Black:0-1:(4, 3):Black:1-4:(2, 0):Black:0-5:(3, 0):Black:0-1:(6, 3):Black:2-4:(5, 0):Black:0-2:(7, 0):Black:0-1:(1, 1):Black:0-1:(6, 1):Black:0-1:(7, 1):Black:0-1:(3, 2):Black:1-6:(6, 2):Black:1-1:(0, 3):Black:1-_Black_2:0:0_2:2:true:true_2:2:true:true_$$$", progress);
		testBoards[2] = new Board(allowance, "$$$_4:(3, 3):White:3-2:(0, 7):White:0-1:(0, 4):White:1-5:(3, 7):White:0-1:(2, 4):White:1-6:(4, 7):White:0-1:(4, 4):White:1-3:(6, 7):White:0-2:(7, 7):White:0-1:(6, 4):White:1-3:(0, 5):White:1-1:(1, 5):White:1-1:(3, 5):White:1-1:(5, 5):White:1-1:(7, 5):White:1-_1:(2, 3):Black:1-2:(0, 0):Black:0-3:(1, 0):Black:0-1:(4, 3):Black:1-4:(2, 0):Black:0-5:(3, 0):Black:0-1:(6, 3):Black:2-6:(5, 0):Black:3-2:(7, 0):Black:0-1:(1, 1):Black:0-4:(4, 1):Black:1-1:(3, 2):Black:1-1:(6, 2):Black:1-1:(7, 2):Black:1-1:(0, 3):Black:1-_Black_2:0:0_2:2:true:true_2:2:true:true_$$$", progress);
		testBoards[3] = new Board(allowance, "$$$_4:(3, 3):White:3-2:(0, 7):White:0-3:(1, 7):White:0-1:(0, 4):White:1-5:(3, 7):White:0-1:(2, 4):White:1-1:(4, 4):White:1-3:(6, 7):White:2-2:(7, 7):White:0-1:(6, 4):White:1-1:(1, 5):White:1-1:(3, 5):White:1-1:(5, 5):White:1-1:(7, 5):White:1-6:(3, 6):White:5-_1:(2, 3):Black:1-2:(0, 0):Black:0-1:(4, 3):Black:1-4:(2, 0):Black:0-5:(3, 0):Black:0-1:(6, 3):Black:2-2:(7, 0):Black:0-1:(1, 1):Black:0-4:(6, 1):Black:3-3:(0, 2):Black:1-1:(3, 2):Black:1-6:(5, 2):Black:6-1:(6, 2):Black:1-1:(7, 2):Black:1-1:(0, 3):Black:1-_Black_2:0:1_2:2:true:true_2:2:true:true_$$$", progress);
		testBoards[4] = new Board(allowance, "$$$_1:(7, 3):White:3-1:(0, 4):White:1-5:(4, 7):White:21-_5:(3, 0):Black:0-1:(6, 3):Black:2-3:(2, 1):Black:2-4:(2, 5):Black:7-1:(7, 2):Black:1-1:(1, 6):Black:4-1:(0, 3):Black:1-2:(3, 6):Black:8-_White_2:0:1_2:2:true:true_2:2:true:true_$$$", progress);
		testBoards[5] = new Board(allowance, "$$$_4:(3, 3):White:3-1:(7, 6):White:0-2:(0, 7):White:0-3:(1, 7):White:0-1:(1, 4):White:1-5:(3, 7):White:0-1:(2, 4):White:1-3:(6, 7):White:0-1:(4, 4):White:1-2:(7, 7):White:0-1:(6, 4):White:1-1:(0, 5):White:1-1:(3, 5):White:1-6:(4, 5):White:1-1:(5, 5):White:1-_1:(2, 3):Black:1-2:(0, 0):Black:0-1:(4, 3):Black:1-4:(2, 0):Black:0-5:(3, 0):Black:0-1:(6, 3):Black:2-4:(5, 0):Black:0-2:(7, 0):Black:0-1:(0, 4):Black:2-1:(1, 1):Black:0-1:(6, 1):Black:0-1:(3, 2):Black:1-3:(5, 2):Black:2-6:(6, 2):Black:1-1:(7, 2):Black:1-_Black_2:0:1_2:2:true:true_2:2:true:true_$$$", progress);
		testBoards[6] = new Board(allowance, "$$$_4:(3, 3):White:3-2:(0, 7):White:0-3:(1, 7):White:0-5:(3, 7):White:0-1:(2, 4):White:1-2:(7, 7):White:0-1:(7, 4):White:2-1:(0, 5):White:1-6:(5, 5):White:3-_2:(0, 0):Black:0-1:(4, 3):Black:1-5:(3, 0):Black:0-2:(7, 0):Black:0-1:(0, 4):Black:2-1:(3, 4):Black:3-1:(1, 1):Black:0-3:(4, 4):Black:3-1:(5, 4):Black:3-4:(4, 1):Black:1-1:(6, 1):Black:0-6:(6, 2):Black:3-_White_2:0:1_2:2:true:true_2:2:true:true_$$$", progress);
		testBoards[7] = new Board(allowance, "$$$_6:(4, 6):White:1-1:(3, 3):White:2-2:(0, 7):White:0-5:(3, 7):White:0-3:(6, 7):White:0-1:(4, 4):White:1-2:(7, 7):White:0-3:(0, 5):White:1-1:(1, 5):White:1-1:(3, 5):White:1-1:(5, 5):White:1-4:(4, 2):White:4-1:(7, 5):White:1-1:(1, 3):White:2-_1:(2, 3):Black:1-2:(0, 0):Black:0-1:(4, 3):Black:1-4:(2, 0):Black:0-5:(3, 0):Black:0-1:(6, 3):Black:2-4:(5, 0):Black:0-2:(7, 0):Black:0-1:(0, 4):Black:2-6:(5, 4):Black:3-3:(3, 1):Black:1-1:(7, 4):Black:3-_White_2:0:1_2:2:true:true_2:2:true:true_$$$", progress);
		testBoards[8] = new Board(allowance, "$$$_1:(5, 6):White:0-1:(6, 6):White:0-1:(7, 6):White:0-2:(0, 7):White:0-3:(1, 7):White:0-4:(2, 7):White:0-5:(3, 7):White:0-6:(4, 7):White:0-4:(2, 4):White:1-3:(6, 7):White:0-1:(4, 4):White:1-2:(7, 7):White:0-1:(0, 6):White:0-1:(1, 6):White:0-1:(2, 6):White:0-1:(3, 6):White:0-_2:(0, 0):Black:0-1:(2, 3):Black:1-3:(1, 0):Black:0-4:(2, 0):Black:0-5:(3, 0):Black:0-6:(4, 0):Black:0-4:(5, 0):Black:0-3:(6, 0):Black:0-2:(7, 0):Black:0-1:(1, 1):Black:0-1:(3, 1):Black:0-1:(4, 1):Black:0-1:(5, 1):Black:0-1:(6, 1):Black:0-1:(7, 1):Black:0-1:(0, 3):Black:1-_White_2:0:1_2:2:true:true_2:2:true:true_$$$", progress);
		testBoards[9] = new Board(allowance, "$$$_2:(7, 6):White:1-3:(1, 7):White:0-1:(0, 4):White:1-5:(3, 7):White:0-1:(2, 4):White:1-6:(5, 7):White:6-3:(6, 7):White:0-1:(6, 4):White:1-1:(1, 5):White:1-1:(7, 5):White:1-_2:(0, 0):Black:0-5:(3, 0):Black:0-1:(6, 3):Black:2-4:(0, 7):Black:2-2:(7, 0):Black:0-3:(1, 4):Black:2-4:(4, 4):Black:2-1:(5, 4):Black:2-1:(3, 5):Black:3-6:(2, 2):Black:4-1:(3, 2):Black:1-1:(6, 2):Black:1-1:(7, 2):Black:1-1:(0, 3):Black:1-_Black_2:0:1_2:2:true:true_2:2:true:true_$$$", progress);
	}
	
	private class RunTests extends Thread{
		ArrayList<Test> tests;
		public RunTests(ArrayList<Test> t) {
			tests = t;
		}
		@Override
		public void run() {
			while(true){
				Test next = getNextTest();
				if(next == null)
					break;
				tests.add(next.run());
				if(!allowance.get()){
					tests.remove(tests.size()-1);
					break;
				}
				writeFile();
			}
			System.out.println("DONE");
		}
		private void writeFile(){
			try {
				logger = new PrintWriter("Learner.txt");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			//User Data
			logger.println("---User Data---");
			setTestCompare(true);
			Collections.sort(tests);
			int i = 0;
			for(int d = 0; d <= Max_Depth && i < tests.size(); d++){
				Test t = tests.get(i);
				if(t.depth == d)
					logger.printf("Best at Depth: %d is: %s\n",d,t);
				else
					continue;
				while(i < tests.size()-1 && tests.get(++i).depth == d);
			}
			setTestCompare(false);
			Collections.sort(tests);
			
			//Excel Data
			logger.println("\n---Excel Data---");
			setTestCompare(false);
			Collections.sort(tests);
			for(Test t: tests)
				logger.print(String.format("%.2e\t%d\t%d\t%d\t%s\t%d\t%d\n", (double)t.runtime, t.depth, t.checkDepth, t.transpositionTableDepth, t.iterativeDeepening, t.iterativeDeepeningDepth,t.killerHeuristicDepth));
			
			//Tests
			setTestCompare(true);
			Collections.sort(tests);
			logger.println("\n---Tests---");
			for(Test t: tests)
				logger.println(t);
			logger.flush();
		}
		private void setTestCompare(boolean b){
			for(Test test: tests)
				test.compareByRunTime = b;
		}
	}
}
