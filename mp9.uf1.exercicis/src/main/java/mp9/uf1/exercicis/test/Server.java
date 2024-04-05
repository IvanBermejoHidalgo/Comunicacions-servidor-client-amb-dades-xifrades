package mp9.uf1.exercicis.test;

import mp9.uf1.cryptoutils.MyCryptoUtils;
import java.io.*;
import java.net.*;
import java.security.*;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(00005);
            System.out.println("Servidor esperando conexiones...");
            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado");

            // Generar un par de claves RSA para el servidor
            KeyPair serverKeyPair = MyCryptoUtils.randomGenerate(2048);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Enviar la clave pública al cliente
            out.writeObject(serverKeyPair.getPublic());

            // Recibir la clave pública del cliente
            PublicKey clientPublicKey = (PublicKey) in.readObject();

            // Thread para leer mensajes del cliente
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        // Recibir mensaje encriptado del cliente
                        byte[] encryptedData = (byte[]) in.readObject();

                        // Descifrar el mensaje recibido
                        byte[] decryptedData = MyCryptoUtils.decryptData(encryptedData, serverKeyPair.getPrivate());
                        System.out.println("Cliente: " + new String(decryptedData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            // Thread para escribir mensajes al cliente
            Thread writeThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
                        // Leer mensaje desde la consola
                        System.out.print("Servidor: ");
                        String msg = reader.readLine();

                        // Encriptar el mensaje con la clave pública del cliente
                        byte[] encryptedData = MyCryptoUtils.encryptData(msg.getBytes(), clientPublicKey);
                        out.writeObject(encryptedData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            writeThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
