package org.webbitserver.easyremote.inbound;

import org.junit.Test;
import org.webbitserver.easyremote.ClientException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class InboundDispatcherTest {
    @Test
    public void raises_javascript_exception_as_java_exception() throws Throwable {
        String inbound = "{\"action\":\"__reportClientException\",\"args\":[\"{anonymous}(\\\"Oh noes\\\")\\nprintStackTrace(#object)\\n{anonymous}(\\\"Oh noes\\\")\\ninvokeOnTarget(\\\"say\\\",[\\\"aslak\\\",\\\"fbomb\\\"])\\n{anonymous}(\\\"say\\\",[\\\"\\\"aslak\\\"\\\",\\\"\\\"fbomb\\\"\\\"])\\njsonParser(\\\"{\\\"args\\\":[\\\"aslak\\\",\\\"fbomb\\\"],\\\"action\\\":\\\"say\\\"}\\\",#function)\\n{anonymous}([object MessageEvent])\"]}";
        InboundDispatcher d = new GsonInboundDispatcher(this, getClass());
        try {
            d.dispatch(null, inbound, this);
            fail();
        } catch(ClientException e) {
            assertEquals("{anonymous}(\"Oh noes\")", e.getMessage());
            assertEquals("JAVASCRIPT.printStackTrace(#object)(???.js)", e.getStackTrace()[0].toString());
        }
    }
}
