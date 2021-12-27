package org.spbelect;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SnapshotMaker {

    static HttpClient client = HttpClients.createDefault();

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
                    String page = null;
                    try {
                        page = getPage(uikLink);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.sleep(10000);
                        page = getPage(uikLink);
                    }

                    //System.out.println(uikLink);
                    String uikIdPrefix = "<h2>Участковая избирательная комиссия ";
                    int idStart = page.indexOf(uikIdPrefix) + uikIdPrefix.length() + 1;
                    int idFinish = page.indexOf("</h2>", idStart);
                    //System.out.println(idStart + " " + idFinish + " " + uikLink);
                    if (idFinish == -1) {
                        System.out.println("Error: " + page);
                        continue;
                    }
                    String uidIdStr = page.substring(idStart, idFinish).replace("\"Д.М. Карбышева\"", "").trim();
                    if (uidIdStr.length() > 100) {
                        uidIdStr = "1139";
                        System.out.println("ц3 -> 1139");
                    }
                    int uikId = Integer.parseInt(uidIdStr);
                    //System.out.println("Processing uidId " + uikId);
                    System.out.println(uikId + "," + uikLink);

                    int pos = page.indexOf("Кем предложен в состав комиссии", idFinish);
                    String nobr = "<nobr>";
                    pos = page.indexOf(nobr, pos);
                    do {
                        pos += nobr.length();
                        int end = page.indexOf("</nobr>", pos);
                        if (end < 0) {
                            System.out.println("Никого нет в УИК " + uikId);
                            break;
                        }

                        int prevClose = page.lastIndexOf("</td>", pos);
                        int prevOpen = page.lastIndexOf("<td>", prevClose);
                        if (prevOpen < 0) {
                            System.out.println("No info for " + uikId + " " + uikLink);
                            break;
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

                        who = who.replace("Член комиссии", "прг").replaceAll("\\s+","");
                        if (who.equals("Председатель")) {
                            who = "председатель";
                        }
                        if (who.equals("Зам.председателя")) {
                            who = "заместитель";
                        }
                        if (who.equals("Секретарь")) {
                            who = "секретарь";
                        }
                        out.println(tikId + "|" + uikId + "|" + id + "|" + name + "|" + who + "|" + from);
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
        HttpGet request = new HttpGet(urlString);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        request.addHeader("Connection", "keep-alive");
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("Upgrade-Insecure-Requests", "1");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.85 YaBrowser/21.11.3.927 Yowser/2.5 Safari/537.36");
        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        request.addHeader("Accept-Language", "ru,en;q=0.9,it;q=0.8,hu;q=0.7,fr;q=0.6,la;q=0.5,nl;q=0.4,de;q=0.3");
        //request.addHeader("Cookie", "_ga=GA1.2.2136833457.1621191061; __utma=252441553.2136833457.1621191061.1621191133.1621576777.2; _ym_uid=1541048061592454066; _ym_d=1625522559; session-cookie=16c463d6587392cb4dcaf2bc80267f934d9d369210d536ec99dd1cdf7594cc5a5e41d0b3cb1865ba48813f488a297738");
        request.addHeader("Host", "www.st-petersburg.vybory.izbirkom.ru");
        request.addHeader("Accept-Encoding", "gzip, deflate");
        HttpResponse response = client.execute(request);

        // Get HttpResponse Status
        System.out.println(response.getProtocolVersion());              // HTTP/1.1
        System.out.println(response.getStatusLine().getStatusCode());   // 200
        System.out.println(response.getStatusLine().getReasonPhrase()); // OK
        System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            // return it as a String
            String result = EntityUtils.toString(entity);
            //System.out.println("result = " + result);
            return result;
        } else {
            throw new Exception("No result");

        }
    }
}
