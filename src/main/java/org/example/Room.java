package org.example;

import java.io.Serializable;

public class Room implements Serializable {
    int roomID;
    Player player1;
    Player player2;
    Status status;
    char[][] board;
    static int index = 1;
    public volatile isMyTurn gameInProgress;
    char whoWon;

    public void setWhoWon(char whoWon) {
        this.whoWon = whoWon;
    }

    public synchronized void setGameInProgress(isMyTurn gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Status getStatus() {
        return status;
    }

    public Room(Player player1) {
        this.player1 = player1;
        this.roomID = index;
        this.status = Status.READY;
        this.board = new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}};
        index++;
    }

}
