package mp9.uf1.exercicis.test;

import mp9.uf1.cryptoutils.MyCryptoUtils;
import java.io.*;
import java.net.*;
import java.security.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 00005);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Generar un par de claves RSA para el cliente
            KeyPair clientKeyPair = MyCryptoUtils.randomGenerate(2048);
            PublicKey clientPublicKey = clientKeyPair.getPublic();
            PrivateKey clientPrivateKey = clientKeyPair.getPrivate();

            // Enviar la clave pública al servidor
            out.writeObject(clientPublicKey);

            // Recibir la clave pública del servidor
            PublicKey serverPublicKey = (PublicKey) in.readObject();

            // Thread para leer mensajes del servidor
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        // Recibir mensaje encriptado del servidor
                        byte[] encryptedData = (byte[]) in.readObject();

                        // Descifrar el mensaje recibido
                        byte[] decryptedData = MyCryptoUtils.decryptData(encryptedData, clientPrivateKey);
                        System.out.println("Servidor: " + new String(decryptedData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            // Thread para escribir mensajes al servidor
            Thread writeThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
                        // Leer mensaje desde la consola
                        System.out.print("Cliente: ");
                        String msg = reader.readLine();

                        // Encriptar el mensaje con la clave pública del servidor
                        byte[] encryptedData = MyCryptoUtils.encryptData(msg.getBytes(), serverPublicKey);
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
