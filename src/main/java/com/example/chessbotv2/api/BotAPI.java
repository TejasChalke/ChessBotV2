package com.example.chessbotv2.api;

import com.example.chessbotv2.bot.BoardUtil;
import com.example.chessbotv2.bot.Bot;
import com.example.chessbotv2.bot.Move;
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
        private String board;
        @JsonProperty
        private boolean whiteToMove;

        public MatchStartData(int[] board, boolean whiteToMove) {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<64; i++) {
                sb.append(board[i]);
                if (i < 63) sb.append('.');
            }
            this.board = sb.toString();
            this.whiteToMove = whiteToMove;
        }

        public String getBoard() {
            return board;
        }

        public boolean isWhiteToMove() {
            return whiteToMove;
        }
    }

    public static class BotTestData {
        public String fen;
        public String mode;
    }

    Bot bot;

    @PostMapping("/startMatch")
    public ResponseEntity<MatchStartData> startMatch(@RequestBody BotTestData data) {
        if (data.mode == null) {
            System.err.println("A mode must be provided to start a match");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (data.mode.equals("BotTest")) {
            System.out.println("\f");
            String fen = data.fen != null && !data.fen.isEmpty() ? data.fen : BoardUtil.DefaultFEN;
            bot = new Bot(fen);
            MatchStartData matchStartData = new MatchStartData(bot.board.board, true);
            return new ResponseEntity<>(matchStartData, HttpStatus.OK);
        }
        return null;
    }

    @GetMapping("/playMove")
    public ResponseEntity<Move> playMove() {
        if (bot == null) {
            System.err.println("Bot does not exist to play the move");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Move move = bot.playMove();
        return new ResponseEntity<>(move, HttpStatus.OK);
    }
}
