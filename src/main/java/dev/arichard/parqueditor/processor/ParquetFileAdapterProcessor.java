package dev.arichard.parqueditor.processor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import dev.arichard.parqueditor.adapter.FieldAdapter;
import dev.arichard.parqueditor.adapter.ParquetFileAdapter;
import dev.arichard.parqueditor.parser.Parser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ParquetFileAdapterProcessor implements Processor<GenericRecord, ParquetFileAdapter> {
    
    private final ParquetFileAdapter adapter = new ParquetFileAdapter();
    
    private final AtomicBoolean firstLine = new AtomicBoolean(true);

    @Override
    public void processLine(GenericRecord gr) {
        if (firstLine.get()) {
            for (Schema.Field f : gr.getSchema().getFields()) {
                adapter.addField(createFieldAdapter(f));
            }
            firstLine.set(false);
        }

        Map<FieldAdapter, StringProperty> line = new HashMap<>();
        for (FieldAdapter field : adapter.getFields()) {
            Object value = gr.get(field.getName());
            line.put(field, new SimpleStringProperty(value == null ? null : value.toString()));
        }
        adapter.addLine(line);
    }
    
    @Override
    public ParquetFileAdapter getProcessedValue() {
        return adapter;
    }
    
    private FieldAdapter createFieldAdapter(Schema.Field field) {
        return new FieldAdapter(field.name(), getType(field), null, isNullable(field));
    }
    
    private Schema.Type getType(Schema.Field field) {
        if (!field.schema().getType().equals(Schema.Type.UNION)) {
            return field.schema().getType();
        }
        Optional<Schema.Type> optional = field.schema().getTypes().stream().filter(s -> !s.getType().equals(Schema.Type.NULL)).map(s -> s.getType()).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return Schema.Type.STRING;
    }

    private boolean isNullable(Schema.Field field) {
        if (!field.schema().getType().equals(Schema.Type.UNION)) {
            return field.schema().getType().equals(Schema.Type.NULL);
        }
        return field.schema().getTypes().stream().anyMatch(s -> s.getType().equals(Schema.Type.NULL));
    }

}
