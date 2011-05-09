package org.webbitserver.easyremote.inbound;

import com.google.gson.Gson;

public class GsonInboundDispatcher extends InboundDispatcher {

    private final Gson gson = new Gson();

    public GsonInboundDispatcher(Object server, Class<?> clientType) {
        super(server, clientType);
    }

    @Override
    protected InboundMessage unmarshalInboundRequest(String msg) {
        return gson.fromJson(msg, ActionArgsTuple.class);
    }

    // Populated by GSON.
    private static class ActionArgsTuple implements InboundMessage {
        String action;
        Object[] args;

        @Override
        public String method() {
            return action;
        }

        @Override
        public Object[] args() {
            return args;
        }
    }

}
