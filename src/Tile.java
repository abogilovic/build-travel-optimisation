import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tile {
    public int x, y;
    public int cost;
    public double weight;
    public boolean walkable;
    public Map<CustomerHeadquarter, Double> floodsCH = new HashMap<>();

    public Tile(int x, int y, double weight, int cost, List<CustomerHeadquarter> customerHeadquarters) {
        this.x = x;
        this.y = y;
        this.cost = cost;
        this.weight = weight;
        this.walkable = weight > 0;
        for (CustomerHeadquarter ch : customerHeadquarters)
            floodsCH.put(ch, -1.0);
    }

    public Double overlappedFloods(List<CustomerHeadquarter> customerHeadquarters) {
        double ret = 0.0;
        if (customerHeadquarters != null)
            for (CustomerHeadquarter ch : customerHeadquarters)
                ret += floodsCH.get(ch);
        else
            for (Double v : floodsCH.values())
                ret += v;
        return ret;
    }

    public int compareForHcs(Tile t1, List<CustomerHeadquarter> customerHeadquarters) {
        double of1 = overlappedFloods(customerHeadquarters), of2 = t1.overlappedFloods(customerHeadquarters);

        if (of1 >= 0 || of2 >= 0) {
            if (of1 * of2 >= 0) return (int) Math.signum(of2 - of1);
            else return of1 > of2 ? 1 : -1;
        }
        return 0;
    }
}
