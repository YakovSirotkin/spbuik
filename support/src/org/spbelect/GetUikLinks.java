package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GetUikLinks {

    public static List<String> getUikLinks(String tikCode) throws Exception {
        List<String> uikLinks = new ArrayList<>();
            String page = OfficialCheck.getPage("http://www.st-petersburg.vybory.izbirkom.ru/region/st-petersburg?action=ikTree&region=78&vrn="
                    + tikCode + "&onlyChildren=true&id=" + tikCode);
            //System.out.println(page);
            String prefix = "id\":\"";
            int pos = page.indexOf(prefix);
            while (pos > 0) {
                int end = page.indexOf("\"", pos + prefix.length());
                String url = "http://www.st-petersburg.vybory.izbirkom.ru/st-petersburg/ik/" + page.substring(pos + prefix.length(), end);
                //System.out.println(url);
                uikLinks.add(url);
                pos = page.indexOf(prefix, end);                
            }
        return uikLinks;
    }
}
