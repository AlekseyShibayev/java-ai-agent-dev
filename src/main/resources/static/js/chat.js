(function () {
    var chat = document.getElementById('chat');
    if (!chat) {
        return;
    }
    var connection = document.getElementById('connection');

    function setBusy(busy) {
        var form = document.getElementById('composer');
        if (!form) {
            return;
        }
        form.querySelectorAll('textarea, #send').forEach(function (element) {
            element.disabled = busy;
        });
        var stop = form.querySelector('#stop');
        if (stop) {
            stop.disabled = !busy;
        }
    }

    document.body.addEventListener('htmx:sseOpen', function () {
        connection.hidden = true;
    });

    document.body.addEventListener('htmx:sseError', function () {
        connection.hidden = false;
    });

    document.body.addEventListener('htmx:sseMessage', function (event) {
        if (event.detail.type !== 'state') {
            return;
        }
        try {
            setBusy(JSON.parse(event.detail.data).state === 'started');
        } catch (ignored) {
            // Unknown payload: the next state event fixes the composer.
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Enter' || event.shiftKey || event.isComposing) {
            return;
        }
        var form = event.target.closest('form#composer');
        if (!form || event.target.disabled) {
            return;
        }
        event.preventDefault();
        form.requestSubmit();
    });
})();
