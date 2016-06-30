package de.farbtrommel.rts.translation.dmn2ruleml;

import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.Util;
import org.omg.dmn.*;
import org.ruleml.deliberation.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Translate a DMN decision table to RuleML.
 */
public class DecisionTable {

    /**
     * Translate a DMN Table to RuleML.
     *
     * @param ruleML    RuleML JAXB Object where the DMN Table get inserted.
     * @param tDecision JAXB unmarshalled DMN Table.
     * @throws RuleTranslationException
     */
    public static void parse(RuleMLType ruleML, TDecision tDecision) throws RuleTranslationException {
        String decisionId = tDecision.getId();
        if (decisionId == null || decisionId.isEmpty()) {
            decisionId = "decisionId";
        }

        AtomType decisionTagFact = Util.RULEML_FACTORY.createAtomType();
        decisionTagFact.setKeyref("decision");
        decisionTagFact.getContent().add(Util.create("Rel", "decision"));
        decisionTagFact.getContent().add(Util.create("Ind", decisionId));
        decisionTagFact.getContent().add(Util.create("Ind", tDecision.getLabel()));
        decisionTagFact.getContent().add(Util.create("Ind", tDecision.getName()));

        MetaType metaDecision = Util.RULEML_FACTORY.createMetaType();
        metaDecision.setAtom(decisionTagFact);

        ruleML.getMeta().add(metaDecision);

        JAXBElement<? extends TExpression> expr = tDecision.getExpression();
        if (expr.getDeclaredType().equals(TDecisionTable.class)) {
            DecisionTable.parseDecisionTable(ruleML, (TDecisionTable) expr.getValue());
        }
    }

    /**
     * @param tDecisionTable
     */
    private static void parseDecisionTable(RuleMLType ruleML,
                                           TDecisionTable tDecisionTable) throws RuleTranslationException {
        //Create RuleML Statements
        AssertType assertType = Util.RULEML_FACTORY.createAssertType();
        FormulaAssertType formulaDecisionTable = Util.RULEML_FACTORY.createFormulaAssertType();
        assertType.getFormulaOrRulebaseOrImplies().add(formulaDecisionTable);
        ruleML.getActOrAssertOrRetract().add(assertType);
        RulebaseType decisionTable = Util.RULEML_FACTORY.createRulebaseType();
        decisionTable.setIri(Util.DMN_NAMESPACE + "#DecisionTable");
        formulaDecisionTable.setRulebase(decisionTable);

        //Create signatures for decision table components
        DecisionTable.createSignatures(decisionTable);

        //Table id
        String decisionTableId = tDecisionTable.getId();
        if (decisionTableId == null || decisionTableId.isEmpty()) {
            decisionTableId = "decisionTableId" + Util.sCounter.getAndIncrement();
            ;
        }

        AtomType decisionTableFact = Util.RULEML_FACTORY.createAtomType();
        decisionTableFact.setKeyref("decisionTable");
        decisionTableFact.getContent().add(Util.create("Rel", "decisionTable"));
        decisionTableFact.getContent().add(Util.create("Ind", decisionTableId));
        //decisionTableFact.getContent().add(Util.create("Ind", tDecisionTable.getOutputLabel().toString()));
        decisionTableFact.getContent().add(Util.create("Ind", tDecisionTable.getHitPolicy().value()));
        if (tDecisionTable.getAggregation() == null) {
            decisionTableFact.getContent().add(Util.create("Ind", ""));
        } else {
            decisionTableFact.getContent().add(Util.create("Ind", tDecisionTable.getAggregation().value()));
        }


        decisionTable.getFormulaOrImpliesOrRule().add(decisionTableFact);


        //Convert each table item to RuleML
        Integer index = 0;
        DecisionTable.translateInputExpressions(decisionTable, decisionTableId, tDecisionTable.getInput());
        List<String> keys = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        DecisionTable.translateOutputExpressions(decisionTable, keys, labels, decisionTableId, tDecisionTable.getOutput());
        DecisionTable.translateRules(assertType, keys, labels, decisionTableId, tDecisionTable.getRule());

    }

