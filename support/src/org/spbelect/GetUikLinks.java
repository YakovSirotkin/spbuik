package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GetUikLinks {
    public static void main(String[] args) throws Exception {
        getUikLinks();
    }

    public static List<String> getUikLinks() throws Exception {
        List<String> uikLinks = new ArrayList<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik/ttiklinks.txt")), "UTF-8"));
        String s = null;
        while ((s = in.readLine()) != null) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            String page = OfficialCheck.getPage(s);
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
        }
        return uikLinks;
    }
}
