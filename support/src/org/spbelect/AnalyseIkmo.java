package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyseIkmo {
    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\projects\\spbuik\\ikmoCandidates.csv")), "Cp1251"));;
        String s = null;
        Map<String, List<District>> ikmos = new HashMap<>();
        while ((s = in.readLine()) != null) {
            String[] d = s.split(",");
            if (d.length < 5) {
                continue;
            }
            for (int i = 0; i < d.length; i++) {
                d[i] = d[i].trim();                
            }
            String ikmo = d[0];
            int districtId = Integer.parseInt(d[1]);
            List<District> districts = ikmos.get(ikmo);
            if (districts == null) {
                districts = new ArrayList<>();
            }
            District district = null;
            for (District de : districts) {
                if (de.id == districtId) {
                    district = de;
                }
            }
            if (district == null) {
                district = new District(districtId);
                districts.add(district);
            }
            district.candidates.add(new Candidate(d));
            ikmos.put(ikmo, districts);
        }
        int twenty = 0;
        int fifteen = 0;
        for (Map.Entry<String, List<District>> ikmo : ikmos.entrySet()) {
            boolean hasGood = false;
            boolean hasBad = false;
            List<District> districts = ikmo.getValue();
            for (District d : districts) {
                if (d.getRegistered() >= 20) {
                    twenty++;
                }
                if (d.getRegistered() >= 15) {
                    fifteen++;
                }
                
                if (d.isBad()) {
                    hasBad = true;
                } else {
                    hasGood = true;
                }
            }
            if (!hasGood) {
                int registerd = 0;
                int er = 0;
                int ldpr = 0;
                for (District district : districts) {
                    registerd += district.getRegistered();
                    er += district.getSourceCount(Source.ER);
                    ldpr += district.getSourceCount(Source.LDPR);                    
                }
                System.out.println("<li><a href=\"\">" + ikmo.getKey() + "</a> " + (5 * districts.size()) + " мандатов, " + registerd + " кандидатов, "  + er + " от ЕР");
            } else if (hasBad) {
                System.out.println("<li><a href=\"\">" + ikmo.getKey() + "</a><ul>");
                for (District d : districts) {
                    if (d.isBad()) {
                        System.out.println("<li>округ " + d.id + ": " + d.getRegistered() + " кандидатов, "  + d.getSourceCount(Source.ER) + " от ЕР");
                        
                    }                   
                }
                System.out.println("</ul>");
            }
        }

        System.out.println(">= 20 " + twenty);
        System.out.println(">= 15 " + fifteen);
    }
    
    static class District {
        int id;
        List<Candidate> candidates = new ArrayList<>();

        District(int id) {
            this.id = id;
        }
        
        boolean isBad() {
            return getRegistered() <= 8 && getRegistered() > 5;
        }

        private int getRegistered() {
            int registered  = 0;
            for (Candidate candidate : candidates) {
                if (candidate.type == Type.REGISTERED) {
                    registered++;
                }
            }
            return registered;
        }

        private int getSourceCount(Source source) {
            int count  = 0;
            for (Candidate candidate : candidates) {
                if (candidate.type == Type.REGISTERED) {
                    if (source.equals(candidate.source)) {
                        count++;
                    }
                }
            }
            return count;
        }

        @Override
        public String toString() {
            return id + " " + getRegistered(); 
        }
    }
    
    static class Candidate {
        Type type;
        Source source;

        public Candidate(String[] d) {
            type = Type.getType(d[3]);
            source = Source.getSource(d[4]);
        }
    }
    
    static enum Type {
        REGISTERED, REFUSE, EMPTY, REMOVE;
        
        public static Type getType(String v) {
            switch (v) {
                case "зарегистрирован": return Type.REGISTERED;
                case "отказ в регистрации": return Type.REFUSE;
                case "выбывший (после регистрации) кандидат": return Type.REMOVE;
                case "": return Type.EMPTY;
                default:
                    System.out.println("v = " + v);
                    return null;
            }
        }
    }
    static enum Source {
        ER, LDPR, KPRF, RODINA, SR, SELF, EMPTY, TRUDOVAY_ROSSIA, GP, ;

        public static Source getSource(String v) {
            switch (v) {
                case "САНКТ-ПЕТЕРБУРГСКОЕ ГОРОДСКОЕ ОТДЕЛЕНИЕ политической партии \"КОММУНИСТИЧЕСКАЯ ПАРТИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ\"":
                case "Политическая партия \"КОММУНИСТИЧЕСКАЯ ПАРТИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ\"": return KPRF;
                case "Региональное отделение ВСЕРОССИЙСКОЙ ПОЛИТИЧЕСКОЙ ПАРТИИ \"РОДИНА\" в городе Санкт-Петербурге": return RODINA;
                case "Региональное отделение Политической партии СПРАВЕДЛИВАЯ РОССИЯ в городе Санкт-Петербурге": return SR;
                case "Самовыдвижение": return SELF;
                case "": return EMPTY;
                case "Политическая партия \"Трудовая партия России\"": return TRUDOVAY_ROSSIA;
                case "Региональное отделение в городе Санкт-Петербурге Политической партии \"Гражданская Платформа\"": return GP;
                case "Всероссийская политическая партия \"ЕДИНАЯ РОССИЯ\"":
                case "Санкт-Петербургское региональное отделение Всероссийской политической партии \"ЕДИНАЯ РОССИЯ\"" : return ER;
                case "Политическая партия ЛДПР - Либерально-демократическая партия России":
                case "Санкт-Петербургское региональное отделение политической партии ЛДПР - Либерально-демократическая партия России": return LDPR;
                default:
                    System.out.println("source = " + v);
                    return null;
            }
        }
    }
    
}
