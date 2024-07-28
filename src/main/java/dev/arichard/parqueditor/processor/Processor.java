package dev.arichard.parqueditor.processor;

import org.apache.avro.generic.GenericRecord;

public interface Processor<T, S> {
    void processLine(T line);
    S getProcessedValue();
}
