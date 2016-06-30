package de.farbtrommel.rts.translation.feel2ruleml;

import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.Util;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.omg.dmn.feel.DmnFeelParser;
import org.ruleml.deliberation.*;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DMN Feel Expressions to RuleML based on the ANTLR ParseTrees.
 */
public class FeelToRuleML {

    static class Settings {
        TranslationMode mode;
        HashMap<String, String> lookupTableQualifiedName;
        JAXBElement<?> initVar;
        JAXBElement<?> lastReturnValue;
        boolean lastStatementAdded = false;

        Settings(JAXBElement<?> initVar, TranslationMode mode) {
            this.initVar = initVar;
            this.mode = mode;
            this.lookupTableQualifiedName = new HashMap<String, String>();
        }

    }

    /**
     * ASSIGNMENT: InputExpression should a literal or simple value returned "to predicate head".
     * COMPARISON: InputEntry or OutputEntry should be a literal or simple value compared to returnValue.
     */
    public enum TranslationMode {
        ASSIGNMENT, COMPARISON
    }

    ;


    /**
     * Initialize transformation by creating a Predicate for the DMN Feel Expression.
     * Call breadthFirstSearch not included in the ANTLR Visitor Pattern to retrieve ParseTree.
     * <p/>
     * {@code
     * <RuleML>
     * <Assert>
     * <formula>
     * <Rulebase>
     * <Implies>
     * <And>
     * <Atom>
     * <Rel iri="swrlb:lessThen">lessThen</Rel>
     * <Var>input<Var>
     * <Data iri="xml:integer">12</Data>
     * </Atom>
     * <!-- Try to add all constrains here -->
     * </And>
     * <Atom>
     * <Rel>DmnFeelRuleCell1</Rel>
     * <Var>input</Var>
     * </Atom>
     * </Implies>
     * <!-- Some cases necessary to add other implies, e.g. logical or. -->
     * </Rulebase>
     * </formula>
     * </Assert>
     * </RuleML>
     * <!--
     * DmnFeelRuleCell1(«input») :- lessThen^swrlb(«input», 12);
     * -->
     * }
     *
     * @param tree Parsed Feel Expression like "2*b+2.3 < 3**c".
     */
    public static void runTranslation(ParseTree tree, AndInnerType and, JAXBElement<?> returnVariable, TranslationMode mode) throws RuleTranslationException {
        //Save return var.
        Settings settings = new Settings(returnVariable, mode);

        //Find all Qualified names and add a query to rule body. Like context(foo, storage1)
        lookupQualifiedName(tree, settings, and);
        JAXBElement firstReturnVar;
        if (mode == TranslationMode.ASSIGNMENT) {
            firstReturnVar = createVariable();
        } else {
            firstReturnVar = returnVariable;
            settings.lastReturnValue = returnVariable;
        }
        breadthFirstSearch(tree, and, firstReturnVar, settings);

        if (!settings.lastStatementAdded && settings.mode == TranslationMode.ASSIGNMENT) {
            addAssignmentStatement(settings, and, settings.initVar,
                    firstReturnVar);
        } else if (!settings.lastStatementAdded && settings.mode == TranslationMode.COMPARISON &&
                !settings.initVar.equals(settings.lastReturnValue)) {
            addEqualStatement(settings, and, settings.initVar,
                    settings.lastReturnValue);
        }
    }

    public static void runTranslation(String code, AndInnerType and, JAXBElement<?> returnValue, TranslationMode mode) throws RuleTranslationException {
        runTranslation(FeelToRuleMLTranslation.getParseTree(code).expression(), and, returnValue, mode);
    }

