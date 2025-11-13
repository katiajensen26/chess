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

            try {
                var map = new Gson().fromJson(body, HashMap.class);
                return map.get("message").toString();
            } catch (Exception e) {
                return body;
            }
        }
    }

    public String listGames() {
        Map<String, List<GameData>> gamesMap = server.listGames(authData);

        List<GameData> games = gamesMap.get("games");
        if (games == null) {
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
        return String.format("Successfully created game: %s", gameName);
    }

    public String joinGame(String... params) {
        if (params.length != 2) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <GAME ID> <COLOR>");
        }

        String chosenGame = params[0];
        Integer gameID = pickGame(chosenGame);
        String color = params[1].toUpperCase();

        server.joinGame(authData, color, gameID);
        gameState = State.INGAME;
        return String.format("Successfully joined game %s as %s", chosenGame, color);
    }

    public String watchGame(String... params) {
        if (params.length != 1) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <GAME ID>");
        }
        String chosenGame = params[0];
        Integer gameId = pickGame(params[0]);
        gameState = State.INGAME;
        return String.format("Now observing game %s", chosenGame);
    }

    public String logout() {
        server.logout(authData);
        state = State.SIGNEDOUT;
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
