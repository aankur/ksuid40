package com.github.ksuid40;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.StringJoiner;

import static com.github.ksuid40.Base62.base62Decode;
import static com.github.ksuid40.Base62.base62Encode;
import static com.github.ksuid40.Hex.hexEncode;

/**
 * A K-Sortable Globally Unique ID (KSUID).
 * <p>
 * Using {@link Ksuid40Generator} is the recommended way to create Ksuid objects.
 * <p>
 * See <a href="https://github.com/segmentio/ksuid">https://github.com/segmentio/ksuid</a>.
 */
@SuppressWarnings("WeakerAccess")
public class Ksuid40 implements Serializable, Comparable<Ksuid40> {
    static final int EPOCH = 0;
    public static final int PAYLOAD_BYTES = 16;
    public static final int LONG_SIZE_BYTES = 8;

    private static final int TIMESTAMP_BYTES = 5;
    public static final int TOTAL_BYTES = TIMESTAMP_BYTES + PAYLOAD_BYTES;
    private static final int PAD_TO_LENGTH = 29;
    private static final Comparator<Ksuid40> COMPARATOR = Comparator.comparingLong(Ksuid40::getTimestamp)
                                                                  .thenComparing(Ksuid40::getPayload);

    private final long timestamp;
    private final byte[] payload;
    private final byte[] ksuidBytes;

    private Ksuid40(final Builder builder) {
        if (builder.ksuidBytes != null) {
            if (builder.ksuidBytes.length < (PAYLOAD_BYTES + 4) ||  builder.ksuidBytes.length > TOTAL_BYTES) {
                throw new IllegalArgumentException("ksuid is not expected length of " + TOTAL_BYTES + " ( 20-21 ) bytes");
            }
            ksuidBytes = new byte[TOTAL_BYTES];
            System.arraycopy(builder.ksuidBytes, 0, ksuidBytes, TOTAL_BYTES - builder.ksuidBytes.length, builder.ksuidBytes.length);
            final ByteBuffer byteBuffer = ByteBuffer.wrap(ksuidBytes);
            byte[] timeStampBytes = new byte[LONG_SIZE_BYTES];
            byteBuffer.get(timeStampBytes, LONG_SIZE_BYTES - TIMESTAMP_BYTES, TIMESTAMP_BYTES);
            timestamp = ByteBuffer.wrap(timeStampBytes, 0, LONG_SIZE_BYTES).getLong();
            payload = new byte[PAYLOAD_BYTES];
            byteBuffer.get(payload);
        } else {
            if (builder.payload.length != PAYLOAD_BYTES) {
                throw new IllegalArgumentException("payload is not expected length of " + PAYLOAD_BYTES + " bytes");
            }

            timestamp = builder.timestamp;

            byte[] timeStampBytes = new byte[LONG_SIZE_BYTES];
            ByteBuffer.wrap(timeStampBytes).putLong(timestamp);

            payload = builder.payload;
            ksuidBytes = ByteBuffer.allocate(TOTAL_BYTES)
                                   .put(timeStampBytes, LONG_SIZE_BYTES - TIMESTAMP_BYTES, TIMESTAMP_BYTES)
                                   .put(payload)
                                   .array();
        }
    }

    /**
     * A builder to create a {@link Ksuid40}.
     *
     * @return builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Static factory to retrieve a new Ksuid.
     *
     * The {@code Ksuid} is generated using a cryptographically strong pseudo
     * random number generator.
     *
     * @return  A randomly generated {@code Ksuid}
     */
    public static Ksuid40 newKsuid() {
        return Ksuid40Generator.createKsuid();
    }
    
    /**
     * Creates a {@code Ksuid} from the string standard representation as
     * described in the {@link #toString} method.
     * 
     * @param  ksuidString
     *         A string that specifies a {@code Ksuid}
     *
     * @return  A {@code Ksuid} with the specified value
     */
    public static Ksuid40 fromString(final String ksuidString) {
        return new Builder()
                .withKsuidString(ksuidString)
                .build();
    }

    /**
     * Get the KSUID as a byte array.
     *
     * @return KSUID bytes
     */
    public byte[] asBytes() {
        return Arrays.copyOf(ksuidBytes, ksuidBytes.length);
    }

    /**
     * Returns a {@code String} object representing this {@code Ksuid}. <code>0ujtsYcgvSTl8PAuAdqWYSMnLOv</code>
     *
     * @return  A string representation of this {@code Ksuid}
     * @deprecated Use {@link #toString()}. Retained for backward-compatibility
     */
    @Deprecated
    public String asString() {
        return toString();
    }

    /**
     * Get the KSUID as a hex string. e.g. <code>0669F7EFB5A1CD34B5F99D1154FB6853345C9735</code>
     *
     * @return KSUID hex string
     */
    public String asRaw() {
        return hexEncode(ksuidBytes);
    }

    /**
     * Get the KSUID time component as an Instant.
     *
     * @return an Instant
     */
    public Instant getInstant() {
        return Instant.ofEpochSecond((long) timestamp + EPOCH);
    }

    /**
     * Get the KSUID time component in the system default timezone. e.g. <code>2017-10-09 21:00:47 -0700 PDT</code>
     *
     * @return KSUID time component string
     */
    public String getTime() {
        return getTime(ZoneId.systemDefault());
    }

