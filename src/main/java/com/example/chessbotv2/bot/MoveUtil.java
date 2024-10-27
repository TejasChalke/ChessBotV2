package com.example.chessbotv2.bot;

import java.util.ArrayList;
import java.util.List;

public class MoveUtil {
    static ArrayList<Integer>[] preComputedKnightMoves = new ArrayList[64];
    static ArrayList<Integer>[] preComputedKingMoves = new ArrayList[64];
    static ArrayList<Integer>[] preComputedBlackPawnAttacks = new ArrayList[64];
    static ArrayList<Integer>[] preComputedWhitePawnAttacks = new ArrayList[64];
    /*
    * First four are straight
    * Last four are diagonal
    */
    static int[] slidingDirectionOffset = new int[] {8, -8, 1, -1, 7, -7, 9, -9};
    /*
    * 0 -> white pawn
    * 1 -> black pawn
    */
    static int[][] pawnAttackOffset = new int[][] {{7, 9}, {-7, -9}};
    static int[][] preComputedSlidingDistance = new int[64][];
    static final int White_King_Start_Square = 4;
    static final int White_Queen_Side_Rook_Square = 0;
    static final int White_King_Side_Rook_Square = 7;
    static final int Black_King_Start_Square = 60;
    static final int Black_Queen_Side_Rook_Square = 56;
    static final int Black_King_Side_Rook_Square = 63;
    static final int White_King_Side_Castle_Mask = 1;
    static final int White_Queen_Side_Castle_Mask = 2;
    static final int White_Castle_Mask = 3;
    static final int Black_King_Side_Castle_Mask = 4;
    static final int Black_Queen_Side_Castle_Mask = 8;
    static final int Black_Castle_Mask = 12;
    static final int Complete_Castle_Mask = 15;

    static {
        int[][] knightDir = new int[][] {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};
        for (int index=0; index<64; index++) {
            int rank = index / 8;
            int file = index % 8;

            // compute indexes for knight
            preComputedKnightMoves[index] = new ArrayList<>();
            for (int[] dir: knightDir) {
                int r = rank + dir[0];
                int f = file + dir[1];
                if (r > 7 || r < 0 || f > 7 || f < 0) continue;
                preComputedKnightMoves[index].add(r * 8 + f);
            }

            // compute indexes for king
            preComputedKingMoves[index] = new ArrayList<>();
            for (int offset: slidingDirectionOffset) {
                int nextIndex = index + offset;
                if (nextIndex < 0 || nextIndex >= 64 || (file == 0 && (offset == 7 || offset == -9 || offset == -1)) || (file == 7 && (offset == 9 || offset == -7 || offset == 1))) continue;
                preComputedKingMoves[index].add(nextIndex);
            }

            // compute pawn moves
            preComputedBlackPawnAttacks[index] = new ArrayList<>();
            preComputedWhitePawnAttacks[index] = new ArrayList<>();
            if (rank > 0 && rank < 7) {
                if (file > 0) {
                    preComputedBlackPawnAttacks[index].add(index-9);
                    preComputedWhitePawnAttacks[index].add(index+7);
                }
                if (file < 7) {
                    preComputedBlackPawnAttacks[index].add(index-7);
                    preComputedWhitePawnAttacks[index].add(index+9);
                }
            }

            // compute distance for sliding pieces
            int top = 7 - rank;
            int bottom = rank;
            int left = file;
            int right = 7 - file;

            // with reference to slidingDirectionOffset
            preComputedSlidingDistance[index] = new int[] {
                top,
                bottom,
                right,
                left,
                Math.min(top, left),
                Math.min(bottom, right),
                Math.min(top, right),
                Math.min(bottom, left)
            };
        }
    }

    public static String getMoveNotation(Move move, int[] board) {
        StringBuilder sb = new StringBuilder();
        int startingPiece = board[move.startingSquare];
        int targetPiece = move.isEnPassant ? Pieces.Pawn : board[move.targetSquare];

        if (!Pieces.isPawn(startingPiece)) {
            char pieceCharacter = Pieces.intToCharMap[Pieces.White | (startingPiece & 7)];
            sb.append(pieceCharacter);
        }
        else if (!Pieces.isNone(targetPiece)) {
            // handle pawn moves and captures
            char file = (char)('a' + (move.startingSquare % 8));
            sb.append(file).append('x');
        }

        ArrayList<String> pieceSquares = getPieceSquares(move, board, startingPiece);
        if (pieceSquares.size() > 1) {
            String currSquare = BoardUtil.getSquareName(move.startingSquare);
            boolean hasSameFile = false;
            boolean hasSameRank = false;
            for (String otherSquare: pieceSquares) {
                if (!currSquare.equals(otherSquare) && otherSquare.charAt(0) == currSquare.charAt(0)) hasSameFile = true;
                else if (!currSquare.equals(otherSquare) && otherSquare.charAt(1) == currSquare.charAt(1)) hasSameRank = true;
            }

            if (hasSameFile && hasSameRank) sb.append(currSquare);
            else if (hasSameFile) sb.append(currSquare.charAt(1));
            else if (hasSameRank) sb.append(currSquare.charAt(0));
            else if (Pieces.isSlidingPiece(startingPiece)) sb.append(currSquare);
        }

        if (!Pieces.isPawn(startingPiece) && !Pieces.isNone(targetPiece)) {
            sb.append('x');
        }

        sb.append(BoardUtil.getSquareName(move.targetSquare));
        return sb.toString();
    }

    private static ArrayList<String> getPieceSquares(Move move, int[] board, int startingPiece) {
        ArrayList<String> pieceSquares = new ArrayList<>();
        if (Pieces.isSlidingPiece(startingPiece)) {
            int startIndex = Pieces.isBishop(startingPiece) ? 4 : 0;
            int endIndex = Pieces.isRook(startingPiece) ? 4 : 8;

            for (int d=startIndex; d<endIndex; d++) {
                int offSet = MoveUtil.slidingDirectionOffset[d];

                for (int i = 1; i <= MoveUtil.preComputedSlidingDistance[move.targetSquare][d]; i++) {
                    int possibleSquare = move.targetSquare + offSet * i;

                    if (Pieces.isNone(board[possibleSquare])) continue;
                    if (Pieces.isSameColor(startingPiece, board[possibleSquare]) && Pieces.isSamePiece(startingPiece, board[possibleSquare])) {
                        pieceSquares.add(BoardUtil.getSquareName(possibleSquare));
                    }
                    break;
                }
            }
        }
        else if (Pieces.isKnight(startingPiece)) {
            for (int possibleSquare: preComputedKnightMoves[move.targetSquare]) {
                if (Pieces.isSameColor(startingPiece, board[possibleSquare]) && Pieces.isKnight(board[possibleSquare])) {
                    pieceSquares.add(BoardUtil.getSquareName(possibleSquare));
                }
            }
        }
        return pieceSquares;
    }

    public static Move getConvertedMove(String move) {
        return null;
    }
}
