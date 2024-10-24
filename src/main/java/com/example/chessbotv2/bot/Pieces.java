package com.example.chessbotv2.bot;

import java.util.HashMap;
import java.util.Map;

public class Pieces {
    static int[] charToIntMap = new int[128];
    static char[] intToCharMap = new char[32];
    static final int None = 0;
    static final int King = 1;
    static final int Queen = 2;
    static final int Pawn = 3;
    static final int Rook = 4;
    static final int Knight = 5;
    static final int Bishop = 6;
    static final int White = 8;
    static final int Black = 16;

    static {
        charToIntMap['k'] = Black |  King;
        charToIntMap['q'] = Black |  Queen;
        charToIntMap['p'] = Black |  Pawn;
        charToIntMap['r'] = Black |  Rook;
        charToIntMap['n'] = Black |  Knight;
        charToIntMap['b'] = Black |  Bishop;

        charToIntMap['K'] = White |  King;
        charToIntMap['Q'] = White |  Queen;
        charToIntMap['P'] = White |  Pawn;
        charToIntMap['R'] = White |  Rook;
        charToIntMap['N'] = White |  Knight;
        charToIntMap['B'] = White |  Bishop;

        intToCharMap[Black | King] = 'k';
        intToCharMap[Black | Queen] = 'q';
        intToCharMap[Black | Pawn] = 'p';
        intToCharMap[Black | Rook] = 'r';
        intToCharMap[Black | Knight] = 'n';
        intToCharMap[Black | Bishop] = 'b';

        intToCharMap[White | King] = 'K';
        intToCharMap[White | Queen] = 'Q';
        intToCharMap[White | Pawn] = 'P';
        intToCharMap[White | Rook] = 'R';
        intToCharMap[White | Knight] = 'N';
        intToCharMap[White | Bishop] = 'B';

        intToCharMap[Black] = intToCharMap[White] = intToCharMap[None] = '-';
    }

    static int getPiece(char piece) {
        return charToIntMap[piece];
    }

    static char getPiece(int piece) {
        return intToCharMap[piece];
    }

    static boolean isSameColor(int piece, int coloredPiece) {
        return (coloredPiece & BoardUtil.PlayerMask & piece) != 0;
    }

    static boolean isSlidingPiece(int piece) {
        piece &= 7;
        return piece == Rook || piece == Bishop || piece == Queen;
    }

    static boolean isRook(int piece) {
        return (piece & 7) == Rook;
    }

    static boolean isBishop(int piece) {
        return (piece & 7) == Bishop;
    }

    static boolean isPawn(int piece) {
        return (piece & 7) == Pawn;
    }

    static boolean isKnight(int piece) {
        return (piece & 7) == Knight;
    }

    static boolean isKing(int piece) {
        return (piece & 7) == King;
    }

    static boolean isQueen(int piece) {
        return (piece & 7) == Queen;
    }

    static boolean isNone(int piece) { return (piece & 7) == None; }
}
