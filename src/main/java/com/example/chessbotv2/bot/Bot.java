package com.example.chessbotv2.bot;

import java.util.ArrayList;
import java.util.Stack;

public class Bot {
    public Board board;
    MoveGenerator moveGenerator;
    Stack<BoardData> prevBoardState;
    int captures, castles, enPassants;

    public Bot() {
        board = new Board();
        moveGenerator = new MoveGenerator(board);
        prevBoardState = new Stack<>();
//        captures = checks = 0;
//        moveGenerator.generateMoves();
//        System.out.println("Number of moves for depth " + 3 + " are : " + getMoveCount(3));
    }

    public Bot(String fen) {
        board = new Board(fen);
        moveGenerator = new MoveGenerator(board);
        prevBoardState = new Stack<>();
    }

    public void resetBoard() {
        prevBoardState.clear();
        board.resetBoard();
    }

    public Move playMove() {
        // generate legal moves
        // find best move
        // make the move
        // return the move
        return null;
    }

    public void testMoves(int depth, boolean rangeTest) {
        for (int i=(rangeTest ? 1 : depth); i<=depth; i++) {
            castles = captures = enPassants = 0;
            System.out.println("Number of moves for depth " + i + " are : " + getMoveCount(i) + " [Castles: " + castles + "], [Captures: " + captures + "], [EP: " + enPassants + "]");
        }
    }

    public int getMoveCount(int depth) {
        if (depth == 0) return 1;
        ArrayList<Move> legalMoves = moveGenerator.generateMoves();
        int cnt = 0;
        boolean displayStats = false;
        for (Move move: legalMoves) {
            displayStats = move.isCastle;
            char currPiece = Pieces.getPiece(board.board[move.startingSquare]);
            makeMove(move, true);
            if (displayStats) {
                System.out.println("Piece: " + currPiece);
                System.out.println("After making move " + (3-depth) + ": " + move);
                board.displayBoard();
            }
            cnt += getMoveCount(depth - 1);
            unMakeMove(move);
            if (displayStats) {
                System.out.println("After resting move: ");
                board.displayBoard();
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
            targetPiece = board.board[board.epSquare];
            board.board[board.epSquare] = Pieces.None;
            enPassants++;
        }

        // testing
        if ((targetPiece & 7) != 0) captures++;

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
            castles++;
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
            board.board[move.targetSquare] = colorMask | pieceMask;
        }

        // flip the turn
        board.playerToMove ^= BoardUtil.PlayerMask;
    }

    void unMakeMove(Move move) {
        BoardData boardData = prevBoardState.pop();
        if (boardData.targetPiece != Pieces.None) {
//            captures++;
            if (move.isEnPassant) {
                // en passant capture
                board.board[boardData.epSquare] = boardData.targetPiece;
            }
            else {
                // normal capture, promotion capture
                board.board[move.targetSquare] = boardData.targetPiece;
            }
        } else {
            board.board[move.targetSquare] = Pieces.None;
        }

//        if (move.targetSquare == 24 && Pieces.isQueen(boardData.startingPiece) && board.board[51] == Pieces.None) checks++;
//        if (move.targetSquare == 33 && Pieces.isBishop(boardData.startingPiece) && board.board[51] == Pieces.None) checks++;
//        if (move.targetSquare == 39 && Pieces.isQueen(boardData.startingPiece) && board.board[53] == Pieces.None) checks++;

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
