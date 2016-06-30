package de.farbtrommel.rts.translation;

import de.farbtrommel.rts.translation.feel2ruleml.FeelToRuleMLTranslation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ruleml.deliberation.RuleMLType;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class TestSFeel2RuleML {

    static String ruleml = "<?xmlversion=\"1.0\"encoding=\"UTF-8\"standalone=\"yes\"?>\n<RuleML xmlns=\"http://ruleml.org/spec\">\n" +
            "    <Assert>\n" +
            "        <Rulebase>\n" +
            "            <Implies>\n" +
            "                <And>\n" +
            "                    %source%" +
            "                </And>\n" +
            "                <Atom>\n" +
            "                    <Rel>rule</Rel>\n" +
            "                    <Var>Var</Var>\n" +
            "                </Atom>\n" +
            "            </Implies>\n" +
            "        </Rulebase>\n" +
            "    </Assert>\n" +
            "</RuleML>\n";

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"12 + 22.0 + 10", "literal-1.txt", "literal-1.ruleml"},

                {"42", "literal-2.txt", "literal-2.ruleml"},

                {"<= 12", "unary-1.txt", "unary-1.ruleml"},

                {"[12 .. 22]", "unary-2.txt", "unary-2.ruleml"},

                {"(1 .. 20)", "unary-3.txt", "unary-3.ruleml"},

                {"foo", "unary-4.txt", "unary-4.ruleml"},

                {"42**1", "unary-5.txt", "unary-5.ruleml"},

                {"\"foobar\"", "unary-6.txt", "unary-6.ruleml"},

                {"42", "unary-7.txt", "unary-7.ruleml"},


        });
    }

    private final String label;
    private final String sourceOriginFile;
    private final String sourceDestinationFile;

    private final static FeelToRuleMLTranslation feelToRuleML = new FeelToRuleMLTranslation();

    public TestSFeel2RuleML(String label, String sourceOriginFile, String sourceDestination) throws IOException {
        this.label = label;
        this.sourceOriginFile = sourceOriginFile;
        this.sourceDestinationFile = sourceDestination;

    }

    @Before
    public void before() {
        Util.sCounter.set(0);
    }

    @Test
    public void test() {
        try {
            HashMap<String, String> options = new HashMap<>();

            if (sourceOriginFile.startsWith("unary")) {
                options.put("mode", "compare");
            } else {
                options.put("mode", "assign");
            }
            String in = Util.readResourceFileAsString("rules/sfeel/" + sourceOriginFile);
            RuleMLType ruleMLType = feelToRuleML.runTranslation(in, options);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            Util.objToOutputStream(ruleMLType,
                    Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME, buffer);

            String translatedRule = String.valueOf(buffer);
            String toCompareRule = Util.readResourceFileAsString("rules/sfeel/" + this.sourceDestinationFile);

            toCompareRule = ruleml.replace("%source%", toCompareRule);

            assertEquals(toCompareRule.replace("\r","").replace(" ", ""), translatedRule.replace("\r","").replace(" ", ""));
        } catch (JAXBException e) {
            e.printStackTrace();
            assertNotNull(null);
        } catch (RuleTranslationException e) {
            e.printStackTrace();
            assertNotNull(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}