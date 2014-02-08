package org.spbelect;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddAdress {

    public static void main(String[] args) throws Exception {
        Map<Integer, File> uiksMap = new HashMap<>();
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
                uiksMap.put(uikId, uik);
            }
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("adress.txt")), "UTF-8"));
        String s = null;
        while ((s = in.readLine()) != null) {
            int ind = s.indexOf(" ");
            if (ind > 0) {
                int uikId = Integer.parseInt(s.substring(0, ind));
                File uikFile = uiksMap.get(uikId);
                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uikFile), "UTF-8"));
                List<String> lines = new ArrayList<>();
                String s2 = null;
                boolean added = false;
                while ((s2 = inUik.readLine()) != null) {
                    lines.add(s2);
                    if (s2.trim().isEmpty() && !added) {
                        lines.add("#Дома  ");
                        lines.add(s.substring(ind + 1));
                        lines.add("");
                        added = true;
                    }
                }
                if (!added) {
                    System.out.println("Missing address space for uik " + uikId);
                }

                uikFile.delete();
                PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(uikFile), "UTF-8"));
                for (String line : lines) {
                    out.println(line);
                }
                out.close();
            }
        }

    }
}
