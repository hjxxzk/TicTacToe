package org.example;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements Runnable, IServer{
    static ArrayList<User> userList = new ArrayList<>();
    static ArrayList<Room> roomList = new ArrayList<>();
    static Room currentRoom;

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    protected Server() throws RemoteException {
        super ();
        startPing();
    }

    public static void main(String[] args) {
        try{
            IServer server = new Server();
            LocateRegistry.createRegistry(2137);
            Naming.rebind("rmi://localhost:2137/Server", server);
            System.out.println("Server ready!");

            ServerSocket serverSocket = new ServerSocket(4200);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStream.writeObject(roomList);

            }
        } catch (Exception e){
            System.out.println(e);
        }
    }

    @Override
    public void connect (String txt) throws RemoteException {
        System.out.println(txt);
    }

    private void startPing() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                pingClients();
            }
        }, 0, 10000);
    }

    private void pingClients() {
        Iterator<User> iterator = userList.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.connected) {
                user.setConnected(false);
            } else {
                remove(user);
                iterator.remove();
                System.out.println("User " + user.ID + " disconnected");
            }
        }
    }


    @Override
    public User logIN () throws RemoteException {
        User user = new User(userList.size());
        user.setConnected(true);
        userList.add(user);
        System.out.println("User " + user.ID + " connected");
        return user;
    }

    @Override
    public ArrayList<Room> getRoomList() throws RemoteException {
        return roomList;
    }

    @Override
    public void createRoom (User user) throws RemoteException{

        remove(user);

        Room room = new Room(String.valueOf(roomList.size()+1));
        room.setBoard(new char[][]{{' ', ' ',' '}, {' ', ' ',' '}, {' ', ' ',' '}});
        room.users.add(user);
        roomList.add(room);
    }

    public void remove(User user)   {
        roomList.forEach(Room -> Room.users.removeIf(User -> User.ID == user.ID));
    }

    @Override
    public void joinRoom (User user, String selectedRoom) throws RemoteException {

        remove(user);

        Room joinedRoom = roomList.stream().filter(Room -> Room.roomID.equals(selectedRoom)).findFirst().orElse(null);

        if (joinedRoom != null && joinedRoom.users.size() < 2) {
            joinedRoom.users.add(user);
            joinedRoom.setBoard(new char[][]{{' ', ' ',' '}, {' ', ' ',' '}, {' ', ' ',' '}});

            if (joinedRoom.users.size() == 2){
                joinedRoom.setBoard(new char[][]{{' ', ' ',' '}, {' ', ' ',' '}, {' ', ' ',' '}});
                joinedRoom.users.get(0).setSign('X');
                joinedRoom.users.get(1).setSign('O');
                Server TicTacToe = new Server();
                TicTacToe.setCurrentRoom(joinedRoom);
                Thread thread = new Thread(TicTacToe);
                thread.start();
            }

        }
    }

    @Override
    public void logOut (User user) throws RemoteException {
        remove(user);
        userList.removeIf(User -> User.ID == user.ID);
        System.out.println("User " + user.ID + " disconnected");
    }

    @Override
    public boolean waitForUser (User user) throws RemoteException{
        Room myRoom = roomList.stream().filter(Room -> Room.users.stream().anyMatch(User -> User.ID == user.ID))
                .findFirst()
                .orElse(null);

        if(myRoom != null)  {
            return myRoom.users.size() == 2;
        }
        return false;
    }

    @Override
    public char[][] getBoard (User user) throws RemoteException{
        Room myRoom = getRoom(user);
        return myRoom.board;
    }

    @Override
    public boolean checkMyTurn (User user) throws RemoteException{

        Room myRoom = getRoom(user);

        if (myRoom != null)  {
            if (myRoom.users.get(0) != null && myRoom.users.get(1) != null && myRoom.users.get(0).ID == user.ID)
                return myRoom.users.get(0).myTurn;
            else
                return myRoom.users.get(1).myTurn;
        }
        return true;
    }

    @Override
    public int[] getWins(User user) throws RemoteException {
        Room myRoom = getRoom(user);

        if (myRoom.users.get(0) != null && myRoom.users.get(0).ID == user.ID)
            return myRoom.users.get(0).getStatistic();
        else if (myRoom.users.get(1) != null)
            return myRoom.users.get(1).getStatistic();
        else
            return null;
    }

    @Override
    public void move(User user) throws RemoteException {
        Room myRoom = getRoom(user);

        if (myRoom != null) {
            myRoom.setBoard(user.table);

            if (myRoom.users.get(0) != null && myRoom.users.get(0).ID == user.ID) {
                myRoom.users.get(0).setTable(user.table);
                myRoom.users.get(0).setHasStarted(true);
                myRoom.users.get(0).setMyTurn(false);

                if (myRoom.users.get(1) != null)
                    myRoom.users.get(1).setMyTurn(true);

            } else if (myRoom.users.get(1) != null && myRoom.users.get(1).ID == user.ID) {
                myRoom.users.get(1).setTable(user.table);
                myRoom.users.get(1).setHasStarted(true);
                myRoom.users.get(1).setMyTurn(false);

                if (myRoom.users.get(0) != null) {
                    myRoom.users.get(0).setMyTurn(true);
                }
            }
        }
    }

    @Override
    public char getSign(User user) throws RemoteException {
        Room myRoom = getRoom(user);

        if (myRoom.users.get(0) != null && myRoom.users.get(0).ID == user.ID)
            return myRoom.users.get(0).sign;
        else if (myRoom.users.get(1) != null)
            return myRoom.users.get(1).sign;

        return ' ';
    }

    @Override
    public char winner(User user) throws RemoteException {
        Room myRoom = getRoom(user);
        return myRoom.winner;
    }

    @Override
    public int[] getStats(String ID) throws RemoteException {
        Room room = roomList.stream().filter(Room -> Room.roomID.equals(ID)).findFirst().orElse(null);

        if(room != null)    {
            if(room.users.get(0) != null)
                return room.users.get(0).statistic;
            else if(room.users.get(1) != null)
                return room.users.get(1).statistic;
        }
        return null;
    }

    @Override
    public void ping(User user) throws RemoteException {
        User myUser = userList.stream().filter(User -> User.ID == user.ID).findFirst().orElse(null);
        if (myUser != null) {
            myUser.setConnected(true);
        }
    }

    public Room getRoom (User user){
        return roomList.stream().filter(Room -> Room.users.stream().anyMatch(User -> User.ID == user.ID))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void run (){

        currentRoom.users.get(0).setStatistic(new int[]{0, 0, 0});
        currentRoom.users.get(1).setStatistic(new int[]{0, 0, 0});
        currentRoom.users.get(0).setTable(new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}});
        currentRoom.users.get(1).setTable(new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}});

        while (currentRoom.users.size() == 2)   {

            currentRoom.setBoard(new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}});
            currentRoom.setWinner('-');
            setTurns();

            while(currentRoom.users.size() == 2 && !isRunning()) {

                if (currentRoom.users.size() == 2 && currentRoom.users.get(0) != null)
                    if( currentRoom.users.get(0).hasStarted) {
                        currentRoom.setBoard(Objects.requireNonNull(currentRoom.users.get(0).table));
                        Objects.requireNonNull(currentRoom).users.get(0).setHasStarted(false);
                        Objects.requireNonNull(currentRoom).users.get(1).setMyTurn(true);
                }

                if (currentRoom.users.size() == 2 && currentRoom.users.get(1) != null)
                    if(currentRoom.users.get(1).hasStarted) {
                    currentRoom.setBoard(Objects.requireNonNull(currentRoom.users.get(1).table));
                    Objects.requireNonNull(currentRoom).users.get(1).setHasStarted(false);
                    Objects.requireNonNull(currentRoom).users.get(0).setMyTurn(true);
                }

            }
            if(currentRoom.users.size() == 2 && currentRoom.users.get(0) != null && currentRoom.users.get(1) != null) {
                makeStatistics();
                swapSigns();
                currentRoom.users.get(0).setMyTurn(false);
                currentRoom.users.get(1).setMyTurn(false);
                currentRoom.users.get(0).setHasStarted(false);
                currentRoom.users.get(1).setHasStarted(false);
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        }


    }

    private void setTurns (){

        if(currentRoom.users.get(0).sign == 'X')  {
            currentRoom.users.get(0).setMyTurn(true);
            currentRoom.users.get(1).setMyTurn(false);
        }   else {
            currentRoom.users.get(0).setMyTurn(false);
            currentRoom.users.get(1).setMyTurn(true);
        }

    }
    private void makeStatistics (){

        if(currentRoom.users.get(0) != null && currentRoom.winner == currentRoom.users.get(0).sign)    {

            int[] statistic1 = currentRoom.users.get(0).getStatistic();
            int[] statistic2 = currentRoom.users.get(1).getStatistic();
            statistic1[0] += 1;
            statistic2[2] += 1;
            currentRoom.users.get(0).setStatistic(statistic1);
            currentRoom.users.get(1).setStatistic(statistic2);

        } else if (currentRoom.users.get(1) != null && currentRoom.winner == currentRoom.users.get(1).sign) {

            int[] statistic1 = currentRoom.users.get(0).getStatistic();
            int[] statistic2 = currentRoom.users.get(1).getStatistic();
            statistic1[2] += 1;
            statistic2[0] += 1;
            currentRoom.users.get(0).setStatistic(statistic1);
            currentRoom.users.get(1).setStatistic(statistic2);

        }   else if (currentRoom.users.get(0) != null && currentRoom.users.get(1) != null){

            int[] statistic1 = currentRoom.users.get(0).getStatistic();
            int[] statistic2 = currentRoom.users.get(1).getStatistic();
            statistic1[1] += 1;
            statistic2[1] += 1;
            currentRoom.users.get(0).setStatistic(statistic1);
            currentRoom.users.get(1).setStatistic(statistic2);
        }
    }
    private static void swapSigns (){
        char signOne = currentRoom.users.get(0).sign;
        char signTwo = currentRoom.users.get(1).sign;
        currentRoom.users.get(0).setSign(signTwo);
        currentRoom.users.get(1).setSign(signOne);
    }

    public boolean isRunning (){

        if(!isWin())
            return isDraw();
        else
            return isWin();
    }

    private static boolean isWin (){

        for (int i = 0; i < 3; i++) {
            if ((currentRoom.board[i][0] == 'X' && currentRoom.board[i][1] == 'X' && currentRoom.board[i][2] == 'X') ||   //vertical
                    (currentRoom.board[0][i] == 'X' && currentRoom.board[1][i] == 'X' && currentRoom.board[2][i] == 'X')) { //horizontal
                currentRoom.setWinner('X');
                return true;
            }

            if ((currentRoom.board[i][0] == 'O' && currentRoom.board[i][1] == 'O' && currentRoom.board[i][2] == 'O') ||   //vertical
                    (currentRoom.board[0][i] == 'O' && currentRoom.board[1][i] == 'O' && currentRoom.board[2][i] == 'O')) { //horizontal
                currentRoom.setWinner('O');
                return true;
            }

        }

        if ((currentRoom.board[0][0] == 'X' && currentRoom.board[1][1] == 'X' && currentRoom.board[2][2] == 'X') ||   //cross
                (currentRoom.board[0][2] == 'X' && currentRoom.board[1][1] == 'X' && currentRoom.board[2][0] == 'X')) {
            currentRoom.setWinner('X');
            return true;
        }

        if ((currentRoom.board[0][0] == 'O' && currentRoom.board[1][1] == 'O' && currentRoom.board[2][2] == 'O') ||   //cross
                (currentRoom.board[0][2] == 'O' && currentRoom.board[1][1] == 'O' && currentRoom.board[2][0] == 'O')) {
            currentRoom.setWinner('O');
            return true;
        }

        return false;
    }

    private static boolean isDraw (){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (currentRoom.board[i][j] == ' ') {
                    return false;
                }
            }
        }
        currentRoom.setWinner(' ');
        return true;
    }
}