package org.spbelect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfficialCheckLocal {
    static boolean checkOrder = false;

    static final String[] roles = new String[]{"председатель", "заместитель", "секретарь"};

    public static void main(String[] args) throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("spbuik\\uikOfficial.txt"), StandardCharsets.UTF_8));
        Map<Integer, Map<Integer, List<Member>>> data = new HashMap<>();
        String s = null;
        while ((s = in.readLine()) != null) {
            if (s.isEmpty()) {
                continue;
            }
            String[] d = s.split("\\|");
            int tikId = Integer.parseInt(d[0]);
            int uikId = Integer.parseInt(d[1]);
            int id = Integer.parseInt(d[2]);
            String name = d[3];
            String role = d[4];
            String party = d[5];
            Member member = new Member(id, name, role, party);
            if (!data.containsKey(tikId)) {
                data.put(tikId, new HashMap<>());
            }
            Map<Integer, List<Member>> tik = data.get(tikId);
            if (!tik.containsKey(uikId)) {
                tik.put(uikId, new ArrayList<>());
            }
            tik.get(uikId).add(member);
        }

        Map<Integer, Set<String>> uiksMap = new HashMap<>();
        Map<Integer, Integer> tiksMap = new HashMap<>();
        File[] tiks = new File("spbuik").listFiles((dir, name) -> name.startsWith("tik"));
        for (File tik : tiks) {
            int tikExistId = Integer.parseInt(tik.getName().substring(3));
            File[] uiks = tik.listFiles((dir, name) -> name.startsWith("uik"));
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
                        if (s2.contains("19") || s2.contains("20")) {
                            //System.out.println(s2 + " " + uik.getName());
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
                tiksMap.put(uikId, tikExistId);
            }
        }

        for (Map.Entry<Integer, Map<Integer, List<Member>>> entry : data.entrySet()) {
            int tikId = entry.getKey();
            Map<Integer, List<Member>> tikData = entry.getValue();
            for (Map.Entry<Integer, List<Member>> tikEntry : tikData.entrySet()) {
                int uikId = tikEntry.getKey();
                List<Member> members = tikEntry.getValue();

                Set<String> names = uiksMap.get(uikId);
                if (names == null) {
                    System.out.println("Add uik " + uikId);
                }
                List<Member> newMembers = new ArrayList<>();
                List<String> deletedMembers = new ArrayList<>();

                Set<String> usedNames = new HashSet<>();
                for (Member member : members) {
                    String nameAndRole = getNameAndRole(member);
                    if (usedNames.contains(nameAndRole)) {
                        System.out.println("duplicate name: " + nameAndRole);
                        continue;
                    }
                    usedNames.add(nameAndRole);

                    if (names.contains(nameAndRole)) {
                        names.remove(nameAndRole);
                    } else {
                        newMembers.add(member);
                    }
                }
                for (String deleted : names) {
                    System.out.println("Удален: \n" + deleted);
                    for (String role : roles) {
                        if (deleted.endsWith(" " + role)) {
                            deleted = deleted.substring(0, deleted.lastIndexOf(role)).trim();
                        }
                    }
                    deletedMembers.add(deleted);
                }
                if (newMembers.size() + deletedMembers.size() > 0) {
                    System.out.println("uik" + uikId);
                    System.out.println();
                    File uikFile = new File("spbuik/tik" + tikId, "uik" + uikId + ".md");
                    List<String> lines = null;
                    try {
                        lines = AddSpaces.file2lines(uikFile);
                    } catch (FileNotFoundException e) {
                        System.out.println("missing file " + uikFile.toString());
                        Path parent = uikFile.toPath().getParent();
                        if (!Files.exists(parent)) {
                            Files.createDirectory(parent);
                        }
                        //uikFile.createNewFile();
                    }
                    if (lines == null) {
                        continue;
                    }
                    AddSpaces.UikStaff staff = AddSpaces.processLines(lines);
                    if (newMembers.size() > 0 && deletedMembers.size() > 0) {
                        boolean forceReplace = false;
                        for (Iterator<Member> iterator = newMembers.iterator(); iterator.hasNext(); ) {
                            Member newMember = iterator.next();
                            for (Iterator<String> iterator2 = deletedMembers.iterator(); iterator2.hasNext(); ) {
                                String deletedMember = iterator2.next();
                                if (newMember.getName().contains(deletedMember)) {
                                    for (int i = 0; i < staff.members.size(); i++) {
                                        String[] member = staff.members.get(i);
                                        if (member[0].contains(deletedMember)) {
                                            System.out.print(deletedMember + " " + (member.length == 3 ? member[2] : "") + " перешел в " + member[0] + " ");
                                            String newRole = newMember.getRole();
                                            if (member.length > 3) {
                                                throw new RuntimeException("Too many lines in " + member[0]);
                                            }
                                            if (member.length == 2) {
                                                member = new String[]{member[0], member[1], newRole};
                                                staff.members.remove(i);
                                                staff.members.add(i, member);
                                                System.out.println(newRole);
                                            } else {
                                                String[] tags = member[2].split(" ");
                                                for (String role : roles) {
                                                    if (role.equals(tags[0])) {
                                                        if (member[2].contains(" ")) {
                                                            member[2] = member[2].substring(member[2].indexOf(" ") + 1);
                                                        } else {
                                                            member[2] = "";
                                                        }
                                                    }
                                                }
                                                if (!newRole.equals("прг")) {
                                                    member[2] = newRole + " " + member[2];
                                                }
                                                member[2] = member[2].trim();
                                                System.out.println(member[2]);
                                            }
                                        }
                                    }
                                    iterator.remove();
                                    iterator2.remove();
                                    forceReplace = true;

                                }
                            }
                        }

                        if (forceReplace) {
                            AddSpaces.replaceFile(uikFile, staff);
                            List<String> linesNew = AddSpaces.file2lines(uikFile);
                            staff = AddSpaces.processLines(linesNew);
                        }
                    }

                    if (newMembers.size() == 0 && deletedMembers.size() > 0) {
                        System.out.println("Deleting " + deletedMembers.size() + " members from " + uikId);
                        for (String deletedMember : deletedMembers) {
                            deleteMember(uikId, staff, deletedMember);
                        }
                        AddSpaces.replaceFile(uikFile, staff);
                    }
                    if (newMembers.size() > 0 && deletedMembers.size() == 0) {
                        System.out.println("Adding " + newMembers.size() + " members to " + uikId);
                        for (Member newMember : newMembers) {
                            System.out.println(newMember.getName());
                            String role = newMember.getRole();
                            staff.members.add(role.equals("прг") ? new String[]{newMember.getName(), newMember.getParty()} : new String[]{newMember.getName(), newMember.getParty(), role});
                        }
                        AddSpaces.replaceFile(uikFile, staff);
                    }
                    if (newMembers.size() > 0 && deletedMembers.size() > 0) {
                        boolean needUpdate = false;
                        for (String deletedMember : deletedMembers) {
                            String[] deletedData = deletedMember.split(" ");
                            boolean doDelete = true;
                            Set<String> matches = new HashSet<>();
                            System.out.println("Checking " + deletedMember + " for deletion:");
                            if (deletedData.length > 2) {
                                for (int i = 0; i < 3; i++) {
                                    for (Member newMember : newMembers) {
                                        String name = newMember.getName();
                                        if (name.toLowerCase().contains(deletedData[i].toLowerCase())) {
                                            if (matches.contains(name)) {
                                                doDelete = false;
                                            } else {
                                                matches.add(name);
                                            }
                                            System.out.println("match " + name);
                                            System.out.println("old[" + deletedMember + "] " + name.split(" ")[0]);
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
                            AddSpaces.replaceFile(uikFile, staff);
                        }
                        System.out.println();
                    }
                }
                uiksMap.remove(uikId);
                if (tikId != tiksMap.get(uikId)) {
                    System.out.println(uikId + " should be in tik " + tikId + " instead of " + tiksMap.get(uikId));
                }
            }
        }
        for (
                Integer uikId : uiksMap.keySet()) {
            System.out.println("Missing data for uik " + uikId);
        }
    }

    public static String getNameAndRole(Member member) {
        String nameAndRole = member.getName();
        if (Arrays.stream(roles).anyMatch(r -> member.getRole().equals(r))) {
            nameAndRole += " " + member.getRole();
        }
        return nameAndRole;
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

    public static class Member {
        private final int id;
        private final String name;
        private final String role;
        private final String party;

        public Member(int id, String name, String role, String party) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.party = party;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }

        public String getParty() {
            return party;
        }
    }
}
