import java.util.List;

public class Main {
    public static void main(String[] args) {
        Content content = new Content();
        content.readFile("src/inputs/test_map.txt");

        content.floodFillCustomerHeadquarters();
        List<ReplyOffice> allReplyOffices = content.allReplyOffices();
        content.createVisualisationImages(allReplyOffices);

        //System.out.println("ALLREPLY"+allReplyOffices.size());
        int totalScore = 0;

        for (ReplyOffice ro : allReplyOffices) {
            int x = ro.tileAt.x, y = ro.tileAt.y;
            int totalOfficeGain = 0;
            for (Path path : ro.paths) {
                totalOfficeGain += path.gain;
                System.out.println(String.format("%d %d %s", x, y, path.notatedPath));
            }
            totalScore += totalOfficeGain;
            System.out.println("Final office score = " + totalOfficeGain);
        }

        System.out.println(String.format("\n TOTAL SCORE = %d + %d = %d", totalScore, content.bonus, totalScore + content.bonus));
    }
}
