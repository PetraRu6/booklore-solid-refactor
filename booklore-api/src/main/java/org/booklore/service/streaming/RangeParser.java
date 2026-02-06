package org.booklore.service.streaming;

public interface RangeParser {
    ByteRange parse(String rangeHeader, long fileSize);
}
