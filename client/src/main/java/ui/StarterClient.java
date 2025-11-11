package ui;

import model.UserData;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;
import static ui.EscapeSequences.*;

public class StarterClient {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public StarterClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to Chess! Type help to start");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit") || !result.equals("q")) {
            printPrompt();
            String line = scanner.nextLine();

            try{
                eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }


    public void printPrompt() {
        System.out.print("\n" + RESET_TEXT_ITALIC + ">>>" + SET_BG_COLOR_GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "r", "register" -> register(params);
                case "l", "login" -> login(params);
            }
        }
    }

    public void register(String... params) {
        if (params.length >= 1) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            var user = new UserData(username, password, email);
            server.register(user);
            state = State.SIGNEDIN;
        }
    }

    public void login(String... params) {
        if (params.length >= 1) {
            String username = params[0];
            String password = params[1];
            var user = new UserData(username, password, null);
            server.login(user);
            state = State.SIGNEDIN;
        }
    }

    public String help() {
        return """
                Options:
                Login as existing user: "l", "login" <USERNAME> <PASSWORD>
                Register a new user: "r", "register" <USERNAME> <PASSWORD> <EMAIL>
                Exit the program: "q", "quit"
                Print this message: "h", "help"
                """;
    }
}
