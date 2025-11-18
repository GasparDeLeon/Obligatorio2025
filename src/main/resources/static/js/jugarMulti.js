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
    let estadoInterval    = null;

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
    // POLLING: ESTADO DE LA SALA
    // ======================
    function chequearEstadoSala() {
        // si ya se envió el form, frenamos polling
        if (!form || form.dataset.enviado) {
            detenerTimers();
            return;
        }

        if (!codigoSala || !jugadorId) {
            return;
        }

        fetch('/multi/estado?codigoSala=' + encodeURIComponent(codigoSala))
            .then(function (resp) {
                if (!resp.ok) {
                    throw new Error('HTTP ' + resp.status);
                }
                return resp.json();
            })
            .then(function (data) {
                if (!data || data.existe === false) {
                    return;
                }

                // Si alguien cantó tutti frutti y NO fui yo, me fuerzan a terminar
                if (data.tuttiFruttiDeclarado &&
                    data.jugadorQueCantoTutti != null &&
                    data.jugadorQueCantoTutti !== jugadorId) {

                    if (!form.dataset.enviado) {
                        inputAccion.value = 'timeout';
                        form.dataset.enviado = 'true';
                        detenerTimers();
                        form.submit();
                    }
                }
            })
            .catch(function (err) {
                console.error('Error consultando estado de sala:', err);
            });
    }

    if (codigoSala && jugadorId && form && inputAccion) {
        // cada 1.5 segundos
        estadoInterval = setInterval(chequearEstadoSala, 1500);
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
});