    /**
     * Walk the Grammar Tree with the breadth first search strategy.
     *
     * @param tree           ANTLR Grammar Tree of parsed Statement.
     * @param and            Current Predicate where the condition get add.
     * @param returnVariable The result should be stored to the variable.
     * @see #unaryTest
     * @see #arithmetic
     */
    private static void breadthFirstSearch(ParseTree tree,
                                           AndInnerType and, JAXBElement<?> returnVariable,
                                           Settings settings) throws RuleTranslationException {
        /*if (tree.getClass() == DmnFeelParser.LblDisjunctionContext.class) {
            disjunction(tree, and, returnVariable, settings);
        } else if (tree.getClass() == DmnFeelParser.LblConjunctionContext.class) {
            conjunction(tree, and, returnVariable, settings);
        } else*/
        if (tree.getClass() == DmnFeelParser.LblSimpleUnaryTestsContext.class ||
                tree.getClass() == DmnFeelParser.LblSimpleUnaryTestContext.class) {
            //settings.lastStatementAdded = true;
            //It is no constraint defined. Therefore, let be "and" object empty. Empty and is always true.
            if (tree.getChild(0).getChild(0).getClass() == DmnFeelParser.LblSimpleUnaryTestsNilContext.class) {
                return;
            }
            unaryTest(tree.getChild(0), and, returnVariable, settings);
        } /*else if (tree.getClass() == DmnFeelParser.LblBetweenContext.class) {
            between(tree, and, returnVariable, settings);
        } else if (tree.getClass() == DmnFeelParser.LblComparisonContext.class) {
            comparison(tree, and, returnVariable, settings);
        } */ else if (tree.getClass() == DmnFeelParser.LblSimpleExpressionContext.class) {
            breadthFirstSearch(tree.getChild(0), and, returnVariable, settings);
        } else if (settings.lastStatementAdded == false && (tree.getClass() == DmnFeelParser.LblLiteralContext.class ||
                tree.getClass() == DmnFeelParser.LblSimpleValueContext.class)) {
            if (settings.mode == TranslationMode.ASSIGNMENT) {
                addAssignmentStatement(settings, and, settings.initVar,
                        isLiteral(tree, settings));
            } else {
                addEqualStatement(settings, and, settings.initVar,
                        isLiteral(tree, settings));
            }
            settings.lastStatementAdded = true;
        } else if (tree.getClass() == DmnFeelParser.LblSimpleExpressionsContext.class) {
            for (int i = 0; i < tree.getChildCount(); i++) {
                if (tree.getChild(i).getClass() != TerminalNodeImpl.class) {
                    breadthFirstSearch(tree.getChild(i),
                            and, returnVariable, settings);
                }
            }
        } else {
            for (Class ArithmeticVisitorClass : FeelAntlrRuleMLHelper.ArithmeticVisitorClasses) {
                if (tree.getClass() == ArithmeticVisitorClass) {
                    arithmetic(tree, and, returnVariable, settings);
                    break;
                }
            }
        }
    }

    private static void lookupQualifiedName(ParseTree tree, Settings settings,
                                            AndInnerType and) {
        for (int i = 0; i < tree.getChildCount(); i++) {
            if (tree.getChild(i).getClass() == DmnFeelParser.LblQuantifiedExpressionContext.class ||
                    tree.getChild(i).getClass() == DmnFeelParser.QualifiedNameContext.class) {
                AtomType query = Util.RULEML_FACTORY.createAtomType();
                RelType rel = Util.RULEML_FACTORY.createRelType();
                rel.getContent().add("context");
                IndType key = Util.RULEML_FACTORY.createIndType();
                String keyName = parseQualifiedName(tree.getChild(i));
                key.getContent().add(keyName);
                VarType result = Util.RULEML_FACTORY.createVarType();
                String resultName = "Storage" + Util.sCounter.getAndIncrement();
                result.getContent().add(resultName);
                query.getContent().add(Util.RULEML_FACTORY.createRel(rel));
                query.getContent().add(Util.RULEML_FACTORY.createInd(key));
                query.getContent().add(Util.RULEML_FACTORY.createVar(result));

                settings.lookupTableQualifiedName.put(keyName, resultName);

                and.getFormulaOrOperatorOrAnd().add(query);
            } else {
                lookupQualifiedName(tree.getChild(i), settings, and);
            }
        }
    }

