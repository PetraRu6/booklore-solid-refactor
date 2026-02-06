package org.booklore.service.streaming;

import org.springframework.stereotype.Component;

@Component
public class Rfc7233RangeParser implements RangeParser {

    @Override
    public ByteRange parse(String header, long size) {
        if (header == null || !header.startsWith("bytes=")) return null;

        String value = header.substring(6).trim();
        String[] parts = value.split(",", 2); // single range only
        String range = parts[0].trim();

        int dash = range.indexOf('-');
        if (dash < 0) return null;

        try {
            // suffix-byte-range-spec: "-<length>"
            if (dash == 0) {
                long suffix = Long.parseLong(range.substring(1));
                if (suffix <= 0) return null;
                suffix = Math.min(suffix, size);
                return new ByteRange(size - suffix, size - 1);
            }

            long start = Long.parseLong(range.substring(0, dash));

            // open-ended: "<start>-"
            if (dash == range.length() - 1) {
                if (start >= size) return null;
                return new ByteRange(start, size - 1);
            }

            // "<start>-<end>"
            long end = Long.parseLong(range.substring(dash + 1));
            if (start > end || start >= size) return null;

            end = Math.min(end, size - 1);
            return new ByteRange(start, end);

        } catch (NumberFormatException e) {
            return null;
        }
    }
}

