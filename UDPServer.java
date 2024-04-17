import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[1024]; // Aumentamos el tamaño del buffer para recibir datos más grandes
    private int port;
    public UDPServer(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);
    }

    public void start() {
        running = true;
        System.out.println("Servidor UDP INICIADO");
        Thread serverThread = new Thread(() -> {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    handlePacket(packet);
                } catch (IOException e) {
                    System.out.println("Error al recibir el paquete: " + e.getMessage());
                }
            }
            socket.close();
        });
        serverThread.start();
    }

    private void handlePacket(DatagramPacket packet) {
        String received = new String(packet.getData(), 0, packet.getLength());
        if ("adeu".equals(received.trim())) {
            return;
        }

        System.out.println("Received from " + packet.getAddress() + ": " + received);
        broadcastMessage(received, packet.getAddress());
    }

    private void broadcastMessage(String message, InetAddress senderAddress) {
        clientPorts.forEach((address, port) -> {
            if (!address.equals(senderAddress)) {
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    System.out.println("Error al enviar el mensaje a " + address + ": " + e.getMessage());
                }
            }
        });
    }

    public void stop() {
        running = false;
        socket.close();
    }
    public static int getPort(){
        return this.port;
    }
}