    private static String parseQualifiedName(ParseTree tree) {
        String keyName = "";
        for (int s = 0; s < tree.getChildCount(); s++) {
            keyName += tree.getChild(s).getText() + " ";
        }
        keyName = keyName.replaceAll("\\s$", "");
        return keyName;
    }

    /**
     * Assign the value of operand2 to operand1.
     * {@code
     * <Equal>
     * «operand1»
     * «operand2»
     * </Equal>
     * }
     *
     * @param settings
     * @param atom     Added the equal statement to it.
     * @param operand1 This Term should be equal to operand2
     * @param operand2 This Term should be equal to operand1
     */
    private static void addAssignmentStatement(Settings settings, AndInnerType atom,
                                               JAXBElement<?> operand1, JAXBElement<?> operand2) {
        EqualType equalType = Util.RULEML_FACTORY.createEqualType();
        equalType.getContent().add(operand1);
        equalType.getContent().add(operand2);
        atom.getFormulaOrOperatorOrAnd().add(equalType);
    }

    /**
     * Compare operand1 and operand2, if there are equal.
     * {@code
     * <Atom>
     * <Rel iri="swrbl:equal">equal</Rel>
     * «operand1»
     * «operand2»
     * </Atom>
     * }
     *
     * @param settings
     * @param atom
     * @param operand1
     * @param operand2
     */
    private static void addEqualStatement(Settings settings, AndInnerType atom,
                                          JAXBElement<?> operand1, JAXBElement<?> operand2) {
        AtomType equal = Util.RULEML_FACTORY.createAtomType();
        equal.getContent().add(FeelAntlrRuleMLHelper.convertComparison("="));
        equal.getContent().add(operand1);
        equal.getContent().add(operand2);
        atom.getFormulaOrOperatorOrAnd().add(equal);
    }

    /**
     * Unary Test: <= 10 or ]5 .. 20]
     * Grammar Rule:
     * <ul>
     * <li>14 simple unary tests</li>
     * <li>13 simple positive unary tests</li>
     * <li>7 simple positive unary test</li>
     * <li>8 interval</li>
     * </ul>
     * <p/>
     * Grammar Rule 7:
     * <code>
     * (< | <= | > | >=) Endpoint
     * interval
     * </code>
     * Translations Rule:
     * <code>
     * lessThen^swrlb(returnVariable, «mainVarType», «Endpoint^Literal»)
     * </code>
     *
     * @param tree           ANTLR Grammar Tree of parsed Statement.
     * @param and            Current Predicate where the condition get add.
     * @param returnVariable the value to compare with
     */
    private static void unaryTest(ParseTree tree, AndInnerType and, JAXBElement<?> returnVariable,
                                  Settings settings) throws RuleTranslationException {

        if (tree.getClass() == DmnFeelParser.LblSimpleUnaryTestsContext.class ||
                tree.getClass() == DmnFeelParser.LblSimplePositiveUnaryTestsContext.class) {
            for (int i = 0; i < tree.getChildCount(); i++) {
                unaryTest(tree.getChild(i), and, returnVariable, settings);
            }
        } else if (tree.getClass() == DmnFeelParser.LblSimpleUnaryTestContext.class ||
                tree.getClass() == DmnFeelParser.LblUnaryTestEndpointContext.class
                ) {
            and.getFormulaOrOperatorOrAnd().add(
                    createComparison(
                            FeelAntlrRuleMLHelper.convertComparison(tree.getChild(0).getText()),
                            returnVariable,
                            isLiteral(tree.getChild(1), settings)
                    )
            );
        } else if (tree.getClass() == DmnFeelParser.LblUnaryTestIntervalContext.class) {
            DmnFeelParser.IntervalContext interval = (DmnFeelParser.IntervalContext) tree.getChild(0);
            and.getFormulaOrOperatorOrAnd().add(
                    createComparison(
                            FeelAntlrRuleMLHelper.convertComparison(interval.getStart().getText()),
                            returnVariable,
                            isLiteral(interval.startInterval, settings)
                    )
            );
            and.getFormulaOrOperatorOrAnd().add(
                    createComparison(
                            FeelAntlrRuleMLHelper.convertComparison(interval.getStop().getText()),
                            returnVariable,
                            isLiteral(interval.endInterval, settings)
                    )
            );
        } else if (tree.getClass() == DmnFeelParser.SimplePositiveUnaryTestsContext.class) {
            and.getFormulaOrOperatorOrAnd().add(
                    createComparison(
                            FeelAntlrRuleMLHelper.convertComparison(tree.getChild(0).getText()),
                            returnVariable,
                            isLiteral(tree.getChild(1), settings)
                    )
            );
            and.getFormulaOrOperatorOrAnd().add(
                    createComparison(
                            FeelAntlrRuleMLHelper.convertComparison(tree.getChild(3).getText()),
                            returnVariable,
                            isLiteral(tree.getChild(2), settings)
                    )
            );
        } else if (tree.getClass() == DmnFeelParser.LblSimpleUnaryTestsPositiveContext.class) {
            unaryTest(tree.getChild(0), and, returnVariable, settings);
        } else if (tree.getClass() == DmnFeelParser.LblSimpleUnaryTestsNegContext.class) {
            throw new RuleTranslationException("Negation not implemented");
        } else if (tree.getClass() == DmnFeelParser.LblSimpleUnaryTestsNilContext.class) {
            // No action. The predicate will any way be true.
        }
    }

