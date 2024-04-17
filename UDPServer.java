import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class UDPServer {
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[1024]; // Buffer para recibir datos
    private Map<String, ClientInfo> clients; // Almacena información de los clientes
    

    public UDPServer(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);
        clients = new ConcurrentHashMap<>(); // Seguro para el acceso concurrente
        System.out.println("Servidor UDP iniciado en el puerto " + port);
    }

    public void start() {
    public void start() {
        running = true;
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // Recibe un paquete

                String received = new String(packet.getData(), 0, packet.getLength());
                handlePacket(packet, received);
            } catch (IOException e) {
                System.out.println("Error al recibir el paquete: " + e.getMessage());
            }
        }
        socket.close();
    }

    private void handlePacket(DatagramPacket packet, String message) {
        String clientKey = packet.getAddress().toString() + ":" + packet.getPort();
        if (message.startsWith("LOGIN")) {
            String clientName = message.substring(6).trim();
            clients.put(clientKey, new ClientInfo(packet.getAddress(), packet.getPort(), clientName));
            sendMessage("Hola " + clientName, packet.getAddress(), packet.getPort());
            System.out.println(clientName + " se ha conectado.");
        } else if (message.equals("adeu")) {
            clients.remove(clientKey);
            System.out.println(clients.get(clientKey).name + " se ha desconectado.");
            if (clients.isEmpty()) {
                running = false;
            }
        } else {
            broadcastMessage(message, clientKey);
        }
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

    private void broadcastMessage(String message, String senderKey) {
        clients.forEach((key, client) -> {
            if (!key.equals(senderKey)) { // No reenviar al emisor
                sendMessage(message, client.address, client.port);
            }
        });
    }

    public static void startServer(ServerConfig config) {
        try {
            UDPServer server = new UDPServer(config.getPort());
            server.start();
        } catch (SocketException e) {
            System.out.println("No se pudo iniciar el servidor UDP: " + e.getMessage());
        }
    }
}

class ClientInfo {
    InetAddress address;
    int port;
    String name;

    ClientInfo(InetAddress addr, int port, String name) {
        this.address = addr;
        this.port = port;
        this.name = name;
    }
}
