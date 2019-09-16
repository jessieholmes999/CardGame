package com.jessie.practice.entity;

import com.jessie.practice.game.Deck;

import java.util.ArrayList;
import java.util.List;

/**
 * 定义玩家的基本属性和行为
 */
public final class Player extends Person implements Runnable {

    /**
     * 玩家手上的卡牌集合
     */
    private final List<Card> cardsInHand;
    /**
     * 玩家名称
     */
    private final String name;
    /**
     * 卡牌累计总分
     */
    private int scoreOfSum;

    public Player(final String name) {
        // 初始化卡牌集合
        this.cardsInHand = new ArrayList<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean joinToDeck(Deck deck) {
        super.joinToDeck(deck);
        return deck.addPlayer(this);
    }

    /**
     * 拿取分发到的卡牌
     *
     * @param card card
     */
    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalStateException("Can't add a null card to a hand.");
        }
        cardsInHand.add(card);
        scoreOfSum += card.getValue();
    }

    /**
     * 返回手上卡牌数量
     *
     * @return int
     */
    public int getCardCount() {
        return cardsInHand.size();
    }

    /**
     * 返回手上卡牌累计分值
     *
     * @return int
     */
    public int getScoreOfSum() {
        return scoreOfSum;
    }

    /**
     * 展示所有卡牌
     */
    public void showCards() {
        cardsInHand.forEach(card -> {
            System.out.println(card.toString());
        });
    }

    @Override
    public void run() {
        getDeck().reportScore(this);
    }

}
