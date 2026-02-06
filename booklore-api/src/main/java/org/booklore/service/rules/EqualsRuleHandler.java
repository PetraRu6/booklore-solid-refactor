package org.booklore.service.rules;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.booklore.model.dto.Rule;
import org.booklore.model.dto.RuleField;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class EqualsRuleHandler {

    private final FieldExpressionResolver fieldResolver;
    private final ValueNormalizer normalizer;
    private final RuleValueUtils valueUtils;

    public Predicate build(Rule rule, RuleCriteriaContext ctx) {
        RuleField field = rule.getField();
        var cb = ctx.getCb();

        if (fieldResolver.isArrayField(field)) {
            List<String> list = valueUtils.toStringList(rule.getValue());
            return valueUtils.arrayEquals(field, list, ctx);
        }

        Expression<?> expr = fieldResolver.resolve(field, ctx.getCb(), ctx.getRoot(), ctx.getProgressJoin());
        if (expr == null) return cb.conjunction();

        Object value = normalizer.normalize(rule.getValue(), field);

        if (field == RuleField.READ_STATUS && value != null && "UNSET".equals(value.toString())) {
            return cb.isNull(expr);
        }

        if (value instanceof Number) return cb.equal(expr, value);

        return cb.equal(cb.lower(expr.as(String.class)), value.toString().toLowerCase());
    }
}

