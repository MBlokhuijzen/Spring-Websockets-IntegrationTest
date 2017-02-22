package nl.mblokhuijzen.springwebsockets.service;

import nl.mblokhuijzen.springwebsockets.beans.GameState;
import nl.mblokhuijzen.springwebsockets.beans.Move;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameService {
    public GameState createGame(UUID uuid) {
        return new GameState(uuid, new int[][] {{1, 1}, {1, 1}}, 0, false);
    }

    public GameState move(UUID uuid, Move move) {
        return new GameState(uuid, new int[][] {{2, 2}, {2, 2}}, 1, false);
    }
}
