package com.jessie.practice.entity;

import com.jessie.practice.game.Deck;

import java.util.List;

/**
 * 定义发牌人相关的属性与行为
 */
public final class Sender extends Person implements Runnable {
    /**
     * 卡牌集合
     */
    private List<Card> cards;
    /**
     * 已发卡牌计数
     */
    private int usedCard = 0;
    /**
     * 总的卡牌数
     */
    private int sizeOfCards;

    /**
     * 加入游戏桌
     */
    @Override
    public boolean joinToDeck(Deck deck) {
        super.joinToDeck(deck);
        cards = deck.getCards();
        sizeOfCards = cards.size();
        return deck.setSender(this);
    }

    /**
     * 洗牌
     */
    public void shuffle() {
        Card.shuffle(cards);
        usedCard = 0;
    }

    /**
     * 剩下还未发出的卡牌数
     *
     * @return count of left cards.
     */
    public int getLeftCardsCount() {
        return sizeOfCards - usedCard;
    }

    /**
     * 拿取剩下卡牌中第一张卡牌
     *
     * @return card
     */
    public Card getCard() {
        if (usedCard == sizeOfCards) {
            throw new IllegalStateException("No cards are left in the deck.");
        }
        usedCard++;
        return cards.get(usedCard - 1);
    }

    /**
     * 发牌
     */
    @Override
    public void run() {
        getDeck().sendCard();
    }
}
