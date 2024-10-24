package com.example.chessbotv2.test;

import com.example.chessbotv2.bot.Bot;

public class TestMain {
    public static void main(String[] args) {
        Bot bot = new Bot("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        bot.testMoves(2, false);

//        Bot bot = new Bot();
//        bot.testMoves(5, true);
    }
}
