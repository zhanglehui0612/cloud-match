package com.cloud.match.store.codec;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import java.io.IOException;

public class AvroDeserializer<T extends SpecificRecord> {
    private final Schema schema;
    private final Class<T> clazz;

    public AvroDeserializer(Schema schema, Class<T> clazz) {
        this.schema = schema;
        this.clazz = clazz;
    }

    public T deserialize(byte[] data) throws IOException {
        DatumReader<T> reader = new SpecificDatumReader<>(schema);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }
}