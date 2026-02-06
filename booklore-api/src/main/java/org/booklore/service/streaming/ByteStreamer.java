package org.booklore.service.streaming;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public interface ByteStreamer {
    void stream(Path path, long start, long end, OutputStream out) throws IOException;
}

