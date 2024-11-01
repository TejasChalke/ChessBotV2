package com.example.chessbotv2.bot.book;

import com.example.chessbotv2.bot.Board;
import com.example.chessbotv2.bot.Move;
import com.example.chessbotv2.bot.MoveUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Book {
    public static class MoveIterator {
        HashMap<String, MoveIterator> nextMove;
        MoveIterator() {
            nextMove = new HashMap<>();
        }
    }

    MoveIterator root, itr;
    int maxBookDepth;

    public Book() {
        root = new MoveIterator();
        maxBookDepth = 26;
        initBook();
    }

    public Book(int maxBookDepth) {
        root = new MoveIterator();
        this.maxBookDepth = maxBookDepth;
        initBook();
    }

    public void initBook() {
        String fileName = "/Games.txt";
        int linesAdded = 0;

        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + fileName);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String game;
                // Read the games
                while ((game = reader.readLine()) != null) {
                    MoveIterator itr = root;
                    String[] moves = game.split(" ");
                    int currentDepth = 0;
                    for (String move: moves) {
                        if (move.equals("1-0") || move.equals("0-1") || move.equals("1/2-1/2")) break;
                        if (currentDepth++ == maxBookDepth) break;
                        if (!itr.nextMove.containsKey(move)) {
                            // add a new move
                            itr.nextMove.put(move, new MoveIterator());
                            linesAdded++;
                        }
                        itr = itr.nextMove.get(move);
                    }
                }
                itr = root;
                System.out.println(linesAdded + " lines are available...");
            }
        } catch (IOException e) {
            System.err.println("Error initializing book: " + e.getMessage());
        }
    }

    public boolean isBookMoveAvailable() {
        return itr != null && !itr.nextMove.isEmpty();
    }

    public Move getBookMove(Board board) {
        int size = itr.nextMove.size();
        if (size == 0) {
            return null; // return null or throw an exception if the map is empty
        }

        // Create a random index within the range of the map's size
        int randomIndex = new Random().nextInt(size);

        // Use iterator to reach the random index
        Iterator<HashMap.Entry<String, MoveIterator>> iterator = itr.nextMove.entrySet().iterator();
        for (int i = 0; i < randomIndex; i++) {
            iterator.next();
        }

        var moveEntry = iterator.next();
        itr = moveEntry.getValue();
        System.out.println("Book move tried: " + moveEntry.getKey());
        return MoveUtil.getMoveFromNotation(board, moveEntry.getKey());
    }

    public void invalidate() {
        itr = null;
    }

    public void resetIterator() {
        itr = root;
    }
}
