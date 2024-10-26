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

        whiteEval += getKingInCornerBonus(board.kingIndex[0], board.kingIndex[1], pieceValues[1]) + (!board.isWhiteToMove() && board.isChecked ? 200 : 0);
        blackEval += getKingInCornerBonus(board.kingIndex[1], board.kingIndex[0], pieceValues[0]) + (board.isWhiteToMove() && board.isChecked ? 200 : 0);

        int perspective = board.isWhiteToMove() ? 1: -1;
        return (whiteEval - blackEval) * perspective;
    }

    // white val, black val, piece count
    int[] getTotalPieceValues() {
        int[] pieceValues = new int[4];
        for (int index=0; index<64; index++) {
            int piece = board.board[index];
            if (Pieces.isNone(piece)) continue;

            pieceValues[Pieces.isWhite(piece) ? 0 : 1] += getPieceValue(piece);

            if (Pieces.isPawn(piece)) {
                // encourage pawns to move forward
                if (Pieces.isWhite(piece)) pieceValues[0] += index / 8 * 500;
                else pieceValues[1] += (7 - index / 8) * 500;
            }
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

    int getKingInCornerBonus(int playerKing, int enemyKing, int enemyPieceWeight) {
        int bonus = 0;

        int enemyRank = enemyKing / 8, enemyFile = enemyKing % 8;
        int ver = Math.min(enemyRank, 7 - enemyRank);
        int hor = Math.min(enemyFile, 7 - enemyFile);
        int dist = Math.min(ver, hor);
        bonus += (4 - dist) * 800;

        int playerRank = playerKing / 8, playerFile = playerKing % 8;
        ver = Math.abs(enemyRank - playerRank);
        hor = Math.abs(enemyFile - playerFile);
        dist = Math.min(ver, hor);

        bonus += (7 - dist) * 500;
        return bonus * (50 - enemyPieceWeight / 100);
    }

    public int getCaptureValue(int capturingPiece, int capturedPiece) {
        int capturingPieceValue = getPieceValue(capturingPiece);
        int capturedPieceValue = getPieceValue(capturedPiece);
        return capturedPieceValue * 10 - capturingPieceValue;
    }
}
