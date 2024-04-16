import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {
    private DatagramSocket socket;
    private Map<String, InetAddress> clientAddresses;
    private Map<String, Integer> clientPorts;
    private boolean running;
    private byte[] buf = new byte[1024];

    public UDPServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
        clientAddresses = new HashMap<>();
        clientPorts = new HashMap<>();
    }

    public void run() {
        running = true;
        System.out.println("Server is running on UDP port " + socket.getLocalPort());

        while (running) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String received = new String(packet.getData(), 0, packet.getLength());

                if (received.startsWith("LOGIN ")) {
                    String userName = received.substring(6);
                    if (!clientAddresses.containsKey(userName)) {
                        clientAddresses.put(userName, address);
                        clientPorts.put(userName, port);
                        System.out.println("User " + userName + " logged in.");
                    }
                } else if (received.startsWith("LOGOUT ")) {
                    String userName = received.substring(7);
                    clientAddresses.remove(userName);
                    clientPorts.remove(userName);
                    System.out.println("User " + userName + " logged out.");
                } else {
                    broadcastMessage(received, address, port);
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                running = false;
            }
        }
        socket.close();
    }

    private void broadcastMessage(String message, InetAddress senderAddr, int senderPort) {
        for (Map.Entry<String, InetAddress> entry : clientAddresses.entrySet()) {
            if (!entry.getValue().equals(senderAddr) || !clientPorts.get(entry.getKey()).equals(senderPort)) {
                try {
                    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length,
                                                               entry.getValue(), clientPorts.get(entry.getKey()));
                    socket.send(packet);
                } catch (IOException e) {
                    System.out.println("Error sending message: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 7778; // Default port
        try {
            UDPServer server = new UDPServer(port);
            server.run();
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        }
    }
}
