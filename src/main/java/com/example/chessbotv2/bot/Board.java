package com.example.chessbotv2.bot;

import java.util.ArrayList;
import java.util.Arrays;

public class Board {
    public int[] board;
    int[] kingIndex; // 0 -> white, 1 -> black
    int playerToMove;
    int castleMask;
    public int epSquare;
    int halfMoveClock;
    int fullMoveCounter;
    boolean isChecked;
    boolean isDoubleChecked;

    public Board() {
        init();
        setUpBoard(BoardUtil.DefaultFEN);
    }

    public Board(String fen) {
        init();
        setUpBoard(fen);
    }

    private void init() {
        board = new int[64];
        kingIndex = new int[2];
        playerToMove = Pieces.White;
        castleMask = 0;
        epSquare = -1;
        fullMoveCounter = 0;
        halfMoveClock = 0;
        isChecked = isDoubleChecked = false;
    }

    void setUpBoard(String fen) {
        Arrays.fill(board, 0);
        int rank = 7, file = 0;
        String[] str = fen.split(" ");

        String pieces = str[0];
        for (char c: pieces.toCharArray()) {
            if (c == '/') {
                rank--;
                file = 0;
            } else if (c >= '0' && c <= '9') {
                file += c - '0';
            } else {
                board[rank * 8 + file] = Pieces.getPiece(c);
                int square = rank * 8 + file;
                if (c == 'K') kingIndex[0] = square;
                else if (c == 'k') kingIndex[1] = square;
                file++;
            }
        }

        if (str.length > 1 && str[1].charAt(0) == 'b') {
            playerToMove = Pieces.Black;
        }

        if (str.length > 2) {
            String castleStr = str[2];
            if (castleStr.indexOf('K') != -1) castleMask |= MoveUtil.White_King_Side_Castle_Mask;
            if (castleStr.indexOf('Q') != -1) castleMask |= MoveUtil.White_Queen_Side_Castle_Mask;
            if (castleStr.indexOf('k') != -1) castleMask |= MoveUtil.Black_King_Side_Castle_Mask;
            if (castleStr.indexOf('q') != -1) castleMask |= MoveUtil.Black_Queen_Side_Castle_Mask;
        }

        if (str.length > 3) {
            String epStr = str[3];
            try {
                epSquare = epStr.equals("-") ? -1 : BoardUtil.getSquareIndex(epStr);
            } catch (Exception e) {
                System.out.println("Error while parsing FEN: " + e.getMessage());
            }
        }

        if (str.length > 4) {
            halfMoveClock = Integer.parseInt(str[4]);
        }

        if (str.length > 5) {
            fullMoveCounter = Integer.parseInt(str[5]);
        }

//        displayBoard();
    }

    public void resetBoard() {
        init();
        setUpBoard(BoardUtil.DefaultFEN);
    }

    public boolean isWhiteToMove() {
        return playerToMove == Pieces.White;
    }

    public void displayBoard() {
        for (int r=7; r>=0; r--) {
            System.out.printf("%d ", (r+1));
            for(int c=0; c<8; c++) {
                System.out.printf("%c ", Pieces.getPiece(board[r * 8 + c]));
            }
            System.out.println();
        }
        System.out.printf("%c ", ' ');
        for(int c=0; c<8; c++) {
            System.out.printf("%c ", (char)('a' + c));
        }
        System.out.println();
        System.out.println("playerToMove: " + (playerToMove == 8 ? "White" : "Black"));
        System.out.println("CastleMask: " + Integer.toString(castleMask, 2));
        System.out.println("epSquare: " + epSquare + "(" + BoardUtil.getSquareName(epSquare) + ")");
        System.out.println("halfMoveClock: " + halfMoveClock);
        System.out.println("fullMoveCounter: " + fullMoveCounter);
        System.out.println("--------------------------------------------------------------------------");
    }
}
