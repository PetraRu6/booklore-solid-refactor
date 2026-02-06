package org.booklore.service.rules;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.booklore.model.dto.RuleField;
import org.booklore.model.entity.BookEntity;
import org.booklore.model.entity.UserBookProgressEntity;
import org.springframework.stereotype.Component;

@Component
public class FieldExpressionResolver {

    public Expression<?> resolve(RuleField field, CriteriaBuilder cb, Root<BookEntity> root,
                                 Join<BookEntity, UserBookProgressEntity> progressJoin) {

        return switch (field) {
            case LIBRARY -> root.get("library").get("id");
            case READ_STATUS -> progressJoin.get("readStatus");
            case DATE_FINISHED -> progressJoin.get("dateFinished");
            case LAST_READ_TIME -> progressJoin.get("lastReadTime");
            case PERSONAL_RATING -> progressJoin.get("personalRating");
            case FILE_SIZE -> root.get("fileSizeKb");
            case METADATA_SCORE -> root.get("metadataMatchScore");
            case TITLE -> root.get("metadata").get("title");
            case SUBTITLE -> root.get("metadata").get("subtitle");
            case PUBLISHER -> root.get("metadata").get("publisher");
            case PUBLISHED_DATE -> root.get("metadata").get("publishedDate");
            case PAGE_COUNT -> root.get("metadata").get("pageCount");
            case LANGUAGE -> root.get("metadata").get("language");
            case SERIES_NAME -> root.get("metadata").get("seriesName");
            case SERIES_NUMBER -> root.get("metadata").get("seriesNumber");
            case SERIES_TOTAL -> root.get("metadata").get("seriesTotal");
            case ISBN13 -> root.get("metadata").get("isbn13");
            case ISBN10 -> root.get("metadata").get("isbn10");
            case AMAZON_RATING -> root.get("metadata").get("amazonRating");
            case AMAZON_REVIEW_COUNT -> root.get("metadata").get("amazonReviewCount");
            case GOODREADS_RATING -> root.get("metadata").get("goodreadsRating");
            case GOODREADS_REVIEW_COUNT -> root.get("metadata").get("goodreadsReviewCount");
            case HARDCOVER_RATING -> root.get("metadata").get("hardcoverRating");
            case HARDCOVER_REVIEW_COUNT -> root.get("metadata").get("hardcoverReviewCount");
            case RANOBEDB_RATING -> root.get("metadata").get("ranobedbRating");
            case FILE_TYPE -> cb.function("SUBSTRING_INDEX", String.class,
                    root.get("fileName"), cb.literal("."), cb.literal(-1));
            case SHELF, AUTHORS, CATEGORIES, MOODS, TAGS, GENRE -> null; // join fields – отделно
            default -> null;
        };
    }

    public Join<?, ?> createArrayJoin(RuleField field, Root<BookEntity> root) {
        if (field == RuleField.SHELF) return root.join("shelves", JoinType.INNER);

        var metadataJoin = root.join("metadata", JoinType.INNER);
        return switch (field) {
            case AUTHORS -> metadataJoin.join("authors", JoinType.INNER);
            case CATEGORIES -> metadataJoin.join("categories", JoinType.INNER);
            case MOODS -> metadataJoin.join("moods", JoinType.INNER);
            case TAGS -> metadataJoin.join("tags", JoinType.INNER);
            case GENRE -> metadataJoin.join("categories", JoinType.INNER);
            default -> throw new IllegalArgumentException("Not an array field: " + field);
        };
    }

    public Expression<String> arrayNameExpr(RuleField field, Join<?, ?> join) {
        return field == RuleField.SHELF ? join.get("id").as(String.class) : join.get("name");
    }

    public boolean isArrayField(RuleField field) {
        return field == RuleField.AUTHORS || field == RuleField.CATEGORIES ||
               field == RuleField.MOODS || field == RuleField.TAGS ||
               field == RuleField.GENRE || field == RuleField.SHELF;
    }
}

