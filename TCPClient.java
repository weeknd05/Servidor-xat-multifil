import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Set;

public class TCPClient implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    protected PrintWriter out;
    private String clientName;
    private boolean loggedIn = false;

    public TCPClient(Socket socket) {
        this.clientSocket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            closeEverything();
        }
    }

    @Override
    public void run() {
        try {
            out.println("Hola! Para usar este chat, necesitas iniciar sesión.");
            out.println("Para hacerlo, escriba 'LOGIN <Tu nombre de usuario>'");
            String receivedMessage;
            while ((receivedMessage = in.readLine()) != null) {
                if (!loggedIn) {
                    if (receivedMessage.startsWith("LOGIN")) {
                        loggedIn = handleLogin(receivedMessage);
                    }
                    if (!loggedIn) {
                        out.println("Necesitas iniciar sesión.(LOGIN <Tu nombre de usuario>).");
                    }
                } else {
                    if (receivedMessage.equals("adeu")) {
                        handleLogout();
                        break;
                    } else {
                        broadcastMessage(clientName + ": " + receivedMessage);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(clientName + " error reading: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    public boolean handleLogin(String message) {
        String[] parts = message.split(" ", 2); // partimos el estring para recoger el nombre de usuario
        if (parts.length > 1 && parts[1] != null && !parts[1].trim().isEmpty()) {
            String proposedName = parts[1].trim();
            Set<String> currentUsers = UserNameManager.loadUsers();
            if (currentUsers.contains(proposedName)) {//si el nombre esta en el fichero, el usuario tendra que escoger otro
                out.println("Este nombre de usuario ya está en uso. Por favor, elija otro.");
                return false;
            }
            clientName = proposedName;
            UserNameManager.addUser(clientName);
            out.println("Hola " + clientName + ", ahora mismo hay " + (TCPServer.getClients().size() - 1) + " usuarios en linea. Escribe 'adeu' para desconectarte.");
            broadcastMessage("El cliente " + clientName + " se ha conectado al chat!");
            return true;
        } else {
            out.println("Login no válido, escriba 'LOGIN <Tu nombre de usuario>'.");
            return false;
        }
    }

    private void handleLogout() {
        broadcastMessage("El cliente " + clientName + " se ha desconectado.");
        UserNameManager.removeUser(clientName);
        closeEverything();
    }

    private void broadcastMessage(String message) {
        List<TCPClient> clients = TCPServer.getClients();
        for (TCPClient client : clients) {
            if (!client.equals(this)) {
                client.out.println(message);
            }
        }
    }

    private void closeEverything() {
        removeClient();
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }

    private void removeClient() {
        TCPServer.getClients().remove(this);
        System.out.println("Número de clientes " + TCPServer.getClients().size());
    }
}
