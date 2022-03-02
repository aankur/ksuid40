package com.github.ksuid40;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainTest {

    private static final TimeZone defaultTimeZone = TimeZone.getDefault();

    @BeforeAll
    public static void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterAll
    public static void afterClass() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    public void testUsage() {
        final RunResult runResult = runMain("-h");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("Usage of ksuid:\n"
                + "  -f string\n"
                + "        One of string, inspect, time, timestamp, payload, raw, or template. (default \"string\")\n"
                + "  -n int\n"
                + "        Number of KSUIDs to generate when called with no other arguments. (default 1)\n"
                + "  -t string\n"
                + "        The Go template used to format the output.\n"
                + "  -v    Turn on verbose mode.\n"
                + "");
    }

    @Test
    public void testGenerateOne() {
        final RunResult runResult = runMain();
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("0Dz40rGb7Ea16c6dXiXv3p5w2yfq\n");
    }

    @Test
    public void testGenerateThree() {
        final RunResult runResult = runMain("-n", "3");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("0Dz40rGb7Ea16c6dXiXv3p5w2yfq\n"
                + "0Dz40rLqt4fKkTWaI45taSxaU2yC\n"
                + "0Dz40rKJa37f8reL0CFGgthinFK2\n");
    }

    @Test
    public void testPositionalArgsNoFlags() {
        final RunResult runResult = runMain("024rUCafWbTglyvWlQEuaxKqqiuY", "024rUCfvIRZ0PqLTVlmt7bCVHnCu");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("024rUCafWbTglyvWlQEuaxKqqiuY\n"
                + "024rUCfvIRZ0PqLTVlmt7bCVHnCu\n");
    }

    @Test
    public void testVerbose() {
        final RunResult runResult = runMain("-v", "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("024rUCafWbTglyvWlQEuaxKqqiuY: 024rUCafWbTglyvWlQEuaxKqqiuY\n");
    }

    @Test
    public void testInspect() {
        final RunResult runResult = runMain("-f", "inspect", "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("REPRESENTATION:\n"
                + "\n"
                + "  String: 024rUCafWbTglyvWlQEuaxKqqiuY\n"
                + "     Raw: 000E9110E816D1D7403226FA924557DA9B3A0F4642\n"
                + "\n"
                + "COMPONENTS:\n"
                + "\n"
                + "       Time: 1977-09-29 13:34:32 +0000 UTC\n"
                + "  Timestamp: 244388072\n"
                + "    Payload: 16D1D7403226FA924557DA9B3A0F4642\n"
                + "\n"
                + "");
    }

    @Test
    public void testTime() {
        final RunResult runResult = runMain("-f", "time", "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("1977-09-29 13:34:32 +0000 UTC\n");
    }

    @Test
    public void testTimestamp() {
        final RunResult runResult = runMain("-f", "timestamp", "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("244388072\n");
    }

    @Test
    public void testPayload() {
        final RunResult runResult = runMain("-f", "payload", "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isZero();
        final String hex = Hex.hexEncode(runResult.stdout.getBytes(UTF_8));
        assertThat(hex).isEqualTo("16EFBFBDEFBFBD403226EFBFBDEFBFBD4557DA9B3A0F4642");
    }

    @Test
    public void testRaw() {
        final RunResult runResult = runMain("-f", "raw", "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isZero();
        final String hex = Hex.hexEncode(runResult.stdout.getBytes(UTF_8));
        assertThat(hex).isEqualTo("000EEFBFBD10EFBFBD16EFBFBDEFBFBD403226EFBFBDEFBFBD4557DA9B3A0F4642");
    }

    @Test
    public void testTemplate() {
        final String templateText = "string={{.String}} raw={{.Raw} time={{.Time}} timestamp={{.Timestamp} payload={{.Payload}}";
        final RunResult runResult = runMain("-f", "template", "-t", templateText, "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("string=024rUCafWbTglyvWlQEuaxKqqiuY raw={{.Raw} time=1977-09-29 13:34:32 +0000 UTC "
                + "timestamp={{.Timestamp} payload=16D1D7403226FA924557DA9B3A0F4642\n"
                + "");
    }

    @Test
    public void testTemplateNoText() {
        final RunResult runResult = runMain("-f", "template", "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isZero();
        assertThat(runResult.stdout).isEqualTo("\n");
    }

    @Test
    public void testBadFormattingFunction() {
        final RunResult runResult = runMain("-f", "foo", "0000024rUCafWbTglyvWlQEuaxKqqiuY");
        assertThat(runResult.exitCode).isOne();
        assertThat(runResult.stdout).isEqualTo("Bad formatting function: foo\n"
                + "Usage of ksuid:\n"
                + "  -f string\n"
                + "        One of string, inspect, time, timestamp, payload, raw, or template. (default \"string\")\n"
                + "  -n int\n"
                + "        Number of KSUIDs to generate when called with no other arguments. (default 1)\n"
                + "  -t string\n"
                + "        The Go template used to format the output.\n"
                + "  -v    Turn on verbose mode.\n"
                + "");
    }

    @Test
    public void testBadNumber() {
        final RunResult runResult = runMain("-n", "foo");
        assertThat(runResult.exitCode).isOne();
        assertThat(runResult.stdout).isEqualTo("invalid value \"foo\" for flag -n: parse error\n"
                + "Usage of ksuid:\n"
                + "  -f string\n"
                + "        One of string, inspect, time, timestamp, payload, raw, or template. (default \"string\")\n"
                + "  -n int\n"
                + "        Number of KSUIDs to generate when called with no other arguments. (default 1)\n"
                + "  -t string\n"
                + "        The Go template used to format the output.\n"
                + "  -v    Turn on verbose mode.\n"
                + "");
    }

    @Test
    public void testUnknownFlag() {
        final RunResult runResult = runMain("-z");
        assertThat(runResult.exitCode).isOne();
        assertThat(runResult.stdout).isEqualTo("flag provided but not defined: -z\n"
                + "Usage of ksuid:\n"
                + "  -f string\n"
                + "        One of string, inspect, time, timestamp, payload, raw, or template. (default \"string\")\n"
                + "  -n int\n"
                + "        Number of KSUIDs to generate when called with no other arguments. (default 1)\n"
                + "  -t string\n"
                + "        The Go template used to format the output.\n"
                + "  -v    Turn on verbose mode.\n"
                + "");
    }

    @Test
    public void testBadKsuid() {
        final RunResult runResult = runMain("z");
        assertThat(runResult.exitCode).isOne();
        assertThat(runResult.stdout).isEqualTo("Error when parsing \"z\": Valid encoded KSUIDs are 27 characters\n"
                + "Usage of ksuid:\n"
                + "  -f string\n"
                + "        One of string, inspect, time, timestamp, payload, raw, or template. (default \"string\")\n"
                + "  -n int\n"
                + "        Number of KSUIDs to generate when called with no other arguments. (default 1)\n"
                + "  -t string\n"
                + "        The Go template used to format the output.\n"
                + "  -v    Turn on verbose mode.\n"
                + "");
    }

    @Test
    public void testMain() {
        try (final MockedConstruction<Main> mocked = Mockito.mockConstruction(Main.class)) {
            Objects.requireNonNull(mocked);
            assertThatNoException().isThrownBy(() -> Main.main("-h"));
        }
    }

    private RunResult runMain(final String... args) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteStream);

        final AtomicInteger exitCodeStorage = new AtomicInteger();
        final IntConsumer exitCodeConsumer = code -> {
            exitCodeStorage.set(code);
            throw new ExitException();
        };

        final Instant now = Instant.parse("2022-02-09T06:27:52.573Z");
        final Clock clock = Clock.tick(Clock.fixed(now, ZoneOffset.UTC), Duration.ofSeconds(2));

        final Main main = new Main(printStream, exitCodeConsumer, new Random(123L), clock);
        try {
            main.run(args);
        } catch (final ExitException ignore) {
            // ignore
        }

        printStream.flush();
        try {
            final String stdout = byteStream.toString("UTF-8");
            return new RunResult(stdout, exitCodeStorage.get());
        } catch (final UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class RunResult {
        private final String stdout;
        private final int exitCode;

        public RunResult(final String stdout, final int exitCode) {
            this.stdout = stdout;
            this.exitCode = exitCode;
        }
    }

    private static class ExitException extends RuntimeException {

        private static final long serialVersionUID = 60714103562415996L;

    }
}
