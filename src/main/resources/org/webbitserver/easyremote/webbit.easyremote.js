/**
 * Creates a new RPC WebSocket
 *
 * @param path
 * @param target - A Javascript object that will receive function calls.
 * @param options - An object for configuring the instance:
 *
 *   serverClientFormat: [csv|json] - How server->client invocations are formatted.
 *                                    Defaults to json. Use 'csv' to use the faster CSV format.
 *
 *   exceptionHandler: A function that will be called if an exception happens in the client when the
 *                     server invokes a function. The default will report the error back to the server,
 *                     using printStackTrace() from http://stacktracejs.org/ if available.
 */
function WebbitSocket(path, target, options) {
    var self = this;

    var opts = {
        serverClientFormat: 'json',
        exceptionHandler: function(e) {
            var message = "\n";
            if(typeof(e) == 'string') {
                message += e + "\n\n";
            }
            if(e.message) message += "message:" + e.message + "\n\n";
            if(e.type) message += "type:" + e.type + "\n\n";
            if(e.stack) message += "stack:" + e.stack + "\n\n";
            if(typeof(window.printStackTrace) == 'function') {
                message += "stacktracejs.org:" + printStackTrace({e:e}).join("\n") + "\n\n";
            }
            self.__reportClientException(message); // This function is dynamically defined upon connection
        }
    };
    for (var opt in options) { opts[opt] = options[opt]; }

    function jsonParser(data, callback) {
        var msg = JSON.parse(data);
        callback(msg.action, msg.args);
    }
    function csvParser(data, callback) {
        var msg = data.split(',');
        callback(msg[0], msg.slice(1));
    }

    var incomingInvocation = opts.serverClientFormat == 'csv' ? csvParser : jsonParser;

    function exportMethods(incomingArgs) {
        incomingArgs.forEach(function(name) {
            self[name] = function() {
                var outgoing = {
                    action: name,
                    args: Array.prototype.slice.call(arguments)
                };
                try {
                    ws.send(JSON.stringify(outgoing));
                } catch (e) {
                    opts.exceptionHandler(e);
                }
            };
        });
        target.onopen && target.onopen();
        self.onopen && self.onopen();
    }

    function invokeOnTarget(incomingAction, incomingArgs) {
        var action = target[incomingAction];
        if (typeof action === 'function') {
            if (action.length == incomingArgs.length) {
                try {
                    action.apply(target, incomingArgs);
                } catch(e) {
                    opts.exceptionHandler(e);
                }
            } else {
                self.__badNumberOfArguments('Javascript Function ' + incomingAction, action.length, incomingArgs);
            }
        } else {
            self.__noSuchFunction('Javascript Function ' + incomingAction);
        }
    }

    var ws = new WebSocket('ws://' + document.location.host + path + '?serverClientFormat=' + opts.serverClientFormat);

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
                exportMethods(incomingArgs);
            } else {
                try {
                    invokeOnTarget(incomingAction, incomingArgs);
                } catch(e) {
                    opts.exceptionHandler(e);
                }
            }
        });
    };
}
