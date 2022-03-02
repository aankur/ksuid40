package com.github.ksuid40;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;
import java.util.function.Supplier;

import static com.github.ksuid40.Ksuid40.EPOCH;
import static com.github.ksuid40.Ksuid40.PAYLOAD_BYTES;

/**
 * Generate K-Sortable Globally Unique IDs (KSUID).
 * <p>
 * Unless otherwise noted, passing a {@code null} argument to a method of this class
 * will cause a {@link NullPointerException NullPointerException} to be thrown.
 * <p>
 * See <a href="https://github.com/segmentio/ksuid">https://github.com/segmentio/ksuid</a>.
 */
@SuppressWarnings("WeakerAccess")
public class Ksuid40Generator {
    private static final Ksuid40Generator INSTANCE = new Ksuid40Generator(new SecureRandom());
    
    private final Supplier<byte[]> payloadSupplier;

    /**
     * Generate a new KSUID-40 string representation
     * 
     * The {@code Ksuid40} is generated using a cryptographically strong pseudo
     * random number generator.
     *
     * @return string representation of new KSUID-40
     */
    public static String generate() {
        return createKsuid().toString();
    }

    static Ksuid40 createKsuid() {
        return INSTANCE.newKsuid();
    }

    /**
     * Construct a KSUID generator.
     *
     * @param random source of random bytes for payload, SecureRandom is recommended
     */
    public Ksuid40Generator(final Random random) {
        this(() -> {
            final byte[] payload = new byte[PAYLOAD_BYTES];
            random.nextBytes(payload);
            return payload;
        });
    }

    /**
     * Construct a KSUID generator.
     *
     * @param payloadSupplier supplier of byte arrays which must be {@link Ksuid40#PAYLOAD_BYTES PAYLOAD_BYTES} in length
     */
    public Ksuid40Generator(final Supplier<byte[]> payloadSupplier) {
        if (payloadSupplier.get().length != PAYLOAD_BYTES) {
            throw new IllegalArgumentException("payloadBytesSupplier must supply byte arrays of length " + PAYLOAD_BYTES);
        }
        this.payloadSupplier = payloadSupplier;
    }

    /**
     * Generate a new KSUID.
     * <p>
     * The equivalent of calling {@link #newKsuid(Instant) newKsuid(Instant.now())}.
     *
     * @return a Ksuid object
     */
    public Ksuid40 newKsuid() {
        return newKsuid(Instant.now());
    }

    /**
     * Generate a new KSUID with a timestamp component derived from an Instant.
     *
     * @param instant an Instant from which to derive the timestamp component
     * @return a Ksuid object
     */
    public Ksuid40 newKsuid(final Instant instant) {
        return Ksuid40.newBuilder()
                    .withTimestamp((instant.getEpochSecond() - EPOCH)) // 5 bytes
                    .withPayload(payloadSupplier.get()) // 16 bytes
                    .build();
    }

}
