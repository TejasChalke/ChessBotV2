package com.example.chessbotv2.bot;

import java.util.ArrayList;

public class MoveGenerator {
    Board board;
    long checkMask; // squares which can be blocked to block a check
    long pinMask; // used to store the pieces which cannot move
    long attackMask; // squares on which the king cant move
    int epPinnedSquare;
    int currentPlayerKingIndex;
    public MoveGenerator(Board board) {
        this.board = board;
    }

    public ArrayList<Move> generateMoves() {
        // get the attack mask
        setAttackMask();
//        displayMasks();
        return getLegalMoves();
    }

    private ArrayList<Move> getLegalMoves() {
        ArrayList<Move> legalMoves = new ArrayList<>();
        // generate king moves first in case of double check to return early
        for (int targetSquare: MoveUtil.preComputedKingMoves[board.kingIndex[board.isWhiteToMove() ? 0 : 1]]) {
            // square is not attacked by an enemy piece
            if (isNotAttacked(targetSquare) && board.board[targetSquare] == Pieces.None) {
                legalMoves.add(new Move(currentPlayerKingIndex, targetSquare, Move.NormalMove));
            }
        }
        if (board.isDoubleChecked) return legalMoves;

        /*
        * Castling moves, no need to check for the position of
        * the rook or king because if they have moved previously
        * right to castle will be lost any way
        * */
        if (board.isWhiteToMove()) {
            // king side castling
            if (!board.isChecked && (board.castleMask & MoveUtil.White_King_Side_Castle_Mask) != 0 && board.board[MoveUtil.White_King_Start_Square + 1] == Pieces.None && board.board[MoveUtil.White_King_Start_Square + 2] == Pieces.None && isNotAttacked(MoveUtil.White_King_Start_Square + 1) && isNotAttacked(MoveUtil.White_King_Start_Square + 2)) {
                legalMoves.add(new Move(currentPlayerKingIndex, currentPlayerKingIndex + 2, Move.Castle));
            }
            // queen side castling
            if (!board.isChecked && (board.castleMask & MoveUtil.White_Queen_Side_Castle_Mask) != 0 && board.board[MoveUtil.White_King_Start_Square - 1] == Pieces.None && board.board[MoveUtil.White_King_Start_Square - 2] == Pieces.None && board.board[MoveUtil.White_King_Start_Square - 3] == Pieces.None && isNotAttacked(MoveUtil.White_King_Start_Square - 1) && isNotAttacked(MoveUtil.White_King_Start_Square - 2)) {
                legalMoves.add(new Move(currentPlayerKingIndex, currentPlayerKingIndex - 2, Move.Castle));
            }
        } else {
            // king side castling
            if (!board.isChecked && (board.castleMask & MoveUtil.Black_King_Side_Castle_Mask) != 0 && board.board[MoveUtil.Black_King_Start_Square + 1] == Pieces.None && board.board[MoveUtil.Black_King_Start_Square + 2] == Pieces.None && isNotAttacked(MoveUtil.Black_King_Start_Square + 1) && isNotAttacked(MoveUtil.Black_King_Start_Square + 2)) {
                legalMoves.add(new Move(currentPlayerKingIndex, currentPlayerKingIndex + 2, Move.Castle));
            }
            // queen side castling
            if (!board.isChecked && (board.castleMask & MoveUtil.Black_King_Side_Castle_Mask) != 0 && board.board[MoveUtil.Black_King_Start_Square - 1] == Pieces.None && board.board[MoveUtil.Black_King_Start_Square - 2] == Pieces.None && board.board[MoveUtil.Black_King_Start_Square - 3] == Pieces.None && isNotAttacked(MoveUtil.Black_King_Start_Square - 1) && isNotAttacked(MoveUtil.Black_King_Start_Square - 2)) {
                legalMoves.add(new Move(currentPlayerKingIndex, currentPlayerKingIndex - 2, Move.Castle));
            }
        }

//        System.out.println(currentPlayerKingIndex + " -> " + Pieces.getPiece(board.board[currentPlayerKingIndex]) + " : ");
//        for (Move move: legalMoves) System.out.println(move);
        for (int index=0; index<64; index++) {
            int piece = board.board[index];
            if (piece == Pieces.None || !Pieces.isSameColor(piece, board.playerToMove) || Pieces.isKing(piece)) continue;

//            int prevSize = legalMoves.size();
            if (Pieces.isSlidingPiece(piece)) generateSlidingMoves(piece, index, legalMoves);
            else if(Pieces.isKnight(piece) && !isPinned(index)) generateKnightMoves(index, legalMoves);
            else if(Pieces.isPawn(piece)) generatePawnMove(index, legalMoves);
//            System.out.println(index + " -> " + Pieces.getPiece(piece) + " : ");
//            for (int i=prevSize; i<legalMoves.size(); i++) System.out.println(legalMoves.get(i));
        }
        return legalMoves;
    }

