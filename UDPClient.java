import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.Set;

public class UDPClient implements Runnable {
    private static DatagramSocket socket;
    private InetAddress address;
    private static int port;
    private static int maxClients;
    private static String name;
    static Scanner scanner = new Scanner(System.in);
    
    public UDPClient(String address, int port) throws IOException {
        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName(address);
        this.port = port;
    }
    
    //hilo para recivir mensajes
    Thread receiveThread = new Thread(() -> {
        try {
            while (true) {
                System.out.println(receive());
            }
        }catch(SocketException ex) {
            System.out.println("Se ha desconectado del chat 2");
            
        }catch (Exception e) {
            System.out.println("Error al recibir mensajes: " + e.getMessage() + e.getStackTrace());
        }
    });
    
    @Override
    public void run() {
        try {
            // Ciclo para iniciar sesión
            if(isServerAlive()){
                if(isCapacity()){
                    send(login());
                    receiveThread.start();
                    
                    // ciclo para enviar mensajes
                    while (true) {
                        String message = scanner.nextLine();
                        if ("adeu".equalsIgnoreCase(message.toLowerCase().trim())) {
                            send("adeu");
                            logout();
                        }
                        send(name + ": " + message);
                    }
                    
                }else {
                    System.out.println("El servidor ha alcanzado su capacidad de usuarios contectados, intentalo mas tarde.");
                    socket.close();
                    System.exit(0);
                }
            }else {
                //System.out.println("El servidor esta apagadou"); depurar
                socket.close();
                System.exit(0);
            }
            
            
            socket.close();
        } catch(SocketException ex){
            if(ex.getMessage().equals("Socket closed")){
                
                System.out.println("Se ha desconectado del chat");
            }else {
                System.out.println("Error de socket: "+ ex.getMessage());
            }
        }
        catch (Exception e) {
            System.out.println("Error en el cliente UDP: " + e.getMessage());
        }
    }
    
    /*
     * Funcion que envia mensajes y llama a otra que  verifica su recepcion
     */
    private void send(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
        isMessageReceived();
       
    }

    /**
     * Funcion que mira si un mensaje se ha recibido, para comprobar durante la conexion
     * ya que si el servidor se cierra y el usuario sigue en el, no sabra que se ha cerrado la conexion
     * @return
     */
    private boolean isMessageReceived() {
        try {
            socket.setSoTimeout(5000); // esperamos 5 sec
            System.out.println("Esperando respuesta del servidor...");
            String response = receive(); // si hay respuesta
            System.out.println("Respuesta recibida: " + response);
            socket.setSoTimeout(0); // desactivamos el set timeout
            return "recibido".equals(response.trim()); // verifica la respuesta esperada
        } catch (SocketTimeoutException timeout) {
            System.out.println("Desafortunadamente se ha perdido la conexión con el servidor x_x");
            return false; // devuelve falso si hay timeout
        } catch (IOException e) {
            System.out.println("Error al comprobar si el servidor está activo: " + e.getMessage());
            return false; // devuelve falso si hay una excepción de I/O
        } finally {
            try {
                socket.setSoTimeout(0); // restablece el timeout para evitar efectos secundarios
            } catch (SocketException e) {
                System.out.println("No se pudo restablecer el timeout del socket: " + e.getMessage());
            }
        }
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
            System.out.println("Iniciando conexion en el puerto:" + port + " \nEspere..");
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
    
    public static boolean isCapacity(){
        return maxClients >= UDPServer.getClients().size() || UDPServer.getClients().isEmpty() || UDPServer.getClients() == null;
    }
    
    
    /**
    * funcion para solicitar al usuario que inicie sesion
    * @return
    */
    public static String login(){
        String input = "";
        System.out.println("Para usar este chat, necesitas iniciar sesión.");
        while (true) {
            System.out.print("Por favor, escribe 'LOGIN <Tu nombre de usuario>': ");
            input = scanner.nextLine();
            if (input.startsWith("LOGIN ")) {
                name = input.substring(6).trim(); // extraemos el nombre
                if (!name.isEmpty() && !isUserNameTaken(name)) {
                    System.out.println("Sesion iniciada con exito");
                    break; // salimos si el nombre no esta vacio
                }
            }else if(input.toLowerCase().startsWith("adeu")){
                socket.close(); //si al loguearse pone adeu, se desconecta
                System.exit(0);
                break;
            }else {
                System.out.println("Formato incorrecto. Asegúrate de escribir 'LOGIN <Tu nombre de usuario>'.");
            }
        }
        return input;
    }

    public static boolean isUserNameTaken(String name){
        Set <String> currentUsers = UserNameManager.loadUsers();
        while(currentUsers.contains(name)) {
            System.out.println("Este nombre de usuario ya esta en uso, Por favor elija otro");
            return true;
        }
        UserNameManager.addUser(name);
        return false;
        
    }

    public static void logout(){
        UserNameManager.removeUser(name);
        socket.close();
        System.exit(0);
    }
    
    // public boolean handleLogin(String message) {
    //     String[] parts = message.split(" ", 2); // partimos el estring para recoger el nombre de usuario
    //     if (parts.length > 1 && parts[1] != null && !parts[1].trim().isEmpty()) {
    //         String proposedName = parts[1].trim();
    //         Set<String> currentUsers = UserNameManager.loadUsers();
    //         if (currentUsers.contains(proposedName)) {//si el nombre esta en el fichero, el usuario tendra que escoger otro
    //             out.println("Este nombre de usuario ya está en uso. Por favor, elija otro.");
    //             return false;
    //         }
    //         clientName = proposedName;
    //         UserNameManager.addUser(clientName);
    //         out.println("Hola " + clientName + ", ahora mismo hay " + (TCPServer.getClients().size() - 1) + " usuarios en linea. Escribe 'adeu' para desconectarte.");
    //         broadcastMessage("El cliente " + clientName + " se ha conectado al chat!");
    //         return true;
    //     } else {
    //         out.println("Login no válido, escriba 'LOGIN <Tu nombre de usuario>'.");
    //         return false;
    //     }
    // }

    
    /**
    * Metodo que mediante un ping verifica que el servidor esta inciado
    * @return
     * @throws IOException 
    **/
    private boolean isServerAlive() throws IOException {
        String message = "Estas?";
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.send(packet);
            socket.setSoTimeout(5000); // esperamos 5 sec para ver si el servidor nos contesta
            String response = receive(); //si hay respuesta
            socket.setSoTimeout(0); //desactivamos el set timeout
            return "si".equals(response);
        } catch (SocketTimeoutException e) {
            System.out.println("El servidor esta apagado, intentalo mas tarde");
            return false; // el server no responde
        } catch (IOException e) {
            System.out.println("Error al comprobar si el servidor esta activo: " + e.getMessage());
            return false;
        }
    }
    
}
