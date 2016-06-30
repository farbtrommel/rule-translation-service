package de.farbtrommel.rts.translation.dmn2ruleml;

import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.Util;
import de.farbtrommel.rts.translation.feel2ruleml.FeelToRuleML;
import de.farbtrommel.rts.translation.feel2ruleml.FeelToRuleMLTranslation;
import org.antlr.v4.runtime.tree.ParseTree;
import org.omg.dmn.TOutputClause;
import org.ruleml.deliberation.*;

import javax.xml.bind.JAXBElement;

/**
 * Static methods to help the translation of an decision table to RuleML.
 */
public class DecisionTableHelper {

    /**
     * Translate InputEntry to RuleML.
     * {@code
     * InputEntry(tableId, columnNumber, rowNumber) :- InputExprssion(tableId, columnNumber, rowNumber, value),
     * (equal('foo', value) |
     * context('vat',storage1), context('foo', storage2), multiply(storage1, storage2, val1), equal(val1,value).)
     * }
     *
     * @param rulebaseType Object where all statements got added.
     * @param tableId      Table identifier.
     * @param key          ID of the InputEntry.
     * @param columnNumber Current number of InputEntry.
     * @param rowNumber    Current number of the Rule.
     * @param source       UnaryTest FEEL Expression.
     * @throws RuleTranslationException
     */
    public static void translateInputEntry(RulebaseType rulebaseType, String tableId, String key, String columnNumber,
                                           String rowNumber, String source) throws RuleTranslationException {

        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.setKeyref("inputEntry");
        atomType.getContent().add(Util.create("Rel", "inputEntry"));
        atomType.getContent().add(Util.create("Ind", tableId));
        atomType.getContent().add(Util.create("Ind", columnNumber));
        atomType.getContent().add(Util.create("Ind", rowNumber));

        //Query value from predicate InputExpression(tableId, colNo, value)
        AtomType atomQuery = Util.RULEML_FACTORY.createAtomType();
        atomQuery.setKeyref("inputExpression");
        atomQuery.getContent().add(Util.create("Rel", "inputExpression"));
        atomQuery.getContent().add(Util.create("Ind", tableId));
        atomQuery.getContent().add(Util.create("Ind", columnNumber));

        VarType varType = Util.RULEML_FACTORY.createVarType();
        varType.getContent().add("Value");
        JAXBElement<?> returnValue = Util.RULEML_FACTORY.createVar(varType);

        atomQuery.getContent().add(returnValue);

        RelType metaRel = Util.RULEML_FACTORY.createRelType();
        metaRel.getContent().add("metaInputEntry");
        JAXBElement jaxbElementMetaRel = Util.RULEML_FACTORY.createRel(metaRel);

        ParseTree tree = FeelToRuleMLTranslation.getParseTree(source).simpleExpressions();
        for (int i = 0; i < tree.getChildCount(); i++) {
            if (tree.getChild(i).getText().trim().equals(",")) {
                continue;
            }
            AtomType metaAtom = Util.RULEML_FACTORY.createAtomType();
            metaAtom.getContent().add(jaxbElementMetaRel);
            //metaAtom.setKeyref("metaInputEntry");
            metaAtom.getContent().add(Util.create("Ind", key));
            metaAtom.getContent().add(Util.create("Ind", Util.sfeelStringToJavaString(tree.getChild(i).getText())));


            MetaType meta = Util.RULEML_FACTORY.createMetaType();
            meta.setAtom(metaAtom);

            AndInnerType and = Util.RULEML_FACTORY.createAndInnerType();
            and.getFormulaOrOperatorOrAnd().add(atomQuery);

            // <relName>(varName, <returnValue>) :- <and>
            ImpliesType impliesType = Util.RULEML_FACTORY.createImpliesType();
            impliesType.getContent().add(Util.RULEML_FACTORY.createMeta(meta));
            impliesType.getContent().add(Util.RULEML_FACTORY.createAnd(and));
            impliesType.getContent().add(Util.RULEML_FACTORY.createAtom(atomType));

            FeelToRuleML.runTranslation(tree.getChild(i), and,
                    returnValue, FeelToRuleML.TranslationMode.COMPARISON);

            rulebaseType.getFormulaOrImpliesOrRule().add(impliesType);
        }
    }

