package ru.lvmlabs.neuronum.calls.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.lvmlabs.neuronum.calls.dto.CallResponseDto;
import ru.lvmlabs.neuronum.calls.model.Call;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    private static final Semaphore semaphore = new Semaphore(1);

    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Text messages not supported"));
    }

    @SneakyThrows
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);

        session.sendMessage(new TextMessage("Hello World!"));
        log.debug("Session open for url: {}", session.getUri());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessions.remove(session);
        log.debug("Session closed for url: {} with status: {} and reason: {}", session.getUri(), closeStatus.getCode(), closeStatus.getReason());
    }

    public void publish(Call call) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.warn("Current thread is interrupted! Exiting...");
            return;
        }

        String clinic = call.getClinic().toString();

        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen() || session.getUri() == null || !session.getUri().getPath().endsWith(clinic)) {
                continue;
            }

            try {
                log.debug("Sending message to clinic: '{}'...", clinic);
                session.sendMessage(
                        new TextMessage(
                                objectMapper.writeValueAsString(
                                        objectMapper.convertValue(call, CallResponseDto.class)
                                )
                        )
                );

            } catch (Exception exception) {
                log.error("Can't send message to clinic '{}'!", clinic);
                exception.printStackTrace();
            }

            break;
        }

        semaphore.release();
    }
}
