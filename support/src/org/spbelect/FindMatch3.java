package org.spbelect;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindMatch3 {

    public static void main(String[] args) throws Exception {
        Map<Integer, List<String>> uiksMap = new HashMap<>();
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
                List<String> names = new ArrayList<>();
                                
                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uik), "UTF-8"));                
                String s2 = null;
                while ((s2 = stringProcess(inUik.readLine())) != null) {
                    if (s2.contains("#Дома")) {
                        break;
                    }

                    //if (s2.indexOf(".") > 0) {
                        names.add(s2);
                    //}
                }
                inUik.close();
                uiksMap.put(uikId, names);
            }
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik/uik2012.txt")), "UTF-8"));
        String s = null;
        int total = 0;
        int found = 0;
        
        while ((s = stringProcess(in.readLine())) != null) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            total++;
            String[] data = s.split(" ");
            if (data[0].contains("[")) {
                found++;
                continue;                
            }

            String familyName = data[1];
            String name = " " + data[2].charAt(0);
            String name2 = " " + data[3].charAt(0);
                    
            Map<Integer, String> match = new HashMap<>();
            for (Map.Entry<Integer, List<String>> entry : uiksMap.entrySet()) {
                for (String target : entry.getValue()) {
                    int ind = target.indexOf(name);
                    if (target.contains(familyName) && ind > 5 && target.substring(ind + 1).contains(name2)) {
                        match.put(entry.getKey(), target);
                    }
                }
            }
            if (match.size() > 0 && match.size() < 3) {
                System.out.println(s);
                for (Map.Entry<Integer, String> entry : match.entrySet()) {
                    int uikId = entry.getKey();
                    System.out.println("uik" + uikId + ".md");                    
                    System.out.println(entry.getValue());                   
                }
                System.out.println();
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
