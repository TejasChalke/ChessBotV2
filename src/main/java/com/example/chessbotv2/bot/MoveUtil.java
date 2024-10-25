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
}
