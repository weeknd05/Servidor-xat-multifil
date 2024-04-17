import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDPClient implements Runnable {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private boolean loggedIn = false;
    private String clientName;

    public UDPClient(String address, int port) throws UnknownHostException, SocketException {
        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName(address);
        this.port = port;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (!loggedIn) {
            System.out.println("Hola! Para usar este chat, necesitas iniciar sesión.");
            System.out.print("Para hacerlo, escribe 'LOGIN <Tu nombre de usuario>': ");
            clientName = scanner.nextLine();
            send("LOGIN " + clientName);
            String response = receive();
            if (response.equals("Hola")) {
                loggedIn = true;
                System.out.println("Bienvenido " + clientName);
            } else {
                System.out.println(response);
            }
        }

        new Thread(() -> {
            while (loggedIn) {
                String message = receive();
                System.out.println(message);
            }
        }).start();

        while (loggedIn) {
            String message = scanner.nextLine();
            if ("adeu".equals(message)) {
                send("adeu");
                loggedIn = false;
                System.out.println("Desconectado.");
            } else {
                send(clientName + ": " + message);
            }
        }
        socket.close();
    }

    private void send(String message) {
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    private String receive() {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        } catch (IOException e) {
            return "Error al recibir un mensaje: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        try {
            UDPClient client = new UDPClient("localhost", UDPServer.getPort());
            new Thread(client).start();
        } catch (Exception e) {
            System.out.println("No se pudo iniciar el cliente UDP: " + e.getMessage());
        }
    }
}