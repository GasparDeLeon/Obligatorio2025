document.addEventListener('DOMContentLoaded', function () {
    const timerEl     = document.getElementById('timer');
    const btnTutti    = document.getElementById('btn-tutti');
    const btnRendirse = document.getElementById('btn-rendirse');
    const inputs      = document.querySelectorAll('.input-respuesta');

    const form        = document.getElementById('form-jugar-multi');
    const inputAccion = document.getElementById('accion');

    const letraEl     = document.getElementById('letraActual');
    const letraRonda  = letraEl ? letraEl.value.trim().toUpperCase() : null;

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

                // si todavía no se envió el formulario, lo mandamos como timeout
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
    // HABILITAR BOTÓN "TUTTI FRUTTI"
    // ======================
    function actualizarEstadoTutti() {
        if (!btnTutti || inputs.length === 0) return;

        const validarLetra = !!letraRonda;
        let todasValidas = true;

        for (const inp of inputs) {
            const texto = inp.value.trim();

            // todas deben estar llenas
            if (texto === '') {
                todasValidas = false;
                break;
            }

            if (validarLetra) {
                const primera = texto.charAt(0).toUpperCase();
                if (primera !== letraRonda) {
                    todasValidas = false;
                    break;
                }
            }
        }

        btnTutti.disabled = !todasValidas;
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
            form.submit();
        });
    }
});