    /**
     * Translate Input Expression to RuleML.
     * {@code
     * InputExpression(tableId, columnNumber, rowNumber, value) :- context('foo', storage1), value=storage1.
     * }
     *
     * @param rulebaseType
     * @param tableId      Table identifier
     * @param columnNumber Number of Input Expression at horizontal decision table represented as column
     * @param source       Simple FEEL Statement
     * @throws RuleTranslationException
     */
    public static void translateInputExpression(RulebaseType rulebaseType, String tableId,
                                                String columnNumber, String source, MetaType meta)
            throws RuleTranslationException {

        JAXBElement<?> returnVar = Util.create("var", "Value");

        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.setKeyref("inputExpression");
        atomType.getContent().add(Util.create("rel", "inputExpression"));
        atomType.getContent().add(Util.create("ind", tableId));
        atomType.getContent().add(Util.create("ind", columnNumber));
        atomType.getContent().add(returnVar);

        AndInnerType and = Util.RULEML_FACTORY.createAndInnerType();

        // <relName>(varName, <returnValue>) :- <and>
        ImpliesType impliesType = Util.RULEML_FACTORY.createImpliesType();
        impliesType.getContent().add(Util.RULEML_FACTORY.createMeta(meta));
        impliesType.getContent().add(Util.RULEML_FACTORY.createAnd(and));
        impliesType.getContent().add(Util.RULEML_FACTORY.createAtom(atomType));

        rulebaseType.getFormulaOrImpliesOrRule().add(impliesType);

        FeelToRuleML.runTranslation(FeelToRuleMLTranslation.getParseTree(source).simpleExpression(), and,
                returnVar, FeelToRuleML.TranslationMode.ASSIGNMENT);
    }

    /**
     * Translate Output Expression to RuleML.
     * {@code
     * InputExpression(tableId, columnNumber, rowNumber, value) :-
     * context('foo', storage1), value=storage1.
     * }
     *
     * @param rulebaseType
     * @param tableId      Table identifier
     * @param outputClause
     * @throws RuleTranslationException
     */
    public static void translateOutputExpression(RulebaseType rulebaseType, String tableId,
                                                 TOutputClause outputClause) throws RuleTranslationException {
        VarType varType = Util.RULEML_FACTORY.createVarType();
        varType.getContent().add("Result");
        JAXBElement<?> returnVar = Util.RULEML_FACTORY.createVar(varType);

        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.setKeyref("outputExpression");
        atomType.getContent().add(Util.create("rel", "outputExpression"));
        //atomType.getContent().add(returnVar);
        atomType.getContent().add(Util.create("ind", tableId));
        atomType.getContent().add(Util.create("ind", outputClause.getId()));
        atomType.getContent().add(Util.create("ind", outputClause.getName()));
        atomType.getContent().add(Util.create("ind", outputClause.getLabel()));
        atomType.getContent().add(Util.create("ind", Util.serializeQName(outputClause.getTypeRef())));

        rulebaseType.getFormulaOrImpliesOrRule().add(atomType);
    }

