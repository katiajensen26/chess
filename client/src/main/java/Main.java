import chess.*;
import model.AuthData;
import ui.GameClient;
import ui.LoggedInClient;
import ui.QuitException;
import ui.StarterClient;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        boolean running = true;

        while (running) {
            try {
                try {
                    StarterClient starterClient = new StarterClient(serverUrl);
                    starterClient.run();
                    AuthData authData = starterClient.getAuthData();
                    if (authData == null) {
                        break;
                    }
                    boolean loggedIn = true;
                    String next = "";
                    while (loggedIn) {
                        LoggedInClient loggedInClient = new LoggedInClient(serverUrl, authData);
                        next = loggedInClient.run();
                        if (next.equals("GAME")) {
                            GameClient gameClient = new GameClient(serverUrl, authData);
                            gameClient.run();
                        } else if (next.equals("LOGOUT")) {
                            loggedIn = false;
                        }
                    }

                } catch (QuitException x) {
                    running = false;
                }
            } catch (Throwable e) {
                System.out.printf("Unable to start server: %s%n", e.getMessage());
            }
        }
    }
}