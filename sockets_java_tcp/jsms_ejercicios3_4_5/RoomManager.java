import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ejercicio 4: Estado compartido thread-safe.
 *  - salas:    NombreSala -> lista de manejadores (ConcurrentHashMap + CopyOnWriteArrayList)
 *  - usuarios: username -> manejador (para garantizar nombres unicos)
 */
public class RoomManager {
    private final Map<String, List<ClientHandler>> salas = new ConcurrentHashMap<>();
    private final Map<String, ClientHandler> usuarios = new ConcurrentHashMap<>();

    // ---- Usuarios ----
    /** @return true si el username estaba libre y quedó registrado. */
    public boolean registrarUsuario(String username, ClientHandler handler) {
        return usuarios.putIfAbsent(username, handler) == null;
    }

    public void removerUsuario(String username) {
        if (username != null) usuarios.remove(username);
    }

    // ---- Salas ----
    public void unirseASala(String nombreSala, ClientHandler cliente) {
        salas.computeIfAbsent(nombreSala, k -> new CopyOnWriteArrayList<>()).add(cliente);
    }

    public void salirDeSala(String nombreSala, ClientHandler cliente) {
        if (nombreSala == null) return;
        List<ClientHandler> miembros = salas.get(nombreSala);
        if (miembros != null) {
            miembros.remove(cliente);
            // Limpia la sala si quedó vacía (remove atómico condicionado a la misma lista)
            if (miembros.isEmpty()) salas.remove(nombreSala, miembros);
        }
    }

    public void broadcast(String nombreSala, String mensaje, ClientHandler emisor) {
        List<ClientHandler> miembros = salas.get(nombreSala);
        if (miembros != null) {
            for (ClientHandler miembro : miembros) {
                if (miembro != emisor) { // Evita eco al propio emisor
                    miembro.enviarMensajeDirecto(mensaje);
                }
            }
        }
    }

    /** Ejercicio 5: avisa a todos los clientes conectados (usado en el shutdown hook). */
    public void notificarATodos(String mensaje) {
        for (ClientHandler h : usuarios.values()) {
            h.enviarMensajeDirecto(mensaje);
        }
    }
}
