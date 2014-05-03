package org.spbelect;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddSpaces {

    public static void main(String[] args) throws Exception {        
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
            for (File uikFile : uiks) {
                String name = uikFile.getName().substring(3);
                name = name.substring(0, name.indexOf("."));
                int uikId = Integer.parseInt(name);
                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uikFile), "UTF-8"));
                List<String> lines = new ArrayList<>();
                String s2 = null;                
                while ((s2 = inUik.readLine()) != null) {
                    if (!s2.endsWith("  ")) {
                        s2 += " ";
                    }
                    if (!s2.endsWith("  ")) {
                        s2 += " ";
                    }
                    lines.add(s2);
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
