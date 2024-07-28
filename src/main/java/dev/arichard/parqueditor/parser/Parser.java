package dev.arichard.parqueditor.parser;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface Parser<O> {
    List<O> parse(File file);
    void streamParse(File file, Consumer<O> linerConsumer);
}
