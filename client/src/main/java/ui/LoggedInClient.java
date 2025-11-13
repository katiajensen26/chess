package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.util.*;

import static ui.EscapeSequences.*;

public class LoggedInClient {
    private final ServerFacade server;
    private State state = State.SIGNEDIN;
    private State gameState = State.NOGAME;
    private State colorState = State.WHITE;
    private final AuthData authData;
    private Map<Integer, Integer> listToGameId = new HashMap<>();

    public LoggedInClient(String serverUrl, AuthData authData) {
        server = new ServerFacade(serverUrl);
        this.authData = authData;
    }

    public void run() {
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            if (state == State.SIGNEDOUT) {
                break;
            }
            if (gameState == State.INGAME) {
                printBoard(colorState);
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
        System.out.print("\n" + RESET_TEXT_ITALIC + "[LOGGED IN]>>> ");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "l", "list" -> listGames();
                case "c", "create" -> createGame(params);
                case "j", "join" -> joinGame(params);
                case "o", "observe" -> watchGame(params);
                case "logout" -> logout();
                default -> help();
            };
        } catch (ResponseException ex) {
            String body = ex.getMessage();

            HandleError error = new HandleError(body);

            return error.sendMessage(body);
        }
    }

    public String listGames() {
        Map<String, List<GameData>> gamesMap = server.listGames(authData);

        List<GameData> games = gamesMap.get("games");
        if (games == null || games.isEmpty()) {
            return "No games created";
        }

        StringBuilder gamesList = new StringBuilder();
        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            gamesList.append(String.format("%d. Game name: %s  White: %s  Black: %s%n",
                    i + 1, game.gameName(), game.whiteUsername(), game.blackUsername()));
            listToGameId.put(i + 1, game.gameID());
        }
        return gamesList.toString();
    }

    public String createGame(String... params) {
        if (params.length != 1) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <GAME NAME>");
        }
        String gameName = params[0];
        server.createGame(authData, gameName);
        gameState = State.NOGAME;
        return String.format("Successfully created game: %s \n List games to see gameID.", gameName);
    }

    public String joinGame(String... params) {
        if (params.length != 2) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <GAME ID> <COLOR>");
        }

        String chosenGame = params[0];
        Integer gameID = pickGame(chosenGame);
        String color = params[1].toUpperCase();

        if (gameID == null) {
            gameState = State.NOGAME;
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Please list games to see game IDs");
        }

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Please pick white or black");
        }

        server.joinGame(authData, color, gameID);
        gameState = State.INGAME;
        if (color.equals("BLACK")) {
            colorState = State.BLACK;
        }
        return String.format("Successfully joined game %s as %s", chosenGame, color);
    }

    public String watchGame(String... params) {
        if (params.length != 1) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <GAME ID>");
        }
        String chosenGame = params[0];
        Integer gameId = pickGame(chosenGame);
        if (gameId == null) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Game doesn't exist.");
        }
        gameState = State.INGAME;
        return String.format("Now observing game %s", chosenGame);
    }

    public String logout() {
        server.logout(authData);
        state = State.SIGNEDOUT;
        gameState = State.NOGAME;
        colorState = State.WHITE;
        return "Successfully logged out.";
    }

    public String help() {
        return """
                Options:
                List current games: "l", "list"
                Create a new game: "c", "create" <GAME NAME>
                Join a game: "j", "join" <GAME ID> <COLOR>
                Observe a game: "o", "observe" <GAME ID>
                Logout: "logout"
                Print this message: "h", "help"
                """;
    }

    public void printBoard(State color) {
        String[][] board = {
                {BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK},
                {BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN,BLACK_PAWN, BLACK_PAWN},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN},
                {WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK},
        };
        System.out.println();
        if (color == State.BLACK) {
            printBlackBoard(board);
        } else {
            printWhiteBoard(board);
        }
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    public void printWhiteBoard(String[][] board) {
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "     a  b   c  d   e  f   g  h    "
                + RESET_BG_COLOR);
        for (int row = 0; row < 8; row++) {
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " ");

            for (int col = 0; col < 8; col++) {
                String bgColor = (row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
                String piece = board[row][col];

                System.out.print(bgColor + piece);
            }
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " " + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "     a  b   c  d   e  f   g  h    "
                + RESET_BG_COLOR);
    }

    public void printBlackBoard(String[][] board) {
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "     h  g   f  e   d  c   b  a    "
                + RESET_BG_COLOR);
        for (int row = 7; row >= 0; row--) {
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " ");

            for (int col = 7; col >= 0; col--) {
                String bgColor = (row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
                String piece = board[row][col];

                System.out.print(bgColor + piece);
            }
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " " + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "     h  g   f  e   d  c   b  a    "
                + RESET_BG_COLOR);
    }

    private Integer pickGame(String gameIdString) {
        int chosenGame;
        try {
            chosenGame = Integer.parseInt(gameIdString);
        } catch (NumberFormatException e) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Game ID must be a number.");
        }

        return listToGameId.get(chosenGame);
    }
}
