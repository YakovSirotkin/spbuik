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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fix2019 {

    public static void main(String[] args) throws Exception {
        BufferedReader in2019 = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik\\uik2019.txt")), StandardCharsets.UTF_8));
        final Map<Integer, List<String>> members = new HashMap<>();
        in2019.lines().forEach(s ->
                {
                    String[] data = s.split(",");
                    int uikId = Integer.parseInt(data[0]);
                    if (!members.containsKey(uikId)) {
                        members.put(uikId, new ArrayList<>());
                    }
                    if (data[3].contains("прг2019[")) {
                        members.get(uikId).add(data[1]);
                    }
                }
        );


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

                uikFile.delete();
                PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(uikFile), StandardCharsets.UTF_8));
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
                        user.add(line);
                    }
                }
                out.println(lines.get(0));
                for (List<String> u : users) {
                    String mainLine = u.get(0);
                    out.println(mainLine);
                    out.println(u.get(1));
                    if (u.size() > 2) {
                        List<String> prg = members.get(uikId);
                        boolean wasPrg = false;
                        for (String p : prg) {
                            if (mainLine.contains(p.trim())) {
                                wasPrg = true;
                                break;
                            }
                        }

                        String tags = u.get(2);
                        if (!wasPrg) {
                            out.println(tags);
                        } else {
                            out.println(tags.replaceFirst("прг2018\\[" + uikId+ "]", "прг2019[" + uikId+ "]"));
                        }
                    }               
                    for (int i = 3; i < u.size(); i++) {
                        out.println(u.get(i));
                    }
                }
                boolean endReached = false;
                for (String line : lines) {
                    if (endReached) {
                        out.println(line);
                    } else {
                        endReached = "".equals(line.trim());
                        if (endReached) {
                            out.println("  ");
                        }
                    }                   
                }                
                out.close();
            }
        }
    }
}
