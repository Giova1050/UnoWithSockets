package net.salesianos.server.thread;

import java.io.*;
import java.net.*;
import java.util.*;

import net.salesianos.server.UnoServer;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private List<String> hand = new ArrayList<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            for (int i = 0; i < 7; i++) {
                hand.add(UnoServer.deck.drawCard());
            }
            sendMessage("Tus cartas: " + hand);

            while (true) {
                String input = in.readLine();
                if (input == null || input.equalsIgnoreCase("salir")) {
                    break;
                }
                sendMessage("Comando recibido: " + input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
