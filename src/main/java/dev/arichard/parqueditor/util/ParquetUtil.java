package dev.arichard.parqueditor.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

public class ParquetUtil {
    
    private ParquetUtil() {
        
    }
    
    public static String toString(Object value) {
        if (value == null) return null;
        if (value instanceof ByteBuffer) {
            return StandardCharsets.UTF_8.decode((ByteBuffer) value).toString();
        }
        return value.toString();
    }
    
    public static Object format(String value, Schema.Type type) {
        switch (type) {
        case BOOLEAN:
            return Boolean.parseBoolean(value == null ? "false" : value);
        case INT:
            return Integer.parseInt(value == null ? "0" : value);
        case DOUBLE:
            return Double.parseDouble(value == null ? "0" : value);
        case FLOAT:
            return Float.parseFloat(value == null ? "0" : value);
        case LONG:
            return Long.parseLong(value == null ? "0" : value);
        case BYTES:
            if (value == null) return value;
            return ByteBuffer.wrap(value.getBytes());
        default:
            return value;
        }
    }
    
    public static Schema.Type getEffectiveType(Schema schema) {
        if(!schema.getType().equals(Schema.Type.UNION)) {
            return schema.getType();
        }
        for (Schema s: schema.getTypes()) {
            if (!s.getType().equals(Schema.Type.NULL)) {
                return s.getType();
            }
        }
        return null;
    }
}
