# 题目
* Task1: Please represent 54 playing Card Deck as a Java class. Imagine which methods could be placed inside.
* Task2: Using task1 as your class. Imagine there are 1 card sender and 3 players. The card sender send card to player one by one in a round. Once the player’s sum points bigger than 50, the player win the game.  Multi-thread should be used in this task.

> [Points define]
```text
"A"=1,"2"=2,"3"=3,"4"=4,"5"=5,"6"=6,"7"=7,"8"=8,"9"=9,"10"=10,"J"=11,"Q"=12,"K"=13,"black Joke"=20,"red Joke"=20;
```

> Example 1:
```text
Round1 = Sender ["A","6","K"=13] -> Player1=1, player2=6, player3=13;
Round2 = Sender ["10","Q","black Joke"]-> Player1=11, player2=18, player3=33;
Round3 = Sender ["9","J","red Joke"]-> Player1=20, player2=29, player3=53-> player 3 win;
```


