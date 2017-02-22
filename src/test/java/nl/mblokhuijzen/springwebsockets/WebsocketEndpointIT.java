package nl.mblokhuijzen.springwebsockets;

import nl.mblokhuijzen.springwebsockets.beans.GameState;
import nl.mblokhuijzen.springwebsockets.beans.Move;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebsocketEndpointIT {
    @Value("${local.server.port}")
    private int port;
    private String URL;

    private static final String SEND_CREATE_BOARD_ENDPOINT = "/app/create/";
    private static final String SEND_MOVE_ENDPOINT = "/app/move/";
    private static final String SUBSCRIBE_CREATE_BOARD_ENDPOINT = "/topic/board/";
    private static final String SUBSCRIBE_MOVE_ENDPOINT = "/topic/move/";

    private CompletableFuture<GameState> completableFuture;

    @Before
    public void setup() {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/game";
    }


    @Test
    public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
        String uuid = UUID.randomUUID().toString();

        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CREATE_BOARD_ENDPOINT + uuid, new CreateGameStompFrameHandler());
        stompSession.send(SEND_CREATE_BOARD_ENDPOINT + uuid, null);

        GameState gameState = completableFuture.get(10, SECONDS);

        assertNotNull(gameState);
    }

    @Test
    public void testMakeMoveEndpoint() throws InterruptedException, ExecutionException, TimeoutException {
        String uuid = UUID.randomUUID().toString();

        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_MOVE_ENDPOINT + uuid, new CreateGameStompFrameHandler());
        stompSession.send(SEND_MOVE_ENDPOINT + uuid, new Move(1, 0));
        GameState gameStateAfterMove = completableFuture.get(5, SECONDS);

        assertNotNull(gameStateAfterMove);
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class CreateGameStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            System.out.println(stompHeaders.toString());
            return GameState.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            System.out.println((GameState) o);
            completableFuture.complete((GameState) o);
        }
    }
}
