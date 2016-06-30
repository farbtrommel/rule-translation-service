package de.farbtrommel.rts.translation;

import de.farbtrommel.rts.translation.ruleml2prova.RuleML2ProvaTranslation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class TestRuleML2Prova {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"rules/ruleml/rulebase.ruleml", "rules/ruleml/rulebase.prova"}
        });
    }

    private String sourceFileContent;
    private String destFileContent;


    private static final RuleML2ProvaTranslation ruleml2prova = new RuleML2ProvaTranslation();

    public TestRuleML2Prova(String sourceFile, String destFile) {
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
            String provaSource = String.valueOf(this.ruleml2prova.runTranslation(this.sourceFileContent));
            assertEquals(this.destFileContent.replace(" ", "").replace("\r", ""), provaSource.replace(" ", "").replace("\r", ""));
        } catch (RuleTranslationException e) {
            e.fillInStackTrace();
            assertNotNull(null);
        }
    }
}
