package dev.arichard.parqueditor.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

/**
 * 
 */
public class ParquetParser implements Parser<List<GenericRecord>> {
	
	public List<GenericRecord> parse(File file) {
		List<GenericRecord> result = new ArrayList<>();
		try (ParquetReader<Object> parquetReader = AvroParquetReader.builder(new LocalInputFile(file.toPath())).build()) {
			GenericRecord record = (GenericRecord) parquetReader.read();
			while(record != null) {
				result.add(record);
				record = (GenericRecord) parquetReader.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
