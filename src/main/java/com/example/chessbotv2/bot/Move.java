package com.example.chessbotv2.bot;

public class Move {
    int startingSquare;
    int targetSquare;
    boolean isEnPassant;
    boolean isCastle;
    boolean isTwoAhead;
    static char Castle = 'C';
    static char EnPassant = 'E';
    static char TwoAhead = 'T';
    static char NormalMove = 'N';
    public Move(int startingSquare, int targetSquare, char specialMove) {
        this.startingSquare = startingSquare;
        this.targetSquare = targetSquare;
        isCastle = specialMove == Castle;
        isEnPassant = specialMove == EnPassant;
        isTwoAhead = specialMove == TwoAhead;
    }
}