    private static AtomType createComparison(JAXBElement<?> rel, JAXBElement<?> op1, JAXBElement<?> op2) {
        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.getContent().add(rel);
        atomType.getContent().add(op1);
        atomType.getContent().add(op2);
        return atomType;
    }

    /**
     * Is current vertex a Literal?
     *
     * @param tree Current vertex as root of a subtree.
     * @return Return literal or new variable.
     */
    private static JAXBElement<?> isLiteral(ParseTree tree, Settings settings) {
        return isLiteral(tree, settings, new AtomicBoolean(false));
    }

    private static JAXBElement<?> isLiteral(ParseTree tree, Settings settings, AtomicBoolean isLeaf) {
        isLeaf.set(false);
        if (tree == null) {
            //do nothing
        } else if (tree.getClass() == DmnFeelParser.EndpointContext.class) {
            return isLiteral(tree.getChild(0), settings, isLeaf);
        } else if (tree.getClass() == DmnFeelParser.LblSimpleValueContext.class) {
            return isLiteral(tree.getChild(0), settings, isLeaf);
        } else if (tree.getClass() == DmnFeelParser.SimpleValueContext.class) {
            isLeaf.set(true);
            if (tree.getChild(0).getClass() == DmnFeelParser.LblQuantifiedExpressionContext.class ||
                    tree.getChild(0).getClass() == DmnFeelParser.QualifiedNameContext.class) {
                VarType var = Util.RULEML_FACTORY.createVarType();
                var.getContent().add(
                        settings.lookupTableQualifiedName.get(
                                parseQualifiedName(tree.getChild(0))
                        )
                );
                return Util.RULEML_FACTORY.createVar(var);
            }
            return FeelAntlrRuleMLHelper.convertLiteral(tree.getChild(0));
        } else if (tree.getClass() == DmnFeelParser.LblLiteralContext.class) {
            isLeaf.set(true);
            return FeelAntlrRuleMLHelper.convertLiteral(tree.getChild(0));
        }

        JAXBElement<?> var = createVariable();
        settings.lastReturnValue = var;
        return var;
    }

