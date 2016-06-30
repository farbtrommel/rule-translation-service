package de.farbtrommel.rts.translation.prova2ruleml;

import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.Util;
import org.ruleml.deliberation.IndType;
import org.ruleml.deliberation.OidType;
import org.ruleml.deliberation.RulebaseType;

import java.util.Collection;
import java.util.HashMap;

public class RulebaseDispatcher {
    HashMap<String, RulebaseType> rulebase = new HashMap<>();

    public RulebaseDispatcher() {

    }

    public RulebaseType getRulebase(String id) throws RuleTranslationException {
        if (id.isEmpty()) {
            id = "___non_rulebase___";
        }

        RulebaseType rulebaseType = rulebase.get(id);
        if (rulebaseType == null) {
            rulebaseType = Util.RULEML_FACTORY.createRulebaseType();
            if (!id.equals("___non_rulebase___")) {
                OidType oid = Util.RULEML_FACTORY.createOidType();
                IndType ind = Util.RULEML_FACTORY.createIndType();
                ind.getContent().add(id);
                oid.setInd(ind);
                rulebaseType.setOid(oid);
            }

            rulebase.put(id, rulebaseType);
        }

        return rulebaseType;
    }

    public Collection<RulebaseType> getAll() {
        return rulebase.values();
    }
}
