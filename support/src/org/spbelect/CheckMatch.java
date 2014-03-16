package org.spbelect;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CheckMatch {
    public static void main(String[] args) throws Exception {
        File[] tiks = new File("spbuik").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("tik");
            }
        });
        List<String[]> finding = new ArrayList<String[]>();
        for (File tik : tiks) {
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
                List<String> data = new ArrayList<>();

                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uik), "UTF-8"));
                String s2;

                while ((s2 = inUik.readLine()) != null) {
                    if (s2.indexOf(".") > 0) {
                        processMember(finding, data, uikId);
                        data.clear();
                        data.add(s2);
                    } else {
                        data.add(s2);
                    }
                    if (s2.startsWith("#Дома")) {
                        break;
                    }
                }
                processMember(finding, data, uikId);
                inUik.close();
            }
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik/uik2012.txt")), "UTF-8"));
        List<String> history = new ArrayList<>();
        String s;
        while ((s = in.readLine()) != null) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            history.add(s);
        }
        Collections.sort(finding, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return o1[0].compareTo(o2[0]);
            }
        });
        for (String[] f : finding) {            
            boolean ok = false;
            for (String h : history) {
                if (h.contains(f[1])) {
                    if (h.contains(f[2])) {
                        ok = true;
                        if (!h.contains(f[0]) || (!h.contains(f[2]) && !f[2].startsWith("прг"))) {
                            System.out.println("Missing parts " + f[0] + " " + f[1] + " "  + f[2] + " " + f[3]);
                        }
                        break;
                    }
                }
            }
            if (!ok) {
                System.out.println("Missing completely \n" + f[0] + " " + f[1] + " "  + f[2] + " " + (f[3].startsWith("прг2012") ? "" : f[3]));
            }
        }

    }

    public static void processMember(List<String[]> finding, List<String> data, int uikId) {        
        if (data.size() > 2) {
            String history = data.get(2).trim();
            if (history.contains("2012")) {
                String name = data.get(0).trim();
                String[] s = new String[4];
                s[0] = "[" + uikId + "]";
                int beforeYear = name.lastIndexOf(" ");
                s[1] =  name.substring(name.indexOf(".") + 2, beforeYear);
                s[2] = name.substring(beforeYear  +1);                
                int start = history.indexOf("[");
                if (start > 0) {
                    int end = history.indexOf("]");
                    s[0] = history.substring(start + 1, end) + s[0];
                    s[3] = history.substring(0, start);
                    finding.add(s);
                } else {
                    System.out.println("Missing source " + s);
                }
            }
        }
    }
}
