package com.example.chessbotv2.bot;

public class BoardUtil {
    static final String DefaultFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    static final int PlayerMask = 24; // 11000

    static int getSquareIndex(String squareStr) throws Exception {
        if (squareStr.length() != 2) {
            throw new Exception("Cannot convert square string to index, invalid format for: " + squareStr);
        }

        int file = squareStr.charAt(0) - 'a';
        int rank = squareStr.charAt(1) - '0';
        return rank * 8 + file;
    }

    static String getSquareName(int index) {
        if (index == -1) return "-";

        char file = (char)('a' + (index % 8));
        char rank = (char)('1' + index / 8);
        return "" + file + rank;
    }

    static boolean isSameRank(int square1, int square2) {
        return  square1 / 8 == square2 / 8;
    }
}
