package org.spbelect;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindMatch2015 {

    public static void main(String[] args) throws Exception {
        Map<Integer, List<String[]>> uiksMap = new HashMap<>();
        File[] tiks = new File("spbuik").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("tik");
            }
        });

        int total = 0;
        int found = 0;
        List<String> reserv = getLines("spbuik/reserv.txt");
        List<String> reserv2 = getLines("spbuik/reserv2.txt");

        List<String> members2011 = getLines("spbuik/uik2011.txt");
        List<String> members2012 = getLines("spbuik/uik2012.txt");
        List<String> members2014 = getLines("spbuik/uik2014suggestion.txt");
        for (File tik : tiks) {
            int tikId = Integer.parseInt(tik.getName().substring(3));
            File[] uiks = tik.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("uik");
                }
            });
            for (File uik : uiks) {
                boolean first = true;
                AddSpaces.UikStaff staff = AddSpaces.processLines(AddSpaces.file2lines(uik));
                for (int i = 0; i < staff.members.size(); i++) {
                    String[] member = staff.members.get(i);
                    String name = member[0];
                    if (name.contains(".19")) {
                        continue;
                    }
                    String year = null;
                    if (name.contains("19")) {
                        int yearStart = name.lastIndexOf("19");
                        year = name.substring(yearStart).trim();
                        name = name.substring(0, yearStart).trim();
                    }

                    first = checkReserv(reserv, uik, first, i, member, name, year);
                    first = checkReserv(reserv2, uik, first, i, member, name, year);
                    first = processMemebers(members2011, uik, first, i, member, name, 2011);
                    first = processMemebers(members2012, uik, first, i, member, name, 2012);
                    first = processMemebers(members2014, uik, first, i, member, name, 2014);
                }
            }
        }
        System.out.println("found = " + found);
        System.out.println("total = " + total);
    }

    public static boolean checkReserv(List<String> reserv, File uik, boolean first, int i, String[] member, String nameOrig, String year) {
        String name = stringProcess(nameOrig);
        for (String r : reserv) {
            if (r.contains(name)) {
                if (year == null || r.contains(year)) {
                    if (first) {
                        first = false;
                        System.out.println();
                        System.out.println(uik.getName());
                    }
                    System.out.print((i + 1) + ". ");
                    for (String s1 : member) {
                        System.out.println(s1);
                    }
                    System.out.println(r);
                }
            }
        }
        return first;
    }

    public static boolean processMemebers(List<String> members, File uik, boolean first, int i, String[] member, String nameOrig, int year) {
        String name = stringProcess(nameOrig);
        if (member.length > 2 && member[2].contains(year + "[")) {
            return first;
        }
        for (String r : members) {
            if (r.contains(name)) {
                if (first) {
                    first = false;
                    System.out.println();
                    System.out.println(uik.getName());
                }
                System.out.print((i + 1) + ". ");
                for (String s1 : member) {
                    System.out.println(s1);
                }
                System.out.println("[" + year + "] " + r);
                String[] prefix = new String[] {"председатель", "заместитель", "секретарь"};
                String id = r.split(" ")[0];
                if (id.contains("[")) {
                    id = id.substring(0, id.indexOf("["));
                }
                String tag = "прг" + year + "[" + id + "]";
                for (String s : prefix) {
                    if (r.contains(s + year)) {
                        tag = s + year + "[" + id + "]";
                    }
                }
                System.out.println(tag);
            }
        }
        return first;
    }

    public static List<String> getLines(String pathname) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathname)), "UTF-8"));
        String s;
        List<String> reserv = new ArrayList<>();
        while ((s = stringProcess(in.readLine())) != null) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            reserv.add(s);
        }
        return reserv;
    }

    static String stringProcess(String s) {
        if (s == null) {
            return null;
        }
        return s.toLowerCase().replace("ё", "е");
    }
}
