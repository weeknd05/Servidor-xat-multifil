import java.io.*;
import java.util.HashSet;
import java.util.Set;
/*
 * Clase para manejar los nombres de usuario
 * esta clase se comunica con ClientHandler
 * los recursos de esta clase seran compartidos y al mismo tiempo pueden acceder varios hilos
 * por lo tanto los metodos seran synchronized
 */
public class UserNameManager {
    private static final String USER_FILE = "users.txt";

    /**
     * Metodo para cargar los usuarios del fichero
     * @return
     */
    public static synchronized Set<String> loadUsers() {
        Set<String> users = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(line.trim());
            }
        } catch (IOException e) {
           // System.out.println("Error al leer el archivo de usuarios: " + e.getMessage());
        }
        return users;
    }

    public static synchronized void addUser(String username) {
        try (FileWriter fw = new FileWriter(USER_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(username);
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo de usuarios: " + e.getMessage());
        }
    }

    public static synchronized void removeUser(String username) {
        Set<String> users = loadUsers();
        if (users.remove(username)) {
            try (PrintWriter out = new PrintWriter(new FileWriter(USER_FILE))) {
                for (String user : users) {
                    out.println(user);
                }
            } catch (IOException e) {
                System.out.println("Error al actualizar el archivo de usuarios: " + e.getMessage());
            }
        }
    }

    public static void removeUserFile(){
        try {
            File fichero = new File(USER_FILE);
            fichero.delete();
        } catch (Exception e) {
            System.out.println(e);
        }
      
    }

    
}
