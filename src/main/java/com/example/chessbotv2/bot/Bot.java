package com.example.chessbotv2.bot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class Bot {
    public Board board;
    MoveGenerator moveGenerator;
    Evaluator evaluator;
    Stack<BoardData> prevBoardState;
    int captures, castles, enPassants, promotions;
    boolean changeStats;
    Move bestMove;
    public Bot() {
        board = new Board();
        init(board);
    }

    public Bot(String fen) {
        board = new Board(fen);
        init(board);
    }

    void init(Board board) {
        evaluator = new Evaluator(board);
        moveGenerator = new MoveGenerator(board);
        prevBoardState = new Stack<>();
    }

    public void resetBoard() {
        prevBoardState.clear();
        board.resetBoard();
    }

    public Move playMove() {
        // find best move
        findBestMove();
        // make the move
        if (bestMove != null && bestMove.startingSquare != -1 && bestMove.startingSquare != 64) {
            makeMove(bestMove, false);
        }
        // return the move
        return bestMove;
    }

    public void findBestMove() {
        bestMove = null;
        int score = Search(4, true);
        if (bestMove == null) {
            if (score == 0) {
                // stalemate
                bestMove = new Move(-1, -1, Move.NormalMove);
            }
            else {
                // checkmate
                bestMove = new Move(64, 64, Move.NormalMove);
            }
        }
    }

    public int Search(int depth, boolean isRoot) {
        if (depth == 0) {
            return evaluator.evaluate();
        }

        int currentBestScore = Integer.MIN_VALUE;
        ArrayList<Move> legalMoves = moveGenerator.generateMoves();
        arrangeMoves(legalMoves);

        if (legalMoves.isEmpty()) {
            return board.isChecked ? Integer.MIN_VALUE : 0;
        }
        for (Move move: legalMoves) {
            makeMove(move, true);
            int currentScore = -Search(depth - 1, false);
            unMakeMove(move);
            if (currentBestScore < currentScore) {
                currentBestScore = currentScore;
                if (isRoot) bestMove = move;
            }
        }
        return currentBestScore;
    }

    void arrangeMoves(ArrayList<Move> legalMoves) {
        Collections.sort(legalMoves, (a, b) -> {
            int moveScoreA = 0, moveScoreB = 0;
            if (Pieces.isNone(board.board[a.targetSquare])){
                if (a.isEnPassant) moveScoreA += 50;
            }
            else {
                moveScoreA += evaluator.getCaptureValue(board.board[a.startingSquare], board.board[a.targetSquare]);
            }

            if (Pieces.isNone(board.board[b.targetSquare])){
                if (b.isEnPassant) moveScoreB += 50;
            }
            else {
                moveScoreB += evaluator.getCaptureValue(board.board[b.startingSquare], board.board[b.targetSquare]);
            }

            if (a.isPromoteToQueen) moveScoreA += 900;
            else if (a.isPromoteToRook) moveScoreA += 500;
            else if (a.isPromoteToBishop || a.isPromoteToKnight) moveScoreA += 300;

            if (b.isPromoteToQueen) moveScoreB += 900;
            else if (b.isPromoteToRook) moveScoreB += 500;
            else if (b.isPromoteToBishop || b.isPromoteToKnight) moveScoreB += 300;
            return Integer.compare(moveScoreA, moveScoreB);
        });
    }

    public void testMoves(int depth, boolean rangeTest, boolean displayStats, boolean displayDetails) {
        for (int i=(rangeTest ? 1 : depth); i<=depth; i++) {
            castles = captures = enPassants = promotions = 0;
            changeStats = false;
            System.out.println("Number of moves for depth " + i + " are : " + getMoveCount(i, displayStats, displayDetails) + " [Castles: " + castles + "], [Captures: " + captures + "], [EP: " + enPassants + "], [Promotions: " + promotions + "]");
        }
    }

    public void testAttackMask() {
        moveGenerator.setAndDisplayAttackMask();
    }

    public void testPinned(int square) {
        System.out.println("Is " + BoardUtil.getSquareName(square) + " pinned? " + moveGenerator.isPinned(square));
    }

    public void testMovingInPinDirection(int startSquare, int kingSquare, int targetSquare) {
        System.out.println("Is moving in pin direction from " + BoardUtil.getSquareName(startSquare) + " when king at " + BoardUtil.getSquareName(kingSquare) + " to " + BoardUtil.getSquareName(targetSquare) + "? " + moveGenerator.isMovingInPinDirection(startSquare, kingSquare, targetSquare));
    }

    public int getMoveCount(int depth, boolean displayStats, boolean displayDetails) {
        if (depth == 0) return 1;
        ArrayList<Move> legalMoves = moveGenerator.generateMoves();
        int cnt = 0;
        for (Move move: legalMoves) {
            char currPiece = Pieces.getPiece(board.board[move.startingSquare]);
            if (displayStats) {
                if (displayDetails) {
                    System.out.println("Piece: " + currPiece + ", EP pinned square: " + BoardUtil.getSquareName(moveGenerator.epPinnedSquare));
                    System.out.println("Before making move : " + move);
                    board.displayBoard();
                }
                changeStats = depth == 1;
            }

            makeMove(move, true);
            if (displayStats && displayDetails) {
                System.out.println("Make move " + (3-depth) + ": " + move);
                board.displayBoard();
            }
            cnt += getMoveCount(depth - 1, displayStats, displayDetails);
            unMakeMove(move);

            if (displayStats && displayDetails) {
                System.out.println("Un-make move: ");
                board.displayBoard();
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            }
        }
        return cnt;
    }

    public void makeMove(Move move, boolean storeState) {
        int targetPiece = board.board[move.targetSquare];
        int startingPiece = board.board[move.startingSquare];

        // move the piece and handle captures
        board.board[move.targetSquare] = startingPiece;
        board.board[move.startingSquare] = Pieces.None;

        // handle en passant captures
        if (move.isEnPassant) {
            targetPiece = board.board[board.epSquare + (board.isWhiteToMove() ? -8 : 8)];
            board.board[board.epSquare + (board.isWhiteToMove() ? -8 : 8)] = Pieces.None;
            if (changeStats) enPassants++;
        }

        // testing
        if (changeStats && (targetPiece & 7) != 0) captures++;

        if (storeState) {
            // store data to unmake move
            prevBoardState.push(new BoardData(board.kingIndex.clone(), board.castleMask, board.epSquare, board.halfMoveClock, board.fullMoveCounter, board.isChecked, board.isDoubleChecked, startingPiece, targetPiece));
        }

        // handle two square ahead
        if (move.isTwoAhead) {
            board.epSquare = move.targetSquare + (board.isWhiteToMove() ? -8 : 8);
        } else {
            board.epSquare = -1;
        }

        // handle castling moves
        if (move.isCastle) {
            if (changeStats) castles++;
            if (board.isWhiteToMove()) {
                if (move.targetSquare == MoveUtil.White_King_Start_Square + 2) {
                    // move the rook to the left of king
                    board.board[MoveUtil.White_King_Start_Square + 3] = Pieces.None;
                    board.board[MoveUtil.White_King_Start_Square + 1] = Pieces.White | Pieces.Rook;
                } else {
                    // move the rook to the right of king
                    board.board[MoveUtil.White_King_Start_Square - 4] = Pieces.None;
                    board.board[MoveUtil.White_King_Start_Square - 1] = Pieces.White | Pieces.Rook;
                }
                // remove the castling rights
                board.castleMask &= MoveUtil.Black_Castle_Mask;
            } else {
                if (move.targetSquare == MoveUtil.Black_King_Start_Square + 2) {
                    // move the rook to the left of king
                    board.board[MoveUtil.Black_King_Start_Square + 3] = Pieces.None;
                    board.board[MoveUtil.Black_King_Start_Square + 1] = Pieces.Black | Pieces.Rook;
                } else {
                    // move the rook to the right of king
                    board.board[MoveUtil.Black_King_Start_Square - 4] = Pieces.None;
                    board.board[MoveUtil.Black_King_Start_Square - 1] = Pieces.Black | Pieces.Rook;
                }
                // remove the castling rights
                board.castleMask &= MoveUtil.White_Castle_Mask;
            }
        } else {
            // handle castling rights
            if (board.isWhiteToMove() && (board.castleMask & MoveUtil.White_Castle_Mask) != 0) {
                if (Pieces.isKing(startingPiece)) {
                    // white king moved
                    board.castleMask &= MoveUtil.Black_Castle_Mask;
                }
                else if (Pieces.isRook(startingPiece)) {
                    if (move.startingSquare == MoveUtil.White_King_Side_Rook_Square) {
                        // white king side rook moved
                        board.castleMask = board.castleMask & (MoveUtil.Complete_Castle_Mask ^ MoveUtil.White_King_Side_Castle_Mask);
                    }
                    else if (move.startingSquare == MoveUtil.White_Queen_Side_Rook_Square) {
                        // white queen side rook moved
                        board.castleMask = board.castleMask & (MoveUtil.Complete_Castle_Mask ^ MoveUtil.White_Queen_Side_Castle_Mask);
                    }
                }
            } else if (!board.isWhiteToMove() && (board.castleMask & MoveUtil.Black_Castle_Mask) != 0) {
                if (Pieces.isKing(startingPiece)) {
                    // black king moved
                    board.castleMask &= MoveUtil.White_Castle_Mask;
                }
                else if (Pieces.isRook(startingPiece)) {
                    if (move.startingSquare == MoveUtil.Black_King_Side_Rook_Square) {
                        // black king side rook moved
                        board.castleMask = board.castleMask & (MoveUtil.Complete_Castle_Mask ^ MoveUtil.Black_King_Side_Castle_Mask);
                    }
                    else if (move.startingSquare == MoveUtil.Black_Queen_Side_Rook_Square) {
                        // black queen side rook moved
                        board.castleMask = board.castleMask & (MoveUtil.Complete_Castle_Mask ^ MoveUtil.Black_Queen_Side_Castle_Mask);
                    }
                }
            }

            // check if rook was captured
            if (Pieces.isRook(targetPiece)) {
                if (board.isWhiteToMove()) {
                    if (move.targetSquare == MoveUtil.Black_King_Side_Rook_Square) {
                        // king side black rook was captured
                        board.castleMask = board.castleMask & (MoveUtil.Complete_Castle_Mask ^ MoveUtil.Black_King_Side_Castle_Mask);
                    }
                    else if (move.targetSquare == MoveUtil.Black_Queen_Side_Rook_Square) {
                        // queen side black rook was captured
                        board.castleMask = board.castleMask & (MoveUtil.Complete_Castle_Mask ^ MoveUtil.Black_Queen_Side_Castle_Mask);
                    }
                }
                else {
                    if (move.targetSquare == MoveUtil.White_King_Side_Rook_Square) {
                        // king side white rook was captured
                        board.castleMask = board.castleMask & (MoveUtil.Complete_Castle_Mask ^ MoveUtil.White_King_Side_Castle_Mask);
                    }
                    else if (move.targetSquare == MoveUtil.White_Queen_Side_Rook_Square) {
                        // queen side white rook was captured
                        board.castleMask = board.castleMask & (MoveUtil.Complete_Castle_Mask ^ MoveUtil.White_Queen_Side_Castle_Mask);
                    }
                }
            }
        }

        // update the kingIndex
        if (Pieces.isKing(startingPiece)) {
            if (board.isWhiteToMove()) {
                board.kingIndex[0] = move.targetSquare;
            }
            else {
                board.kingIndex[1] = move.targetSquare;
            }
        }

        // handle promotions
        int colorMask = board.isWhiteToMove() ? Pieces.White : Pieces.Black;
        int pieceMask = Pieces.None;
        if (move.isPromoteToKnight) {
            pieceMask = Pieces.Knight;
        }
        else if (move.isPromoteToBishop) {
            pieceMask = Pieces.Bishop;
        }
        else if (move.isPromoteToQueen) {
            pieceMask = Pieces.Queen;
        }
        else if (move.isPromoteToRook) {
            pieceMask = Pieces.Rook;
        }
        // update the piece
        if (pieceMask != Pieces.None) {
            if (changeStats) promotions++;
            board.board[move.targetSquare] = colorMask | pieceMask;
        }

        // flip the turn
        board.playerToMove ^= BoardUtil.PlayerMask;
    }

    void unMakeMove(Move move) {
        BoardData boardData = prevBoardState.pop();
        if (boardData.targetPiece != Pieces.None) {
            if (move.isEnPassant) {
                // en passant capture
                board.board[boardData.epSquare + (!board.isWhiteToMove() ? -8 : 8)] = boardData.targetPiece;
                board.board[move.targetSquare] = Pieces.None;
            }
            else {
                // normal capture, promotion capture
                board.board[move.targetSquare] = boardData.targetPiece;
            }
        } else {
            board.board[move.targetSquare] = Pieces.None;
        }

        // place the piece on its original square
        board.board[move.startingSquare] = boardData.startingPiece;

        // handle castling
        if (move.isCastle) {
            if (board.isWhiteToMove()) {
                // this means black had castled in the previous move
                if (move.targetSquare == MoveUtil.Black_King_Start_Square + 2) {
                    // move the rook to the left of king
                    board.board[MoveUtil.Black_King_Start_Square + 3] = Pieces.Black | Pieces.Rook;
                    board.board[MoveUtil.Black_King_Start_Square + 1] = Pieces.None;
                } else {
                    // move the rook to the right of king
                    board.board[MoveUtil.Black_King_Start_Square - 4] = Pieces.Black | Pieces.Rook;
                    board.board[MoveUtil.Black_King_Start_Square - 1] = Pieces.None;
                }
            }
            else {
                // white had castled in the previous move
                if (move.targetSquare == MoveUtil.White_King_Start_Square + 2) {
                    // move the rook to the left of king
                    board.board[MoveUtil.White_King_Start_Square + 3] = Pieces.White | Pieces.Rook;
                    board.board[MoveUtil.White_King_Start_Square + 1] = Pieces.None;
                } else {
                    // move the rook to the right of king
                    board.board[MoveUtil.White_King_Start_Square - 4] = Pieces.White | Pieces.Rook;
                    board.board[MoveUtil.White_King_Start_Square - 1] = Pieces.None;
                }
            }
        }

        // reset the properties of the board
        boardData.resetBoardData(board);
        // flip the turn
        board.playerToMove ^= BoardUtil.PlayerMask;
    }
}
