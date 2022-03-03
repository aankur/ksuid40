package com.github.ksuid40;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class Ksuid40Test {
    private static final String PAYLOAD_RAW = "B5A1CD34B5F99D1154FB6853345C9735";
    private static final byte[] PAYLOAD_BYTES = Hex.hexDecode(PAYLOAD_RAW);
    private static final String KSUID_RAW = "000669F7EF" + PAYLOAD_RAW;
    private static final byte[] KSUID_BYTES = Hex.hexDecode(KSUID_RAW);
    private static final String KSUID_STRING = "000ujtsYcgvSTl8PAuAdqWYSMnLOv";
    private static final long TIMESTAMP = 107608047;
    private static final int EPOCH = 0;
    private static final Instant INSTANT = Instant.ofEpochSecond((long) TIMESTAMP + EPOCH);
    private static final String TIME = String.format("%1$tF %1$tT %1$tz %1$tZ", INSTANT.atZone(ZoneId.systemDefault()));
    private static final String TIME_UTC = String.format("%1$tF %1$tT %1$tz %1$tZ", INSTANT.atZone(ZoneId.of("UTC")));

    public static final int[] INCORRECT_SIZES = {0, 10, 25}; // anything but 16

    private static Stream<Arguments> inCorrectSizeProvider() {
        return Stream.of(
          Arguments.of(INCORRECT_SIZES[0]),
          Arguments.of(INCORRECT_SIZES[1]),
          Arguments.of(INCORRECT_SIZES[2])
        );
    }

    public static final Ksuid40[] KSUID_64s = {
            Ksuid40.newBuilder().withKsuidBytes(KSUID_BYTES).build(),
            Ksuid40.newBuilder().withKsuidString(KSUID_STRING).build(),
            Ksuid40.newBuilder().withTimestamp(TIMESTAMP).withPayload(PAYLOAD_BYTES).build()
    };

    private static Stream<Arguments> ksuidProvider() {
        return Stream.of(
          Arguments.of(KSUID_64s[0]),
          Arguments.of(KSUID_64s[1]),
          Arguments.of(KSUID_64s[2])
        );
    }


    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void asBytes(final Ksuid40 ksuid40) {
        assertThat(ksuid40.asBytes()).isEqualTo(KSUID_BYTES);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    @SuppressWarnings("deprecation")
    public void asString(final Ksuid40 ksuid40) {
        assertThat(ksuid40.asString()).isEqualTo(KSUID_STRING);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void toString(final Ksuid40 ksuid40) {
        assertThat(ksuid40.toString()).isEqualTo(KSUID_STRING);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void fromString(final Ksuid40 ksuid40) {
        final String ksuidString = ksuid40.toString();
        assertThat(Ksuid40.fromString(ksuidString)).isEqualTo(ksuid40);
    }
    
    @Test
    public void testNewKsuid() {
        assertThat(Ksuid40.newKsuid()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void asRaw(final Ksuid40 ksuid40) {
        assertThat(ksuid40.asRaw()).isEqualTo(KSUID_RAW);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void getInstant(final Ksuid40 ksuid40) {
        assertThat(ksuid40.getInstant()).isEqualTo(Instant.ofEpochSecond(TIMESTAMP + EPOCH));
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void getTime(final Ksuid40 ksuid40) {
        assertThat(ksuid40.getTime()).isEqualTo(TIME);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void getTimeInZone(final Ksuid40 ksuid40) {
        assertThat(ksuid40.getTime(ZoneId.of("UTC"))).isEqualTo(TIME_UTC);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void getTimestamp(final Ksuid40 ksuid40) {
        assertThat(ksuid40.getTimestamp()).isEqualTo(TIMESTAMP);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void getPayload(final Ksuid40 ksuid40) {
        assertThat(ksuid40.getPayload()).isEqualTo(PAYLOAD_RAW);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void toInspectString(final Ksuid40 ksuid40) {
        final String s = String.format("REPRESENTATION:%n%n  String: %1$s%n     Raw: %2$s%n%nCOMPONENTS:%n%n       Time: %3$s%n  Timestamp: %4$d%n    Payload: %5$s%n",
                                       KSUID_STRING, KSUID_RAW, TIME, TIMESTAMP, PAYLOAD_RAW);
        assertThat(ksuid40.toInspectString()).isEqualTo(s);
    }

    @ParameterizedTest
    @MethodSource("ksuidProvider")
    public void testToLogString(final Ksuid40 ksuid40) {
        assertThat(ksuid40.toLogString()).isEqualTo("Ksuid40[string = 000ujtsYcgvSTl8PAuAdqWYSMnLOv"  +
                                                       ", timestamp = " + TIMESTAMP +
                                                       ", payload = [-75, -95, -51, 52, -75, -7, -99, 17, 84, -5, 104, 83, 52, 92, -105, 53]" +
                                                       ", ksuidBytes = [0, 6, 105, -9, -17, -75, -95, -51, 52, -75, -7, -99, 17, 84, -5, 104, 83, 52, 92, -105, 53]]");
    }

    @Test
    public void equalsAndHashcode() {
        EqualsVerifier.forClass(Ksuid40.class).verify();
        assertThat(KSUID_64s[0]).isEqualTo(KSUID_64s[1]);
        assertThat(KSUID_64s[1]).isEqualTo(KSUID_64s[2]);
    }

    @Test
    public void comparableIsConsistentWithEquals() {
        assertThat(KSUID_64s[0].compareTo(KSUID_64s[1])).isEqualTo(0);
        assertThat(KSUID_64s[1].compareTo(KSUID_64s[2])).isEqualTo(0);
    }

    @Test
    public void comparable() {
        final Clock utc = Clock.systemUTC();

        final Ksuid40Generator generator = new Ksuid40Generator(new SecureRandom());
        final Ksuid40 first = generator.newKsuid(Instant.now(Clock.offset(utc, Duration.ofSeconds(2))));
        final Ksuid40 second = generator.newKsuid(Instant.now(Clock.offset(utc, Duration.ofSeconds(4))));
        final Ksuid40 third = generator.newKsuid(Instant.now(Clock.offset(utc, Duration.ofSeconds(6))));
        final Ksuid40 fourth = generator.newKsuid(Instant.now(Clock.offset(utc, Duration.ofSeconds(8))));

        final List<Ksuid40> orderedList = Arrays.asList(first, second, third, fourth);
        final List<Ksuid40> list = Arrays.asList(first, second, third, fourth);
        while (list.equals(orderedList)) {
            Collections.shuffle(list);
        }
        Collections.sort(list);
        assertThat(list).isEqualTo(orderedList);
    }

    @ParameterizedTest
    @MethodSource("inCorrectSizeProvider")
    public void constructWithIncorrectPayloadSize(final int incorrectSize) {
        assertThatCode(() -> Ksuid40.newBuilder().withTimestamp(TIMESTAMP).withPayload(new byte[incorrectSize]).build())
                .hasStackTraceContaining("payload is not expected length of 16 bytes");
    }

    @ParameterizedTest
    @MethodSource("inCorrectSizeProvider")
    public void constructWithIncorrectKsuidBytesSize(final int incorrectSize) {
        assertThatCode(() -> Ksuid40.newBuilder().withKsuidBytes(new byte[incorrectSize]).build())
                .hasStackTraceContaining("ksuid is not expected length");
    }

    @ParameterizedTest
    @MethodSource("inCorrectSizeProvider")
    public void constructWithIncorrectKsuidStringSize(final int incorrectSize) {
        assertThatCode(() -> {
            Ksuid40.newBuilder()
                 .withKsuidString(IntStream.range(0, incorrectSize).mapToObj(i -> "a").collect(joining()))
                 .build();
        }).hasStackTraceContaining("ksuid is not expected length");
    }
}
