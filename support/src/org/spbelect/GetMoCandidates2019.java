package org.spbelect;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GetMoCandidates2019 {

    private static OutputStreamWriter out = null;
    public static void main(String[] args) throws Exception {
        String root = OfficialCheck.getPage("http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?start_date=01.09.2019&urovproved=all&vidvibref=all&vibtype=all&end_date=14.09.2019&sxemavib=all&action=search_by_calendar&region=78");

        Date date = new Date();
        out = new OutputStreamWriter(new FileOutputStream("spbuik\\candidates2019_0" + (date.getMonth() + 1) + "_" + date.getDate() + ".csv"));
        String[] links = root.split("<a href=\"");
        String endLink = "\" class=\"vibLink\">";
        int count = 0;
        for (String link : links) {
            if (link.contains(endLink)) {
                System.out.println(link);
                count++;
                if (count > 1) {
                    String ikmoLink = link.substring(0, link.indexOf(endLink));
                    String imkPage = getPage(ikmoLink);
                    if (!imkPage.contains("Прометей")) {
                        //continue;
                    }

                    String[] candidatesLink = imkPage.split("<a href=\"");
                    String mo = null;
                    List<Candidate> moCandidates = new ArrayList<>();
                    for (String s : candidatesLink) {
                        if (s.contains("Сведения о кандидатах")) {
                            String pageLink = s.substring(0, s.indexOf("\""));
                            //println(pageLink);
                            int page = 1;
                            String p = getPage(pageLink);
                            String[] rowsh = p.split("action=show&root=1&tvd=");

                            for (String r : rowsh) {
                                r = r.replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ");
                                if (r.toLowerCase().contains(">Избирательная комиссия".toLowerCase()) || r.contains(">Территориальная избирательная") || r.contains(">ИКМО")) {
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
                                            "Избирательная комиссия внутригородского муниципального образования",
                                            "ИКМО муниципальное образование муниципальный округ"

                                    };


                                    if (r.contains(">Территориальная избирательная комиссия № 15")) {
                                        mo = "Кронштадт";
                                    } else if (r.contains(">Территориальная избирательная комиссия № 29")) {
                                        if (p.contains("№ 75")) {
                                            mo = "№ 75";
                                        }
                                        if (p.contains("Балканский")) {
                                            mo = "Балканский";
                                        }
                                    } else if (r.contains(">Территориальная избирательная комиссия № 23")) {
                                        mo = "Купчино";
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
                                        println(r.contains(prefixes[9]));
                                    }
                                }
                            }    
                            while (true) {
                                //println("loading page " + page);
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


                                    int endIndex = row.indexOf("\">");
                                    if (endIndex < 0) {
                                        System.out.println("pageLink = " + pageLink);
                                        continue;
                                    }
                                    candidate.link = "http://www.st-petersburg.vybory.izbirkom.ru/region/region/st-petersburg?action=show&root=1&tvd=" + row.substring(0, endIndex);
                                    //println(row);
                                    String[] d = row.split(">");

                                    candidate.name = getValue(d[1]);


                                    candidate.birthday = getValue(d[5]);
                                    candidate.source = getValue(d[7]);
                                    candidate.districtId = Integer.parseInt(getValue(d[9]));
                                    candidate.move = getValue(d[11]);
                                    //println("d13 = " + d[13] + row);
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
                                            candidate.p3 = printVal(candidate.p3, val, "3");
                                            candidate.p4 = printVal(candidate.p4, val, "4");
                                            candidate.p5= printVal(candidate.p5, val, "5");
                                            candidate.p6 = printVal(candidate.p6, val, "6");
                                            candidate.p7 = printVal(candidate.p7, val, "7");
                                            candidate.p8 = printVal(candidate.p8, val, "8");
                                            candidate.p9 = printVal(candidate.p9, val, "9");
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
                        if (false && s.contains("Данные о предварительных итогах голосования")) {
                            String p = getPage(s.substring(0, s.indexOf("\"")));
                            String[] districtLinks = p.split("<option value=\"");
                            for (String districtLink : districtLinks) {
                                if (districtLink.startsWith("http://")) {
                                    districtLink = districtLink.substring(0, districtLink.indexOf("\""));
                                    //println(districtLink.replace("&amp;", "&"));
                                    String res = getPage(districtLink);
                                    int nStart = res.indexOf(">ОИК ") + 6;
                                    int endIndex = res.indexOf("<", nStart);
                                    if (endIndex < 0) {
                                        println("Error: " + districtLink);

//Ghttp://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=show&root=784001002&tvd=4784001175152&vrn=4784001175146&prver=0&pronetvd=null&region=78&sub_region=78&type=0&vibid=4784001175152
//Bhttp://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=show&root=784001001&tvd=4784001175151&vrn=4784001175146&prver=0&pronetvd=null&region=78&sub_region=78&type=426&vibid=4784001175151
                                        continue;
                                    }
                                    int districtId = Integer.parseInt(res.substring(nStart, endIndex));
                                    System.out.println(res);
                                    String[] rows = res.split("<tr bgcolor=\"#");
                                    int invalid = getValueFromRow(rows[10]);
                                    int valid = getValueFromRow(rows[11]);

                                    for (int r = 15; r < rows.length; r++) {
                                        String[] row = rows[r].split("</td>");
                                        String name = row[1].substring(row[1].lastIndexOf(">") + 1);
                                        if ("Авдушева Мария Михайловна".equals(name) &&
                                                "http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=show&root=784001001&tvd=4784001175256&vrn=4784001175251&prver=0&pronetvd=null&region=78&sub_region=78&type=426&vibid=4784001175256"
                                                .equals(districtLink)){
                                            name = "Рыбчак Мария Михайловна";
                                        }
                                        if ("Крупенина Юлия Петровна".equals(name) &&
                                                "http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=show&root=784012001&tvd=4784012139454&vrn=4784012139449&prver=0&pronetvd=null&region=78&sub_region=78&type=426&vibid=4784012139454"
                                                        .equals(districtLink)){
                                            name = "Пудовкина Юлия Петровна";
                                        }


                                        String[] d = row[2].split(">");
                                        int votes = Integer.parseInt(d[2].substring(0, d[2].indexOf("<")));
                                        //double percent = Double.parseDouble(d[5].substring(0, d[5].indexOf("%")));
                                        Candidate moCandidate = null;
                                        for (Candidate c : moCandidates) {
                                            if (c.districtId == districtId && c.name.replace("ё","е").equals(name.replace("ё","е")) && !"утративший статус выдвинутого кандидата".equals(c.move)
                                                && !"отказ в регистрации".equals(c.reg) && !"выбывший (после регистрации) кандидат".equals(c.reg)) {
                                                if (moCandidate != null) {
                                                    throw new RuntimeException("Pair of candidates!" + name);
                                                }
                                                moCandidate = c;
                                                moCandidate.votes = votes;
                                                //moCandidate.percent = percent;
                                                moCandidate.valid = valid;
                                                moCandidate.invalid = invalid;
                                            }
                                        }
                                        if (moCandidate == null) {
                                            println("Error " + name + " " + districtLink);
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
                        //if (c.isbr.contains("сложивший")) {
                            println(mo + "|" + c.districtId + "|" + c.name + "|" + c.isbr + "|" + (c.votes != null ? Integer.toString(c.votes) : "") + "|" +
                                    //(c.votes != null ? Double.toString(c.percent) + "%|" + c.valid + "|" + c.invalid  : "||") + "|" +
                                    c.reg + "|" + c.source + "|" + c.birthday + "|" + c.move +
                                    "|" + c.link + "|" + nvl(c.p3) + "|" + nvl(c.p4) + "|" + nvl(c.p5) + "|" + nvl(c.p6) + "|" + nvl(c.p7) + "|" + nvl(c.p8) + "|" + nvl(c.p9));
                        //}

                    }
                }
            }
            out.flush();
        }
        out.close();
    }

    public static int getValueFromRow(String row1) {
        String[] row = row1.split("</td>");
        String[] d = row[2].split(">");
        return Integer.parseInt(d[2].substring(0, d[2].indexOf("<")));
    }

    public static String nvl(String s){
        return s == null ? "" : s;
    }

    public static String printVal(String old, String val, String id) {
        if (old != null) {
            return old;
        }
        if (val.startsWith(id)) {
            val = val.substring(val.indexOf(" valign=\"top\">"));
            val = val.replace("<br>", "").replace("</br>", "");
            val = val.substring(val.indexOf(">")  +1);
            val = val.substring(0, val.indexOf("</td"));
            return val.trim();
        }
        return null;
    }

    public static String getPage(String pageLink) throws Exception {
        return OfficialCheck.getPage(pageLink).replace("&amp;", "&");
    }

    public static String getValue(String s) {
        //println(s);
        return s.substring(0, s.indexOf("<")).trim();
    }

    public static void println(Object s) throws IOException {
        out.write(s +"\n");
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
        String p3, p4,p5, p6, p7, p8, p9;
        Integer votes = null;
        Integer valid = null;
        Integer invalid = null;
        Double percent = null;
    }
}
