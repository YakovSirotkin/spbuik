package org.spbelect;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IkmoDistrict {

    public static void main(String[] args) throws Exception {
        String root = SnapshotMaker.getPage("http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg");
        String[] links = root.split("<a href=\"");
        String endLink = "\" class=\"vibLink\">";
        int count = 0;
        System.out.println(links.length);
        for (String link : links) {
            if (link.contains(endLink)) {
                count++;
                if (count == 3) {
                    //break;
                }
                if (count > 1) {
                    String ikmoLink = link.substring(0, link.indexOf(endLink));
                    String p = getPage(ikmoLink);


                    String[] rowsh = p.split("&region=78&global=null&type=0&prver=0&pronetvd=null");
                    String mo = null;
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
                            if (mo == null) {
                                System.out.println(r.contains(prefixes[9]));
                            }
                            //System.out.println(mo);
                            //http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=show&root=784001001&tvd=4784001175336&vrn=4784001175331&prver=0&pronetvd=null&region=78&sub_region=78&type=0&vibid=4784001175336
                            //http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=show&amp;root=784001001&amp;tvd=4784001175828&amp;vrn=4784001175823&amp;prver=0&amp;pronetvd=null&amp;region=78&amp;sub_region=78&amp;type=0&amp;vibid=4784001175828    
                            String[] okrugs = p.split("<option value=\"");
                            for (String okrug : okrugs) {
                                if (okrug.contains("</option>") && !okrug.contains("Нижестоящие")) {
                                    int end = okrug.indexOf(">");
                                    String url = okrug.substring(0, end - 1);
                                    String name = okrug.substring(end + 1, okrug.indexOf("</"));
                                    String uiksPage = getPage(url.replace("&amp;", "&"));
                                    String[] uiks = uiksPage.split("<option value=\"");
                                    for (String uik : uiks) {
                                        if (uik.contains("</option>") && !uik.contains("Нижестоящие")) {
                                            end = uik.indexOf(">");
                                            //String url = okrug.substring(0, end - 1);
                                            String nameUik = uik.substring(end + 1, uik.indexOf("</"));
                                            System.out.println(mo + "," + name + "," + nameUik.substring(0, nameUik.indexOf(" ")));

                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    public static String getPage(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
        Matcher m = p.matcher(con.getContentType());
/* If Content-Type doesn't match this pre-conception, choose default and 
 * hope for the best. */
        String charset = m.matches() ? m.group(1) : "Windows-1251";
        Reader r = new InputStreamReader(con.getInputStream(), charset);
        StringBuilder buf = new StringBuilder();
        while (true) {
            int ch = r.read();
            if (ch < 0)
                break;
            buf.append((char) ch);
        }
        return buf.toString();
    }
}
