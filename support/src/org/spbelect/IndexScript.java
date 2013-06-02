package org.spbelect;

import java.io.*;
import java.util.*;

public class IndexScript {
    public static void main(String[] args) throws Exception {
        Map<Integer, Integer> map = new HashMap<>();
        File[] tiks = new File("../spbuik").listFiles(new FilenameFilter() {
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
                map.put(uikId, tikId);
            }
        }
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("index.md"), "UTF-8"));
        List<Integer> uiks = new ArrayList<>(map.keySet());
        Collections.sort(uiks);
        int prev = -1;
        for (Integer uik : uiks) {
            int tik = map.get(uik);
            if (tik != prev) {
                prev = tik;
                out.println("# ТИК " + tik);
            }
            out.println("   [УИК " + uik + "](tik" + tik + "/uik" + uik + ".md)  ");
        }
        out.close();
    }
}
