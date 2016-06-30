package de.farbtrommel.rts.translation.feel2ruleml;

import de.farbtrommel.rts.translation.Util;
import org.antlr.v4.runtime.tree.ParseTree;
import org.joda.time.DateTime;
import org.omg.dmn.feel.DmnFeelParser;
import org.ruleml.deliberation.RelType;
import org.ruleml.deliberation.VarType;

import javax.xml.bind.JAXBElement;

/**
 * Static methods or attributes to support the Translation of S-FEEL expression to RuleML.
 */
public class FeelAntlrRuleMLHelper {
    public static Class ArithmeticVisitorClasses[] = new Class[]{
            DmnFeelParser.LblDivisionContext.class,
            DmnFeelParser.LblSubtractionContext.class,
            DmnFeelParser.LblAdditionContext.class,
            DmnFeelParser.LblMultiplicationContext.class,
            DmnFeelParser.LblExponentiationContext.class,
            DmnFeelParser.LblSimpleAdditionContext.class,
            DmnFeelParser.LblSimpleDivisionContext.class,
            DmnFeelParser.LblSimpleSubtractionContext.class,
            DmnFeelParser.LblSimpleMultiplicationContext.class,
            DmnFeelParser.LblSimpleExponentiationContext.class,
    };


    public static JAXBElement<RelType> convertComparison(String operator) {
        String operatorIri = "NonValidOperator";
        String operatorName = "NonValidOperatorName";
        switch (operator) {
            case "=":
                operatorIri = "http://www.w3.org/2003/11/swrlb#equal";
                operatorName = "equal";
                break;
            case "!=":
                operatorIri = "http://www.w3.org/2003/11/swrlb#notEqual";
                operatorName = "notEqual";
                break;
            case "<":
            case ")":
                operatorIri = "http://www.w3.org/2003/11/swrlb#lessThan";
                operatorName = "lessThan";
                break;
            case "<=":
            case "]":
                operatorIri = "http://www.w3.org/2003/11/swrlb#lessThanOrEqual";
                operatorName = "lessThanOrEqual";
                break;
            case ">":
            case "(":
                operatorIri = "http://www.w3.org/2003/11/swrlb#greaterThan";
                operatorName = "greaterThan";
                break;
            case ">=":
            case "[":
                operatorIri = "http://www.w3.org/2003/11/swrlb#greaterThanOrEqual";
                operatorName = "greaterThanOrEqual";
                break;
        }
        RelType relType = Util.RULEML_FACTORY.createRelType();
        relType.getContent().add(operatorName);
        relType.setIri(operatorIri);
        return Util.RULEML_FACTORY.createRel(relType);
    }

    public static JAXBElement<RelType> convertArithmetic(Object ctx) {
        String operatorIri = "NonValidOperatorIri";
        String operatorName = "NonValidOperatorName";
        if (
                ctx.getClass() == DmnFeelParser.LblSubtractionContext.class ||
                        ctx.getClass() == DmnFeelParser.LblSimpleSubtractionContext.class
                ) {
            operatorIri = "http://www.w3.org/2003/11/swrlb#subtract";
            operatorName = "subtract";
        } else if (
                ctx.getClass() == DmnFeelParser.LblAdditionContext.class ||
                        ctx.getClass() == DmnFeelParser.LblSimpleAdditionContext.class
                ) {
            operatorIri = "http://www.w3.org/2003/11/swrlb#add";
            operatorName = "add";
        } else if (
                ctx.getClass() == DmnFeelParser.LblMultiplicationContext.class ||
                        ctx.getClass() == DmnFeelParser.LblSimpleMultiplicationContext.class
                ) {
            operatorIri = "http://www.w3.org/2003/11/swrlb#multiply";
            operatorName = "multiply";
        } else if (
                ctx.getClass() == DmnFeelParser.LblDivisionContext.class ||
                        ctx.getClass() == DmnFeelParser.LblSimpleDivisionContext.class
                ) {
            operatorIri = "http://www.w3.org/2003/11/swrlb#divide";
            operatorName = "divide";
        } else if (
                ctx.getClass() == DmnFeelParser.LblExponentiationContext.class ||
                        ctx.getClass() == DmnFeelParser.LblSimpleExponentiationContext.class
                ) {
            operatorIri = "http://www.w3.org/2003/11/swrlb#pow";
            operatorName = "pow";
        }
        RelType relType = Util.RULEML_FACTORY.createRelType();
        relType.setIri(operatorIri);
        relType.getContent().add(operatorName);
        return Util.RULEML_FACTORY.createRel(relType);
    }

    public static JAXBElement<?> convertLiteral(ParseTree obj) {
        if (obj instanceof DmnFeelParser.TypeLiteralContext ||
                obj instanceof DmnFeelParser.SimpleValueContext) {
            obj = obj.getChild(0);
        }

        if (obj instanceof DmnFeelParser.LblNameContext) {
            VarType varType = Util.RULEML_FACTORY.createVarType();
            varType.getContent().add(((DmnFeelParser.LblNameContext) obj).getText());
            return Util.RULEML_FACTORY.createVar(varType);
        }

        if (obj instanceof DmnFeelParser.LblQuantifiedExpressionContext ||
                obj instanceof DmnFeelParser.QualifiedNameContext) {

        }

        String dataValue = "ParseError";
        JAXBElement<Object> data = Util.RULEML_FACTORY.createData(null);
        if (obj instanceof DmnFeelParser.TypeBooleanContext) {
            //dataType = "boolean";
            dataValue = ((DmnFeelParser.TypeBooleanContext) obj).getText();
            data.setValue(Boolean.valueOf(dataValue));
        } else if (obj instanceof DmnFeelParser.TypeNumericContext) {
            //dataType = "decimal";
            dataValue = ((DmnFeelParser.TypeNumericContext) obj).getText();
            data.setValue(Double.valueOf(dataValue));
        } else if (obj instanceof DmnFeelParser.TypeNullContext) {
            //dataType = "null";
            data.setValue(null);
        } else if (obj instanceof DmnFeelParser.TypeStringContext) {
            //dataType = "string";
            dataValue = ((DmnFeelParser.TypeStringContext) obj).getText();
            data.setValue(Util.sfeelStringToJavaString(String.valueOf(dataValue)));
        } else if (obj instanceof DmnFeelParser.TypeDateContext) {
            //dataType = "date";
            dataValue = ((DmnFeelParser.TypeDateContext) obj).getText();
            data.setValue(new DateTime(dataValue));
        }

        return data;
    }
}
