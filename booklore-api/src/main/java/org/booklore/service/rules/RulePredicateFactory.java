package org.booklore.service.rules;

import jakarta.persistence.criteria.Predicate;
import org.booklore.model.dto.Rule;
import org.booklore.model.dto.RuleOperator;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

@Component
public class RulePredicateFactory {

    private final Map<RuleOperator, BiFunction<Rule, RuleCriteriaContext, Predicate>> handlers;

    public RulePredicateFactory(EqualsRuleHandler equalsHandler,
                                ContainsRuleHandler containsHandler,
                                CompareRuleHandler compareHandler) {
        // Можеш да добавяш нов handler без да пипаш service -> OCP
        handlers = new EnumMap<>(RuleOperator.class);

        handlers.put(RuleOperator.EQUALS, equalsHandler::build);
        handlers.put(RuleOperator.NOT_EQUALS, (r, c) -> c.getCb().not(equalsHandler.build(r, c)));

        handlers.put(RuleOperator.CONTAINS, containsHandler::contains);
        handlers.put(RuleOperator.DOES_NOT_CONTAIN, (r, c) -> c.getCb().not(containsHandler.contains(r, c)));
        handlers.put(RuleOperator.STARTS_WITH, containsHandler::startsWith);
        handlers.put(RuleOperator.ENDS_WITH, containsHandler::endsWith);

        handlers.put(RuleOperator.GREATER_THAN, compareHandler::greaterThan);
        handlers.put(RuleOperator.GREATER_THAN_EQUAL_TO, compareHandler::greaterThanOrEqual);
        handlers.put(RuleOperator.LESS_THAN, compareHandler::lessThan);
        handlers.put(RuleOperator.LESS_THAN_EQUAL_TO, compareHandler::lessThanOrEqual);

        // Останалите ги добавяме по същия начин (IN_BETWEEN, IS_EMPTY, INCLUDES_ANY, etc.)
    }

    public Predicate build(Rule rule, RuleCriteriaContext ctx) {
        var op = rule.getOperator();
        var handler = handlers.get(op);
        if (handler == null) return ctx.getCb().conjunction();
        return handler.apply(rule, ctx);
    }
}

