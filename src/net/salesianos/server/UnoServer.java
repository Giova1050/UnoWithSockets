package net.salesianos.server;

import java.io.*;
import java.net.*;
import java.util.*;

import net.salesianos.client.Deck;

public class UnoServer {
    private static final int PORT = 12345;
    public static Deck deck = new Deck();
    private static List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>());
    private static String currentCard;
    private static int currentPlayerIndex = 0;
    private static boolean gameRunning = true;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor UNO iniciado...");
            
            currentCard = deck.drawCard(); 
            System.out.println("Carta inicial: " + currentCard);
            
            while (players.size() < 2) {
                Socket socket = serverSocket.accept();
                ClientHandler player = new ClientHandler(socket, players.size());
                players.add(player);
                new Thread(player).start();
            }
            
            Thread.sleep(1000);
            broadcast("Carta inicial en juego: " + currentCard);
            broadcast("Turno del jugador " + currentPlayerIndex);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized static boolean playCard(String card, int playerIndex) {
        if (!gameRunning) {
            return false;
        }

        if (playerIndex != currentPlayerIndex) {
            players.get(playerIndex).sendMessage("No es tu turno.");
            return false;
        }
        
        if (card.equalsIgnoreCase("robar")) {
            String newCard = deck.drawCard();
            players.get(playerIndex).hand.add(newCard);
            players.get(playerIndex).sendMessage("Has robado una carta: " + newCard);
            players.get(playerIndex).sendMessage("Tus cartas actualizadas: " + players.get(playerIndex).hand);
            return false;
        }
        
        String[] currentParts = currentCard.split(" ");
        String[] newParts = card.split(" ");
        
        if (newParts.length < 2) {
            players.get(playerIndex).sendMessage("Formato de carta inválido. Debe ser 'Color Valor'.");
            return false;
        }

        if (!players.get(playerIndex).hand.contains(card)) {
            players.get(playerIndex).sendMessage("No tienes esa carta en tu mano.");
            return false;
        }

        if (newParts[0].equals(currentParts[0]) || newParts[1].equals(currentParts[1])) {
            currentCard = card;
            players.get(playerIndex).hand.remove(card);
            broadcast("Nueva carta en juego: " + currentCard);
            
            if (players.get(playerIndex).hand.isEmpty()) {
                broadcast("¡Ha ganado el jugador " + playerIndex + "!");
                broadcast("La partida ha terminado.");
                gameRunning = false;
                return true;
            }
            
            currentPlayerIndex = (currentPlayerIndex + 1) % 2;
            broadcast("Jugador " + playerIndex + " ha jugado: " + currentCard);
            broadcast("Turno del jugador " + currentPlayerIndex);
            players.get(playerIndex).sendMessage("Tus cartas restantes: " + players.get(playerIndex).hand);
            return true;
        } else {
            players.get(playerIndex).sendMessage("Carta inválida. Debes jugar una carta del mismo color o número.");
            return false;
        }
    }

    private static void broadcast(String message) {
        for (ClientHandler player : players) {
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int playerIndex;
        private List<String> hand;

        public ClientHandler(Socket socket, int playerIndex) {
            this.socket = socket;
            this.playerIndex = playerIndex;
            this.hand = new ArrayList<>();
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                for (int i = 0; i < 7; i++) {
                    hand.add(deck.drawCard());
                }
                
                sendMessage("Bienvenido, jugador " + playerIndex);
                sendMessage("Tus cartas: " + hand);
                
                while (gameRunning) {
                    String input = in.readLine();
                    if (input == null || input.equalsIgnoreCase("salir")) {
                        break;
                    }
                    
                    while (gameRunning && !playCard(input, playerIndex)) {
                        sendMessage("Si no puedes jugar, escribe 'robar' para tomar una carta.");
                        input = in.readLine();
                        if (input.equalsIgnoreCase("salir")) {
                            break;
                        }
                    }
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
}
