import chess.*;
import model.AuthData;
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
                        running = false;
                    } else {
                        new LoggedInClient(serverUrl, authData).run();
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