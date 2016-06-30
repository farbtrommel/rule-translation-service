package de.farbtrommel.rts.translation;

import org.ruleml.deliberation.RuleMLType;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Generic interface for any language transformed to RuleML
 */
public interface RuleTranslationToRuleML extends RuleTranslation {
    RuleMLType runTranslation(String source) throws RuleTranslationException;

    RuleMLType runTranslation(String source, Map<String, String> options) throws RuleTranslationException;

    RuleMLType runTranslation(File source) throws RuleTranslationException;

    RuleMLType runTranslation(File source, Map<String, String> options) throws RuleTranslationException;

    RuleMLType runTranslation(InputStream source) throws RuleTranslationException;

    RuleMLType runTranslation(InputStream source, Map<String, String> options) throws RuleTranslationException;
}
