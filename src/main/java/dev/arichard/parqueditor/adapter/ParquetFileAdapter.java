package dev.arichard.parqueditor.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.beans.property.StringProperty;

public class ParquetFileAdapter {
    
    private final List<FieldAdapter> fields;
    
    private final List<Map<FieldAdapter, StringProperty>> lines;
    
    public ParquetFileAdapter() {
        this.fields = new ArrayList<>();
        this.lines = new ArrayList<>();
    }
    
    public ParquetFileAdapter(List<FieldAdapter> fields, List<Map<FieldAdapter, StringProperty>> lines) {
        this.fields = fields;
        this.lines = lines;
    }
    
    public void addField(FieldAdapter field) {
        fields.add(field);
    }
    
    public void addLine(Map<FieldAdapter, StringProperty> line) {
        lines.add(line);
    }

    public List<FieldAdapter> getFields() {
        return fields;
    }

    public List<Map<FieldAdapter, StringProperty>> getLines() {
        return lines;
    }
    
    
}
