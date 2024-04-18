import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
    private static InetAddress address;
    private static int port;
    private static int maxClients;
    private static String name;
    private static boolean logged;
    static Scanner scanner = new Scanner(System.in);
    
    public UDPClient(String address, int port) throws IOException {
        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName(address);
        this.port = port;
        
    }
    
    //hilo para recivir mensajes
    Thread receiveThread = new Thread(() -> {
        try {
            while (true && logged) {
                
                System.out.println(receive());
            }
        }catch(SocketException ex) {
            System.out.println("Se ha desconectado del chat 2");
            
        }catch(SocketTimeoutException exe){
            System.out.println("Socket tineout exeception: " + exe);
        }
        catch (Exception e) {
            System.out.println("Error al recibir mensajes en el receive thread: " + e.getMessage() + e.getStackTrace());
        }
    });
    
    
    Thread keepAliveThread = new Thread(()->{
        while (true) {
            try {
                send("Estas?");
                socket.setSoTimeout(5000); 
                String response = receive();
                socket.setSoTimeout(0);
                if (!"si".equals(response)) {
                    System.out.println("El servidor no responde. Verificando de nuevo en 60 segundos.");
                    Thread.sleep(60000); 
                } else {
                    System.out.println("El servidor esta activo.");
                    Thread.sleep(60000); 
                }
            }catch (SocketTimeoutException timeout) {
                System.out.println( timeout+" en el keep alive");
            }
            catch (InterruptedException e) {
                System.out.println("Keep-alive interrumpido: " + e.getMessage());
                break;
            } catch (IOException e) {
                System.out.println("Problemas de conexion durante el keep-alive: " + e.getMessage());
            }
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
                    // keepAliveThread.start();
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
                System.out.println("El servidor esta apagado, intentalo mas tarde");
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
    private static void send(String message) throws IOException, InterruptedException {
        
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
        
    }
    
    
    
    private static String receive() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }
    
    public static void main(String[] args) throws IOException {
        try {
            readConfig();
        } catch (FileNotFoundException e) {
            System.out.println("Hay un error en la configuracion..Puede ser que el servidor no este iniciado");
            System.exit(0);
        }
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
    
    public static boolean isCapacity() throws IOException, InterruptedException{
        
        System.out.println("Clientes activos ahora mismo: " + requestClientCount());
        return maxClients >= requestClientCount();
    }
    
    // Método para solicitar la cantidad de clientes
    public static int requestClientCount() throws IOException, InterruptedException {
        send("GET_CLIENT_COUNT");
        String response = receive();
        return Integer.parseInt(response);
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
                    logged = true;
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
    
    
    
    /**
    * Metodo que mediante un ping verifica que el servidor esta inciado
    * @return
    * @throws IOException 
    * @throws InterruptedException 
    **/
    private boolean isServerAlive() throws IOException, InterruptedException {
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
            return false; // el server no responde
        } catch (IOException e) {
            System.out.println("Error al comprobar si el servidor esta activo: " + e.getMessage());
            return false;
        }
    }
    
}
