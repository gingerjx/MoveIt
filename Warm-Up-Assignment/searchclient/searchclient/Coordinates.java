package searchclient;

import java.util.Objects;

public class Coordinates {
    int row;
    int col;

    public Coordinates(int x, int y) {
        this.row = x;
        this.col = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}