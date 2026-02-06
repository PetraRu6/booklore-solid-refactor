@Slf4j
@Service
@AllArgsConstructor
public class BookRuleEvaluatorService {

    private final ObjectMapper objectMapper;
    private final RulePredicateFactory predicateFactory;

    public Specification<BookEntity> toSpecification(GroupRule groupRule, Long userId) {
        return (root, query, cb) -> {
            Join<BookEntity, UserBookProgressEntity> progressJoin = root.join("userBookProgress", JoinType.LEFT);

            Predicate userPredicate = cb.or(
                    cb.isNull(progressJoin.get("user").get("id")),
                    cb.equal(progressJoin.get("user").get("id"), userId)
            );

            Predicate rulePredicate = buildGroupPredicate(groupRule, cb, root, progressJoin);

            return cb.and(userPredicate, rulePredicate);
        };
    }

    private Predicate buildGroupPredicate(GroupRule group, CriteriaBuilder cb, Root<BookEntity> root,
                                         Join<BookEntity, UserBookProgressEntity> progressJoin) {
        if (group.getRules() == null || group.getRules().isEmpty()) return cb.conjunction();

        List<Predicate> predicates = new ArrayList<>();
        RuleCriteriaContext ctx = new RuleCriteriaContext(cb, root, progressJoin);

        for (Object ruleObj : group.getRules()) {
            if (ruleObj == null) continue;

            Map<String, Object> ruleMap = objectMapper.convertValue(ruleObj, new TypeReference<>() {});
            String type = (String) ruleMap.get("type");

            if ("group".equals(type)) {
                GroupRule subGroup = objectMapper.convertValue(ruleObj, GroupRule.class);
                predicates.add(buildGroupPredicate(subGroup, cb, root, progressJoin));
            } else {
                try {
                    Rule rule = objectMapper.convertValue(ruleObj, Rule.class);
                    predicates.add(predicateFactory.build(rule, ctx));
                } catch (Exception e) {
                    log.error("Failed to parse rule: {}, error: {}", ruleObj, e.getMessage(), e);
                }
            }
        }

        if (predicates.isEmpty()) return cb.conjunction();

        return group.getJoin() == org.booklore.model.dto.JoinType.AND
                ? cb.and(predicates.toArray(new Predicate[0]))
                : cb.or(predicates.toArray(new Predicate[0]));
    }
}
