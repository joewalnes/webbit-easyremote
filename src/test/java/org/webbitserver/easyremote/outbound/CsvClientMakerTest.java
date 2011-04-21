package org.webbitserver.easyremote.outbound;

import org.junit.Test;
import org.webbitserver.easyremote.NotCsvSerializableException;

import static org.junit.Assert.assertEquals;

public class CsvClientMakerTest {
    private TestableCsvClientMaker cm = new TestableCsvClientMaker();

    @Test
    public void formats_numbers() throws Exception {
        assertEquals("add,1,2.0", cm.createMessage("add", new Object[]{1, 2.0}));
    }

    @Test
    public void formats_booleans() throws Exception {
        assertEquals("add,true,false", cm.createMessage("add", new Object[]{true, false}));
    }

    @Test
    public void formats_null_as_empty_string_which_is_falseley_in_javascript() throws Exception {
        assertEquals("add,", cm.createMessage("add", new Object[]{null}));
    }

    @Test
    public void formats_strings() throws Exception {
        assertEquals("say,de er små", cm.createMessage("say", new Object[]{"de er små"}));
    }

    private static enum Stuff {
        KEY, PHONE
    }

    @Test
    public void formats_enums() throws Exception {
        assertEquals("say,KEY,PHONE", cm.createMessage("say", new Object[]{Stuff.KEY, Stuff.PHONE}));
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
