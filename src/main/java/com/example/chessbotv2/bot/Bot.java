package com.example.chessbotv2.bot;

public class Bot {
    Board board;
    MoveGenerator moveGenerator;
    public Bot() {
        board = new Board();
        moveGenerator = new MoveGenerator(board);
        moveGenerator.generateMoves();
    }

    public Bot(String fen) {
        board = new Board(fen);
        moveGenerator = new MoveGenerator(board);
    }

    public void resetBoard() {
        board.resetBoard();
    }
}
