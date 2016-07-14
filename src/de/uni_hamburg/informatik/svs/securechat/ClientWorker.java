package de.uni_hamburg.informatik.svs.securechat;

import javax.net.ssl.SSLSocket;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

class ClientWorker implements Runnable {

    private static final List<ClientWorker> CLIENT_WORKERS = new ArrayList<>();

    private SSLSocket client;
    private JTextArea textArea;
    private PrintWriter out;
    private BufferedReader in;

    ClientWorker(SSLSocket client, JTextArea textArea) {
        this.client = client;
        this.textArea = textArea;
        out = null;
        in = null;
    }

    @Override
    public void run() {
        String line;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("in or out failed: " + e.getMessage());
            System.exit(-1);
        }

        synchronized (CLIENT_WORKERS) {
            if (!CLIENT_WORKERS.contains(this)) {
                CLIENT_WORKERS.add(this);
            }
        }

        boolean running = true;
        while (running) {
            try {
                line = in.readLine();
                //Send data back to client
                if (line != null) {
                    broadcastMessage(line);
                }
                textArea.append(line);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                running = false;
            }
        }
    }

    private synchronized void broadcastMessage(String line) {
        CLIENT_WORKERS.forEach(client -> client.out.println(line));
    }
}
