package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import static ui.EscapeSequences.*;

public class StarterClient {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private AuthData authData;

    public StarterClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_BLUE + "♕ Welcome to Chess! Type register to start");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            if (state == State.SIGNEDIN) {
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
        if (result.equalsIgnoreCase("quit")) {
            throw new QuitException("Quitting...");
        }
    }


    public void printPrompt() {
        System.out.print("\n" + RESET_TEXT_ITALIC + "[LOGGED OUT]>>> ");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "r", "register" -> register(params);
                case "l", "login" -> login(params);
                case "q", "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            String body = ex.getMessage();

            HandleError error = new HandleError(body);

            return error.sendMessage(body);
        }
    }

    public String register(String... params) {
        if (params.length != 3) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
        }
        String username = params[0];
        String password = params[1];
        String email = params[2];
        var user = new UserData(username, password, email);
        this.authData = server.register(user);
        state = State.SIGNEDIN;
        return "Successfully registered!\n";
    }

    public String login(String... params) {
        if (params.length != 2) {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Expected: <USERNAME> <PASSWORD>");
        }

        String username = params[0];
        String password = params[1];
        var user = new UserData(username, password, null);
        this.authData = server.login(user);
        state = State.SIGNEDIN;
        return "Successfully logged in!\n";
    }

    public String help() {
        return SET_TEXT_COLOR_WHITE + """
                Options:
                Login as existing user: "l", "login" <USERNAME> <PASSWORD>
                Register a new user: "r", "register" <USERNAME> <PASSWORD> <EMAIL>
                Exit the program: "q", "quit"
                Print this message: "h", "help"
                """;
    }

    public AuthData getAuthData() {
        if (state == State.SIGNEDIN) {
            return authData;
        } else {
            throw new ResponseException(ResponseException.StatusCode.BadRequest, "Error: not logged in.");
        }
    }
}