    /**
     * Translate a OutputEntry to Rule base.
     * {@code
     * OutputEntry(tableId, columnNumber, rowNumber, key, value) :- (value="foobar" |
     * context('foo', storage1), multiply(storage1,2,val1), value=val1),
     * InputEntry(tableId, 0, rowNumber),
     * InputEntry(tableId, 1, rowNumber),
     * ...
     * }
     *
     * @param rulebaseType            Rule base where all statements got added.
     * @param tableId                 Table identifier.
     * @param numberOfInputExpression Number of Input Expression.
     * @param columnNumber            Number of Output at horizontal table represented as column
     * @param rowNumber               Number of Rule at horizontal table represented as row
     * @param outputExpressionKey     Key to store it to meta data
     * @param source                  Simple FEEL Expression
     * @throws RuleTranslationException
     */
    public static void translateOutputEntry(RulebaseType rulebaseType, String tableId, Integer numberOfInputExpression,
                                            String columnNumber, String rowNumber, String id,
                                            String outputExpressionKey, //String outputExpressionLabel,
                                            String source) throws RuleTranslationException {

        // OutputEntry(tableId, columnNo, rowNo, key, <returnValue>) :- <and>
        if (id == null) {
            id = "outputEntryId" + Util.sCounter.incrementAndGet();
        }
        VarType varType = Util.RULEML_FACTORY.createVarType();
        varType.getContent().add("Value");
        JAXBElement<?> returnVar = Util.RULEML_FACTORY.createVar(varType);

        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.setKeyref("outputEntry");
        atomType.getContent().add(Util.create("Rel", "outputEntry"));
        atomType.getContent().add(Util.create("Ind", tableId));
        atomType.getContent().add(Util.create("Ind", outputExpressionKey));
        atomType.getContent().add(Util.create("Ind", rowNumber));
        atomType.getContent().add(Util.create("Ind", id));
        atomType.getContent().add(returnVar);

        RelType metaRel = Util.RULEML_FACTORY.createRelType();
        metaRel.getContent().add("metaOutputEntry");

        AtomType metaAtom = Util.RULEML_FACTORY.createAtomType();
        //metaAtom.setKeyref("metaOutputEntry");
        metaAtom.getContent().add(Util.RULEML_FACTORY.createRel(metaRel));
        metaAtom.getContent().add(Util.RULEML_FACTORY.createData(id));
        metaAtom.getContent().add(Util.RULEML_FACTORY.createData(Util.sfeelStringToJavaString(source)));

        MetaType meta = Util.RULEML_FACTORY.createMetaType();
        meta.setAtom(metaAtom);

        AndInnerType and = Util.RULEML_FACTORY.createAndInnerType();

        ImpliesType impliesType = Util.RULEML_FACTORY.createImpliesType();
        impliesType.getContent().add(Util.RULEML_FACTORY.createMeta(meta));
        impliesType.getContent().add(Util.RULEML_FACTORY.createAnd(and));
        impliesType.getContent().add(Util.RULEML_FACTORY.createAtom(atomType));

        rulebaseType.getFormulaOrImpliesOrRule().add(impliesType);

        //OutputEntry(tableID, colNo, rowNo, key, value) :- contact("",..input.., value), ... rules ...
        //Add all condition when the output should shown.
        for (int i = 0; i < numberOfInputExpression; i++) {
            and.getFormulaOrOperatorOrAnd().add(
                    DecisionTableHelper.createInputEntryCondition(
                            tableId,
                            String.valueOf(i),
                            rowNumber
                    )
            );
        }

        //Translate FEEL statement to RuleML
        FeelToRuleML.runTranslation(FeelToRuleMLTranslation.getParseTree(source).simpleExpression(), and,
                returnVar, FeelToRuleML.TranslationMode.ASSIGNMENT);

    }

    /**
     * Create a InputEntry predicates to include in implies body at output entry.
     * See code.
     * {@code
     * OutputEntry(tableId, columnNumber, rowNumber, key, value) :- value="foobar", InputEntry(tableId, 0, rowNumber),
     * InputEntry(tableId, 1, rowNumber),
     * ...
     * InputEntry(tableId, columnNumber, rowNumber).
     * }
     *
     * @param tableId      Table ID
     * @param columnNumber Number of the current column
     * @param rowNumber    Number of the current row
     * @return Atom object
     */
    private static AtomType createInputEntryCondition(String tableId, String columnNumber, String rowNumber) throws RuleTranslationException {
        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.setKeyref("inputEntry");
        atomType.getContent().add(Util.create("Rel", "inputEntry"));
        atomType.getContent().add(Util.create("Ind", tableId));
        atomType.getContent().add(Util.create("Ind", columnNumber));
        atomType.getContent().add(Util.create("Ind", rowNumber));

        return atomType;
    }
}
