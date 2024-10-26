package com.example.chessbotv2.bot;

public class Evaluator {
    final int PAWN_VALUE = 100;
    final int QUEEN_VALUE = 900;
    final int ROOK_VALUE = 500;
    final int BISHOP_VALUE = 320;
    final int KNIGHT_VALUE = 300;
    Board board;

    public Evaluator(Board board) {
        this.board = board;
    }

    public int evaluate() {
        int[] pieceValues = getTotalPieceValues();
        int whiteEval = pieceValues[0];
        int blackEval = pieceValues[1];
        int perspective = board.isWhiteToMove() ? 1: -1;
        return (whiteEval - blackEval) * perspective;
    }

    int[] getTotalPieceValues() {
        int[] pieceValues = new int[2];
        for (int piece: board.board) {
            if (Pieces.isNone(piece)) continue;
            pieceValues[Pieces.isWhite(piece) ? 0 : 1] += getPieceValue(piece);
        }
        return pieceValues;
    }

    int getPieceValue(int piece) {
        return switch (piece & 7) {
            case Pieces.Pawn -> PAWN_VALUE;
            case Pieces.Queen -> QUEEN_VALUE;
            case Pieces.Rook -> ROOK_VALUE;
            case Pieces.Knight -> KNIGHT_VALUE;
            case Pieces.Bishop -> BISHOP_VALUE;
            default -> 0;
        };
    }

    public int getCaptureValue(int capturingPiece, int capturedPiece) {
        int capturingPieceValue = getPieceValue(capturingPiece);
        int capturedPieceValue = getPieceValue(capturedPiece);
        return capturedPieceValue * 2 - capturingPieceValue / 3;
    }
}
