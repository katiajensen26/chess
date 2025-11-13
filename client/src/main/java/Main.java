import chess.*;
import model.AuthData;
import ui.LoggedInClient;
import ui.StarterClient;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        try {
            StarterClient starterClient = new StarterClient(serverUrl);
            starterClient.run();
            AuthData authData = starterClient.getAuthData();
            new LoggedInClient(serverUrl, authData).run();
            new StarterClient(serverUrl).run();
        } catch (Throwable e) {
            System.out.printf("Unable to start server: %s%n", e.getMessage());
        }
    }
}