package org.spbelect;

import java.util.List;

public class SignatureResearch2 {

    public static final int NUMBER_OF_CANDIDATES = 8;

    public static void main(String[] args) throws Exception {
        /**
         *
         * 0 Бабурин Сергей Николаевич
         * 1 Грудинин Павел Николаевич
         * 2 Жириновский Владимир Вольфович
         * 3 Путин Владимир Владимирович
         * 4 Собчак Ксения Анатольевна
         * 5 Сурайкин Максим Александрович
         * 6 Титов Борис Юрьевич
         * 7 Явлинский Григорий Алексеевич
         * Исходные данные: http://www.vybory.izbirkom.ru/region/region/izbirkom?action=show&root=1&tvd=100100084849066&vrn=100100084849062&region=0&global=1&sub_region=0&prver=0&pronetvd=null&vibid=100100084849066&type=227
         */
        List<String> input = FindMatch2015.getLines("spbuik/president2018.txt");
        int[][] stat = new int[NUMBER_OF_CANDIDATES][85];
        for (int candidate = 0; candidate < NUMBER_OF_CANDIDATES; candidate++) {
            for (int region = 0; region < 85; region++) {
                String[] s = input.get(87 * candidate + region).split(" ");
                stat[candidate][region] = Integer.parseInt(s[1]);
            }
        }
        int maxConversion = 12;
        System.out.println("Количество голосов по регионам:");
        int[][] estimate = new int[8][maxConversion];
        for (int i = 0; i < NUMBER_OF_CANDIDATES; i++) {
            int sum = 0;
            for (int j = 0; j < 85; j++) {
                int v = stat[i][j];
                System.out.print(v + " ");
                sum += v;
                for (int k = 1; k < maxConversion; k++) {
                    estimate[i][k] += Math.min(2500, v / k);
                }
            }
            System.out.println();
            System.out.println(sum);
        }

        System.out.println("Количество подписей при конверсии от 1 до " + (maxConversion -1) + " голосов на подпись:");
        for (int i = 0; i < NUMBER_OF_CANDIDATES; i++) {
            System.out.println("Кандидат " + i);
            for (int j = 1; j < maxConversion; j++) {
                int v = estimate[i][j];
                System.out.print(v + " ");
            }
            System.out.println();
        }
    }
}


