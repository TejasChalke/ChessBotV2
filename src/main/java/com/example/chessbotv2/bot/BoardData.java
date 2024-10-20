package com.example.chessbotv2.bot;

public class BoardData {
    int[] kingIndex;
    int castleMask;
    int epSquare;
    int halfMoveClock;
    int fullMoveCounter;
    boolean isChecked;
    boolean isDoubleChecked;
    int startingPiece;
    int targetPiece;

    public BoardData(int[] kingIndex, int castleMask, int epSquare, int halfMoveClock, int fullMoveCounter, boolean isChecked, boolean isDoubleChecked, int startingPiece, int targetPiece) {
        this.kingIndex = kingIndex;
        this.castleMask = castleMask;
        this.epSquare = epSquare;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveCounter = fullMoveCounter;
        this.isChecked = isChecked;
        this.isDoubleChecked = isDoubleChecked;
        this.startingPiece = startingPiece;
        this.targetPiece = targetPiece;
    }

    public void resetBoardData(Board board) {
        board.kingIndex = kingIndex;
        board.castleMask = castleMask;
        board.epSquare = epSquare;
        board.halfMoveClock = halfMoveClock;
        board.fullMoveCounter = fullMoveCounter;
        board.isChecked = isChecked;
        board.isDoubleChecked = isDoubleChecked;
    }
}
