package de.farbtrommel.rts.translation.ruleml2prova;

import de.farbtrommel.rts.RuleLanguage;
import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.RuleTranslationFromRuleML;
import de.farbtrommel.rts.translation.Util;
import org.ruleml.deliberation.RuleMLType;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Translate a JAXB RuleML Structure to Prova.
 */
public class RuleML2ProvaTranslation implements RuleTranslationFromRuleML {

    @Override
    public String runTranslation(JAXBElement<?> source) throws RuleTranslationException {
        return RuleML2Prova.translate(source);
    }

    @Override
    public String runTranslation(JAXBElement<?> source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public String runTranslation(RuleMLType source) throws RuleTranslationException {
        return RuleML2Prova.translate(Util.RULEML_FACTORY.createRuleML(source));
    }

    @Override
    public String runTranslation(RuleMLType source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public String runTranslation(String source) throws RuleTranslationException {
        JAXBElement<?> ruleML = Util.loadObject(source, Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME);
        return RuleML2Prova.translate(ruleML);
    }

    @Override
    public String runTranslation(String source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public String runTranslation(File source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public String runTranslation(File source) throws RuleTranslationException {
        JAXBElement<?> ruleML = Util.loadObject(source, Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME);
        return RuleML2Prova.translate(ruleML);
    }

    @Override
    public String runTranslation(InputStream source) throws RuleTranslationException {
        JAXBElement<?> ruleML = Util.loadObject(source, Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME);
        return RuleML2Prova.translate(ruleML);
    }

    @Override
    public String runTranslation(InputStream source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public RuleLanguage getInputLanguage() {
        return new RuleLanguage("RuleML", "1.0", "application/xml");
    }

    @Override
    public RuleLanguage getOutputLanguage() {
        return new RuleLanguage("Prova", "3.0", "text/plain");
    }
}
