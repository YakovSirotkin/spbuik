package org.spbelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class GetUikLinks {
    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("spbuik/ttiklinks.txt")), "UTF-8"));
        String s = null;
        while ((s = in.readLine()) != null) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            String page = OfficialCheck.getPage(s);
            //System.out.println(page);
            String prefix = "/st-petersburg/ik/";
            int pos = page.indexOf(prefix);
            while (pos > 0) {
                int end = page.indexOf("\"", pos);
                System.out.println("http://www.st-petersburg.vybory.izbirkom.ru/" + page.substring(pos, end));
                pos = page.indexOf(prefix, end);                
            }
        }
    }    
}
