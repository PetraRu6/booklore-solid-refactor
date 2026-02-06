package org.booklore.service.telemetry;

public interface TelemetryCollector<T> {
    T collect();
}

