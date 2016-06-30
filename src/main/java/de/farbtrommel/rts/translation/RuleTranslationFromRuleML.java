package de.farbtrommel.rts.translation;

import org.ruleml.deliberation.RuleMLType;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Generic interface for RuleML to translate in any other language.
 */
public interface RuleTranslationFromRuleML extends RuleTranslation {
    String runTranslation(JAXBElement<?> source) throws RuleTranslationException;

    String runTranslation(JAXBElement<?> source, Map<String, String> options) throws RuleTranslationException;

    String runTranslation(RuleMLType source) throws RuleTranslationException;

    String runTranslation(RuleMLType source, Map<String, String> options) throws RuleTranslationException;

    String runTranslation(String source) throws RuleTranslationException;

    String runTranslation(String source, Map<String, String> options) throws RuleTranslationException;

    String runTranslation(File source) throws RuleTranslationException;

    String runTranslation(File source, Map<String, String> options) throws RuleTranslationException;

    String runTranslation(InputStream source) throws RuleTranslationException;

    String runTranslation(InputStream source, Map<String, String> options) throws RuleTranslationException;
}
