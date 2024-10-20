package com.example.chessbotv2.bot;

import java.util.ArrayList;

public class Bot {
    Board board;
    MoveGenerator moveGenerator;
    public Bot() {
        board = new Board();
        moveGenerator = new MoveGenerator(board);
        ArrayList<Move> moves = moveGenerator.generateMoves();
    }

    public Bot(String fen) {
        board = new Board(fen);
        moveGenerator = new MoveGenerator(board);
    }

    public void resetBoard() {
        board.resetBoard();
    }
}
