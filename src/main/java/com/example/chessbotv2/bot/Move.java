package com.example.chessbotv2.bot;

public class Move {
    int startingSquare;
    int targetSquare;
    boolean isEnPassant;
    boolean isCastle;
    boolean isTwoAhead;
    boolean isPromoteToQueen;
    boolean isPromoteToBishop;
    boolean isPromoteToKnight;
    boolean isPromoteToRook;
    static char Castle = 'C';
    static char EnPassant = 'E';
    static char TwoAhead = 'T';
    static char NormalMove = 'M';
    static char Promote_To_Queen = 'Q';
    static char Promote_To_Bishop = 'B';
    static char Promote_To_Knight = 'N';
    static char Promote_To_Rook = 'R';
    public Move(int startingSquare, int targetSquare, char moveType) {
        this.startingSquare = startingSquare;
        this.targetSquare = targetSquare;
        isCastle = moveType == Castle;
        isEnPassant = moveType == EnPassant;
        isTwoAhead = moveType == TwoAhead;
        isPromoteToQueen = moveType == Promote_To_Queen;
        isPromoteToBishop = moveType == Promote_To_Bishop;
        isPromoteToKnight = moveType == Promote_To_Knight;
        isPromoteToRook = moveType == Promote_To_Rook;
    }

    @Override
    public String toString() {
        return "[" + startingSquare + " -> " + targetSquare + "] : " + getMoveType();
    }

    private String getMoveType() {
        if (isCastle) return "Castle";
        else if (isEnPassant) return "En Passant";
        else if (isTwoAhead) return "Two Ahead";
        else if (isPromoteToRook) return "Rook";
        else if (isPromoteToQueen) return "Queen";
        else if (isPromoteToBishop) return "Bishop";
        else if (isPromoteToKnight) return "Knight";
        else return "Normal";
    }
}
