import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;
import java.io.File;

public  class ServerConfig {
    private int port;
    private boolean useTCP;
    private int maxClients;
    public ServerConfig() throws IOException {
        configure();
    }

    private void configure() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {

            System.out.println("Introduce el número máximo de clientes:");
            String clients = reader.readLine();
            maxClients = Integer.parseInt(clients);

            System.out.println("Introduce el puerto de conexión (0 para aleatorio):");
            String portInput = reader.readLine();
            int port = Integer.parseInt(portInput);
            if (port == 0) {
                port = randomSafePort();
            }
            while (port < 1024 || port > 49151) {
                System.out.println("Puerto no válido. Introduce un puerto entre 1024 y 49151:");
                port = Integer.parseInt(reader.readLine());
            }
            this.port = port;

            System.out.println("Elige el protocolo de conexión (1 para TCP, 2 para UDP):");
            String protocolInput;
            do {
                protocolInput = reader.readLine();
            } while (!protocolInput.equals("1") && !protocolInput.equals("2"));
            useTCP = "1".equals(protocolInput.trim());

            //guardamos la configuracion
            saveConfig();
        } catch (IOException e) {
            System.out.println("Error al leer la configuracion: " + e.getMessage());
        }
    }


    
    public  void saveConfig() throws IOException {
        PrintWriter writer = new PrintWriter(new File("config.txt"));
        writer.println(this.port);
        writer.println(this.useTCP);
        writer.println(this.maxClients);
        writer.close();
    }

    private int randomSafePort() {
        Random random = new Random();
        return random.nextInt(49151 - 1024 + 1) + 1024;
    }

    public int getPort() {
        return port;
    }
    public int getMaxClients() {
        return maxClients;
    }

    public boolean isUseTCP() {
        return useTCP;
    }
}
