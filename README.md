# ksuid40

This is a Java port of [Segment's K-Sortable Globally Unique IDs](https://github.com/segmentio/ksuid). with 40 bit timestamp and epoch as 1 January 1970 UTC 00:00:00 and 29 bytes long

KSUID is for K-Sortable Unique IDentifier. It's a way to generate globally unique IDs similar to RFC 4122 UUIDs,
but contain a time component so they can be "roughly" sorted by time of creation.
The remainder of the KSUID is randomly generated bytes.

In summary:
*   Roughly sortable by creation time;
*   Can be stored as a string of 29 chars;
*   Can be stored as an array of 21 bytes;
*   String format is encoded to [base-62](https://en.wikipedia.org/wiki/Base62) (0-9A-Za-z);
*   String format is URL safe and has no hyphens.

For the story of how KSUIDs came about, see [A brief history of the UUID](https://segment.com/blog/a-brief-history-of-the-uuid).

## Usage as cli

To use this as a command-line program on Unix-like systems, run

```bash
wget https://repo1.maven.org/maven2/com/github/ksuid40/ksuid40/1.1.0/ksuid40-1.1.0-cli.jar
sudo mv ksuid40-1.1.0-cli.jar /usr/local/bin/ksuid40
sudo chmod +x /usr/local/bin/ksuid40

ksuid40
# prints 1HCpXwx2EK9oYluWbacgeCnFcLf
```

## Usage as library

Then simply generate a ksuid40 string like this:

```java
String ksuid40 = Ksuid40.newKsuid().toString();
System.out.println(ksuid40); // prints 1HCpXwx2EK9oYluWbacgeCnFcLf

```

&nbsp;

For more complex use cases, create a `40` with a `SecureRandom` and get a new `Ksuid40` for use.

Note that `Ksuid40Generator` is threadsafe and `Ksuid40` is immutable (and therefore threadsafe).

```java
// Construct a new KsuidGenerator object. Since it is threadsafe you only need one.
private static final Ksuid40Generator KSUID_GENERATOR = new Ksuid40Generator(new SecureRandom());

// Get a new Ksuid object.
final Ksuid ksuid40 = ksuid40Generator.newKsuid();

// The toString() method is the string representation of KSUID.
System.out.println("ksuid40:\n" + ksuid40  + "\n");

// The log string format shows some details on one line, suitable for logging.
System.out.println("ksuid40.toLogString():\n" + ksuid40.toLogString() + "\n");

// The inspect string format shows details.
System.out.println("ksuid40.toInspectString():\n" + ksuid40.toInspectString());

```
The output from the code block above is

```
ksuid40:
1HCpXwx2EK9oYluWbacgeCnFcLf

ksuid40.toLogString():
Ksuid[timestamp = 150215977, string = 1HCpXwx2EK9oYluWbacgeCnFcLf payload = [124, 76, 43, -110, 116, -6, \
    -91, 45, 0, -125, -127, 109, 28, 24, 28, -17], ksuidBytes = [8, -12, 29, 41, 124, 76, 43, -110, 116, \
    -6, -91, 45, 0, -125, -127, 109, 28, 24, 28, -17]]

ksuid40.toInspectString():
REPRESENTATION:

  String: 1HCpXwx2EK9oYluWbacgeCnFcLf
     Raw: 08F41D297C4C2B9274FAA52D0083816D1C181CEF

COMPONENTS:

       Time: 2019-02-14 23:32:57 -0800 PST
  Timestamp: 150215977
    Payload: 7C4C2B9274FAA52D0083816D1C181CEF
```

## Performance

A very rough performance profile for generating KSUIDs was run on a MacBook Pro with a 3.1 GHz Intel Core i7 and 16 GB 2133 MHz LPDDR3 RAM.

```java
public static void main(final String[] args) {
    final Ksuid40Generator generator = new Ksuid40Generator(new SecureRandom());
    IntStream.range(0, 100).forEach(i -> generator.newKsuid()); // prime the random

    IntStream.iterate(1000, operand -> operand * 10)
             .limit(5)
             .forEach(count -> {
                 final long start = System.nanoTime();
                 IntStream.range(0, count).forEach(i -> generator.newKsuid());
                 final long duration = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                 System.out.println(String.format("%,d in %,d ms. rate = %,d/ms", count, duration, count / duration));
             });
}
```
The output from the code block above is
```
1,000 in 14 ms. rate = 71/ms
10,000 in 32 ms. rate = 312/ms
100,000 in 95 ms. rate = 1,052/ms
1,000,000 in 881 ms. rate = 1,135/ms
10,000,000 in 6,665 ms. rate = 1,500/ms
```

## License
This library is Open Source software released under the [MIT license](https://opensource.org/licenses/MIT).
