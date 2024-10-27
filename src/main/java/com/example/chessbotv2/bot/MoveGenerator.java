package com.example.chessbotv2.bot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class MoveGenerator {
    Board board;
    long checkMask; // squares which can be blocked to block a check
    long pinMask; // used to store the pieces which cannot move
    long attackMask; // squares on which the king cant move
    public int epPinnedSquare;
    int currentPlayerKingIndex;
    public MoveGenerator(Board board) {
        this.board = board;
    }

    public ArrayList<Move> generateMoves() {
        setAttackMask();
        return getLegalMoves(false);
    }

    public ArrayList<Move> generateMoves(boolean capturesOnly) {
        setAttackMask();
        return getLegalMoves(capturesOnly);
    }

    private ArrayList<Move> getLegalMoves(boolean capturesOnly) {
        ArrayList<Move> legalMoves = new ArrayList<>();
        // draw condition
        if ((board.pieceCount[0] == 1 && board.pieceCount[1] == 1) || board.halfMoveClock >= 50 || board.fullMoveCounter >= 100) return legalMoves;

        // generate king moves first in case of double check to return early
        for (int targetSquare: MoveUtil.preComputedKingMoves[currentPlayerKingIndex]) {
            // square is not attacked by an enemy piece
            if (isNotAttacked(targetSquare) && !Pieces.isSameColor(board.board[currentPlayerKingIndex], board.board[targetSquare])) {
                if (!capturesOnly || !Pieces.isNone(board.board[targetSquare])) {
                    legalMoves.add(new Move(currentPlayerKingIndex, targetSquare, Move.NormalMove));
                }
            }
        }
        if (board.isDoubleChecked) return legalMoves;

        /*
        * Castling moves, no need to check for the position of
        * the rook or king because if they have moved previously
        * right to castle will be lost any way
        * */
        if (!capturesOnly) {
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
                if (!board.isChecked && (board.castleMask & MoveUtil.Black_Queen_Side_Castle_Mask) != 0 && board.board[MoveUtil.Black_King_Start_Square - 1] == Pieces.None && board.board[MoveUtil.Black_King_Start_Square - 2] == Pieces.None && board.board[MoveUtil.Black_King_Start_Square - 3] == Pieces.None && isNotAttacked(MoveUtil.Black_King_Start_Square - 1) && isNotAttacked(MoveUtil.Black_King_Start_Square - 2)) {
                    legalMoves.add(new Move(currentPlayerKingIndex, currentPlayerKingIndex - 2, Move.Castle));
                }
            }
        }

        for (int index=0; index<64; index++) {
            int piece = board.board[index];
            if (piece == Pieces.None || !Pieces.isSameColor(piece, board.playerToMove) || Pieces.isKing(piece)) continue;

            if (Pieces.isSlidingPiece(piece)) generateSlidingMoves(piece, index, legalMoves, capturesOnly);
            else if(Pieces.isKnight(piece) && !isPinned(index)) generateKnightMoves(index, legalMoves, capturesOnly);
            else if(Pieces.isPawn(piece)) generatePawnMove(index, legalMoves, capturesOnly);
        }

        return legalMoves;
    }

    private void generateSlidingMoves(int piece, int startingSquare, ArrayList<Move> legalMoves, boolean capturesOnly) {
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

                // generate captures moves and make an exception for checks
                if (capturesOnly && Pieces.isNone(board.board[targetSquare])) continue;

                if (notPinnedNotChecked || (isPinned(startingSquare) && isMovingInPinDirection(startingSquare, currentPlayerKingIndex, targetSquare))) {
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

    private void generateKnightMoves(int startingSquare, ArrayList<Move> legalMoves, boolean capturesOnly) {
        for (int targetSquare: MoveUtil.preComputedKnightMoves[startingSquare]) {
            if (!board.isChecked || isBlockingCheck(targetSquare)) {
                // generate captures moves and make an exception for checks
                if (capturesOnly && Pieces.isNone(board.board[targetSquare])) continue;

                if (!Pieces.isSameColor(board.board[startingSquare], board.board[targetSquare])) {
                    legalMoves.add(new Move(startingSquare, targetSquare, Move.NormalMove));
                }
            }
        }
    }

    private void generatePawnMove(int startingSquare, ArrayList<Move> legalMoves, boolean capturesOnly) {
        int rank = startingSquare / 8;
        int file = startingSquare % 8;
        int delta = 0;
        int rankToCheck = 0;

        // one move ahead
        delta = board.isWhiteToMove() ? 8 : -8;
        if (!capturesOnly && board.board[startingSquare + delta] == Pieces.None && (!board.isChecked || isBlockingCheck(startingSquare + delta)) && (!isPinned(startingSquare) || isMovingInPinDirection(startingSquare, currentPlayerKingIndex, startingSquare + delta))) {
            if (((board.isWhiteToMove() && rank < 6) || (!board.isWhiteToMove() && rank > 1))) {
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
        if (!capturesOnly && rank == rankToCheck && board.board[startingSquare + delta / 2] == Pieces.None && board.board[startingSquare + delta] == Pieces.None && (!board.isChecked || isBlockingCheck(startingSquare + delta)) && (!isPinned(startingSquare) || isMovingInPinDirection(startingSquare, currentPlayerKingIndex, startingSquare + delta))) {
            legalMoves.add(new Move(startingSquare, startingSquare + delta, Move.TwoAhead));
        }

        // capture on left
        delta = board.isWhiteToMove() ? 7 : -9;
        if (file > 0 && board.board[startingSquare + delta] != Pieces.None && !Pieces.isSameColor(board.board[startingSquare], board.board[startingSquare + delta]) && (!board.isChecked || isBlockingCheck(startingSquare + delta)) && (!isPinned(startingSquare) || isMovingInPinDirection(startingSquare, currentPlayerKingIndex, startingSquare + delta))) {
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
        if (board.epSquare != -1 && file > 0 && startingSquare + delta == board.epSquare && epPinnedSquare != startingSquare && (!board.isChecked || isBlockingCheck(startingSquare + delta + (board.isWhiteToMove() ? -8 : 8))) && (!isPinned(startingSquare) || isMovingInPinDirection(startingSquare, currentPlayerKingIndex, startingSquare + delta))) {
            legalMoves.add(new Move(startingSquare, board.epSquare, Move.EnPassant));
        }

        // capture on right
        delta = board.isWhiteToMove() ? 9 : -7;
        if (file < 7 && board.board[startingSquare + delta] != Pieces.None && !Pieces.isSameColor(board.board[startingSquare], board.board[startingSquare + delta]) && (!board.isChecked || isBlockingCheck(startingSquare + delta)) && (!isPinned(startingSquare) || isMovingInPinDirection(startingSquare, currentPlayerKingIndex, startingSquare + delta))) {
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
        if (board.epSquare != -1 && file < 7 && startingSquare + delta == board.epSquare && epPinnedSquare != startingSquare && (!board.isChecked || isBlockingCheck(startingSquare + delta + (board.isWhiteToMove() ? -8 : 8))) && (!isPinned(startingSquare) || isMovingInPinDirection(startingSquare, currentPlayerKingIndex, startingSquare + delta))) {
            legalMoves.add(new Move(startingSquare, board.epSquare, Move.EnPassant));
        }
    }

    private boolean isNotAttacked(int index) {
        return (attackMask & (1L << index)) == 0;
    }

    public boolean isPinned(int index) {
        return  (pinMask & (1L << index)) != 0;
    }

    public boolean isMovingInPinDirection(int startingSquare, int kingIndex, int targetSquare) {
        // Calculate rows and columns of each square
        int kingRow = kingIndex / 8, kingCol = kingIndex % 8;
        int startRow = startingSquare / 8, startCol = startingSquare % 8;
        int targetRow = targetSquare / 8, targetCol = targetSquare % 8;

        // Calculate the vector from the king to the starting square
        int kingToPieceRow = startRow - kingRow;
        int kingToPieceCol = startCol - kingCol;

        // Calculate the vector from the start square to the target square
        int moveRow = targetRow - startRow;
        int moveCol = targetCol - startCol;

        // Check for same or opposite directions in horizontal, vertical, and diagonal lines
        // Horizontal movement (same row)
        if (kingToPieceRow == 0 && moveRow == 0) return true;

        // Vertical movement (same column)
        if (kingToPieceCol == 0 && moveCol == 0) return true;

        // Diagonal movement
        if (Math.abs(kingToPieceRow) == Math.abs(kingToPieceCol) && Math.abs(moveRow) == Math.abs(moveCol)) {
            return (kingToPieceRow * moveRow > 0 && kingToPieceCol * moveCol > 0)
                    || (kingToPieceRow * moveRow < 0 && kingToPieceCol * moveCol < 0);
        }

        // Not in any valid direction
        return false;
    }

    private boolean isBlockingCheck(int index) {
        return (checkMask & (1L << index)) != 0;
    }

    public HashSet<Integer> getPossibleCheckSquares(int startingSquare, int king) {
        HashSet<Integer> indexes = new HashSet<>();
        for (int d = 0; d < 8; d++) {
            int offSet = MoveUtil.slidingDirectionOffset[d];

            for (int i = 1; i <= MoveUtil.preComputedSlidingDistance[startingSquare][d]; i++) {
                int targetSquare = startingSquare + offSet * i;
                if (Pieces.isNone(board.board[targetSquare])) continue;
                int piece = board.board[targetSquare];
                if (Pieces.isSameColor(king, piece)) {
                    indexes.add(targetSquare);
                    break;
                }
                else if (!Pieces.isSameColor(king, piece) && (Pieces.isQueen(piece)) || (Pieces.isRook(piece) && d > 3) || (Pieces.isBishop(piece) && d < 4) || (i == 1 && d < 4 && Pieces.isPawn(piece))) {
                    indexes.add(targetSquare);
                    break;
                }
            }
        }

        for (int targetSquare: MoveUtil.preComputedKnightMoves[startingSquare]) {
            if (!Pieces.isSameColor(king, board.board[targetSquare]) && Pieces.isKnight(board.board[targetSquare])) {
                indexes.add(targetSquare);
            }
        }
        return indexes;
    }

    private void setAttackMask() {
        checkMask = pinMask = attackMask = 0L;
        currentPlayerKingIndex = board.isWhiteToMove() ? board.kingIndex[0] : board.kingIndex[1];
        board.isChecked = board.isDoubleChecked = false;
        int otherPlayer = board.playerToMove ^ BoardUtil.PlayerMask;

        for (int index=0; index<64; index++) {
            int piece = board.board[index];
            if (piece == Pieces.None || !Pieces.isSameColor(piece, otherPlayer)) continue;

            if (Pieces.isSlidingPiece(piece)) setSlidingAttackMask(piece, index);
            else if (Pieces.isKnight(piece)) {
                for (int targetSquare: MoveUtil.preComputedKnightMoves[index]) {
                    attackMask |= (1L << targetSquare);
                    if (targetSquare == currentPlayerKingIndex) {
                        checkMask |= (1L << index);
                        checkMask |= (1L << targetSquare);
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
                        if (targetSquare == currentPlayerKingIndex) {
                            checkMask |= (1L << index);
                            checkMask |= (1L << targetSquare);
                            if (!board.isChecked) board.isChecked = true;
                            else board.isDoubleChecked = true;
                        }
                    }
                } else {
                    for (int targetSquare: MoveUtil.preComputedWhitePawnAttacks[index]) {
                        attackMask |= (1L << targetSquare);
                        if (targetSquare == currentPlayerKingIndex) {
                            checkMask |= (1L << index);
                            checkMask |= (1L << targetSquare);
                            if (!board.isChecked) board.isChecked = true;
                            else board.isDoubleChecked = true;
                        }
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
                if (currentPlayerKingIndex < epPieceSquare) {
                    // king is on the left of en passant pawn
                    boolean anotherPieceOnLeft = false;
                    boolean isRookOrQueenOnRight = false;
                    int leftIndex = -1, rightIndex = 64;

                    if (currFile > 1 && !Pieces.isSameColor(board.board[epPieceSquare - 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare - 1])) {
                        // a pawn is on the left of the en passant pawn
                        leftIndex = epPieceSquare - 2;
                        rightIndex = epPieceSquare + 1;
                        epPinnedPawn = epPieceSquare - 1;
                    } else if (currFile < 6 && !Pieces.isSameColor(board.board[epPieceSquare + 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare + 1])) {
                        // a pawn is on the right of the en passant pawn
                        leftIndex = epPieceSquare - 1;
                        rightIndex = epPieceSquare + 2;
                        epPinnedPawn = epPieceSquare + 1;
                    } else {
                        // no pawn on the left or right
                        return;
                    }

                    while (leftIndex > currentPlayerKingIndex) {
                        if (!Pieces.isNone(board.board[leftIndex])) {
                            anotherPieceOnLeft = true;
                            break;
                        }
                        leftIndex--;
                    }

                    if (!anotherPieceOnLeft) {
                        while (BoardUtil.isSameRank(rightIndex, epPieceSquare)) {
                            if (!Pieces.isNone(board.board[rightIndex])) {
                                if (Pieces.isSameColor(board.board[rightIndex], otherPlayer) && (Pieces.isRook(board.board[rightIndex]) || Pieces.isQueen(board.board[rightIndex]))) {
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

                    if (currFile > 1 && !Pieces.isSameColor(board.board[epPieceSquare - 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare - 1])) {
                        // a pawn is on the left of the en passant pawn
                        leftIndex = epPieceSquare - 2;
                        rightIndex = epPieceSquare + 1;
                        epPinnedPawn = epPieceSquare - 1;
                    } else if (currFile < 6 && !Pieces.isSameColor(board.board[epPieceSquare + 1], board.board[epPieceSquare]) && Pieces.isPawn(board.board[epPieceSquare + 1])) {
                        // a pawn is on the right of the en passant pawn
                        leftIndex = epPieceSquare - 1;
                        rightIndex = epPieceSquare + 2;
                        epPinnedPawn = epPieceSquare + 1;
                    } else {
                        // no pawn on the left or right
                        return;
                    }

                    while (rightIndex < currentPlayerKingIndex) {
                        if (!Pieces.isNone(board.board[rightIndex])) {
                            anotherPieceOnRight = true;
                            break;
                        }
                        rightIndex++;
                    }

                    if (!anotherPieceOnRight) {
                        while (BoardUtil.isSameRank(leftIndex, epPieceSquare)) {
                            if (!Pieces.isNone(board.board[leftIndex])) {
                                if (Pieces.isSameColor(board.board[leftIndex], otherPlayer) && (Pieces.isRook(board.board[leftIndex]) || Pieces.isQueen(board.board[leftIndex]))) {
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
            boolean isPieceCheckingKing = false;

            for (int i=1; i<=MoveUtil.preComputedSlidingDistance[startingSquare][d]; i++) {
                int targetSquare = startingSquare + offSet * i;

                // for squares beyond king where king cannot move.
                // will break the loop in case a pin exists so no need to check for pin
                if (isPieceCheckingKing) {
                    attackMask |= (1L << targetSquare);
                    // if any piece is present king cannot capture because of attack mask
                    // and cannot move in case of friendly piece or empty square
                    break;
                }

                // in case of friendly piece at targetSquare, will protect it from being captured by the king
                // stop calculating the attack mask if enemy piece is pinned
                if (pinnedIndex == -1) attackMask |= (1L << targetSquare);

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
                // so not point updating checkMask
                if (pinnedIndex == -1) currCheckMask |= (1L << targetSquare);

                if (targetSquare == currentPlayerKingIndex) {
                    if (pinnedIndex == -1) {
                        // no pinned piece found, so it is a direct check
                        checkMask |= currCheckMask;
                        isPieceCheckingKing = true;
                        if (!board.isChecked) board.isChecked = true;
                        else board.isDoubleChecked = true;
                    } else {
                        // update the pinned mask
                        pinMask |= currPinMask;
                        break;
                    }
                }
            }
        }
    }

    public void setAndDisplayAttackMask() {
        setAttackMask();
        displayMasks();
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
