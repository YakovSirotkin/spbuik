package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Check2019 {

    public static void main(String[] args) throws Exception {
        File[] tiks = new File("spbuik").listFiles((dir, name) -> name.startsWith("tik"));

        List<String> lines2019 = file2lines(new File("spbuik/uik2019.txt"));

        for (File tik : tiks) {
            Map<String, String[]> oldData = new HashMap<>();
            Set<Integer> uikIds = new HashSet<>();
            File[] uiks = tik.listFiles((dir, name) -> name.startsWith("uik"));

            for (File uik : uiks) {
                String name = uik.getName();
                int uikId = Integer.parseInt(name.substring(3, name.length() - 3));
                uikIds.add(uikId);
            }

            for (String s : lines2019) {
                String[] d = s.split(",");
                if (uikIds.contains(Integer.parseInt(d[0]))) {
                    String name = d[1];
                    String year = "";
                    if (name.contains("19")) {
                        String[] dn = name.split(" ");
                        name = "";
                        for (int i = 0; i < dn.length - 1; i++) {
                            name += dn[i] + " ";
                        }
                        name = name.trim();
                        year = dn[dn.length - 1];
                    }
                    String history = d[d.length - 1].trim();
                    String[] dh = history.split(" ");
                    if (!dh[0].contains("[")) {
                        history = "";
                        for (int i = 1; i < dh.length; i++) {
                            history += dh[i] + " ";
                        }
                        history = history.trim();
                    }
                    oldData.put(name, new String[]{year, history});
                }
            }
            uiks = tik.listFiles((dir, name) -> name.startsWith("uik"));

            for (File uikFile : uiks) {
                List<String> lines = file2lines(uikFile);
                processLines(lines, oldData);
            }
        }

        /* Uncomment to print list
        Collections.sort(data);

        for (UikStaff uikStaff : data) {
            for (String[] member : uikStaff.members) {

                System.out.println(uikStaff.uikdId + "," + member[0] + "," + member[1] + "," + member[2]);
            }
        }*/

    }

    public static List<String> file2lines(File uikFile) throws IOException {
        BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uikFile), StandardCharsets.UTF_8));
        List<String> lines = new ArrayList<>();
        String s3;
        while ((s3 = inUik.readLine()) != null) {
            lines.add(s3);
        }
        inUik.close();
        return lines;
    }

    public static void processLines(List<String> lines, Map<String, String[]> oldData) {

        boolean membersProcessed = false;
        List<String> memberBuffer = new ArrayList<>();
        for (String s2 : lines) {
            if (s2.startsWith("#Состав")) {
                continue;
            }
            if (!s2.endsWith("  ")) {
                s2 += " ";
            }
            if (!s2.endsWith("  ")) {
                s2 += " ";
            }
            if (s2.startsWith("#Дома")) {
                membersProcessed = true;
                addMember(memberBuffer, oldData);
            }

            if (!membersProcessed) {
                int counter = 0;
                if (Character.isDigit(s2.charAt(0)) && Character.isDigit(s2.charAt(1)) && Character.isDigit(s2.charAt(2)) && Character.isDigit(s2.charAt(3)) && (s2.charAt(4) == '.')) {
                    s2 = s2.substring(5);
                    counter++;
                } else if (Character.isDigit(s2.charAt(0)) && Character.isDigit(s2.charAt(1)) && Character.isDigit(s2.charAt(2)) && (s2.charAt(3) == '.')) {
                    s2 = s2.substring(4);
                    counter++;
                } else if (Character.isDigit(s2.charAt(0)) && Character.isDigit(s2.charAt(1)) && (s2.charAt(2) == '.')) {
                    s2 = s2.substring(3);
                    counter++;
                } else if (Character.isDigit(s2.charAt(0)) && (s2.charAt(1) == '.')) {
                    s2 = s2.substring(2);
                    counter++;
                }
                if (counter == 1) {
                    addMember(memberBuffer, oldData);
                }
                if (s2.trim().length() > 0) {
                    memberBuffer.add(s2.trim());
                }
            }
        }
    }

    public static void addMember(List<String> memberBuffer, Map<String, String[]> oldData) {
        if (memberBuffer.size() == 0) {
            return;
        }
        String name = memberBuffer.get(0);
        if (name.contains("19")) {
            name = name.substring(0, name.lastIndexOf(" ")).trim();
        }
        String history = memberBuffer.size() > 2 ? memberBuffer.get(2) : "";
        if (!history.contains("2019[")) {
            String[] old = oldData.get(name);
            if (old != null) {
                for (String s : memberBuffer) {
                    System.out.println(s);
                }
                for (String s : old) {
                    System.out.println(s);
                }

            }
        }
        memberBuffer.clear();
    }

}