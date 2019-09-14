package com.jessie.practice.entity;

import com.jessie.practice.game.Deck;

import java.util.concurrent.locks.Condition;

public class Person {
    /**
     * 待加入的棋牌桌
     */
    private Deck deck;
    private Condition condition;

    /**
     * 加入棋牌桌
     *
     * @param deck 棋牌桌
     * @return true表明加入成功；反之失败
     */
    public boolean joinToDeck(Deck deck) {
        if (null == deck) {
            throw new IllegalStateException("Can't join to a null deck.");
        }
        this.deck = deck;
        this.condition = deck.getLock().newCondition();
        return true;
    }

    public Deck getDeck() {
        return deck;
    }

    public Condition getCondition() {
        return condition;
    }
}
