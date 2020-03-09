import java.util.ArrayList;
import java.util.List;

public class Path {
    public CustomerHeadquarter customerHeadquarter;
    public Tile toTile;
    public List<Tile> tiles;
    public List<Integer> costs = new ArrayList<>();
    public int totalCost = 0;
    public int gain = 0;
    public String notatedPath;

    public Path(CustomerHeadquarter customerHeadquarter, Tile toTile, List<Tile> tiles) {
        this.customerHeadquarter = customerHeadquarter;
        this.toTile = toTile;
        this.tiles = tiles;

        calculatePathNotation();
        calculateCosts();
    }

    private void calculatePathNotation() {
        StringBuilder sb = new StringBuilder();

        Tile prevTile = toTile;
        for (Tile tile : tiles) {
            if (tile != prevTile) {
                int difX = tile.x - prevTile.x;
                int difY = tile.y - prevTile.y;
                if (difX != 0) sb.append(difX > 0 ? "R" : "L");
                else sb.append(difY > 0 ? "D" : "U");
                prevTile = tile;
            }
        }

        notatedPath = sb.toString();
    }

    private void calculateCosts() {
        for (Tile t : tiles) {
            costs.add(t.cost);
            totalCost += t.cost;
        }
        gain = customerHeadquarter.reward - totalCost;
    }
}
