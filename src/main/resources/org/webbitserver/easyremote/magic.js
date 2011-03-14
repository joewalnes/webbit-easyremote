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
            });
            target.onopen && target.onopen();
            self.onopen && self.onopen();
        } else {
            var action = target[msg.action];
            if (typeof action === 'function') {
                if(action.length == msg.args.length) {
                    action.apply(target, msg.args);
                } else {
                    self.__badNumberOfArguments('Function ' + msg.action + ' called with: ' + msg.args.length + ' arguments, but it takes ' + action.length + ' arguments.');
                }
            } else {
                self.__noSuchFunction('No such function: ' + msg.action);
            }
        }
    };
}