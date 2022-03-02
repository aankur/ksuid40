package com.github.ksuid40;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Stream;

import static com.github.ksuid40.Base62.*;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Base62Test {


    public static final Entry<byte[], String> P1 = new SimpleEntry<>(Hex.hexDecode("0E9110E816D1D7403226FA924557DA9B3A0F4642"), "24rUCafWbTglyvWlQEuaxKqqiuY");
    public static final Entry<byte[], String> P2 = new SimpleEntry<>(Hex.hexDecode("0E9110E8C3764DCE8A9C539F5F0D1BE0FCD49E8C"), "24rUCfvIRZ0PqLTVlmt7bCVHnCu");
    public static final Entry<byte[], String> P3 = new SimpleEntry<>(Hex.hexDecode("0E9110E890F44EB7DAF786297B276912EB478BAE"), "24rUCeNzQ1KoETEDtwGE1wdazYk");

    private static Stream<Arguments> rawByteProvider() {
        return Stream.of(
          Arguments.of(P1),
          Arguments.of(P2),
          Arguments.of(P3)
        );
    }

    @Test
    public void utilityClass() {
        assertThrows(InvocationTargetException.class, () -> {
            final Constructor<Base62> constructor = Base62.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    public void base() {
        assertThat(BASE).isEqualTo(BigInteger.valueOf(62));
    }

    @Test
    public void characterSet() {
        assertThat(BASE_62_CHARACTERS).isEqualTo("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray());
    }

    @Test
    public void indexOf() {
        range(0, BASE_62_CHARACTERS.length)
                .forEach(index -> {
                    final char charAtIndex = BASE_62_CHARACTERS[index];
                    assertThat(Base62.indexOf(charAtIndex)).isEqualTo(index);
                });
    }

    @ParameterizedTest
    @MethodSource("rawByteProvider")
    public void encodeNoPadding(final Entry<byte[], String> entry) {
        final String s = base62Encode(entry.getKey());
        assertThat(s).isEqualTo(entry.getValue());
    }

    @ParameterizedTest
    @MethodSource("rawByteProvider")
    public void encodeWithPadding(final Entry<byte[], String> entry) {
        final String s = base62Encode(entry.getKey(), entry.getValue().length() + 4);
        assertThat(s).isEqualTo("0000" + entry.getValue());
    }

    @ParameterizedTest
    @MethodSource("rawByteProvider")
    public void encodeWithSameLengthPadding(final Entry<byte[], String> entry) {
        final String s = base62Encode(entry.getKey(), entry.getValue().length());
        assertThat(s).isEqualTo(entry.getValue());
    }

    @ParameterizedTest
    @MethodSource("rawByteProvider")
    public void encodeWithPaddingToSmall(final Entry<byte[], String> entry) {
        final String s = base62Encode(entry.getKey(), entry.getValue().length() - 4);
        assertThat(s).isEqualTo(entry.getValue());
    }

    @ParameterizedTest
    @MethodSource("rawByteProvider")
    public void decode(final Entry<byte[], String> entry) {
        final byte[] bytes = base62Decode(entry.getValue());
        assertThat(bytes).isEqualTo(entry.getKey());
    }

    @ParameterizedTest
    @MethodSource("rawByteProvider")
    public void decodeWithPadding(final Entry<byte[], String> entry) {
        final byte[] bytes = base62Decode("0000" + entry.getValue());
        assertThat(bytes).isEqualTo(entry.getKey());
    }

    @Test
    public void decodeWithInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> {
            base62Decode("01-AB*ab");
        });
    }

    @ParameterizedTest
    @MethodSource("rawByteProvider")
    public void encodeDecode(final Entry<byte[], String> entry) {
        assertThat(base62Decode(base62Encode(entry.getKey()))).isEqualTo(entry.getKey());
        assertThat(base62Decode(base62Encode(entry.getKey(), entry.getValue().length() + 4))).isEqualTo(entry.getKey());
    }
    
    @Test
    public void testBase62EncodeUnsigned() {
        final Random random = new Random();
        random.setSeed(123L);

        final Instant timestamp = Instant.parse("2083-01-27T08:18:32.577Z");
        final Ksuid40 ksuid40 = new Ksuid40Generator(random).newKsuid(timestamp);
        final byte[] ksuidBytes = ksuid40.asBytes();
        assertThat(Base62.base62Encode(ksuidBytes, 27)).isEqualTo("ULUyOmRbtzUsdIObKkJUTWQwB06");
    }
}
