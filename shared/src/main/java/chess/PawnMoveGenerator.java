package chess;

import java.util.Collection;
import java.util.HashSet;

public class PawnMoveGenerator implements MoveGenerator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece pawn = board.getPiece(myPosition);

        int direction = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? +1 : -1;
        int[][] capture = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? new int[][]{{+1, +1}, {+1, -1}} : new int[][]{{-1, +1}, {-1, -1}};
        int startRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int lastRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        ChessPiece.PieceType[] promotionTypes = {ChessPiece.PieceType.ROOK, ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP};

        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();

        int goForward1 = currentRow + direction;
        int goForward2 = currentRow + direction*2;

        ChessPosition newPosition1 = new ChessPosition(goForward1, currentCol);
        ChessPosition newPosition2 = new ChessPosition(goForward2, currentCol);

        if (board.getPiece(newPosition1) == null) {
            if (goForward1 == lastRow) {
                for (ChessPiece.PieceType promoType : promotionTypes) {
                    moves.add(new ChessMove(myPosition, newPosition1, promoType));
                }
            } else {
                moves.add(new ChessMove(myPosition, newPosition1, null));
            }

            if (newPosition2.getRow() >= 1 && newPosition2.getRow() <= 8 && board.getPiece(newPosition2) == null && currentRow == startRow) {
                moves.add(new ChessMove(myPosition, newPosition2, null));
            }

        }

        for (int[] cap : capture) {
            int moveRow = currentRow + cap[0];
            int moveCol = currentCol + cap[1];

            if (moveRow >= 1 && moveRow <= 8 && moveCol >= 1 && moveCol <= 8) {

                ChessPosition capPosition = new ChessPosition(moveRow, moveCol);
                ChessPiece target = board.getPiece(capPosition);

                if (target != null && target.getTeamColor() != pawn.getTeamColor()) {
                    if (moveRow == lastRow) {
                        for (ChessPiece.PieceType promoType : promotionTypes) {
                            moves.add(new ChessMove(myPosition, capPosition, promoType));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, capPosition, null));
                    }
                }
            }
        }


        return moves;
    }
}
