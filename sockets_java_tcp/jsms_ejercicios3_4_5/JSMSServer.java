import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

/** Servidor JSMS completo: pool de hilos, salas, timeouts y apagado limpio. */
public class JSMSServer {
    private static final int PUERTO = 8080;
    private static final int MAX_HILOS = 50;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);
        RoomManager roomManager = new RoomManager();

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {

            // Ej.5: Graceful shutdown (Ctrl+C)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nIniciando apagado controlado del servidor...");
                roomManager.notificarATodos("SERVER_SHUTDOWN El servidor se esta apagando");
                try { serverSocket.close(); } catch (IOException ignored) {}
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(3, TimeUnit.SECONDS)) pool.shutdownNow();
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                }
            }));

            System.out.println("Servidor JSMS escuchando en puerto " + PUERTO);
            while (!serverSocket.isClosed()) {
                Socket cliente = serverSocket.accept();
                System.out.println("Conexion desde " + cliente.getInetAddress());
                pool.execute(new ClientHandler(cliente, roomManager));
            }
        } catch (SocketException e) {
            System.out.println("Socket del servidor cerrado.");
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}
