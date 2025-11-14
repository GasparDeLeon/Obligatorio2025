let stompClient = null;

function appendLog(texto) {
    const logDiv = document.getElementById('log');
    const linea = document.createElement('div');
    linea.textContent = texto;
    logDiv.appendChild(linea);
    logDiv.scrollTop = logDiv.scrollHeight;
}

function conectarLobby() {
    const codigoSala = document.getElementById('codigoSala').value;
    const jugadorId = parseInt(document.getElementById('jugadorId').value, 10);

    const socket = new SockJS('/ws-tutti');
    stompClient = Stomp.over(socket);

    // si no querés ver tanto ruido en consola:
    // stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log('Conectado al WS: ' + frame);
        appendLog('Conectado al WebSocket como jugador ' + jugadorId +
            ' en sala ' + codigoSala);

        // 1) Suscribirse a la sala: /topic/sala.{codigo}
        const destinoSala = '/topic/sala.' + codigoSala;
        stompClient.subscribe(destinoSala, function (message) {
            const body = message.body;
            console.log('Mensaje recibido en ' + destinoSala + ': ' + body);
            appendLog(body);
        });

        // 2) Enviar mensaje de "unirse" al servidor
        const joinPayload = {
            codigoSala: codigoSala,
            jugadorId: jugadorId
        };

        stompClient.send('/app/sala.unirse', {}, JSON.stringify(joinPayload));
        appendLog('Avisando al servidor que me uní a la sala...');
    }, function (error) {
        console.error('Error de conexión WS', error);
        appendLog('Error de conexión WebSocket: ' + error);
    });
}

function configurarBotonEnviar() {
    const btn = document.getElementById('btn-enviar');
    const input = document.getElementById('input-mensaje');
    const codigoSala = document.getElementById('codigoSala').value;

    btn.addEventListener('click', function () {
        if (!stompClient || !stompClient.connected) {
            appendLog('No estás conectado al WebSocket.');
            return;
        }

        const texto = input.value.trim();
        if (texto.length === 0) return;

        // Enviamos al destino /app/sala.{codigo}.mensaje
        const destinoApp = '/app/sala.' + codigoSala + '.mensaje';
        stompClient.send(destinoApp, {}, texto);

        input.value = '';
    });
}

// Cuando cargue el DOM, conectamos y configuramos eventos
document.addEventListener('DOMContentLoaded', function () {
    conectarLobby();
    configurarBotonEnviar();
});
