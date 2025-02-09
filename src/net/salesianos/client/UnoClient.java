package net.salesianos.client;

import java.io.*;
import java.net.*;

public class UnoClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8085);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor UNO.");

            // Hilo para recibir mensajes del servidor
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Enviar mensajes al servidor
            while (true) {
                System.out.println("Escribe tu carta a jugar o escribe 'salir' para terminar: \n");
                String input = userInput.readLine();
                if (input != null && !input.trim().isEmpty()) {
                    out.println(input);
                    if (input.equalsIgnoreCase("salir")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
