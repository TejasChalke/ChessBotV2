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

        whiteEval += getKingInCornerBonus(board.kingIndex[0], board.kingIndex[1], pieceValues[0], pieceValues[1]) + (!board.isWhiteToMove() && board.isChecked ? 200 : 0);
        blackEval += getKingInCornerBonus(board.kingIndex[1], board.kingIndex[0], pieceValues[1], pieceValues[0]) + (board.isWhiteToMove() && board.isChecked ? 200 : 0);

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
                if (Pieces.isWhite(piece)) pieceValues[0] += (2 + index / 8) * 30;
                else pieceValues[1] += (9 - index / 8) * 30;
            }
        }

        int whitePawnMoveMultiplier = Math.max(1, ((pieceValues[1] + 1) / (pieceValues[3] + 1)) / 100);
        int blackPawnMoveMultiplier = Math.max(1, ((pieceValues[0] + 1) / (pieceValues[2] + 1)) / 100);

        for (int index=0; index<64; index++) {
            int piece = board.board[index];
            if (Pieces.isNone(piece)) continue;

            if (Pieces.isPawn(piece)) {
                if (Pieces.isWhite(piece)) {
                    // encourage pawns to move forward
                    pieceValues[0] += (2 + index / 8) * 30 / whitePawnMoveMultiplier;
                    pieceValues[0] += PieceSquareTables.WHITE_PAWN[index];
                }
                else {
                    // encourage pawns to move forward
                    pieceValues[1] += (9 - index / 8) * 30 / blackPawnMoveMultiplier;
                    pieceValues[1] += PieceSquareTables.BLACK_PAWN[index];
                }
            }
            else if (Pieces.isKing(piece)) {
                if (Pieces.isWhite(piece)) {
                    pieceValues[0] += PieceSquareTables.WHITE_KING[index];
                }
                else {
                    pieceValues[1] += PieceSquareTables.BLACK_KING[index];
                }
            }
            else if (Pieces.isQueen(piece)) {
                if (Pieces.isWhite(piece)) {
                    pieceValues[0] += PieceSquareTables.WHITE_QUEEN[index];
                }
                else {
                    pieceValues[1] += PieceSquareTables.BLACK_QUEEN[index];
                }
            }
            else if (Pieces.isRook(piece)) {
                if (Pieces.isWhite(piece)) {
                    pieceValues[0] += PieceSquareTables.WHITE_ROOK[index];
                }
                else {
                    pieceValues[1] += PieceSquareTables.BLACK_ROOK[index];
                }
            }
            else if (Pieces.isBishop(piece)) {
                if (Pieces.isWhite(piece)) {
                    pieceValues[0] += PieceSquareTables.WHITE_BISHOP[index];
                }
                else {
                    pieceValues[1] += PieceSquareTables.BLACK_BISHOP[index];
                }
            }
            else if (Pieces.isKnight(piece)) {
                if (Pieces.isWhite(piece)) {
                    pieceValues[0] += PieceSquareTables.WHITE_KNIGHT[index];
                }
                else {
                    pieceValues[1] += PieceSquareTables.BLACK_KNIGHT[index];
                }
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

    int getKingInCornerBonus(int playerKing, int enemyKing, int friendlyPieceWeight, int enemyPieceWeight) {
        int bonus = 0;

        int enemyRank = enemyKing / 8, enemyFile = enemyKing % 8;
        int ver = Math.min(enemyRank, 7 - enemyRank);
        int hor = Math.min(enemyFile, 7 - enemyFile);
        int dist = Math.max(ver, hor);
        bonus += (8 - dist) * 40;

        int playerRank = playerKing / 8, playerFile = playerKing % 8;
        ver = Math.abs(enemyRank - playerRank);
        hor = Math.abs(enemyFile - playerFile);
        dist = Math.max(ver, hor);

        bonus += (16 - dist) * 30;
        return bonus * (5 + (friendlyPieceWeight - enemyPieceWeight) / 100);
    }

    public int getCaptureValue(int capturingPiece, int capturedPiece) {
        int capturingPieceValue = getPieceValue(capturingPiece);
        int capturedPieceValue = getPieceValue(capturedPiece);
        return capturedPieceValue * 10 - capturingPieceValue;
    }

    public int getPawnAttackedPenalty(int piece, Move move) {
        int penalty = 0, file = move.targetSquare % 8, rank = move.targetSquare / 8;
        if (Pieces.isWhite(piece)) {
            if (rank < 6 && file > 0 && !Pieces.isSameColor(piece, board.board[move.targetSquare + 7]) && Pieces.isPawn(board.board[move.targetSquare + 7])) {
                penalty += getPieceValue(piece);
            }
            if (rank < 6 && file < 7 && !Pieces.isSameColor(piece, board.board[move.targetSquare + 9]) && Pieces.isPawn(board.board[move.targetSquare + 9])) {
                penalty += getPieceValue(piece);
            }
        }
        else {
            if (rank > 1 && file > 0 && !Pieces.isSameColor(piece, board.board[move.targetSquare - 9]) && Pieces.isPawn(board.board[move.targetSquare - 9])) {
                penalty += getPieceValue(piece);
            }
            if (rank > 1 && file < 7 && !Pieces.isSameColor(piece, board.board[move.targetSquare - 7]) && Pieces.isPawn(board.board[move.targetSquare - 7])) {
                penalty += getPieceValue(piece);
            }
        }
        return penalty;
    }
}
