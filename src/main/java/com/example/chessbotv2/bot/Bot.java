package com.example.chessbotv2.bot;

import com.example.chessbotv2.bot.book.Book;

import java.util.*;

public class Bot {
    final int MIN_VAL = -999999;
    final int MAX_VAL = 999999;
    final int CHECK_MATE = 100000;
    public Board board;
    MoveGenerator moveGenerator;
    Evaluator evaluator;
    Stack<BoardData> prevBoardState;
    int captures, castles, enPassants, promotions;
    boolean changeStats;
    Move bestMove;
    HashMap<String, Integer> moveCnt;
    Book book;

    public Bot() {
        board = new Board();
        book = new Book();
        init(board);
    }

    public Bot(String fen) {
        board = new Board(fen);
        book = null;
        init(board);
    }

    void init(Board board) {
        evaluator = new Evaluator(board);
        moveGenerator = new MoveGenerator(board);
        prevBoardState = new Stack<>();
        moveCnt = new HashMap<>();
    }

    public void resetBoard() {
        prevBoardState.clear();
        board.resetBoard();
        if (book == null) {
            book = new Book();
        }
        book.resetIterator();
    }

    public Move playMove() {
        bestMove = null;
        String moveType = "book";
        // find best move
        System.out.println("Looking for move....");
        if (book != null && book.isBookMoveAvailable()) {
            bestMove = book.getBookMove(board);
            if (bestMove == null || bestMove.startingSquare == -1) {
                book.invalidate();
                System.err.println("Error getting valid book move");
                findBestMove();
                moveType = "search";
            }
        } else {
            findBestMove();
            moveType = "search";
        }
        System.out.println("Search ended: " + bestMove + " (" + moveType + ")");
        // make the move
        if (bestMove != null && bestMove.startingSquare != -1 && bestMove.startingSquare != 64) {
            makeMove(bestMove, false);
        }
        // return the move
        return bestMove;
    }

    public void findBestMove() {
        int score = search(4, 0, MIN_VAL, MAX_VAL);
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

    public int search(int depth, int movesFromRoot, int alpha, int beta) {
        if (depth == 0) {
            return searchQuietPosition(alpha, beta);
//            return evaluator.evaluate();
        }

        if (movesFromRoot > 0) {
            alpha = Math.max(alpha, -CHECK_MATE + movesFromRoot);
            beta = Math.min(beta, CHECK_MATE - movesFromRoot);
            if (alpha >= beta) {
                return alpha;
            }
        }

        String currentPos = Arrays.toString(board.board);
        int currentPosCount = moveCnt.getOrDefault(currentPos, 0);
        ArrayList<Move> legalMoves = currentPosCount == 3 ? new ArrayList<>() : moveGenerator.generateMoves();

        if (legalMoves.isEmpty()) {
            // TODO: add proper checks for draw, handled in move generator
            return board.isChecked ? -(CHECK_MATE - movesFromRoot) : 0;
        }

        HashSet<Integer> kingCheckIndexes = moveGenerator.getPossibleCheckSquares(board.kingIndex[board.isWhiteToMove() ? 1 : 0], (board.isWhiteToMove() ? Pieces.Black : Pieces.White) | Pieces.King);
        arrangeMoves(legalMoves, kingCheckIndexes);
        for (Move move: legalMoves) {
//            StringBuilder sb = new StringBuilder();
//            if (movesFromRoot == 0) {
//                sb.append("Move played : ").append(move);
//            }
            makeMove(move, true);
            int eval = -search(depth - 1, movesFromRoot + 1, -beta, -alpha);
            unMakeMove(move);
//            if (movesFromRoot == 0) {
//                sb.append(" with eval ").append(eval);
//                System.out.println(sb.toString());
//            }

            if (eval >= beta) {
                return beta;
            }
            if (eval > alpha) {
                alpha = eval;
                if (movesFromRoot == 0) bestMove = move;
            }
        }

        return alpha;
    }

    public int searchQuietPosition(int alpha, int beta) {
        int eval = evaluator.evaluate();
        if (eval >= beta) {
            return beta;
        }
        alpha = Math.max(alpha, eval);

        ArrayList<Move> legalMoves = moveGenerator.generateMoves(true);
        HashSet<Integer> kingCheckIndexes = moveGenerator.getPossibleCheckSquares(board.kingIndex[board.isWhiteToMove() ? 1 : 0], (board.isWhiteToMove() ? Pieces.Black : Pieces.White) | Pieces.King);
        arrangeMoves(legalMoves, kingCheckIndexes);

        for (Move move: legalMoves) {
            makeMove(move, true);
            eval = -searchQuietPosition(-beta, -alpha);
            unMakeMove(move);

            if (eval >= beta) {
                return beta;
            }
            alpha = Math.max(eval, alpha);
        }
        return alpha;
    }

    void arrangeMoves(ArrayList<Move> legalMoves, HashSet<Integer> kingCheckIndexes) {
        Collections.sort(legalMoves, (a, b) -> {
            int moveScoreA = 0, moveScoreB = 0;
            if (Pieces.isNone(board.board[a.targetSquare])){
                if (a.isEnPassant) moveScoreA += 100;
            }
            else {
                moveScoreA += evaluator.getCaptureValue(board.board[a.startingSquare], board.board[a.targetSquare]);
            }

            if (Pieces.isNone(board.board[b.targetSquare])){
                if (b.isEnPassant) moveScoreB += 100;
            }
            else {
                moveScoreB += evaluator.getCaptureValue(board.board[b.startingSquare], board.board[b.targetSquare]);
            }

            if (a.isPromoteToQueen) moveScoreA += 900;
            else if (a.isPromoteToRook) moveScoreA += 500;
            else if (a.isPromoteToBishop || a.isPromoteToKnight) moveScoreA += 300;
            else if (a.isCastle) moveScoreA += 500;

            if (b.isPromoteToQueen) moveScoreB += 900;
            else if (b.isPromoteToRook) moveScoreB += 500;
            else if (b.isPromoteToBishop || b.isPromoteToKnight) moveScoreB += 300;
            else if (b.isCastle) moveScoreB += 500;

            // move places piece next to pawn
            moveScoreA -= evaluator.getPawnAttackedPenalty(board.board[a.startingSquare], a);
            moveScoreB -= evaluator.getPawnAttackedPenalty(board.board[b.startingSquare], b);

            // if will check
            if (kingCheckIndexes.contains(a.targetSquare)) moveScoreA += 900;
            if (kingCheckIndexes.contains(b.targetSquare)) moveScoreB += 900;

            return Integer.compare(moveScoreB, moveScoreA);
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

        // used for draw condition when only kings are left
        if (!Pieces.isNone(targetPiece)) {
            board.pieceCount[board.isWhiteToMove() ? 1 : 0]--;
        }

        if (Pieces.isPawn(startingPiece) || move.isEnPassant || !Pieces.isNone(targetPiece)) {
            board.halfMoveClock = 0;
        }
        else {
            board.halfMoveClock++;
        }
        if (!board.isWhiteToMove()) {
            board.fullMoveCounter++;
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

        String currentPos = Arrays.toString(board.board);
        int currentPosCount = moveCnt.getOrDefault(currentPos, 0);
        moveCnt.put(currentPos, currentPosCount + 1);
    }

    void unMakeMove(Move move) {
        String currentPos = Arrays.toString(board.board);
        int currentPosCount = moveCnt.getOrDefault(currentPos, 0);
        moveCnt.put(currentPos, currentPosCount - 1);

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

            board.pieceCount[board.isWhiteToMove() ? 0 : 1]++;
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
