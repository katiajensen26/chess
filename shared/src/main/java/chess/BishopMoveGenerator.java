package chess;


public class BishopMoveGenerator extends SlidingPieceMoveGenerator {

    @Override
    public int[][] getDirections() {
        return new int[][] {{+1, +1}, {+1, -1}, {-1, +1}, {-1, -1}};

    }
}
