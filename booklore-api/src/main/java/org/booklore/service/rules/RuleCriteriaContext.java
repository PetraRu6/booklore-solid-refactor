package org.booklore.service.rules;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.booklore.model.entity.BookEntity;
import org.booklore.model.entity.UserBookProgressEntity;

@Getter
@AllArgsConstructor
public class RuleCriteriaContext {
    private final CriteriaBuilder cb;
    private final Root<BookEntity> root;
    private final Join<BookEntity, UserBookProgressEntity> progressJoin;
}

