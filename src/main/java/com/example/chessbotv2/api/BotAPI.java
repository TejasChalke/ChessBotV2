package com.example.chessbotv2.api;

import com.example.chessbotv2.bot.Bot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController("/v2")
public class BotAPI {
    Bot bot;
    @GetMapping({"/setBoard", "/setBoard/{fen}"})
    private ResponseEntity<String> setBoard(@PathVariable(required = false) String fen) {
        if (this.bot == null) {
            bot = fen == null || fen.isEmpty() ? new Bot() : new Bot(fen);
        } else {
            bot.resetBoard();
        }

        return ResponseEntity.ok("Board Set");
    }

    @GetMapping("/testMoves/{depth}")
    private ResponseEntity<String> testMoves(@PathVariable int depth) {
        if (this.bot == null) {
            return ResponseEntity.ok("Bot is not initialized");
        }

        bot.testMoves(depth);
        return ResponseEntity.ok("Board Set");
    }
}
