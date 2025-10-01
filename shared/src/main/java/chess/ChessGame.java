package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTeam;
    private ChessBoard board;
    private ChessMove move;
    private ChessPiece piece;

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        return piece.pieceMoves(board, startPosition);
    }

    private void executeMove(ChessMove move) {
        piece = board.getPiece(move.getStartPosition());
        int lastRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        if (board.getPiece(move.getEndPosition()) != null) {
            board.removePiece(move.getEndPosition());
        }

        board.removePiece(move.getStartPosition());
        board.addPiece(move.getEndPosition(), piece);

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getEndPosition().getRow() == lastRow) {
                ChessPiece.PieceType promoType = (move.getPromotionPiece() != null) ? move.getPromotionPiece() : ChessPiece.PieceType.QUEEN;
                board.removePiece(move.getEndPosition());
                board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), promoType));
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("No piece at this position.");
        } else if (piece.getTeamColor() != currentTeam) {
            throw new InvalidMoveException("You can't move this piece.");
        } else if (!validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("You can't move there.");
        } else if (isInCheck(piece.getTeamColor())) {
            throw new InvalidMoveException("The piece is in check.");
        }


        executeMove(move);

        setTeamTurn(currentTeam == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece currentPiece = board.getPiece(new ChessPosition(row, col));
                if (currentPiece != null && currentPiece.getTeamColor() != teamColor) {
                    for (ChessMove move : validMoves(new ChessPosition(row, col))) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return new ChessPosition(row, col);
                }
            }
        }
        throw new RuntimeException("King is missing.");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
