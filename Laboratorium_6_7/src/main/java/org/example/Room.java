package org.example;

import java.io.Serializable;
import java.util.ArrayList;

public class Room implements Serializable {
    ArrayList<User> users = new ArrayList<>();
    String roomID;
    char [][] board;
    char winner;

    public Room(String roomID) {
        this.roomID = roomID;
        this.board = new char[][]{{' ', ' ',' '}, {' ', ' ',' '}, {' ', ' ',' '}};
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public void setWinner(char winner) {
        this.winner = winner;
    }
}