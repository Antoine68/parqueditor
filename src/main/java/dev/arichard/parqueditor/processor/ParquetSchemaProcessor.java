package dev.arichard.parqueditor.processor;

import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.BaseFieldTypeBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.FieldTypeBuilder;
import org.apache.avro.SchemaBuilder.RecordBuilder;

import dev.arichard.parqueditor.adapter.FieldAdapter;
import dev.arichard.parqueditor.util.ParquetUtil;

public class ParquetSchemaProcessor implements Processor<FieldAdapter, Schema>{

    private final FieldAssembler<Schema> fields;
    
    public ParquetSchemaProcessor(String name, String namespace) {
        RecordBuilder<Schema> builder = SchemaBuilder.record(name).namespace(namespace);
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
    
    private void manageType(Schema.Type type, boolean nullable, String defaultValue, FieldTypeBuilder<Schema> baseBuilder) {
        BaseFieldTypeBuilder<Schema> typeBuilder = nullable ? baseBuilder.nullable() : (BaseFieldTypeBuilder<Schema>) baseBuilder;
        Object formatedDefaultValue =  ParquetUtil.format(defaultValue, type);
        switch (type) {
        case BOOLEAN:
            if (defaultValue != null) {
                typeBuilder.booleanType().booleanDefault((boolean) formatedDefaultValue);
            } else {
                typeBuilder.booleanType().noDefault();
            }
            break;
        case STRING:
            if (defaultValue != null) {
                typeBuilder.stringType().stringDefault((String) formatedDefaultValue);
            } else {
                typeBuilder.stringType().noDefault();
            }
            break;
        case INT:
            if (defaultValue != null) {
                typeBuilder.intType().intDefault((int) formatedDefaultValue);
            } else {
                typeBuilder.intType().noDefault();
            }
            break;
        case DOUBLE:
            if (defaultValue != null) {
                typeBuilder.doubleType().doubleDefault((double) formatedDefaultValue);
            } else {
                typeBuilder.doubleType().noDefault();
            }
            break;
        case FLOAT:
            if (defaultValue != null) {
                typeBuilder.floatType().floatDefault((float) formatedDefaultValue);
            } else {
                typeBuilder.floatType().noDefault();
            }
            break;
        case LONG:
            if (defaultValue != null) {
                typeBuilder.longType().longDefault((long) formatedDefaultValue);
            } else {
                typeBuilder.longType().noDefault();
            }
            break;
        case BYTES:
            if (defaultValue != null) {
                typeBuilder.bytesType().bytesDefault((ByteBuffer) formatedDefaultValue);
            } else {
                typeBuilder.bytesType().noDefault();
            }
            break;
        default:
            break;
        }
    }
   

}
