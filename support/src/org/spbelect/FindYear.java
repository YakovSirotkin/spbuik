package org.spbelect;

import java.io.*;
import java.util.*;

public class FindYear {

    public static void main(String[] args) throws Exception {        
        File[] tiks = new File("spbuik").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("tik");
            }
        });
        HashMap<String, String> all = new HashMap<>();
        HashMap<String, String> all2 = new HashMap<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik\\support\\all.csv")), "UTF-8"));
        in.readLine();
        String s;
        while ((s = in.readLine()) != null) {
            String[] d = s.split(",");
            all.put(d[4].trim().toLowerCase(), d[5]);
        }

        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik\\reserv.txt")), "UTF-8"));
        in.readLine();
        while ((s = in.readLine()) != null) {
            //System.out.println(s);
            String[] d = s.split(" ");
            String name = d[3].trim().toLowerCase();
            int i =4;
            while (!d[i].contains("19")) {
                name += " " + d[i].trim().toLowerCase();
                i++;
            }
            all2.put(name, d[i]);
        }
        Set<String> allNames = new HashSet<>();
        Set<String> doubles = new HashSet<>();
        List<String> full = new ArrayList<>();
        List<String> single = new ArrayList<>();
        for (File tik : tiks) {
            int tikId = Integer.parseInt(tik.getName().substring(3));
            File[] uiks = tik.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("uik");
                }
            });
            
            for (File uikFile : uiks) {
                int counter = 0;
                String name = uikFile.getName().substring(3);
                name = name.substring(0, name.indexOf("."));
                int uikId = Integer.parseInt(name);

                BufferedReader inUik = new BufferedReader(new InputStreamReader(new FileInputStream(uikFile), "UTF-8"));
                List<String> lines = new ArrayList<>();
                String s2 = null;              
                boolean membersProcessed = false;
                while ((s2 = inUik.readLine()) != null) {
                    s2 = s2.trim();
                    if (s2.startsWith("#Дома")) {
                        membersProcessed = true;
                    }

                    
                    if (!membersProcessed) {
                        if (s2.contains(". ")) {
                            full.add(uikId + " " + s2);
                            if (!s2.contains("19")) {
                                String member = s2.substring(s2.indexOf(".")  + 1).trim().toLowerCase();
                                if (allNames.contains(member)) {
                                    doubles.add(member);
                                } else {
                                    allNames.add(member);
                                }
                                if (all.containsKey(member)) {
                                    System.out.println(all.get(member) + " " + uikFile.getName() + " " +  s2);
                                    counter++;
                                }
                                
                                
                            } else {
                                String member = s2.substring(s2.indexOf(".")  + 1).trim().toLowerCase();
                                //System.out.println(s2);
                                int ind = member.lastIndexOf(" ");
                                String d = member.substring(ind).trim();
                                member = member.substring(0, ind).trim();
                                if (allNames.contains(member)) {
                                    doubles.add(member);
                                } else {
                                    allNames.add(member);                                    
                                }
                                
                                if (all.containsKey(member)) {
                                    if (!all.get(member).equals(d) ) {
                                        System.out.println(s2 + " " + all.get(member) +  " " + uikFile.getName());
                                        single.add(s2);
                                    }
                                }
                                if (all2.containsKey(member)) {
                                    if (!all2.get(member).equals(d) ) {
                                        System.out.println(s2 + " " + all2.get(member) +  " " + uikFile.getName());
                                    }
                                }

                            }                            
                        } 
                    }

                }
                if (counter > 0) {
                    System.out.println();
                }
            }
        }
        for (String name : doubles) {
            
            in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik\\support\\all.csv")), "UTF-8"));
            in.readLine();            
            List<String> source = new ArrayList<>();
            while ((s = in.readLine()) != null) {
                if (s.toLowerCase().contains(name)) {
                    source.add(s);
                }
            }
            for (String s1 : full) {
                if (s1.toLowerCase().contains(name)) {
                    String id = s1.substring(0, s1.indexOf(" "));
                    String year = s1.substring(s1.lastIndexOf(" ")).trim();
                    boolean found = false;
                    for (String s2 : source) {
                        if (s2.contains("," + id + ",")) {
                            found = true;
                            if (!s2.contains("," + year + ",")) {
                                System.out.println(s2);
                                System.out.println(s1);
                                System.out.println();
                            }
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println(s1);
                        System.out.println();
                    }
                            
                            
                }
            }
            
        }
        for (String s1 : single) {
            boolean doubled = false;
            for (String s2 : doubles) {
                if (s1.toLowerCase().contains(s2.toLowerCase())) {
                    doubled = true;
                    break;
                }
            }
            if (!doubled) {
                System.out.println(s1);
            }
        }
    }
}
