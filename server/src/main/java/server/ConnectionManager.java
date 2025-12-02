package server;

import org.eclipse.jetty.websocket.api.Session;
import websocket.*;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    public final Map<Integer, Map<Session, String>> connections = new ConcurrentHashMap<>();

    public void add(int gameId, Session session, String username) {
        Map<Session, String> sessions;
        if (connections.containsKey(gameId)) {
            sessions = connections.get(gameId);
        } else {
            sessions = new ConcurrentHashMap<>();
            connections.put(gameId, sessions);
        }
        sessions.put(session, username);
    }

    public void remove(int gameId, Session session) {
        Map<Session, String> sessions;
        if (connections.containsKey(gameId)) {
            sessions = connections.get(gameId);
            sessions.remove(session);
            if (sessions.isEmpty()) {
                connections.remove(gameId);
            }
        }
    }

    public void broadcast(Session excludeSession, ServerMessage message, int gameId) throws IOException {
        Map<Session,String> sessions;
        sessions = connections.get(gameId);
        if (sessions.isEmpty()) return;
        String msg = message.toString();
        for (Session c : sessions.keySet()) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        }

    }
}
