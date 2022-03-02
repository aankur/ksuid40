package com.github.ksuid40;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Main program for generating ksuids from command-line.
 *
 * <p>
 * Mimics https://github.com/segmentio/ksuid/blob/v1.0.4/cmd/ksuid/main.go
 */
public final class Main {

    private final PrintStream printStream;
    private final IntConsumer exit;
    private final Random random;
    private final Clock clock;
    private final Flags flags = new Flags();
    private final Map<String, Consumer<Ksuid40>> printers;

    public static void main(final String... args) {
        final Main main = new Main(System.out, System::exit, new SecureRandom(), Clock.systemUTC());
        main.run(args);
    }

    Main(final PrintStream printStream, final IntConsumer exit, final Random random, final Clock clock) {
        this.printStream = printStream;
        this.exit = exit;
        this.random = random;
        this.clock = clock;

        printers = new HashMap<>();
        printers.put("string", this::printString);
        printers.put("inspect", this::printInspect);
        printers.put("time", this::printTime);
        printers.put("timestamp", this::printTimestamp);
        printers.put("payload", this::printPayload);
        printers.put("raw", this::printRaw);
        printers.put("template", this::printTemplate);
    }

    public void run(final String... args) {
        try {
            tryRun(args);
        } catch (final CliException e) {
            printStream.println(e.getMessage());
            printUsage(1);
        }
    }

    private void tryRun(final String... args) {
        parseFlags(args);

        final Consumer<Ksuid40> printer = printers.get(flags.format);

        final List<Ksuid40> ksuid40s = new ArrayList<>();
        final Ksuid40Generator ksuid40Generator = new Ksuid40Generator(random);
        if (flags.positionalArguments.isEmpty()) {
            IntStream.range(0, flags.count)
                    .forEach(any -> {
                        final Instant now = Instant.now(clock);
                        final Ksuid40 ksuid40 = ksuid40Generator.newKsuid(now);
                        ksuid40s.add(ksuid40);
                    });
        }

        flags.positionalArguments.forEach(arg -> ksuid40s.add(parse(arg)));

        ksuid40s.forEach(ksuid -> {
            if (flags.verbose) {
                printStream.printf("%s: ", ksuid);
            }
            printer.accept(ksuid);
        });
    }

    private Ksuid40 parse(final String arg) {
        try {
            return Ksuid40.fromString(arg);
        } catch (final IllegalArgumentException e) {
            throw new CliException("Error when parsing \"" + arg + "\": Valid encoded KSUIDs are 27 characters");
        }
    }

    private void parseFlags(final String... args) {
        boolean positionArgsOnly = false;
        final Iterator<String> iterator = Arrays.asList(args).iterator();
        while (iterator.hasNext()) {
            final String flag = iterator.next();

            if (!flag.startsWith("-")) {
                positionArgsOnly = true;
            }

            if (positionArgsOnly) {
                flags.positionalArguments.add(flag);
                continue;
            }

            final Supplier<String> value = () -> {
                if (!iterator.hasNext()) {
                    throw new CliException("flag needs an argument: " + flag);
                }
                return iterator.next();
            };

            switch (flag) {
                case "-n":
                    final String countValue = value.get();
                    try {
                        flags.count = Integer.parseInt(countValue);
                    } catch (final Exception e) {
                        throw new CliException("invalid value \"" + countValue + "\" for flag -n: parse error");
                    }
                    break;

                case "-f":
                    final String formatValue = value.get();
                    if (!printers.containsKey(formatValue)) {
                        throw new CliException("Bad formatting function: " + formatValue);
                    }
                    flags.format = formatValue;
                    break;

                case "-t":
                    flags.templateText = value.get();
                    break;

                case "-v":
                    flags.verbose = true;
                    break;

                case "-h":
                    printUsage(0);
                    break;

                default:
                    throw new CliException("flag provided but not defined: " + flag);
            }
        }
    }

    private void printUsage(final int exitCode) {
        printStream.print("Usage of ksuid:\n"
                + "  -f string\n"
                + "        One of string, inspect, time, timestamp, payload, raw, or template. (default \"string\")\n"
                + "  -n int\n"
                + "        Number of KSUIDs to generate when called with no other arguments. (default 1)\n"
                + "  -t string\n"
                + "        The Go template used to format the output.\n"
                + "  -v    Turn on verbose mode.\n"
                + "");
        exit.accept(exitCode);
    }

    private void printString(final Ksuid40 ksuid40) {
        printStream.println(ksuid40);
    }

    private void printInspect(final Ksuid40 ksuid40) {
        printStream.println(ksuid40.toInspectString());
    }

    private void printTime(final Ksuid40 ksuid40) {
        printStream.println(ksuid40.getTime());
    }

    private void printTimestamp(final Ksuid40 ksuid40) {
        printStream.println(ksuid40.getTimestamp());
    }

    private void printPayload(final Ksuid40 ksuid40) {
        printByteArray(ksuid40.getPayload());
    }

    private void printRaw(final Ksuid40 ksuid40) {
        printByteArray(ksuid40.asRaw());
    }

    private void printTemplate(final Ksuid40 ksuid40) {
        String result = flags.templateText;
        result = result.replace("{{.String}}", ksuid40.toString());
        result = result.replace("{{.Raw}}", ksuid40.asRaw());
        result = result.replace("{{.Time}}", ksuid40.getTime());
        result = result.replace("{{.Timestamp}}", ksuid40.getTimestamp() + "");
        result = result.replace("{{.Payload}}", ksuid40.getPayload());
        printStream.println(result);
    }

    private void printByteArray(final String hexBytes) {
        try {
            printStream.write(Hex.hexDecode(hexBytes));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class Flags {
        private int count = 1;
        private String format = "string";
        private String templateText = "";
        private boolean verbose;
        private final List<String> positionalArguments = new ArrayList<>();
    }

    private static class CliException extends RuntimeException {

        private static final long serialVersionUID = -7135545958349833195L;

        public CliException(final String msg) {
            super(msg);
        }
    }
}