    private static JAXBElement createVariable() {
        VarType varType = Util.RULEML_FACTORY.createVarType();
        varType.getContent().add("Var".concat(String.valueOf(Util.sCounter.getAndIncrement())));
        return Util.RULEML_FACTORY.createVar(varType);
    }
    /**
     * Between: «a» BETWEEN «b» and «c»
     * @param tree ANTLR Grammar Tree of parsed Statement.
     * @param atom Current Predicate where the condition get add.
     * @param returnVariable The result should be stored to the variable.
     */
    /*
    TODO: FEEL Expression
    private static void between(ParseTree tree, AndInnerType atom, JAXBElement<?> returnVariable,
                                Settings settings) throws RuleTranslationException {
        DmnFeelParser.LblBetweenContext statement = (DmnFeelParser.LblBetweenContext) tree;

        JAXBElement<?> result = isLiteral(statement.a, settings);
        breadthFirstSearch(statement.a, atom, result, settings);

        AtomType leftLimit = Util.RULEML_FACTORY.createAtomType();
        leftLimit.getContent().add(FeelAntlrRuleMLHelper.convertComparison(">="));
        leftLimit.getContent().add(returnVariable);
        leftLimit.getContent().add(result);
        JAXBElement<?> left = isLiteral(statement.b, settings);
        leftLimit.getContent().add(left);

        atom.getFormulaOrOperatorOrAnd().add(leftLimit);

        breadthFirstSearch(statement.b, atom, left, settings);

        AtomType rightLimit = Util.RULEML_FACTORY.createAtomType();
        rightLimit.getContent().add(FeelAntlrRuleMLHelper.convertComparison("<="));
        rightLimit.getContent().add(returnVariable);
        rightLimit.getContent().add(result);
        JAXBElement<?> right = isLiteral(statement.c, settings);
        rightLimit.getContent().add(right);

        atom.getFormulaOrOperatorOrAnd().add(rightLimit);

        breadthFirstSearch(statement.c, atom, right, settings);
    }
    */

    /**
     * Arithmetic Operation like +, -, *, /, ** get converted to SWRL Namespace.
     *
     * @param tree           ANTLR Grammar Tree of parsed Statement.
     * @param and            Current Predicate to add the condition
     * @param returnVariable The result should be stored to the variable.
     */
    private static void arithmetic(ParseTree tree, AndInnerType and, JAXBElement<?> returnVariable,
                                   Settings settings) throws RuleTranslationException {
        if (settings.mode == TranslationMode.COMPARISON && settings.initVar.equals(returnVariable)) {
            returnVariable = createVariable();
            settings.lastReturnValue = returnVariable;
        }
        AtomicBoolean leftIsLiteral = new AtomicBoolean(false);
        JAXBElement<?> left = isLiteral(tree.getChild(0), settings, leftIsLiteral);
        AtomicBoolean rightIsLiteral = new AtomicBoolean(false);
        JAXBElement<?> right = isLiteral(tree.getChild(2), settings, rightIsLiteral);


        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.getContent().add(FeelAntlrRuleMLHelper.convertArithmetic(tree));
        //if (leftIsLiteral.get() && rightIsLiteral.get()) {
        //    atomType.getContent().add(isLiteral(null, settings));
        //} else {
        atomType.getContent().add(returnVariable);
        //}
        atomType.getContent().add(left);
        atomType.getContent().add(right);

        if (!leftIsLiteral.get()) {
            breadthFirstSearch(tree.getChild(0), and, left, settings);
        }
        if (!rightIsLiteral.get()) {
            breadthFirstSearch(tree.getChild(2), and, right, settings);
        }

        and.getFormulaOrOperatorOrAnd().add(atomType);
    }

