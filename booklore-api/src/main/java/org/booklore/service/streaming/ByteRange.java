package org.booklore.service.streaming;

public record ByteRange(long start, long end) {
    public long length() {
        return end - start + 1;
    }
}

