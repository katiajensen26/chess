package chess;


public class RookMoveGenerator extends SlidingPieceMoveGenerator {

    @Override
    public int[][] getDirections() {
        return new int[][] {{+1, 0}, {-1, 0}, {0, +1}, {0, -1}};

    }
}
