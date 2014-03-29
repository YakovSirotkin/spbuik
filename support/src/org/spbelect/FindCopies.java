package org.spbelect;

import java.io.*;
import java.util.*;

public class FindCopies {
    public static void main(String[] args) throws Exception {
        File[] tiks = new File("spbuik").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("tik");
            }
        });
        List<String[]> finding = new ArrayList<String[]>();
        for (File tik : tiks) {
            String tikName = tik.getName();
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
                    int pointIndex = s2.indexOf(".");
                    if (pointIndex > 0 && pointIndex < 3) {
                        processMember(finding, data, uikId, tikName);
                        data.clear();
                        data.add(s2);
                    } else {
                        data.add(s2);
                    }
                    if (s2.startsWith("#Дома")) {
                        break;
                    }
                }
                processMember(finding, data, uikId, tikName);
                inUik.close();
            }
        }
        int n = 1;
        for (int i = 0; i < finding.size(); i++) {
            String[] m1 = finding.get(i);
            for (int j = i + 1; j < finding.size(); j++) {
                String[] m2 = finding.get(j);                
                if (m1[0].equals(m2[0]) && last4(m1[2]).equals(last4(m2[2]))) {
                    System.out.println("Совпадение " + n++ + ":");
                    printMember(m1);
                    printMember(m2);
                    System.out.println();
                }                
            }
        }
    }

    private static void printMember(String[] m) {
        System.out.println(m[1] + " " + m[0] + " " + m[2]);
        for (int k = 3; k < m.length; k++) {
            String s = m[k].trim();
            if (s.startsWith("#Дома")) {
                break;
            }                    
            if (s.length()>0) {
                System.out.println(s);
            }
        }
    }

    private static String last4(String s) {
        return s.substring(s.length() - 4);
    }

    public static void processMember(List<String[]> finding, List<String> data, int uikId, String tik) {
        String name = data.get(0).trim();
        String[] s = new String[data.size() + 3];
        s[1] = Integer.toString(uikId);
        int beforeYear = name.lastIndexOf(" ");
        int pointIndex = name.indexOf(".");
        if (pointIndex < 0 || pointIndex > 4) {
            return;
        }
        s[0] = name.substring(pointIndex + 2, beforeYear);
        s[2] = name.substring(beforeYear).trim();
        for (int i = 3; i < s.length - 1; i++) {
            s[i] = data.get(i - 2);
        }
        s[s.length - 1] = "https://github.com/YakovSirotkin/spbuik/blob/master/" + tik + "/uik" + uikId + ".md";
        finding.add(s);

    }
}
