package chess.board;

public class RuleSet{
    public enum TimeLimit{
        off,total,turn;
        public int seconds;
        public int minutes;
        private TimeLimit() {
            seconds = 0;
            minutes = 0;
        }
        public int toSeconds(){
            return (minutes * 60 + seconds);
        }
        @Override
        public String toString() {
            switch (this) {
                case off:
                    return "Off";
                case total:
                    return String.format("Total: %dm %ds",minutes, seconds);
                case turn:
                    return String.format("Turn: %dm %ds",minutes, seconds);
                default:
                    return "Other";
            }
        }
    }

    public enum GameMode{
        pvp, pvc, cvc;
        @Override
        public String toString() {
            switch (this) {
                case pvp:
                    return "Player vs Player";
                case pvc:
                    return "Player vs Computer";
                case cvc:
                    return "Computer vs Computer";
                default:
                    return "Unknown GameMode";
            }
        }
    }

    private GameMode mode;
    private boolean debug;
    private boolean undo;
    private boolean cantCastleThroughCheck;
    private boolean cantCastleAfterCheck;
    private boolean topPlayer;


    private boolean computerPlayer;

    TimeLimit timeLimit;

    public RuleSet(String importString){
        this();
        String[] sa = importString.split(":");
        if(sa.length != 3)
            return;
        int mode = Integer.parseInt(sa[0]);
        int top = Integer.parseInt(sa[1]);
        int comp = Integer.parseInt(sa[2]);
        if(mode == 0)
            this.mode = GameMode.pvp;
        else if(mode == 1)
            this.mode = GameMode.pvc;
        else
            this.mode = GameMode.cvc;
        topPlayer = top == 1;
        computerPlayer = comp == 1;
    }

    public RuleSet() {
        mode = GameMode.pvc;
        cantCastleThroughCheck = true;
        cantCastleAfterCheck = false;
        topPlayer = false;
        timeLimit = TimeLimit.off;
        timeLimit.seconds = 0;
        computerPlayer = false;
        undo = true;
        debug = true;
    }

    @Override
    public String toString() {
        return String.format("Rules\n"
                + "Mode: %s\n"
                + "Can't Castle Through Check: %b\n"
                + "Can't Castle After Check: %b\n"
                + "Top Player: %s\n"
                + "Computer Player: %s\n"
                + "Time Limit: %s\n", mode, cantCastleThroughCheck, cantCastleAfterCheck, (topPlayer ? "White" : "Black"), (computerPlayer ? "White" : "Black"), timeLimit);
    }

    public String export(){
        int m = -1;
        switch(mode){
            case pvp:
                m = 0;
                break;
            case pvc:
                m = 1;
                break;
            case cvc:
                m = 2;
                break;
        }
        return String.format("%d:%d:%d", m, topPlayer ? 1 : 0, computerPlayer ? 1 : 0);
    }


    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isUndo() {
        return undo;
    }

    public void setUndo(boolean undo) {
        this.undo = undo;
    }

    public boolean isCantCastleThroughCheck() {
        return cantCastleThroughCheck;
    }

    public void setCantCastleThroughCheck(boolean cantCastleThroughCheck) {
        this.cantCastleThroughCheck = cantCastleThroughCheck;
    }

    public boolean isCantCastleAfterCheck() {
        return cantCastleAfterCheck;
    }

    public void setCantCastleAfterCheck(boolean cantCastleAfterCheck) {
        this.cantCastleAfterCheck = cantCastleAfterCheck;
    }

    public boolean isTopPlayer() {
        return topPlayer;
    }

    public void setTopPlayer(boolean topPlayer) {
        this.topPlayer = topPlayer;
    }

    public boolean isComputerPlayer() {
        return computerPlayer;
    }

    public void setComputerPlayer(boolean computerPlayer) {
        this.computerPlayer = computerPlayer;
    }

    public TimeLimit getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(TimeLimit timeLimit) {
        this.timeLimit = timeLimit;
    }
}
