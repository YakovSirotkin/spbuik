package org.spbelect;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Convert2014 {

    private static final int YEAR = 2019;

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
                int counter = 1;
                String name = uikFile.getName().substring(3);
                name = name.substring(0, name.indexOf("."));
                int uikId = Integer.parseInt(name);
                if (uikId!=1283) {
                    //continue;
                }
                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uikFile), "UTF-8"));
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
                PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(uikFile), "UTF-8"));
                int expectedStart = 1;
                List<List<String>> users = new ArrayList<List<String>>();
                List user = null;
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
                        user = new ArrayList();
                    }
                    if (user != null) {
                        user.add(line);
                    }
                }
                out.println(lines.get(0));
                for (List<String> u : users) {
                    out.println(u.get(0));
                    out.println(u.get(1));
                    if (u.size() == 2) {
                        out.println("    прг" + YEAR + "[" + uikId + "]  ");
                    } else {
                        String[] tags = u.get(2).trim().split(" ");
                        String tags2014 = "председатель_секретарь_заместитель_";
                        LinkedList<String> newTags = new LinkedList<>();
                        boolean roleDefined = false;
                        for (String tag : tags) {
                            tag = tag.trim();
                            if (tag.length() == 0) {
                                continue;
                            }
                            if (tags2014.contains(tag)) {
                                roleDefined = true;
                                tag = tag + YEAR;
                                newTags.addFirst(tag + "[" + uikId + "]");
                                newTags.addFirst(tag.substring(0, tag.length() - 4));                                
                            } else {
                                newTags.add(tag);
                            }
                        }
                        if (!roleDefined) {
                            newTags.addFirst("прг" + YEAR + "[" + uikId + "]");
                        }
                        out.print("    ");
                        for (String newTag : newTags) {
                            out.print(newTag + " ");
                        }
                        out.println(" ");
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
                            out.println();
                        }
                    }                   
                }                
                out.close();
            }
        }
    }
}
