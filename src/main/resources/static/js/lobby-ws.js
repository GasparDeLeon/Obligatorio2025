let stompClient = null;
// Map of jugadorId -> nombre
const jugadoresEnSala = new Map();
const jugadoresListos = new Set(); // quiénes están listos

function appendLog(texto) {
    const logDiv = document.getElementById('log');
    const linea = document.createElement('div');
    linea.textContent = texto;
    logDiv.appendChild(linea);
    logDiv.scrollTop = logDiv.scrollHeight;
}

/**
 * Helper function to get display name for a player
 * Falls back to "Jugador X" if no name is stored
 */
function getNombreJugador(id) {
    const nombre = jugadoresEnSala.get(id);
    if (nombre && nombre.trim() !== '') {
        return nombre;
    }
    return 'Jugador ' + id;
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

    // Sort by player ID
    const jugadoresOrdenados = Array.from(jugadoresEnSala.entries())
        .sort((a, b) => a[0] - b[0]);

    jugadoresOrdenados.forEach(([id, nombre]) => {
        const div = document.createElement('div');
        div.className = 'jugador-item';
        const estaListo = jugadoresListos.has(id);

        if (id === 1) {
            div.classList.add('host');
        }
        if (estaListo) {
            div.classList.add('listo');
        }

        // Display the actual name, or fallback to "Jugador X"
        const nombreVisible = (nombre && nombre.trim() !== '') ? nombre : ('Jugador ' + id);
        div.textContent = nombreVisible + (estaListo ? ' ✓' : '');
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
            const nombreUsuario = payload.nombreUsuario || null;
            if (typeof jugadorId === 'number') {
                // Store name if provided, otherwise keep existing or use fallback
                if (nombreUsuario && nombreUsuario.trim() !== '') {
                    jugadoresEnSala.set(jugadorId, nombreUsuario);
                } else if (!jugadoresEnSala.has(jugadorId)) {
                    jugadoresEnSala.set(jugadorId, null);
                }
                const nombreVisible = getNombreJugador(jugadorId);
                appendLog(nombreVisible + ' se ha unido a la sala.');
                renderJugadores();
            }
            break;
        }
        case 'JUGADOR_LISTO': {
            const payload = evento.payload || {};
            const jugadorId = payload.jugadorId;
            const nombreUsuario = payload.nombreUsuario || null;
            if (typeof jugadorId === 'number') {
                // Update name if provided
                if (nombreUsuario && nombreUsuario.trim() !== '') {
                    jugadoresEnSala.set(jugadorId, nombreUsuario);
                } else if (!jugadoresEnSala.has(jugadorId)) {
                    jugadoresEnSala.set(jugadorId, null);
                }
                jugadoresListos.add(jugadorId);
                const nombreVisible = getNombreJugador(jugadorId);
                appendLog(nombreVisible + ' está listo.');
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
    // Get the current user's name from the global variable set by the server
    const nombreUsuario = window.miNombreUsuario || null;

    const socket = new SockJS('/ws-tutti');
    stompClient = Stomp.over(socket);

    // para silenciar logs:
    // stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log('Conectado al WS: ' + frame);
        const nombreMostrar = nombreUsuario || ('Jugador ' + jugadorId);
        appendLog('Conectado al WebSocket como ' + nombreMostrar +
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
            jugadorId: jugadorId,
            nombreUsuario: nombreUsuario
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
    const jugadorId = parseInt(document.getElementById('jugadorId').value, 10);
    const nombreUsuario = window.miNombreUsuario || null;

    if (!btn) return;

    const enviarMensaje = function () {
        if (!stompClient || !stompClient.connected) {
            appendLog('No estás conectado al WebSocket.');
            return;
        }

        const texto = input.value.trim();
        if (texto.length === 0) return;

        const destinoApp = '/app/sala.' + codigoSala + '.mensaje';
        const payload = {
            codigoSala: codigoSala,
            jugadorId: jugadorId,
            nombreUsuario: nombreUsuario,
            mensaje: texto
        };

        stompClient.send(destinoApp, {}, JSON.stringify(payload));
        input.value = '';
    };

    btn.addEventListener('click', enviarMensaje);

    // También permitir enviar con Enter
    input.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            enviarMensaje();
        }
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
    const nombreUsuario = window.miNombreUsuario || null;

    btn.addEventListener('click', function () {
        if (!stompClient || !stompClient.connected) {
            appendLog('No estás conectado al WebSocket.');
            return;
        }

        const payload = {
            codigoSala: codigoSala,
            jugadorId: jugadorId,
            nombreUsuario: nombreUsuario
        };

        stompClient.send('/app/sala.listo', {}, JSON.stringify(payload));
        appendLog('Marcándome como listo...');
    });
}

document.addEventListener('DOMContentLoaded', function () {
    // 1) Inicializar los jugadores que ya estaban en la sala (datos del servidor)
    // The server now sends an array of {id, nombre} objects
    if (Array.isArray(window.jugadoresIniciales)) {
        window.jugadoresIniciales.forEach(jugador => {
            if (typeof jugador === 'object' && jugador !== null && typeof jugador.id === 'number') {
                // New format: {id, nombre}
                jugadoresEnSala.set(jugador.id, jugador.nombre || null);
            } else if (typeof jugador === 'number') {
                // Backwards compatibility: just an ID
                jugadoresEnSala.set(jugador, null);
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