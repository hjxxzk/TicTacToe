package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

public class TicTacToeBoard extends JFrame {
    private final JPanel boardPanel;
    private final TicTacToeService game;
    private final Player player;
    public TicTacToeBoard(TicTacToeService game, Player player) {

        this.game = game;
        this.player = player;

        setTitle("Tic Tac Toe");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3));
        add(boardPanel);
        setVisible(true);
        setLocationRelativeTo(null);
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//
//                try {
//                    game.logOut(player);
//                } catch (RemoteException ex) {
//                    throw new RuntimeException(ex);
//                }
//                System.err.println("Closing the application...");
//
//                System.exit(0);
//            }
//        });
    }

    public void updateBoard(char[][] board, boolean turn, Player player, TicTacToeService game) {
        boardPanel.removeAll();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JButton button = createButton(board, i, j, turn, player, game);
                boardPanel.add(button);
            }
        }
        revalidate();
        repaint();
    }



    private JButton createButton(char[][] board, int i, int j, boolean turn, Player player, TicTacToeService game) {
        JButton button = new JButton(String.valueOf(board[i][j]));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));

        if (turn)   {
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    board[i][j] = player.getSign();
                    player.board[i][j] = player.getSign();
                    updateBoard(board, false, player, game);
                    try {
                        game.makeMove(player);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    player.setMoveMade(true);
                    player.setIsMyTurn(isMyTurn.NO);

                }
            });
        }

        return button;
    }

}
