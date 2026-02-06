package org.booklore.service.rules;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class RuleTextUtils {

    public List<String> toStringList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof List) {
            return ((Collection<?>) value).stream().map(Object::toString).toList();
        }
        return List.of(value.toString());
    }

    public String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}

