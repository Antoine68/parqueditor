package dev.arichard.parqueditor.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData.Record;

import dev.arichard.parqueditor.adapter.FieldAdapter;
import dev.arichard.parqueditor.util.ParquetUtil;
import javafx.beans.property.StringProperty;

public class RecordsProcessor implements Processor<Map<FieldAdapter, StringProperty>, List<Record>> {
    
    private List<Record> records = new ArrayList<>();
    
    private Schema schema;
    
    public RecordsProcessor(Schema schema) {
        this.schema = schema;
    }

    @Override
    public void processLine(Map<FieldAdapter, StringProperty> line) {
        Record record = new Record(schema);
        for (Map.Entry<FieldAdapter, StringProperty> entry: line.entrySet() ) {
            Field field = schema.getField(entry.getKey().getName());
            if (field != null) {
                record.put(entry.getKey().getName(), ParquetUtil.format(entry.getValue().getValue(), ParquetUtil.getEffectiveType(field.schema())));
            }
        }
        records.add(record);
        
    }

    @Override
    public List<Record> getProcessedValue() {
        return records;
    }

}
