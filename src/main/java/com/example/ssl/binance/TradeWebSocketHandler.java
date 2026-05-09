package com.example.ssl.binance;

import com.example.ssl.JwtDecoder;
import com.example.ssl.protobuf.PriceUpdateClass;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.*;

@Component
public class TradeWebSocketHandler extends BinaryWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<BinaryMessage>> sessionQueues = new ConcurrentHashMap<>();
    private final ExecutorService messageSenderExecutor = Executors.newCachedThreadPool();
    private final JwtDecoder jwtDecoder;

    public TradeWebSocketHandler(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromSession(session);

        if (token == null || !jwtDecoder.isValid(token)) {
            System.out.println("Невалідний токен, з'єднання відхилено: " + session.getId());
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        sessions.add(session);
        ConcurrentLinkedQueue<BinaryMessage> queue = new ConcurrentLinkedQueue<>();
        sessionQueues.put(session.getId(), queue);
        messageSenderExecutor.submit(() -> processSessionMessages(session, queue));
        System.out.println("WebSocket сесія відкрита: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        sessionQueues.remove(session.getId());
        System.out.println("WebSocket сесія закрита: " + session.getId());
    }

    public void broadcastPriceUpdate(PriceUpdateClass.PriceUpdate protoUpdate) {
        byte[] bytes = protoUpdate.toByteArray();
        BinaryMessage message = new BinaryMessage(bytes);
        for (WebSocketSession session : sessions) {
            ConcurrentLinkedQueue<BinaryMessage> queue = sessionQueues.get(session.getId());
            if (queue != null) {
                queue.offer(message);
            }
        }
    }

    private String getTokenFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    private void processSessionMessages(WebSocketSession session, ConcurrentLinkedQueue<BinaryMessage> queue) {
        while (session.isOpen()) {
            BinaryMessage message = queue.poll();
            if (message != null) {
                try {
                    session.sendMessage(message);
                } catch (IOException | IllegalStateException e) {
                    System.err.println("Помилка відправки: " + e.getMessage());
                    if (!session.isOpen()) break;
                }
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        messageSenderExecutor.shutdown();
    }
}