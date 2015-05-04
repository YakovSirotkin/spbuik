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
        for (File tik : tiks) {
            int tikId = Integer.parseInt(tik.getName().substring(3));
            File[] uiks = tik.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("uik");
                }
            });
            for (File uik : uiks) {
                String name = uik.getName().substring(3);
                name = name.substring(0, name.indexOf("."));
                int uikId = Integer.parseInt(name);
                List<String[]> names = new ArrayList<>();
                                
                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uik), "UTF-8"));                
                String s2 = null;
                while ((s2 = inUik.readLine()) != null) {
                    if (s2.contains("#Дома")) {
                        break;
                    }

                    //if (s2.indexOf(".") > 0) {
                        names.add(new String[]{stringProcess(s2), s2});
                    //}
                }
                inUik.close();
                uiksMap.put(uikId, names);
            }
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik/reserv.txt")), "UTF-8"));
        String s = null;
        int total = 0;
        int found = 0;
        List<String> reserv = new ArrayList<>();
        while ((s = stringProcess(in.readLine())) != null) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            reserv.add(s);
        }

        for (Map.Entry<Integer, List<String[]>> entry : uiksMap.entrySet()) {
            System.out.println("\n uik" + entry.getKey().toString() + ".md");
            List<String[]> value = entry.getValue();
            for (String[] member : value) {
                String name = member[0];
                int point = name.indexOf(".");
                if (point > 0 && point < 3) {
                    name = name.substring(point + 1).trim();
                    if (name.contains(".19")) {
                        continue;
                    }
                    String year = null;
                    if (name.contains("19")) {
                        int yearStart = name.lastIndexOf("19");
                        year = name.substring(yearStart).trim();
                        name = name.substring(0, yearStart).trim();
                    }

                    for (String r : reserv) {
                        if (r.contains(name)) {
                            if (year == null || r.contains(year)) {
                                System.out.println(member[1]);
                                System.out.println(r);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("found = " + found);
        System.out.println("total = " + total);
    }
    
    static String stringProcess(String s) {
        if (s == null) {
            return null;
        }
        return s.toLowerCase().replace("ё", "е");
    }
}
