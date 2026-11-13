(function () {
    var chat = document.getElementById('chat');
    if (!chat) {
        return;
    }
    var connection = document.getElementById('connection');
    var stream = null;

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

    function disconnect() {
        if (stream) {
            stream.close();
            stream = null;
        }
    }

    function connect(url) {
        disconnect();
        var source = new EventSource(url);
        stream = source;
        source.onopen = function () {
            connection.hidden = true;
        };
        source.onerror = function () {
            if (source.readyState !== EventSource.CLOSED) {
                connection.hidden = false;
            }
        };
        source.addEventListener('text', function (event) {
            var box = chat.querySelector('.answer-text');
            if (box) {
                box.insertAdjacentHTML('beforeend', event.data);
            }
        });
        source.addEventListener('state', function (event) {
            var state = JSON.parse(event.data).state;
            setBusy(state === 'started');
            if (state !== 'started') {
                source.close();
                if (stream === source) {
                    stream = null;
                }
            }
        });
    }

    function connectAnswer(scope) {
        var answer = (scope || chat).querySelector('[data-sse-url]');
        if (answer) {
            connect(answer.getAttribute('data-sse-url'));
        }
    }

    document.body.addEventListener('htmx:afterSwap', function (event) {
        if (event.detail.target.id === 'thread') {
            connectAnswer(event.detail.target);
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

    connectAnswer(document);
})();
