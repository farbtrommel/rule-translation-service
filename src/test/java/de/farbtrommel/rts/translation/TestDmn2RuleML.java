package de.farbtrommel.rts.translation;

import de.farbtrommel.rts.translation.dmn2ruleml.Dmn2RuleMLTranslation;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDmn2RuleML {
    @Before
    public void before() {
        Util.sCounter.set(0);
    }

    @Test
    public void test() {
        try {
            String source = Util.readResourceFileAsString("rules/dmn/table.dmn");
            Dmn2RuleMLTranslation dmn2RuleML = new Dmn2RuleMLTranslation();
            Object ruleML = dmn2RuleML.runTranslation(source);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            Util.objToOutputStream(ruleML,
                    Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME, buffer);
            String translatedDmnTable = String.valueOf(buffer);
            translatedDmnTable = translatedDmnTable.replace(" ", "").toLowerCase();

            String dest = Util.readResourceFileAsString("rules/dmn/table.ruleml");
            dest = dest.replace(" ", "").replace("\r", "").toLowerCase();

            assertEquals(dest, translatedDmnTable);
        } catch (IOException e) {
            e.printStackTrace();
            assertNotNull(null);
        } catch (RuleTranslationException e) {
            e.fillInStackTrace();
            assertNotNull(null);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
