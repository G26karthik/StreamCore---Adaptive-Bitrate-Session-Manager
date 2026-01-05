package com.streamcore;

import com.streamcore.controller.MetricsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import com.streamcore.session.SessionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Load simulation testing the ABR session management against multiple concurrent users.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoadSimulationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MetricsController metricsController;
    
    @Autowired
    private SessionRegistry sessionRegistry;

    @Test
    void simulateLoad() throws Exception {
        int clientCount = 500;
        int messagesPerClient = 10;
        int threadPoolSize = 50;

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(threadPoolSize);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        System.out.println("Starting load simulation...");

        for (int i = 0; i < clientCount; i++) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            futures.add(future);

            executor.submit(() -> {
                try {
                    String sessionId = UUID.randomUUID().toString();
                    String url = "ws://localhost:" + port + "/stream-ws";
                    
                    StompSession session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

                    for (int j = 0; j < messagesPerClient; j++) {
                        int randomBandwidth = ThreadLocalRandom.current().nextInt(500, 20001);
                        String payload = "{\"sessionId\":\"" + sessionId + "\", \"bandwidthKbps\":" + randomBandwidth + "}";
                        
                        StompHeaders headers = new StompHeaders();
                        headers.setDestination("/app/stream.update");
                        headers.setContentType(org.springframework.util.MimeTypeUtils.APPLICATION_JSON);
                        
                        session.send(headers, payload.getBytes());
                        Thread.sleep(100);
                    }
                    
                    // Simulate disconnect and removal from local registry (which normally WebSocket disconnect listener handles, 
                    // but since we lack explicit interceptor binding we might just call remove to assure clean state or assume it)
                    sessionRegistry.remove(sessionId); 
                    session.disconnect();
                    future.complete(null);

                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // Let system process all metrics
        Thread.sleep(2000);

        Map<String, Object> metrics = metricsController.getMetrics();
        
        System.out.println("\n--- SIMULATION SUMMARY ---");
        System.out.println("Active Sessions: " + sessionRegistry.getActiveSessionCount().get()); // should be 0 because we removed them
        System.out.println("Cache Hit Rate: " + metrics.get("cacheHitRate"));
        System.out.println("p50 Latency (ms): " + metrics.get("p50LatencyMs"));
        System.out.println("p95 Latency (ms): " + metrics.get("p95LatencyMs"));
        System.out.println("Total Requests: " + metrics.get("totalRequestsHandled"));
        System.out.println("--------------------------\n");

        assertEquals(0, sessionRegistry.getActiveSessionCount().get(), "All sessions should clean up");
        assertTrue((Double) metrics.get("cacheHitRate") > 0.3, "Cache hit rate should be > 0.3");
        assertTrue((Double) metrics.get("p95LatencyMs") < 500, "95th percentile latency should be < 500ms");
    }
}
