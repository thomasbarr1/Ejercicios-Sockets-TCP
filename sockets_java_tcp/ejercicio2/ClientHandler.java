import java.io.*;
import java.net.*;

/** Ejercicio 2: Manejador que atiende a un cliente (eco) dentro de un hilo del pool. */
public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String hilo = Thread.currentThread().getName();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String linea;
            while ((linea = in.readLine()) != null) {
                if ("CHAO".equalsIgnoreCase(linea.trim())) break;
                out.println("[" + hilo + "] ECO: " + linea);
            }
        } catch (IOException e) {
            System.err.println("Cliente desconectado: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("Cliente liberado (" + hilo + ")");
        }
    }
}
