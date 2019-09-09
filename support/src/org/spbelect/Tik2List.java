package org.spbelect;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Tik2List {
    public static void main(String[] args) throws Exception {
        File[] tiks = new File("spbuik").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("tik");
            }
        });
        List<File> allUiks = new ArrayList<>();
        for (File tik : tiks) {
            int tikId = Integer.parseInt(tik.getName().substring(3));
            if (tikId != 2) {
                //continue;
            }
            File[] uiks = tik.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("uik");
                }
            });

            for (File uik : uiks) {
                allUiks.add(uik);
            }
        }
        allUiks.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                            return new Integer(getUikId(o1)).compareTo(getUikId(o2));
            }
        });
            for (File uik : allUiks) {
                int uikId = getUikId(uik);
                if (uikId != 1744) {
                    //continue;
                }
                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uik), "UTF-8"));
                String s2 = null;
                while ((s2 = inUik.readLine()) != null) {
                    if (s2.contains("#Дома")) {
                        break;
                    }
                    if (s2.contains("#Состав")) {
                        continue;
                    }

                    s2 = s2.trim().replace("\"", "");
                    if ("".equals(s2)) {
                        continue;
                    }
                    int pointIndex = s2.indexOf(".");
                    if (pointIndex > 0 && pointIndex < 4) {
                        System.out.println();
                        s2 = s2.substring(pointIndex + 1).trim();
                        System.out.print(uikId + "," + s2);
                    } else {
                        System.out.print("," + s2.trim());
                    }

                }
                inUik.close();                
            }
        }

    public static int getUikId(File uik) {
        String name = uik.getName().substring(3);
        name = name.substring(0, name.indexOf("."));
        return Integer.parseInt(name);
    }
}


