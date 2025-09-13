package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMoveGenerator implements MoveGenerator{


    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{+1, +1}, {+1, -1}, {-1, +1}, {-1, -1}};

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
                ChessMove move = new ChessMove(myPosition, newPosition, null);

                moves.add(move);
            }

        }

        return moves;
    }
}
