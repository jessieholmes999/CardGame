package com.jessie.practice.game;

import com.jessie.practice.constant.LoggerLevel;
import com.jessie.practice.constant.RuleConstant;
import com.jessie.practice.entity.Card;
import com.jessie.practice.entity.Player;
import com.jessie.practice.entity.Sender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.jessie.practice.Bootstrap.LOG_LEVEL;
import static com.jessie.practice.Bootstrap.WAIT_FOR_COMPLETION;

/**
 * 虚拟为棋牌桌，其中定义当前棋牌桌的基本属性和游戏规则：
 * 1、当前棋牌桌最多3个玩家同时游戏；
 * 2、只有得分超过阈值的玩家方可进入候选名单；如果出现超过阈值玩家则本轮未超过阈值玩家被剔除；
 * 3、发牌人将对进入候选名单的玩家进行仲裁，得分高者获胜；如果得分相同则共同进入下一轮；
 * 4、直到最终仅有一人胜出；
 * 5、如果剩余的卡牌不足以分发至所有玩家，则认为剩下玩家均为获胜者；
 */
public final class Deck {
    /**
     * 卡牌
     */
    private final List<Card> cards;
    /**
     * 玩家，当前桌子最多支持3个玩家
     */
    private static final int MAX_PLAYER_COUNT = 3;
    /**
     * 只有一个sender线程对其进行修改与读取，故采用ArrayList即可
     */
    private final List<Player> players = new ArrayList<>(MAX_PLAYER_COUNT);
    /**
     * 发牌人
     */
    private Sender sender;
    /**
     * 得分超过min_line的玩家则加入到候选名单，类似为明牌玩家；只有这些玩家进入下一轮发牌
     */
    private final List<Player> candidates = new Vector<>(MAX_PLAYER_COUNT);

    private final Lock lock = new ReentrantLock();
    /**
     * 一个线程修改多个线程读取，通过volatile实现可见性
     */
    private volatile boolean isStart = false;
    /**
     * 存储玩家计分上报进度
     */
    private final AtomicInteger processed = new AtomicInteger(0);

    public Deck() {
        // 初始化54张card
        cards = Card.initialize();
    }

    public Lock getLock() {
        return lock;
    }

    public List<Card> getCards() {
        return cards;
    }

    public List<Player> getPlayers() {
        return players;
    }

    /**
     * 设置发牌人
     *
     * @param sender 发牌人
     * @return true表明设置成功;反之失败
     */
    public boolean setSender(final Sender sender) {
        if (null == sender) {
            throw new IllegalArgumentException("The sender must not be null.");
        }
        if (isStart) {
            System.out.println("Game is starting , can not replace the sender.");
            return false;
        }
        this.sender = sender;
        return true;
    }

    /**
     * 添加玩家
     *
     * @param player 玩家
     * @return true表明添加成功;反之失败
     */
    public boolean addPlayer(final Player player) {
        if (isStart) {
            System.out.println("Game has started. Can not add any player.");
            return false;
        }
        if (null == player) {
            throw new IllegalArgumentException("The player must not be null.");
        }
        if (players.size() == MAX_PLAYER_COUNT) {
            System.out.println("Tip: Sorry " + player.getName() + " , the deck is full now. You can watch or join us in the next round.");
            return false;
        } else {
            players.add(player);
            return true;
        }
    }

