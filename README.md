# ksuid

This is a Java port of [Segment's K-Sortable Globally Unique IDs](https://github.com/segmentio/ksuid).

KSUID is for K-Sortable Unique IDentifier. It's a way to generate globally unique IDs similar to RFC 4122 UUIDs,
but contain a time component so they can be "roughly" sorted by time of creation.
The remainder of the KSUID is randomly generated bytes.

For the story of how KSUIDs came about, see [A brief history of the UUID](https://segment.com/blog/a-brief-history-of-the-uuid).

## Usage

Create a `KsuidGenerator` with a `SecureRandom` and get a new `Ksuid` for use.

Note that a `KsuidGenerator` is threadsafe and a `Ksuid` is immutable (and therefore threadsafe).

```java
// Construct a new KsuidGenerator object. Since it is threadsafe you only need one.
private static final KsuidGenerator KSUID_GENERATOR = new KsuidGenerator(new SecureRandom());

// Get a new Ksuid object.
final Ksuid ksuid = ksuidGenerator.newKsuid();

// The toString() method is the string representation of the object.
System.out.println("ksuid:\n" + ksuid  + "\n");

// The string format is the KSUID represenation.
System.out.println("ksuid.asString():\n" + ksuid.asString() + "\n");

// The inspect string format shows details.
System.out.println("ksuid.toInspectString():\n" + ksuid.toInspectString());

```
The output from the code block above is
```
ksuid:
Ksuid[timestamp = 150215977, payload = [124, 76, 43, -110, 116, -6, -91, 45, 0, -125, -127, 109, 28, 24, 28, -17], ksuidBytes = [8, -12, 29, 41, 124, 76, 43, -110, 116, -6, -91, 45, 0, -125, -127, 109, 28, 24, 28, -17]]

ksuid.asString():
1HCpXwx2EK9oYluWbacgeCnFcLf

ksuid.toInspectString():
REPRESENTATION:

  String: 1HCpXwx2EK9oYluWbacgeCnFcLf
     Raw: 08F41D297C4C2B9274FAA52D0083816D1C181CEF

COMPONENTS:

       Time: 2019-02-14 23:32:57 -0800 PST
  Timestamp: 150215977
    Payload: 7C4C2B9274FAA52D0083816D1C181CEF
```

## Performance

A very rough performance profile for generating KSUIDs was run on a 2017 MacBook Pro with a 3.1 GHz Intel Core i7 and 16 GB 2133 MHz LPDDR3 RAM.

```java
public static void main(final String[] args) {
    final KsuidGenerator generator = new KsuidGenerator(new SecureRandom());
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

## Contributors
* `rlubbat@paypal.com` Ramsey Lubbat
