package com.obligatorio2025.validacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;
import com.obligatorio2025.dominio.Categoria;

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
    private final String baseUrl; // p.ej. https://api.openai.com/v1
    private final String model;   // p.ej. gpt-4o-mini
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
            // Normalizamos entrada
            if (textoRespuesta == null || textoRespuesta.trim().isEmpty()) {
                return new VeredictoIA(false, "Vacío");
            }
            String texto = textoRespuesta.trim();

            // Obtenemos el nombre de la categoría desde la CLASE dominio.Categoria
            Categoria categoria = categoriaRepositorio.buscarPorId(categoriaId);
            String nombreCategoria = (categoria != null)
                    ? categoria.getNombre()
                    : ("categoría " + categoriaId);

            // Prompt + schema para obtener JSON estricto
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("input", new Object[]{
                    Map.of(
                            "role", "system",
                            "content",
                            "Eres un juez estricto de Tutti Frutti. Devuelve SOLO JSON con " +
                                    "{\"valida\":true|false,\"motivo\":\"...\"}. " +
                                    "Valida si el texto pertenece a la categoría indicada " +
                                    "y si empieza con la letra dada. Sin explicaciones extra."
                    ),
                    Map.of(
                            "role", "user",
                            "content",
                            "Categoria: " + nombreCategoria + "\n" +
                                    "Letra: " + Character.toUpperCase(letraRonda) + "\n" +
                                    "Texto: " + texto
                    )
            });
            body.put("response_format", Map.of(
                    "type", "json_schema",
                    "json_schema", Map.of(
                            "name", "ValidacionCategoria",
                            "schema", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "valida", Map.of("type", "boolean"),
                                            "motivo", Map.of("type", "string")
                                    ),
                                    "required", new String[]{"valida", "motivo"},
                                    "additionalProperties", false
                            )
                    )
            ));

            String json = mapper.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/responses"))
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

            // 1) Camino feliz: output_text directo
            String payload = null;
            if (root.hasNonNull("output_text")) {
                payload = root.get("output_text").asText();
            }

            // 2) Camino responses.output[*].content[*].text
            if ((payload == null || payload.isBlank()) && root.has("output")) {
                for (JsonNode outItem : root.path("output")) {
                    for (JsonNode c : outItem.path("content")) {
                        if (c.hasNonNull("text")) {
                            payload = c.get("text").asText();
                            if (payload != null && !payload.isBlank()) break;
                        }
                    }
                    if (payload != null && !payload.isBlank()) break;
                }
            }

            // 3) Si aún no tenemos JSON, tratamos el body entero y extraemos el primer {...}
            if (payload == null || payload.isBlank()) {
                // intenta encontrar un objeto JSON dentro del texto de respuesta
                payload = tryExtractFirstJsonObject(resp.body());
            }

            if (payload == null || payload.isBlank()) {
                return new VeredictoIA(false, "IA sin contenido");
            }

            // Parse final del JSON del schema
            JsonNode parsed = mapper.readTree(payload);
            boolean valida = parsed.path("valida").asBoolean(false);
            String motivo = parsed.path("motivo").asText("Sin motivo");

            return new VeredictoIA(valida, motivo);

        } catch (Exception e) {
            return new VeredictoIA(false, "IA no disponible: " + e.getClass().getSimpleName());
        }
    }

    /**
     * Extrae el primer objeto JSON bien balanceado encontrado en un texto.
     * Útil cuando el modelo envía prólogo/epílogo alrededor del JSON.
     */
    private static String tryExtractFirstJsonObject(String text) {
        if (text == null) return null;

        // Heurística simple: buscar el primer bloque {...} balanceado
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

        // Plan B: regex muy permisivo (puede fallar en casos con anidados)
        Pattern p = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group();

        return null;
    }
}
