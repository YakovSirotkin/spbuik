package org.spbelect;

import java.io.*;
import java.util.*;

public class AnalyseIkmo {
    public static void main(String[] args) throws Exception {
        String s = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\projects\\spbuik\\spbuik\\ikmo\\ikmoDistrict.csv")), "UTF-8"));
        List<String[]> uiks = new ArrayList<>();
        while ((s = in.readLine()) != null) {
            String[] d = s.split(",");
            uiks.add(new String[]{d[0], d[1].substring(0, d[1].indexOf(" ")), d[2]});
        }        
        in.close();

        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\projects\\spbuik\\spbuik\\ikmo\\praimeriz.txt")), "UTF-8"));
        List<String> p = new ArrayList<>();
        while ((s = in.readLine()) != null) {
            p.add(s.trim());
        }
        in.close();

        Map<Integer, String> uikLinks = new HashMap<>();
        File[] tiks = new File("spbuik").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("tik");
            }
        });
        for (File tik : tiks) {
            int tikId = Integer.parseInt(tik.getName().substring(3));
            File[] uikFiles = tik.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("uik");
                }
            });
            for (File uikFile : uikFiles) {
                String name = uikFile.getName();
                uikLinks.put(Integer.parseInt(name.substring(3, name.indexOf("."))), "../../" + tik.getName() + "/" + uikFile.getName());
            }
        }
        
        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\projects\\spbuik\\ikmoCandidates.csv")), "Cp1251"));        
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
        File ikmo2014 = new File("spbuik\\ikmo2014");
        ikmo2014.mkdir();
        int totalUiks = 0;
        for (Map.Entry<String, List<District>> entry : ikmos.entrySet()) {
            String name = entry.getKey();
            String folderName = toTranslit(name).replace(" ", "");
            if ("number15".equals(folderName) && entry.getValue().size() == 2) {
                folderName = "Kronshtadt";
                name = "Кронштадт";
            }
            File ikmo = new File(ikmo2014, folderName);
            ikmo.mkdir();
            System.out.println("##["+name + "](" + folderName + ")");
            for (District district : entry.getValue()) {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(ikmo, "district" + district.id + ".md")), "UTF-8"));
                out.println("#" + name + ", " + district.id + " округ  ");
                System.out.print("[" + district.id + " округ](" + folderName + "/district" + district.id +".md) ");
                out.print("##УИК: ");
                boolean isFirst = true;
                for (String[] uik : uiks) {
                    if (uik[0].equals(name) && uik[1].equals(Integer.toString(district.id))) {
                        if (!isFirst) {
                            out.print(", ");
                            System.out.print(", ");
                        }
                        out.print("[" + uik[2]+"]("+ uikLinks.get(Integer.parseInt(uik[2])) + ")");
                        System.out.print(uik[2]);
                        totalUiks++;
                        isFirst = false;
                    }
                }
                if (isFirst) {
                    throw new RuntimeException("No uiks");
                }
                out.println("  ");
                System.out.println("  ");
                Collections.sort(district.candidates, new Comparator<Candidate>() {
                    @Override
                    public int compare(Candidate o1, Candidate o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
                out.println("##Зарегистрированные кандидаты");
                int id = 1;
                int totalGood = 0;
                for (Candidate candidate : district.candidates) {
                    boolean isGood = true;
                    
                    if (candidate.type == Type.REGISTERED) {
                        out.println(id + ". **" + candidate.name + "** " + candidate.birthday + "  ");                        
                        out.println(candidate.getSourceName() + "  ");
                        out.println(candidate.education + ", " + candidate.work + ", " +  candidate.prof + (candidate.deputy.length() > 0 ? ", " + candidate.deputy : "") +
                                        (candidate.crime.length() > 0 ? ", " + candidate.crime : "") +
                                "  ");
                        out.println("[ссылка](" + candidate.link + ")  ");
                        for (Map.Entry<String, List<District>> entry2 : ikmos.entrySet()) {
                            for (District district2 : entry2.getValue()) {
                                if (district2 != district) {
                                    for (Candidate candidate2 : district2.candidates) {
                                        if (candidate2.type == Type.REGISTERED) {
                                            if (candidate2.name.trim().equalsIgnoreCase(candidate.name.trim())) {
                                                if (candidate2.birthday.equals(candidate.birthday)) {
                                                    out.println("Также зарегистрирован: " + entry2.getKey() + ", округ " + district2.id +
                                                        ", " + candidate2.getSourceName()
                                                    );
                                                    if (candidate2.source == Source.ER || candidate2.source == Source.LDPR ) {
                                                        isGood = false;
                                                    }
                                                    
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Source source = candidate.source;
                        if (source == Source.ER || source == Source.LDPR ) {
                            isGood = false;
                        }
                        if (source != Source.ER) {
                            for (String s1 : p) {
                                if (s1.equals(candidate.name)) {
                                    out.println("Участник праймериз Единой России  ");
                                    isGood = false;
                                }
                            }
                        }
                        if (isGood) {
                            totalGood++;
                        }
                        id++;
                    }
                }
                
                out.println("\n##Можно выбирать из " + totalGood + " " + (totalGood == 1 ? "кандидата" : "кандидатов") + ".  ");
                out.close();
                
            }
        }
        System.out.println("totalUiks = " + totalUiks);
        //printAlerts(ikmos);
    }


    public static String toTranslit(String src) {
        String [] f = {".", "-", "№", "А","Б","В","Г","Д","Е","Ё", "Ж", "З","И","Й","К","Л","М","Н","О","П","Р","С","Т","У","Ф","Х","Ч", "Ц","Ш", "Щ", "Э","Ю", "Я", "Ы","Ъ", "Ь", "а","б","в","г","д","е","ё", "ж", "з","и","й","к","л","м","н","о","п","р","с","т","у","ф","х","ч", "ц","ш", "щ", "э","ю", "я", "ы","ъ","ь"};
        String [] t = {"", "", "number", "A","B","V","G","D","E","Jo","Zh","Z","I","J","K","L","M","N","O","P","R","S","T","U","F","H","Ch","C","Sh","Csh","E","Ju","Ja","Y","`", "'", "a","b","v","g","d","e","jo","zh","z","i","y","k","l","m","n","o","p","r","s","t","u","f","h","ch","c","sh","csh","e","ju","ja","y","",""};

        String res = "";

        for (int i = 0; i < src.length(); ++i) {
            String add = src.substring(i, i + 1);
            for (int j = 0; j < f.length; j++) {
                if (f[j].equals(add)) {
                    add = t[j];
                    break;
                }
            }
            res += add;
        }

        return res;
    }    
    
    public static void printAlerts(Map<String, List<District>> ikmos) {
        int twenty = 0;
        int fifteen = 0;
        for (Map.Entry<String, List<District>> ikmo : ikmos.entrySet()) {
            boolean hasGood = false;
            boolean hasBad = false;
            List<District> districts = ikmo.getValue();
            if (districts.size() ==1) {
                //System.out.println("Один округ! " + ikmo.getKey());
                District d = districts.get(0);
                System.out.println("<li><a href=\"\">" + ikmo.getKey() + "</a> " + d.getRegistered() + " кандидатов, из них "  + d.getSourceCount(Source.ER) + " от ЕР");
            } else {
                continue;
            }
            if (districts.size() ==3) {
                System.out.println("Три округа! " + ikmo.getKey());
            }

            if (districts.size() > 4) {
                System.out.println("Округа не пятимандатные! " + ikmo.getKey() + " " + districts.size() + " округов");
            }
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
        String sourceName;
        String name;
        String birthday;
        String link;
        String education;
        String work;
        String prof;
        String deputy;
        String crime;
        
        public Candidate(String[] d) {
            type = Type.getType(d[3]);
            source = Source.getSource(d[4]);
            sourceName = d[4].trim();
            birthday = d[5].trim();    
            name = d[2].trim();
            link = d[7].trim();
            education = d[8].trim();
            work = d[9].trim();
            prof = d[10].trim();
            deputy = d[11].trim();
            crime = d[12].trim();
        }

        public String getSourceName() {
            if (source != null) {
                return source.getName();
            }
            System.out.println(sourceName);
            return sourceName;
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
        ER("Единая Россия"), LDPR("ЛДПР"), KPRF("КПРФ"), RODINA("Родина"), 
        SR("Справедливая Россия"), SELF("Самовыдвиженец"), TRUDOVAY_ROSSIA("Трудовая Россия"), 
        GP("Гражданская Платформа"), 
        KOMMUMISTY_ROSSII("Коммунисты России"),
        VALIKOE_OTECHECSTVO("Великое Отечество"),
        YABLOKO("Яблоко"),
        ZA_SPRAVEDLIVOST("ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ!"),
        SOCZASHITA("СОЦИАЛЬНОЙ ЗАЩИТЫ"),
        SOC_PARTIYA("Российская Социалистическая партия"),
        FRONT("Российский Объединённый Трудовой Фронт"),
        WOMAN("За женщин России");
        
        

        String name;

        Source(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Source getSource(String v) {
            v = v.replace("«", "");
            v = v.replace("»", "");
            switch (v) {
                case "САНКТ-ПЕТЕРБУРГСКОЕ ГОРОДСКОЕ ОТДЕЛЕНИЕ политической партии \"КОММУНИСТИЧЕСКАЯ ПАРТИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ\"":
                case "Красносельское местное (районное) отделение Санкт-Петербургского городского отделения политической партии Коммунистическая партия Российской Федерации":    
                case "Политическая партия \"КОММУНИСТИЧЕСКАЯ ПАРТИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ\"": 
                case "Невское местное (районное) отделение Санкт-Петербургского городского отделения политической партии Коммунистическая партия Российской Федерации":
                case "Петроградское местное (районное) отделение Санкт-Петербургского городского отделения политической партии Коммунистическая партия Российской Федерации":                   
                    return KPRF;
                case "Региональное отделение ВСЕРОССИЙСКОЙ ПОЛИТИЧЕСКОЙ ПАРТИИ \"РОДИНА\" в городе Санкт-Петербурге":
                case "ВСЕРОССИЙСКАЯ ПОЛИТИЧЕСКАЯ ПАРТИЯ \"РОДИНА\"":    
                    return RODINA;
                case "Региональное отделение Политической партии СПРАВЕДЛИВАЯ РОССИЯ в городе Санкт-Петербурге":
                case "Политическая партия СПРАВЕДЛИВАЯ РОССИЯ":
                    return SR;
                case "Самовыдвижение": 
                    return SELF;
                //case "": 
                //    return EMPTY;
                case "Региональное отделение Политической партии \"Трудовая партия России\" в Санкт-Петербурге":
                case "Политическая партия \"Трудовая партия России\"": 
                    return TRUDOVAY_ROSSIA;
                case "Региональное отделение в городе Санкт-Петербурге Политической партии \"Гражданская Платформа\"": 
                    return GP;
                case "Всероссийская политическая партия \"ЕДИНАЯ РОССИЯ\"":
                case "Санкт-Петербургское региональное отделение Всероссийской политической партии \"ЕДИНАЯ РОССИЯ\"" : 
                    return ER;
                case "Политическая партия ЛДПР - Либерально-демократическая партия России":
                case "Санкт-Петербургское региональное отделение политической партии ЛДПР - Либерально-демократическая партия России": 
                    return LDPR;
                case "Санкт-Петербургское городское отделение Политической партии \"КОММУНИСТЫ РОССИИ\"":
                case "САНКТ-ПЕТЕРБУРГСКОЕ ГОРОДСКОЕ ОТДЕЛЕНИЕ Политической партии КОММУНИСТИЧЕСКАЯ ПАРТИЯ КОММУНИСТЫ РОССИИ":
                case "Политическая Партия КОММУНИСТИЧЕСКАЯ ПАРТИЯ КОММУНИСТЫ РОССИИ":    
                    return KOMMUMISTY_ROSSII;
                case "Региональное отделение в городе Санкт-Петербурге Всероссийской политической партии \"Партия Великое Отечество\"":
                case "Всероссийская политическая партия \"ПАРТИЯ ВЕЛИКОЕ ОТЕЧЕСТВО\"":    
                    return VALIKOE_OTECHECSTVO;
                case "Санкт-Петербургское региональное отделение политической партии \"Российская объединенная демократическая партия \"ЯБЛОКО\"":
                case "Политическая партия \"Российская объединенная демократическая партия \"ЯБЛОКО\"":                    
                    return YABLOKO;
                case "Региональное отделение Всероссийской политической партии ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ! в г.Санкт-Петербурге":
                case "Всероссийская политическая партия ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ!":
                    return ZA_SPRAVEDLIVOST;
                case "Региональное отделение политической партии СОЦИАЛЬНОЙ ЗАЩИТЫ в г.Санкт-Петербурге":
                case "Политическая партия СОЦИАЛЬНОЙ ЗАЩИТЫ":    
                    return SOCZASHITA;
                case "Санкт-Петербургское региональное отделение Общероссийской политической партии \"Народная партия \"За женщин России\"":
                    return WOMAN;
                case "Региональное отделение политической партии \"Российская Социалистическая партия\" города Санкт-Петербурга":
                    return SOC_PARTIYA;
                case "Санкт-Петербургское региональное отделение политическое партии \"Российский Объединённый Трудовой Фронт\"":
                    return FRONT;                    
                default:
                    if (v.toLowerCase().contains("Коммунистическая партия Российской Федерации".toLowerCase())) {
                        return KPRF;
                    }
                    if (v.toLowerCase().contains(ER.getName().toLowerCase())) {
                        return ER;
                    }
                    
                    //System.out.println("source = " + v);
                    return null;
            }
        }
    }
    
}