    private static void translateInputExpressions(RulebaseType decisionTable, String tableId,
                                                  List<TInputClause> inputClauses) throws RuleTranslationException {
        Integer colNo = 0;
        for (TInputClause item : inputClauses) {
            //"serialize" inputClause to meta

            AtomType meta = Util.RULEML_FACTORY.createAtomType();
            //meta.setKeyref("inputClause");
            meta.getContent().add(Util.create("rel", "inputClause"));
            meta.getContent().add(Util.create("ind", item.getId()));
            meta.getContent().add(Util.create("ind", item.getLabel()));
            meta.getContent().add(Util.create("ind", Util.serializeQName(item.getInputExpression().getTypeRef())));
            meta.getContent().add(Util.create("ind", (item.getInputExpression().getText().isEmpty()) ? "-" : item.getInputExpression().getText()));
            MetaType metaType = Util.RULEML_FACTORY.createMetaType();
            metaType.setAtom(meta);

            DecisionTableHelper.translateInputExpression(
                    decisionTable,
                    tableId,
                    String.valueOf(colNo++),
                    item.getInputExpression().getText(),
                    metaType);
        }
    }

    private static void translateOutputExpressions(RulebaseType decisionTable, List<String> keys, List<String> labels,
                                                   String tableId, List<TOutputClause> outputClauses) throws RuleTranslationException {
        for (TOutputClause item : outputClauses) {
            keys.add(item.getName());
            labels.add(item.getLabel());

            DecisionTableHelper.translateOutputExpression(decisionTable, tableId,
                    item);
        }
    }

    private static void translateRules(AssertType assertType, List<String> keys, List<String> labels,
                                       String tableId, List<TDecisionRule> rules) throws RuleTranslationException {
        final AtomicInteger rowNo = new AtomicInteger(0);
        //rules.stream().forEach isn't possible, because
        // rowNo needed be in sequenz
        // workaround create packages with rule and rowNo

        for (TDecisionRule item : rules) {
            FormulaAssertType formulaAssertType = Util.RULEML_FACTORY.createFormulaAssertType();
            RulebaseType rulebaseType = Util.RULEML_FACTORY.createRulebaseType();
            formulaAssertType.setRulebase(rulebaseType);
            assertType.getFormulaOrRulebaseOrImplies().add(formulaAssertType);

            String ruleId = item.getId();
            if (ruleId == null || ruleId.isEmpty()) {
                ruleId = "rule" + Util.sCounter.getAndIncrement();
            }
            OidType oidType = Util.RULEML_FACTORY.createOidType();
            IndType indOidType = Util.RULEML_FACTORY.createIndType();
            indOidType.getContent().add(ruleId);
            oidType.setInd(indOidType);

            rulebaseType.setOid(oidType);
            try {
                AtomType atomType = Util.RULEML_FACTORY.createAtomType();
                atomType.setKeyref("rule");
                atomType.getContent().add(Util.create("rel", "rule"));
                atomType.getContent().add(Util.create("ind", tableId));
                atomType.getContent().add(Util.create("ind", ruleId));
                atomType.getContent().add(Util.create("ind", item.getLabel()));
                atomType.getContent().add(Util.create("ind", item.getDescription()));

                rulebaseType.getFormulaOrImpliesOrRule().add(atomType);

                translateRulesInputEntry(rulebaseType, tableId, rowNo.get(),
                        item.getInputEntry());
                translateRulesOutputEntry(rulebaseType, keys, labels, tableId,
                        item.getInputEntry().size(), rowNo.get(), item.getOutputEntry());
            } catch (Exception e) {
                //TODO
            }
            rowNo.incrementAndGet();
        }
    }

