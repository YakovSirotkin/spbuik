package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddPreviousInfo {

    public static void main(String[] args) throws Exception {        
        File[] tiks = new File("spbuik").listFiles((dir, name) -> name.startsWith("tik"));

        List<String> lines2018 = file2lines(new File("spbuik/uik2019.txt"));

        for (File tik : tiks) {
            int tikId = Integer.parseInt(tik.getName().substring(3));
            if (tikId != 11) {
                //continue;
            }
            Map<String, String[]> oldData = new HashMap<>();
            Set<Integer> uikIds = new HashSet<>();
            File[] uiks = tik.listFiles((dir, name) -> name.startsWith("uik"));

            for (File uik : uiks) {
                String name = uik.getName();
                int uikId = Integer.parseInt(name.substring(3, name.length() - 3));
                uikIds.add(uikId);
            }

            for (String s : lines2018) {
                String[] d = s.split(",");
                if(uikIds.contains(Integer.parseInt(d[0]))) {
                    String name = d[1];
                    String year = "";
                    if (name.contains("19")) {
                        String[] dn = name.split(" ");
                        name = "";
                        for (int i = 0; i < dn.length - 1; i++) {
                            name += dn[i] + " ";
                        }
                        name = name.trim();
                        year = dn[dn.length-1];
                    }
                    String history = d[d.length - 1].trim();
                    String[] dh = history.split(" ");
                    if (!dh[0].contains("[")) {
                        history = "";
                        for (int i = 1; i < dh.length; i++) {
                            history += dh[i] +" ";
                        }
                        history = history.trim();
                    }
                    oldData.put(name, new String[] {year, history});
                }
            }
            uiks = tik.listFiles((dir, name) -> name.startsWith("uik"));

            for (File uikFile : uiks) {
                List<String> lines = file2lines(uikFile);

                UikStaff uikStaff = processLines(lines , oldData);
                String fileName = uikFile.getName().substring(3);
                uikStaff.uikdId = Integer.parseInt(fileName.substring(0, fileName.indexOf(".")));
                replaceFile(uikFile, uikStaff);
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

    public static UikStaff processLines(List<String> lines, Map<String, String[]> oldData) {
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
                staff.addMember(memberBuffer, oldData);
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
                    staff.addMember(memberBuffer, oldData);
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

    public static void replaceFile(File uikFile, UikStaff staff) throws FileNotFoundException {
        uikFile.delete();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(uikFile), StandardCharsets.UTF_8));
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

    public static class UikStaff implements Comparable<UikStaff>{
        List<String[]> members = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        int uikdId;

        public void addMember(List<String> memberBuffer, Map<String, String[]> oldData) {
            if (memberBuffer.size() == 0) {
                return;
            }
            String name = memberBuffer.get(0);
            String history = memberBuffer.size() > 2 ? memberBuffer.get(2) : "";
            if (!name.contains("19") && !history.contains("[")) {
                String[] old = oldData.get(name);
                if (old != null) {
                    if (old[0].length() > 0) {
                        memberBuffer.remove(0);
                        memberBuffer.add(0, name + " " + old[0]);
                    }
                    if (memberBuffer.size() > 2) {
                        memberBuffer.remove(2);
                    }
                    memberBuffer.add((history + " " + old[1]).trim());
                    for (String s : memberBuffer) {
                        System.out.println(s);
                    }
                }
            }
            members.add(memberBuffer.toArray(new String[]{}));
            memberBuffer.clear();
        }

        @Override
        public int compareTo(UikStaff o) {
            return uikdId - o.uikdId;
        }
    }
}
/*
#Состав
1. Осипова Людмила Ивановна
    собрание-дом
    председатель
2. Нугис Алексей Валериевич
    РОДИНА
    заместитель
3. Архипова Екатерина Сергеевна
    Зелёные
    секретарь
4. Ермакова Ольга Александровна
    ЕР
5. Желамкова Вера Ивановна
    Гражданская Платформа
6. Иванов Владимир Алексеевич
    КПРФ
7. Коберт Юлия Гербертовна
    ЯБЛОКО
8. Ляховец Елена Виоленовна
    собрание-дом
9. Осипов Олег Викторович
    собрание-дом
10. Петрова Наталия Константиновна
    КОММУНИСТЫ РОССИИ
11. Плаксина Галина Александровна
    собрание-дом
12. Плаксина Татьяна Александровна
    собрание-дом
13. Суслова Анна Астафиевна
    ЛДПР
14. Суслова Инна Викторовна
    Союз Труда

#Дома
ул. Композиторов, дома №№ 22 (корпуса 1, 4), 24 (корпус 1), 26/3. Адреса помещений: участковой избирательной комиссии - ул. Композиторов, дом № 22 корп. 2, школа № 494; для голосования - ул. Композиторов, дом № 22 корп. 2, школа № 494.

#Выборы-2012
##Номер УИК
307

 */