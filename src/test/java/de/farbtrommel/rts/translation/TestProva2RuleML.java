package de.farbtrommel.rts.translation;

import de.farbtrommel.rts.translation.prova2ruleml.Prova2RuleMLTranslation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ruleml.deliberation.RuleMLType;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class TestProva2RuleML {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"rules/ruleml/rulebase.prova", "rules/ruleml/rulebase.ruleml"}
        });
    }

    private String sourceFileContent;
    private String destFileContent;


    private static final Prova2RuleMLTranslation prova2ruleml = new Prova2RuleMLTranslation();

    public TestProva2RuleML(String sourceFile, String destFile) {
        try {
            this.sourceFileContent = Util.readResourceFileAsString(sourceFile);
            this.destFileContent = String.valueOf(Util.readResourceFileAsString(destFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test() {
        try {
            RuleMLType ruleML = this.prova2ruleml.runTranslation(this.sourceFileContent);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            Util.objToOutputStream(ruleML,
                    Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME, buffer);
            String translatedDmnTable = String.valueOf(buffer);

            //TODO: not finish now
            /*assertEquals(this.destFileContent.replace(" ", "").replace("\r",""),
                    translatedDmnTable.replace(" ", "").replace("\r",""));
                    */
        } catch (RuleTranslationException e) {
            e.fillInStackTrace();
            assertNotNull(null);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