    private static void translateRulesInputEntry(RulebaseType rulebaseType,
                                                 String tableId, Integer rowNo, List<TUnaryTests> inputEntries)
            throws RuleTranslationException {
        int s = 0;
        //Translate all condition of a cell to a implication
        for (TUnaryTests input : inputEntries) {
            DecisionTableHelper.translateInputEntry(rulebaseType,
                    tableId,
                    input.getId(),
                    String.valueOf(s++),
                    rowNo.toString(),
                    input.getText());
        }
    }

    private static void translateRulesOutputEntry(RulebaseType rulebaseType, List<String> keys, List<String> labels,
                                                  String tableId, Integer numberOfRules,
                                                  Integer rowNo, List<TLiteralExpression> outputEntries)
            throws RuleTranslationException {
        int s = 0;
        //Join all conditions.
        for (TLiteralExpression output : outputEntries) {
            DecisionTableHelper.translateOutputEntry(rulebaseType,
                    tableId,
                    numberOfRules,
                    String.valueOf(s),
                    rowNo.toString(),
                    output.getId(),
                    keys.get(s),
                    //labels.get(s),
                    output.getText());
            s++;
        }
    }

    /**
     * Add all signatures to rule base.
     *
     * @param decisionTable Rule base to add the signatures
     */
    private static void createSignatures(RulebaseType decisionTable) {
        //Signature definitions
        decisionTable.getSignature().add(
                createSignature("definitions", new String[][]{
                        new String[]{"ind", "DefinitionsId"},
                        new String[]{"ind", "DefinitionsName"}
                })
        );

        //Signature Decision
        decisionTable.getSignature().add(
                createSignature("decision", new String[][]{
                        new String[]{"ind", "DecisionId"},
                        new String[]{"ind", "DecisionName"}
                })
        );

        //Signature Decision Table
        decisionTable.getSignature().add(
                createSignature("decisionTable", new String[][]{
                        new String[]{"ind", "DecisionTableId"},
                        new String[]{"ind", "HitPolicy"},
                        new String[]{"ind", "Aggregation"},
                })
        );

        //Signature InputExpression:
        decisionTable.getSignature().add(
                createSignature("inputExpression", new String[][]{
                        new String[]{"ind", "TableId"},
                        new String[]{"ind", "ColumnNumber"},
                        new String[]{"var", "Value"}
                })
        );
        //Signature InputEntry:
        decisionTable.getSignature().add(
                createSignature("inputEntry",
                        new String[][]{
                                new String[]{"ind", "TableId"},
                                new String[]{"ind", "ColumnNumber"},
                                new String[]{"ind", "RowNumber"}
                        })
        );

        //Signature Rule:
        decisionTable.getSignature().add(
                createSignature("rule",
                        new String[][]{
                                new String[]{"ind", "TableId"},
                                new String[]{"ind", "Id"},
                                new String[]{"ind", "Label"},
                                new String[]{"ind", "Description"}
                        })
        );
        //Signature OutputExpression:
        decisionTable.getSignature().add(
                createSignature("inputClause",
                        new String[][]{
                                new String[]{"ind", "TableId"},
                                new String[]{"ind", "Id"},
                                new String[]{"ind", "Label"},
                                new String[]{"ind", "TypeRef"},
                                new String[]{"ind", "Expression"}
                        })
        );
        //Signature OutputExpression:
        decisionTable.getSignature().add(
                createSignature("outputExpression",
                        new String[][]{
                                //new String[]{"var", "Result"},
                                new String[]{"ind", "TableId"},
                                new String[]{"ind", "Key"},
                                new String[]{"ind", "Name"},
                                new String[]{"ind", "Label"}
                        })
        );
        //Signature OutputEntry:
        decisionTable.getSignature().add(
                createSignature("metaOutputEntry", new String[][]{
                        new String[]{"ind", "Id"},
                        new String[]{"ind", "Expression"}})
        );
        //Signature OutputEntry:
        decisionTable.getSignature().add(
                createSignature("outputEntry", new String[][]{
                        new String[]{"ind", "TableId"},
                        //new String[]{"ind", "ColumnNumber"},
                        new String[]{"ind", "key"},
                        new String[]{"ind", "RowNumber"},
                        //new String[]{"ind", "label"},
                        new String[]{"ind", "id"},
                        new String[]{"var", "Value"}})
        );

    }

