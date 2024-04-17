import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient implements Runnable {
    private DatagramSocket socket;
    private InetAddress address;
    private static int port;
    private static int maxClients;
    private String name;

    public UDPClient(String address, int port) throws IOException {
        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName(address);
        this.port = port;
    }

    @Override
    public void run() {
        try {
            // Ciclo para iniciar sesión
            Scanner scanner = new Scanner(System.in);
            String input;

            // solicitar al usuario que inicie sesión
            System.out.println("Para usar este chat, necesitas iniciar sesión.");
            while (true) {
                System.out.print("Por favor, escribe 'LOGIN <Tu nombre de usuario>': ");
                input = scanner.nextLine();
                if (input.startsWith("LOGIN ")) {
                    this.name = input.substring(6).trim(); // extraemos el nombre
                    if (!this.name.isEmpty()) {
                        break; // salimos si el nombre no esta vacio
                    }
                }
                System.out.println("Formato incorrecto. Asegúrate de escribir 'LOGIN <Tu nombre de usuario>'.");
            }
          
            send(this.name);

            // Hilo para recibir mensajes
            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        System.out.println(receive());
                    }
                } catch (Exception e) {
                    System.out.println("Error al recibir mensajes: " + e.getMessage());
                }
            });
            receiveThread.start();

            // Ciclo para enviar mensajes
            while (true) {
                String message = scanner.nextLine();
                if ("adeu".equalsIgnoreCase(message.trim())) {
                    send("adeu");
                    break;
                }
                send(name + ": " + message);
            }
            socket.close();
        } catch (Exception e) {
            System.out.println("Error en el cliente UDP: " + e.getMessage());
        }
    }

    private void send(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

    private String receive() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public static void main(String[] args) throws IOException {
        readConfig();
        try {
            UDPClient client = new UDPClient("localhost", port);
            new Thread(client).start();
        } catch (IOException e) {
            System.out.println("No se pudo iniciar el cliente UDP: " + e.getMessage());
        }
    }

    public static void readConfig() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
        UDPClient.port = Integer.parseInt(reader.readLine());
        UDPClient.maxClients = Integer.parseInt(reader.readLine());
        reader.close();
        
      
    }
}
