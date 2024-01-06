package org.example;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Objects;

public class TicTacToeServiceImpl extends UnicastRemoteObject implements Runnable, TicTacToeService {
    ArrayList<Player> players = new ArrayList<>();
    static ArrayList<Room> rooms = new ArrayList<>();
    private Room roomActive;

    protected TicTacToeServiceImpl() throws RemoteException {
        super();
    }
    public void setRoomActive(Room roomActive) {
        this.roomActive = roomActive;
    }

    @Override
    public void run()   {

        System.out.println("Game started");
        char playerOne = roomActive.player1.getSign();
        char playerTwo = roomActive.player2.getSign();
        roomActive.setGameInProgress(isMyTurn.YES);
        while(!gameInProgress(playerOne, playerTwo, roomActive)) {
           System.out.println(gameInProgress(playerOne, playerTwo, roomActive));
            if (roomActive.player1.moveMade) {
                System.out.println("true");
                roomActive.setBoard(roomActive.player1.getBoard());
                roomActive.player1.setMoveMade(false);
            }

            if (roomActive.player2.moveMade) {
                roomActive.setBoard(roomActive.player2.getBoard());
                roomActive.player2.setMoveMade(false);
            }
        }
        roomActive.setGameInProgress(isMyTurn.NO);
        roomActive.player2.setIsMyTurn(isMyTurn.NO);
        roomActive.player1.setIsMyTurn(isMyTurn.NO);

    }
    @Override
    public void start(String text) throws RemoteException {
        System.out.println(text);
    }

    @Override
    public char[][] display(Player player) throws RemoteException {
        Room roomActive = findMyRoom(player);
        return roomActive.board;

    }

    @Override
    public void makeMove(Player player) throws RemoteException {

        Room roomActive = findMyRoom(player);
        roomActive.setBoard(player.getBoard());

        roomActive = rooms.stream().filter(Room -> Room.player1.id.equals(player.id))
                .findFirst()
                .orElse(null);

        if(roomActive == null)  {
            roomActive = rooms.stream().filter(Room -> Room.player2.id.equals(player.id))
                    .findFirst()
                    .orElse(null);
            Objects.requireNonNull(roomActive).player2.setBoard(player.getBoard());
            Objects.requireNonNull(roomActive).player2.setMoveMade(true);
            roomActive.player2.setIsMyTurn(isMyTurn.NO);
            roomActive.player1.setIsMyTurn(isMyTurn.YES);


        } else {
            Objects.requireNonNull(roomActive).player1.setBoard(player.getBoard());
            roomActive.player1.setMoveMade(true);
            roomActive.player1.setIsMyTurn(isMyTurn.NO);
            roomActive.player2.setIsMyTurn(isMyTurn.YES);
        }


    }

    @Override
    public Player begin() throws RemoteException {

        Player player = new Player(String.valueOf(players.size() + 1));
        players.add(player);
        return player;
    }

    @Override
    public boolean waitForRoom(Player player) throws RemoteException {

        Room doWeHaveARoom = rooms.stream()
                .filter(Room -> (Room.getPlayer1().getId().equals(player.getId())))
                .findFirst()
                .orElse(null);

        return Objects.requireNonNull(doWeHaveARoom).player2 != null;
    }

    @Override
    public boolean isInProgress(Player player) throws RemoteException {

        Room roomActive = rooms.stream().filter(Room -> Room.player1.id.equals(player.id) || Room.player2.id.equals(player.id))
                .findFirst()
                .orElse(null);

        return Objects.requireNonNull(roomActive).gameInProgress == isMyTurn.YES;
    }

    @Override
    public Room findMyRoom(Player player)   {
        return rooms.stream().filter(Room -> Room.player1.id.equals(player.id) ||
                (Room.player2 != null && Room.player2.id.equals(player.id)))
                .findFirst()
                .orElse(null);
    }

    @Override
    public char getSign(Player player) throws RemoteException {

        Room roomActive = rooms.stream().filter(Room -> Room.player1.id.equals(player.id))
                .findFirst()
                .orElse(null);

        if(roomActive == null)  {
            roomActive = rooms.stream().filter(Room -> Room.player2.id.equals(player.id))
                    .findFirst()
                    .orElse(null);

            return roomActive.player2.getSign();


        } else {
            return roomActive.player1.getSign();
        }
    }

