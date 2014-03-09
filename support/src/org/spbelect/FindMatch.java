package org.spbelect;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindMatch {

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
                while ((s2 = inUik.readLine()) != null) {
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
        while ((s = in.readLine()) != null) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            String[] data = s.split(" ");
            if (data[0].contains("[")) {
                continue;
            }            
            boolean justPrint = false;
            for (Map.Entry<Integer, List<String>> entry : uiksMap.entrySet()) {
                for (String target : entry.getValue()) {
                    if (target.contains(".") || target.contains("#")) {
                        justPrint = false;
                    }
                    if (justPrint) {
                        System.out.println(target);
                        continue;
                    }
                    int score = 0;
                    for (int i = 1; i <= 4; i++) {
                        if (i > data.length) {
                            continue;
                        }
                        String word = data[i];
                        int lastPoint = word.lastIndexOf(".");
                        if (lastPoint > 0) {
                            word = word.substring(lastPoint + 1);
                        }
                        if (target.indexOf(word) > 0) {
                            score++;
                        }
                    }
                    if (score > 3) {
                        System.out.println();
                        System.out.println(s);
                        System.out.println("New uik" + entry.getKey() + " " + target);
                        justPrint = true;
                    }
                }
            }
        }

    }
}
