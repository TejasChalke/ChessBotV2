package com.example.chessbotv2.api;

import com.example.chessbotv2.bot.Bot;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/v2")
public class BotAPI {
    static public class MatchStartData {
        @JsonProperty
        private int[] board;
        @JsonProperty
        private boolean whiteToMove;
        @JsonProperty
        private int epSquare;

        public MatchStartData(int[] board, int epSquare, boolean whiteToMove) {
            this.board = board;
            this.whiteToMove = whiteToMove;
            this.epSquare = epSquare;
        }

        public int[] getBoard() {
            return board;
        }

        public boolean isWhiteToMove() {
            return whiteToMove;
        }

        public int getEpSquare() {
            return epSquare;
        }
    }

    Bot bot;

    @GetMapping("/startMatch")
    public ResponseEntity<MatchStartData> startMatch(@RequestParam String mode) {
        if (mode == null) {
            System.err.println("A mode must be provided to start a match");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        bot = new Bot();
        MatchStartData data = new MatchStartData(bot.board.board, bot.board.epSquare, true);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
