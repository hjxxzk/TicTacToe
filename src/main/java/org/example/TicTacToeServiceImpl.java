package org.example;

import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

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
        boolean running = true;
        while(running) {

            if(roomActive == null)  {
                running = false;
            }   else if (roomActive.status.equals(Status.BUSY) && roomActive.player1.wantToPlayNext.equals(isMyTurn.YES) && roomActive.player2.wantToPlayNext.equals(isMyTurn.YES)) {

                System.out.println("Game has started");
                char playerOne = roomActive.player1.getSign();
                char playerTwo = roomActive.player2.getSign();
                setOrder(roomActive);
                roomActive.setBoard(new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}});
                roomActive.setGameInProgress(isMyTurn.YES);

                while (!gameInProgress(playerOne, playerTwo, roomActive)) {

                    if(roomActive.player1 == null || roomActive.player2 == null) {
                        roomActive.setStatus(Status.READY);
                        roomActive.setGameInProgress(isMyTurn.NO);
                        break;
                    }

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
                roomActive.player1.setWantToPlayNext(isMyTurn.NO);
                roomActive.player2.setWantToPlayNext(isMyTurn.NO);
                roomActive.setGameInProgress(isMyTurn.NO);
                roomActive.player2.setIsMyTurn(isMyTurn.NO);
                roomActive.player1.setIsMyTurn(isMyTurn.NO);
                roomActive.player1.setMoveMade(false);
                roomActive.player2.setMoveMade(false);
                roomActive.player1.setSign(playerTwo);
                roomActive.player2.setSign(playerOne);
              //
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }


            }  else {

                if(roomActive.player1 != null && roomActive.player2!= null)
                    roomActive.setStatus(Status.BUSY);

            }
        }
    }

    private void setOrder(Room roomActive)  {
        if(roomActive.player1.sign == 'X')  {
            roomActive.player1.setIsMyTurn(isMyTurn.YES);
        }   else {
            roomActive.player2.setIsMyTurn(isMyTurn.YES);
        }
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

        if (roomActive != null) {
            roomActive.setBoard(player.getBoard());

            if (roomActive.player1 != null && roomActive.player1.id.equals(player.id)) {
                roomActive.player1.setBoard(player.getBoard());
                roomActive.player1.setMoveMade(true);
                roomActive.player1.setIsMyTurn(isMyTurn.NO);

                if (roomActive.player2 != null) {
                    roomActive.player2.setIsMyTurn(isMyTurn.YES);
                }
            } else if (roomActive.player2 != null && roomActive.player2.id.equals(player.id)) {
                roomActive.player2.setBoard(player.getBoard());
                roomActive.player2.setMoveMade(true);
                roomActive.player2.setIsMyTurn(isMyTurn.NO);

                if (roomActive.player1 != null) {
                    roomActive.player1.setIsMyTurn(isMyTurn.YES);
                }
            }
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
        if (doWeHaveARoom != null)  {
            return Objects.requireNonNull(doWeHaveARoom).player2 != null;
        }   else {
            doWeHaveARoom = rooms.stream()
                    .filter(Room -> (Room.getPlayer2().getId().equals(player.getId())))
                    .findFirst()
                    .orElse(null);
        }
        return Objects.requireNonNull(doWeHaveARoom).player1 != null;
    }

    @Override
    public boolean isInProgress(Player player) throws RemoteException {

        Room roomActive = findMyRoom(player);

        return Objects.requireNonNull(roomActive).gameInProgress == isMyTurn.YES;
    }

    @Override
    public Room findMyRoom(Player player)   {
        return rooms.stream().filter(Room -> (Room.player1 != null && Room.player1.id.equals(player.id)) ||
                (Room.player2 != null && Room.player2.id.equals(player.id)))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void logOut(Player player) throws RemoteException {
        Room roomActive = findMyRoom(player);

        if(roomActive.player1 != null && roomActive.player1.id.equals(player.id))   {
            roomActive.setPlayer1(null);
            roomActive.setStatus(Status.READY);
            //rooms.remove(roomActive);

        }

        if(roomActive.player2 != null && roomActive.player2.id.equals(player.id))   {
            roomActive.setPlayer2(null);
            roomActive.setStatus(Status.READY);
           // rooms.remove(roomActive);

        }
    }

    @Override
    public boolean isThereSomeone(Player player) throws RemoteException {
        Room myRoom = findMyRoom(player);
        if (myRoom.player1 == null || myRoom.player2 == null) {
            rooms.remove(myRoom);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void wantToPlay(Player player) throws RemoteException {
        Room room = findMyRoom(player);

        if(room.player1.id.equals(player.id))   {
            room.player1.setWantToPlayNext(isMyTurn.YES);
        }   else {
            room.player2.setWantToPlayNext(isMyTurn.YES);
        }

        if(room.player1.wantToPlayNext.equals(isMyTurn.YES) && room.player2.wantToPlayNext.equals(isMyTurn.YES))    {
            room.setGameInProgress(isMyTurn.YES);
        }

    }

    @Override
    public char getSign(Player player) throws RemoteException {

        Room roomActive = findMyRoom(player);

        if (roomActive != null) {
            if (roomActive.player1 != null && roomActive.player1.id.equals(player.getId())) {
                return roomActive.player1.getSign();
            } else if (roomActive.player2 != null && roomActive.player2.id.equals(player.getId())) {
                return roomActive.player2.getSign();
            }
        }
        return ' ';
    }

    @Override
    public isMyTurn canIMakeMove(Player player) throws RemoteException {

        Room roomActive = findMyRoom(player);

        if (roomActive != null) {
            if (roomActive.player1 != null && roomActive.player1.id.equals(player.getId())) {
                return roomActive.player1.isMyTurn;
            } else if (roomActive.player2 != null && roomActive.player2.id.equals(player.getId())) {
                return roomActive.player2.isMyTurn;
            }
        }

        return isMyTurn.NO;

    }

    //Rooms////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ArrayList<Room> showRoomList() throws RemoteException {
        return rooms.stream()
                .filter(room -> room.getStatus().equals(Status.READY))
                .collect(Collectors.toCollection(ArrayList::new));
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

        if(Objects.requireNonNull(roomToJoin).player1 == null)  {
            roomToJoin.setPlayer1(player);
        }   else if (roomToJoin.player2 == null) {
            roomToJoin.setPlayer2(player);
        }

        roomToJoin.player1.setSign('X');
        roomToJoin.player2.setSign('O');
        roomToJoin.player1.setIsMyTurn(isMyTurn.YES);
        roomToJoin.player2.setIsMyTurn(isMyTurn.NO);
        roomToJoin.player1.setWantToPlayNext(isMyTurn.YES);
        roomToJoin.player2.setWantToPlayNext(isMyTurn.YES);
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
        boolean didWin = didSomeoneWon(playerOne, playerTwo, roomActive);
        if(didWin)  {
            return true;
        }
        boolean isDraw = isADraw(roomActive);

        return isDraw;
    }

    private static boolean didSomeoneWon(char playerOne, char playerTwo, Room roomActive)   {
        System.out.println("tak");
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
        System.out.println("tak");
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

            ServerSocket serverSocket = new ServerSocket(1098);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

//                // Tworzenie wątku obsługującego klienta
//                Thread clientThread = new Thread(new ClientHandler(clientSocket));
//                clientThread.start();
            }

        } catch (Exception exception) {

            System.out.println(exception);
            exception.printStackTrace();
        }

    }


}
