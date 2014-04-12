package org.spbelect;

import java.io.*;
import java.util.*;

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
                if (!name.startsWith("10")) {
                    //continue;
                }
                name = name.substring(0, name.indexOf("."));
                int uikId = Integer.parseInt(name);
                List<String> data = new ArrayList<>();

                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uik), "UTF-8"));
                String s2;

                while ((s2 = inUik.readLine()) != null) {
                    int pointIndex = s2.indexOf(".");
                    if (pointIndex > 0 && pointIndex < 3) {
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
            history.add(s);
        }
        Map<Integer, String> modificators = new HashMap<>();
        Collections.sort(finding, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return o1[0].compareTo(o2[0]);
            }
        });
        for (String[] f : finding) {            
            boolean ok = false;
            for (int i = 0; i < history.size(); i++) {
                String h = history.get(i);
                if (h.contains(f[1])) {
                    if (h.contains(f[2])) {
                        ok = true;
                        String code = f[0];
                        int ind = code.indexOf("[");
                        if (ind > 0) {
                            int old = Integer.parseInt(code.substring(0, ind));
                            int newV = Integer.parseInt(code.substring(ind + 1, code.length() - 1));
                            if (h.startsWith(Integer.toString(old))) {
                                String exist = modificators.get(i);
                                exist = exist == null ? Integer.toString(newV) : exist + ":" + Integer.toString(newV);
                                modificators.put(i, exist);
                            }
                        }
                        String[] codes = new String[] {"заместитель", "председатель", "секретарь"};                        
                        for (String c : codes) {
                            if (h.contains(c + "2012") && !f[3].contains(c)) {
                                System.out.println("Missing historical role  " + h);
                            }
                        }
                        if (!h.contains(code) || (!h.contains(f[3]) && !f[3].startsWith("прг"))) {
                            System.out.println("   Missing parts " + code + " " + f[1] + " "  + f[2] + " " + f[3]);
                        }
                        break;
                    }
                }
            }
            if (!ok) {
                System.out.println("Missing completely \n" + f[0] + " " + f[1] + " "  + f[2] + " " + (f[3].startsWith("прг2012") ? "" : f[3]));
            }
        }

        System.out.println();

        for (int i = 0; i < history.size(); i++) {
            String h = history.get(i);
            String mod = modificators.get(i);
            if ("1630:41".equals(mod)) {
                mod = "41:1630";
            }
            if (h.indexOf("[") > 0 && h.indexOf("[") < 10 &&(mod == null || !h.contains(mod))) {
                System.out.println("Error in line " + i + " : " + h);
            }
        }

        for (int i = 0; i < history.size(); i++) {
            String h = history.get(i);
            String mod = modificators.get(i);
            if (mod != null) {
                if (!h.contains("[" + mod)) {
                    int ind = h.indexOf(" ");
                    h = h.substring(0, ind) + "[" + mod + "]" + h.substring(ind);
                }
            }
            System.out.println(h);
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
