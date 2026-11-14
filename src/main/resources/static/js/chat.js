(function () {
    var chat = document.getElementById('chat');
    if (!chat) {
        return;
    }
    var connection = document.getElementById('connection');
    var stream = null;
    var run = emptyRun();

    function emptyRun() {
        return {
            reasoning: null,
            tools: {},
            cost: 0,
            input: 0,
            output: 0,
            reasoningTokens: 0,
            cache: 0,
            interrupted: false
        };
    }

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

    function answer() {
        return chat.querySelector('.answer');
    }

    function steps() {
        var box = answer();
        return box && box.querySelector('.run-steps');
    }

    function parse(event) {
        try {
            return JSON.parse(event.data);
        } catch (error) {
            return {};
        }
    }

    function reasoningText() {
        if (run.reasoning) {
            return run.reasoning;
        }
        var box = steps();
        if (!box) {
            return null;
        }
        var block = document.createElement('details');
        block.className = 'reasoning';
        var summary = document.createElement('summary');
        summary.textContent = 'Reasoning';
        block.appendChild(summary);
        var text = document.createElement('pre');
        text.className = 'reasoning-text';
        block.appendChild(text);
        box.appendChild(block);
        run.reasoning = text;
        return text;
    }

    function toolCard(data) {
        var callId = data.callId;
        if (!callId) {
            return null;
        }
        var card = run.tools[callId];
        if (card) {
            return card;
        }
        var box = steps();
        if (!box) {
            return null;
        }
        card = document.createElement('details');
        card.className = 'tool-card';
        card.setAttribute('data-call-id', callId);
        var summary = document.createElement('summary');
        summary.className = 'tool-summary';
        var name = document.createElement('span');
        name.className = 'tool-name';
        name.textContent = data.name || 'инструмент';
        var status = document.createElement('span');
        status.className = 'tool-status';
        status.textContent = 'вызов';
        summary.appendChild(name);
        summary.appendChild(status);
        card.appendChild(summary);
        var body = document.createElement('div');
        body.className = 'tool-body';
        var input = document.createElement('pre');
        input.className = 'tool-input';
        var result = document.createElement('pre');
        result.className = 'tool-result';
        body.appendChild(input);
        body.appendChild(result);
        card.appendChild(body);
        box.appendChild(card);
        run.tools[callId] = card;
        return card;
    }

    function status(card, text) {
        var label = card.querySelector('.tool-status');
        if (label) {
            label.textContent = text;
        }
    }

    function failTool(data) {
        if (data.errorType === 'aborted' || run.interrupted) {
            return;
        }
        var card = toolCard(data);
        if (!card) {
            return;
        }
        card.classList.add('tool-error');
        status(card, 'ошибка');
        var message = document.createElement('pre');
        message.className = 'tool-error-message';
        message.textContent = data.message || data.errorType || 'инструмент завершился ошибкой';
        card.querySelector('.tool-body').appendChild(message);
    }

    function addUsage(data) {
        run.cost += Number(data.cost) || 0;
        var tokens = data.tokens || {};
        run.input += Number(tokens.input) || 0;
        run.output += Number(tokens.output) || 0;
        run.reasoningTokens += Number(tokens.reasoning) || 0;
        run.cache += Number(tokens.cacheRead) || 0;
    }

    function showUsage() {
        var box = answer();
        if (!box) {
            return;
        }
        var footer = box.querySelector('.run-usage');
        var cost = box.querySelector('.run-cost');
        var tokens = box.querySelector('.run-tokens');
        if (!footer || !cost || !tokens) {
            return;
        }
        cost.textContent = '$' + run.cost.toFixed(6);
        var text = 'вход ' + run.input + ' · выход ' + run.output;
        if (run.reasoningTokens > 0) {
            text += ' · reasoning ' + run.reasoningTokens;
        }
        if (run.cache > 0) {
            text += ' · кэш ' + run.cache;
        }
        tokens.textContent = text;
        footer.hidden = false;
    }

    function connect(url) {
        disconnect();
        run = emptyRun();
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
        source.addEventListener('reasoning', function (event) {
            var text = reasoningText();
            var data = parse(event);
            if (!text) {
                return;
            }
            if (typeof data.delta === 'string') {
                text.textContent += data.delta;
            } else if (typeof data.text === 'string' && !text.textContent) {
                text.textContent = data.text;
            }
        });
        source.addEventListener('tool.started', function (event) {
            var data = parse(event);
            var card = toolCard(data);
            var name = card && card.querySelector('.tool-name');
            if (name && data.name && name.textContent === 'инструмент') {
                name.textContent = data.name;
            }
        });
        source.addEventListener('tool.called', function (event) {
            var data = parse(event);
            var card = toolCard(data);
            if (!card) {
                return;
            }
            var name = card.querySelector('.tool-name');
            if (name && data.name && name.textContent === 'инструмент') {
                name.textContent = data.name;
            }
            var input = card.querySelector('.tool-input');
            if (input) {
                input.textContent = data.input === undefined ? '' : JSON.stringify(data.input, null, 2);
            }
            status(card, 'вход');
        });
        source.addEventListener('tool.result', function (event) {
            var data = parse(event);
            var card = run.tools[data.callId];
            if (!card || run.interrupted) {
                return;
            }
            var result = card.querySelector('.tool-result');
            if (result) {
                result.textContent = data.content || '';
            }
            if (data.status === 'error') {
                card.classList.add('tool-error');
                status(card, 'ошибка');
            } else if (data.status) {
                status(card, data.status === 'completed' ? 'готово' : data.status);
            }
        });
        source.addEventListener('tool.failed', function (event) {
            failTool(parse(event));
        });
        source.addEventListener('usage.step', function (event) {
            addUsage(parse(event));
        });
        source.addEventListener('state', function (event) {
            var state = parse(event).state;
            setBusy(state === 'started');
            if (state === 'started') {
                return;
            }
            run.interrupted = state === 'interrupted';
            showUsage();
            source.close();
            if (stream === source) {
                stream = null;
            }
        });
    }

    function connectAnswer(scope) {
        var answerBox = (scope || chat).querySelector('[data-sse-url]');
        if (answerBox) {
            connect(answerBox.getAttribute('data-sse-url'));
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
