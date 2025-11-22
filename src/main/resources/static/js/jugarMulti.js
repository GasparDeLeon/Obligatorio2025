document.addEventListener('DOMContentLoaded', function () {
    const timerEl     = document.getElementById('timer');
    const btnTutti    = document.getElementById('btn-tutti');
    const btnRendirse = document.getElementById('btn-rendirse');
    const inputs      = document.querySelectorAll('.input-respuesta');

    const form        = document.getElementById('form-jugar-multi');
    const inputAccion = document.getElementById('accion');

    const codigoSalaInput   = document.getElementById('codigoSala');
    const jugadorIdInput    = document.getElementById('jugadorId');
    const numeroRondaInput  = document.getElementById('numeroRonda');

    const codigoSala  = codigoSalaInput ? codigoSalaInput.value : null;
    const jugadorId   = jugadorIdInput ? parseInt(jugadorIdInput.value, 10) : null;
    const numeroRonda = numeroRondaInput ? parseInt(numeroRondaInput.value, 10) : 1;

    // Letra de la ronda (del header)
    const letraSpan  = document.getElementById('letra-actual');
    const letraRonda = letraSpan
        ? letraSpan.textContent.trim().toUpperCase()
        : null;

    // Timers
    let countdownInterval = null;
    let estadoInterval    = null; // por si después lo volvés a usar
    let graciaInterval    = null;

    // Config de tiempos
    let duracion = 60;
    let duracionGracia = 0;
    let graciaHabilitada = false;

    if (timerEl) {
        const rawDur = timerEl.getAttribute('data-duracion');
        let tmp = parseInt(rawDur, 10);
        if (!isNaN(tmp) && tmp > 0) {
            duracion = tmp;
        }

        const rawGracia = timerEl.getAttribute('data-duracion-gracia');
        tmp = parseInt(rawGracia, 10);
        if (!isNaN(tmp) && tmp > 0) {
            duracionGracia = tmp;
        }

        const rawHabilitada = timerEl.getAttribute('data-gracia-habilitada');
        graciaHabilitada = (rawHabilitada === 'true');
    }

    let restante = duracion;
    let enGracia = false;
    let graciaRestante = 0;

    function detenerTimers() {
        if (countdownInterval) {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }
        if (estadoInterval) {
            clearInterval(estadoInterval);
            estadoInterval = null;
        }
        if (graciaInterval) {
            clearInterval(graciaInterval);
            graciaInterval = null;
        }
    }

    function renderTiempo(seg) {
        if (!timerEl) return;
        const m = String(Math.floor(seg / 60)).padStart(2, '0');
        const s = String(seg % 60).padStart(2, '0');
        timerEl.textContent = m + ':' + s;
    }

    // ======================
    // RELOJ NORMAL
    // ======================
    if (timerEl && form && inputAccion) {
        renderTiempo(restante);

        countdownInterval = setInterval(function () {
            restante--;

            if (restante <= 0) {
                restante = 0;
                renderTiempo(restante);
                clearInterval(countdownInterval);
                countdownInterval = null;

                // Caso: nadie declaró Tutti Frutti y se acaba el tiempo normal
                if (!enGracia && !form.dataset.enviado) {
                    inputAccion.value = 'timeout';
                    form.dataset.enviado = 'true';
                    detenerTimers();
                    form.submit();
                }
                return;
            }

            renderTiempo(restante);
        }, 1000);
    }

    // ======================
    // Helpers de validación de letra
    // ======================
    function normalizarPrimeraLetra(texto) {
        if (!texto) return null;

        const limpio = texto.trim()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .toUpperCase();

        return limpio.length > 0 ? limpio.charAt(0) : null;
    }

    function actualizarEstadoTutti() {
        if (!btnTutti || inputs.length === 0) return;

        const puedeTutti = Array.from(inputs).every(inp => {
            const valor = inp.value.trim();
            if (valor === "") return false;

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
    // GRACIA (solo para los que NO cantaron)
    // ======================
    function iniciarGracia() {
        if (enGracia) return;
        enGracia = true;

        // cortamos el reloj normal en esta pestaña
        if (countdownInterval) {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }

        // si no hay gracia configurada, mantenemos comportamiento viejo:
        if (!graciaHabilitada || duracionGracia <= 0) {
            if (form && !form.dataset.enviado) {
                inputAccion.value = 'timeout';
                form.dataset.enviado = 'true';
                detenerTimers();
                form.submit();
            }
            return;
        }

        graciaRestante = duracionGracia;
        renderTiempo(graciaRestante);

        graciaInterval = setInterval(function () {
            graciaRestante--;

            if (graciaRestante <= 0) {
                graciaRestante = 0;
                renderTiempo(graciaRestante);
                clearInterval(graciaInterval);
                graciaInterval = null;

                // Al terminar la gracia, si todavía no mandé el form -> lo mando
                if (form && !form.dataset.enviado) {
                    inputAccion.value = 'timeout'; // semánticamente "se me acabó el tiempo"
                    form.dataset.enviado = 'true';
                    detenerTimers();
                    form.submit();
                }
                return;
            }

            renderTiempo(graciaRestante);
        }, 1000);
    }

    // ======================
    // WebSocket / STOMP
    // ======================
    let stompClient = null;

    function manejarTuttiFruttiDeclarado(payload) {
        if (!payload || typeof payload.jugadorId === 'undefined') {
            return;
        }

        const jugadorQueCanto = payload.jugadorId;

        // Si fui yo el que cantó:
        // ya mandé el formulario y me voy a "esperando", no hago nada acá.
        if (jugadorQueCanto === jugadorId) {
            return;
        }

        console.log('[jugarMulti] Tutti Frutti declarado por jugador', jugadorQueCanto,
                    '-> inicio gracia para esta pestaña');

        // Para los demás jugadores: inicio de gracia
        iniciarGracia();
    }

    function manejarEventoSala(evento) {
        if (!evento || !evento.tipo) return;

        switch (evento.tipo) {
            case 'TUTTI_FRUTTI_DECLARADO':
                manejarTuttiFruttiDeclarado(evento.payload);
                break;
            // Aquí podrías agregar otros eventos más adelante
        }
    }

    function conectarWS() {
        if (!codigoSala) return;

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

            // comportamiento original: este jugador manda el formulario como "tutti-frutti"
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
