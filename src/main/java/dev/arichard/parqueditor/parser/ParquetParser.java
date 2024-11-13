package dev.arichard.parqueditor.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.springframework.stereotype.Component;

/**
 * 
 */
@Component
public class ParquetParser implements Parser<GenericRecord> {

    @Override
    public List<GenericRecord> parse(File file) {
        List<GenericRecord> result = new ArrayList<>();
        streamParse(file, result::add);
        return result;
    }
    
    @Override
    public void streamParse(File file, Consumer<GenericRecord> consumer) {
        try (ParquetReader<Object> parquetReader = AvroParquetReader.builder(new LocalInputFile(file.toPath()))
                .build()) {
            GenericRecord record = (GenericRecord) parquetReader.read();
            while (record != null) {
                consumer.accept(record);
                record = (GenericRecord) parquetReader.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
