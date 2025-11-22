// /js/esperandoRondaMulti.js

document.addEventListener("DOMContentLoaded", function () {
    const codigoSalaInput = document.getElementById("codigoSala");
    const numeroRondaInput = document.getElementById("numeroRonda");
    const jugadorIdInput = document.getElementById("jugadorId");

    if (!codigoSalaInput || !numeroRondaInput || !jugadorIdInput) {
        console.error("[esperando] No se encontraron los inputs ocultos");
        return;
    }

    const codigoSala = codigoSalaInput.value;
    const numeroRonda = numeroRondaInput.value;
    const jugadorId = jugadorIdInput.value;

    if (!codigoSala || !numeroRonda || !jugadorId) {
        console.error("[esperando] Valores inválidos en los inputs ocultos", {
            codigoSala,
            numeroRonda,
            jugadorId
        });
        return;
    }

    console.log("[esperando] sala =", codigoSala,
        "ronda =", numeroRonda,
        "jugador =", jugadorId);

    function irAResultados() {
        const url = `/multi/resultados?codigoSala=${encodeURIComponent(codigoSala)}`
            + `&ronda=${encodeURIComponent(numeroRonda)}`
            + `&jugadorId=${encodeURIComponent(jugadorId)}`;

        console.log("[esperando] Redirigiendo a", url);
        window.location.href = url;
    }

    // -----------------------------
    // WebSocket / STOMP
    // -----------------------------
    let stompClient = null;

    try {
        const socket = new SockJS("/ws-tutti"); // mismo endpoint que en lobby y jugarMulti

        stompClient = Stomp.over(socket);

        // opcional: silenciar logs
        stompClient.debug = null;

        stompClient.connect({}, function () {
            const topic = "/topic/sala." + codigoSala;
            console.log("[esperando] Conectado STOMP, suscribiendo a", topic);

            stompClient.subscribe(topic, function (message) {
                console.log("[esperando] Mensaje WS:", message.body);
                try {
                    const evento = JSON.parse(message.body);

                    if (evento.tipo === "RONDA_FINALIZADA") {
                        console.log("[esperando] Evento RONDA_FINALIZADA recibido");
                        irAResultados();
                    }

                } catch (e) {
                    console.error("[esperando] Error procesando mensaje de sala:", e);
                }
            });
        }, function (error) {
            console.error("[esperando] Error conectando STOMP:", error);
        });
    } catch (e) {
        console.error("[esperando] No se pudo inicializar STOMP:", e);
    }

    // -----------------------------
    // Polling HTTP de respaldo
    // -----------------------------
    function chequearEstadoRonda() {
        const url = `/multi/estado-ronda?codigoSala=${encodeURIComponent(codigoSala)}`
            + `&ronda=${encodeURIComponent(numeroRonda)}`;

        fetch(url)
            .then(r => {
                if (!r.ok) throw new Error("HTTP " + r.status);
                return r.json();
            })
            .then(data => {
                console.log("[esperando] estado-ronda:", data);
                if (data.finalizada) {
                    console.log("[esperando] Ronda finalizada detectada por HTTP");
                    irAResultados();
                }
            })
            .catch(err => {
                console.error("[esperando] Error en estado-ronda:", err);
            });
    }

    // Primera consulta a los 3s, luego cada 5s
    setTimeout(chequearEstadoRonda, 3000);
    setInterval(chequearEstadoRonda, 5000);
});