    /**
     * Get the KSUID time component in the provided timezone. e.g. <code>2017-10-10 04:00:47 +0000 UTC</code>
     *
     * @param zoneId the timezone
     * @return KSUID time component string
     */
    public String getTime(final ZoneId zoneId) {
        return String.format("%1$tF %1$tT %1$tz %1$tZ", getInstant().atZone(zoneId));
    }

    /**
     * Get the KSUID timestamp component.
     *
     * @return KSUID timestamp component
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the KSUID payload component. e.g. <code>B5A1CD34B5F99D1154FB6853345C9735</code>
     *
     * @return KSUID payload component
     */
    public String getPayload() {
        return hexEncode(payload);
    }

    /**
     * Get the KSUID inspect formatting string representation. e.g.
     * <pre>
     *  REPRESENTATION:
     *  
     *    String: 0ujtsYcgvSTl8PAuAdqWYSMnLOv
     *       Raw: 0669F7EFB5A1CD34B5F99D1154FB6853345C9735
     *  
     *  COMPONENTS:
     *  
     *         Time: 2017-10-09 21:00:47 -0700 PDT
     *    Timestamp: 107608047
     *      Payload: B5A1CD34B5F99D1154FB6853345C9735
     * </pre>
     *
     * @return KSUID inspect formatting string
     */
    public String toInspectString() {
        return String.format("REPRESENTATION:%n%n  String: %1$s%n     Raw: %2$s%n%nCOMPONENTS:%n%n       Time: %3$s%n  Timestamp: %4$d%n    Payload: %5$s%n",
                             toString(), asRaw(), getTime(), getTimestamp(), getPayload());
    }

    /**
     * Get string representation suitable for logging. e.g.
     * <pre>
     * Ksuid[asString = 0ujtsYcgvSTl8PAuAdqWYSMnLOv, timestamp = 107608047, payload = [-75, ...], ksuidBytes = [6, ...]]
     * </pre>
     * 
     * @return KSUID log string
     * @see #toString()
     */
    public String toLogString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("string = " + toString())
                .add("timestamp = " + timestamp)
                .add("payload = " + Arrays.toString(payload))
                .add("ksuidBytes = " + Arrays.toString(ksuidBytes))
                .toString();
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ksuid40)) {
            return false;
        }

        final Ksuid40 that = (Ksuid40) o;

        return Objects.equals(this.timestamp, that.timestamp) &&
                Arrays.equals(this.payload, that.payload) &&
                Arrays.equals(this.ksuidBytes, that.ksuidBytes);
    }

    @Override
    public final int hashCode() {
        int result = Objects.hash(timestamp);
        result = 31 * result + Arrays.hashCode(payload);
        result = 31 * result + Arrays.hashCode(ksuidBytes);
        return result;
    }

    /**
     * Returns a {@code String} object representing this {@code Ksuid}. <code>0ujtsYcgvSTl8PAuAdqWYSMnLOv</code>
     *
     * @return  A string representation of this {@code Ksuid}
     */
    @Override
    public String toString() {
        return base62Encode(ksuidBytes, PAD_TO_LENGTH);
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") final Ksuid40 other) {
        return COMPARATOR.compare(this, other);
    }


    /**
     * Builder to create a {@link Ksuid40}.
     * <p>
     * Get a Builder using {@link Ksuid40#newBuilder() Ksuid.newBuilder()}.
     */
    public static final class Builder {
        private long timestamp;
        private byte[] payload;
        private byte[] ksuidBytes;

        private Builder() {
        }

        /**
         * Specify the timestamp component of a KSUID.
         * <p>
         * The payload must also be specified before {@link #build()} is called.
         *
         * @param timestamp the timestamp component
         * @return this builder
         */
        public Builder withTimestamp(final long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Specify the payload component of a KSUID.
         * <p>
         * The timestamp must also be specified before {@link #build()} is called.
         *
         * @param payload the payload component
         * @return this builder
         */
        public Builder withPayload(final byte[] payload) {
            this.payload = payload;
            return this;
        }

        /**
         * Specify the KSUID bytes of the form returned by {@link #asBytes()}.
         * <p>
         * The values for payload and timestamp will be ignored when {@link #build()} is called.
         *
         * @param ksuidBytes the KSUID bytes
         * @return this builder
         */
        public Builder withKsuidBytes(final byte[] ksuidBytes) {
            this.ksuidBytes = ksuidBytes;
            return this;
        }

        /**
         * Specify the KSUID string of the form returned by {@link #asString()}.
         * <p>
         * The values for payload and timestamp will be ignored when {@link #build()} is called.
         *
         * @param ksuidString the KSUID string
         * @return this builder
         */
        public Builder withKsuidString(final String ksuidString) {
            this.ksuidBytes = base62Decode(ksuidString);
            return this;
        }

        /**
         * Build a {@link Ksuid40} instance.
         * <p>
         * You must have specified either KSUID bytes, KSUID string, or a timestamp and payload.
         *
         * @return a new {@link Ksuid40} instance
         */
        public Ksuid40 build() {
            return new Ksuid40(this);
        }
    }

}
