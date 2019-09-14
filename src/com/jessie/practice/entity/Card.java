package com.jessie.practice.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 定义卡牌的基础属性和预定义行为
 */
public final class Card {
    /**
     * 定义JOKER卡牌的value
     */
    private final static int JOKER_VALUE = 20;

    /**
     * 定义卡牌的花色，主要为SPADES, HEARTS, DIAMONDS, CLUBS, or JOKER.
     * 初始化完成后卡牌的花色即不能被修改.
     */
    private final SuitEnum suit;

    /**
     * 定义卡牌对应的value值，用于得分累加，
     * 初始化后value值同样不能修改.
     */
    private final int value;

    private Card(SuitEnum suit, int value) {
        if (null == suit) {
            throw new IllegalArgumentException("The suit of the card must not be null.");
        }

        boolean isJoker = suit.isJoker;
        if (isJoker && value != JOKER_VALUE) {
            throw new IllegalArgumentException("The value of the joker suit must be 20.");
        } else if (!isJoker && (value < 1 || value > 13)) {
            throw new IllegalArgumentException("Illegal playing card value");
        }
        this.suit = suit;
        this.value = value;
    }

    /**
     * 初始化54张卡牌
     *
     * @retur 卡牌集合
     */
    public static List<Card> initialize() {
        final List<Card> cards = new ArrayList<>(54);
        Stream.of(SuitEnum.values())
                .filter(suitEnum -> !suitEnum.isJoker)
                .forEach(suit -> {
                    IntStream.rangeClosed(1, 13).forEach(value -> {
                        cards.add(new Card(suit, value));
                    });
                });
        cards.add(new Card(SuitEnum.BLACK_JOKER, JOKER_VALUE));
        cards.add(new Card(SuitEnum.RED_JOKER, JOKER_VALUE));

        return cards;
    }

    /**
     * 洗牌
     *
     * @param cards 待处理的卡牌集合
     */
    public static void shuffle(final List<Card> cards) {
        Collections.shuffle(cards);
    }

    /**
     * 返回卡牌的花色
     *
     * @return the suit
     */
    public SuitEnum getSuit() {
        return suit;
    }

    /**
     * 返回卡牌的value值
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * 获取每张卡牌显示的名称，主要依据当前卡牌的类型或者value来判断
     *
     * @return 卡牌显示名称
     */
    private String getDisplayName() {
        if (suit.isJoker) {
            return suit.getDisplayName();
        } else {
            switch (value) {
                case 1:
                    return "Ace";
                case 2:
                    return "2";
                case 3:
                    return "3";
                case 4:
                    return "4";
                case 5:
                    return "5";
                case 6:
                    return "6";
                case 7:
                    return "7";
                case 8:
                    return "8";
                case 9:
                    return "9";
                case 10:
                    return "10";
                case 11:
                    return "Jack";
                case 12:
                    return "Queen";
                default:
                    return "King";
            }
        }
    }

    /**
     * 打印卡牌信息
     *
     * @return 如果是joker花色，其格式为black joker with value : 20;非joker花色，其格式为1 of space with value : 1
     */
    @Override
    public String toString() {
        return (suit.isJoker ? getDisplayName() : getDisplayName() + " of " + suit.getDisplayName()) + " with value : " + getValue();
    }

    /**
     * 定义卡牌的花色
     */
    enum SuitEnum {
        /**
         * space
         */
        SPADE("space", false),
        /**
         * heart
         */
        HEART("heart", false),
        /**
         * diamond
         */
        DIAMOND("diamond", false),
        /**
         * club
         */
        CLUB("club", false),
        /**
         * red joker
         */
        RED_JOKER("red joker", true),
        /**
         * black joker
         */
        BLACK_JOKER("black joker", true);

        private String displayName;
        private boolean isJoker;

        SuitEnum(String displayName, boolean isJoker) {
            this.displayName = displayName;
            this.isJoker = isJoker;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isJoker() {
            return isJoker;
        }
    }

}
