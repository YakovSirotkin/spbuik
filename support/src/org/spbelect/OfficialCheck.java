package org.spbelect;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfficialCheck {
    static boolean checkOrder = true;
    
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
                String last = s2;
                while ((s2 = inUik.readLine()) != null) {
                    if (s2.contains("#Дома")) {
                        break;
                    }

                    s2 = s2.trim();

                    int pointIndex = s2.indexOf(".");
                    if (pointIndex > 0 && pointIndex < 4) {
                        //System.out.println(s2);
                        if (!checkOrder) {
                            s2 = s2.substring(pointIndex + 1).trim();
                        }
                        if (s2.contains("19")) {
                            //System.out.println(s2);
                            s2 = s2.substring(0, s2.lastIndexOf(" ")).trim();
                        }
                        names.add(s2);
                        last = s2;
                    }
                    if (s2.contains("2014")) {
                        names.remove(last);
                        int endIndex = s2.indexOf("2014");
                        names.add(last + " " + s2.substring(0, endIndex));
                    }
                    
                }
                inUik.close();
                uiksMap.put(uikId, names);
            }
        }
        List<String> uikLinks = GetUikLinks.getUikLinks();
        int changed = 0;
        int total = 0;
        for (String uikLink : uikLinks) {
            String page = getPage(uikLink);
            //System.out.println(uikLink);
            String uikIdPrefix = "<h2>Участковая избирательная комиссия ";
            int idStart = page.indexOf(uikIdPrefix) + uikIdPrefix.length() + 1;
            int idFinish = page.indexOf("</h2>", idStart);
            int uikId = Integer.parseInt(page.substring(idStart, idFinish).replace("\"Д.М. Карбышева\"", "").trim());
            Set<String> names = uiksMap.get(uikId);
            if (names == null) {
                System.out.println("Add uik " + uikId);
            }
            int pos = page.indexOf("Кем рекомендован в состав комиссии", idFinish);            
            String nobr = "<nobr>";
            pos = page.indexOf(nobr, pos);
            int oldCounter = changed;
            do {
                pos += nobr.length();        
                int end = page.indexOf("</nobr>", pos);
                if (end < 0) {
                    System.out.println("Никого нет в УИК " + uikId);
                    names.clear();
                    break;
                }
                int prevClose = page.lastIndexOf("</td>", pos);
                int prevOpen = page.lastIndexOf("<td>", prevClose);
                String id = page.substring(prevOpen + 4, prevClose).trim();
                
                String name = checkOrder ? id + ". " : "";
                name += page.substring(pos, end).trim();
                total++;
                String td = "<td>";
                pos = page.indexOf(td, end) + td.length();
                end = page.indexOf("</td>", pos);
                String who = page.substring(pos, end);
                pos = page.indexOf(td, end) + td.length();
                end = page.indexOf("</td>", pos);
                String from = page.substring(pos, end);

                if (from.contains("\"ЕДИНАЯ РОССИЯ\"")) {
                    from = "    ЕР";
                }
                if (from.contains("ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ")) {
                    from = "    ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ";
                }
                if (from.contains("СПРАВЕДЛИВАЯ РОССИЯ")) {
                    from = "    СР";
                }
                if (from.contains("КОММУНИСТИЧЕСКАЯ ПАРТИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ")) {
                    from = "    КПРФ";
                }
                if (from.contains("Либерально-демократическая партия России")) {
                    from = "    ЛДПР";
                }

                if (who.equals("Председатель")) {
                    who = "    председатель2014";
                    name += " председатель";
                }
                if (who.equals("Зам.председателя")) {
                    who = "    заместитель2014";
                    name += " заместитель";
                }
                if (who.equals("Секретарь")) {
                    who = "    секретарь2014";
                    name += " секретарь";
                }                


                if (names.contains(name)) {
                    names.remove(name);
                } else {
                    if (changed == oldCounter) {
                        System.out.println("New members:");
                    }
                    if (who.contains("2014")) {
                        name = name.substring(0, name.lastIndexOf(" ")).trim();
                    }
                    if (!name.contains(".")) {
                        name = id + ". " + name;
                    }
                        
                    System.out.println(name);
                    if (!from.startsWith("    ")) {
                        from = "    " + from;
                    }
                    System.out.println(from);
                    if (!who.contains("Член")) {
                        System.out.println(who);
                    }
                    changed++;
                }                       
                pos = page.indexOf(nobr, pos);                
            } while (pos > 0);
            for (String deleted : names) {
                    System.out.println("Удален: \n" + deleted);
                changed++;
            }
            if (changed > oldCounter) {
                System.out.println("uik" + uikId);
                System.out.println();    
            }
            uiksMap.remove(uikId);
            //if (changed > 100) { 
           //     break;
           // }
        }
        for (Integer uikId : uiksMap.keySet()) {
            System.out.println("Missing data for uik " + uikId);
        }
        System.out.println("Официально в составах УИК " + total);
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