    private void generateSlidingMoves(int piece, int startingSquare, ArrayList<Move> legalMoves) {
        if (board.isChecked && isPinned(startingSquare)) return;
        int startIndex = Pieces.isBishop(piece) ? 4 : 0;
        int endIndex = Pieces.isRook(piece) ? 4 : 8;
        boolean notPinnedNotChecked = !board.isChecked && !isPinned(startingSquare);

        for (int d=startIndex; d<endIndex; d++) {
            int offSet = MoveUtil.slidingDirectionOffset[d];
            boolean isBlocked = false;
            for (int i = 1; i <= MoveUtil.preComputedSlidingDistance[startingSquare][d]; i++) {
                int targetSquare = startingSquare + offSet * i;
                if (board.board[targetSquare] != Pieces.None) {
                    isBlocked = true;
                    if (Pieces.isSameColor(piece, board.board[targetSquare])) break;
                }

                if (notPinnedNotChecked || (isPinned(startingSquare) && isPinned(startingSquare + offSet))) {
                    // a sliding pinned piece can only move if,
                    // it is not a check and, it is moving in the direction of the pin
                    legalMoves.add(new Move(startingSquare, targetSquare, Move.NormalMove));
                } else if (board.isChecked && isBlockingCheck(targetSquare)) {
                    legalMoves.add(new Move(startingSquare, targetSquare, Move.NormalMove));
                }
                if (isBlocked) break;
            }
        }
    }

    private void generateKnightMoves(int startingSquare, ArrayList<Move> legalMoves) {
        for (int targetSquare: MoveUtil.preComputedKnightMoves[startingSquare]) {
            if (!board.isChecked || isBlockingCheck(targetSquare)) {
                if (!Pieces.isSameColor(board.board[startingSquare], board.board[targetSquare])) {
                    legalMoves.add(new Move(startingSquare, targetSquare, Move.NormalMove));
                }
            }
        }
    }

