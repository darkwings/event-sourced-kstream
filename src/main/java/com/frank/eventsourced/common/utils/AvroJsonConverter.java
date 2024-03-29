package com.frank.eventsourced.common.utils;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * The Class AvroJsonConverter is used to convert {#link org.apache.avro.specific.SpecificRecordBase
 * specific Avro records} to/from json. NOT Thread safe.
 *
 * @param <T> the generic type that extends SpecificRecordBase
 */
public class AvroJsonConverter<T extends SpecificRecord> {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String SCHEMA_FIELD_NAME = "SCHEMA$";

    Schema schema;
    SpecificDatumReader<T> avroReader;
    SpecificDatumWriter<T> avroWriter;
    JsonDecoder jsonDecoder;
    JsonEncoder jsonEncoder;

    public static AvroJsonConverter fromType(String className) {

        try {
            Class klass = Class.forName(className);
            Field f = klass.getField(SCHEMA_FIELD_NAME);
            Schema schema = (Schema) f.get(null);
            return new AvroJsonConverter(schema, klass);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T extends SpecificRecordBase> AvroJsonConverter<T> fromClass(Class<T> klass) {

        try {
            Field f = klass.getField(SCHEMA_FIELD_NAME);
            Schema schema = (Schema) f.get(null);
            return new AvroJsonConverter<>(schema, klass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Instantiates a new Avro json converter based on class.
     *
     * @param schema             the schema
     * @param typeParameterClass the type parameter class
     */
    private AvroJsonConverter(Schema schema, Class<T> typeParameterClass) {
        super();
        this.schema = schema;
        avroReader = new SpecificDatumReader<T>(typeParameterClass);
        avroWriter = new SpecificDatumWriter<T>(typeParameterClass);
    }

    /**
     * Decode json data.
     *
     * @param data the data
     * @return the decoded object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decodeJson(String data) throws IOException {
        return decodeJson(data, null);
    }

    /**
     * Decode json data.
     *
     * @param data the data
     * @return the decoded object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decodeJson(byte[] data) throws IOException {
        return decodeJson(new String(data, UTF8), null);
    }

    /**
     * Decode json data.
     *
     * @param data  the data
     * @param reuse the reuse
     * @return the decoded object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decodeJson(String data, T reuse) throws IOException {
        jsonDecoder = DecoderFactory.get().jsonDecoder(this.schema, data);
        return avroReader.read(null, jsonDecoder);
    }

    /**
     * Encode record to Json String.
     *
     * @param record the object to encode
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String encodeToJson(T record) throws IOException {
        return new String(encodeToJsonBytes(record), UTF8);
    }

    /**
     * Encode record to Json and then convert to byte array.
     *
     * @param record the object to encode
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public byte[] encodeToJsonBytes(T record) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jsonEncoder = EncoderFactory.get().jsonEncoder(this.schema, baos, true);
        avroWriter.write(record, jsonEncoder);
        jsonEncoder.flush();
        baos.flush();
        return baos.toByteArray();
    }

}
