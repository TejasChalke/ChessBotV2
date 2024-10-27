package com.example.chessbotv2.bot;

public class PieceSquareTables {
    // Pawn table for white
    public static final int[] WHITE_PAWN = {
            0,   5,  10,  15,  15,  10,   5,   0,
            5,  10,  15,  20,  20,  15,  10,   5,
            1,   5,  10,  15,  15,  10,   5,   1,
            0,   5,  10,  20,  20,  10,   5,   0,
            5,  10,  10,  15,  15,  10,  10,   5,
            5,   5,   5,  10,  10,   5,   5,   5,
            10,  10,   0, -20, -20,   0,  10,  10,
            0,   0,   0,   0,   0,   0,   0,   0
    };

    // Knight table for white
    public static final int[] WHITE_KNIGHT = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20,   0,   5,   5,   0, -20, -40,
            -30,   5,  10,  15,  15,  10,   5, -30,
            -30,   0,  15,  20,  20,  15,   0, -30,
            -30,   5,  15,  20,  20,  15,   5, -30,
            -30,   0,  10,  15,  15,  10,   0, -30,
            -40, -20,   0,   0,   0,   0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50
    };

    // Bishop table for white
    public static final int[] WHITE_BISHOP = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10,   5,   0,   0,   0,   0,   5, -10,
            -10,  10,  10,  10,  10,  10,  10, -10,
            -10,   0,  10,  10,  10,  10,   0, -10,
            -10,   5,   5,  10,  10,   5,   5, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -20, -10, -10, -10, -10, -10, -10, -20
    };

    // Rook table for white
    public static final int[] WHITE_ROOK = {
            0,   0,   5,  10,  10,   5,   0,   0,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            -5,   0,   0,   0,   0,   0,   0,  -5,
            5,  10,  10,  10,  10,  10,  10,   5,
            0,   0,   5,  10,  10,   5,   0,   0
    };

    // Queen table for white
    public static final int[] WHITE_QUEEN = {
            -20, -10, -10,  -5,  -5, -10, -10, -20,
            -10,   0,   5,   0,   0,   0,   0, -10,
            -10,   5,   5,   5,   5,   5,   0, -10,
            0,   0,   5,   5,   5,   5,   0,  -5,
            -10,   0,   5,   5,   5,   5,   0, -10,
            -10,   5,   5,   5,   5,   5,   0, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -20, -10, -10,  -5,  -5, -10, -10, -20
    };

    // King table for white (middle game)
    public static final int[] WHITE_KING = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20,  20,   0,   0,   0,   0,  20,  20,
            20,  30,  10,   0,   0,  10,  30,  20
    };

    // Flip for black by reversing the order of each white table
    public static final int[] BLACK_PAWN = reverse(WHITE_PAWN);
    public static final int[] BLACK_KNIGHT = reverse(WHITE_KNIGHT);
    public static final int[] BLACK_BISHOP = reverse(WHITE_BISHOP);
    public static final int[] BLACK_ROOK = reverse(WHITE_ROOK);
    public static final int[] BLACK_QUEEN = reverse(WHITE_QUEEN);
    public static final int[] BLACK_KING = reverse(WHITE_KING);

    private static int[] reverse(int[] table) {
        int[] reversed = new int[table.length];
        for (int i = 0; i < table.length; i++) {
            reversed[i] = table[table.length - i - 1];
        }
        return reversed;
    }
}

