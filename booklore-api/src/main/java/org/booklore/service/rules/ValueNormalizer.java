package org.booklore.service.rules;

import org.booklore.model.dto.RuleField;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ValueNormalizer {

    public Object normalize(Object value, RuleField field) {
        if (value == null) return null;

        // Датите ВРЪЩАМЕ като LocalDateTime (не Instant), за да е консистентно с comparisons
        if (field == RuleField.PUBLISHED_DATE || field == RuleField.DATE_FINISHED || field == RuleField.LAST_READ_TIME) {
            return parseDateTime(value);
        }

        if (field == RuleField.READ_STATUS) return value.toString();

        if (value instanceof Number) return value;

        return value.toString();
    }

    public LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime dt) return dt;

        try {
            return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDate.parse(value.toString()).atStartOfDay();
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
