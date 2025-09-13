package chess;

import java.util.Collection;

public interface MoveGenerator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}
