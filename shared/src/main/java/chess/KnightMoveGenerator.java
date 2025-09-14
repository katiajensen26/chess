package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class KnightMoveGenerator implements MoveGenerator{


    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();

        int[][] directions = {{+2, +1}, {+2, -1}, {-2, +1}, {-2, -1}, {+1, +2}, {+1, -2}, {-1, +2}, {-1, -2}};

        for (int[] direction: directions) {
            int moveRow = direction[0];
            int moveColumn = direction[1];


            int currentRow = myPosition.getRow();
            int currentColumn = myPosition.getColumn();

            int newRow = currentRow + moveRow;
            int newColumn = currentColumn + moveColumn;

            if (newRow < 1 || newRow > 8|| newColumn < 1 || newColumn > 8) {
                continue;
            }

            ChessPosition newPosition = new ChessPosition(newRow, newColumn);
            ChessPiece possiblePiece = board.getPiece(newPosition);
            ChessPiece knight = board.getPiece(myPosition);

            if (possiblePiece == null) {
                ChessMove move = new ChessMove(myPosition, newPosition, null);
                moves.add(move);
            } else if (possiblePiece.getTeamColor() != knight.getTeamColor()) {
                ChessMove move = new ChessMove(myPosition, newPosition, null);
                moves.add(move);
            } else {
                continue;
            }

        }
        return moves;
    }
}
