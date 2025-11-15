let stompClient = null;
const jugadoresEnSala = new Set();

function appendLog(texto) {
    const logDiv = document.getElementById('log');
    const linea = document.createElement('div');
    linea.textContent = texto;
    logDiv.appendChild(linea);
    logDiv.scrollTop = logDiv.scrollHeight;
}

function renderJugadores() {
    const ul = document.getElementById('lista-jugadores');
    ul.innerHTML = '';

    if (jugadoresEnSala.size === 0) {
        const li = document.createElement('li');
        li.textContent = '(Todavía no hay jugadores en la sala)';
        ul.appendChild(li);
        return;
    }

    Array.from(jugadoresEnSala)
        .sort((a, b) => a - b)
        .forEach(id => {
            const li = document.createElement('li');
            li.textContent = 'Jugador ' + id;
            ul.appendChild(li);
        });
}

// NUEVO: leer jugadores iniciales del hidden "jugadoresActuales"
function inicializarJugadoresDesdeHtml() {
    const hidden = document.getElementById('jugadoresActuales');
    if (!hidden) return;

    const value = hidden.value;
    if (!value) return; // puede venir vacío

    value
        .split(',')
        .map(v => parseInt(v, 10))
        .filter(n => !isNaN(n))
        .forEach(id => jugadoresEnSala.add(id));
}

function manejarEventoSala(evento) {
    if (!evento || !evento.tipo) {
        appendLog('Evento desconocido: ' + JSON.stringify(evento));
        return;
    }

    switch (evento.tipo) {
        case 'JUGADOR_ENTRA': {
            const payload = evento.payload || {};
            const jugadorId = payload.jugadorId;
            if (typeof jugadorId === 'number') {
                jugadoresEnSala.add(jugadorId);
                appendLog('Jugador ' + jugadorId + ' se ha unido a la sala.');
                renderJugadores();
            }
            break;
        }
        case 'PARTIDA_INICIA': {
            const payload = evento.payload || {};
            const codigo = payload.codigoSala || '';
            appendLog('La partida ha sido iniciada en la sala ' + (codigo || '') + '.');
            // más adelante podrías redirigir a la pantalla de juego
            break;
        }
        default:
            appendLog('Evento [' + evento.tipo + '] recibido: ' + JSON.stringify(evento.payload));
            break;
    }
}

function conectarLobby() {
    const codigoSala = document.getElementById('codigoSala').value;
    const jugadorId = parseInt(document.getElementById('jugadorId').value, 10);

    const socket = new SockJS('/ws-tutti');
    stompClient = Stomp.over(socket);

    // si querés silenciar logs:
    // stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log('Conectado al WS: ' + frame);
        appendLog('Conectado al WebSocket como jugador ' + jugadorId +
            ' en sala ' + codigoSala);

        const destinoSala = '/topic/sala.' + codigoSala;
        stompClient.subscribe(destinoSala, function (message) {
            const body = message.body;
            console.log('Mensaje recibido en ' + destinoSala + ': ', body);

            let parsed = null;
            try {
                parsed = JSON.parse(body);
            } catch (e) {
                parsed = null;
            }

            if (parsed && typeof parsed === 'object' && parsed.tipo) {
                manejarEventoSala(parsed);
            } else {
                appendLog(body);
            }
        });

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

    if (!btn) return;

    btn.addEventListener('click', function () {
        if (!stompClient || !stompClient.connected) {
            appendLog('No estás conectado al WebSocket.');
            return;
        }

        const texto = input.value.trim();
        if (texto.length === 0) return;

        const destinoApp = '/app/sala.' + codigoSala + '.mensaje';
        stompClient.send(destinoApp, {}, texto);

        input.value = '';
    });
}

function configurarBotonIniciar() {
    const btn = document.getElementById('btn-iniciar');
    if (!btn) return; // si no es host, no hay botón

    const codigoSala = document.getElementById('codigoSala').value;
    const jugadorId = parseInt(document.getElementById('jugadorId').value, 10);

    btn.addEventListener('click', function () {
        if (!stompClient || !stompClient.connected) {
            appendLog('No estás conectado al WebSocket.');
            return;
        }

        const payload = {
            codigoSala: codigoSala,
            jugadorId: jugadorId
        };

        stompClient.send('/app/sala.iniciar', {}, JSON.stringify(payload));
        appendLog('Solicitando inicio de partida al servidor...');
    });
}

document.addEventListener('DOMContentLoaded', function () {
    inicializarJugadoresDesdeHtml(); // 1) cargo lo que vino del servidor
    renderJugadores();               // 2) pinto la lista inicial
    conectarLobby();                 // 3) me conecto al WS
    configurarBotonEnviar();         // 4) chat
    configurarBotonIniciar();        // 5) botón iniciar (si es host)
});
