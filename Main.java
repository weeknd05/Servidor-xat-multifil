import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig();
        if (config.isUseTCP()) {
            try {
            TCPServer.startServer(config);
            } catch (IOException e) {
                System.out.println("Error al iniciar el cliente TCP: " + e.getMessage());
            }
        } else {
            
        }
    }
}
