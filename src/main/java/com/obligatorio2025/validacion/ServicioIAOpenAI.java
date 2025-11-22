package com.obligatorio2025.validacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obligatorio2025.dominio.Categoria;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServicioIAOpenAI implements ServicioIA {

    private final CategoriaRepositorio categoriaRepositorio;
    private final String apiKey;
    private final String baseUrl; // ej: https://api.openai.com/v1
    private final String model;   // ej: gpt-4o-mini
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();


    public ServicioIAOpenAI(CategoriaRepositorio categoriaRepositorio,
                            String apiKey,
                            String baseUrl,
                            String model) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.apiKey = apiKey;
        this.baseUrl = (baseUrl != null && baseUrl.endsWith("/"))
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : (baseUrl != null ? baseUrl : "https://api.openai.com/v1");
        this.model = (model != null && !model.isBlank()) ? model : "gpt-4o-mini";
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public VeredictoIA validar(int categoriaId, char letraRonda, String textoRespuesta) {
        try {
            if (textoRespuesta == null || textoRespuesta.trim().isEmpty()) {
                return new VeredictoIA(false, "Vacío");
            }
            String texto = textoRespuesta.trim();

            Categoria categoria = categoriaRepositorio.buscarPorId(categoriaId);
            String nombreCategoria = (categoria != null)
                    ? categoria.getNombre()
                    : ("categoría " + categoriaId);

            // IMPORTANTE: la IA SOLO decide si encaja en la CATEGORÍA.
            // La letra se valida en ServicioValidacionPorRonda.coincideConLetra(...)
            String systemPrompt =
                    "Eres un juez estricto del juego Tutti Frutti.\n" +
                            "Tu única tarea es decidir si una palabra pertenece razonablemente " +
                            "a la categoría dada.\n" +
                            "\n" +
                            "NO debes verificar con qué letra comienza la palabra; " +
                            "eso ya lo controla el sistema por separado.\n" +
                            "Ignora completamente cualquier letra inicial.\n" +
                            "\n" +
                            "Reglas importantes:\n" +
                            "- Considera válida cualquier palabra que entre de forma razonable en la categoría.\n" +
                            "- No inventes criterios extra como 'moderna', 'antigua', 'muy genérica', " +
                            "  'demasiado específica', 'poco usada', etc.\n" +
                            "- Las respuestas tienen que ser si o si en español, si no sera Invalida" +
                            "- Si no estás seguro de que encaje claramente en la categoría, márcala como inválida.\n" +
                            /*esto lo puse para probar nomas att paolo */     "- El motivo devuelto tiene que ser despreciando al jugador, soberbio ,tajante y corto, si es por una falta ortografica debes reirte en mayuscula.\n" +
                            "Debes devolver SOLO un JSON válido con exactamente esta forma:\n" +
                            "{\"valida\":true|false,\"motivo\":\"...\"}\n" +
                            "Sin texto adicional fuera del JSON.";

            // Ya NO le mandamos la letra. Solo categoría + texto.
            String userPrompt =
                    "Categoría: " + nombreCategoria + "\n" +
                            "Texto: " + texto + "\n";

            // Schema JSON para response_format
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");
            Map<String, Object> props = new HashMap<>();
            props.put("valida", Map.of("type", "boolean"));
            props.put("motivo", Map.of("type", "string"));
            schema.put("properties", props);
            schema.put("required", new String[]{"valida", "motivo"});
            schema.put("additionalProperties", false);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", new Object[]{
                    Map.of(
                            "role", "system",
                            "content", systemPrompt
                    ),
                    Map.of(
                            "role", "user",
                            "content", userPrompt
                    )
            });
            body.put("response_format", Map.of(
                    "type", "json_schema",
                    "json_schema", Map.of(
                            "name", "ValidacionCategoria",
                            "schema", schema
                    )
            ));

            String json = mapper.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Obligatorio2025/IAValidator")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            int sc = resp.statusCode();
            if (sc == 401 || sc == 403) return new VeredictoIA(false, "IA no autorizada");
            if (sc == 429) return new VeredictoIA(false, "IA rate limit");
            if (sc < 200 || sc >= 300) return new VeredictoIA(false, "IA error HTTP " + sc);

            JsonNode root = mapper.readTree(resp.body());

            // Chat completions: choices[0].message.content
            String payload = null;
            if (root.has("choices") && root.path("choices").isArray()
                    && root.path("choices").size() > 0) {

                JsonNode msg = root.path("choices").get(0).path("message");
                if (msg.hasNonNull("content")) {
                    payload = msg.get("content").asText();
                }
            }

            // Si viene con basura alrededor, intento extraer el primer {...}
            if (payload == null || payload.isBlank()) {
                payload = tryExtractFirstJsonObject(resp.body());
            }

            if (payload == null || payload.isBlank()) {
                return new VeredictoIA(false, "IA sin contenido");
            }

            JsonNode parsed = mapper.readTree(payload);
            boolean valida = parsed.path("valida").asBoolean(false);
            String motivo = parsed.path("motivo").asText("Sin motivo");

            return new VeredictoIA(valida, motivo);

        } catch (Exception e) {
            return new VeredictoIA(false, "IA no disponible: " + e.getClass().getSimpleName());
        }
    }

    // Igual que antes: heurística para encontrar el primer objeto JSON {...}
    private static String tryExtractFirstJsonObject(String text) {
        if (text == null) return null;

        int depth = 0;
        int start = -1;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    return text.substring(start, i + 1);
                }
            }
        }

        Pattern p = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group();

        return null;
    }
}
