package ui;

import model.AuthData;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.RESET_TEXT_ITALIC;
import static ui.EscapeSequences.SET_TEXT_COLOR_WHITE;

public class GameClient {
    private final ServerFacade server;
    private State state = State.SIGNEDIN;
    private State gameState = State.NOGAME;
    private State colorState = State.WHITE;
    private final AuthData authData;

    public GameClient(String serverUrl, AuthData authData) {
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
//                printBoard(colorState);
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
//                case "r", "redraw" -> redraw();
//                case "m", "move" -> makeMove(params);
//                case "h", "highlight" -> highlightMoves();
//                case "r", "resign" -> resign();
//                case "l", "leave" -> leave();
                default -> help();
            };
        } catch (ResponseException ex) {
            String body = ex.getMessage();

            HandleError error = new HandleError(body);

            return error.sendMessage(body);
        }
    }

    public String help() {
        return """
                Options:
                Redraw the board: "r", "redraw"
                Make a move: "m", "move" <START> <END>
                Highlight legal moves: "h", "highlight"
                Resign from a game: "r", "resign"
                Leave a game: "l", "leave"
                Print this message: "h", "help"
                """;
    }

}
