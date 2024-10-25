package com.example.chessbotv2.test;

import com.example.chessbotv2.bot.Bot;

public class TestMain {
    public static void main(String[] args) {
        // default starting position
//        testBot("Default position test", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", false, -1, -1, -1, -1, 4, false);

        // default testing pos 2 from website
//        testBot("Default position 2 test", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", false, -1, -1, -1, -1, 4, true);

        // default testing pos 3 from website
        testBot("Default position 3 test", "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", false, -1, -1, -1, -1, 6, true);

        // default testing pos 5 from website
//        testBot("Default position 5 test", "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", false, -1, -1, -1, -1, 4, true);

        // default testing pos 6 from website
//        testBot("Default position 6 test", "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", false, -1, -1, -1, -1, 4, true);

        // pinned en passant pawn immediate left to the king
//        testBot("Pinned en passant pawn immediate left to the king test", "8/2p5/3p4/KP5r/3RPpk1/8/6P1/8 b - e3", false, -1, -1, -1, -1, 1, false);

        // pinned pawn capture check
//        testBot("Pinned pawn capture test", "8/2p5/3p4/KP5r/1R3p1k/4P3/6P1/8 b - -", true, 33, 33, 32, 41, 1, false);

        // pawn next to ep pawn
//        testBot("Pawn next to EP pawn test", "8/8/8/KPpp3r/5pk1/1R6/4P1P1/8 w - c6 0 1", true, -1, -1, -1, -1, 1, false);
    }

    public static void testBot(String testName, String fen, boolean testAttackMask, int pinSquare, int startSquare, int kingSquare, int targetSquare, int depth, boolean rangeTest) {
        Bot bot = null;;
        if (fen != null) bot = new Bot(fen);
        else bot = new Bot();

        System.out.println("Running: " + testName);
        if (testAttackMask) {
            bot.testAttackMask();
        }
        if (pinSquare != -1) {
            bot.testPinned(pinSquare);
        }
        if (startSquare != -1 && kingSquare != 1 && targetSquare != -1) {
            bot.testMovingInPinDirection(startSquare, kingSquare, targetSquare);
        }
        if (depth != -1) {
            bot.testMoves(depth, rangeTest, true, false);
        }
    }
}
