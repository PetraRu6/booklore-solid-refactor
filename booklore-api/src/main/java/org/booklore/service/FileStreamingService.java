package org.booklore.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.booklore.service.streaming.ByteRange;
import org.booklore.service.streaming.ByteStreamer;
import org.booklore.service.streaming.ClientDisconnectDetector;
import org.booklore.service.streaming.RangeParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStreamingService {

    private final RangeParser rangeParser;
    private final ByteStreamer byteStreamer;
    private final ClientDisconnectDetector disconnectDetector;

    public void streamWithRangeSupport(
            Path filePath,
            String contentType,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        long fileSize = Files.size(filePath);
        String rangeHeader = request.getHeader("Range");

        applyCommonHeaders(response, contentType);

        // HEAD
        if ("HEAD".equalsIgnoreCase(request.getMethod())) {
            writeHead(response, fileSize);
            return;
        }

        try {
            // NO RANGE
            if (rangeHeader == null) {
                writeFullContent(response, fileSize);
                byteStreamer.stream(filePath, 0, fileSize - 1, response.getOutputStream());
                return;
            }

            // RANGE
            ByteRange range = rangeParser.parse(rangeHeader, fileSize);
            if (range == null) {
                writeUnsatisfiable(response, fileSize);
                return;
            }

            writePartialContent(response, range, fileSize);
            byteStreamer.stream(filePath, range.start(), range.end(), response.getOutputStream());

        } catch (IOException e) {
            if (disconnectDetector.isClientDisconnect(e)) {
                log.debug("Client disconnected during streaming: {}", e.getMessage());
                return;
            }
            throw e;
        }
    }

    private static void applyCommonHeaders(HttpServletResponse response, String contentType) {
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Content-Disposition", "inline");
        response.setContentType(contentType);
    }

    private static void writeHead(HttpServletResponse response, long fileSize) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentLengthLong(fileSize);
    }

    private static void writeFullContent(HttpServletResponse response, long fileSize) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentLengthLong(fileSize);
    }

    private static void writePartialContent(HttpServletResponse response, ByteRange range, long fileSize) {
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Range", "bytes " + range.start() + "-" + range.end() + "/" + fileSize);
        response.setContentLengthLong(range.length());
    }

    private static void writeUnsatisfiable(HttpServletResponse response, long fileSize) {
        response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
        response.setHeader("Content-Range", "bytes */" + fileSize);
    }
}