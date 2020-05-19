package chess.board;

public enum State{
    INPROGRESS, WHITEWON, BLACKWON, STALEMATE;
    @Override
    public String toString() {
        switch (this) {
            case INPROGRESS:
                return "Game In Progress";
            case WHITEWON:
                return "White Won!";
            case BLACKWON:
                return "Black Won!";
            case STALEMATE:
                return "Stalemate!";
            default:
                return "Other";
        }
    }

    public int getGameOutcome() {
        switch (this) {
            case WHITEWON:
                return 1;
            case BLACKWON:
                return -1;
            default:
                return 0;

        }
    }

}

