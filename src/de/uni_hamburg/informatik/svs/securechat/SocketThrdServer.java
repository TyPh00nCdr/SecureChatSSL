package de.uni_hamburg.informatik.svs.securechat;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.ServerSocket;

class SocketThrdServer extends JFrame {

    private static final int PORT = 4444;

    private JTextArea textArea;
    private SSLServerSocket server;

    private SocketThrdServer() {
        textArea = new JTextArea();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.white);
        getContentPane().add(panel);
        panel.add("North", new JLabel("Text received over socket:"));
        panel.add("Center", textArea);
    }

    private void listenSocket() {
        try {
            server = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(PORT);
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT + "\n" + "Error: " + e.getMessage());
            System.exit(-1);
        }
        while (true) {
            ClientWorker w;
            try {
                w = new ClientWorker((SSLSocket) server.accept(), textArea);
                Thread t = new Thread(w);
                t.start();
            } catch (IOException e) {
                System.err.println("Accept failed: " + PORT + "\n" + "Error: " + e.getMessage());
                System.exit(-1);
            }
        }
    }

    @Override
    protected void finalize() {
        // Objects created in run method are finalized when
        // program terminates and thread exits
        try {
            server.close();
            super.finalize();
        } catch (IOException e) {
            System.err.println("Could not close socket: " + e.getMessage());
            System.exit(-1);
        } catch (Throwable e) {
            System.err.println("Could not free all resources: " + e.getMessage());
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        SocketThrdServer frame = new SocketThrdServer();
        frame.setTitle("Server Program");
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
