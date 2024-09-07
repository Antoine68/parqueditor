package dev.arichard.parqueditor.processor;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.FieldTypeBuilder;
import org.apache.avro.SchemaBuilder.RecordBuilder;
import org.apache.avro.SchemaBuilder.TypeBuilder;

import dev.arichard.parqueditor.adapter.FieldAdapter;

public class ParquetSchemaProcessor implements Processor<FieldAdapter, Schema>{
    
    private final RecordBuilder<Schema> builder;
    private final FieldAssembler<Schema> fields;
    
    public ParquetSchemaProcessor(String name, String namespace) {
        builder = SchemaBuilder.record(name).namespace(namespace);
        fields = builder.fields();
    }

    @Override
    public void processLine(FieldAdapter field) {
        createType(field, fields.name(field.getName()).type());
    }

    @Override
    public Schema getProcessedValue() {
        return fields.endRecord();
    }
    
    private void createType(FieldAdapter field, FieldTypeBuilder<Schema> typeBuilder) {
       manageType(field.getType(), field.getNullable(), field.getDefaultValue(), typeBuilder);
    }
    
    private void manageType(Schema.Type type, boolean nullable, String defaultValue, FieldTypeBuilder<Schema> typeBuilder) {
        if (nullable) {
            typeBuilder.nullable();
        }
        switch (type) {
        case BOOLEAN:
            if (defaultValue != null) {
                typeBuilder.booleanType().booleanDefault(Boolean.parseBoolean(defaultValue));
            } else {
                typeBuilder.booleanType().noDefault();
            }
            break;
        case STRING:
            if (defaultValue != null) {
                typeBuilder.stringType().stringDefault(defaultValue);
            } else {
                typeBuilder.stringType().noDefault();
            }
            break;
        case INT:
            if (defaultValue != null) {
                typeBuilder.intType().intDefault(Integer.parseInt(defaultValue));
            } else {
                typeBuilder.intType().noDefault();
            }
            break;
        case DOUBLE:
            if (defaultValue != null) {
                typeBuilder.doubleType().doubleDefault(Double.parseDouble(defaultValue));
            } else {
                typeBuilder.doubleType().noDefault();
            }
            break;
        case FLOAT:
            if (defaultValue != null) {
                typeBuilder.floatType().floatDefault(Float.parseFloat(defaultValue));
            } else {
                typeBuilder.floatType().noDefault();
            }
            break;
        case LONG:
            if (defaultValue != null) {
                typeBuilder.longType().longDefault(Long.parseLong(defaultValue));
            } else {
                typeBuilder.longType().noDefault();
            }
            break;
        case BYTES:
            typeBuilder.bytesType().noDefault();
            break;
        default:
            break;
        }
    }
   

}
