package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

public class TicTacToeClient {
    public static void main(String[] args) {
        ask();
    }

    public static void ask()   {

        JFrame frame = new JFrame("Game Options");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 100);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton playButton = new JButton("Play");
        JButton observeButton = new JButton("Observe");
        Dimension buttonSize = new Dimension(120, 40);
        playButton.setPreferredSize(buttonSize);
        observeButton.setPreferredSize(buttonSize);

        panel.add(playButton);
        panel.add(observeButton);
        frame.add(panel);

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();

                try {
                    TicTacToeService game = (TicTacToeService) Naming.lookup("rmi://localhost:1099/TicTacToeService");
                    game.start("Success");
                    Player player = game.begin();

                    if (!game.showRoomList().isEmpty()) {
                        findYourRoom(game, player);

                    } else {
                        System.err.println("No room available. Creating 'Room 1'.");
                        game.createRoom(player);
                        TicTacToeBoard board = new TicTacToeBoard(game, player);
                        while (!game.waitForRoom(player)) {     //wait for other player
                            System.err.println("Looking for a player...");
                            Thread.sleep(1000);
                        }
                        System.err.println("Player found!");
                        playGame(player, game, board);
                    }

                } catch (Exception exception)   {
                    System.out.println(exception);
                }

            }
        });

        observeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();

                try {
                    Socket socket = new Socket("localhost", 1098);
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());



                } catch (IOException exception) {
                    exception.printStackTrace();
                }

            }
        });

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void findYourRoom(TicTacToeService game, Player player) throws RemoteException {

        JFrame frame = new JFrame("Room Selection");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 200);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);



            JComboBox<Room> roomComboBox = new JComboBox<>(game.showRoomList().toArray(new Room[0]));
            makeRoomsList(roomComboBox);

            JButton joinButton = new JButton("Join room");
            JLabel orLabel = new JLabel("or");
            JButton createButton = new JButton("Create a room");

            joinButton.setMargin(new Insets(5, 10, 5, 10));
            createButton.setMargin(new Insets(5, 10, 5, 10));

            joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            createButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(roomComboBox);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(joinButton);
            panel.add(orLabel);
            panel.add(createButton);

            joinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Room selectedRoom = (Room) roomComboBox.getSelectedItem();
                    if (selectedRoom != null) {
                        System.err.println("Joining Room " + selectedRoom.roomID);
                        try {
                            game.joinRoom(player, String.valueOf(selectedRoom.roomID));
                            TicTacToeBoard board = new TicTacToeBoard(game, player);
                            playGame(player, game, board);
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                        frame.dispose();


                    }
                }
            });

            createButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        game.createRoom(player);

                    System.err.println("Creating a room " + game.findMyRoom(player).roomID);
                    frame.dispose();
                    TicTacToeBoard board = new TicTacToeBoard(game, player);
                        while (!game.waitForRoom(player)) {     //wait for other player
                            System.err.println("Looking for a player...");
                            Thread.sleep(1000);
                        }
                        System.err.println("Player found!");

                        playGame(player, game, board);
                    } catch (RemoteException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            frame.add(panel);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
    }

    public static void makeRoomsList(JComboBox<Room> roomComboBox) {
        roomComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Room) {
                    value = "Room " + ((Room) value).roomID;
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
    }

    public static void playGame(Player player, TicTacToeService game, TicTacToeBoard board) {

        try {
            System.err.println("Waiting for game to begin...");
            player.setSign(game.getSign(player)); //set sign

            while (!game.isInProgress(player))  {
                Thread.sleep(100);
            }

            if(!game.isThereSomeone(player)) {
                cls();
                board.dispose();
                findYourRoom(game, player);
            }
        //    System.out.println(player.sign);
            System.err.println("Game has started");

            SwingWorker<Void, Void> gameWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    player.setIsMyTurn(game.canIMakeMove(player));
                  Thread.sleep(1000);
                    if (player.isMyTurn.equals(isMyTurn.YES)) {
                        System.out.println("You start");
                        checkOtherPlayer(game, player, board);
                    } else {
                        System.out.println("Your opponent starts");
                        checkOtherPlayer(game, player, board);
                    }

                    checkOtherPlayer(game, player, board);

                    while (game.isInProgress(player)) {
                       checkOtherPlayer(game, player, board);
                        //      System.out.println(game.isInProgress(player));
                        publish();

                        Thread.sleep(100);
                    }
                    publish();
                    System.out.println(game.whoWon(player) + " wins!");

                    System.out.println("Do you want to play again? (Y/N): ");
                    Scanner scanner = new Scanner(System.in);
                    String answer = scanner.nextLine();

                    if(answer.equalsIgnoreCase("Y"))    {
                        cls();
                        if(!game.isThereSomeone(player)) {
                            board.dispose();
                            findYourRoom(game, player);
                        }   else {
                            game.wantToPlay(player);
                            board.updateBoard(new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}}, false, player, game);
                            playGame(player, game, board);
                        }
                    }   else {
                        board.dispose();
                        game.logOut(player);
                        System.err.println("Closing the application...");
                    }

                    return null;
                }

                @Override
                protected void process(java.util.List<Void> chunks) {
                    try {
                        checkOtherPlayer(game, player, board);
                        player.setIsMyTurn(game.canIMakeMove(player));
                        checkOtherPlayer(game, player, board);
                        player.setBoard(game.display(player));

                        if (player.isMyTurn.equals(isMyTurn.YES)) {
                            checkOtherPlayer(game, player, board);
                            board.updateBoard(game.display(player), true, player, game);

                        } else {
                            checkOtherPlayer(game, player, board);
                            board.updateBoard(game.display(player), false, player, game);
                        }
                        checkOtherPlayer(game, player, board);
                    } catch (Exception exception) {
                        System.out.println(exception);
                    }
                }
            };

            gameWorker.execute();

        } catch (Exception e)   {
            System.out.println(e);
        }
    }

    public static void checkOtherPlayer(TicTacToeService game, Player player, TicTacToeBoard board)   {
        try {
            if(!game.isThereSomeone(player)) {
                board.dispose();
                player.setBoard(new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}});
                findYourRoom(game, player);
                System.err.println("Opponent left.");
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public static void cls() throws IOException, InterruptedException   {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "cls");
        Process process = processBuilder.inheritIO().start();
        process.waitFor();
    }

}

