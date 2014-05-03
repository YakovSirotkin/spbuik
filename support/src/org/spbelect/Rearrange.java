package org.spbelect;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rearrange {
    public static void main(String[] args) throws Exception {
        Map<Integer, Uik> uiksMap = new HashMap<>();
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
                String s2 = null;

                int id = 1;
                boolean stopMembers = false;
                inUik.readLine();
                Uik uik = new Uik();
                uik.file = uikFile;
                List<String> member = null;
                while ((s2 = inUik.readLine()) != null) {
                    if (s2.contains("#Дома")) {
                        stopMembers = true;
                    }
                    if (stopMembers) {
                        uik.other.add(s2);
                    } else {
                        if (s2.startsWith(id + ".")) {
                            member = new ArrayList<>();
                            uik.members.add(member);
                            s2 = s2.substring((id + ".").length()).trim();
                            if (!"".equals(s2.trim())) {
                                member.add(s2);
                            }
                            id++;
                        } else {
                            member.add(s2.trim());
                        }
                    }
                }
                if (uik.members.size() < 8) {
                    throw new RuntimeException("member size is " + uik.members.size() + " " + uikFile.getName());
                }
                inUik.close();
                uiksMap.put(uikId, uik);
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
            Uik uik = uiksMap.get(uikId);
            counter++;

            int pos = page.indexOf("Кем рекомендован в состав комиссии", idFinish);
            String nobr = "<nobr>";
            pos = page.indexOf(nobr, pos);
            String head = null;
            String zam = null;
            String secretary = null;
            List<String> names = new ArrayList<>();
            do {
                pos += nobr.length();
                int end = page.indexOf("</nobr>", pos);
                String name = page.substring(pos, end).trim();
                String td = "<td>";
                pos = page.indexOf(td, end) + td.length();
                end = page.indexOf("</td>", pos);
                String who = page.substring(pos, end);
                if ("Председатель".equals(who) && head == null) {
                    head = name;
                } else if ("Зам.председателя".equals(who) && zam == null) {
                    zam = name;
                } else if ("Секретарь".equals(who) && secretary == null) {
                    secretary = name;
                } else if ("Член".equals(who)) {
                    names.add(name);
                } else {
                    throw new RuntimeException("Unknown role " + who + " in " + uikId);
                }
                pos = page.indexOf(nobr, pos);
            } while (pos > 0);

            
            uik.file.delete();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(uik.file), "UTF-8"));
            out.println("#Состав  ");
            int id = 1;
            if (head != null) {
                List<String> m = getMember(uik, head);
                addTag(m, "председатель2014");
                prinrMember(out, m, id);
                id++;
            } else {
                System.out.println("Нет председателя! " + uikId);
            }
            if (zam != null) {
                List<String> m = getMember(uik, zam);
                addTag(m, "заместитель2014"); 
                prinrMember(out, m, id);
                id++;
            } else {
                System.out.println("Нет заместителя! " + uikId);
            }
            if (secretary != null) {
                List<String> m = getMember(uik, secretary);
                addTag(m, "секретарь2014");
                prinrMember(out, m, id);
                id++;

            } else {
                System.out.println("Нет секретаря! " + uikId);
            }
            for (String name : names) {
                List<String> m = getMember(uik, name);
                prinrMember(out, m, id);
                id++;
            }
            
            out.println();
            for (String s1 : uik.other) {
                out.println(s1.trim() + "  ");
            }
            
            if (uik.members.size() > 0) {
                System.out.println("Members left " + uik.members.size() + " for " + uikId);
            }
            out.close();
            if (counter > 1) {
                //break;
            }
        }
    }

    private static void prinrMember(PrintWriter out, List<String> m, int id) {
        out.println(id + ". " + m.get(0).trim() + "  ");
        for (int i = 1; i < m.size(); i++) {
            String s = m.get(i).trim();
            if (s.length() > 0) {
                out.println("    " + s + "  ");
            }
        }        
    }


    private static void addTag(List<String> member, String tag) {
        if (member.size() < 2) {
            throw new RuntimeException(member.size() + " < 2");
        }
        if (member.size() == 2) {
            member.add(tag);
        } else {
            String tags = member.get(2);
            if (tags.contains(tag)) {
                return;
            }
            if (tags.contains("2012[") || tags.contains("2011[") || tags.contains("дубль[")) {
                member.remove(tags);
                member.add(2, tag + " " + tags);
            } else {
                member.add(2, tag);
            }
        }
    }
    private static List<String> getMember(Uik uik, String name) {
        name = name.replace("Михайлов Игорь Анатольеивч", "Михайлов Игорь Анатольевич");
        name = name.replace("Бычков Дмитирий Леонидович", "Бычков Дмитрий Леонидович");
        name = name.replace("Колесникова Наталья Игорьевна", "Колесникова Наталья Игоревна");
        name = name.replace("Мезенцева Анна Владмиировна", "Мезенцева Анна Владимировна");
        name = name.replace("Зернова Антонина Алксеевна", "Зернова Антонина Алексеевна");
        name = name.replace("Осипова Елена Воадимировна", "Осипова Елена Владимировна");
        name = name.replace("Верещагина Людмила Дмитириевна", "Верещагина Людмила Дмитриевна");
        for (Iterator<List<String>> it = uik.members.iterator(); it.hasNext(); ) {
            List<String> member = it.next();
            if (member.get(0).startsWith(name)) {
                it.remove();
                return member;
            }
        }
        System.out.println("Missing " + name + " " + uik.file.getName());
        return null;
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


    static class Uik {
        List<List<String>> members = new ArrayList<>();
        List<String> other = new ArrayList<>();
        File file;
    }
}
