package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.*;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_BG_COLOR_DARK_GREY;
import static ui.EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
import static ui.EscapeSequences.SET_BG_COLOR_WHITE;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class GameClient implements NotificationHandler{
    private final ServerFacade server;
    private final WebsocketServerFacade ws;
    private State gameState = State.INGAME;
    private State colorState = State.WHITE;
    private final AuthData authData;
    private ChessGame currentGame;
    private final GameData chessGame;

    public GameClient(String serverUrl, AuthData authData, GameData chessGame, State colorState) {
        server = new ServerFacade(serverUrl);
        ws = new WebsocketServerFacade(serverUrl, this);
        this.authData = authData;
        this.chessGame = chessGame;
        this.colorState = colorState;
        currentGame = chessGame.game();
    }

    public void run() {
        System.out.print(help());
        ws.connect(authData.authToken(), chessGame.gameID());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            if (gameState == State.NOGAME) {
                break;
            }
            printPrompt();
            String line = scanner.nextLine();

            try{
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_WHITE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void printPrompt() {
        System.out.print("\n" + RESET_TEXT_ITALIC + "[IN GAME]>>> ");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "d", "redraw" -> redraw();
                case "m", "move" -> makeMove(params);
                case "h", "highlight" -> highlightMoves(params);
                case "r", "resign" -> resign();
                case "l", "leave" -> leave();
                default -> help();
            };
        } catch (ResponseException ex) {
            String body = ex.getMessage();

            HandleError error = new HandleError(body);

            return error.sendMessage(body);
        }
    }


    public String redraw() {
       printBoard(currentGame, colorState, null);
       return "";
    }

    public String makeMove(String... params) {
        String startPosition = params[0];
        String endPosition = params[1];

        ChessPosition startPos = parsePosition(startPosition, colorState);
        ChessPosition endPos = parsePosition(endPosition, colorState);

        ChessMove requestedMove = new ChessMove(startPos, endPos, null);

        ws.makeMove(authData.authToken(), chessGame.gameID(), requestedMove);
        return "";
    }

    public String resign() {
        ws.resign(authData.authToken(), chessGame.gameID());
        return "";
    }

    public String leave() {
        ws.leave(authData.authToken(), chessGame.gameID());
        gameState = State.NOGAME;
        return "";
    }

    public String highlightMoves(String... params) {
        String position = params[0];

        ChessPosition piecePosition = parsePosition(position, colorState);

        Collection<ChessMove> validMoves = currentGame.validMoves(piecePosition);
        Collection<ChessPosition> highlights = new ArrayList<>();

        for (ChessMove move : validMoves) {
            highlights.add(move.getEndPosition());
        }

        printBoard(currentGame, colorState, highlights);
        return "";
    }


    public void printBoard(ChessGame game, State color, Collection<ChessPosition> highlights) {
        String[][] board = new String[8][8];
        var currentBoard = game.getBoard();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                var piece = currentBoard.getPiece(new ChessPosition(i, j));
                if (piece == null) {
                    board[i-1][j-1] = " ";
                } else {
                    board[i-1][j-1] = pieceSymbol(piece);
                }
            }
        }
        System.out.println();
        if (color == State.BLACK) {
            printBlackBoard(board, highlights);
        } else {
            printWhiteBoard(board, highlights);
        }
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    public String pieceSymbol(ChessPiece piece) {
        String symbol;
        switch (piece.getPieceType()) {
            case PAWN -> symbol = "P";
            case ROOK -> symbol = "R";
            case BISHOP -> symbol = "B";
            case KNIGHT -> symbol = "N";
            case QUEEN -> symbol = "Q";
            case KING -> symbol = "K";
            default -> symbol = "?";
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            symbol = symbol.toLowerCase();
        }
        return symbol;
    }

    public void printWhiteBoard(String[][] board, Collection<ChessPosition> highlights) {
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    a  b  c  d  e  f  g  h    "
                + RESET_BG_COLOR);
        for (int row = 0; row < 8; row++) {
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " ");

            for (int col = 0; col < 8; col++) {
                String bgColor = (row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

                int boardRow = 8 - row;
                int boardColumn = 1 + col;
                ChessPosition currentPos = new ChessPosition(boardRow, boardColumn);
                if (highlights != null && highlights.contains(currentPos)) {
                    bgColor = SET_BG_COLOR_YELLOW;
                }
                String piece = board[row][col];

                System.out.print(bgColor + " " + piece + " ");
            }
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " " + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    a  b  c  d  e  f  g  h    "
                + RESET_BG_COLOR);
    }

    public void printBlackBoard(String[][] board, Collection<ChessPosition> highlights) {
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    h  g  f  e  d  c  b  a    "
                + RESET_BG_COLOR);
        for (int row = 7; row >= 0; row--) {
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " ");

            for (int col = 7; col >= 0; col--) {
                String bgColor = (row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

                int boardRow = row + 3;
                int boardColumn = col + 1;
                ChessPosition currentPos = new ChessPosition(boardRow, boardColumn);
                if (highlights != null && highlights.contains(currentPos)) {
                    bgColor = SET_BG_COLOR_YELLOW;
                }
                String piece = board[row][col];

                System.out.print(bgColor + " " + piece + " ");
            }
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " " + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    h  g  f  e  d  c  b  a    "
                + RESET_BG_COLOR);
    }


    public String help() {
        return """
                Options:
                Redraw the board: "r", "redraw"
                Make a move: "m", "move" <START> <END>
                Highlight legal moves: "h", "highlight" <PIECE POSITION>
                Resign from a game: "r", "resign"
                Leave a game: "l", "leave"
                Print this message: "h", "help"
                """;
    }

    @Override
    public void notify(ServerMessage serverMessage) {

        if (serverMessage instanceof NotificationMessage) {
            System.out.println(SET_TEXT_COLOR_BLUE + ((NotificationMessage) serverMessage).getMessage());
        } else if (serverMessage instanceof LoadGameMessage) {
            currentGame = ((LoadGameMessage) serverMessage).getGame();
            redraw();
        } else if (serverMessage instanceof ErrorMessage) {
            System.out.println(SET_TEXT_COLOR_RED + ((ErrorMessage) serverMessage).getErrorMessage());
        }
        printPrompt();
    }

    public ChessPosition parsePosition(String pos, State colorState) {
        int col = pos.charAt(0) - 'a' + 1;

        int row = Character.getNumericValue(pos.charAt(1));

        if (colorState == State.BLACK) {
            row = 7 - row;
        }

        return new ChessPosition(row, col);
    }
}