    @Override
    public isMyTurn canIMakeMove(Player player) throws RemoteException {
        Room roomActive = rooms.stream().filter(Room -> Room.player1.id.equals(player.id))
                .findFirst()
                .orElse(null);

        if(roomActive == null)  {
            roomActive = rooms.stream().filter(Room -> Room.player2.id.equals(player.id))
                    .findFirst()
                    .orElse(null);

            assert roomActive != null;
            return roomActive.player2.isMyTurn;

        } else {
            return roomActive.player1.isMyTurn;
        }

    }

    //Rooms////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ArrayList<Room> showRoomList() throws RemoteException {
        return rooms;
    }

    @Override
    public void createRoom(Player player) throws RemoteException {
        Room room = new Room(player);
        rooms.add(room);
    }

    @Override
    public void joinRoom(Player player, String selectedRoom) throws RemoteException {

        StringBuilder result = new StringBuilder();
        for (char character : selectedRoom.toCharArray()) {
            if (Character.isDigit(character)) {
                result.append(character);
            }
        }

        int roomNumber = Integer.parseInt(result.toString());

        Room roomToJoin = rooms.stream()
                .filter(Room -> Room.roomID == roomNumber)
                .findFirst()
                .orElse(null);

        Objects.requireNonNull(roomToJoin).setPlayer2(player);
        roomToJoin.player1.setSign('X');
        roomToJoin.player2.setSign('O');
        roomToJoin.player1.setIsMyTurn(isMyTurn.YES);
        roomToJoin.player2.setIsMyTurn(isMyTurn.NO);
        roomToJoin.setBoard(new char[][]{{' ', ' ', ' '},{' ', ' ',' '},{' ', ' ', ' '}});

        roomToJoin.setStatus(Status.BUSY);
        TicTacToeServiceImpl newGame = new TicTacToeServiceImpl();
        newGame.setRoomActive(roomToJoin);
        Thread ticTacToeThread = new Thread(newGame);
        ticTacToeThread.start();
    }

    @Override
    public String whoWon(Player player) throws RemoteException {
        Room ourRoom = findMyRoom(player);
        if (ourRoom.whoWon == 'X') {
            return "X";
        } else if(ourRoom.whoWon == 'O')    {
            return "O";
        }   else {
            return "No one";
        }
    }


    //logic of the game /////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean gameInProgress(char playerOne, char playerTwo, Room roomActive) {
         return didSomeoneWon(playerOne, playerTwo, roomActive) || isADraw(roomActive);
    }

    private static boolean didSomeoneWon(char playerOne, char playerTwo, Room roomActive)   {

       char[][] board = roomActive.board;
        for (int i = 0; i < 3; i++) {
            if ((board[i][0] == playerOne && board[i][1] == playerOne && board[i][2] == playerOne) ||   //vertical
                    (board[0][i] == playerOne && board[1][i] == playerOne && board[2][i] == playerOne)) { //horizontal
                roomActive.setWhoWon(playerOne);
                return true;
            }

            if ((board[i][0] == playerTwo && board[i][1] == playerTwo && board[i][2] == playerTwo) ||   //vertical
                    (board[0][i] == playerTwo && board[1][i] == playerTwo && board[2][i] == playerTwo)) { //horizontal
                roomActive.setWhoWon(playerTwo);
                return true;
            }

        }

        if ((board[0][0] == playerOne && board[1][1] == playerOne && board[2][2] == playerOne) ||   //cross
                (board[0][2] == playerOne && board[1][1] == playerOne && board[2][0] == playerOne)) {
            roomActive.setWhoWon(playerOne);
            return true;
        }

        if ((board[0][0] == playerTwo && board[1][1] == playerTwo && board[2][2] == playerTwo) ||   //cross
                (board[0][2] == playerTwo && board[1][1] == playerTwo && board[2][0] == playerTwo)) {
            roomActive.setWhoWon(playerTwo);
            return true;
        }

        return false;

    }

    private static boolean isADraw(Room roomActive)   {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (roomActive.board[i][j] == ' ') {
                    return false;
                }
            }
        }
        roomActive.setWhoWon(' ');
        return true;
    }

    public static void main(String[] args) {

        try {

            TicTacToeService game = new TicTacToeServiceImpl();
            LocateRegistry.createRegistry(1099);
            Naming.rebind("rmi://localhost:1099/TicTacToeService", game);
            System.err.println("Server running...");

        } catch (Exception exception) {

            System.out.println(exception);
        }

    }


}
