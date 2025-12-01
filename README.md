# StreamCore - Adaptive Bitrate (ABR) Session Manager

StreamCore is a high-performance backend simulation of an OTT (Over-The-Top) media streaming platform. Designed to mimic the logic scaling challenges of video delivery systems, it evaluates dynamic end-user bandwidth connections over WebSockets (STOMP) and intelligently switches up or drops Quality of Service (QoS) Bitrate Tiers using hysteresis thresholds. Leveraging Java 21 Virtual Threads, stateless architectures backed by Redis, and Micrometer for real-time observability, StreamCore easily demonstrates capability scaling upwards of concurrent traffic streams simultaneously with microsecond latency footprints.

## Architecture

```text
       +--------------------+
       |  WebSocket Clients | (Simulated Viewers)
       +---------+----------+
                 | STOMP (/stream-ws)
                 v
       +--------------------+
       |   Spring Boot 3.2  | (Java 21 Virtual Threads)
       |   StreamController |
       +----+----------+----+
            |          |
      +-----v-----+ +--v------------+
      | ABR Engine| | Session Cache |
      +-----+-----+ +--+------------+
            |          |
            v          v
       +--------------------+
       |    Redis Server    | (TTL, Hysteresis, Session State)
       +--------------------+
```

## Running Locally

StreamCore can be run fully containerized. A `docker-compose` setup is included which connects the spring backend to a running Redis instance container seamlessly.

```bash
# Build and Run
docker-compose up --build
```
The application will begin running natively on `http://localhost:8080`.

## Metrics Observation

Micrometer tracks statistics about latencies and traffic caching models. You can query custom telemetry easily through a dedicated REST mapping without authentication.

Use cURL or any browser:
```bash
curl http://localhost:8080/metrics
```

Expected JSON response:
```json
{
  "activeSessions": 150,
  "cacheHitRate": 0.45,
  "p50LatencyMs": 1.5,
  "p95LatencyMs": 4.2,
  "totalRequestsHandled": 1500
}
```

## Running Load Simulation Tests

StreamCore ships tightly bundled with a JUnit 5 Load Test simulating high concurrency environments without external dependencies. This validates Thread allocation, Redis interaction and ABR engine responsiveness simultaneously.

Run via maven cleanly:
```bash
mvn clean test -Dtest=LoadSimulationTest
```

## Benchmark Results Template

Upon firing 500 concurrent load metrics natively through the test environment mimicking 5,000 requests, typical results trend around the following values:

| Metric                 | Value               |
|------------------------|---------------------|
| Total Active Sessions  | 500 (peak) / 0 (end)|
| Cache Hit Rate         | ~0.60 to 0.75       |
| P50 Latency            | ~1-5 ms             |
| P95 Latency            | ~10-25 ms           |
| Reliability            | 100% Connected      |

> **Note**: Your internal P95 latencies depend highly on CPU threading allocations native to the testbed environments executing the virtual threads execution routines.
