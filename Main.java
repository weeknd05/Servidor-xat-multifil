import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main {

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> shutdownTask;
    private static boolean wasEmpty = true; // variable para ver si la lista estaba  vacia

    public static void main(String[] args) {
        ServerConfig config = new ServerConfig();
        shutdownHook();
        if (config.isUseTCP()) {
            scheduleServerShutdown(TCPServer.getClients());
            try {
                TCPServer.startServer(config);
            } catch (IOException e) {
                System.out.println("Error al iniciar el servidor TCP: " + e.getMessage());
            }
        }else {
            try {
                UDPServer udpServer = new UDPServer(config.getPort());
                udpServer.start();
            } catch (SocketException e) {
                System.out.println("Error al iniciar el servidor UDP: " + e.getMessage());
            }
        }
    }

    /**
     * metodo que inicia un hook para cuando se cierra el servidor, de esta forma se borra la lista de los nombres de usuario
     */
    private static void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando servidor");
            UserNameManager.removeUserFile();
            scheduler.shutdownNow();
        }));
    }

    /**
     * Función que mira si hay usuarios activos o no cada 5 segundos. Si no hay usuarios activos, se espera 30 segundos y cierra el servidor
     * 
     * @param clients
     */
    private static void scheduleServerShutdown(List<?> clients) {
        scheduler.scheduleWithFixedDelay(() -> {
            boolean isEmpty = clients.isEmpty();
            if (isEmpty) {
                if (shutdownTask == null || shutdownTask.isDone() || shutdownTask.isCancelled()) {
                    System.out.println("No hay ningún usuario activo, preparando para cerrar en 30 segundos...");
                    shutdownTask = scheduler.schedule(() -> {
                        if (clients.isEmpty()) {
                            System.out.println("No se ha conectado ningún usuario durante 30 segundos, cerrando servidor.");
                            System.exit(0);
                        }
                    }, 30, TimeUnit.SECONDS);
                }
            } else if (!isEmpty && wasEmpty) { //si no esta vacia y estuvo vacia, entra
                if (shutdownTask != null) {
                    shutdownTask.cancel(false);
                    System.out.println("Usuarios activos detectados, cancelando cierre programado.");
                }
            }
            wasEmpty = isEmpty; // Actualiza el estado de wasEmpty para la próxima verificación, asi no se repetira el mensaje en la termianl
        }, 20, 5, TimeUnit.SECONDS);
    }
}
