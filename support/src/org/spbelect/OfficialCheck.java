package org.spbelect;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfficialCheck {
    static boolean checkOrder = false;

    static final String[] roles = new String[]{"председатель", "заместитель", "секретарь"};

    public static void main(String[] args) throws Exception {
        Map<Integer, String[]> ikmos = new HashMap<>();
        String s = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\projects\\spbuik\\spbuik\\ikmo\\ikmoDistrict.csv")), "UTF-8"));
        while ((s = in.readLine()) != null) {
            String[] d = s.split(",");
            ikmos.put(Integer.parseInt(d[2]), d);
        }

        //PrintWriter uikTab = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("uikTab.csv")), "Cp1251")); 
        Map<Integer, Set<String>> uiksMap = new HashMap<>();
        Map<Integer, Integer> tiksMap = new HashMap<>();
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

                List<String> lines = AddSpaces.file2lines(uik);
                String last = null;

                for (String s2 : lines) {
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
                    String[] tags = s2.split(" ");
                    for (String tag : tags) {
                        for (String role : roles) {
                            if (role.equals(tag.trim())) {
                                names.remove(last);                                
                                names.add(last + " " + tag);
                            }
                        }
                    }                    
                }

                uiksMap.put(uikId, names);
                tiksMap.put(uikId, tikId);
            }
        }
        int total = 0;
        for (int tikId = 1; tikId < 31; tikId++) {
            if (tikId != 6) {
                //continue;
            }

            List<String> uikLinks = GetUikLinks.getUikLinks(tikId);
            int changed = 0;
            for (String uikLink : uikLinks) {
                String page = getPage(uikLink);
                //System.out.println(uikLink);
                String uikIdPrefix = "<h2>Участковая избирательная комиссия ";
                int idStart = page.indexOf(uikIdPrefix) + uikIdPrefix.length() + 1;
                int idFinish = page.indexOf("</h2>", idStart);
                int uikId = Integer.parseInt(page.substring(idStart, idFinish).replace("\"Д.М. Карбышева\"", "").trim());
                if (uikId != 1793) {
                    //continue;
                }
                Set<String> names = uiksMap.get(uikId);
                if (names == null) {
                    System.out.println("Add uik " + uikId);
                }
                int pos = page.indexOf("Кем рекомендован в состав комиссии", idFinish);
                String nobr = "<nobr>";
                pos = page.indexOf(nobr, pos);
                int oldCounter = changed;
                List<String[]> newMembers = new ArrayList<>();
                List<String> deletedMembers = new ArrayList<>();

                boolean noInfo = true;
                do {
                    pos += nobr.length();
                    int end = page.indexOf("</nobr>", pos);
                    if (end < 0) {
                        System.out.println("Никого нет в УИК " + uikId);
                        names.clear();
                        break;
                    }
                    noInfo = false;

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

                    final String originalWho = who;
                    final String originalFrom = from;
                    final String originalName = name;

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
                        who = "    председатель";
                        name += " председатель";
                    }
                    if (who.equals("Зам.председателя")) {
                        who = "    заместитель";
                        name += " заместитель";
                    }
                    if (who.equals("Секретарь")) {
                        who = "    секретарь";
                        name += " секретарь";
                    }

                    String[] is = ikmos.get(uikId);
                    if (is == null) {
                        is = new String[]{"-", "-"};
                    }
                    //uikTab.println(uikId + "\t" + tiksMap.get(uikId) + "\t" + is[0] + "\t" + is[1] + "\t" + originalName + "\t" + originalWho + "\t" + originalFrom);
                    if (names.contains(name)) {
                        names.remove(name);
                    } else {
                        if (changed == oldCounter) {
                            System.out.println("New members:");
                        }

                        if (!name.contains(".")) {
                            name = id + ". " + name;
                        }

                        System.out.println(name);
                        if (!from.startsWith("    ")) {
                            from = "    " + from;
                        }
                        System.out.println(from);
                        String[] data = {id + ". " + originalName, from};
                        if (!who.contains("Член")) {
                            System.out.println(who);
                            data = new String[] {data[0], data[1], who};
                        }

                        newMembers.add(data);
                        changed++;

                    }
                    pos = page.indexOf(nobr, pos);
                } while (pos > 0);
                for (String deleted : names) {
                    System.out.println("Удален: \n" + deleted);
                    for (String role : roles) {
                        if (deleted.endsWith(" " + role)) {
                            deleted = deleted.substring(0, deleted.lastIndexOf(role)).trim();
                        }
                    }
                    deletedMembers.add(deleted);
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

                if (!noInfo && !checkOrder) {
                    for (File tik : tiks) {
                        int tikIdCur = Integer.parseInt(tik.getName().substring(3));
                        if (tikIdCur != tikId) {
                            continue;
                        }
                        File[] uiks = tik.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.startsWith("uik");
                            }
                        });
                        for (File uik : uiks) {
                            String name = uik.getName().substring(3);
                            name = name.substring(0, name.indexOf("."));
                            if (uikId == Integer.parseInt(name)) {
                                AddSpaces.UikStaff staff = null;
                                if (newMembers.size() > 0 || deletedMembers.size() > 0) {
                                    List<String> lines = AddSpaces.file2lines(uik);
                                    staff = AddSpaces.processLines(lines);
                                }
                                if (newMembers.size() > 0 && deletedMembers.size() > 0) {
                                    boolean forceReplace = false;
                                    for (Iterator<String[]> iterator = newMembers.iterator(); iterator.hasNext(); ) {
                                        String[] newMember =  iterator.next();
                                        for (Iterator<String> iterator2 = deletedMembers.iterator(); iterator2.hasNext(); ) {
                                            String deletedMember =  iterator2.next();
                                            if (newMember[0].contains(deletedMember)) {
                                                for (int i = 0; i < staff.members.size(); i++) {
                                                    String[] member =  staff.members.get(i);
                                                    if (member[0].contains(deletedMember)) {
                                                        System.out.print(deletedMember + " " + (member.length  == 3 ? member[2] : "") + " перешел в " + member[0] + " ");
                                                        if (newMember.length == 3) {
                                                            String position = newMember[2].trim();
                                                            if (member.length > 3) {
                                                                throw new RuntimeException("Too many lines in " + name);
                                                            }
                                                            if (member.length == 2) {
                                                                member = new String[]{member[0], member[1], position};
                                                                staff.members.remove(i);
                                                                staff.members.add(i, member);
                                                                System.out.println(position);
                                                            } else {
                                                                String[] tags = member[2].split(" ");
                                                                for (String role : roles) {
                                                                    if (role.equals(tags[0])) {
                                                                        if (member[2].contains(" ")) {
                                                                            member[2] = member[2].substring(member[2].indexOf(" "));
                                                                        } else {
                                                                            member[2] = "";
                                                                        }
                                                                    }
                                                                }
                                                                member[2] = (position + " " + member[2]).trim();
                                                                System.out.println(member[2]);
                                                            }
                                                        } else {
                                                            String[] tags = member[2].split(" ");
                                                            member[2] = "";
                                                            for (int j = 1; j < tags.length; j++) {
                                                                String t =  tags[j];
                                                                member[2] += " " + t;
                                                            }
                                                            member[2] = member[2].trim();
                                                            System.out.println(member[2]);
                                                        }
                                                        iterator.remove();
                                                        iterator2.remove();
                                                        forceReplace = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (forceReplace) {
                                        AddSpaces.replaceFile(uik, staff);
                                        List<String> lines = AddSpaces.file2lines(uik);
                                        staff = AddSpaces.processLines(lines);
                                    }
                                }

                                if(newMembers.size() == 0 && deletedMembers.size() > 0) {
                                    System.out.println("Deleting " + deletedMembers.size() + " members from " + uikId);
                                    for (String deletedMember : deletedMembers) {
                                        deleteMember(uikId, staff, deletedMember);
                                    }
                                    AddSpaces.replaceFile(uik, staff);
                                }
                                if(newMembers.size() > 0 && deletedMembers.size() == 0) {
                                    System.out.println("Adding " + newMembers.size() + " members to " + uikId);
                                    List<String[]> membersList2 = new ArrayList<>();
                                    int counter = 1;
                                    for (String[] member : staff.members) {
                                        counter = tryToAddMembers(membersList2, counter, newMembers);
                                        membersList2.add(member);
                                        counter++;
                                    }
                                    tryToAddMembers(membersList2, counter, newMembers);
                                    staff.members = membersList2;
                                    AddSpaces.replaceFile(uik, staff);
                                }
                                if(newMembers.size() > 0 && deletedMembers.size() > 0) {
                                    boolean needUpdate = false;
                                    for (String deletedMember : deletedMembers) {
                                        String[] deletedData = deletedMember.split(" ");
                                        boolean doDelete = true;
                                        System.out.println("Checking " + deletedMember + " for deletion:");
                                        if (deletedData.length > 2) {
                                            for (int i = 0; i < 3; i++) {
                                                for (String[] newMember : newMembers) {
                                                    if (newMember[0].contains(deletedData[i])) {
                                                        doDelete = false;
                                                        System.out.println("match " + newMember[0]);
                                                    }
                                                }
                                            }
                                        }
                                        System.out.println();
                                        if (doDelete) {
                                            deleteMember(uikId, staff, deletedMember);
                                            needUpdate = true;
                                        }
                                    }
                                    if (needUpdate) {
                                        AddSpaces.replaceFile(uik, staff);
                                    }
                                    System.out.println();
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Integer uikId : uiksMap.keySet()) {
            System.out.println("Missing data for uik " + uikId);
        }

        System.out.println("Официально в составах УИК " + total);
        //uikTab.close();
    }

    public static void deleteMember(int uikId, AddSpaces.UikStaff staff, String deletedMember) {
        for (Iterator<String[]> it = staff.members.iterator(); it.hasNext(); ) {
            String[] member = it.next();
            if (member[0].startsWith(deletedMember)) {
                it.remove();
                System.out.println("Deleting member from uik " + uikId + " " + deletedMember);
                break;
            }
        }
    }

    public static int tryToAddMembers(List<String[]> membersList2, int counter, List<String[]> newMembers) {
        for (String[] newMember : newMembers) {
            if (newMember[0].startsWith(counter + ". ")) {
                System.out.println(newMember[0]);
                newMember[0] = newMember[0].substring(Integer.toString(counter).length()  + 1).trim();
                membersList2.add(newMember);
                counter++;
            }
        }
        return counter;
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
