package ui;

import chess.*;
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
        if (params.length != 2) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <START> <END>");
        }
        String startPosition = params[0];
        String endPosition = params[1];

        ChessPiece.PieceType promotion = null;

        ChessPosition startPos = parsePosition(startPosition, colorState);
        if (startPos == null) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Try again.");
        }
        ChessPosition endPos = parsePosition(endPosition, colorState);

        if (endPos == null) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Try again.");
        }

        var currentBoard = currentGame.getBoard();
        ChessPiece startPiece = currentBoard.getPiece(startPos);

        boolean whitePromotion = (startPiece.getPieceType() == ChessPiece.PieceType.PAWN
                && startPiece.getTeamColor() == ChessGame.TeamColor.WHITE
                && endPos.getRow() == 8);
        boolean blackPromotion = (startPiece.getPieceType() == ChessPiece.PieceType.PAWN
                && startPiece.getTeamColor() == ChessGame.TeamColor.BLACK
                && endPos.getRow() == 1);

        if (whitePromotion || blackPromotion) {
            System.out.println("Promote pawn? Pick Q, R, B, N");
            Scanner scanner = new Scanner(System.in);
            String promotePick = scanner.nextLine().toUpperCase();

            switch (promotePick) {
                case "Q" -> promotion = ChessPiece.PieceType.QUEEN;
                case "R" -> promotion = ChessPiece.PieceType.ROOK;
                case "B" -> promotion = ChessPiece.PieceType.BISHOP;
                case "N" -> promotion = ChessPiece.PieceType.KNIGHT;
                default -> {
                    System.out.println("I don't recognize that option. Defaulting to Queen.");
                    promotion = ChessPiece.PieceType.QUEEN;
                }
            }
        }
        ChessMove requestedMove = new ChessMove(startPos, endPos, promotion);

        ws.makeMove(authData.authToken(), chessGame.gameID(), requestedMove);
        return "";
    }

    public String resign() {
        System.out.println("Are you sure you want to resign? (Y or N)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine().toUpperCase();

        if (answer.equals("Y")) {
            ws.resign(authData.authToken(), chessGame.gameID());
        } else if (answer.equals("N")) {
            return "Then the game will continue.";

        } else {
            return "If you want to resign, try the command again and enter Y or N.";
        }
        return "";
    }

    public String leave() {
        ws.leave(authData.authToken(), chessGame.gameID());
        gameState = State.NOGAME;
        return "";
    }

    public String highlightMoves(String... params) {
        if (params.length != 1) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <PIECE POSITION>");
        }
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
        for (int row = 7; row >= 0; row--) {
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (1+row) + " ");
            printRows(board, highlights, row, 0, 8, 1);
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (1+row) + " " + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    a  b  c  d  e  f  g  h    "
                + RESET_BG_COLOR);
    }

    public void printBlackBoard(String[][] board, Collection<ChessPosition> highlights) {
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    h  g  f  e  d  c  b  a    "
                + RESET_BG_COLOR);
        for (int row = 0; row < 8; row++) {
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (1+row) + " ");
            printRows(board, highlights, row, 7, -1, -1);
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (1+row) + " " + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    h  g  f  e  d  c  b  a    "
                + RESET_BG_COLOR);
    }


    public String help() {
        return """
                Options:
                Redraw the board: "d", "redraw"
                Make a move: "m", "move" <START> <END>
                Highlight legal moves: "h", "highlight" <PIECE POSITION>
                Resign from a game: "r", "resign"
                Leave a game: "l", "leave"
                Print this message: "h", "help"
                **All positions must be entered with a lowercase letter and a number. Ex: e2**
                """;
    }

    @Override
    public void notify(ServerMessage serverMessage) {

        if (serverMessage instanceof NotificationMessage) {
            System.out.println("\n" + SET_TEXT_COLOR_BLUE + ((NotificationMessage) serverMessage).getMessage() + RESET_TEXT_COLOR);
        } else if (serverMessage instanceof LoadGameMessage) {
            currentGame = ((LoadGameMessage) serverMessage).getGame();
            redraw();
        } else if (serverMessage instanceof ErrorMessage) {
            System.out.println("\n" + SET_TEXT_COLOR_RED + ((ErrorMessage) serverMessage).getErrorMessage() + RESET_TEXT_COLOR);
        }
        printPrompt();
    }

    public ChessPosition parsePosition(String pos, State colorState) {
        int col = pos.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(pos.charAt(1));

        return new ChessPosition(row, col);
    }

    private void printRows(String[][] board, Collection<ChessPosition> highlights, int row, int startCol, int endCol, int colStep) {
        for (int col = startCol; col != endCol; col += colStep) {
            String bgColor = (row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
            ChessPosition currentPos = highlightSquares(row, col);
            if (highlights != null && highlights.contains(currentPos)) {
                bgColor = SET_BG_COLOR_YELLOW;
            }
            String piece = board[row][col];

            System.out.print(bgColor + " " + piece + " ");
        }
    }

    public ChessPosition highlightSquares(int row, int col) {
        int boardRow = row + 1;
        int boardCol = col + 1;
        ChessPosition currentPos = new ChessPosition(boardRow, boardCol);

        return currentPos;
    }
}
