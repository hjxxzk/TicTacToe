package org.example;

import java.io.Serializable;

public class Player implements Serializable  {
    final String id;
    char[][] board;
    char sign;
    Status status;
    boolean moveMade;
    int x;
    int y;
    isMyTurn isMyTurn;
    public volatile boolean gameStarted;

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public boolean isMoveMade() {
        return moveMade;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setMoveMade(boolean moveMade) {
        this.moveMade = moveMade;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public org.example.isMyTurn getIsMyTurn() {
        return isMyTurn;
    }

    public void setIsMyTurn(org.example.isMyTurn isMyTurn) {
        this.isMyTurn = isMyTurn;
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public char[][] getBoard() {
        return board;
    }

    public void setSign(char sign) {
        this.sign = sign;
    }

    public char getSign() {
        return sign;
    }

    public synchronized boolean isGameStarted() {
        return gameStarted;
    }

    public synchronized void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
       // this.sign = 'O';
    }

    public Player(String id) {
        this.id = id;
        this.status = Status.READY;
        this.gameStarted = false;
    }

}
