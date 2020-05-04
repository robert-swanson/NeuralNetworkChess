package chess.ai;

import neuralnetwork.NN;

public class Strategy {
    private int depth;
    private boolean alphaBeta;
    private boolean transpositionTable;
    private int transpositionTableDepth;
    private boolean killerHeuristic;
    private int killerHeuristicDepth;
    private boolean iterativeDeepening;
    private int iterativedeepeningDepth;
    private int checkDepth;

    private String scoringNetworkFilePath;
    public NN scoringNetwork;

    private boolean nodes;
    private boolean preventCycles;


    boolean addRand;

    public Strategy() {
        depth = 4;
        alphaBeta = true;
        transpositionTable = true;
        transpositionTableDepth = 4;
        killerHeuristic =  false;
        killerHeuristicDepth = 4;
        iterativeDeepening = true;
        iterativedeepeningDepth = 2;
        addRand = true;
        checkDepth = 1;
        scoringNetworkFilePath = "";
        scoringNetwork = null;

        nodes = true;
        preventCycles = true;
    }
    public Strategy(String importString){
        this();
        String[] strat = importString.split(":");
        if(strat.length != 4)
            return;
        depth = Integer.parseInt(strat[0]);
        checkDepth = Integer.parseInt(strat[1]);
        alphaBeta = Boolean.parseBoolean(strat[2]);
        addRand = Boolean.parseBoolean(strat[3]);
    }
    @Override
    public String toString() {
        return String.format("Stratagy\n"
                + "Depth: %d\n"
                + "Check Depth: %d\n"
                + "AlphaBeta: %b\n"
                + "Add Random Element: %b\n"
                + "Transposition Table: %b Depth: %d\n"
                + "Killer Heuristic: %b Depth: %d\n"
                + "Iterative Deepening: %b Depth: %d\n",depth, checkDepth, alphaBeta, addRand,  transpositionTable, transpositionTableDepth, killerHeuristic, killerHeuristicDepth, iterativeDeepening, iterativedeepeningDepth);
    }

    public String export(){
        return String.format("%d:%d:%b:%b", depth, checkDepth, alphaBeta, addRand);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isAlphaBeta() {
        return alphaBeta;
    }

    public void setAlphaBeta(boolean alphaBeta) {
        this.alphaBeta = alphaBeta;
    }

    public boolean isTranspositionTable() {
        return transpositionTable;
    }

    public void setTranspositionTable(boolean transpositionTable) {
        this.transpositionTable = transpositionTable;
    }

    public int getTranspositionTableDepth() {
        return transpositionTableDepth;
    }

    public void setTranspositionTableDepth(int transpositionTableDepth) {
        this.transpositionTableDepth = transpositionTableDepth;
    }

    public boolean isKillerHeuristic() {
        return killerHeuristic;
    }

    public void setKillerHeuristic(boolean killerHeuristic) {
        this.killerHeuristic = killerHeuristic;
    }

    public int getKillerHeuristicDepth() {
        return killerHeuristicDepth;
    }

    public void setKillerHeuristicDepth(int killerHeuristicDepth) {
        this.killerHeuristicDepth = killerHeuristicDepth;
    }

    public boolean isIterativeDeepening() {
        return iterativeDeepening;
    }

    public void setIterativeDeepening(boolean iterativeDeepening) {
        this.iterativeDeepening = iterativeDeepening;
    }

    public int getIterativedeepeningDepth() {
        return iterativedeepeningDepth;
    }

    public void setIterativedeepeningDepth(int iterativedeepeningDepth) {
        this.iterativedeepeningDepth = iterativedeepeningDepth;
    }

    public int getCheckDepth() {
        return checkDepth;
    }

    public void setCheckDepth(int checkDepth) {
        this.checkDepth = checkDepth;
    }

    public boolean isNodes() {
        return nodes;
    }

    public void setNodes(boolean nodes) {
        this.nodes = nodes;
    }

    public boolean isPreventCycles() {
        return preventCycles;
    }

    public void setPreventCycles(boolean preventCycles) {
        this.preventCycles = preventCycles;
    }

    public boolean isAddRand() {
        return addRand;
    }

    public void setAddRand(boolean addRand) {
        this.addRand = addRand;
    }

    public String getScoringNetworkFilePath() {
        return scoringNetworkFilePath;
    }

    public void setScoringNetworkFilePath(String scoringNetworkFilePath) {
        this.scoringNetworkFilePath = scoringNetworkFilePath;
    }
}