    private void generatePawnMove(int startingSquare, ArrayList<Move> legalMoves) {
        int rank = startingSquare / 8;
        int file = startingSquare % 8;
        int delta = 0;
        int rankToCheck = 0;

        if (!isPinned(startingSquare)) {
            // one move ahead
            delta = board.isWhiteToMove() ? 8 : -8;
            if (board.board[startingSquare + delta] == Pieces.None && (!board.isChecked || isBlockingCheck(startingSquare + delta))) {
                if ((board.isWhiteToMove() && rank < 6) || (!board.isWhiteToMove() && rank > 1)) {
                    legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.NormalMove));
                }
                else {
                    // promotions
                    legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Queen));
                    legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Bishop));
                    legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Knight));
                    legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Rook));
                }
            }

            // two move ahead
            delta = board.isWhiteToMove() ? 16 : -16;
            rankToCheck = board.isWhiteToMove() ? 1 : 6;
            if (rank == rankToCheck && board.board[startingSquare + delta / 2] == Pieces.None && board.board[startingSquare + delta] == Pieces.None && (!board.isChecked || isBlockingCheck(startingSquare + delta))) {
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.TwoAhead));
            }
        }

        // capture on left
        delta = board.isWhiteToMove() ? 7 : -9;
        if (file > 0 && board.board[startingSquare + delta] != Pieces.None && !Pieces.isSameColor(board.board[startingSquare], board.board[startingSquare + delta]) && (!isPinned(startingSquare) || isPinned(startingSquare + delta))) {
            if ((board.isWhiteToMove() && rank < 6) || (!board.isWhiteToMove() && rank > 1)) {
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.NormalMove));
            }
            else {
                // capture and promote
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Queen));
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Bishop));
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Knight));
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Rook));
            }
        }
        // en passant on left
        if (board.epSquare != -1 && file > 0 && startingSquare + delta == board.epSquare && epPinnedSquare != startingSquare) {
            legalMoves.add(new Move(startingSquare, board.epSquare, Move.EnPassant));
        }

        // capture on right
        delta = board.isWhiteToMove() ? 9 : -7;
        if (file < 7 && board.board[startingSquare + delta] != Pieces.None && !Pieces.isSameColor(board.board[startingSquare], board.board[startingSquare + delta]) && (!isPinned(startingSquare) || isPinned(startingSquare + delta))) {
            if ((board.isWhiteToMove() && rank < 6) || (!board.isWhiteToMove() && rank > 1)) {
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.NormalMove));
            }
            else {
                // capture and promote
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Queen));
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Bishop));
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Knight));
                legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.Promote_To_Rook));
            }
        }
        // en passant on right
        if (board.epSquare != -1 && file < 7 && startingSquare + delta == board.epSquare && epPinnedSquare != startingSquare) {
            legalMoves.add(new Move(startingSquare, board.epSquare, Move.EnPassant));
        }
    }

    private boolean isNotAttacked(int index) {
        return (attackMask & (1L << index)) == 0;
    }

    private boolean isPinned(int index) {
        return  (pinMask & (1L << index)) != 0;
    }

    private boolean isBlockingCheck(int index) {
        return (checkMask & (1L << index)) != 0;
    }

    private void setAttackMask() {
        checkMask = pinMask = attackMask = 0L;
        currentPlayerKingIndex = board.isWhiteToMove() ? board.kingIndex[0] : board.kingIndex[1];
        board.isChecked = board.isDoubleChecked = false;
        int otherPlayer = board.playerToMove ^ BoardUtil.PlayerMask;
        for (int index=0; index<64; index++) {
            // we can only generate moves for the king
            // if it is a double check so break early
            if (board.isDoubleChecked) break;
            int piece = board.board[index];
            if (piece == Pieces.None || !Pieces.isSameColor(piece, otherPlayer)) continue;

            if (Pieces.isSlidingPiece(piece)) setSlidingAttackMask(piece, index);
            else if (Pieces.isKnight(piece)) {
                for (int targetSquare: MoveUtil.preComputedKnightMoves[index]) {
                    attackMask |= (1L << targetSquare);
                    if (targetSquare == currentPlayerKingIndex) {
                        if (!board.isChecked) board.isChecked = true;
                        else board.isDoubleChecked = true;
                    }
                }
            }
            else if (Pieces.isKing(piece)) {
                for (int targetSquare: MoveUtil.preComputedKingMoves[index]) {
                    attackMask |= (1L << targetSquare);
                }
            }
            else {
                if (board.isWhiteToMove()) {
                    for (int targetSquare: MoveUtil.preComputedBlackPawnAttacks[index]) {
                        attackMask |= (1L << targetSquare);
                    }
                } else {
                    for (int targetSquare: MoveUtil.preComputedWhitePawnAttacks[index]) {
                        attackMask |= (1L << targetSquare);
                    }
                }
            }
        }

        // handle en passant
        epPinnedSquare = -1;
        if (board.epSquare != -1) {
            int epPieceSquare = board.epSquare + (board.isWhiteToMove() ? -8 : 8);
            int currFile = epPieceSquare % 8;
            int epPinnedPawn;

            if (BoardUtil.isSameRank(epPieceSquare, currentPlayerKingIndex)) {
                // en passant by both pawns are possible
                if (currFile > 0 && currFile < 7 && !Pieces.isSameColor(board.board[epPieceSquare - 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare - 1]) && !Pieces.isSameColor(board.board[epPieceSquare + 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare + 1])) {
                    // if one pawn takes the other can still block
                    return;
                }

                if (currentPlayerKingIndex < epPieceSquare) {
                    // king is on the left of en passant pawn
                    boolean anotherPieceOnLeft = false;
                    boolean isRookOrQueenOnRight = false;
                    int leftIndex = -1, rightIndex = 64;

                    if (BoardUtil.isSameRank(epPieceSquare, epPieceSquare - 1) && !Pieces.isSameColor(board.board[epPieceSquare - 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare - 1])) {
                        // a pawn is on the left of the en passant pawn
                        leftIndex = epPieceSquare - 2;
                        rightIndex = epPieceSquare + 1;
                        epPinnedPawn = epPieceSquare - 1;
                    } else if (BoardUtil.isSameRank(epPieceSquare, epPieceSquare + 1) && !Pieces.isSameColor(board.board[epPieceSquare + 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare + 1])) {
                        // a pawn is on the right of the en passant pawn
                        leftIndex = epPieceSquare - 1;
                        rightIndex = epPieceSquare + 2;
                        epPinnedPawn = epPieceSquare + 1;
                    } else {
                        // no pawn on the left or right
                        return;
                    }

                    while (leftIndex > currentPlayerKingIndex) {
                        if (board.board[leftIndex] != Pieces.None) {
                            anotherPieceOnLeft = true;
                            break;
                        }
                        leftIndex--;
                    }

                    if (!anotherPieceOnLeft) {
                        while (BoardUtil.isSameRank(rightIndex, epPieceSquare)) {
                            if (board.board[rightIndex] != Pieces.None) {
                                if (Pieces.isSameColor(board.board[rightIndex], otherPlayer) && (Pieces.isRook(board.board[rightIndex]) || !Pieces.isQueen(board.board[rightIndex]))) {
                                    isRookOrQueenOnRight = true;
                                }
                                break;
                            }
                            rightIndex++;
                        }
                    }

                    if (!anotherPieceOnLeft && isRookOrQueenOnRight) {
                        epPinnedSquare = epPinnedPawn;
                    }
                } else {
                    // king is on the right of en passant pawn
                    boolean anotherPieceOnRight = false;
                    boolean isRookOrQueenOnLeft = false;
                    int leftIndex = -1, rightIndex = 64;

                    if (BoardUtil.isSameRank(epPieceSquare, epPieceSquare - 1) && !Pieces.isSameColor(board.board[epPieceSquare - 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare - 1])) {
                        // a pawn is on the left of the en passant pawn
                        leftIndex = epPieceSquare - 2;
                        rightIndex = epPieceSquare + 1;
                        epPinnedPawn = epPieceSquare - 1;
                    } else if (BoardUtil.isSameRank(epPieceSquare, epPieceSquare + 1) && !Pieces.isSameColor(board.board[epPieceSquare + 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare + 1])) {
                        // a pawn is on the right of the en passant pawn
                        leftIndex = epPieceSquare - 1;
                        rightIndex = epPieceSquare + 2;
                        epPinnedPawn = epPieceSquare + 1;
                    } else {
                        // no pawn on the left or right
                        return;
                    }

                    while (rightIndex < currentPlayerKingIndex) {
                        if (board.board[rightIndex] != Pieces.None) {
                            anotherPieceOnRight = true;
                            break;
                        }
                        rightIndex--;
                    }

                    if (!anotherPieceOnRight) {
                        while (BoardUtil.isSameRank(leftIndex, epPieceSquare)) {
                            if (board.board[leftIndex] != Pieces.None) {
                                if (Pieces.isSameColor(board.board[leftIndex], otherPlayer) && (Pieces.isRook(board.board[leftIndex]) || !Pieces.isQueen(board.board[leftIndex]))) {
                                    isRookOrQueenOnLeft = true;
                                }
                                break;
                            }
                            leftIndex--;
                        }
                    }

                    if (!anotherPieceOnRight && isRookOrQueenOnLeft) {
                        epPinnedSquare = epPinnedPawn;
                    }
                }
            }
        }
    }

    private void setSlidingAttackMask(int piece, int startingSquare) {
        int startIndex = Pieces.isBishop(piece) ? 4 : 0;
        int endIndex = Pieces.isRook(piece) ? 4 : 8;

        for (int d=startIndex; d<endIndex; d++) {
            int offSet = MoveUtil.slidingDirectionOffset[d];
            int pinnedIndex = -1;
            long currCheckMask = 1L << startingSquare;
            long currPinMask = 1L << startingSquare;

            for (int i=1; i<=MoveUtil.preComputedSlidingDistance[startingSquare][d]; i++) {
                int targetSquare = startingSquare + offSet * i;

                // in case of friendly piece at targetSquare, will protect it from being captured by the king
                attackMask |= (1L << targetSquare);
                // friendly piece is blocking the path
                if (board.board[targetSquare] != Pieces.None && Pieces.isSameColor(board.board[targetSquare], piece)) break;

                currPinMask |= (1L << targetSquare);
                // enemy piece encountered which is not a king piece
                if (board.board[targetSquare] != Pieces.None && !Pieces.isSameColor(board.board[targetSquare], piece) && targetSquare != currentPlayerKingIndex) {
                    if (pinnedIndex == -1) {
                        // it is the first enemy piece we encounter
                        pinnedIndex = targetSquare;
                    } else {
                        // more than 1 piece is blocking the path so, can't be pinned
                        break;
                    }
                }

                // if the current piece is pining another piece it can't check directly
                if (pinnedIndex == -1) currCheckMask |= (1L << targetSquare);

                if (targetSquare == currentPlayerKingIndex) {
                    if (pinnedIndex == -1) {
                        // no pinned piece found, so it is a direct check
                        checkMask |= currCheckMask;
                        if (!board.isChecked) board.isChecked = true;
                        else board.isDoubleChecked = true;
                    } else {
                        // update the pinned mask
                        pinMask |= currPinMask;
                    }
                    break;
                }
            }
        }
    }

    public void displayMasks() {
        System.out.println("Attack mask");
        for (int r=7; r>=0; r--) {
            for (int f=0; f<8; f++) {
                boolean isAttacked = (attackMask & (1L << (r*8+f))) != 0;
                System.out.printf("%c (%2d) ", (isAttacked ? 'A' : '-'), r*8+f);
            }
            System.out.println();
        }
        System.out.println();

        System.out.println("Pin mask");
        for (int r=7; r>=0; r--) {
            for (int f=0; f<8; f++) {
                boolean isAttacked = (pinMask & (1L << (r*8+f))) != 0;
                System.out.printf("%c (%2d) ", (isAttacked ? 'A' : '-'), r*8+f);
            }
            System.out.println();
        }
        System.out.println();

        System.out.println("Check in mask");
        for (int r=7; r>=0; r--) {
            for (int f=0; f<8; f++) {
                boolean isAttacked = (checkMask & (1L << (r*8+f))) != 0;
                System.out.printf("%c (%2d) ", (isAttacked ? 'A' : '-'), r*8+f);
            }
            System.out.println();
        }
        System.out.println();
    }
}
