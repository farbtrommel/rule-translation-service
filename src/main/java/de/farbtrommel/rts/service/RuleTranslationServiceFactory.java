package de.farbtrommel.rts.service;

import de.farbtrommel.rts.UnknownRulesLanguageException;
import de.farbtrommel.rts.translation.RuleTranslationFromRuleML;
import de.farbtrommel.rts.translation.RuleTranslationToRuleML;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Setup translation service and create the configuration. This Class couples
 * the instances of all individual translation packages.
 */
public class RuleTranslationServiceFactory {
    public static RuleTranslationService createRuleTranslationService() {
        RuleTranslationServiceImpl translatorService = new RuleTranslationServiceImpl();

        try {
            ServiceLoader<RuleTranslationFromRuleML> translatorsFromRuleMLLoader = ServiceLoader
                    .load(RuleTranslationFromRuleML.class);

            Iterator<RuleTranslationFromRuleML> iteratorFromRuleML = translatorsFromRuleMLLoader.iterator();
            while (iteratorFromRuleML.hasNext()) {
                RuleTranslationFromRuleML translator = iteratorFromRuleML.next();
                translatorService.addTranslatorFromRuleML(translator);
            }


            ServiceLoader<RuleTranslationToRuleML> translatorsToRuleMLLoader = ServiceLoader
                    .load(RuleTranslationToRuleML.class);
            Iterator<RuleTranslationToRuleML> iteratorToRuleML = translatorsToRuleMLLoader.iterator();
            while (iteratorToRuleML.hasNext()) {
                RuleTranslationToRuleML translator = iteratorToRuleML.next();
                translatorService.addTranslatorToRuleML(translator);
            }

        } catch (UnknownRulesLanguageException e) {
            e.printStackTrace();
        }

        return translatorService;
    }
}
