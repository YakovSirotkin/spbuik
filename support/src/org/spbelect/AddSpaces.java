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
                int counter = 1;
                String name = uikFile.getName().substring(3);
                name = name.substring(0, name.indexOf("."));
                int uikId = Integer.parseInt(name);
                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uikFile), "UTF-8"));
                List<String> lines = new ArrayList<>();
                String s2 = null;              
                boolean membersProcessed = false;
                while ((s2 = inUik.readLine()) != null) {
                    if (!s2.endsWith("  ")) {
                        s2 += " ";
                    }
                    if (!s2.endsWith("  ")) {
                        s2 += " ";
                    }
                    if (s2.startsWith("#Дома")) {
                        membersProcessed = true;
                    }
                            
                    if (!membersProcessed) {
                        if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1)) && Character.isDigit(s2.charAt(2))&& Character.isDigit(s2.charAt(3))  && (s2.charAt(4) == '.')) {
                            s2 = counter + s2.substring(4);
                            counter++;
                        } else if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1)) && Character.isDigit(s2.charAt(2))  && (s2.charAt(3) == '.')) {
                            s2 = counter + s2.substring(3);
                            counter++;
                        } else if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1))  && (s2.charAt(2) == '.')) {
                            s2 = counter + s2.substring(2);
                            counter++;
                        } else if (Character.isDigit(s2.charAt(0)) && (s2.charAt(1) == '.')) {
                            s2 = counter + s2.substring(1);
                            counter++;
                        }
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
