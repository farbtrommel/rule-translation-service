package de.farbtrommel.rts.translation.feel2ruleml;

import de.farbtrommel.rts.RuleLanguage;
import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.RuleTranslationToRuleML;
import de.farbtrommel.rts.translation.Util;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.omg.dmn.feel.DmnFeelLexer;
import org.omg.dmn.feel.DmnFeelParser;
import org.ruleml.deliberation.*;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Translate S-FEEL statements to RuleML.
 */
public class FeelToRuleMLTranslation implements RuleTranslationToRuleML {
    private static final Map<String, String> sOptions = createDefaultOptions();

    private static Map<String, String> createDefaultOptions() {
        Map<String, String> tmpOptions = new HashMap<String, String>();
        /**
         * mode = assign | compare
         */
        tmpOptions.put("mode", "assign");

        /**
         * grammar rule entry =
         *   simpleExpression |
         *   simpleExpressions |
         *   unaryTest
         */
        tmpOptions.put("grammarRuleEntry", "simpleExpression");

        return tmpOptions;
    }
    @Override
    public RuleMLType runTranslation(String source) throws RuleTranslationException {
        return runTranslation(source, sOptions);
    }

    @Override
    public RuleMLType runTranslation(String source, Map<String, String> options) throws RuleTranslationException {
        CharStream input = Util.antlr(source + "\r\n");
        return runTranslation(input, options);
    }

    @Override
    public RuleMLType runTranslation(File source) throws RuleTranslationException {
        return runTranslation(source, sOptions);
    }

    @Override
    public RuleMLType runTranslation(File source, Map<String, String> options) throws RuleTranslationException {
        CharStream input = Util.antlr(source);
        return runTranslation(input, options);
    }

    @Override
    public RuleMLType runTranslation(InputStream source) throws RuleTranslationException {
        return runTranslation(source, sOptions);
    }

    @Override
    public RuleMLType runTranslation(InputStream source, Map<String, String> options) throws RuleTranslationException {
        CharStream input = Util.antlr(source);
        return runTranslation(input, options);
    }


    /**
     * TODO: Not clean
     * @param input
     * @return
     */
    public static DmnFeelParser getParseTree(String input) throws RuleTranslationException {
        return getParseTree(new ANTLRInputStream(prepareFEEL(input)));
    }

    public static DmnFeelParser getParseTree(CharStream input) {
        DmnFeelLexer lexer = new DmnFeelLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        DmnFeelParser parser = new DmnFeelParser(tokens);
        //parser.addParseListener(new FeelExpListener());
        //FeelExpParser.ExpressionContext uu = parser.expression();
        //return parser.expression();
        return parser;
    }

    private RuleMLType runTranslation(CharStream input, Map<String, String> options) throws RuleTranslationException {
        if (options == null) {
            options = new HashMap<>();
        }
        RuleMLType ruleMLType = Util.RULEML_FACTORY.createRuleMLType();
        AssertType assertType = Util.RULEML_FACTORY.createAssertType();
        ruleMLType.getActOrAssertOrRetract().add(assertType);
        RulebaseType rulebaseType = Util.RULEML_FACTORY.createRulebaseType();
        assertType.getFormulaOrRulebaseOrImplies().add(rulebaseType);

        VarType var = Util.RULEML_FACTORY.createVarType();
        var.getContent().add("Var");
        JAXBElement<?> varWrapped = Util.RULEML_FACTORY.createVar(var);

        AtomType atom = Util.RULEML_FACTORY.createAtomType();
        atom.getContent().add(Util.create("Rel", "rule"));
        atom.getContent().add(varWrapped);

        AndInnerType and = Util.RULEML_FACTORY.createAndInnerType();

        ImpliesType implies = Util.RULEML_FACTORY.createImpliesType();
        implies.getContent().add(Util.RULEML_FACTORY.createAnd(and));
        implies.getContent().add(Util.RULEML_FACTORY.createAtom(atom));
        rulebaseType.getFormulaOrImpliesOrRule().add(implies);

        /* prepare options eventually set default options */
        FeelToRuleML.TranslationMode translationMode = FeelToRuleML.TranslationMode.COMPARISON;
        if (!options.containsKey("mode")) {
            options.put("mode", sOptions.get("mode"));
        }
        if (!options.containsKey("grammarRuleEntry")) {
            options.put("grammarRuleEntry", sOptions.get("grammarRuleEntry"));
        }
        if (options.get("mode").equals("assign")) {
            translationMode = FeelToRuleML.TranslationMode.ASSIGNMENT;
        } else if (options.get("mode").equals("compare")) {
            translationMode = FeelToRuleML.TranslationMode.COMPARISON;
        }

        String grammarRuleEntry = options.get("grammarRuleEntry");
        if (grammarRuleEntry.equals("simpleExpressions")) {
            FeelToRuleML.runTranslation(getParseTree(input).simpleExpressions(), and, varWrapped,
                    translationMode);
        } else if (grammarRuleEntry.equals("simpleExpression")) {
            FeelToRuleML.runTranslation(getParseTree(input).simpleExpression(), and, varWrapped,
                    translationMode);
        } else if (grammarRuleEntry.equals("unaryTest")) {
            FeelToRuleML.runTranslation(getParseTree(input).simpleUnaryTests(), and, varWrapped,
                    translationMode);
        }

        return ruleMLType;
    }

    /**
     * Some Grammar Rules needed preparation.
     * When the DmnFeel.g4 got fix probably this function not needed.
     * @param code
     * @return
     */
    private static String prepareFEEL(String code) throws RuleTranslationException {
        try {
            if (code == null || code.isEmpty()) {
                code = "-";
            }
            code = code.replace("..", " .. ");
            code += "\r\n";
            return code;
        } catch (Exception e) {
            throw new RuleTranslationException(e.getMessage());
        }
    }

    @Override
    public RuleLanguage getInputLanguage() {
        return new RuleLanguage("SFEEL", "1.1", "text/plain");
    }

    @Override
    public RuleLanguage getOutputLanguage() {
        return new RuleLanguage("RuleML", "1.0", "application/xml");
    }

}
