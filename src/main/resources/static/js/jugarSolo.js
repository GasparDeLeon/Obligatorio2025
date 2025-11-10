document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("form-jugar-solo");
    const inputAccion = document.getElementById("input-accion");
    const btnTutti = document.getElementById("btnTuttiFrutti");
    const btnRendirse = document.getElementById("btnMeRindo");
    const inputs = Array.from(document.querySelectorAll(".input-respuesta"));
    const timerElement = document.getElementById("timer");

    let submitted = false;

    // Letra con la que deben empezar las palabras
    let letra = "";
    if (inputs.length > 0) {
        const l = inputs[0].getAttribute("data-letra");
        if (l) {
            letra = l.toUpperCase();
        }
    }

    function formatearTiempo(segundos) {
        const min = Math.floor(segundos / 60);
        const seg = segundos % 60;
        const mm = String(min).padStart(2, "0");
        const ss = String(seg).padStart(2, "0");
        return mm + ":" + ss;
    }

    // Habilita o deshabilita el botón Tutti Frutti según las respuestas
    function actualizarEstadoBoton() {
        if (inputs.length === 0) {
            btnTutti.disabled = true;
            return;
        }

        const todoValido = inputs.every(input => {
            const valor = input.value.trim();
            if (!valor) {
                return false;
            }
            if (!letra) {
                return true;
            }
            return valor[0].toUpperCase() === letra;
        });

        btnTutti.disabled = !todoValido;
    }

    inputs.forEach(input => {
        input.addEventListener("input", function () {
            actualizarEstadoBoton();
        });
    });

    btnTutti.addEventListener("click", function (e) {
        e.preventDefault();
        if (submitted) return;
        inputAccion.value = "tutti-frutti";
        submitted = true;
        form.submit();
    });

    btnRendirse.addEventListener("click", function () {
        if (submitted) return;
        inputAccion.value = "rendirse";
        submitted = true;
        form.submit();
    });

    // Timer
    if (timerElement) {
        const duracionStr = timerElement.getAttribute("data-duracion");
        let restante = parseInt(duracionStr, 10);
        if (isNaN(restante) || restante <= 0) {
            restante = 60; // por si viene algo raro
        }

        timerElement.textContent = formatearTiempo(restante);

        const intervalo = setInterval(function () {
            if (restante <= 0) {
                clearInterval(intervalo);
                if (!submitted) {
                    inputAccion.value = "timeout";
                    submitted = true;
                    form.submit();
                }
                return;
            }

            restante--;
            timerElement.textContent = formatearTiempo(restante);
        }, 1000);
    }

    // Estado inicial del botón
    actualizarEstadoBoton();
});
