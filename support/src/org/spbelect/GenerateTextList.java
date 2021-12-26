package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateTextList {

    public static void main(String[] args) throws Exception {
        final Map<Integer, List<String>> members = new HashMap<>();

        File[] tiks = new File("spbuik").listFiles((dir, name) -> name.startsWith("tik"));

        for (File tik : tiks) {
            int tikId = Integer.parseInt(tik.getName().substring(3));
            File[] uiks = tik.listFiles((dir, name) -> name.startsWith("uik"));
            for (File uikFile : uiks) {
                int counter = 1;
                String name = uikFile.getName().substring(3);
                name = name.substring(0, name.indexOf("."));
                int uikId = Integer.parseInt(name);
                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uikFile), StandardCharsets.UTF_8));
                List<String> lines = new ArrayList<>();
                String s2 = null;              
                boolean membersProcessed = false;
                while ((s2 = inUik.readLine()) != null) {
                    if (!s2.endsWith("  ")) {
                        s2 += " ";
                    }
                    if (!s2.endsWith("  ")) {
                        s2 += " ";
                    }
                    if (s2.startsWith("#Дома")) {
                        membersProcessed = true;
                    }
                            
                    if (!membersProcessed) {
                        if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1)) && Character.isDigit(s2.charAt(2))&& Character.isDigit(s2.charAt(3))  && (s2.charAt(4) == '.')) {
                            s2 = counter + s2.substring(4);
                            counter++;
                        } else if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1)) && Character.isDigit(s2.charAt(2))  && (s2.charAt(3) == '.')) {
                            s2 = counter + s2.substring(3);
                            counter++;
                        } else if (Character.isDigit(s2.charAt(0)) &&  Character.isDigit(s2.charAt(1))  && (s2.charAt(2) == '.')) {
                            s2 = counter + s2.substring(2);
                            counter++;
                        } else if (Character.isDigit(s2.charAt(0)) && (s2.charAt(1) == '.')) {
                            s2 = counter + s2.substring(1);
                            counter++;
                        }
                    }

                    lines.add(s2);
                }

                int expectedStart = 1;
                List<List<String>> users = new ArrayList<>();
                List<String> user = null;
                for (String line : lines) {
                    boolean isFirstLine = line.startsWith(expectedStart + ". ");
                    boolean endReached = "".equals(line.trim());
                    if (isFirstLine || endReached) {
                        if (user != null) {
                            users.add(user);
                        }
                        if (endReached) {
                            break;
                        }
                        expectedStart++;
                        user = new ArrayList<>();
                    }
                    if (user != null) {
                        line = line.trim();
                        if (line.indexOf('.') > 0 && line.indexOf('.') < 4) {
                            line = line.substring(line.indexOf(".") + 2);
                        }
                        user.add(line);
                    }
                }

                List<String> userLines = new ArrayList<>();
                for (List<String> u : users) {
                    StringBuilder sb = new StringBuilder();
                    u.forEach(line -> sb.append(",").append(line));
                    userLines.add(sb.toString());
                    //422,Матюшева Юлия Андреевна 1990,Жители блокадного Ленинграда,секретарь секретарь2019[422] секретарь2018[422] прг2016[422] прг2014[422] old[Сидорова Юлия Андреевна]
                }
                members.put(uikId, userLines);
            }
        }
        Integer[] uikIds = members.keySet().toArray(new Integer[0]);
        Arrays.sort(uikIds);
        Arrays.stream(uikIds).forEach(uikId ->
                {
                    List<String> users = members.get(uikId);
                    users.stream().forEach(line -> System.out.println(uikId + line));
                }
        );

    }
}
