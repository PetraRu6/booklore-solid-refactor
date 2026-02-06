package org.booklore.service.streaming;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;

@Component
public class RandomAccessFileByteStreamer implements ByteStreamer {

    private static final int BUFFER_SIZE = 64 * 1024;

    @Override
    public void stream(Path path, long start, long end, OutputStream out) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            raf.seek(start);

            long remaining = end - start + 1;
            byte[] buffer = new byte[BUFFER_SIZE];

            while (remaining > 0) {
                int read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1) break;

                out.write(buffer, 0, read);
                remaining -= read;
            }
            out.flush();
        }
    }
}

