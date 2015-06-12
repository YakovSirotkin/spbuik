package org.spbelect;

import java.io.*;
import java.util.ArrayList;
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
        String s3= null;
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
