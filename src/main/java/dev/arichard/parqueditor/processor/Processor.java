package dev.arichard.parqueditor.processor;

public interface Processor<T, S> {
    void processLine(T line);
    S getProcessedValue();
}
