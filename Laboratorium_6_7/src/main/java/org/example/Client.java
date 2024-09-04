package org.example;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Client {
    static private JComboBox<Room> rooms;
    static private JPanel buttons;
    static private JLabel labelWins, labelDraws, labelLosses;
    static IServer server;
    static User myUser;
    static boolean isRunning;

    public static void main(String[] args) {
        try {
            server = (IServer) Naming.lookup("rmi://localhost:2137/Server");
            server.connect("Connected");
            myUser = server.logIN();
        } catch (Exception e) {
            System.out.println(e);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setTitle("Tic Tac Toe");
                frame.setSize(450, 600);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

                // Przerwa
                frame.add(Box.createVerticalStrut(25));

                // Napis "Tic Tac Toe"
                JLabel titleLabel = new JLabel("Tic Tac Toe");
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Ustawienia wielkości
                frame.add(titleLabel);

                // Przerwa
                frame.add(Box.createVerticalStrut(25));

                // Combobox z marginesem
                try {
                    comboBoxWorker();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                rooms.setMaximumSize(new Dimension(150, 30)); // Ustawienia szerokości i wysokości
                rooms.setAlignmentX(Component.CENTER_ALIGNMENT);
                rooms.setBorder(new EmptyBorder(5, 2, 5, 2)); // Dodanie marginesów
                frame.add(rooms);

                // Przerwa
                frame.add(Box.createVerticalStrut(10));

                // Siatka kwadratowych buttonów 3x3
                buttons = new JPanel(new GridLayout(3, 3));
                buttons.setBorder(new EmptyBorder(10, 10, 10, 10)); // Dodanie marginesów
                for (int i = 1; i <= 9; i++) {
                    JButton button = new JButton();
                    button.setPreferredSize(new Dimension(80, 80)); // Ustawienia wielkości
                    buttons.add(button);
                }
                frame.add(buttons);

                // Przerwa
                frame.add(Box.createVerticalStrut(5));

                // JLabele
                labelWins = new JLabel("Wins: 0");
                labelDraws = new JLabel("Draws: 0");
                labelLosses = new JLabel("Losses: 0");

                labelWins.setFont(new Font("Arial", Font.PLAIN, 16)); // Ustawienia czcionki
                labelDraws.setFont(new Font("Arial", Font.PLAIN, 16)); // Ustawienia czcionki
                labelLosses.setFont(new Font("Arial", Font.PLAIN, 16)); // Ustawienia czcionki

                JPanel labelsPanel = new JPanel();
                labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.X_AXIS));
                labelsPanel.add(labelWins);
                labelsPanel.add(Box.createHorizontalStrut(10)); // Dodanie przerwy
                labelsPanel.add(labelDraws);
                labelsPanel.add(Box.createHorizontalStrut(10)); // Dodanie przerwy
                labelsPanel.add(labelLosses);
                frame.add(labelsPanel);

                // Przerwa
                frame.add(Box.createVerticalStrut(10));

                // Buttony
                JButton joinButton = new JButton("  Join Room  ");
                JButton createButton = new JButton("Create Room");
                JButton observeButton = new JButton("Observe Room");

                JPanel buttonsPanel2 = new JPanel();
                buttonsPanel2.setLayout(new BoxLayout(buttonsPanel2, BoxLayout.X_AXIS));
                buttonsPanel2.add(joinButton);
                buttonsPanel2.add(Box.createHorizontalStrut(20));
                buttonsPanel2.add(createButton);
                buttonsPanel2.add(Box.createHorizontalStrut(20));
                buttonsPanel2.add(observeButton);
                frame.add(buttonsPanel2);

                joinButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            join();
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });

                createButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            create();
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });

                observeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            observe();
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });

                // Przerwa
                frame.add(Box.createVerticalStrut(10));

                frame.setVisible(true);

                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {

                        try {
                            server.logOut(myUser);
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                        System.exit(0);
                    }
                });
            }
        });
    }

    public static void join() throws RemoteException {
        Room selectedRoom = (Room) rooms.getSelectedItem();
        if (selectedRoom != null) {
            server.joinRoom(myUser, selectedRoom.roomID);
            server.ping(myUser);
            myUser.setTable(new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}});
            JOptionPane.showMessageDialog(null, "Joined Room" + selectedRoom.roomID, "Information", JOptionPane.INFORMATION_MESSAGE);
            updateTable();
            isRunning = false;
            labelWins.setText("Wins: 0");
            labelDraws.setText("Draws: 0");
            labelLosses.setText("Losses: 0");
        }

    }

    public static void create() throws RemoteException {
        server.createRoom(myUser);
        server.ping(myUser);
        myUser.setTable(new char[][]{{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}});
        JOptionPane.showMessageDialog(null, "Room created", "Information", JOptionPane.INFORMATION_MESSAGE);
        updateTable();
        isRunning = false;
        labelWins.setText("Wins: 0");
        labelDraws.setText("Draws: 0");
        labelLosses.setText("Losses: 0");
    }

    public static void observe() throws RemoteException {
        isRunning = true;
        Room selectedRoom = (Room) rooms.getSelectedItem();

        SwingWorker<Void, Void> observer = new SwingWorker<Void, Void>() {
            private Room pickedRoom;
            @Override
            protected Void doInBackground() throws Exception {
                while (isRunning) {
                    try (Socket socket = new Socket("localhost", 4200);
                         ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

                        ArrayList<Room> roomsList = (ArrayList<Room>) inputStream.readObject();
                        server.ping(myUser);
                        pickedRoom = roomsList.stream().filter(room -> {
                            assert selectedRoom != null;
                            return room.roomID.equals(selectedRoom.roomID);
                        }).findFirst().orElse(null);

                        if (pickedRoom == null) {
                            break;
                        }
                        publish();
                        if(server.getStats(selectedRoom.roomID) != null)    {
                            int [] wins = server.getStats(selectedRoom.roomID);
                            labelWins.setText("Player 1 wins: " + wins[0] + "   ");
                            labelDraws.setText("Draws: " + wins[1] + "   ");
                            labelLosses.setText("Player 2 wins: " + wins[2] + "   ");
                        }
                        Thread.sleep(1500);

                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Void> chunks) {
                buttons.removeAll();

                if (pickedRoom != null) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            JButton button = new JButton(String.valueOf(pickedRoom.board[i][j]));
                            button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 35));
                            buttons.add(button);
                        }
                    }
                    SwingUtilities.invokeLater(() -> {
                        buttons.revalidate();
                        buttons.repaint();
                    });
                }

            }
        };
        observer.execute();
    }

    static void comboBoxWorker() throws RemoteException {

        rooms = new JComboBox<>(server.getRoomList().toArray(new Room[0]));
        rooms.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Room room) {
                    if (room.users.isEmpty())
                        value = "Room " + room.roomID + " (0 users)";
                    else if (room.users.size() == 1)
                        value = "Room " + room.roomID + " (1 user)";
                    else
                        value = "Room " + room.roomID + " (2 users)";
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        SwingWorker<Void, ArrayList<Room>> comboBoxWorker = new SwingWorker<Void, ArrayList<Room>>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (true) {
                    try (Socket socket = new Socket("localhost", 4200);
                         ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
                        ArrayList<Room> roomsList = (ArrayList<Room>) inputStream.readObject();
                        publish(roomsList); // Przesyłaj listę pokoi
                        server.ping(myUser);
                        Thread.sleep(2000);

                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            protected void process(java.util.List<ArrayList<Room>> chunks) {

                ArrayList<Room> latestRoomsList = chunks.get(chunks.size() - 1);
                Room selectedRoom = (Room) rooms.getSelectedItem();
                checkOpponent();
                try {
                    server.ping(myUser);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                rooms.setModel(new DefaultComboBoxModel<>(latestRoomsList.toArray(new Room[0])));
                if (selectedRoom != null) {
                    Room matchingRoom = latestRoomsList.stream()
                            .filter(Room -> Room.roomID.equals(selectedRoom.roomID))
                            .findFirst()
                            .orElse(null);

                    if (matchingRoom != null) {
                        rooms.setSelectedItem(matchingRoom);
                    }
                }

                rooms.repaint();
                rooms.revalidate();
            }
        };

        comboBoxWorker.execute();
    }

    public static void checkOpponent() {
        try {
            if (server.waitForUser(myUser) && !myUser.busy) {
                playGame();
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void playGame() throws RemoteException {
        myUser.setMyTurn(false);
        myUser.setBusy(true);
        server.ping(myUser);
        updateTable();
        SwingWorker<Void, Void> gameWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {

                while (server.waitForUser(myUser)) {
                    server.ping(myUser);
                    myUser.setSign(server.getSign(myUser));
                    while (server.waitForUser(myUser) && server.winner(myUser) == '-') {
                        publish();

                        Thread.sleep(100);
                    }
                    publish();

                    int[] wins = server.getWins(myUser);
                    labelWins.setText("Wins: " + wins[0]);
                    labelDraws.setText("Draws: " + wins[1]);
                    labelLosses.setText("Losses: " + wins[2]);
                }
                myUser.setBusy(false);
                return null;
            }

            @Override
            protected void process(java.util.List<Void> chunks) {
                try {
                    myUser.setMyTurn(server.checkMyTurn(myUser));
                    myUser.setTable(server.getBoard(myUser));
                    updateTable();
                    server.ping(myUser);

                } catch (Exception exception) {
                    System.out.println(exception);
                }
            }
        };

        gameWorker.execute();


    }

    public static void updateTable() {
        buttons.removeAll();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JButton button = createButton(i, j);
                buttons.add(button);
            }
        }
        buttons.revalidate();
        buttons.repaint();
    }



    private static JButton createButton(int i, int j) {
        JButton button = new JButton(String.valueOf(myUser.table[i][j]));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 35));


        if (myUser.myTurn && myUser.table[i][j] == ' ')   {
            final int row = i;
            final int col = j;
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    myUser.table[row][col] = myUser.sign;
                    updateTable();
                    try {
                        server.move(myUser);
                        server.ping(myUser);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    myUser.setHasStarted(true);
                    myUser.setMyTurn(false);
                }
            });
        }

        return button;
    }
}
