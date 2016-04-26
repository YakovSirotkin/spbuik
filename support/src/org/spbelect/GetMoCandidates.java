package org.spbelect;

import java.util.*;

public class GetMoCandidates {
    public static void main(String[] args) throws Exception {
        String root = OfficialCheck.getPage("http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?start_date=14.09.2014&urovproved=all&vidvibref=all&vibtype=all&end_date=14.09.2014&sxemavib=all&action=search_by_calendar&region=78");
        String[] links = root.split("<a href=\"");
        String endLink = "\" class=\"vibLink\">";
        int count = 0;
        for (String link : links) {
            if (link.contains(endLink)) {
                count++;
                if (count > 1) {
                    String ikmoLink = link.substring(0, link.indexOf(endLink));
                    String imkPage = getPage(ikmoLink);
                    String[] candidatesLink = imkPage.split("<a href=\"");
                    String mo = null;
                    List<Candidate> moCandidates = new ArrayList<>();
                    for (String s : candidatesLink) {
                        if (s.contains("Сведения о кандидатах")) {
                            String pageLink = s.substring(0, s.indexOf("\""));
                            //System.out.println(pageLink);
                            int page = 1;
                            String p = getPage(pageLink);
                            String[] rowsh = p.split("action=show&root=1&tvd=");

                            for (String r : rowsh) {
                                r = r.replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ");
                                if (r.toLowerCase().contains(">Избирательная комиссия".toLowerCase()) || r.contains(">Территориальная избирательная")) {
                                    final String[] prefixes = new String[]{
                                            "Избирательная комиссия муниципального образования муниципальный округ",
                                            "Избирательная комиссия муниципального образования муниципального округа",
                                            "Избирательная комиссия муниципального образования муниципальный округ",
                                            "Избирательная комиссия муниципального образования",

                                            "Избирательная комиссия внутригородского муниципального образования Санкт-Петербурга муниципальный округ",
                                            "Избирательная комиссия внутригородского муниципального образования Санкт-Петербурга муниципального округа",
                                            "Избирательная комиссия внутригородского муниципального образования муниципальный округ",
                                            "Избирательная комиссия внутригородского муниципального образования Санкт-Петербурга",
                                            "Территориальная избирательная комиссия",
                                            "Избирательная комиссия МО",
                                            "Избирательная комиссия внутригородского муниципального образования"

                                    };


                                    if (r.contains(">Территориальная избирательная")) {
                                        mo = "Кронштадт";
                                    } else {
                                        for (String prefix : prefixes) {
                                            if (mo == null && r.toLowerCase().contains(prefix.toLowerCase())) {
                                                mo = r.substring(r.toLowerCase().indexOf(prefix.toLowerCase())).substring(prefix.length()).trim();
                                                mo = mo.substring(0, mo.indexOf("<"))
                                                        .replace("\"", "")
                                                        .replace("Санкт-Петербурга", "")
                                                        .replace("округ", "")
                                                        .trim();
                                                break;
                                            }
                                        }
                                    }
                                    if (mo == null) {
                                        System.out.println(r.contains(prefixes[9]));
                                    }
                                }
                            }    
                            while (true) {
                                //System.out.println("loading page " + page);
                                boolean last = true;
                                String[] rows = p.split("action=show&root=1&tvd=");
                                for (String row : rows) {
                                    if (row.contains("<!DOCTYPE")) {
                                        continue;
                                    }
                                    if (row.contains("&number=")) {
                                        continue;
                                    }
                                    Candidate candidate = new Candidate();

                                    candidate.link = "http://www.st-petersburg.vybory.izbirkom.ru/region/region/st-petersburg?action=show&root=1&tvd=" + row.substring(0, row.indexOf("\">"));
                                    //System.out.println(row);
                                    String[] d = row.split(">");

                                    candidate.name = getValue(d[1]);
                                    if (candidate.name.equals("Савина Людмила Геннадьевна")) {
                                        candidate.name = "Плотникова Людмила Геннадьевна";
                                    }
                                    if (candidate.name.equals("Скапишева Жанна Юрьевна")) {
                                        candidate.name = "Столярова Жанна Юрьевна";
                                    }



                                    candidate.birthday = getValue(d[5]);
                                    candidate.source = getValue(d[7]);
                                    candidate.districtId = Integer.parseInt(getValue(d[9]));
                                    candidate.move = getValue(d[11]);
                                    //System.out.println("d13 = " + d[13] + row);
                                    candidate.reg = getValue(d[13]);
                                    candidate.isbr = getValue(d[15]);

                                    if (mo == null) {
                                        System.out.println(rows[0]);
                                    }
                                    /*
                                    <nobr><a href="http://www.st-petersburg.vybory.izbirkom.ru/region/region/st-petersburg?action=show&amp;root=1&amp;tvd=" +
                                            "4784016148951&amp;vrn=4784016148951&amp;region=78&amp;global=&amp;sub_region=78&amp;prver=0&amp;pronetvd=null&amp;type=341&amp;vibid=4784016151524">Христенко Иван Владимирович</a>
                                    </nobr>
                                    </td>
                                    <td>02.09.1990</td>
                                    <td>Самовыдвижение</td>
                                    <td> 218</td>
                                    <td>выдвинут</td>
                                    <td>зарегистрирован</td>
                                    <td> </td>
                                    </tr>
                                      */

                                    //if (name.toLowerCase().contains("даниил")) {
                                    //if (source.toLowerCase().contains("яблоко") && reg.contains("зарегистрирован")) {
                                    //    yablokoCount++;
                                    //if (isbr.length() > 0) {


                                        String[] cd = getPage(candidate.link).split("<td align=\"center\" valign=\"top\" style=\"color:black\">");
                                        for (String val : cd) {
                                            candidate.p4 = printVal(val, "4");
                                            candidate.p5= printVal(val, "5");
                                            candidate.p6 = printVal(val, "6");
                                            candidate.p7 = printVal(val, "7");
                                            candidate.p8 = printVal(val, "8");
                                        }
                                    moCandidates.add(candidate);
                                    //}
                                    last = false;
                                }
                                if (last) {
                                    break;
                                }                                
                                page++;
                                p = getPage(pageLink + "&number=" + page);
                            }
                        }
                        if (s.contains("Данные о предварительных итогах голосования")) {
                            String p = getPage(s.substring(0, s.indexOf("\"")));
                            String[] districtLinks = p.split("<option value=\"");
                            for (String districtLink : districtLinks) {
                                if (districtLink.startsWith("http://")) {
                                    districtLink = districtLink.substring(0, districtLink.indexOf("\""));
                                    //System.out.println(districtLink.replace("&amp;", "&"));
                                    String res = getPage(districtLink);
                                    int nStart = res.indexOf(">ОИК ") + 6;
                                    int districtId = Integer.parseInt(res.substring(nStart, res.indexOf("<", nStart)));
                                    String[] rows = res.split("<tr bgcolor=\"#");
                                    int invalid = getValueFromRow(rows[10]);
                                    int valid = getValueFromRow(rows[11]);

                                    for (int r = 15; r < rows.length; r++) {
                                        String[] row = rows[r].split("</td>");
                                        String name = row[1].substring(row[1].lastIndexOf(">") + 1);
                                        String[] d = row[2].split(">");
                                        int votes = Integer.parseInt(d[2].substring(0, d[2].indexOf("<")));
                                        double percent = Double.parseDouble(d[5].substring(0, d[5].indexOf("%")));
                                        Candidate moCandidate = null;
                                        for (Candidate c : moCandidates) {
                                            if (c.districtId == districtId && c.name.equals(name) && !"утративший статус выдвинутого кандидата".equals(c.move)
                                                && !"отказ в регистрации".equals(c.reg) && !"выбывший (после регистрации) кандидат".equals(c.reg)) {
                                                if (moCandidate != null) {
                                                    throw new RuntimeException("Pair of candidates!" + name);
                                                }
                                                moCandidate = c;
                                                moCandidate.votes = votes;
                                                moCandidate.percent = percent;
                                                moCandidate.valid = valid;
                                                moCandidate.invalid = invalid;
                                            }
                                        }
                                        if (moCandidate == null) {
                                            throw new RuntimeException("Unknown candidate " + name);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Collections.sort(moCandidates, new Comparator<Candidate>() {
                        @Override
                        public int compare(Candidate o1, Candidate o2) {
                            if (o1.districtId < o2.districtId) {
                                return -1;
                            }
                            if (o1.districtId > o2.districtId) {
                                return 1;
                            }
                            if (o1.votes != null) {
                                if (o2.votes == null) {
                                    return -1;
                                }
                                if (o1.votes > o2.votes) {
                                    return -1;
                                }
                                if (o1.votes < o2.votes) {
                                    return 1;
                                }
                            } else {
                                if (o2.votes != null) {
                                    return 1;
                                }
                            }

                            int r = o1.name.compareTo(o2.name);
                            if (r != 0) {
                                return r;
                            }
                            r = o1.source.compareTo(o2.source);
                            return r;
                            //throw new RuntimeException("Equal candidate " + o1.name + " and " + o2.name);
                        }
                    });
                    for (Candidate c : moCandidates) {
                        System.out.println(mo + ", " + c.districtId + ", " + c.name + ", " + c.isbr + ", " + (c.votes != null ? Integer.toString(c.votes) : "") + ", " +
                                (c.votes != null ? Double.toString(c.percent) + "%, " + c.valid + ", " + c.invalid  : ", , ") + ", " + c.reg + ", " + c.source + ", " + c.birthday + ", " + c.move +
                                ", " + c.link + ", " + nvl(c.p4) + ", " + nvl(c.p5) + ", " + nvl(c.p6) + ", " + nvl(c.p7) + ", " + nvl(c.p8));

                    }
                }
            }
        }
    }

    public static int getValueFromRow(String row1) {
        String[] row = row1.split("</td>");
        String[] d = row[2].split(">");
        return Integer.parseInt(d[2].substring(0, d[2].indexOf("<")));
    }

    public static String nvl(String s){
        return s == null ? "" : s;
    }

    public static String printVal(String val, String id) {
        if (val.startsWith(id)) {
            val = val.substring(val.indexOf(" valign=\"top\">"));
            val = val.replace("<br>", "").replace("</br>", "");
            val = val.substring(val.indexOf(">")  +1);
            val = val.substring(0, val.indexOf("</td"));
            return val;
        }
        return null;
    }

    public static String getPage(String pageLink) throws Exception {
        return OfficialCheck.getPage(pageLink).replace("&amp;", "&");
    }

    public static String getValue(String s) {
        //System.out.println(s);
        return s.substring(0, s.indexOf("<")).trim();
    }

    public static class Candidate {
        int districtId;
        String name;
        String reg;
        String source;
        String birthday;
        String move;
        String isbr;
        String link;
        String p4,p5, p6, p7, p8;
        Integer votes = null;
        Integer valid = null;
        Integer invalid = null;
        Double percent = null;
    }
}
