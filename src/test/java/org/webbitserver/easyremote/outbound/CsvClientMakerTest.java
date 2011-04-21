package org.webbitserver.easyremote.outbound;

import org.junit.Test;
import org.webbitserver.easyremote.NotCsvSerializableException;

public class CsvClientMakerTest {
    private TestableCsvClientMaker cm = new TestableCsvClientMaker();

    @Test
    public void allows_numbers() throws Exception {
        cm.createMessage("add", new Object[]{1, 2.0});
    }

    @Test
    public void allows_booleans() throws Exception {
        cm.createMessage("add", new Object[]{true, false});
    }

    @Test
    public void allows_null() throws Exception {
        cm.createMessage("add", new Object[]{null});
    }

    @Test
    public void allows_strings() throws Exception {
        cm.createMessage("say", new Object[]{"de er små"});
    }

    @Test(expected = NotCsvSerializableException.class)
    public void refuses_strings_with_comma() throws Exception {
        cm.createMessage("wtf", new Object[]{"de er noen små, hissige skapninger"});
    }

    @Test(expected = NotCsvSerializableException.class)
    public void refuses_big_objects() throws Exception {
        cm.createMessage("wtf", new Object[]{this});
    }

    private static class TestableCsvClientMaker extends CsvClientMaker {
        // Make it public
        @Override
        public String createMessage(String methodName, Object[] args) {
            return super.createMessage(methodName, args);
        }
    }
}
