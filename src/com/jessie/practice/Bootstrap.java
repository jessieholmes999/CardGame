package com.jessie.practice;

import com.jessie.practice.entity.Player;
import com.jessie.practice.entity.Sender;
import com.jessie.practice.game.Deck;
import com.jessie.practice.constant.LoggerLevel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * 启动入口，更多游戏规则可查看{@link Deck}类说明
 */
public class Bootstrap {
    /**
     * 通过设置LOG_LEVEL可以控制打印内容，模拟日志级别
     */
    public static final LoggerLevel LOG_LEVEL = LoggerLevel.DEBUG;

    public static final CountDownLatch WAIT_FOR_COMPLETION = new CountDownLatch(1);
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        try {
            // 初始化牌桌
            Deck deck = new Deck();

            // 初始化发牌人
            Sender sender = new Sender();
            sender.joinToDeck(deck);

            // 初始化玩家，并加入牌桌
            IntStream.range(0, 4).forEach(i -> {
                final Player player = new Player("player-" + i);
                if (!player.joinToDeck(deck)) {
                    System.out.println(player.getName() + " join to the deck failure.");
                }
            });

            // 启动发牌人线程
            THREAD_POOL.execute(sender);
            // 启动多个玩家线程
            deck.getPlayers().forEach(THREAD_POOL::execute);

            WAIT_FOR_COMPLETION.await();
        } catch (InterruptedException e) {
            System.out.println("Error:" + e.getMessage());
            System.exit(1);
        } finally {
            THREAD_POOL.shutdownNow();
        }
        System.exit(0);
    }
}
