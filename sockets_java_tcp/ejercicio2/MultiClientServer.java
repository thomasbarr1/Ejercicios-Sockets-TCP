import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/** Ejercicio 2: Servidor multihilo con pool fijo de 10 hilos. */
public class MultiClientServer {
    private static final int PUERTO = 8080;
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PUERTO)) {
            System.out.println("Servidor Multihilo listo en puerto " + PUERTO + "...");
            while (true) {
                Socket socket = server.accept();
                System.out.println("Nuevo cliente: " + socket.getInetAddress());
                pool.execute(new ClientHandler(socket)); // Asigna hilo del pool
            }
        } finally {
            pool.shutdown();
        }
    }
}
