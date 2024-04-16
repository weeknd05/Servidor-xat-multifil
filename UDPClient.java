import java.io.*;
import java.net.*;

public class UDPClient implements Runnable {
    private static final int SERVER_PORT = 7778; // Puerto del servidor
    private static final String SERVER_ADDRESS = "localhost"; // Dirección del servidor
    private DatagramSocket socket;
    private InetAddress serverIPAddress;
    private String name; // Nombre del usuario

    public UDPClient(String name) {
        this.name = name;
        try {
            this.socket = new DatagramSocket();
            this.serverIPAddress = InetAddress.getByName(SERVER_ADDRESS);
        } catch (IOException e) {
            System.out.println("Error al crear el socket UDP o resolver la dirección del servidor: " + e.getMessage());
        }
    }

    public void sendLoginRequest() {
        String loginMessage = "LOGIN " + name;
        sendMessage(loginMessage);
    }

    public void sendMessage(String message) {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, SERVER_PORT);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    public void run() {
        byte[] buffer = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println(receivedMessage); // Muestra el mensaje recibido en la consola
            } catch (IOException e) {
                System.out.println("Error al recibir un paquete: " + e.getMessage());
                break;
            }
        }
    }

    public void close() {
        socket.close();
        System.out.println("Conexión cerrada.");
    }

    public static void main(String[] args) {
        System.out.println("Por favor, proporciona tu nombre de usuario como argumento.");

        UDPClient client = new UDPClient(args[0]);
        Thread thread = new Thread(client);
        thread.start();
        
        // Mantén la consola interactiva para enviar mensajes
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Cliente UDP iniciado. Escribe tus mensajes (escribe 'SALIR' para terminar):");
        client.sendLoginRequest(); // Enviar solicitud de inicio de sesión
        
        try {
            String line;
            while (!(line = reader.readLine()).equalsIgnoreCase("SALIR")) {
                client.sendMessage(client.name + ": " + line);
            }
        } catch (IOException e) {
            System.out.println("Error al leer de la consola: " + e.getMessage());
        } finally {
            client.close();
        }
    }
}
