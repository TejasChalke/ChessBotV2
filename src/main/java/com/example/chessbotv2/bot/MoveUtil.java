package com.example.chessbotv2.bot;

import java.util.ArrayList;

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

    public static String getMoveNotation(Move move, int[] board, int enemyKingIndex) {
        if (move.isCastle) {
            return move.startingSquare < move.targetSquare ? "O-O" : "O-O-O";
        }

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
            else sb.append(currSquare.charAt(0));
        }

        if (!Pieces.isPawn(startingPiece) && !Pieces.isNone(targetPiece)) {
            sb.append('x');
        }

        sb.append(BoardUtil.getSquareName(move.targetSquare));
        int pieceToCheck = startingPiece;
        if (move.isPromoteToQueen) sb.append("=").append(Pieces.intToCharMap[pieceToCheck = (Pieces.White | Pieces.Queen)]);
        else if (move.isPromoteToRook) sb.append("=").append(Pieces.intToCharMap[pieceToCheck = (Pieces.White | Pieces.Rook)]);
        else if (move.isPromoteToBishop) sb.append("=").append(Pieces.intToCharMap[pieceToCheck = (Pieces.White | Pieces.Bishop)]);
        else if (move.isPromoteToKnight) sb.append("=").append(Pieces.intToCharMap[pieceToCheck = (Pieces.White | Pieces.Knight)]);

        if (!Pieces.isKing(pieceToCheck) && pieceIsCheckingKing(board, pieceToCheck | (Pieces.isWhite(startingPiece) ? Pieces.White : Pieces.Black), move.startingSquare, move.targetSquare, enemyKingIndex)) {
            sb.append("+");
        }
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

    public static boolean pieceIsCheckingKing(int[] board, int piece, int pieceSquare, int startingSquare, int kingIndex) {
        int kingRank = kingIndex / 8, kingFile = kingIndex % 8;
        int pieceRank = startingSquare / 8, pieceFile = startingSquare % 8;
        boolean isWhitePiece = Pieces.isWhite(piece);

        if (Pieces.isPawn(piece)) {
            if (isWhitePiece && kingRank == pieceRank + 1 && ((pieceFile > 0 && startingSquare + 7 == kingIndex) || (pieceFile < 7 && startingSquare + 9 == kingIndex))) return true;
            else if (!isWhitePiece && kingRank == pieceRank - 1 && ((pieceFile > 0 && startingSquare - 9 == kingIndex) || (pieceFile < 7 && startingSquare - 7 == kingIndex))) return true;
        }
        else if (Pieces.isKnight(piece)) {
            for (int targetSquare: preComputedKnightMoves[startingSquare]) {
                if (targetSquare == kingIndex) return true;
            }
        }

        if ((Pieces.isRook(piece) || Pieces.isQueen(piece)) && (pieceRank == kingRank || pieceFile == kingFile)) {
            int itr = 0, change = 0;
            if (pieceRank == kingRank) {
                if (pieceFile < kingFile) itr = change = 1;
                else itr = change = -1;
            }
            else {
                if (pieceRank < kingRank) itr = change = 8;
                else itr = change = -8;
            }

            while (startingSquare + itr != kingIndex && (startingSquare + itr == pieceSquare || Pieces.isNone(board[startingSquare + itr]))) itr += change;
            if (startingSquare + itr == kingIndex) return true;
            else if (Pieces.isRook(piece)) return false;
        }

        if ((Pieces.isBishop(piece) || Pieces.isQueen(piece)) && (Math.abs(kingFile - pieceFile) == Math.abs(kingRank - pieceRank))) {
            int itr = 0, change = 0;
            if (pieceRank < kingRank) {
                if (pieceFile < kingFile) itr = change = 9;
                else itr = change = 7;
            }
            else {
                if (pieceFile < kingFile) itr = change = -7;
                else itr = change = -9;
            }

            while (startingSquare + itr != kingIndex && (startingSquare + itr == pieceSquare || Pieces.isNone(board[startingSquare + itr]))) itr += change;
            return startingSquare + itr == kingIndex;
        }
        return false;
    }

    public static Move getMoveFromNotation(Board board, String moveNotation) {
        int startingSquare = -1, targetSquare = -1;
        char moveType = Move.NormalMove;
        try {
            // remove the '+' mark for checks
            if (moveNotation.charAt(moveNotation.length() - 1) == '+') {
                moveNotation = moveNotation.substring(0, moveNotation.length() - 1);
            }

            if (moveNotation.equals("O-O") || moveNotation.equals("O-O-O")) {
                startingSquare = board.isWhiteToMove() ? White_King_Start_Square : Black_King_Start_Square;
                targetSquare = moveNotation.equals("O-O") ? startingSquare + 2 : startingSquare - 2;
                return new Move(startingSquare, targetSquare, Move.Castle);
            }

            // promotions
            if (moveNotation.contains("=")) {
                moveType = moveNotation.charAt(moveNotation.length() - 1);
                moveNotation = moveNotation.substring(0, moveNotation.length() - 2);
            }

            if (moveNotation.length() == 2) {
                // pawn move, handles 1 and 2 squares ahead, and promotions
                targetSquare = BoardUtil.getSquareIndex(moveNotation);
                if (board.isWhiteToMove())  {
                    startingSquare = Pieces.isNone(board.board[targetSquare - 8]) ? targetSquare - 16 : targetSquare - 8;
                }
                else {
                    startingSquare = Pieces.isNone(board.board[targetSquare + 8]) ? targetSquare + 16 : targetSquare + 8;
                }
                if (Math.abs(startingSquare - targetSquare) / 8 == 2) moveType = Move.TwoAhead;
                return new Move(startingSquare, targetSquare, moveType);
            }

            // get the targetSquare
            targetSquare = BoardUtil.getSquareIndex(moveNotation.substring(moveNotation.length() - 2));
            moveNotation = moveNotation.substring(0, moveNotation.length() - 2);

            // remove the 'x' mark from the move
            if (moveNotation.charAt(moveNotation.length() - 1) == 'x') {
                moveNotation = moveNotation.substring(0, moveNotation.length() - 1);
            }

            // handles pawn captures and en passant
            if (moveNotation.length() == 1 && moveNotation.charAt(0) >= 'a' && moveNotation.charAt(0) <= 'z') {
                int startingRank = targetSquare / 8 + (board.isWhiteToMove() ?  -1 : + 1);
                int startingFile = moveNotation.charAt(0) - 'a';
                startingSquare = startingRank * 8 + startingFile;

                if (targetSquare == board.epSquare) moveType = Move.EnPassant;
                return new Move(startingSquare, targetSquare, moveType);
            }

            // gives the complete square notation
            if (moveNotation.length() == 3) {
                startingSquare = BoardUtil.getSquareIndex(moveNotation.substring(1));
                return new Move(startingSquare, targetSquare, moveType);
            }

            // moveNotation of length 1
            char movePiece = moveNotation.charAt(0);
            if (moveNotation.length() == 1) {
                if (movePiece == 'K') {
                    for (int possibleSquare: preComputedKingMoves[targetSquare]) {
                        if (!Pieces.isKing(board.board[possibleSquare]) || (board.isWhiteToMove() != Pieces.isWhite(board.board[possibleSquare]))) continue;
                        return new Move(possibleSquare, targetSquare, moveType);
                    }
                }
                if (movePiece == 'N') {
                    for (int possibleSquare: preComputedKnightMoves[targetSquare]) {
                        if (!Pieces.isKnight(board.board[possibleSquare]) || (board.isWhiteToMove() != Pieces.isWhite(board.board[possibleSquare]))) continue;
                        return new Move(possibleSquare, targetSquare, moveType);
                    }
                }

                for (int d=0; d<8; d++) {
                    int offSet = slidingDirectionOffset[d];
                    for (int i=1; i<=preComputedSlidingDistance[targetSquare][d]; i++) {
                        int possibleSquare = startingSquare + offSet * i;
                        if ((movePiece == 'B' && Pieces.isBishop(board.board[possibleSquare])) || (movePiece == 'R' && Pieces.isRook(board.board[possibleSquare])) || (movePiece == 'Q' && Pieces.isQueen(board.board[possibleSquare]))) {
                            if (board.isWhiteToMove() == Pieces.isWhite(board.board[possibleSquare])) {
                                return new Move(possibleSquare, targetSquare, moveType);
                            }
                        }
                        if (!Pieces.isNone(board.board[possibleSquare])) break;
                    }
                }
            }

            // moveNotation of length 2 is remaining
            // [Piece][Rank OR File]
            char rankOrFile = moveNotation.charAt(1);
            if (movePiece == 'N') {
                for (int possibleSquare: preComputedKnightMoves[targetSquare]) {
                    if (!Pieces.isKnight(board.board[possibleSquare]) || (board.isWhiteToMove() != Pieces.isWhite(board.board[possibleSquare]))) continue;
                    char rank = (char)('1' + possibleSquare / 8);
                    char file = (char)('a' + possibleSquare % 8);
                    if (rank == rankOrFile || file == rankOrFile) {
                        return new Move(possibleSquare, targetSquare, moveType);
                    }
                }
            }

            for (int d=0; d<8; d++) {
                int offSet = slidingDirectionOffset[d];
                for (int i=1; i<=preComputedSlidingDistance[targetSquare][d]; i++) {
                    int possibleSquare = startingSquare + offSet * i;
                    char rank = (char)('1' + possibleSquare / 8);
                    char file = (char)('a' + possibleSquare % 8);
                    if ((movePiece == 'B' && Pieces.isBishop(board.board[possibleSquare])) || (movePiece == 'R' && Pieces.isRook(board.board[possibleSquare])) || (movePiece == 'Q' && Pieces.isQueen(board.board[possibleSquare]))) {
                        if (board.isWhiteToMove() == Pieces.isWhite(board.board[possibleSquare]) && (rank == rankOrFile || file == rankOrFile)) {
                            return new Move(possibleSquare, targetSquare, moveType);
                        }
                    }
                    if (!Pieces.isNone(board.board[possibleSquare])) break;
                }
            }
        }
        catch (Exception e) {
            System.err.println("Unable to generate move from notation");
            System.out.println(e.getMessage());
        }
        return new Move(startingSquare, targetSquare, moveType);
    }
}
