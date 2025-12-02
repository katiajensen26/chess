package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage{
    private final ChessGame chessGame;

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.chessGame = game;
    }

    public ChessGame getGame() {
        return chessGame;
    }
}
