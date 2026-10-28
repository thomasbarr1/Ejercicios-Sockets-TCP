import java.io.*;
import java.net.*;

/** Ejercicio 1: Cliente de eco. Lee de la consola y muestra la respuesta del servidor. */
public class EchoClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado a localhost:8080. Escribe mensajes (CHAO para salir).");
            String linea;
            while ((linea = teclado.readLine()) != null) {
                out.println(linea);
                if ("CHAO".equalsIgnoreCase(linea)) break;
                String respuesta = in.readLine();
                if (respuesta == null) {
                    System.out.println("El servidor cerró la conexión.");
                    break;
                }
                System.out.println(respuesta);
            }
        }
    }
}
