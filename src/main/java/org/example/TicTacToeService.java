package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface TicTacToeService extends Remote {

     void start(String text) throws RemoteException;
     char[][] display(Player player) throws RemoteException;
     void makeMove(Player player) throws RemoteException;
     Player begin() throws RemoteException;
     boolean waitForRoom(Player player) throws RemoteException;
     boolean isInProgress(Player player) throws RemoteException;
     char getSign(Player player) throws RemoteException;
     isMyTurn canIMakeMove(Player player) throws RemoteException;
     ArrayList<Room> showRoomList() throws RemoteException;
     void createRoom(Player player) throws RemoteException;
     void joinRoom(Player player, String selectedRoom) throws RemoteException;
     String whoWon(Player player) throws RemoteException;
     Room findMyRoom(Player player) throws RemoteException;
     void logOut(Player player) throws RemoteException;
     boolean isThereSomeone(Player player) throws RemoteException;
     void wantToPlay(Player player) throws RemoteException;


}
