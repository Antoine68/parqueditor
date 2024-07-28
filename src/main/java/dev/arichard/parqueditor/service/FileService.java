package dev.arichard.parqueditor.service;

import java.io.File;
import java.util.function.Consumer;

public interface FileService<T> {
    
     void consumeFile(File file, Consumer<T> lineConsumer);
     
     
    
}
