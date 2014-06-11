package org.spbelect;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfficialCheck {
    public static void main(String[] args) throws Exception {
        Map<Integer, Set<String>> uiksMap = new HashMap<>();
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
                Set<String> names = new HashSet<>();

                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uik), "UTF-8"));
                String s2 = null;
                while ((s2 = inUik.readLine()) != null) {
                    if (s2.contains("#Дома")) {
                        break;
                    }

                    s2 = s2.trim();

                    int pointIndex = s2.indexOf(".");
                    if (pointIndex > 0 && pointIndex < 4) {
                        //System.out.println(s2);
                        s2 = s2.substring(pointIndex + 1).trim();
                        if (s2.contains("19")) {
                            //System.out.println(s2);
                            s2 = s2.substring(0, s2.lastIndexOf(" ")).trim();
                        }
                        names.add(s2);
                    }
                }
                inUik.close();
                uiksMap.put(uikId, names);
            }
        }        
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik/uiklinks.txt")), "UTF-8"));
        String s = null;
        int counter = 0;
        while ((s = in.readLine()) != null) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            String page = getPage(s);
            //System.out.println(page);
            String uikIdPrefix = "<h2>Участковая избирательная комиссия ";
            int idStart = page.indexOf(uikIdPrefix) + uikIdPrefix.length() + 1;
            int idFinish = page.indexOf("</h2>", idStart);
            int uikId = Integer.parseInt(page.substring(idStart, idFinish));
            Set<String> names = uiksMap.get(uikId);
            
            int pos = page.indexOf("Кем рекомендован в состав комиссии", idFinish);            
            String nobr = "<nobr>";
            pos = page.indexOf(nobr, pos);
            int oldCounter = counter;
            do {
                pos += nobr.length();        
                int end = page.indexOf("</nobr>", pos);
                if (end < 0) {
                    System.out.println("Никого нет в УИК " + uikId);
                    names.clear();
                    break;
                }
                
                String name = page.substring(pos, end).trim();
                if (names.contains(name)) {
                    names.remove(name);
                } else {
                    System.out.println("New member:");
                    String td = "<td>";
                    pos = page.indexOf(td, end) + td.length();
                    end = page.indexOf("</td>", pos);
                    String who = page.substring(pos, end);
                    pos = page.indexOf(td, end) + td.length();
                    end = page.indexOf("</td>", pos);
                    String from = page.substring(pos, end);
                    System.out.println(name);
                    System.out.println(who);
                    System.out.println(from);
                    counter++;
                }                       
                pos = page.indexOf(nobr, pos);                
            } while (pos > 0);
            for (String deleted : names) {
                    System.out.println("Удален: \n" + deleted);
                counter++;
            }
            if (counter > oldCounter) {
                System.out.println("uik" + uikId);
                System.out.println();    
            }
            
            //if (counter > 100) { 
           //     break;
           // }
        }        
    }
    
    public static String getPage(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
        Matcher m = p.matcher(con.getContentType());
/* If Content-Type doesn't match this pre-conception, choose default and 
 * hope for the best. */
        String charset = m.matches() ? m.group(1) : "Windows-1251";
        Reader r = new InputStreamReader(con.getInputStream(), charset);
        StringBuilder buf = new StringBuilder();
        while (true) {
            int ch = r.read();
            if (ch < 0)
                break;
            buf.append((char) ch);
        }
        return buf.toString();        
    }
}
