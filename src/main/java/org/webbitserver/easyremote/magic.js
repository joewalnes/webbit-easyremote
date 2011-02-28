function WebbitSocket(path, target) {
    var self = this;
    var ws = new WebSocket('ws://' + document.location.host + path);
    ws.onclose = function() {
        target.onclose && target.onclose();
        self.onclose && self.onclose();
    };
    ws.onerror = function() {
        target.onerror && target.onerror();
        self.onerror && self.onerror();
    };
    ws.onmessage = function(e) {
        target.onmessage && target.onmessage(e);
        self.onmessage && self.onmessage(e);
        var msg = JSON.parse(e.data);
        if (msg.exports) {
            msg.exports.forEach(function(name) {
                self[name] = function() {
                    var outgoing = {
                        action: name,
                        args: Array.prototype.slice.call(arguments)
                    };
                    ws.send(JSON.stringify(outgoing));
                };
            })
            target.onopen && target.onopen();
            self.onopen && self.onopen();
        } else {
            var action = target[msg.action];
            if (typeof action === 'function') {
                action.apply(target, msg.args);
            } else {
                // TODO: ?
            }
        }
    };
}