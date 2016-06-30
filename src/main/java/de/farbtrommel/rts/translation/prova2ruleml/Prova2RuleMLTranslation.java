package de.farbtrommel.rts.translation.prova2ruleml;

import de.farbtrommel.rts.RuleLanguage;
import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.RuleTranslationToRuleML;
import de.farbtrommel.rts.translation.Util;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.ruleml.deliberation.AssertType;
import org.ruleml.deliberation.QueryType;
import org.ruleml.deliberation.RuleMLType;
import org.ruleml.deliberation.RulebaseType;
import ws.prova.parser.ProvaLexer;
import ws.prova.parser.ProvaParser;

import java.io.File;
import java.io.InputStream;
import java.util.Map;


/**
 * Prova to RuleML translation.
 */
public class Prova2RuleMLTranslation implements RuleTranslationToRuleML {

    @Override
    public RuleMLType runTranslation(String source) throws RuleTranslationException {
        CharStream input = Util.antlr(source);
        return runTranslation(input);
    }

    @Override
    public RuleMLType runTranslation(String source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public RuleMLType runTranslation(File source) throws RuleTranslationException {
        CharStream input = Util.antlr(source);
        return runTranslation(input);
    }

    @Override
    public RuleMLType runTranslation(File source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public RuleMLType runTranslation(InputStream source) throws RuleTranslationException {
        CharStream input = Util.antlr(source);
        return runTranslation(input);
    }

    @Override
    public RuleMLType runTranslation(InputStream source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    public static ProvaParser getParseTree(CharStream input) {
        ProvaLexer lexer = new ProvaLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        ProvaParser parser = new ProvaParser(tokens);
        return parser;
    }

    private RuleMLType runTranslation(CharStream input) throws RuleTranslationException {
        RuleMLType ruleMLType = Util.RULEML_FACTORY.createRuleMLType();

        AssertType assertType = Util.RULEML_FACTORY.createAssertType();
        ruleMLType.getActOrAssertOrRetract().add(assertType);

        RulebaseDispatcher rulebaseDispatcher = new RulebaseDispatcher();

        QueryType query = Util.RULEML_FACTORY.createQueryType();
        ruleMLType.getActOrAssertOrRetract().add(query);

        Prova2RuleML.runTranslation(getParseTree(input).rulebase(), rulebaseDispatcher, query);

        for (RulebaseType item : rulebaseDispatcher.getAll()) {
            assertType.getFormulaOrRulebaseOrImplies().add(item);
        }

        return ruleMLType;
    }

    @Override
    public RuleLanguage getInputLanguage() {
        return new RuleLanguage("Prova", "3.0", "text/plain");
    }

    @Override
    public RuleLanguage getOutputLanguage() {
        return new RuleLanguage("RuleML", "1.0", "application/xml");
    }

}
