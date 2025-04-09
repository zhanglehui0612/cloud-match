package com.cloud.match.store.codec;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * avro序列化器
 * @param <T>
 */
public class AvroSerializer<T extends SpecificRecord> {
    public byte[] serialize(T data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DatumWriter<T> writer = new SpecificDatumWriter<>(data.getSchema());
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        writer.write(data, encoder);
        encoder.flush();
        return out.toByteArray();
    }
}
