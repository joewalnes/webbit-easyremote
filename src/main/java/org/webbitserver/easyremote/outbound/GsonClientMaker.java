package org.webbitserver.easyremote.outbound;

import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GsonClientMaker extends DynamicProxyClientMaker {

    private final Gson gson;

    public GsonClientMaker(Gson gson) {
        this.gson = gson;
    }

    public GsonClientMaker() {
        this(new Gson());
    }

    @Override
    public String createMessage(Method method, Object[] args) {
        Map<String, Object> outgoing = new HashMap<String, Object>();
        outgoing.put("action", method.getName());
        outgoing.put("args", args);
        return gson.toJson(outgoing);
    }
}
