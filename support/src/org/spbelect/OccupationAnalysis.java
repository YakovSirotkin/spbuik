package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class OccupationAnalysis {
    public static void main(String[] args) throws Exception {
        String s = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik\\ikmoCandidatesWithResults.csv")), "UTF-8"));
        while ((s = in.readLine()) != null) {
            String[] d = s.split(",");
            if (d[3].trim().startsWith("избр.")) {
                String p = SnapshotMaker.getPage(d[12]);
                String place = getValue(p, "Основное место работы или службы");
                String position = getValue(p, "Занимаемая должность (или род занятий)");
                String placeLower = place.toLowerCase();
                if (placeLower.contains("гбоу") || placeLower.contains("гоу") || placeLower.contains("школа") || placeLower.contains("лицей") || placeLower.contains("гимназия")) {
                    if (position.toLowerCase().contains("директор") && !position.toLowerCase().contains("заместитель")) {
                        System.out.println(d[2] + ", " + place + ", " + position + ", " + d[9]);
                    }
                }

            }
        }
    }

    public static String getValue(String p, String field) {
        int start = p.indexOf(field);
        start = p.indexOf(">", start) + 1;
        start = p.indexOf(">", start) + 1;
        int end = p.indexOf("<", start);
        return p.substring(start, end);
    }
}
