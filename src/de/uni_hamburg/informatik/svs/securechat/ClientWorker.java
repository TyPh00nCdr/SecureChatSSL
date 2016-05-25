package de.uni_hamburg.informatik.svs.securechat;

import de.uni_hamburg.informatik.svs.passwordhash.Useradmin;
import de.uni_hamburg.informatik.svs.passwordhash.Useradministration;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

class ClientWorker implements Runnable {

    private static final Map<String, ClientWorker> CLIENT_WORKERS = new HashMap<>();
    private static final Map<InetAddress, Date> LAST_LOGIN_TRY = new HashMap<>();
    private static final Useradministration USER_ADMIN = new Useradmin();

    private Socket client;
    private JTextArea textArea;
    private PrintWriter out;
    private BufferedReader in;

    ClientWorker(Socket client, JTextArea textArea) {
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

        boolean authenticated;
        do {
            authenticated = authenticateUser();
            if (authenticated) {
                out.println("You are now logged in");
            } else {
                out.println("Authentication failed");
            }
        } while (!authenticated);

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

    private boolean authenticateUser() {
        boolean authenticated = false;

        try {
            out.println("Username:");
            String username = in.readLine();
            out.println("Password:");
            // Unsichere Lösung, da das Password als Klartext-String übergeben wird! Bei dieser einfachen Implementation
            // jedoch nicht ohne großen Aufwand änderbar!
            String readPassword = in.readLine();
            char[] password = (readPassword != null) ? readPassword.toCharArray() : new char[]{};
            synchronized (USER_ADMIN) {
                authenticated = USER_ADMIN.checkUser(username, password);
            }
            Arrays.fill(password, ' ');

            synchronized (CLIENT_WORKERS) {
                if (!CLIENT_WORKERS.containsKey(username) && authenticated) {
                    CLIENT_WORKERS.put(username, this);
                } else {
                    authenticated = false;
                }
            }
        } catch (IOException e) {
            System.err.println("I/O: " + e.getMessage());
        }

        // check time since last login
        synchronized (LAST_LOGIN_TRY) {
            Date lastLogin = LAST_LOGIN_TRY.get(client.getInetAddress());
            Date now = new Date();
            if (lastLogin != null && now.getTime() - lastLogin.getTime() <= 1000) {
                out.println("Please wait another " + (now.getTime() - lastLogin.getTime()) + "ms until next attempt!");
                authenticated = false;
            } else {
                LAST_LOGIN_TRY.remove(client.getInetAddress());
                LAST_LOGIN_TRY.put(client.getInetAddress(), now);
            }
        }

        return authenticated;
    }

    private synchronized void broadcastMessage(String line) {
        CLIENT_WORKERS.values().forEach(client -> client.out.println(line));
    }
}
