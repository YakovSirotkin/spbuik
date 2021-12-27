package org.spbelect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ReservDownloader {
    public static void main(String[] args) throws Exception {
        //downloadReserv();
        downloadUiks();
    }

    public static void downloadReserv() throws Exception {
        String fp = SnapshotMaker
                .getPage("http://www.st-petersburg.vybory.izbirkom.ru/st-petersburg/ik" + "_tree/");
        JSONArray children = getChildren(fp);
        for (int i = 0; i < children.length(); i++) {
            JSONObject tik = (JSONObject) children.get(i);
            String tikName = tik.getString("text");
            String up = SnapshotMaker
                    .getPage("http://www.st-petersburg.vybory.izbirkom.ru/st-petersburg/ik" + "_tree/" + tik.getString("id"));
            JSONArray uiks = new JSONArray(up);
            for (int j = 0; j < uiks.length(); j++) {
                JSONObject uik = uiks.getJSONObject(j);
                String uikName = uik.getString("text");
                String uh = SnapshotMaker
                        .getPage("http://www.st-petersburg.vybory.izbirkom.ru/st-petersburg/ik" + "/" + uik.getString("id"));
                String[] rows = uh.split("<tr>");
                for (int k = 3; k < rows.length; k++) {
                    String[] r = rows[k].split("<td>");
                    String id = r[1].trim();
                    id = getStart(id);
                    System.out.println(tikName + "," + uikName + "," + id + "," +
                            getName(r[2]) + "," + getStart(r[4].trim()));
                }
            }
        }
    }

    public static void downloadUiks() throws Exception {
        String suffix = //"";
                "_r";
        String fp = SnapshotMaker.getPage("http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=ikTree&region=78&vrn=27820001006425&id=%23");
        JSONArray children = getChildren(fp);
        for (int i = 0; i < children.length(); i++) {
            JSONObject tik = (JSONObject) children.get(i);
            String tikName = tik.getString("text");
            String tikCode = tik.getString("id");
            List<String> uikLinks = GetUikLinks.getUikLinks(tikCode);

            for (String uikLink : uikLinks) {
                String uh = SnapshotMaker.getPage(uikLink);
                String uikIdPrefix = "<h2>Участковая избирательная комиссия ";
                int idStart = uh.indexOf(uikIdPrefix) + uikIdPrefix.length() + 1;
                int idFinish = uh.indexOf("</h2>", idStart);
                int uikId = Integer.parseInt(uh.substring(idStart, idFinish).replace("\"Д.М. Карбышева\"", "").trim());

                String[] rows = uh.split("<tr>");
                for (int k = 5; k < rows.length; k++) {
                    String[] r = rows[k].split("<td>");
                    String id = r[1].trim();
                    id = getStart(id);

                    System.out.println(tikName + "," + uikId + "," + id + "," +
                            getName(r[2]) + "," + getStart(r[4].trim()));
                }
            }
        }
    }

    private static String getStart(String id) {
        return id.substring(0, id.indexOf("<")).trim();
    }

    private static String getName(String s) {
        s = s.trim();
        return s.substring(s.indexOf("nobr>") + 5, s.lastIndexOf("</n"));
    }

    private static JSONArray getChildren(String fp) {
        JSONArray a = new JSONArray(fp);
        JSONObject o = (JSONObject) a.get(0);
        return o.getJSONArray("children");
    }
}
