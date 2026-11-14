(function () {
    var chat = document.getElementById('chat');
    if (!chat) {
        return;
    }
    var connection = document.getElementById('connection');
    var stream = null;
    var currentSessionId = null;
    var run = emptyRun();

    function emptyRun() {
        return {
            reasoning: null,
            tools: {},
            permissions: {},
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

    function permissionCard(data) {
        var requestId = data.requestId;
        if (!requestId) {
            return null;
        }
        var card = run.permissions[requestId];
        if (card) {
            return card;
        }
        var box = steps();
        if (!box) {
            return null;
        }
        card = document.createElement('section');
        card.className = 'permission-card';
        card.setAttribute('data-request-id', requestId);
        card.setAttribute('role', 'group');
        card.setAttribute('aria-label', 'Запрос разрешения');
        var title = document.createElement('p');
        title.className = 'permission-title';
        title.textContent = 'Требуется разрешение';
        card.appendChild(title);
        var action = document.createElement('p');
        action.className = 'permission-action';
        action.textContent = data.action || 'действие';
        card.appendChild(action);
        var resources = document.createElement('ul');
        resources.className = 'permission-resources';
        (Array.isArray(data.resources) ? data.resources : []).forEach(function (resource) {
            var item = document.createElement('li');
            item.textContent = String(resource);
            resources.appendChild(item);
        });
        card.appendChild(resources);
        var actions = document.createElement('div');
        actions.className = 'permission-actions';
        actions.appendChild(permissionButton(card, requestId, 'allow', 'Разрешить'));
        actions.appendChild(permissionButton(card, requestId, 'reject', 'Отклонить'));
        card.appendChild(actions);
        var error = document.createElement('p');
        error.className = 'permission-error';
        error.setAttribute('role', 'alert');
        error.hidden = true;
        card.appendChild(error);
        box.appendChild(card);
        run.permissions[requestId] = card;
        return card;
    }

    function permissionButton(card, requestId, decision, label) {
        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'permission-' + decision;
        button.textContent = label;
        button.addEventListener('click', function () {
            replyPermission(card, requestId, decision);
        });
        return button;
    }

    function replyPermission(card, requestId, decision) {
        if (card.dataset.answered) {
            return;
        }
        card.dataset.answered = 'sending';
        var error = card.querySelector('.permission-error');
        if (error) {
            error.hidden = true;
        }
        setPermissionButtons(card, true);
        if (!currentSessionId) {
            permissionFailed(card, 'Нет сессии чата: ответить нельзя.', true);
            return;
        }
        fetch('/api/chat/' + encodeURIComponent(currentSessionId) + '/permission/' + encodeURIComponent(requestId), {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({decision: decision})
        }).then(function (response) {
            if (response.ok) {
                closePermission(requestId);
                return;
            }
            return response.json().catch(function () {
                return {};
            }).then(function (body) {
                var message = body && body.error ? body.error : 'Не удалось отправить ответ';
                if (response.status === 404 || response.status === 400) {
                    permissionFailed(card, 'Запрос устарел: ' + message, true);
                } else {
                    permissionFailed(card, message, false);
                }
            });
        }).catch(function () {
            permissionFailed(card, 'Нет связи с сервером', false);
        });
    }

    function permissionFailed(card, message, final) {
        var error = card.querySelector('.permission-error');
        if (error) {
            error.textContent = message;
            error.hidden = false;
        }
        if (final) {
            card.classList.add('permission-stale');
            card.dataset.answered = 'failed';
            setPermissionButtons(card, true);
            return;
        }
        delete card.dataset.answered;
        setPermissionButtons(card, false);
    }

    function setPermissionButtons(card, disabled) {
        card.querySelectorAll('.permission-actions button').forEach(function (button) {
            button.disabled = disabled;
        });
    }

    function closePermission(requestId) {
        var card = run.permissions[requestId];
        if (!card) {
            return;
        }
        delete run.permissions[requestId];
        card.remove();
    }

    function foreignSession(data) {
        return !!(currentSessionId && data.sessionId && data.sessionId !== currentSessionId);
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

    function connect(url, sessionId) {
        disconnect();
        currentSessionId = sessionId || null;
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
        source.addEventListener('permission.asked', function (event) {
            var data = parse(event);
            if (foreignSession(data)) {
                return;
            }
            permissionCard(data);
        });
        source.addEventListener('permission.replied', function (event) {
            var data = parse(event);
            if (foreignSession(data)) {
                return;
            }
            closePermission(data.requestId);
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
            connect(answerBox.getAttribute('data-sse-url'), answerBox.getAttribute('data-session-id'));
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
