package org.spbelect;

import javafx.util.Pair;

import javax.jnlp.IntegrationService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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

public class AnalyseIkmo2019 {
    public static void main(String[] args) throws Exception {
        String s = null;
        Set<String> oldDeputy = new HashSet<>();
        Set<String> otkaz = new HashSet<>();

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\projects\\spbuik\\ikmoCandidatesWithResults2018.csv")), "windows-1251"));
        while ((s = in.readLine()) != null) {
            String[] d = s.split(",");
            if ("избр.".equals(d[3].trim())) {
                oldDeputy.add(d[2].trim() + d[7].trim());
            }
            if ("сложивший полномочия".equals(d[3].trim())) {
                otkaz.add(d[2].trim() + d[7].trim());
            }

        }
        in.close();


        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\projects\\spbuik\\spbuik\\ikmo\\ikmoDistrict2019.csv")), StandardCharsets.UTF_8));
        List<String[]> uiks = new ArrayList<>();
        Map<Integer, String> uikLink = new HashMap<>();
        Map<Integer, String> uikLinkName = new HashMap<>();
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

        Map<Source, Integer> sourceStat = new HashMap<>();
        Map<Source, Set<String>> sourceStat2 = new HashMap<>();
        int totalPlace = 0;
        int totalCandidates = 0;
        int totalDistricts = 0;
        int totalDeputy = 0;
        int totalOtkaz = 0;
        int badDistrict = 0;
        int trivial = 0;
        int hard = 0;


        Map<String, Integer> duplicate = new HashMap<>();

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
        Map<String, Integer> ikmoTotal = new HashMap<>();
        Map<String, Integer> ikmoRegistered = new HashMap<>();


        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik\\candidates2019_09_4.csv")), "UTF-8"));
        Map<String, List<District>> ikmos = new HashMap<>();
        while ((s = in.readLine()) != null) {
            String[] d = s.split("\\|");
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
        File ikmo2019 = new File("spbuik\\ikmo2019");
        ikmo2019.delete();
        ikmo2019.mkdir();
        int totalUiks = 0;
        for (Map.Entry<String, List<District>> entry : ikmos.entrySet()) {
            String name = entry.getKey();
            String folderName = toTranslit(name).replace(" ", "");
            List<District> districts = entry.getValue();
            int numberOfDistricts = districts.size();
            if ("number15".equals(folderName) && numberOfDistricts == 2) {
                folderName = "Kronshtadt";
                name = "Кронштадт";
            }
            File ikmo = new File(ikmo2019, folderName);
            ikmo.mkdir();
            //System.out.println("##["+name + "](" + folderName + ")");
            for (District district : districts) {
                totalDistricts++;
                PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(ikmo, "district" + district.id + ".md")), "UTF-8"));
                out.println("#" + name + ", " + district.id + " округ  ");
                //System.out.print("[" + district.id + " округ](" + folderName + "/district" + district.id +".md) ");
                out.print("##УИК: ");
                boolean isFirst = true;
                for (String[] uik : uiks) {
                    if (uik[0].equals(name) && uik[1].equals(Integer.toString(district.id))) {
                        if (!isFirst) {
                            out.print(", ");
                            //System.out.print(", ");
                        }
                        int uikId = Integer.parseInt(uik[2]);
                        out.print("[" + uik[2] + "](" + uikLinks.get(uikId) + ")");
                        //System.out.print(uik[2]);
                        uikLink.put(uikId, folderName + "/district" + district.id + ".md");
                        uikLinkName.put(uikId, name + ", " + district.id + " округ");
                        totalUiks++;
                        isFirst = false;

                    }
                }
                if (isFirst) {
                    throw new RuntimeException("No uiks");
                }
                out.println("  ");
                //System.out.println("  ");
                Collections.sort(district.candidates, new Comparator<Candidate>() {
                    @Override
                    public int compare(Candidate o1, Candidate o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
                out.println("##Зарегистрированные кандидаты");
                int id = 1;
                int candidatesNumber = 0;
                for (Candidate candidate : district.candidates) {
                    ikmoTotal.put(name, ikmoTotal.getOrDefault(name, 0) + 1);
                    if (candidate.type == Type.REGISTERED) {
                        ikmoRegistered.put(name, ikmoRegistered.getOrDefault(name, 0) + 1);
                        candidatesNumber++;
                        out.println(id + ". **" + candidate.name + "** " + candidate.birthday + "  ");
                        out.println(candidate.getSourceName() + "  ");
                        String born = "Родился в " + candidate.born;
                        if (!"город Санкт-Петербург".equals(candidate.place)) {
                            born += ", живет в " + candidate.place;
                        }
                        out.println(born);

                        if (candidate.education.length() > 0) {
                            out.println(filter(candidate.education));
                        }
                        String work = candidate.prof;
                        if (candidate.work.length() > 0) {
                            work += " в " + candidate.work;
                        }
                        out.println(filter(work));
                        if (candidate.deputy.length() > 0) {
                            out.println(candidate.deputy);
                        }

                        if (candidate.crime.length() > 0) {
                            out.println(candidate.crime);
                        }
                        out.println("[ссылка](" + candidate.link + ")  ");
                        int also = 0;
                        for (Map.Entry<String, List<District>> entry2 : ikmos.entrySet()) {
                            for (District district2 : entry2.getValue()) {
                                if (district2 != district) {
                                    for (Candidate candidate2 : district2.candidates) {
                                        if (candidate2.type == Type.REGISTERED) {
                                            if (candidate2.name.trim().equalsIgnoreCase(candidate.name.trim())) {
                                                if (candidate2.birthday.equals(candidate.birthday)) {
                                                    also++;
                                                    out.println("Также зарегистрирован: " + entry2.getKey() + ", округ " + district2.id +
                                                            ", " + candidate2.getSourceName()
                                                    );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (also > 0) {
                            duplicate.put(candidate.name + candidate.birthday, also + 1);
                        }
                        if (also > 3) {
                            //System.out.println("also " + also + " " + candidate.name) ;
                        }
                        if (oldDeputy.contains(candidate.name + candidate.birthday)) {
                            totalDeputy++;
                            if (also > 0) {
                                //System.out.println("Alert " + candidate.name);
                            }
                        }
                        if (otkaz.contains(candidate.name + candidate.birthday)) {
                            totalOtkaz++;
                            if (also > 0) {
                                System.out.println("Alert otkaz" + candidate.name);
                            }
                        }

                        Source source = candidate.source;
                        sourceStat.put(source, sourceStat.getOrDefault(source, 0) + 1);
                        Set<String> candidates = sourceStat2.getOrDefault(source, new HashSet<String>());
                        candidates.add(candidate.name + candidate.birthday);
                        sourceStat2.put(source, candidates);
                        if (source != Source.ER) {
                            for (String s1 : p) {
                                if (s1.equals(candidate.name)) {
                                    out.println("Участник праймериз Единой России  ");
                                }
                            }
                        }
                        id++;
                    }
                }

                int n = 5;
                switch (numberOfDistricts) {
                    case 1:
                        n = 10;
                        break;
                    case 5:
                        n = 2;
                        break;
                    case 10:
                        n = 1;
                        break;
                }
                if (n > 1) {
                    out.println("\n##Нужно выбрать " + n + " кандидатов.");
                } else {
                    out.println("\n##Нужно выбрать 1 кандидатa.");
                }
                totalPlace += n;
                totalCandidates += candidatesNumber;
                if (candidatesNumber < 2 * n) {
                    badDistrict++;
                    System.out.println(ikmo.getName() +  " " + district.id + " "  + n + " " + candidatesNumber);
                }
                if (candidatesNumber <= 2 * n) {
                    trivial++;
                } else {
                    hard++;
                }
                out.close();
            }
        }

        System.out.println("totalUiks = " + totalUiks);
        List<Integer> ids = new ArrayList<>(uikLink.keySet());
        Collections.sort(ids);
        for (Integer id : ids) {
            //System.out.println("УИК " + id + " [" + uikLinkName.get(id) + "](" + uikLink.get(id) + ")");
        }
        System.out.println("totalCandidates = " + totalCandidates);
        System.out.println("totalPlace = " + totalPlace);
        for (Map.Entry<Source, Integer> entry : sourceStat.entrySet()) {
            Source source = entry.getKey();
            System.out.println(source.getName() + ": " + entry.getValue() + " : " + sourceStat2.get(source).size());
        }
        int sumAlso = 0;
        for (Map.Entry<String, Integer> entry : duplicate.entrySet()) {
            String candidate = entry.getKey();
            sumAlso += entry.getValue() - 1;
        }
        System.out.println("Total duplicates:" + duplicate.size());
        System.out.println("sum also " + sumAlso);
        System.out.println("unique candidates " + (totalCandidates - sumAlso));
        System.out.println("totalDistricts = " + totalDistricts);
        System.out.println("ikmos.size() = " + ikmos.size());
        System.out.println("totalDeputy = " + totalDeputy);
        System.out.println("totalOtkaz = " + totalOtkaz);
        System.out.println("bad district " + badDistrict );
        System.out.println("hard = " + hard);
        System.out.println("trivial = " + trivial);

        List<Pair<String, Double>> accepted = new ArrayList<>();
        for (String ikmo : ikmoRegistered.keySet()) {
            double a = ikmoRegistered.get(ikmo);
            accepted.add(new Pair<>(ikmo, a/ikmoTotal.get(ikmo)));
        }
        accepted.sort(new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for (Pair<String, Double> v : accepted) {
            System.out.println(v.getKey() + " " + v.getValue());
        }
    }


    public static String toTranslit(String src) {
        String[] f = {".", "-", "№", "А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З", "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ч", "Ц", "Ш", "Щ", "Э", "Ю", "Я", "Ы", "Ъ", "Ь", "а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ч", "ц", "ш", "щ", "э", "ю", "я", "ы", "ъ", "ь"};
        String[] t = {"", "", "number", "A", "B", "V", "G", "D", "E", "Jo", "Zh", "Z", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ch", "C", "Sh", "Csh", "E", "Ju", "Ja", "Y", "`", "'", "a", "b", "v", "g", "d", "e", "jo", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ch", "c", "sh", "csh", "e", "ju", "ja", "y", "", ""};

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
            int registered = 0;
            for (Candidate candidate : candidates) {
                if (candidate.type == Type.REGISTERED) {
                    registered++;
                }
            }
            return registered;
        }

        private int getSourceCount(Source source) {
            int count = 0;
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
        String born;
        String place;
        String education;
        String work;
        String prof;
        String deputy = "";
        String crime = "";

        public Candidate(String[] d) {
            type = Type.getType(d[5]);
            source = Source.getSource(d[6]);
            sourceName = d[6].trim();
            birthday = d[7].trim();
            name = d[2].trim();
            link = d[9].trim();
            //if (birthday.equalsIgnoreCase("03.06.1974")) {
            //    System.out.println(link);
            //}
            born = d[10].trim();
            place = d[11].trim();
            education = d[12].trim();
            work = d[13].trim();
            prof = d[14].trim();
            if (d.length > 15) {
                deputy = d[15].trim();
            }
            if (d.length > 16) {
                crime = d[16].trim();
            }
        }

        public String getSourceName() {
            if (source != null) {
                return source.getName();
            }
            return sourceName;
        }
    }

    static enum Type {
        REGISTERED, REFUSE, EMPTY, REMOVE;

        public static Type getType(String v) {
            switch (v) {
                case "зарегистрирован":
                    return Type.REGISTERED;
                case "отказ в регистрации":
                    return Type.REFUSE;
                case "выбывший (после регистрации) кандидат":
                    return Type.REMOVE;
                case "":
                    return Type.EMPTY;
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
        ZA_SPRAVEDLIVOST2("Российская партия пенсионеров за социальную справедливость"),
        SOCZASHITA("СОЦИАЛЬНОЙ ЗАЩИТЫ"),
        SOC_PARTIYA("Российская Социалистическая партия"),
        FRONT("Российский Объединённый Трудовой Фронт"),
        WOMAN("За женщин России"),
        ROST("ПАРТИЯ РОСТА"),
        KAZAKI("Казачья партия");


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
                case "Региональное отделение Партии СПРАВЕДЛИВАЯ РОССИЯ в г. Санкт-Петербурге":
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
                case "Санкт-Петербургское региональное отделение Всероссийской политической партии \"ЕДИНАЯ РОССИЯ\"":
                    return ER;
                case "Политическая партия ЛДПР - Либерально-демократическая партия России":
                case "Санкт-Петербургское региональное отделение политической партии ЛДПР - Либерально-демократическая партия России":
                case "Санкт-Петербургское региональное отделение Политической партии ЛДПР - Либерально-демократической партии России":
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
                case "Санкт-Петербургское региональное отделение Российской объединенной демократической партии \"ЯБЛОКО\"":
                    return YABLOKO;
                case "Региональное отделение Всероссийской политической партии ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ! в г.Санкт-Петербурге":
                case "Всероссийская политическая партия ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ!":
                    return ZA_SPRAVEDLIVOST;
                case "Региональное отделение Политической партии \"Российская партия пенсионеров за социальную справедливость\" в городе Санкт-Петербурге":
                    return ZA_SPRAVEDLIVOST2;
                case "Региональное отделение политической партии СОЦИАЛЬНОЙ ЗАЩИТЫ в г.Санкт-Петербурге":
                case "Политическая партия СОЦИАЛЬНОЙ ЗАЩИТЫ":
                case "САНКТ-ПЕТЕРБУРГСКОЕ РЕГИОНАЛЬНОЕ ОТДЕЛЕНИЕ Политической партии СОЦИАЛЬНОЙ ЗАЩИТЫ":
                    return SOCZASHITA;
                case "Санкт-Петербургское региональное отделение Общероссийской политической партии \"Народная партия \"За женщин России\"":
                    return WOMAN;
                case "Региональное отделение политической партии \"Российская Социалистическая партия\" города Санкт-Петербурга":
                    return SOC_PARTIYA;
                case "Санкт-Петербургское региональное отделение политическое партии \"Российский Объединённый Трудовой Фронт\"":
                    return FRONT;
                case "Региональное отделение в Санкт-Петербурге Всероссийской политической партии \"ПАРТИЯ РОСТА\"":
                    return ROST;
                case "Региональное отделение политической партии \"Казачья партия Российской Федерации в городе Санкт-Петербурге\"":
                    return KAZAKI;
                default:
                    if (v.toLowerCase().contains("Коммунистическая партия Российской Федерации".toLowerCase())) {
                        return KPRF;
                    }
                    if (v.toLowerCase().contains(ER.getName().toLowerCase())) {
                        return ER;
                    }

                    System.out.println("source = " + v);
                    return null;
            }
        }
    }

    public static String filter(String s) {
        return s.replaceAll("&laquo;", "\"").replaceAll("&raquo;", "\"").replaceAll("<br>", " ");
    }

}
