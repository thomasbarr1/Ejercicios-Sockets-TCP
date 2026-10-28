import java.io.*;
import java.net.*;

/** Ejercicio 1: Servidor de eco mono-hilo. Escribe "CHAO" para cerrar. */
public class EchoServer {
    public static void main(String[] args) throws IOException {
        int puerto = 8080;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado en puerto " + puerto);
            Socket clientSocket = serverSocket.accept(); // Bloqueante
            System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
            try (BufferedReader in = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    if ("CHAO".equalsIgnoreCase(mensaje)) break;
                    out.println("ECO: " + mensaje);
                }
            } finally {
                clientSocket.close();
            }
        }
    }
}
