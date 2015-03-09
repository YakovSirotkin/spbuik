package org.spbelect;

import java.io.*;
import java.util.*;

public class SourceStat {

    public static void main(String[] args) throws Exception {
        String s = null;

        int total = 0;
        SortedMap<String, Integer> stat = new TreeMap<>();
        //PrintWriter uikTab = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("uikTab.csv")), "Cp1251")); 
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
                Set<String> names = new HashSet<>();

                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uik), "UTF-8"));
                String s2 = null;
                while ((s2 = inUik.readLine()) != null) {
                    if (s2.contains("#Дома")) {
                        break;
                    }

                    s2 = s2.trim();

                    String[] prefixes = new String[]{"МО ", "собрание-работа", "собрание-дом", "собрание избирателей по месту службы", "собрание избирателей по месту учебы"}; 
                    int pointIndex = s2.indexOf(".");
                    if (pointIndex > 0 && pointIndex < 4) {
                        String source = inUik.readLine().trim();
                        for (String prefix : prefixes) {
                            if (source.startsWith(prefix)) {
                                source = prefix;
                            }
                        }
                        
                        if (source.trim().length() < 2) {
                            System.out.println("Никто в " + uikId);
                        }
                                
                                
                        total++;
                        if (stat.containsKey(source)) {
                            stat.put(source, stat.get(source) + 1);
                        } else {
                            stat.put(source, 1);
                        }
                    }
                }
                inUik.close();
            }
        }
        List<Map.Entry<String, Integer>> r = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : stat.entrySet()) {
            r.add(entry);
        }
        Collections.sort(r, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue() - o2.getValue();
            }
        });
        for (Map.Entry<String, Integer> entry : r) {
            System.out.println(entry.getValue() + " " + entry.getKey());
        }
        System.out.println("Всего " + r.size() + " источников");

        System.out.println("Официально в составах УИК " + total);
    }
    
}
