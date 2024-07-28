package dev.arichard.parqueditor.service;

import java.io.File;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import dev.arichard.parqueditor.parser.Parser;

public abstract class AbstractFileService<T> implements FileService<T> {
    
    @Autowired
    protected Parser<T> parser;
    
    @Override
    public void consumeFile(File file, Consumer<T> lineConsumer) {
        parser.streamParse(file, lineConsumer);
    }
    
}
