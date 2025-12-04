package ui;

import model.AuthData;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_BG_COLOR_DARK_GREY;
import static ui.EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
import static ui.EscapeSequences.SET_BG_COLOR_WHITE;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

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
                case "r", "redraw" -> redraw();
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


    public void redraw() {
       printBoard(colorState);
    }


    public void printBoard(State color) {
        String[][] board = {
                {"R","N","B","Q","K","B","N","R"},
                {"P","P","P","P","P","P","P","P"},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {"P","P","P","P","P","P","P","P"},
                {"R","N","B","Q","K","B","N","R"},
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
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    a  b  c  d  e  f  g  h    "
                + RESET_BG_COLOR);
        for (int row = 0; row < 8; row++) {
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " ");

            for (int col = 0; col < 8; col++) {
                String bgColor = (row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
                String piece = board[row][col];

                System.out.print(bgColor + " " + piece + " ");
            }
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " " + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    a  b  c  d  e  f  g  h    "
                + RESET_BG_COLOR);
    }

    public void printBlackBoard(String[][] board) {
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    h  g  f  e  d  c  b  a    "
                + RESET_BG_COLOR);
        for (int row = 7; row >= 0; row--) {
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " ");

            for (int col = 7; col >= 0; col--) {
                String bgColor = (row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
                String piece = board[row][col];

                System.out.print(bgColor + " " + piece + " ");
            }
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + " " + (8-row) + " " + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLUE + "    h  g  f  e  d  c  b  a    "
                + RESET_BG_COLOR);
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
