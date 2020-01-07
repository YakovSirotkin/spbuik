package org.spbelect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnapshotMaker {

    public static void main(String[] args) throws Exception {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("spbuik\\uikOfficial.txt"), StandardCharsets.UTF_8));
        JSONArray tikJson = new JSONArray(getPage("http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=ikTree&region=78&vrn=27820001006425&id=%23"))
                .getJSONObject(0).getJSONArray("children");
        int total = 0;
        for (int k = 0; k < tikJson.length(); k++) {
            JSONObject json = tikJson.getJSONObject(k);
            String text = json.getString("text");
            if (text.length() < 15) {
                int tikId = Integer.parseInt(text.substring(6).trim());
                String code = json.getString("id");
                List<String> uikLinks = GetUikLinks.getUikLinks(code);
                for (String uikLink : uikLinks) {
                    String page = getPage(uikLink);
                    //System.out.println(uikLink);
                    String uikIdPrefix = "<h2>Участковая избирательная комиссия ";
                    int idStart = page.indexOf(uikIdPrefix) + uikIdPrefix.length() + 1;
                    int idFinish = page.indexOf("</h2>", idStart);
                    //System.out.println(idStart + " " + idFinish + " " + uikLink);
                    if (idFinish == -1) {
                        System.out.println("Error: " + page);
                        continue;
                    }
                    int uikId = Integer.parseInt(page.substring(idStart, idFinish).replace("\"Д.М. Карбышева\"", "").trim());


                    int pos = page.indexOf("Кем предложен в состав комиссии", idFinish);
                    String nobr = "<nobr>";
                    pos = page.indexOf(nobr, pos);
                    do {
                        pos += nobr.length();
                        int end = page.indexOf("</nobr>", pos);
                        if (end < 0) {
                            System.out.println("Никого нет в УИК " + uikId);
                        }

                        int prevClose = page.lastIndexOf("</td>", pos);
                        int prevOpen = page.lastIndexOf("<td>", prevClose);
                        if (prevOpen < 0) {
                            System.out.println("No info for " + uikId + " " + uikLink);
                        }
                        String id = page.substring(prevOpen + 4, prevClose).trim();

                        String name = page.substring(pos, end).trim();

                        String td = "<td>";
                        pos = page.indexOf(td, end) + td.length();
                        end = page.indexOf("</td>", pos);
                        String who = page.substring(pos, end);
                        pos = page.indexOf(td, end) + td.length();
                        end = page.indexOf("</td>", pos);
                        String from = page.substring(pos, end);
                        pos = page.indexOf(nobr, pos);


                        if (from.contains("\"ЕДИНАЯ РОССИЯ\"")) {
                            from = "ЕР";
                        }
                        if (from.contains("ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ")) {
                            from = "ПАРТИЯ ЗА СПРАВЕДЛИВОСТЬ";
                        }
                        if (from.contains("СПРАВЕДЛИВАЯ РОССИЯ")) {
                            from = "СР";
                        }
                        if (from.contains("КОММУНИСТИЧЕСКАЯ ПАРТИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ")) {
                            from = "КПРФ";
                        }
                        if (from.contains("Либерально-демократическая партия России") || from.contains("ЛДПР")) {
                            from = "ЛДПР";
                        }

                        if (from.contains("собрание избирателей по месту работы")) {
                            from = "собрание-работа";
                        }
                        if (from.contains("собрание избирателей по месту жительства")) {
                            from = "собрание-дом";
                        }

                        if (from.contains("собрание избирателей по месту учебы")) {
                            from = "собрание-учеба";
                        }


                        if (from.contains("ПАРТИЯ РОСТА")) {
                            from = "ПАРТИЯ РОСТА";
                        }

                        if (from.contains("ЯБЛОКО")) {
                            from = "ЯБЛОКО";
                        }

                        if (from.contains("РОДИНА")) {
                            from = "РОДИНА";
                        }

                        if (from.contains("Гражданская Платформа")) {
                            from = "Гражданская Платформа";
                        }

                        if (from.contains("ПАТРИОТЫ РОССИИ")) {
                            from = "ПАТРИОТЫ РОССИИ";
                        }


                        if (from.contains("Трудовая партия России")) {
                            from = "Трудовая партия России";
                        }
                        if (from.contains("Зелёные")) {
                            from = "Зелёные";
                        }
                        if (from.contains("За женщин России")) {
                            from = "За женщин России";
                        }

                        if (from.contains("КОММУНИСТИЧЕСКАЯ ПАРТИЯ КОММУНИСТЫ РОССИИ")) {
                            from = "КОММУНИСТЫ РОССИИ";
                        }

                        if (from.contains("СОЦИАЛЬНОЙ ЗАЩИТЫ")) {
                            from = "СОЦИАЛЬНОЙ ЗАЩИТЫ";
                        }

                        if (who.equals("Председатель")) {
                            who = "председатель";
                        }
                        if (who.equals("Зам.председателя")) {
                            who = "заместитель";
                        }
                        if (who.equals("Секретарь")) {
                            who = "секретарь";
                        }
                        if (who.equals("Член")) {
                            who = "прг";
                        }
                        out.println(tikId + "|" + uikId + "|" + id + "|" + name + "|"  + who + "|" + from);
                        total++;
                    } while (pos > 0);
                }
            }
        }
        out.close();
        System.out.println(total + " membvers in uiks");
        //uikTab.close();
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
