document.addEventListener('DOMContentLoaded', function () {
    const timerEl     = document.getElementById('timer');
    const btnTutti    = document.getElementById('btn-tutti');
    const btnRendirse = document.getElementById('btn-rendirse');
    const inputs      = document.querySelectorAll('.input-respuesta');

    const form        = document.getElementById('form-jugar-multi');
    const inputAccion = document.getElementById('accion');

    const codigoSalaInput = document.getElementById('codigoSala');
    const jugadorIdInput  = document.getElementById('jugadorId');

    const codigoSala = codigoSalaInput ? codigoSalaInput.value : null;
    const jugadorId  = jugadorIdInput ? parseInt(jugadorIdInput.value, 10) : null;

    // Letra de la ronda (la leemos del header)
    const letraSpan  = document.getElementById('letra-actual');
    const letraRonda = letraSpan
        ? letraSpan.textContent.trim().toUpperCase()
        : null;

    // Referencias a intervalos para poder frenarlos
    let countdownInterval = null;
    let estadoInterval    = null; // ya no lo usaremos, pero lo dejamos por compatibilidad

    function detenerTimers() {
        if (countdownInterval) {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }
        if (estadoInterval) {
            clearInterval(estadoInterval);
            estadoInterval = null;
        }
    }

    // ======================
    // RELOJ / COUNTDOWN
    // ======================
    if (timerEl && form && inputAccion) {
        const raw = timerEl.getAttribute('data-duracion');
        let duracion = parseInt(raw, 10);

        if (isNaN(duracion) || duracion <= 0) {
            duracion = 60;
        }

        let restante = duracion;

        function renderTiempo() {
            const m = String(Math.floor(restante / 60)).padStart(2, '0');
            const s = String(restante % 60).padStart(2, '0');
            timerEl.textContent = m + ':' + s;
        }

        renderTiempo();

        countdownInterval = setInterval(function () {
            restante--;

            if (restante <= 0) {
                restante = 0;
                renderTiempo();
                clearInterval(countdownInterval);
                countdownInterval = null;

                // si todavía no se envió el formulario, lo mandamos como timeout
                if (!form.dataset.enviado) {
                    inputAccion.value = 'timeout';
                    form.dataset.enviado = 'true';
                    detenerTimers();
                    form.submit();
                }
                return;
            }

            renderTiempo();
        }, 1000);
    }

    // ======================
    // Helpers de validación
    // ======================
    function normalizarPrimeraLetra(texto) {
        if (!texto) return null;

        const limpio = texto.trim()
            .normalize("NFD")                    // separa acentos
            .replace(/[\u0300-\u036f]/g, "")     // elimina acentos
            .toUpperCase();

        return limpio.length > 0 ? limpio.charAt(0) : null;
    }

    // ======================
    // HABILITAR BOTÓN "TUTTI FRUTTI"
    // ======================
    function actualizarEstadoTutti() {
        if (!btnTutti || inputs.length === 0) return;

        const puedeTutti = Array.from(inputs).every(inp => {
            const valor = inp.value.trim();
            if (valor === "") return false;

            // si no tenemos letra de referencia, solo pedimos que estén llenos
            if (!letraRonda) return true;

            const primera = normalizarPrimeraLetra(valor);
            return primera === letraRonda;
        });

        btnTutti.disabled = !puedeTutti;
    }

    inputs.forEach(inp => {
        inp.addEventListener('input', actualizarEstadoTutti);
    });
    actualizarEstadoTutti();

    // ======================
    // WebSocket / STOMP
    // ======================
    let stompClient = null;

    function manejarTuttiFruttiDeclarado(payload) {
        if (!payload || typeof payload.jugadorId === 'undefined') {
            return;
        }

        const jugadorQueCanto = payload.jugadorId;

        // Si fui yo, ya estoy enviando el formulario; no hago nada más
        if (jugadorQueCanto === jugadorId) {
            return;
        }

        // Si fue otro jugador y yo todavía no mandé el formulario, me fuerzan a terminar
        if (form && !form.dataset.enviado) {
            inputAccion.value = 'timeout';
            form.dataset.enviado = 'true';
            detenerTimers();
            form.submit();
        }
    }

    function manejarEventoSala(evento) {
        if (!evento || !evento.tipo) return;

        switch (evento.tipo) {
            case 'TUTTI_FRUTTI_DECLARADO':
                manejarTuttiFruttiDeclarado(evento.payload);
                break;
            // A futuro:
            // case 'RESULTADOS_RONDA_LISTOS':
            // case 'RONDA_INICIA':
            // case 'PARTIDA_FINALIZADA':
            //   ...
        }
    }

    function conectarWS() {
        if (!codigoSala) return;

        // Asumimos que SockJS y Stomp ya están incluidos en la página
        const socket = new SockJS('/ws-tutti');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function () {
            const destino = '/topic/sala.' + codigoSala;

            stompClient.subscribe(destino, function (message) {
                try {
                    const evento = JSON.parse(message.body);
                    manejarEventoSala(evento);
                } catch (e) {
                    console.error('Error parseando evento de sala (jugarMulti):', e, message.body);
                }
            });
        }, function (error) {
            console.error('Error de conexión STOMP en jugarMulti:', error);
        });
    }

    // ======================
    // BOTONES
    // ======================
    if (btnRendirse && form && inputAccion) {
        btnRendirse.addEventListener('click', function () {
            if (form.dataset.enviado) return;

            inputAccion.value = 'rendirse';
            form.dataset.enviado = 'true';
            detenerTimers();
            form.submit();
        });
    }

    if (btnTutti && form && inputAccion) {
        btnTutti.addEventListener('click', function () {
            if (form.dataset.enviado) return;

            inputAccion.value = 'tutti-frutti';
            form.dataset.enviado = 'true';
            detenerTimers();
            form.submit();
        });
    }

    // ======================
    // Inicio: conectar WS si tengo sala y jugador
    // ======================
    if (codigoSala && jugadorId) {
        conectarWS();
    }
});
