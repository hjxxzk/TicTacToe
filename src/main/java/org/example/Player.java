package org.example;

import java.io.Serializable;

public class Player implements Serializable  {
    final String id;
    char[][] board;
    char sign;
    Status status;
    boolean moveMade;
    isMyTurn isMyTurn;
    isMyTurn wantToPlayNext;

    public void setWantToPlayNext(org.example.isMyTurn wantToPlayNext) {
        this.wantToPlayNext = wantToPlayNext;
    }

    public org.example.isMyTurn getWantToPlayNext() {
        return wantToPlayNext;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public boolean isMoveMade() {
        return moveMade;
    }

    public int getX() {
        return x;
    }

    public void setMoveMade(boolean moveMade) {
        this.moveMade = moveMade;
    }

    public void setIsMyTurn(org.example.isMyTurn isMyTurn) {
        this.isMyTurn = isMyTurn;
    }

    public String getId() {
        return id;
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


    public Player(String id) {
        this.id = id;
        this.status = Status.READY;
    }

}
