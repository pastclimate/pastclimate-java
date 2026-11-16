# PastClimate Java SDK

The official Java client for the [PastClimate API](https://developer.pastclimate.com/api/):
historical daily, hourly, monthly and yearly weather, climate normals and records.

- Java 11 or newer. Uses `java.net.http.HttpClient`.
- One runtime dependency: [Gson](https://github.com/google/gson) (see [Why Gson](#why-gson)).
- One method per API operation, with typed response models.
- One exception class per kind of failure (unchecked).
- Automatic retries where retrying is safe.
- Credit and rate-limit numbers on every response.

## Install

Maven (the `groupId` is a placeholder until the project owner publishes under a verified namespace):

```xml
<dependency>
  <groupId>com.pastclimate</groupId>
  <artifactId>pastclimate</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.pastclimate:pastclimate:0.1.0'
```

## Quickstart

Get a key on your [API page](https://www.pastclimate.com/account/api/) and put it in an
environment variable. Never put a key in source code or in a URL.

```sh
export PASTCLIMATE_API_KEY=your_key_here
```

```java
import com.pastclimate.*;
import com.pastclimate.model.DailyRow;
import java.time.LocalDate;
import java.util.List;

PastClimateClient client = PastClimateClient.builder().build(); // reads PASTCLIMATE_API_KEY

ApiResponse<List<DailyRow>> response = client.getDaily(
        Place.id("gb/london"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

for (DailyRow day : response.getData()) {
    System.out.println(day.getDate() + ": " + day.getDailyMinTemp() + " to " + day.getDailyMaxTemp());
}

System.out.println("Used " + response.getCreditsUsed() + " credits, " + response.getCreditsRemaining() + " left");
```

A runnable version is in [`examples/Quickstart.java`](examples/Quickstart.java). It makes real calls, so it
only runs when `PASTCLIMATE_API_KEY` is set.

## Configuration

```java
PastClimateClient client = PastClimateClient.builder()
        .apiKey("...")                            // default: the PASTCLIMATE_API_KEY environment variable
        .baseUrl("https://api.pastclimate.com")   // default: PASTCLIMATE_BASE_URL, then this value
        .timeout(Duration.ofSeconds(30))          // per attempt
        .maxRetries(2)                            // retries after the first try
        .maxRetryDelay(Duration.ofSeconds(60))    // longest wait the client will sleep between tries
        .build();
```

- With no key, `build()` throws `ConfigurationException`.
- `baseUrl` must be `https`. Plain `http` is accepted only for `localhost` and `127.0.0.1`.
- The key is sent as `Authorization: Bearer <key>`, never in the URL. `client.toString()` hides it.
- Redirects are not followed, so the key cannot be sent to another host.
- The `User-Agent` is `pastclimate-java/<version>`.
- A client is thread-safe; create one and share it.

## Places

Every weather method takes a `Place`. Give it in one of two ways.

```java
// 1. A location id: country code, slash, place slug.
Place london = Place.id("gb/london");

// 2. A latitude and longitude, with the two-letter country they are in.
Place point = Place.point(51.5, -0.12, "gb");
```

Find ids with `searchLocations`. A point is snapped to the nearest place in that country; the response's
`getLocation().getRequested()` shows the point you sent and how far away the chosen place is.

## Methods

Every JSON method returns an `ApiResponse<T>` with `getData()`, `getLocation()`, `getMeta()`,
`getRateLimit()`, `getCreditsUsed()`, `getCreditsRemaining()`, `getStatus()` and `getHeaders()`. The type of
`getData()` is given below. Getters on models return `null` when the server leaves a field out; fields this
version does not know about are ignored.

Dates are `LocalDate` and timestamps are `Instant`. Each method has overloads with and without the optional
arguments (`Units`, `days`, `month`).

### `searchLocations(String q)` and `searchLocations(String q, String country)`

Search places by the start of their name. Data: `List<LocationSearchResult>`.

```java
for (LocationSearchResult place : client.searchLocations("lon", "gb").getData()) {
    System.out.println(place.getId() + " " + place.getName() + " " + place.getWeatherAvailable());
}
```

### `nearestLocation(double lat, double lon, String country)`

The place nearest a point, inside one country. Data: `NearestLocation`.

```java
NearestLocation nearest = client.nearestLocation(51.5, -0.12, "gb").getData();
System.out.println(nearest.getId() + ", " + nearest.getRequested().getDistanceKm() + " km away");
```

### `getLocation(String id)`

One location by id. Data: `LocationDetail`, including `getCoverage()` (`getFirstYear()`, `getLastDate()`).

```java
LocationDetail london = client.getLocation("gb/london").getData();
System.out.println(london.getTimezone() + " " + london.getCoverage().getLastDate());
```

### `getDaily(Place, LocalDate start, LocalDate end[, Units])`

Daily values from `start` to `end`, both included. Data: `List<DailyRow>`.

```java
ApiResponse<List<DailyRow>> daily = client.getDaily(
        Place.id("gb/london"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), Units.IMPERIAL);
for (DailyRow d : daily.getData()) {
    System.out.println(d.getDate() + " " + d.getDailyAvgTemp() + " " + daily.getMeta().getUnits().getTemperature());
}
```

### `getHourly(Place, LocalDate date[, int days[, Units]])`

Hourly values for `days` consecutive local days starting at `date`. Leave `days` out for the server
default. Data: `List<HourlyRow>`.

```java
for (HourlyRow h : client.getHourly(london, LocalDate.of(2024, 1, 15), 1).getData()) {
    System.out.println(h.getTimestampUtc() + " " + h.getTemperature());
}
```

### `getMonthly(Place[, Units])`

Every month on file. Data: `List<MonthlyRow>`.

```java
List<MonthlyRow> monthly = client.getMonthly(london).getData();
System.out.println(monthly.get(monthly.size() - 1).getMonthlyTotalRain());
```

### `getYearly(Place[, Units])`

Every year on file. Data: `List<YearlyRow>`.

```java
for (YearlyRow y : client.getYearly(london).getData()) {
    System.out.println(y.getYear() + " " + y.getYearlyAvgTemp());
}
```

### `getNormals(Place[, Integer month[, Units]])`

Climate normals. Data: `NormalsData` with `getMonthly()` and `getDayOfYear()` lists. `month` (1 to 12) narrows
both to one month.

```java
NormalsData normals = client.getNormals(london, 7).getData();
System.out.println(normals.getMonthly().get(0).getAvgTemp() + " " + normals.getDayOfYear().get(0).getRainProb());
```

### `getRecords(Place[, Units])`

The hottest, coldest, wettest and windiest day on file. Data: `List<RecordItem>`.

```java
for (RecordItem r : client.getRecords(london).getData()) {
    System.out.println(r.getMetric() + ": " + r.getValue() + " " + r.getUnit() + " on " + r.getOccurredOn());
}
```

### `getCoverage()` and `getCoverage(String country)`

The countries the archive serves, and the first year and last day of published data for each. Data:
`CoverageData`. Check it before asking for dates near the end of the archive.

```java
for (CountryCoverage c : client.getCoverage("gb").getData().getCountries()) {
    System.out.println(c.getCountry() + ": " + c.getFirstYear() + " to " + c.getLastDate());
}
```

### `getMe()`

Your plan, credit balance and limits. Data: `MeData`. Use it instead of hard-coding limits.

```java
MeData me = client.getMe().getData();
System.out.println(me.getPlan().getName() + " " + me.getCredits().getUsedThisMonth() + " " + me.getRateLimit().getPerMinute());
```

## Units

Pass `Units.METRIC` (the server default) or `Units.IMPERIAL`, or leave it out. Field names are the same in
both; the unit of each value is in `response.getMeta().getUnits()` (`getSystem()`, `getTemperature()`,
`getPrecipitation()`, `getWindSpeed()`, `getPressure()`).

```java
ApiResponse<List<RecordItem>> response = client.getRecords(london, Units.IMPERIAL);
System.out.println(response.getMeta().getUnits().getTemperature()); // F
```

## CSV

Daily and hourly data have CSV methods, so the return type stays typed: `getDailyCsv` and `getHourlyCsv`
(same arguments as `getDaily` and `getHourly`) return a `CsvResponse`.

```java
CsvResponse csv = client.getDailyCsv(london, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
Files.writeString(Path.of("london-2023.csv"), csv.getCsv());
System.out.println(csv.getCreditsUsed());
```

A CSV body has no `meta`, so `getCreditsUsed()`, `getCreditsRemaining()` and `getRateLimit()` come from the
response headers. If the request fails you still get a typed exception, never an error saved as CSV.

## Credits and rate limits

Every response, JSON or CSV, tells you what the call cost and what is left:

```java
ApiResponse<List<MonthlyRow>> response = client.getMonthly(london);

response.getCreditsUsed();          // credits this call used (Long)
response.getCreditsRemaining();     // credits you can still spend (may be null)
response.getMeta().getMethodologyVersion();

RateLimit limit = response.getRateLimit();   // the per-minute request limit
limit.getLimit();                   // requests allowed per minute
limit.getRemaining();               // requests left in this minute
limit.getReset();                   // seconds until the oldest request leaves the minute window
```

The SDK does not know what a call costs and does not check your balance. Read the numbers from the response,
or call `getMe()`. Rate-limit values are `null` when the server did not send the header.

## Errors

Everything the SDK throws extends `PastClimateException` (a `RuntimeException`).

| Class | Thrown for (error code) |
|---|---|
| `AuthenticationException` | `missing_key`, `invalid_key`, `key_revoked`, `email_not_verified`, `account_suspended` |
| `InvalidRequestException` | `invalid_request`, `range_too_large`, `key_in_query`, `cors_not_supported`, `method_not_allowed` |
| `NotFoundException` | `location_not_found`, `not_published`, `not_found`, `location_blocked` |
| `PlanRequiredException` | `plan_required` |
| `RateLimitedException` | `rate_limited`, `daily_request_cap_reached` |
| `QuotaExceededException` | `quota_exceeded` |
| `ServerException` | `internal_error`, `api_disabled`, `metering_unavailable` (and `invalid_response`, when a success body cannot be read) |
| `ConnectionException` | no HTTP answer: DNS, TLS, reset or timeout (`e.isTimeout()`) |
| `ConfigurationException` | the client was set up wrongly |

The first seven rows extend `ApiException` and carry `getStatus()`, `getCode()`, `getMessage()`, `getDocsUrl()`,
plus `getHeaders()`, `getRateLimit()`, `getRetryAfter()` (seconds, or `null`), `getUpgradeUrl()` and `getBody()`.
An unknown future code still maps to a class by its HTTP status. If a proxy answers without the API's error
body, `getCode()` is `"http_<status>"` and `getDocsUrl()` is `null`.

Bad arguments (a blank id, a month of 13) throw `IllegalArgumentException` before any request is sent.

```java
try {
    client.getDaily(london, LocalDate.of(1900, 1, 1), LocalDate.of(1900, 1, 31));
} catch (NotFoundException e) {
    System.out.println("No data: " + e.getCode() + " - " + e.getMessage()); // e.g. not_published; see getCoverage()
} catch (QuotaExceededException e) {
    System.out.println("Out of credits. Upgrade: " + e.getUpgradeUrl());
} catch (RateLimitedException e) {
    System.out.println("Slow down. Wait " + e.getRetryAfter() + " seconds.");
} catch (ApiException e) {
    System.out.println(e.getStatus() + " " + e.getCode() + ": " + e.getMessage() + " (" + e.getDocsUrl() + ")");
} catch (ConnectionException e) {
    System.out.println("Network problem: " + e.getMessage());
}
```

Branch on the class or `getCode()`, not on the message. Failed calls are not charged.

## Retries

The client retries a call automatically, up to `maxRetries` times (default 2):

| Failure | What happens |
|---|---|
| `429` **with** `Retry-After` | waits that many seconds, then retries |
| `429` **without** `Retry-After` | thrown at once (on a plan with one-time credits this means they are used up, and waiting does not help) |
| `500`, `502`, `503`, any `5xx` | retries with exponential backoff and jitter |
| network error or timeout | retries with the same backoff |
| any other `4xx` | thrown at once; fix the request |

A `429` whose `Retry-After` is longer than `maxRetryDelay` (default 60 seconds), for example "wait until
midnight UTC", is thrown instead of slept on; `getRetryAfter()` tells you how long. `maxRetries(0)` turns
retrying off. Retries repeat the same GET, so they cannot double-send anything. Refused calls still count
toward your daily request limit, so do not wrap the client in an unbounded retry loop of your own.

## Custom HTTP transport

The default transport is `JdkHttpTransport`. To use another HTTP library, or a fake in your own tests,
implement `HttpTransport` (`TransportResponse get(URI uri, Map<String, String> headers, Duration timeout)`,
throwing `ConnectionException` when no HTTP answer arrives) and pass it to `.transport(...)`.

## Why Gson

The SDK needs a JSON library for typed models. Gson is one jar with no transitive dependencies, ignores
unknown fields by default (the API adds fields without notice), and needs no annotation processing or module
configuration. Jackson would add three jars (core, annotations, databind) for no benefit here.

## Development

```sh
mvn test
```

The tests use a fake transport, plus a throwaway local HTTP server for the default transport; they never call
the real API.

## License

MIT. See [LICENSE](LICENSE).
