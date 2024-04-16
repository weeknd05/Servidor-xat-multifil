import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Objects;

public class ChatServer {
    private static int maxClients;
    private static int port;
    private static boolean useTCP = true;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Introduce el número máximo de clientes:");
        maxClients = scanner.nextInt();
        
        System.out.println("Introduce el puerto de conexión (0 para aleatorio):");
        port = scanner.nextInt();
        if (port == 0) {
            port = randomSafePort();
        } else {
            while (!isValidPort(port)) {
                System.out.println("Puerto no válido. Introduce un puerto entre 1024 y 49151:");
                port = scanner.nextInt();
            }
        }
        
        System.out.println("Elije el protocolo de conexión (1 para TCP, 2 para UDP):");
        int protocol = scanner.nextInt();
        useTCP = (protocol == 1);
        
        if (useTCP) {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor TCP iniciado en el puerto " + port);
            
            while (true) {
                if (clients.size() < maxClients) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            }
        } else {
            // Implementación UDP si es necesaria
        }
    }

    private static int randomSafePort() {
        return (int) (Math.random() * (49151 - 1024 + 1)) + 1024;
    }

    //tenemos en cuenta los puertos que reversa el sistema operativo
    private static boolean isValidPort(int port) {
        return port >= 1024 && port <= 49151;
    }


    //getters y setters
    public static int getMaxClients() {
        return maxClients;
    }

    public static void setMaxClients(int maxClients) {
        ChatServer.maxClients = maxClients;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        ChatServer.port = port;
    }

    public static boolean isUseTCP() {
        return useTCP;
    }

    public static void setUseTCP(boolean useTCP) {
        ChatServer.useTCP = useTCP;
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }

    public static void setClients(List<ClientHandler> clients) {
        ChatServer.clients = clients;
    }


    
}
