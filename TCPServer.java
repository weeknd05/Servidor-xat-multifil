import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Objects;

public class TCPServer {
    private static List<TCPClient> clients = new ArrayList<>();
    private static ServerSocket serverSocket;

    public static void startServer(ServerConfig config) throws IOException {
        
        serverSocket = new ServerSocket(config.getPort());
        System.out.println("Servidor TCP iniciado en el puerto " + config.getPort());
        
        while (true) {
            Socket clientSocket = serverSocket.accept();
            if (clients.size() >= config.getMaxClients()) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("El servidor esta lleno. Por favor, intentalo mas tarde.");
                clientSocket.close(); //se cierra la conexion.
            } else {
                TCPClient clientHandler = new TCPClient(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        }
        
        
    }
    
    
  
    
    
    
    //getters y setters
    public static List<TCPClient> getClients() {
        return clients;
    }
    
    public static void setClients(List<TCPClient> clients) {
        TCPServer.clients = clients;
    }
    
    
    
}
