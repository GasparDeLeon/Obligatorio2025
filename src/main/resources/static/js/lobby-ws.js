let stompClient = null;
const jugadoresEnSala = new Set();
const jugadoresListos = new Set(); // quiénes están listos

function appendLog(texto) {
    const logDiv = document.getElementById('log');
    const linea = document.createElement('div');
    linea.textContent = texto;
    logDiv.appendChild(linea);
    logDiv.scrollTop = logDiv.scrollHeight;
}

function renderJugadores() {
    const contenedor = document.getElementById('lista-jugadores');
    contenedor.innerHTML = '';

    if (jugadoresEnSala.size === 0) {
        const div = document.createElement('div');
        div.className = 'jugador-item';
        div.textContent = '(Todavía no hay jugadores en la sala)';
        contenedor.appendChild(div);
        return;
    }

    Array.from(jugadoresEnSala)
        .sort((a, b) => a - b)
        .forEach(id => {
            const div = document.createElement('div');
            div.className = 'jugador-item';
            const estaListo = jugadoresListos.has(id);

            if (id === 1) {
                div.classList.add('host');
            }
            if (estaListo) {
                div.classList.add('listo');
            }

            div.textContent = 'Jugador ' + id + (estaListo ? ' ✓' : '');
            contenedor.appendChild(div);
        });
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
        case 'JUGADOR_LISTO': {
            const payload = evento.payload || {};
            const jugadorId = payload.jugadorId;
            if (typeof jugadorId === 'number') {
                jugadoresEnSala.add(jugadorId); // por si acaso aún no estaba
                jugadoresListos.add(jugadorId);
                appendLog('Jugador ' + jugadorId + ' está listo.');
                renderJugadores();
            }
            break;
        }
        case 'PARTIDA_INICIA': {
            const payload = evento.payload || {};
            const codigo = payload.codigoSala || '';
            appendLog('La partida ha sido iniciada en la sala ' + (codigo || '') + '.');
            // más adelante acá podríamos redirigir a la pantalla de juego
            break;
        }
        case 'ERROR_INICIO': {
            const payload = evento.payload || {};
            const mensaje = payload.mensaje || 'No se pudo iniciar la partida.';

            // id del jugador actual (el que está viendo esta pestaña)
            const jugadorActualId = parseInt(
                document.getElementById('jugadorId').value,
                10
            );

            // Solo el host (jugador 1) ve el alert y el log explícito
            if (jugadorActualId === 1) {
                appendLog('[ERROR] ' + mensaje);
                alert(mensaje);
            } else {
                // para los demás no mostramos nada,
                // o podrías dejar un log suave si querés
                // appendLog('[INFO] El host intentó iniciar la partida pero faltan jugadores listos.');
            }

            break;
        }
               case 'RONDA_INICIA': {
                   const payload = evento.payload || {};
                   const numero = payload.numero;
                   const letra = payload.letra;

                   appendLog('Comienza la ronda ' + numero + ' con la letra ' + letra);

                   // Redirigimos a la pantalla de juego multijugador
                   const codigoSala = document.getElementById('codigoSala').value;
                   const jugadorId = parseInt(
                       document.getElementById('jugadorId').value,
                       10
                   );

                   const url = '/multi/ronda'
                       + '?codigoSala=' + encodeURIComponent(codigoSala)
                       + '&jugadorId=' + encodeURIComponent(jugadorId)
                       + '&ronda=' + encodeURIComponent(numero);

                   window.location.href = url;

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

    // para silenciar logs:
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

function configurarBotonListo() {
    const btn = document.getElementById('btn-listo');
    if (!btn) return;

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

        stompClient.send('/app/sala.listo', {}, JSON.stringify(payload));
        appendLog('Marcándome como listo...');
    });
}

document.addEventListener('DOMContentLoaded', function () {
    // 1) Inicializar los jugadores que ya estaban en la sala (datos del servidor)
    if (Array.isArray(window.jugadoresIniciales)) {
        window.jugadoresIniciales.forEach(id => {
            if (typeof id === 'number') {
                jugadoresEnSala.add(id);
            }
        });
    }

    // 2) Render inicial de la lista
    renderJugadores();

    // 3) Conectar WebSocket y configurar botones
    conectarLobby();
    configurarBotonEnviar();
    configurarBotonIniciar();
    configurarBotonListo();
});