package com.github.ksuid40;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class Ksuid40GeneratorTest {

    public static final int[] INCORRECT_SIZES = {0, 10, 25}; // anything but 16

    public static final Ksuid40Generator[] GENERATORS = {
            new Ksuid40Generator(new SecureRandom()),
            new Ksuid40Generator(() -> new byte[16])
    };

    private static Stream<Arguments> inCorrectSizeProvider() {
        return Stream.of(
          Arguments.of(INCORRECT_SIZES[0]),
          Arguments.of(INCORRECT_SIZES[1]),
          Arguments.of(INCORRECT_SIZES[2])
        );
    }


    private static Stream<Arguments> generatorProvider() {
        return Stream.of(
          Arguments.of(GENERATORS[0]),
          Arguments.of(GENERATORS[1])
        );
    }

    @ParameterizedTest
    @MethodSource("generatorProvider")
    public void newKsuid(final Ksuid40Generator generator) {
        Ksuid40 ksuid40 = generator.newKsuid();
        assertThat(ksuid40).isNotNull();
        assertThat(ksuid40.getInstant()).isBefore(now());

        final Instant instant = now();
        ksuid40 = generator.newKsuid(instant);
        assertThat(ksuid40).isNotNull();
        assertThat(ksuid40.getInstant()).isEqualTo(instant.truncatedTo(SECONDS));
    }

    @ParameterizedTest
    @MethodSource("inCorrectSizeProvider")
    public void constructWithSupplierOfIncorrectSize(final int incorrectSize) {
        assertThatCode(() -> {
            new Ksuid40Generator(() -> new byte[incorrectSize]);
        }).isExactlyInstanceOf(IllegalArgumentException.class)
          .hasMessage("payloadBytesSupplier must supply byte arrays of length 16");
    }

    @Test
    public void testGenerate() {
        assertThat(Ksuid40Generator.generate()).matches("[0-9a-zA-Z]{28}");
    }
}
