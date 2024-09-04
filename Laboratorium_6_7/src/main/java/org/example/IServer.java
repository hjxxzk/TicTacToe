package org.example;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IServer extends Remote {
    void connect (String txt) throws RemoteException;
    User logIN () throws RemoteException;
    ArrayList<Room> getRoomList () throws RemoteException;
    void createRoom (User user) throws RemoteException;
    void joinRoom (User user, String selectedRoom) throws RemoteException;
    void logOut (User user) throws RemoteException;
    boolean waitForUser (User user) throws RemoteException;
    char[][] getBoard (User user) throws RemoteException;
    boolean checkMyTurn (User user) throws RemoteException;
    int[] getWins (User user) throws RemoteException;
    void move (User user) throws RemoteException;
    char getSign(User user) throws RemoteException;
    char winner(User user) throws RemoteException;
    int[] getStats(String ID) throws RemoteException;
    void ping(User user) throws RemoteException;
}