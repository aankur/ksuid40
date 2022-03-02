package com.github.ksuid40;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HexTest {
    private static final String PLAIN_TEXT = "The quick brown fox jumps over the lazy dog";
    private static final String HEX = "54686520717569636B2062726F776E20666F78206A756D7073206F76657220746865206C617A7920646F67";

    @Test
    public void utilityClass()  {
        assertThrows(InvocationTargetException.class, () -> {
            final Constructor<Hex> constructor = Hex.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    public void characterSet() {
        assertThat(Hex.HEX_CHARACTERS).isEqualTo("0123456789ABCDEF".toCharArray());
    }

    @Test
    public void hexDecode() {
        assertThat(Hex.hexDecode(HEX)).isEqualTo(PLAIN_TEXT.getBytes());
    }

    @Test
    public void hexDecodeCaseInsensitive() {
        assertThat(Hex.hexDecode(HEX.toLowerCase())).isEqualTo(PLAIN_TEXT.getBytes());
    }

    @Test
    public void hexDecodeNull() {
        assertThat(Hex.hexDecode(null)).isNull();
    }

    @Test
    public void hexDecodeIllegalHexCharacter() {
        assertThrows(IllegalArgumentException.class, () -> {
            Hex.hexDecode(HEX.replace('5', 'z'));
        });
    }

    @Test
    public void hexDecodeOddLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            Hex.hexDecode(HEX.substring(1));
        });
    }

    @Test
    public void hexEncode() {
        assertThat(Hex.hexEncode(PLAIN_TEXT.getBytes())).isEqualTo(HEX);
    }

    @Test
    public void hexEncodeNull() {
        assertThat(Hex.hexEncode(null)).isNull();
    }
}
