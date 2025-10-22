package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class SlidingPieceMoveGenerator implements MoveGenerator{

    public abstract int[][] getDirections();


    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();

        int[][] directions = getDirections();

        for (int[] direction: directions) {
            int moveRow = direction[0];
            int moveColumn = direction[1];

            int currentRow = myPosition.getRow();
            int currentColumn = myPosition.getColumn();

            while (true) {
                currentRow += moveRow;
                currentColumn += moveColumn;

                if (currentRow < 1 || currentRow > 8 || currentColumn < 1 || currentColumn > 8) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(currentRow, currentColumn);
                ChessPiece possiblePiece = board.getPiece(newPosition);
                ChessPiece rook = board.getPiece(myPosition);

                if (possiblePiece == null) {
                    ChessMove move = new ChessMove(myPosition, newPosition, null);
                    moves.add(move);
                    continue;
                } else if (possiblePiece.getTeamColor() != rook.getTeamColor()) {
                    ChessMove move = new ChessMove(myPosition, newPosition, null);
                    moves.add(move);
                    break;

                } else {
                    break;
                }

            }
        }
        return moves;
    }
}
