package org.spbelect;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReservDownloader {
    public static void main(String[] args) throws Exception {
        String fp = OfficialCheck.getPage("http://www.st-petersburg.vybory.izbirkom.ru/st-petersburg/ik_r_tree/");
        JSONArray children = getChildren(fp);
        for (int i = 0; i < children.length(); i++) {
            JSONObject tik = (JSONObject)children.get(i);
            String tikName = tik.getString("text");
            String up = OfficialCheck.getPage("http://www.st-petersburg.vybory.izbirkom.ru/st-petersburg/ik_r_tree/" + tik.getString("id"));
            JSONArray uiks = new JSONArray(up);
            for (int j = 0; j < uiks.length(); j++) {
                JSONObject uik = uiks.getJSONObject(j);
                String uikName = uik.getString("text");
                String uh = OfficialCheck.getPage("http://www.st-petersburg.vybory.izbirkom.ru/st-petersburg/ik_r/" + uik.getString("id"));
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

    private static String getStart(String id) {
        return id.substring(0, id.indexOf("<")).trim();
    }

    private static String getName(String s) {
        s = s.trim();        
        return s.substring(s.indexOf("nobr>") + 5, s.lastIndexOf("</n"));
    }

    private static JSONArray getChildren(String fp) {
        JSONArray a = new JSONArray(fp);
        JSONObject o = (JSONObject)a.get(0);
        return o.getJSONArray("children");
    }
}
