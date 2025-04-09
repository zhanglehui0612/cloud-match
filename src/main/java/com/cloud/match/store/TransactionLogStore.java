package com.cloud.match.store;

import com.cloud.match.avro.TransactionLog;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionLogStore {
    private static final String LOG_FILE_PATH = "transaction_logs/";
    private final Schema schema;

    public TransactionLogStore() {
        try {
            this.schema = new Schema.Parser().parse(new File("TransactionLog.avsc"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 这里的逻辑写入失败，应该重试几次
    public boolean append(TransactionLog log) {
        try {
            File file = new File(LOG_FILE_PATH + log.getSymbol() + "_" + log.getId() + ".dat");
            DatumWriter<TransactionLog> datumWriter = new SpecificDatumWriter<>(TransactionLog.class);
            DataFileWriter<TransactionLog> dataFileWriter = new DataFileWriter<>(datumWriter);
            dataFileWriter.create(log.getSchema(), file);
            dataFileWriter.append(log);
            dataFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }




    public List<TransactionLog> queryAfterOffset(String symbol, long targetOffset) throws IOException {
        List<TransactionLog> result = new ArrayList<>();
        File file = getLogFile(symbol);
        if (!file.exists()) return result;

        DatumReader<TransactionLog> reader = new SpecificDatumReader<>(TransactionLog.class);
        try (DataFileReader<TransactionLog> dataFileReader = new DataFileReader<>(file, reader)) {
            while (dataFileReader.hasNext()) {
                TransactionLog log = dataFileReader.next();
                if (log.getOffset() > targetOffset) {
                    result.add(log);
                }
            }
        }
        return result;
    }


    private File getLogFile(String symbol) {
        return new File(LOG_FILE_PATH + symbol + ".dat");
    }
}