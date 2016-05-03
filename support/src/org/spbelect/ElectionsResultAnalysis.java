package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ElectionsResultAnalysis {
    public static void main(String[] args) throws Exception {
        String s = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik\\ikmoCandidatesWithResults.csv")), "UTF-8"));
        List<String[]> records = new ArrayList<>();
        Map<String, Integer> districtsTotal = new HashMap<>();
        Map<String, Integer> districtsInvalid = new HashMap<>();

        int totalRes = 0;
        Set<String> sources = new HashSet<>();
        while ((s = in.readLine()) != null) {
            String[] d = s.split(",");
            if (d[4].trim().length() > 0) {
                totalRes ++;
                records.add(d);
                int v = Integer.parseInt(d[4].trim());

                    double percent = Double.parseDouble(d[5].substring(0, d[5].indexOf("%")));
                int invalid = Integer.parseInt(d[7].trim());
                int total = Integer.parseInt(d[6].trim()) + invalid;
                        String key = getDistrictKey(d);
                districtsTotal.put(key, total);
                districtsInvalid.put(key, invalid);

                sources.add(getSource(d));

                if (Math.abs(-percent + (v * 100d)/total) > 0.005) {
                    //System.out.println(v + " " + total + " " + percent + " " + ((v * 1d)/total) + " " + s);
                }
            }
        }
        System.out.println("totalRes = " + totalRes);
        in.close();

        double totalShare = 0;
        List<Source> allData = new ArrayList<>();
        for (String sourceName : sources) {
            //System.out.println(source);
            Set<String> districts = new HashSet<>();
            Map<String, Integer> maxVote = new HashMap<>();
            for (String[] record : records) {
                if (sourceName.equals(getSource(record))) {
                    String districtKey = getDistrictKey(record);
                    districts.add(districtKey);
                    maxVote.put(districtKey, 0);
                }
            }
            int total = 0;
            for (String district : districts) {
                total += districtsTotal.get(district);
            }
            Set<String> candidates = new HashSet<>();
            for (String[] record : records) {
                if (sourceName.equals(getSource(record))) {
                    String districtKey = getDistrictKey(record);
                    int votes = Integer.parseInt(record[4].trim());
                    maxVote.put(districtKey, Math.max(maxVote.get(districtKey), votes));
                    String candidateName = record[2].trim();
                    if (candidates.contains(candidateName)) {
                        //System.out.println(sourceName + " " + candidateName);
                    }
                    candidates.add(candidateName);
                }
            }
            int votes = 0;
            for (String district : districts) {
                votes += maxVote.get(district);
            }
            double v = 1d * votes / total;
            totalShare  += v;
            Source source = new Source();
            source.name = sourceName;
            source.total = total;
            source.votes = votes;
            source.share = v;
            source.districts = districts.size();
            source.candidates = candidates.size();
            allData.add(source);
        }

        int totalInvalid = 0;
        int total = 0;
        Set<Map.Entry<String, Integer>> entries = districtsInvalid.entrySet();
        for (Map.Entry<String, Integer> invalidEntry : entries) {
            if (invalidEntry.getValue() == 0) {
                System.out.println("Нет недействительных в " + invalidEntry.getKey());
            }
            totalInvalid += invalidEntry.getValue();
            total += districtsTotal.get(invalidEntry.getKey());
        }

        Source invalid = new Source();
        invalid.name = "Недействительные";
        invalid.total = total;
        invalid.votes = totalInvalid;
        invalid.share = 1d * totalInvalid / total;
        invalid.districts = entries.size();
        invalid.candidates = 1;
        allData.add(invalid);
        totalShare += invalid.share;

        Collections.sort(allData, new Comparator<Source>() {
            @Override
            public int compare(Source o1, Source o2) {
                double v = o1.share - o2.share;
                if (v > 0) {
                    return -1;
                }
                if (v < 0) {
                    return 1;
                }
                return 0;
            }
        });
        System.out.println("totalShare = " + totalShare);
        for (Source source : allData) {
            System.out.println(source.name + " " + source.total + " " + source.votes + " " + source.share + " " + (source.share / totalShare) + " " + source.districts + " " + source.candidates);
        }
    }

    public static String getDistrictKey(String[] d) {
        return d[0] + "_" + d[1].trim();
    }

    public static String getSource(String[] d) {
        String s = d[9].trim();
        if (s.contains("\"ЯБЛОКО\"")) {
            return "Яблоко";
        }
        if (s.contains("КОММУНИСТЫ РОССИИ")) {
            return "КОММУНИСТЫ РОССИИ";
        }
        if (s.toLowerCase().contains("Коммунистическая партия Российской Федерации".toLowerCase())) {
            return "КПРФ";
        }
        if (s.contains("ЛДПР")) {
            return "ЛДПР";
        }
        if (s.contains("ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ")) {
            return "ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ";
        }
        if (s.contains("ЕДИНАЯ РОССИЯ")) {
            return "ЕР";
        }
        if (s.contains("СПРАВЕДЛИВАЯ РОССИЯ")) {
            return "СР";
        }
        if (s.toLowerCase().contains("Партия Великое Отечество".toLowerCase())) {
            return "Партия Великое Отечество";
        }
        if (s.toLowerCase().contains("РОДИНА".toLowerCase())) {
            return "РОДИНА";
        }
        if (s.toLowerCase().contains("Трудовая партия России".toLowerCase())) {
            return "Трудовая партия России";
        }
        if (s.toLowerCase().contains("Российский Объединённый Трудовой Фронт".toLowerCase())) {
            return "Российский Объединённый Трудовой Фронт";
        }
        if (s.toLowerCase().contains("Российская Социалистическая партия".toLowerCase())) {
            return "Российская Социалистическая партия";
        }
        if (s.toLowerCase().contains("За женщин России".toLowerCase())) {
            return "За женщин России";
        }
        if (s.toLowerCase().contains("Гражданская Платформа".toLowerCase())) {
            return "Гражданская Платформа";
        }
        if (s.toLowerCase().contains("СОЦИАЛЬНОЙ ЗАЩИТЫ".toLowerCase())) {
            return "СОЦИАЛЬНОЙ ЗАЩИТЫ";
        }

        return s;
    }

    public static class Source {
        String name;
        int total;
        int votes;
        double share;
        int districts;
        int candidates;
    }
}
