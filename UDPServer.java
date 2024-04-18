import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class UDPServer {
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[1024]; // Buffer para recibir datos
    private static List<ClientInfo> clients = new ArrayList<>(); // Lista para almacenar información de los clientes

    public UDPServer(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        // Inicializa la lista de clientes
        System.out.println("Servidor UDP iniciado en el puerto " + port);
    }

    public void start() {
        running = true;
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                handlePacket(packet);
            } catch (IOException e) {
                System.out.println("Error al recibir el paquete: " + e.getMessage());
            }
        }
        socket.close();
    }

    private void handlePacket(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        InetAddress address = packet.getAddress();
        int port = packet.getPort();

        if ("Estas?".equals(message)) {
            sendMessage("si", packet.getAddress(), packet.getPort());
        }
        sendMessage("recibido", packet.getAddress(), packet.getPort());

        if (message.startsWith("LOGIN ")) {
            String clientName = message.substring(6).trim();
            ClientInfo newClient = new ClientInfo(address, port, clientName);
            clients.add(newClient);
            sendMessage("Hola " + clientName + ", bienvenido al chat.", address, port);
            broadcastMessage(clientName + " se ha unido al chat.", newClient);
        } else if (message.equals("adeu")) {
            ClientInfo leavingClient = findClientByAddressAndPort(address, port);
            if (leavingClient != null) {
                clients.remove(leavingClient);
                System.out.println(leavingClient.name + " ha dejado el chat.");
                broadcastMessage(leavingClient.name + " ha dejado el chat.", leavingClient);
            }
        } else {
            ClientInfo sender = findClientByAddressAndPort(address, port);
            if (sender != null) {
                System.out.println(message);
                broadcastMessage(message, sender);
            }
        }
    }

    private ClientInfo findClientByAddressAndPort(InetAddress address, int port) {
        for (ClientInfo client : clients) {
            if (client.address.equals(address) && client.port == port) {
                return client;
            }
        }
        return null;
    }

    private void sendMessage(String message, InetAddress address, int port) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    private void broadcastMessage(String message, ClientInfo sender) {
        for (ClientInfo client : clients) {
            if (!client.equals(sender)) {
                sendMessage(message, client.address, client.port);
            }
        }
    }

    public static List<ClientInfo> getClients(){
        return clients;
    }

    // private boolean verifyCapacity(){
    //     return true;

    //     if(clients.isEmpty() || ){}

    //     return false;
    // }
}

class ClientInfo {
    InetAddress address;
    int port;
    String name;

    public ClientInfo(InetAddress addr, int port, String name) {
        this.address = addr;
        this.port = port;
        this.name = name;
    }
}
