import java.io.*;
import java.net.*;

/**
 * Cliente CLI con recepcion asincrona: un hilo escucha al servidor
 * mientras el hilo principal lee de System.in.
 * Uso: java JSMSClient [host] [puerto]
 */
public class JSMSClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int puerto = args.length > 1 ? Integer.parseInt(args[1]) : 8080;

        try (Socket socket = new Socket(host, puerto);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            // Hilo receptor
            Thread receptor = new Thread(() -> {
                try {
                    String linea;
                    while ((linea = in.readLine()) != null) {
                        System.out.println(linea);
                    }
                } catch (IOException ignored) {
                }
                System.out.println("[Conexion cerrada por el servidor]");
                System.exit(0);
            });
            receptor.setDaemon(true);
            receptor.start();

            // Hilo principal: teclado -> servidor
            String entrada;
            while ((entrada = teclado.readLine()) != null) {
                out.println(entrada);
                if ("QUIT".equalsIgnoreCase(entrada.trim())) break;
            }
        } catch (IOException e) {
            System.err.println("No se pudo conectar: " + e.getMessage());
        }
    }
}
