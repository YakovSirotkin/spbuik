package org.spbelect;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rating2016 {

    public static final String CHAIR_2012_PREFIX = "председатель2012[";
    public static final int STATUS_UNKNOWN = 1;
    public static final int STATUS_CHAIR = 2;
    public static final int STATUS_MEMBER = 3;
    public static final int STATUS_FADE = 4;

    public static void main(String[] args) throws Exception {
        List<String> chairs2012list = FindMatch2015.getLines("spbuik/uik2012.txt");
        Map<Integer, String> chairs2012map = new HashMap<>();
        for (String s : chairs2012list) {
            if (s.contains("председатель2012") && !s.startsWith("nnn")) {
                String[] d = s.split(" ");
                String id = d[0];
                if (id.indexOf("[") > 0) {
                    id = id.substring(0, id.indexOf("["));
                }
                chairs2012map.put(Integer.parseInt(id), s);
            }
        }

        List<String> data = FindMatch2015.getLines("spbuik/results2012.csv");
        Map<Integer, Double> res2012 = new HashMap<>();
        Map<Integer, Integer> status = new HashMap<>();
        Map<Integer, Integer> tiksMap = new HashMap<>();
        int minKnown = 5000;
        for (String s : data) {
            String[] d = s.split(",");
            int tikId = Integer.parseInt(d[0]);
            int uikId = Integer.parseInt(d[1]);
            tiksMap.put(uikId, tikId);
            int votes = Integer.parseInt(d[10]) + Integer.parseInt(d[11]);
            int putin = Integer.parseInt(d[24]);
            double share = 100d * putin / votes;

            if (chairs2012map.containsKey(uikId)) {
                minKnown = Math.min(minKnown, votes);
            }
            if (votes > 100) {
                res2012.put(uikId, share);
                status.put(uikId, STATUS_UNKNOWN);
            } else {
                System.out.println("УИК " + uikId + " только " + votes + " голосов");
            }
        }

        System.out.println("minKnown = " + minKnown);

        File[] tiks = new File("spbuik").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("tik");
            }
        });
        List<Chair> chairs = new ArrayList<>();

        Map<Integer, AddSpaces.UikStaff> allMembers = new HashMap<>();
        int recognized = 0;
        for (File tik : tiks) {
            int tikId = Integer.parseInt(tik.getName().substring(3));
            File[] uiks = tik.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("uik");
                }
            });
            for (File uikFile : uiks) {

                List<String> lines = AddSpaces.file2lines(uikFile);

                AddSpaces.UikStaff uikStaff = AddSpaces.processLines(lines);
                String uikFileName = uikFile.getName();
                int uikId = Integer.parseInt(uikFileName.substring(3, uikFileName.indexOf(".")));
                allMembers.put(uikId, uikStaff);
                for (String[] member : uikStaff.members) {
                    if (member.length > 2) {
                        if (member[2].contains(CHAIR_2012_PREFIX)) {
                            Chair chair = new Chair(uikId, tikId, member);
                            chair.putin = res2012.get(chair.uik2012);
                            chairs.add(chair);
                            String remove = chairs2012map.remove(chair.uik2012);
                            if (status.containsKey(chair.uik2012)) {
                                if (chair.isChair) {
                                    status.put(chair.uik2012, STATUS_CHAIR);
                                } else {
                                    status.put(chair.uik2012, STATUS_MEMBER);
                                }
                                recognized++;
                            } else {
                                System.out.println("Нет официальных результатов по УИК " + chair.uik2012);
                            }

                            if (remove == null) {
                                throw new RuntimeException("Нет председателя 2012 в УИК " + chair.uik2012);
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(chairs, new Comparator<Chair>() {
            @Override
            public int compare(Chair o1, Chair o2) {
                if (o1.putin > o2.putin) {
                    return -1;
                }
                if (o1.putin < o2.putin) {
                    return 1;
                }
                if (o1.tikId > o2.tikId) {
                    return 1;
                }
                return -1;
            }
        });

        int count = 0;
        System.out.println("Оставшиеся председатели:");
        for (Chair chair : chairs) {
            if (chair.isChair) {
                count++;
                System.out.println(count + ". " + chair.toString());
            }
        }

        count = 0;
        System.out.println("Смещённые председатели:");
        for (Chair chair : chairs) {
            if (!chair.isChair) {
                count++;
                System.out.println(count + ". " + chair.toString());
            }
        }

        System.out.println("Ушедшие пердседатели");
        for (Map.Entry<Integer, String> e : chairs2012map.entrySet()) {
            String s = e.getValue();
            System.out.println(s);
            String[] d = s.split(" ");
            String name = d[1] + " " + d[2];
            if (name.contains(".")) {
                name = name.substring(0, name.indexOf("."));
            }
            if (name.length() > 2) {
                if (status.containsKey(e.getKey())) {
                    status.put(e.getKey(), STATUS_FADE);
                    recognized++;
                } else {
                    System.out.println("Нет официальных результатов по УИК " + e.getKey());
                }

                if (false) {
                    for (Map.Entry<Integer, AddSpaces.UikStaff> entry : allMembers.entrySet()) {
                        List<String[]> members = entry.getValue().members;

                        for (String[] member : members) {
                            boolean match = false;
                            for (String s1 : member) {
                                if (s1.toLowerCase().contains(name.toLowerCase())) {
                                    match = true;
                                }
                            }
                            if (match) {
                                System.out.println("match " + entry.getKey() + " " + member[0]);
                                for (int i = 1; i < member.length; i++) {
                                    System.out.println(member[i]);
                                }
                            }
                        }
                    }
                    System.out.println();
                }
            }
        }
        System.out.println("recognized = " + recognized);
        int[][] stat = new int[101][5];

        int min = 100;
        int max = 0;
        for (Map.Entry<Integer, Integer> e : status.entrySet()) {
            int uikId = e.getKey();
            int res = (int) Math.round(res2012.get(uikId));
            min = Math.min(res, min);
            max = Math.max(res, max);
            System.out.println(uikId + "=" + res + " ");
            stat[res][e.getValue()]++;
            stat[res][0]++;
        }

        BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        int start = 10;
        int middle = 220;
        g.setColor(Color.white);
        g.fillRect(0, 0, 1000, 1000);
        g.setColor(Color.black);
        g.drawLine(start, middle, start + 10 * (2 + max - min), middle);
        System.out.println("min = " + min);
        System.out.println("max = " + max);
        System.out.println("total = " + res2012.size());
        for (int i = min; i <= max; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 5; j++) {
                System.out.print(stat[i][j] + " ");
            }
            System.out.println();
            g.setColor(Color.red);
            int x = start + (i - min) * 10;
            int h = stat[i][STATUS_CHAIR] * 5;
            g.fillRect(x, middle - h, 10, h);
            g.setColor(Color.blue);
            int h2 = stat[i][STATUS_MEMBER] * 5;
            g.fillRect(x, middle - h - h2, 10, h2);
            g.setColor(Color.gray);
            int h3 = stat[i][STATUS_UNKNOWN] * 5;
            g.fillRect(x, middle, 10, h3);
            g.setColor(Color.black);
            g.fillRect(x, middle + h3, 10, stat[i][STATUS_FADE] * 5);
        }
        g.setColor(Color.green);
        for (int i = min; i <= max; i++) {
            if (i % 5 == 0) {
                int x = start + (i - min) * 10;
                g.drawLine(x, middle, x + 10, middle);
                g.drawString(Integer.toString(i), x, middle + 13);
            }
        }

        for (int i = -200; i < 600; i += 50) {
            if (i != 0) {
                g.drawLine(start, middle + i, start + 900, middle + i);
                g.drawString(Integer.toString(Math.abs(i)/5), start, middle + i + 13);
            }
        }

        int legendX = 450;
        g.setColor(Color.blue);
        g.fillRect(legendX, middle + 170, 10, 5);
        g.setColor(Color.black);
        g.drawString("Остались в УИК, но не председателями", legendX + 20, middle + 178);
        g.setColor(Color.red);
        g.fillRect(legendX, middle + 220, 10, 5);
        g.setColor(Color.black);
        g.drawString("Остались председателями", legendX + 20, middle + 228);
        g.setColor(Color.gray);
        g.fillRect(legendX, middle + 270, 10, 5);
        g.setColor(Color.black);
        g.drawString("Председатели неизвестны", legendX + 20, middle + 278);
        g.setColor(Color.black);
        g.fillRect(legendX, middle + 320, 10, 5);
        g.setColor(Color.black);
        g.drawString("Ушли из УИК", legendX + 20, middle + 328);

        File output = new File("spbuik/chair2012.png");
        ImageIO.write(image, "png", output);
    }

    public static class Chair {
        int uik2016;
        int uik2012;
        int tikId;
        String name;
        String source;
        String tags;
        String date = "";
        boolean isChair;
        double putin;

        public Chair(int uikId, int tikId,  String[] info) {
            this.uik2016 = uikId;
            this.tikId = tikId;
            name = info[0];
            if (name.contains("19")) {
                String[] words = name.split(" ");
                date = words[words.length -1];
                name = name.substring(0, name.indexOf(date)).trim();
            }
            source = info[1];
            tags = info[2];
            String[] allTags = tags.split(" ");
            isChair = false;
            for (String t : allTags) {
                if ("председатель".equals(t.trim())) {
                    isChair = true;
                }
                if (t.startsWith(CHAIR_2012_PREFIX)) {
                    uik2012 = Integer.parseInt(t.substring(t.indexOf("[") + 1, t.indexOf("]")));
                }
            }
        }

        @Override
        public String toString() {
            return String.format("%.2f", putin) +  "% " + name + ", ТИК " + tikId + ", УИК " + uik2016 + "(" + uik2012 + ") " + tags  + " " + date + ",  " + source;
        }
    }
}
