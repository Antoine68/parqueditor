package dev.arichard.parqueditor.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import dev.arichard.parqueditor.adapter.FieldAdapter;
import dev.arichard.parqueditor.adapter.ParquetFileAdapter;
import dev.arichard.parqueditor.util.ParquetUtil;
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
            line.put(field, new SimpleStringProperty(ParquetUtil.toString(gr.get(field.getName()))));
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
        Optional<Schema.Type> optional = field.schema().getTypes().stream().map(Schema::getType).filter(type -> !type.equals(Schema.Type.NULL)).findFirst();
        return optional.orElse(Schema.Type.STRING);
    }

    private boolean isNullable(Schema.Field field) {
        if (!field.schema().getType().equals(Schema.Type.UNION)) {
            return field.schema().getType().equals(Schema.Type.NULL);
        }
        return field.schema().getTypes().stream().anyMatch(s -> s.getType().equals(Schema.Type.NULL));
    }

}
