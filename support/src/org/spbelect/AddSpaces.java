package org.spbelect;

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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

                List<String> lines = file2lines(uikFile);

                UikStaff uikStaff = processLines(lines);

                replaceFile(uikFile, uikStaff);
            }
        }
    }

    public static List<String> file2lines(File uikFile) throws IOException {
        String name = uikFile.getName().substring(3);
        name = name.substring(0, name.indexOf("."));
        int uikId = Integer.parseInt(name);
        BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uikFile), "UTF-8"));
        List<String> lines = new ArrayList<>();
        String s3;
        while ((s3 = inUik.readLine()) != null) {
            lines.add(s3);
        }
        inUik.close();
        return lines;
    }

    public static UikStaff processLines(List<String> lines) {
        UikStaff staff = new UikStaff();

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
                staff.addMember(memberBuffer);
            }

            if (!membersProcessed) {
                int counter = 0;
                if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1)) && Character.isDigit(s2.charAt(2))&& Character.isDigit(s2.charAt(3))  && (s2.charAt(4) == '.')) {
                    s2 = s2.substring(5);
                    counter++;
                } else if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1)) && Character.isDigit(s2.charAt(2))  && (s2.charAt(3) == '.')) {
                    s2 = s2.substring(4);
                    counter++;
                } else if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1))  && (s2.charAt(2) == '.')) {
                    s2 = s2.substring(3);
                    counter++;
                } else if (Character.isDigit(s2.charAt(0)) && (s2.charAt(1) == '.')) {
                    s2 = s2.substring(2);
                    counter++;
                }
                if (counter == 1) {
                    staff.addMember(memberBuffer);
                }
                if (s2.trim().length() > 0) {
                    memberBuffer.add(s2.trim());
                }
            } else {
                staff.lines.add(s2);
            }
        }
        return staff;
    }

    public static void replaceFile(File uikFile, UikStaff staff) throws UnsupportedEncodingException, FileNotFoundException {
        uikFile.delete();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(uikFile), "UTF-8"));
        out.println("#Состав  ");
        int counter = 0;

        Collections.sort(staff.members, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                int role1 = getRole(o1);
                int role2 = getRole(o2);
                if (role1 != role2) {
                    return role1 - role2;
                } else {
                    return getNormalizedString(o1[0]).compareTo(getNormalizedString(o2[0]));
                }
            }

            public String getNormalizedString(String s) {
                return s.toLowerCase().replaceAll("ё", "е");
            }

            public int getRole(String[] o1) {
                int role = 10;
                if (o1.length == 3) {
                    String[] tags = o1[2].split(" ");
                    for (int i = 0; i < OfficialCheck.roles.length; i++) {
                        String s = OfficialCheck.roles[i];
                        if (s.equals(tags[0])) {
                            role = i;
                        }
                    }
                }
                return role;
            }
        });
        for (String[] memeber : staff.members) {
            counter++;
            out.println(counter + ". " + memeber[0].trim() + "  ");
            for (int i = 1; i < memeber.length; i++) {
                out.println("    " + memeber[i].trim() + "  ");
            }

        }
        out.println("  ");
        for (String s : staff.lines) {
            out.println(s.trim() + "  ");
        }
        out.close();
    }

    public static class UikStaff {
        List<String[]> members = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        public void addMember(List<String> memberBuffer) {
            if (memberBuffer.size() == 0) {
                return;
            }
            members.add(memberBuffer.toArray(new String[]{}));
            memberBuffer.clear();
        }
    }
}