    /**
     * Disjunction is simple, because all arguments get combined by an and.
     * Therefore, we need to do nothing here.
     * @param tree ANTLR Grammar Tree of parsed Statement.
     * @param and Current Predicate where the condition get add.
     * @param returnVariable The result should be stored to the variable.
     */
    /*
        TODO: FEEL Expression
    private static void disjunction(ParseTree tree, AndInnerType and, JAXBElement<?> returnVariable,
                                    Settings settings) throws RuleTranslationException {
        breadthFirstSearch(tree.getChild(0), and, returnVariable, settings);
        breadthFirstSearch(tree.getChild(2), and, returnVariable, settings);
    }
    */

    /**
     * Conjunction are included in RuleML but not in OO jDrew.
     * Therefore, we add a workaround here by adding two predicates with the same name but with two different bodies.
     * @param tree ANTLR Grammar Tree of parsed Statement.
     * @param and Current Predicate where the condition get add.
     * @param returnVariable The result should be stored to the variable.
     */
    /*
        TODO: FEEL Expression
    private static void conjunction(ParseTree tree, AndInnerType and, JAXBElement<?> returnVariable,
                                    Settings settings) throws RuleTranslationException {
        String key = "dmnOr" + FeelToRuleML.ImpliesCounter++;

        ImpliesType leftImplies = Util.RULEML_FACTORY.createImpliesType();
        AndInnerType leftAnd = Util.RULEML_FACTORY.createAndInnerType();
        leftImplies.setKey(key);
        leftImplies.getContent().add(Util.RULEML_FACTORY.createAnd(leftAnd));
        leftImplies.getContent().add(Util.RULEML_FACTORY.createAtom(mainImpliesAtom));

        ImpliesType rightImplies = Util.RULEML_FACTORY.createImpliesType();
        AndInnerType rightAnd = Util.RULEML_FACTORY.createAndInnerType();
        rightImplies.setKey(key);
        rightImplies.getContent().add(Util.RULEML_FACTORY.createAnd(rightAnd));
        rightImplies.getContent().add(Util.RULEML_FACTORY.createAtom(mainImpliesAtom));

        rulebaseType.getFormulaOrImpliesOrRule().add(leftImplies);
        rulebaseType.getFormulaOrImpliesOrRule().add(rightImplies);

        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.setIri(Util.serializeQName(Util.sDmnQNameOr));
        atomType.setKeyref(key);

        and.getFormulaOrOperatorOrAnd().add(atomType);

        breadthFirstSearch(tree.getChild(0), leftAnd, returnVariable);
        breadthFirstSearch(tree.getChild(2), rightAnd, returnVariable);

        throw new RuleTranslationException("Conjunction not implmented.");
    }
    */

    /**
     * Comparison like =, <=, >=, <, > get convert with SWRL Namespace.
     * @param tree ANTLR Grammar Tree of parsed Statement.
     * @param atom Current Predicate where the condition get add.
     * @param returnVariable The result should be stored to the variable.
     */
    /*
        TODO: FEEL Expression
    private static void comparison(ParseTree tree, AndInnerType atom, JAXBElement<?> returnVariable,
                                   Settings settings) throws RuleTranslationException {
        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.getContent().add(FeelAntlrRuleMLHelper.convertComparison(tree.getChild(1).getText()));
        AtomicBoolean leftIsLeaf = new AtomicBoolean(false);
        JAXBElement<?> left = isLiteral(tree.getChild(0), settings, leftIsLeaf);
        atomType.getContent().add(left);
        AtomicBoolean rightIsLeaf = new AtomicBoolean(false);
        JAXBElement<?> right = isLiteral(tree.getChild(0), settings, rightIsLeaf);
        atomType.getContent().add(right);

        atom.getFormulaOrOperatorOrAnd().add(atomType);

        if (leftIsLeaf.get()) {
            breadthFirstSearch(tree.getChild(0), atom, left, settings);
        }
        if (rightIsLeaf.get()) {
            breadthFirstSearch(tree.getChild(2), atom, right, settings);
        }
    }
    */
}
