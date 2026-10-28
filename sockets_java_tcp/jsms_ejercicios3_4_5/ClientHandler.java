import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Atiende a un cliente: parsea comandos (Ej.3), usa RoomManager para
 * broadcast (Ej.4) y maneja timeouts/desconexiones (Ej.5).
 */
public class ClientHandler implements Runnable {
    private static final int TIMEOUT_MS = 30000;

    private final Socket socket;
    private final RoomManager roomManager;
    private PrintWriter out;

    private volatile String username;   // null hasta que haga CONNECT
    private volatile String salaActual; // null hasta que haga JOIN

    public ClientHandler(Socket socket, RoomManager roomManager) {
        this.socket = socket;
        this.roomManager = roomManager;
    }

    /** Envia una linea al cliente (PrintWriter.println es thread-safe). */
    public void enviarMensajeDirecto(String mensaje) {
        if (out != null) out.println(mensaje);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            this.out = writer;
            socket.setSoTimeout(TIMEOUT_MS);   // Ej.5: detectar inactividad
            socket.setKeepAlive(true);

            enviarMensajeDirecto("Bienvenido a JSMS. Usa: CONNECT <username>");

            String linea;
            boolean seguir = true;
            while (seguir && (linea = in.readLine()) != null) {
                seguir = procesar(ProtocolParser.parse(linea));
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout por inactividad: " + (username != null ? username : socket.getInetAddress()));
            enviarMensajeDirecto("TIMEOUT Conexion cerrada por inactividad");
        } catch (SocketException e) {
            System.err.println("Conexion cerrada/abortada: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        } finally {
            limpiar();
        }
    }

    /** @return false si la sesion debe terminar. */
    private boolean procesar(Command cmd) {
        switch (cmd.getType()) {
            case INVALID:
                enviarMensajeDirecto("400 ERROR " + cmd.getError());
                return true;

            case CONNECT:
                if (username != null) {
                    enviarMensajeDirecto("401 ERROR Ya estas conectado como " + username);
                } else if (roomManager.registrarUsuario(cmd.getArgument(), this)) {
                    username = cmd.getArgument();
                    enviarMensajeDirecto("200 OK");
                } else {
                    enviarMensajeDirecto("401 ERROR Username en uso");
                }
                return true;

            case JOIN:
                if (!exigirConexion()) return true;
                roomManager.salirDeSala(salaActual, this); // sale de la anterior (si hay)
                roomManager.unirseASala(cmd.getArgument(), this);
                salaActual = cmd.getArgument();
                enviarMensajeDirecto("JOINED " + salaActual);
                roomManager.broadcast(salaActual, "* " + username + " se unio a la sala", this);
                return true;

            case MSG:
                if (!exigirConexion()) return true;
                if (salaActual == null) {
                    enviarMensajeDirecto("403 ERROR Primero usa JOIN <sala>");
                } else {
                    roomManager.broadcast(salaActual, "MSG " + username + ": " + cmd.getArgument(), this);
                    enviarMensajeDirecto("200 OK");
                }
                return true;

            case QUIT:
                enviarMensajeDirecto("200 BYE");
                return false;

            default:
                return true;
        }
    }

    private boolean exigirConexion() {
        if (username == null) {
            enviarMensajeDirecto("403 ERROR Primero usa CONNECT <username>");
            return false;
        }
        return true;
    }

    /** Libera recursos: sale de la sala, des-registra al usuario y cierra el socket. */
    private void limpiar() {
        if (salaActual != null) {
            roomManager.broadcast(salaActual, "* " + username + " salio de la sala", this);
            roomManager.salirDeSala(salaActual, this);
        }
        roomManager.removerUsuario(username);
        try { socket.close(); } catch (IOException ignored) {}
        System.out.println("Sesion finalizada: " + (username != null ? username : "(anonimo)"));
    }
}
