package de.farbtrommel.rts.translation.dmn2ruleml;

import de.farbtrommel.rts.RuleLanguage;
import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.RuleTranslationToRuleML;
import de.farbtrommel.rts.translation.Util;
import org.omg.dmn.*;
import org.ruleml.deliberation.AtomType;
import org.ruleml.deliberation.MetaType;
import org.ruleml.deliberation.RuleMLType;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Convert DMN XML files to RuleML Files.
 */
public class Dmn2RuleMLTranslation implements RuleTranslationToRuleML {

    @Override
    public RuleMLType runTranslation(String source) throws RuleTranslationException {
        TDefinitions tDefinitions = (TDefinitions) Util.loadObject(source, Util.DMN_PACKAGE_NAME).getValue();
        return parseDefinitions(tDefinitions);
    }

    @Override
    public RuleMLType runTranslation(String source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public RuleMLType runTranslation(File source) throws RuleTranslationException {
        TDefinitions tDefinitions = (TDefinitions) Util.loadObject(source, Util.DMN_PACKAGE_NAME).getValue();
        return parseDefinitions(tDefinitions);
    }

    @Override
    public RuleMLType runTranslation(File source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    @Override
    public RuleMLType runTranslation(InputStream source) throws RuleTranslationException {
        TDefinitions tDefinitions = (TDefinitions) Util.loadObject(source, Util.DMN_PACKAGE_NAME).getValue();
        return parseDefinitions(tDefinitions);
    }

    @Override
    public RuleMLType runTranslation(InputStream source, Map<String, String> options) throws RuleTranslationException {
        return runTranslation(source);
    }

    /**
     * Root Element of DMN XML files.
     *
     * @param tDefinitions
     * @throws RuleTranslationException
     */
    private RuleMLType parseDefinitions(TDefinitions tDefinitions) throws RuleTranslationException {
        RuleMLType ruleML = new RuleMLType();

        List<JAXBElement<? extends TDRGElement>> list = tDefinitions.getDrgElement();

        AtomType decisionFact = Util.RULEML_FACTORY.createAtomType();
        decisionFact.setKeyref("definitions");
        decisionFact.getContent().add(Util.create("Rel", "definitions"));
        decisionFact.getContent().add(Util.create("Ind", tDefinitions.getId()));
        decisionFact.getContent().add(Util.create("Ind", tDefinitions.getName()));

        MetaType metaType = Util.RULEML_FACTORY.createMetaType();
        metaType.setAtom(decisionFact);

        ruleML.getMeta().add(metaType);

        for (JAXBElement<? extends TDRGElement> item : list) {
            if (item.getDeclaredType().equals(TBusinessKnowledgeModel.class)) {
                //TODO: BusinessKnowledgeModel
                //throw new RuleTranslationException("BusinessKnowledgeModel not yet implemented");
            } else if (item.getDeclaredType().equals(TDecision.class)) {
                TDecision decision = (TDecision) item.getValue();
                parseDecision(ruleML, decision);
            } else if (item.getDeclaredType().equals(TInputData.class)) {
                //TODO: InputData
                //throw new RuleTranslationException("InputData not yet implemented");
            } else if (item.getDeclaredType().equals(TKnowledgeSource.class)) {
                //TODO: KnowledgeSource
                //throw new RuleTranslationException("KnowledgeSource not yet implemented");
            }
        }

        return ruleML;
    }

    /**
     * Parse a Decision Tag to RuleML.
     *
     * @param tDecision
     */
    private void parseDecision(RuleMLType ruleML, TDecision tDecision) throws RuleTranslationException {
        DecisionTable.parse(ruleML, tDecision);
    }

    @Override
    public RuleLanguage getInputLanguage() {
        return new RuleLanguage("DMN", "1.1", "application/xml");
    }

    @Override
    public RuleLanguage getOutputLanguage() {
        return new RuleLanguage("RuleML", "1.0", "application/xml");
    }

}
