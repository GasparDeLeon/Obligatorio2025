// src/main/resources/static/js/jugarSolo.js

document.addEventListener('DOMContentLoaded', function () {
    const timerEl      = document.getElementById('timer');
    const form         = document.getElementById('form-jugar-solo');
    const inputAccion  = document.getElementById('input-accion');
    const btnTutti     = document.getElementById('btn-tutti');
    const btnRendirse  = document.getElementById('btn-rendirse');
    const inputs       = document.querySelectorAll('.input-respuesta');

    if (!form) {
        // por las dudas, pero en tu vista siempre debería existir
        return;
    }

    // ======================
    // RELOJ / COUNTDOWN
    // ======================
    if (timerEl) {
        // leemos el atributo data-duracion que pone Thymeleaf
        const raw = timerEl.getAttribute('data-duracion');
        let duracion = parseInt(raw, 10);

        if (isNaN(duracion) || duracion <= 0) {
            duracion = 60; // fallback por si viene raro
        }

        let restante = duracion;

        function renderTiempo() {
            const m = String(Math.floor(restante / 60)).padStart(2, '0');
            const s = String(restante % 60).padStart(2, '0');
            timerEl.textContent = `${m}:${s}`;
        }

        // mostramos el valor inicial
        renderTiempo();

        const intervalo = setInterval(function () {
            restante--;

            if (restante <= 0) {
                restante = 0;
                renderTiempo();
                clearInterval(intervalo);

                // si todavía no se envió el formulario, lo mandamos como timeout
                if (inputAccion && !form.dataset.enviado) {
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

        const todasLlenas = Array.from(inputs)
            .every(inp => inp.value.trim() !== '');

        btnTutti.disabled = !todasLlenas;
    }

    inputs.forEach(inp => {
        inp.addEventListener('input', actualizarEstadoTutti);
    });
    actualizarEstadoTutti(); // cálculo inicial

    // ======================
    // BOTONES (por si no usás inline onclick)
    // ======================
    if (btnRendirse && inputAccion) {
        btnRendirse.addEventListener('click', function () {
            inputAccion.value = 'rendirse';
            form.submit();
        });
    }

    if (btnTutti && inputAccion) {
        btnTutti.addEventListener('click', function () {
            inputAccion.value = 'tutti-frutti';
            form.submit();
        });
    }
});
