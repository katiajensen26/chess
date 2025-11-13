package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import server.ResponseException;
import server.ServerFacade;

import java.util.*;

import static ui.EscapeSequences.*;

public class LoggedInClient {
    private final ServerFacade server;
    private State state = State.SIGNEDIN;
    private State gameState = State.NOGAME;
    private AuthData authData;

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
        System.out.print("\n" + RESET_TEXT_ITALIC + "[LOGGED IN]>>>");
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

            var map = new Gson().fromJson(body, HashMap.class);

            return map.get("message").toString();
        }
    }

    public String listGames() {
        Map<String, List<GameData>> gamesList = server.listGames(authData);
        return "here's a list";
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
        String gameIdString = params[0];
        String color = params[1].toUpperCase();

        int gameID = Integer.parseInt(gameIdString);
        server.joinGame(authData, color, gameID);
        gameState = State.INGAME;
        return String.format("Successfully joined game %s as %s", gameIdString, color);
    }

    public String watchGame(String... params) {
        if (params.length != 1) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <GAME ID>");
        }
        String gameId = params[0];
        gameState = State.INGAME;
        return String.format("Now observing game %s", gameId);
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

}
