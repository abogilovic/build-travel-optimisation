import java.util.ArrayList;
import java.util.List;

public class CustomerHeadquarter {
    public Tile tileAt;
    public int reward;

    //pomocni jer pri ucitavanju
    public int x, y;

    public CustomerHeadquarter(int x, int y, int reward) {
        this.x = x;
        this.y = y;
        this.reward = reward;
    }

    public CustomerHeadquarter(Tile tileAt, int reward) {
        this.tileAt = tileAt;
        this.reward = reward;
    }

    public Path pathToTile(List<List<Tile>> mapTiles, Tile toTile) {
        List<Tile> pathTiles = new ArrayList<>();
        Tile chTile = mapTiles.get(tileAt.y).get(tileAt.x);
        Tile nextTile = toTile;


        while (nextTile != chTile) {
            ArrayList<Tile> tilesAround = new ArrayList<>();
            try {
                tilesAround.add(mapTiles.get(nextTile.y + 1).get(nextTile.x));
            } catch (IndexOutOfBoundsException e) {
            }
            try {
                tilesAround.add(mapTiles.get(nextTile.y - 1).get(nextTile.x));
            } catch (IndexOutOfBoundsException e) {
            }
            try {
                tilesAround.add(mapTiles.get(nextTile.y).get(nextTile.x + 1));
            } catch (IndexOutOfBoundsException e) {
            }
            try {
                tilesAround.add(mapTiles.get(nextTile.y).get(nextTile.x - 1));
            } catch (IndexOutOfBoundsException e) {
            }

            Tile bestNextTile = nextTile;

            for (Tile tAround : tilesAround)
                if (tAround.walkable && tAround.floodsCH.get(this) < bestNextTile.floodsCH.get(this))
                    bestNextTile = tAround;

            if (bestNextTile == nextTile) break;
            nextTile = bestNextTile;
            pathTiles.add(nextTile);
        }

        return new Path(this, toTile, pathTiles);
    }
}
