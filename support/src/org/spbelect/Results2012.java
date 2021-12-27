package org.spbelect;

import java.util.ArrayList;
import java.util.List;

public class Results2012 {

    public static void main(String[] args) throws Exception {
        String rootPage = SnapshotMaker.getPage("http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=show&root_a=782000001&vrn=100100031793505&region=78&global=true&type=0&sub_region=78&root=1000039&prver=0&pronetvd=null&tvd=100100031793888");
        String[] options = rootPage.split("option value=\"");
        for (String option : options) {
            if (option.startsWith("http://")) {
                String url = option.substring(0, option.indexOf("\"")).replaceAll("&amp;", "&");
                int idStart = option.indexOf(">") + 1;
                int idEnd = option.indexOf(" ", idStart);
                int tikId = Integer.parseInt(option.substring(idStart, idEnd));
                String tikMain = SnapshotMaker.getPage(url);
                int urlEnd = tikMain.indexOf("\">Сводная таблица итогов голосования</a>");
                int urlStart = tikMain.lastIndexOf("http://", urlEnd);
                String urlTable = tikMain.substring(urlStart, urlEnd).replaceAll("&amp;", "&");
                String table = SnapshotMaker.getPage(urlTable);
                String[] uiks = table.split("<nobr>УИК ");
                List<List<String>> results = new ArrayList<>();
                String last = null;
                for (String uik : uiks) {
                    if (uik.startsWith("№")) {
                        ArrayList<String> d = new ArrayList<>();
                        d.add(uik.substring(1, uik.indexOf("<")));
                        results.add(d);
                        last = uik;
                    }
                }
                String[] rows = last.split("<tr bgcolor=\"");
                for (int i = 1; i <= 24; i++) {
                    if (i == 19) {
                        continue;
                    }
                    String[] cells = rows[i].split("<b>");
                    for (int j = 1; j <= results.size(); j++) {
                        results.get(j-1).add(cells[j].substring(0, cells[j].indexOf("<")));
                    }
                }
                for (List<String> result : results) {
                    System.out.print(tikId);
                    for (String s : result) {
                        System.out.print("," + s);
                    }
                    System.out.println();
                }
            }
        }
    }
}
