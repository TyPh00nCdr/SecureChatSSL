package de.uni_hamburg.informatik.svs.securechat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

class SocketClient extends JFrame implements ActionListener {


    private JButton button;
    private JTextField textField;
    private PrintWriter out;
    private BufferedReader in;

    private SocketClient() {
        JLabel text = new JLabel("Text to send over socket:");
        textField = new JTextField(20);
        button = new JButton("Click Me");
        button.addActionListener(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.white);
        getContentPane().add(panel);
        panel.add("North", text);
        panel.add("Center", textField);
        panel.add("South", button);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == button) {
            // Send data over socket
            String text = textField.getText();
            out.println(text);
            textField.setText("");
            // Receive text from server
            try {
                String line = in.readLine();
                System.out.println("Text received: " + line);
            } catch (IOException e) {
                System.err.println("Read failed: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private void listenSocket() {
        //Create socket connection
        try {
            // socket = new Socket("kq6py", 4444);
            Socket socket = new Socket("localhost", 4444);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: kq6py.eng" + "\n" + "Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("No I/O: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        SocketClient frame = new SocketClient();
        frame.setTitle("Client Program");
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };

        frame.addWindowListener(l);
        frame.pack();
        frame.setVisible(true);
        frame.listenSocket();
    }
}
