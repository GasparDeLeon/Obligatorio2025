document.addEventListener('DOMContentLoaded', function () {
    // ======================
    // INTRO CON VIDEO (solo single player)
    // ======================
    const introWrapper = document.getElementById('intro-cuenta');
    const introVideo   = document.getElementById('video-cuenta');

    // Detectar si la página se abrió por recarga (F5 / Ctrl+R) o por navegación normal
    function esRecarga() {
        if (performance.getEntriesByType) {
            const navEntries = performance.getEntriesByType('navigation');
            if (navEntries && navEntries.length > 0) {
                return navEntries[0].type === 'reload';
            }
        }
        // Fallback viejo
        if (performance.navigation) {
            return performance.navigation.type === 1; // 1 = reload
        }
        return false;
    }

    const recargada = esRecarga();
    let juegoIniciado = false;

    // ======================
    // TODA LA LÓGICA DEL JUEGO (solo + multi)
    // ======================
    function iniciarJuego() {
        if (juegoIniciado) return; // evitar doble init
        juegoIniciado = true;

        const timerEl     = document.getElementById('timer');
        const btnTutti    = document.getElementById('btn-tutti');
        const btnRendirse = document.getElementById('btn-rendirse');
        const inputs      = document.querySelectorAll('.input-respuesta');

        // 🔹 Soportar ambas pantallas: solo y multi
        const formMulti   = document.getElementById('form-jugar-multi');
        const formSolo    = document.getElementById('form-jugar-solo');
        const form        = formMulti || formSolo;

        // En solo el input se llama "input-accion"
        const inputAccion = document.getElementById('accion') || document.getElementById('input-accion');

        // 🔹 Overlay de Tutti
        const overlay     = document.getElementById('tutti-loading-overlay');

        // Letra de la ronda (la leemos del header)
        const letraSpan   = document.getElementById('letra-actual');
        const letraRonda  = letraSpan
            ? letraSpan.textContent.trim().toUpperCase()
            : null;

        // Datos de sala / jugador para el polling (solo multi)
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
                .normalize('NFD')
                .replace(/[\u0300-\u036f]/g, '')
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
                if (valor === '') return false;

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
            btnRendirse.addEventListener('click', function (e) {
                e.preventDefault(); // evitamos doble submit

                if (form.dataset.enviado) return;

                inputAccion.value = 'rendirse';
                form.dataset.enviado = 'true';

                btnRendirse.disabled = true;
                if (btnTutti) btnTutti.disabled = true;

                form.submit();
            });
        }

        if (btnTutti && form && inputAccion) {
            btnTutti.addEventListener('click', function (e) {
                e.preventDefault(); // evitamos doble submit

                if (form.dataset.enviado) return;

                inputAccion.value = 'tutti-frutti';
                form.dataset.enviado = 'true';

                if (overlay) {
                    overlay.style.display = 'flex';
                }

                btnTutti.disabled = true;
                if (btnRendirse) btnRendirse.disabled = true;

                form.submit();
            });
        }

        // ======================
        // POLLING: ¿alguien cantó Tutti Frutti? (solo multi)
        // ======================
        function chequearEstadoTutti() {
            if (!codigoSala || !jugadorIdActual || !form || !inputAccion) {
                return;
            }

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

                    if (declarado &&
                        jugadorQueCanto != null &&
                        jugadorQueCanto !== jugadorIdActual) {

                        if (!form.dataset.enviado) {
                            inputAccion.value = 'timeout'; // o "forzado-por-tutti"
                            form.dataset.enviado = 'true';
                            form.submit();
                        }
                    }
                })
                .catch(err => {
                    console.error('Error consultando estado de sala:', err);
                });
        }

        if (codigoSala && jugadorIdActual) {
            setInterval(chequearEstadoTutti, 2000);
        }
    }

    // ======================
    // LÓGICA DE LA INTRO (mostrar salvo recarga)
    // ======================
    if (introWrapper && introVideo && !recargada) {
        // overlay visible por CSS; intentamos reproducir con un pequeño delay
        setTimeout(() => {
            const playPromise = introVideo.play();
            if (playPromise !== undefined) {
                playPromise.catch(err => {
                    console.log('Autoplay bloqueado, el usuario deberá darle play al video.', err);
                });
            }
        }, 500);

        const cerrarIntroYEmpezar = () => {
            if (juegoIniciado) return;

            introWrapper.classList.add('fade-out');
            setTimeout(() => {
                introWrapper.style.display = 'none';
                iniciarJuego();
            }, 700);
        };

        // Cuando termina el video → cerrar intro y arrancar juego
        introVideo.addEventListener('ended', cerrarIntroYEmpezar);

        // Opcional: permitir click para saltear la intro
        introWrapper.addEventListener('click', cerrarIntroYEmpezar);
    } else {
        // No hay intro, o es una recarga → arrancamos directo
        if (introWrapper) {
            introWrapper.style.display = 'none';
        }
        iniciarJuego();
    }
});
