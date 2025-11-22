document.addEventListener('DOMContentLoaded', function () {
    const timerEl     = document.getElementById('timer');
    const btnTutti    = document.getElementById('btn-tutti');
    const btnRendirse = document.getElementById('btn-rendirse');
    const inputs      = document.querySelectorAll('.input-respuesta');
    const overlay = document.getElementById('tutti-loading-overlay');

    const form =
        document.getElementById('form-jugar-solo');
    const inputAccion =
        document.getElementById('accion') ||
        document.getElementById('input-accion');


    // Letra de la ronda (la leemos del header)
    const letraSpan   = document.getElementById('letra-actual');
    const letraRonda  = letraSpan
        ? letraSpan.textContent.trim().toUpperCase()
        : null;

    // Datos de sala / jugador para el polling
    const codigoSalaInput = document.getElementById('codigoSala');
    const jugadorIdInput  = document.getElementById('jugadorId');

    const codigoSala = codigoSalaInput ? codigoSalaInput.value : null;
    const jugadorIdActual = jugadorIdInput
        ? parseInt(jugadorIdInput.value, 10)
        : null;

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

        const intervalo = setInterval(function () {
            restante--;

            if (restante <= 0) {
                restante = 0;
                renderTiempo();
                clearInterval(intervalo);

                if (!form.dataset.enviado) {
                    inputAccion.value = 'timeout';
                    form.dataset.enviado = 'true';
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
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
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
    // BOTONES
    // ======================
    if (btnRendirse && form && inputAccion) {
        btnRendirse.addEventListener('click', function () {
            if (form.dataset.enviado) return;

            inputAccion.value = 'rendirse';
            form.dataset.enviado = 'true';
            form.submit();
        });
    }

if (btnTutti && form && inputAccion) {
    btnTutti.addEventListener('click', function () {
        if (form.dataset.enviado) return;

        inputAccion.value = 'tutti-frutti';
        form.dataset.enviado = 'true';

        // Mostrar overlay
        if (overlay) {
            overlay.style.display = 'flex';
        }

        form.submit();
    });
}

    // ======================
    // POLLING: ¿alguien cantó Tutti Frutti?
    // ======================
    function chequearEstadoTutti() {
        if (!codigoSala || !jugadorIdActual || !form || !inputAccion) {
            return;
        }

        // si ya mandamos el formulario, no seguimos chequeando
        if (form.dataset.enviado) {
            return;
        }

        fetch(`/multi/estado?codigoSala=${encodeURIComponent(codigoSala)}`)
            .then(resp => {
                if (!resp.ok) throw new Error('Error HTTP ' + resp.status);
                return resp.json();
            })
            .then(data => {
                if (!data) return;

                const declarado = !!data.tuttiFruttiDeclarado;
                const jugadorQueCanto = data.jugadorQueCantoTutti;

                // si ya se declaró tutti y NO fui yo, cierro mi ronda
                if (declarado &&
                    jugadorQueCanto != null &&
                    jugadorQueCanto !== jugadorIdActual) {

                    if (!form.dataset.enviado) {
                        inputAccion.value = 'timeout'; // o algún valor tipo "forzado-por-tutti"
                        form.dataset.enviado = 'true';
                        form.submit();
                    }
                }
            })
            .catch(err => {
                // por ahora solo log en consola
                console.error('Error consultando estado de sala:', err);
            });
    }

    // cada 2 segundos preguntamos si alguien cantó tutti
    setInterval(chequearEstadoTutti, 2000);
});
