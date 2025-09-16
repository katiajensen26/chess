package chess;

import java.util.Collection;
import java.util.HashSet;

public class PawnMoveGenerator implements MoveGenerator{

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessPiece pawn = board.getPiece(myPosition);

        int currentRow = myPosition.getRow();
        int currentColumn = myPosition.getColumn();

        int direction = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? +1 : -1;
        int[][] capture = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? new int[][]{{+1, -1}, {+1, +1}} : new int[][]{{-1, -1}, {-1, +1}};
        int startRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int lastRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        int goForward1 = currentRow + direction;
        int goForward2 = currentRow + direction * 2;

        ChessPosition newPosition1 = new ChessPosition(goForward1, currentColumn);
        ChessPosition newPosition2 = new ChessPosition(goForward2, currentColumn);

        if (board.getPiece(newPosition1) == null) {
            if (goForward1 == lastRow) {
                //add promotion logic
            } else {
                ChessMove move = new ChessMove(myPosition, newPosition1, null);
                moves.add(move);
            }

            if (currentRow == startRow && board.getPiece(newPosition2) == null) {
                ChessMove move = new ChessMove(myPosition, newPosition2, null);
                moves.add(move);
            }
//            } else {
//                ChessMove move = new ChessMove(myPosition, newPosition1, null);
//                moves.add(move);
//            }
        }

        for (int[] cap : capture) {
            int moveRow = currentRow + cap[0];
            int moveColumn = currentColumn + cap[1];

            if (currentRow < 1 || currentRow > 8 || currentColumn < 1 || currentColumn > 8) {
                ChessPosition capPosition = new ChessPosition(moveRow, moveColumn);
                ChessPiece target = board.getPiece(capPosition);

                if (target != null && target.getTeamColor() != pawn.getTeamColor()) {
                    if (moveRow == lastRow) {
                        //promotion logic
                    } else {
                        ChessMove move = new ChessMove(myPosition, capPosition, null);
                        moves.add(move);
                    }
                }
            }
        }
        return moves;
    }
}
