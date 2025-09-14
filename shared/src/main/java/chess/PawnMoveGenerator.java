package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class PawnMoveGenerator implements MoveGenerator{

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessPiece pawn = board.getPiece(myPosition);

        int currentRow = myPosition.getRow();
        int currentColumn = myPosition.getColumn();

        int direction = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? +1 : -1;
        int startRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int lastRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        int goForward1 = currentRow + direction;
        int goForward2 = currentRow + direction * 2;

        ChessPosition newPosition1 = new ChessPosition(goForward1, currentColumn);
        ChessPosition newPosition2 = new ChessPosition(goForward2, currentColumn);

        if (board.getPiece(newPosition1) == null || board.getPiece(newPosition2) == null) {
            if (goForward1 == lastRow) {
                //add promotion logic
            } else {
                ChessMove move = new ChessMove(myPosition, newPosition1, null);
                moves.add(move);
            }

            if (currentRow == startRow) {
                ChessMove move = new ChessMove(myPosition, newPosition2, null);
                moves.add(move);
            } else {
                ChessMove move = new ChessMove(myPosition, newPosition1, null);
                moves.add(move);
            }
        }

        return List.of();
    }
}