    /**
     * 发牌人发牌
     */
    public void sendCard() {
        try {
            lock.lock();

            // 如果没有玩家则直接退出游戏
            if (0 == players.size()) {
                System.out.println("Warning: There is no player. Game is over.");
                WAIT_FOR_COMPLETION.countDown();
                return;
            }

            System.out.println("\n***************************");
            System.out.println("Shuffle the cards at first.");
            // 优先洗牌后方可发牌
            sender.shuffle();
            System.out.println("Starting to send cards.");
            System.out.println("***************************\n");
            while (true) {
                // 避免未发牌即开始判断
                if (isStart) {
                    // 如果候选人不为空，则判断所有的候选玩家手上的总分，平分则加赛
                    if (candidates.size() > 0) {
                        checkCard(candidates);
                        // 如果仅剩一位候选人则游戏结束
                        if (candidates.size() == 1) {
                            System.out.println("\n***************************");
                            printWinnerInfo(candidates.get(0));
                            System.out.println("***************************\n");
                            WAIT_FOR_COMPLETION.countDown();
                            break;
                        }
                        // 进入下一轮玩家
                        players.retainAll(candidates);
                        candidates.clear();
                    }

                    System.out.println("Next round with " + players.size() + " players.");
                    // 判断剩下的牌是否满足发牌条件：剩余牌需要大于等于参与的玩家数
                    if (sender.getLeftCardsCount() < players.size()) {
                        System.out.println("\n***************************");
                        System.out.println("There is no more left card for each player. So you are all winners:");
                        players.forEach(this::printWinnerInfo);
                        System.out.println("***************************\n");
                        WAIT_FOR_COMPLETION.countDown();
                        break;
                    }
                }

                isStart = true;
                // 重置上报进度
                processed.set(0);
                // 发牌人对玩家列表依次发牌
                players.forEach(player -> {
                    player.addCard(sender.getCard());
                    player.getCondition().signal();
                });
                sender.getCondition().await();
            }
        } catch (InterruptedException e) {
            System.out.println("Sender quits the game.");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 玩家计分上报
     *
     * @param currentPlayer 当前玩家
     */
    public void reportScore(final Player currentPlayer) {
        try {
            lock.lock();
            while (true) {
                // 只有启动发牌了才会进行玩家计分上报环节
                if (isStart) {
                    // 每发一轮，玩家上报自己的得分，仅在超过win_line时才会上报
                    if (currentPlayer.getScoreOfSum() >= RuleConstant.MIN_SCORE_LINE_FOR_WINNER) {
                        candidates.add(currentPlayer);
                    }
                    // 当剩下参加的所有玩家均完成本轮计分则唤醒发牌人继续发牌
                    if (processed.incrementAndGet() == players.size()) {
                        sender.getCondition().signal();
                    }
                } else {
                    sender.getCondition().signal();
                }
                currentPlayer.getCondition().await();
            }
        } catch (InterruptedException e) {
            System.out.println(currentPlayer.getName() + " quit the game.");
        } finally {
            lock.unlock();
        }

    }

    /**
     * 对所有的玩家进行计分判定
     *
     * @param candidates 当前分数超过50分的玩家
     */
    private void checkCard(List<Player> candidates) {
        System.out.println("\n" + candidates.size() + " player's score is more than " + RuleConstant.MIN_SCORE_LINE_FOR_WINNER);
        // 如果有玩家的得分超过win_line，则开始比对玩家得分，并剔除低分者
        List<Player> tempPlayers = candidates.stream()
                .sorted(Comparator.comparingInt(Player::getScoreOfSum).reversed())
                .collect(Collectors.toList());
        if (LoggerLevel.DEBUG.equals(LOG_LEVEL)) {
            System.out.println("Show cards of these players");
            tempPlayers.forEach(player -> {
                System.out.println("**********" + player.getName() + "**********");
                player.showCards();
                System.out.println("Has " + player.getCardCount() + " cards in hand. Score is " + player.getScoreOfSum());
            });
        }
        switch (tempPlayers.size()) {
            case 0:
                // 没有玩家超过，保留所有玩家继续发牌
                break;
            case 1:
                // 仅有一个玩家超过win_line，则游戏结束，该玩家为最终赢家
                break;
            case 2:
                // 有两个玩家则比对其大小，如果两个玩家的大小相等，则只有这两个玩家进入下一轮；反之分高者取胜
                if (tempPlayers.get(0).getScoreOfSum() == tempPlayers.get(1).getScoreOfSum()) {
                    candidates.retainAll(tempPlayers);
                } else {
                    candidates.remove(tempPlayers.get(1));
                }
                break;
            case 3:
                // 如果第一个玩家得分大于第二个玩家，则第一个玩家即为赢家
                if (tempPlayers.get(0).getScoreOfSum() > tempPlayers.get(1).getScoreOfSum()) {
                    candidates.remove(tempPlayers.get(1));
                    candidates.remove(tempPlayers.get(2));
                    break;
                }
                // 如果三个玩家得分相同，则三个玩家均进入下一轮
                if (tempPlayers.get(0).getScoreOfSum() == tempPlayers.get(1).getScoreOfSum() &&
                        tempPlayers.get(0).getScoreOfSum() == tempPlayers.get(2).getScoreOfSum()) {
                    break;
                }
                if (tempPlayers.get(0).getScoreOfSum() == tempPlayers.get(1).getScoreOfSum()) {
                    candidates.remove(tempPlayers.get(2));
                    break;
                }
            default:
        }
    }


    /**
     * 打印赢家信息
     *
     * @param winner the win player
     */
    private void printWinnerInfo(Player winner) {
        System.out.println("Winner is " + winner.getName() + ", has " + winner.getCardCount() + " cards,sum score is " + winner.getScoreOfSum());
        winner.showCards();
    }
}
