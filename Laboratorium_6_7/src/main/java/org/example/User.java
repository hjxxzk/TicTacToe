package org.example;

import java.io.Serializable;

public class User implements Serializable {
    int ID;
    char[][] table;
    boolean hasStarted;
    boolean myTurn;
    boolean busy;
    boolean connected;
    int [] statistic;
    char sign;

    public User(int ID) {
        this.ID = ID;
        this.busy = false;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setTable(char[][] table) {
        this.table = table;
    }

    public void setHasStarted(boolean hasStarted) {
        this.hasStarted = hasStarted;
    }

    public int[] getStatistic() {
        return statistic;
    }

    public void setStatistic(int[] statistic) {
        this.statistic = statistic;
    }

    public void setSign(char sign) {
        this.sign = sign;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }
}