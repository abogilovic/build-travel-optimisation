import org.paukov.combinatorics3.Generator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Content {
    public int width = 0, height = 0, amountCH = 0, amountR = 0, bonus = 0;
    public List<List<Tile>> mapTiles = new ArrayList<>();
    public List<CustomerHeadquarter> customerHeadquarters = new ArrayList<>();

    private Map<Character, Integer> tileCosts = new HashMap<>() {{
        put('#', -1);
        put('~', 800);   //water
        put('*', 200);  //traffic jam
        put('+', 150);  //dirt
        put('X', 120);  //railway level crossing
        put('_', 100);  //standard terrain
        put('H', 70);   //highway
        put('T', 50);    //railway
    }};

    private Map<Integer, Color> tileColors = new HashMap<>() {{
        put(-1, new Color(0, 0, 0));              //not walkable
        put(800, new Color(103, 187, 224));     //water
        put(200, new Color(123, 124, 133));     //traffic jam
        put(150, new Color(156, 116, 75));      //dirt
        put(120, new Color(156, 36, 36));       //railway level crossing
        put(100, new Color(152, 188, 54));      //standard terrain
        put(70, new Color(136, 129, 122));      //highway
        put(50, new Color(230, 63, 52));        //railway
    }};

    //Algorithm

    private double biggestCHReward = 0;

    //Done
    public void floodFillCustomerHeadquarters() {
        for (CustomerHeadquarter customerHeadquarter : customerHeadquarters)
            if (customerHeadquarter.reward > biggestCHReward) biggestCHReward = customerHeadquarter.reward;

        for (CustomerHeadquarter customerHeadquarter : customerHeadquarters) {
            ArrayList<Tile> nextTiles = new ArrayList<>();

            Tile customerTile = customerHeadquarter.tileAt;
            customerTile.floodsCH.replace(customerHeadquarter, 0.0);
            nextTiles.add(customerTile);

            ArrayList<Tile> newNextTiles = new ArrayList<>();

            while (nextTiles.size() > 0) {
                for (int j = 0; j < nextTiles.size(); j++) {
                    Tile t = nextTiles.get(j);
                    if (t.walkable) {
                        ArrayList<Tile> newTiles = new ArrayList<>();
                        try {
                            newTiles.add(mapTiles.get(t.y + 1).get(t.x));
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            newTiles.add(mapTiles.get(t.y - 1).get(t.x));
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            newTiles.add(mapTiles.get(t.y).get(t.x + 1));
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            newTiles.add(mapTiles.get(t.y).get(t.x - 1));
                        } catch (IndexOutOfBoundsException e) {
                        }

                        for (Tile tl : newTiles) {
                            if (tl.walkable) {
                                double flood = t.floodsCH.get(customerHeadquarter) + tl.weight;
                                if (tl.floodsCH.get(customerHeadquarter) < 0 || flood < tl.floodsCH.get(customerHeadquarter)) {
                                    tl.floodsCH.replace(customerHeadquarter, flood);
                                    newNextTiles.add(tl);
                                }
                            }
                        }
                    }
                }
                nextTiles.clear();
                nextTiles.addAll(newNextTiles);
                newNextTiles.clear();
            }

            for (int k = 0; k < mapTiles.size(); k++)
                for (int j = 0; j < mapTiles.get(k).size(); j++) {
                    Tile tile = mapTiles.get(k).get(j);
                    if (tile.walkable)
                        tile.floodsCH.replace(customerHeadquarter, tile.floodsCH.get(customerHeadquarter) * (2.0 - customerHeadquarter.reward / biggestCHReward));
                }
        }
    }

    //Done
    private List<List<CustomerHeadquarter>> allCHGroups(List<CustomerHeadquarter> customerHeadquarters) {
        List<List<CustomerHeadquarter>> chGroups = new ArrayList<>();

        for (int i = 1; i <= customerHeadquarters.size(); i++)
            chGroups.addAll(Generator.combination(customerHeadquarters)
                    .simple(i)
                    .stream()
                    .collect(Collectors.toList()));

        //System.out.println(chGroups.size());
        return chGroups;
    }

    private int groupsClusterGain(List<List<CustomerHeadquarter>> groupsCluster, List<ReplyOffice> fillReplyOffices) {
        boolean getBonus = true;
        for (CustomerHeadquarter ch : customerHeadquarters) {
            boolean chInside = false;
            for (List<CustomerHeadquarter> chGroup : groupsCluster)
                if (chGroup.contains(ch)) {
                    chInside = true;
                    break;
                }
            if (!chInside) {
                getBonus = false;
                break;
            }
        }

        int wholeClusterGain = getBonus ? bonus : 0;

        List<Tile> allTiles = new ArrayList<>();
        for (int i = 0; i < mapTiles.size(); i++)
            allTiles.addAll(mapTiles.get(i));

        for (List<CustomerHeadquarter> chGroup : groupsCluster) {
            allTiles.sort((tile, t1) -> tile.compareForHcs(t1, chGroup));

            boolean foundOfficePlace = false;
            for (int i = 0; !foundOfficePlace; i++) {
                int m = allTiles.size() - 1 - i;
                Tile t = allTiles.get(m);

                foundOfficePlace = true;
                for (CustomerHeadquarter customerHeadquarter : customerHeadquarters)
                    if (t == customerHeadquarter.tileAt) {
                        foundOfficePlace = false;
                        break;
                    }

                if (foundOfficePlace) {
                    ReplyOffice ro = new ReplyOffice(t);
                    if (fillReplyOffices != null)
                        fillReplyOffices.add(ro);
                    List<Path> officePaths = new ArrayList<>();
                    for (CustomerHeadquarter ch : chGroup) {
                        Path path = ch.pathToTile(mapTiles, ro.tileAt);
                        officePaths.add(path);
                        wholeClusterGain += path.gain;
                    }
                    ro.paths = officePaths;
                }
            }

        }

        return wholeClusterGain;
    }

    public List<ReplyOffice> allReplyOffices() {
        List<ReplyOffice> replyOffices = new ArrayList<>();

        List<List<CustomerHeadquarter>> bestGroupsCluster = null;
        int bestGroupsClusterGain = 0;

        for (int N = amountR; N > 0; N--) {
            List<List<CustomerHeadquarter>> bestNGroupsCluster;
            int bestNGroupsClusterGain = 0;

            List<List<List<CustomerHeadquarter>>> chGroupsClusters = Generator.combination(allCHGroups(customerHeadquarters))
                    .simple(N)
                    .stream()
                    .collect(Collectors.toList());

            bestNGroupsCluster = chGroupsClusters.get(0);
            bestNGroupsClusterGain = groupsClusterGain(bestNGroupsCluster, null);

            for (List<List<CustomerHeadquarter>> chGroupsCluster : chGroupsClusters) {
                int grpClusterGain = groupsClusterGain(chGroupsCluster, null);
                if (grpClusterGain > bestNGroupsClusterGain) {
                    bestNGroupsCluster = chGroupsCluster;
                    bestNGroupsClusterGain = grpClusterGain;
                }
            }

            if (bestGroupsCluster == null || bestNGroupsClusterGain > bestGroupsClusterGain) {
                System.out.println("TOTAL NUMBER OF FORMATION COMBINATIONS OF ALL CH GROUP COMBINATIONS = " + chGroupsClusters.size() + " " + N);

                bestGroupsCluster = bestNGroupsCluster;
                bestGroupsClusterGain = bestNGroupsClusterGain;
            } else break;
        }

        //System.out.println("GROUPS CLUSTER"+bestGroupsCluster.size());

        groupsClusterGain(bestGroupsCluster, replyOffices); //dodaje nove ofise unutar replyOffices
        return replyOffices;
    }


    //Visualisation

    //Done
    public void createVisualisationImages(List<ReplyOffice> replyOffices) {
        for (CustomerHeadquarter ch : customerHeadquarters)
            createCHFloodImage(ch);
        createWeightMapImage();
        createGlobalFloodMapImage(customerHeadquarters, replyOffices);
        createRealisticGlobalMapImage(customerHeadquarters, replyOffices);
    }

    //Done
    private void createWeightMapImage() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double weight;
        for (int i = 0; i < mapTiles.size(); i++)
            for (int j = 0; j < mapTiles.get(i).size(); j++) {
                weight = mapTiles.get(i).get(j).weight;
                if (weight < 0) img.setRGB(j, i, new Color(120, 34, 34).getRGB());
                else img.setRGB(j, i, new Color(0, (int) (255 * (1f - weight)), 0).getRGB());
            }

        try {
            File f = new File("src/imgs/WeightMapImage.png");
            ImageIO.write(img, "PNG", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Done
    private void createCHFloodImage(CustomerHeadquarter customerHeadquarter) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double biggestFlood = 0;

        for (int i = 0; i < mapTiles.size(); i++)
            for (int j = 0; j < mapTiles.get(i).size(); j++) {
                double flood = mapTiles.get(i).get(j).floodsCH.get(customerHeadquarter);
                if (flood > biggestFlood) biggestFlood = flood;
            }

        for (int i = 0; i < mapTiles.size(); i++)
            for (int j = 0; j < mapTiles.get(i).size(); j++) {
                if (i == customerHeadquarter.tileAt.y && j == customerHeadquarter.tileAt.x) {
                    img.setRGB(j, i, new Color(255, 255, 0).getRGB());
                } else {
                    int c = 255 - (int) (255 * (mapTiles.get(i).get(j).floodsCH.get(customerHeadquarter) / biggestFlood));
                    if (c >= 0 && c <= 255) img.setRGB(j, i, new Color(0, c, 0).getRGB());
                    else img.setRGB(j, i, new Color(120, 34, 34).getRGB());
                }
            }

        try {
            String path = String.format("src/imgs/CH(%d, %d, %d)flood.png", customerHeadquarter.tileAt.x, customerHeadquarter.tileAt.y, customerHeadquarter.reward);
            File f = new File(path);
            ImageIO.write(img, "PNG", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRealisticGlobalMapImage(List<CustomerHeadquarter> customerHeadquarters, List<ReplyOffice> replyOffices) {
        List<Path> paths = new ArrayList<>();
        replyOffices.forEach(replyOffice -> paths.addAll(replyOffice.paths));

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < mapTiles.size(); i++)
            for (int j = 0; j < mapTiles.get(i).size(); j++) {
                boolean isSpecial = false;
                for (CustomerHeadquarter ch : customerHeadquarters)
                    if (i == ch.tileAt.y && j == ch.tileAt.x) {
                        img.setRGB(j, i, new Color(255, 255, 0).getRGB());
                        isSpecial = true;
                        break;
                    }
                if (!isSpecial)
                    for (ReplyOffice ro : replyOffices)
                        if (i == ro.tileAt.y && j == ro.tileAt.x) {
                            img.setRGB(j, i, new Color(0, 100, 255).getRGB());
                            isSpecial = true;
                            break;
                        }
                if (!isSpecial) img.setRGB(j, i, tileColors.get(mapTiles.get(i).get(j).cost).getRGB());
                label:
                if (!isSpecial)
                    for (Path path : paths)
                        for (Tile tile : path.tiles)
                            if (i == tile.y && j == tile.x) {
                                img.setRGB(j, i, new Color(img.getRGB(j, i)).brighter().brighter().getRGB());
                                //img.setRGB(j, i, new Color(34, 151, 250).getRGB());
                                break label;
                            }
            }

        try {
            File f = new File("src/imgs/RealisticGlobalMap.png");
            ImageIO.write(img, "PNG", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Done
    private void createGlobalFloodMapImage(List<CustomerHeadquarter> customerHeadquarters, List<ReplyOffice> replyOffices) {
        List<Path> paths = new ArrayList<>();
        replyOffices.forEach(replyOffice -> paths.addAll(replyOffice.paths));

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double biggestOverlappedFlood = 0;
        for (int i = 0; i < mapTiles.size(); i++)
            for (int j = 0; j < mapTiles.get(i).size(); j++) {
                double overlappedFlood = mapTiles.get(i).get(j).overlappedFloods(customerHeadquarters);
                if (overlappedFlood > biggestOverlappedFlood) biggestOverlappedFlood = overlappedFlood;
            }

        for (int i = 0; i < mapTiles.size(); i++)
            for (int j = 0; j < mapTiles.get(i).size(); j++) {
                boolean isSpecial = false;
                for (CustomerHeadquarter ch : customerHeadquarters)
                    if (i == ch.tileAt.y && j == ch.tileAt.x) {
                        img.setRGB(j, i, new Color(255, 255, 0).getRGB());
                        isSpecial = true;
                        break;
                    }
                if (!isSpecial)
                    for (ReplyOffice ro : replyOffices)
                        if (i == ro.tileAt.y && j == ro.tileAt.x) {
                            img.setRGB(j, i, new Color(0, 100, 255).getRGB());
                            isSpecial = true;
                            break;
                        }
                label:
                if (!isSpecial)
                    for (Path path : paths)
                        for (Tile tile : path.tiles)
                            if (i == tile.y && j == tile.x) {
                                img.setRGB(j, i, new Color(250, 160, 100).getRGB());
                                isSpecial = true;
                                break label;
                            }
                if (!isSpecial) {
                    int c = (int) (255 * (1 - mapTiles.get(i).get(j).overlappedFloods(customerHeadquarters) / biggestOverlappedFlood));
                    if (c >= 0 && c <= 255) img.setRGB(j, i, new Color(0, c, 0).getRGB());
                    else img.setRGB(j, i, new Color(120, 34, 34).getRGB());
                }
            }

        try {
            File f = new File("src/imgs/GlobalFloodMap.png");
            ImageIO.write(img, "PNG", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Input file

    //Done
    public void readFile(String fileName) {
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);

            int k = 0;
            while (myReader.hasNextLine())
                parseLine(myReader.nextLine(), k++);
            myReader.close();

            for (CustomerHeadquarter ch : customerHeadquarters) {
                ch.tileAt = mapTiles.get(ch.y).get(ch.x);
                bonus += ch.reward;
            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //Done
    private void parseLine(String content, int line) {
        if (line == 0) {
            String[] niz = content.split(" ");
            width = Integer.parseInt(niz[0]);
            height = Integer.parseInt(niz[1]);
            amountCH = Integer.parseInt(niz[2]);
            amountR = Integer.parseInt(niz[3]);
        } else if (line <= amountCH) {
            String[] niz = content.split(" ");
            customerHeadquarters.add(new CustomerHeadquarter(Integer.parseInt(niz[0]), Integer.parseInt(niz[1]), Integer.parseInt(niz[2])));
        } else {
            List<Tile> linija = new ArrayList<>();
            for (int i = 0; i < content.length(); i++)
                linija.add(new Tile(i, line - (amountCH + 1), tileCosts.get(content.charAt(i)) / 800.0, tileCosts.get(content.charAt(i)), customerHeadquarters));
            mapTiles.add(linija);
        }
    }
}
