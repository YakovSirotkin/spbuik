package org.spbelect;

public class GetMoCandidates {
    public static void main(String[] args) throws Exception {
        String root = OfficialCheck.getPage("http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg");
        String[] links = root.split("<a href=\"");
        String endLink = "\" class=\"vibLink\">";
        int count = 0;
        int count2 = 0;
        System.out.println(links.length);
        for (String link : links) {
            if (link.contains(endLink)) {
                count++;
                if (count ==3) {
                    //break;
                }
                if (count > 1) {
                    String ikmoLink = link.substring(0, link.indexOf(endLink));
                    String imkPage = OfficialCheck.getPage(ikmoLink).replace("&amp;", "&");
                    String[] candidatesLink = imkPage.split("<a href=\"");
                    for (String s : candidatesLink) {
                        if (s.contains("Сведения о кандидатах")) {
                            String pageLink = s.substring(0, s.indexOf("\""));
                            //System.out.println(pageLink);
                            int page = 1;
                            String p = OfficialCheck.getPage(pageLink).replace("&amp;", "&");
                            while (true) {
                                //System.out.println("loading page " + page);
                                boolean last = true;
                                String[] rows = p.split("action=show&root=1&tvd=");
                                String mo = null;
                                for (String row : rows) {
                                    if (row.contains("</html>")) {
                                        break;
                                    }
                                    if (row.contains("&number=")) { 
                                        break;
                                    }
                                    row = row.replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ").replace("  ", " ");
                                    if (row.toLowerCase().contains(">Избирательная комиссия".toLowerCase()) || row.contains(">Территориальная избирательная")) {
                                        final String[] prefixes = new String[] {
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

                                        
                                        for (String prefix : prefixes) {                                            
                                            if (mo == null && row.toLowerCase().contains(prefix.toLowerCase())) {
                                                mo = row.substring(row.toLowerCase().indexOf(prefix.toLowerCase())).substring(prefix.length()).trim();
                                                mo = mo.substring(0, mo.indexOf("<"))
                                                        .replace("\"", "")
                                                        .replace("Санкт-Петербурга", "")
                                                        .replace("округ","")
                                                        .trim();
                                                break;
                                            } 
                                        }
                                        if(mo == null) {
                                            System.out.println(row.contains(prefixes[9]));
                                        }
                                        continue;
                                    }
                                    //System.out.println(row);
                                    String rLink = "http://www.st-petersburg.vybory.izbirkom.ru/region/region/st-petersburg?action=show&root=1&tvd=" + row.substring(0, row.indexOf("\">"));
                                    //System.out.println(row);
                                    String[] d = row.split(">");
                                    String name = getValue(d[1]);
                                    String birthday = getValue(d[5]);
                                    String source = getValue(d[7]);
                                    String district = getValue(d[9]);
                                    String move = getValue(d[11]);
                                    String reg = getValue(d[13]);
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
                                    System.out.println(mo + ", " +  district + ", " + name + ", " + reg + ", " + source + ", " + birthday + ", " + move + ", " + rLink);
                                    last = false;
                                }
                                if (last) {
                                    break;
                                }                                
                                page++;
                                p = OfficialCheck.getPage(pageLink + "&number=" + page);
                            }
                            count2++;
                        }
                    }
                }
            }
        }
        System.out.println(count2);
    }

    public static String getValue(String s) {
        //System.out.println(s);
        return s.substring(0, s.indexOf("<")).trim();
    }
}
