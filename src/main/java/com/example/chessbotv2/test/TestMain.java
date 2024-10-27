package com.example.chessbotv2.test;

import com.example.chessbotv2.bot.Bot;
import com.example.chessbotv2.bot.Move;
import com.example.chessbotv2.bot.MoveUtil;

public class TestMain {
    public static void main(String[] args) {
        // test a match between 2 bots
//        testMatch();

        // default starting position
//        testBot("Default position test", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", false, -1, -1, -1, -1, 4, true);

        // default testing pos 2 from website
//        testBot("Default position 2 test", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", false, -1, -1, -1, -1, 4, true);

        // default testing pos 3 from website
//        testBot("Default position 3 test", "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", false, -1, -1, -1, -1, 6, true);

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

        // check for stalemate
//        testBot("Checkmate stalemate test test", "6k1/3Q4/5R2/4K3/8/8/8/8 w - -", true, -1, -1, -1, -1, 1, false);

        // check for notations
        testNotations("Same file knight test", "7k/6pp/1N6/3p4/1N6/8/PP6/K7 b - - 0 1", new Move(25, 35, 'N'));
        testNotations("Same rank knight test", "7k/6pp/8/3p4/1N3N2/8/PP6/K7 b - - 0 1", new Move(25, 35, 'N'));
        testNotations("Unique file and rank knight", "7k/2N3pp/8/3p4/1N6/8/PP6/K7 b - - 0 1", new Move(25, 35, 'N'));
        testNotations("Same file same rank knight test", "7k/6pp/1N6/3p4/1N3N2/8/PP6/K7 b - - 0 1", new Move(25, 35, 'N'));

        testNotations("Same file knight test", "7k/6pp/1N6/8/1N6/8/PP6/K7 b - - 0 1", new Move(25, 35, 'N'));
        testNotations("Same rank knight test", "7k/6pp/8/8/1N3N2/8/PP6/K7 b - - 0 1", new Move(25, 35, 'N'));
        testNotations("Unique file and rank knight", "7k/2N3pp/8/8/1N6/8/PP6/K7 b - - 0 1", new Move(25, 35, 'N'));
        testNotations("Same file same rank knight test", "7k/6pp/1N6/8/1N3N2/8/PP6/K7 b - - 0 1", new Move(25, 35, 'N'));

        testNotations("Same file bishop test", "1B5k/6pp/3p4/8/1B6/8/PP6/K7 b - - 0 1", new Move(25, 43, 'N'));
        testNotations("Same rank bishop test", "7k/6pp/3p4/8/1B3B2/8/PP6/K7 b - - 0 1", new Move(25, 43, 'N'));
        testNotations("Unique file and rank bishop test", "1B5k/6pp/3p4/8/1B6/6B1/PP6/K7 b - - 0 1", new Move(25, 43, 'N'));
        testNotations("Same file same rank bishop test", "1B5k/6pp/3p4/8/1B3B2/8/PP6/K7 b - - 0 1", new Move(25, 43, 'N'));

        testNotations("Same file bishop test", "1B5k/6pp/8/8/1B6/8/PP6/K7 b - - 0 1", new Move(25, 43, 'N'));
        testNotations("Same rank bishop test", "7k/6pp/8/8/1B3B2/8/PP6/K7 b - - 0 1", new Move(25, 43, 'N'));
        testNotations("Unique file and rank bishop test", "1B5k/6pp/8/8/1B6/6B1/PP6/K7 b - - 0 1", new Move(25, 43, 'N'));
        testNotations("Same file same rank bishop test", "1B5k/6pp/8/8/1B3B2/8/PP6/K7 b - - 0 1", new Move(25, 43, 'N'));
    }

    public static void testNotations(String testName, String fen, Move move) {
        if (fen == null || fen.isEmpty()) {
            System.err.println("Fen cannot be empty for testing");
            return;
        }
        Bot bot = new Bot(fen);
        System.out.println("Running : " + testName);
        System.out.println("For move " + move + " the notation is " + MoveUtil.getMoveNotation(move, bot.board.board));
        System.out.println("------------------------------------------------------");
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
        System.out.println("--------------------------------------------------------------------------");
        System.out.println();
    }

    public static void testMatch() {
        Bot botWhite = new Bot();
        Bot botBlack = new Bot();
        boolean whiteToMove = true;
        boolean matchEnded = false;
        int moveCount = 0;
        while (!matchEnded && moveCount < 100) {
            if (whiteToMove) {
                Move bestMove = botWhite.playMove();
                if (bestMove.startingSquare == -1) {
                    // stalemate
                    System.out.println("Stalemate");
                    matchEnded = true;
                }
                else if (bestMove.startingSquare == 64) {
                    // checkmate
                    System.out.println("Black Wins");
                    matchEnded = true;
                }
                else {
                    botBlack.makeMove(bestMove, false);
                }
            }
            else {
                Move bestMove = botBlack.playMove();
                if (bestMove.startingSquare == -1) {
                    // stalemate
                    System.out.println("Stalemate");
                    matchEnded = true;
                }
                else if (bestMove.startingSquare == 64) {
                    // checkmate
                    System.out.println("White Wins");
                    matchEnded = true;
                }
                else {
                    botWhite.makeMove(bestMove, false);
                }
            }
            botWhite.board.displayBoard();
            whiteToMove = !whiteToMove;
            moveCount++;
        }
    }
}