    /**
     * {@code
     * <signature>
     * <Atom>
     * <Rel>«keyRef»</Rel>
     * <Var>«Fields[i]»</Var>
     * <Var>«Fields[i+1]»</Var>
     * ...
     * <Var>«Fields[n]»</Var>
     * </Atom>
     * </signature>
     * }
     *
     * @param keyRef Name of Signature
     * @param fields List of variable names.
     * @return
     */
    private static SignatureType createSignature(String keyRef, String[][] fields) {
        SignatureType signature = Util.RULEML_FACTORY.createSignatureType();

        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        RelType relType = Util.RULEML_FACTORY.createRelType();
        //relType.setIri(Util.serializeQName(Util.sDmnQNameInputClause));
        relType.getContent().add(keyRef);
        atomType.getContent().add(Util.RULEML_FACTORY.createRel(relType));
        atomType.setKey(keyRef);

        for (String[] field : fields) {
            if (field[0].toLowerCase().equals("var")) {
                VarType varType = Util.RULEML_FACTORY.createVarType();
                varType.getContent().add(field[1]);
                atomType.getContent().add(Util.RULEML_FACTORY.createVar(varType));
            } else if (field[0].toLowerCase().equals("ind")) {
                IndType indType = Util.RULEML_FACTORY.createIndType();
                indType.getContent().add(field[1]);
                atomType.getContent().add(Util.RULEML_FACTORY.createInd(indType));
            }
        }

        signature.setAtom(atomType);

        return signature;
    }

    /**
     * Create a Meta Tag where the information stored in a slotted structure.
     * {@code
     * <Meta index="«index»">
     * <Atom iri=«qName»>
     * <Slot>
     * ... «list» ... see createSlot
     * </Slot>
     * </Atom>
     * </Meta>
     * }
     *
     * @param qName
     * @param list
     * @return
     */
    protected static MetaType createMetaTag(QName qName, List<JAXBElement<?>> list) {
        RelType relType = Util.RULEML_FACTORY.createRelType();
        if (qName != null) {
            relType.getContent().add(qName.getLocalPart());
            relType.setIri(Util.serializeQName(qName));
        }

        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.getContent().add(Util.RULEML_FACTORY.createRel(relType));
        for (JAXBElement<?> item : list) {
            atomType.getContent().add(item);
        }


        MetaType metaType = Util.RULEML_FACTORY.createMetaType();
        metaType.setAtom(atomType);
        //metaType.setIndex(new BigInteger(String.valueOf(index)));

        return metaType;
    }

    /**
     * Create a slot.
     * {@code
     * <slot>
     * <Ind>«key»</Ind>
     * <Ind>«value»</Ind>
     * </slot>
     * }
     *
     * @param key
     * @param value
     * @return
     */
    protected static JAXBElement<SlotType> createSlot(String key, String value) {
        IndType keyInd = Util.RULEML_FACTORY.createIndType();
        keyInd.getContent().add(String.valueOf(key));
        IndType valueInd = Util.RULEML_FACTORY.createIndType();
        valueInd.getContent().add(String.valueOf((value == null || value.isEmpty()) ? "" : value));

        SlotType slotType = Util.RULEML_FACTORY.createSlotType();
        slotType.getContent().add(Util.RULEML_FACTORY.createInd(keyInd));
        slotType.getContent().add(Util.RULEML_FACTORY.createInd(valueInd));

        return Util.RULEML_FACTORY.createSlot(slotType);
    }


}
