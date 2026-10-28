/**
 * Ejercicio 3: Analiza una línea de texto y devuelve un Command.
 * Nunca lanza excepciones: ante sintaxis inválida devuelve Command INVALID
 * con un mensaje descriptivo (así el socket no se cae).
 *
 * Protocolo:  CONNECT <username> | JOIN <sala> | MSG <texto> | QUIT
 */
public class ProtocolParser {

    public static Command parse(String lineaBruta) {
        if (lineaBruta == null || lineaBruta.trim().isEmpty()) {
            return Command.invalid("Comando vacio");
        }
        String[] partes = lineaBruta.trim().split("\\s+", 2);
        String comando = partes[0].toUpperCase();
        String argumento = partes.length > 1 ? partes[1].trim() : "";

        switch (comando) {
            case "CONNECT":
                if (argumento.isEmpty())
                    return Command.invalid("Uso: CONNECT <username>");
                if (argumento.contains(" "))
                    return Command.invalid("El username no puede contener espacios");
                return Command.valid(Command.Type.CONNECT, argumento);

            case "JOIN":
                if (argumento.isEmpty())
                    return Command.invalid("Uso: JOIN <nombre_sala>");
                if (argumento.contains(" "))
                    return Command.invalid("El nombre de sala no puede contener espacios");
                return Command.valid(Command.Type.JOIN, argumento);

            case "MSG":
                if (argumento.isEmpty())
                    return Command.invalid("Uso: MSG <texto>");
                return Command.valid(Command.Type.MSG, argumento);

            case "QUIT":
                return Command.valid(Command.Type.QUIT, "");

            default:
                return Command.invalid("Comando no reconocido: " + comando);
        }
    }
}
