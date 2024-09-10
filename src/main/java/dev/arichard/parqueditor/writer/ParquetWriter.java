package dev.arichard.parqueditor.writer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.util.HadoopOutputFile;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.hadoop.ParquetFileWriter;

public class ParquetWriter implements Writer {
    
    private Schema schema;
    private List<Record> records;
    
    public ParquetWriter(Schema schema, List<Record> records) {
        this.schema = schema;
        this.records = records;
    }

    public void write(File file) {
        Path path = new Path(file.getAbsolutePath());
        Configuration config = new Configuration();
        OutputFile outputFile;
        try {
            outputFile = HadoopOutputFile.fromPath(path, config);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try (org.apache.parquet.hadoop.ParquetWriter<GenericData.Record> writer = AvroParquetWriter
                .<GenericData.Record>builder(outputFile)
                .withSchema(schema)
                .withConf(config)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build()) {
            records.forEach(r -> {
                try {
                    writer.write(r);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
