/**
 * Creates a new RPC WebSocket
 *
 * @param path
 * @param target - A Javascript object that will receive function calls.
 * @param format - How server->client invocations are formatted. Defaults to JSON. Use 'csv' to use the faster CSV format.
 */
function WebbitSocket(path, target, format) {
    function jsonParser(data, callback) {
        var msg = JSON.parse(data);
        callback(msg.action, msg.args);
    }
    function csvParser(data, callback) {
        var msg = data.split(',');
        callback(msg[0], msg.slice(1));
    }

    var incomingInvocation = format == 'csv' ? csvParser : jsonParser;

    var self = this;
    var ws = new WebSocket('ws://' + document.location.host + path + '?format=' + format);
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
        incomingInvocation(e.data, function(incomingAction, incomingArgs) {
            if (incomingAction == '__exportMethods') {
                incomingArgs.forEach(function(name) {
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
                var action = target[incomingAction];
                if (typeof action === 'function') {
                    if(action.length == incomingArgs.length) {
                        action.apply(target, incomingArgs);
                    } else {
                        self.__badNumberOfArguments('Function ' + incomingAction + ' called with: ' + incomingArgs.length + ' arguments, but it takes ' + action.length + ' arguments.');
                    }
                } else {
                    self.__noSuchFunction('No such function: ' + incomingAction);
                }
            }
        });
    };
}