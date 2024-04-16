import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error setting up streams: " + e.getMessage());
            closeEverything();
        }
    }

    @Override
    public void run() {
        try {
            String receivedMessage;
            while ((receivedMessage = in.readLine()) != null) {
                if (receivedMessage.startsWith("LOGIN")) {
                    handleLogin(receivedMessage);
                } else if (receivedMessage.equals("adeu")) {
                    handleLogout();
                    break;
                } else {
                    broadcastMessage(clientName + ": " + receivedMessage);
                }
            }
        } catch (IOException e) {
            System.out.println(clientName + " error reading: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    public void handleLogin(String message) {
        StringTokenizer tokenizer = new StringTokenizer(message);
        tokenizer.nextToken(); // Skip the "LOGIN" part
        clientName = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "Anonymous";
        out.println("HELLO " + clientName + ", there are currently " + (ChatServer.getClients().size() - 1) + " other users online. Type 'adeu' to disconnect.");
        broadcastMessage("The client " + clientName + " has connected to the chat!");
    }

    private void handleLogout() {
        broadcastMessage("The client " + clientName + " has disconnected.");
        closeEverything();
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : ChatServer.getClients()) {
            if (!client.equals(this)) { // Don't send the message to itself
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
        ChatServer.getClients().remove(this);
        System.out.println("Current clients count: " + ChatServer.getClients().size());
    }
}
