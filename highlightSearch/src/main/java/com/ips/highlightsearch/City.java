package com.ips.highlightsearch;
/**
 * @author E-mail: redoforient@163.com
 * @version ：2013-3-6 上午11:09:31
 */

import java.util.HashMap;
import java.util.List;

public class City {
    public static String[] cityNames;

    public City() {
    }

    public static void setCityNames(List<HashMap<String, Object>> data) {
        int size = data.size();
        cityNames = new String[size];
        for (int i = 0; i < size; i++) {
            cityNames[i] = data.get(i).get(HighlightSearchCityActivity.KEY_DISPLAYNAME).toString();
        }
    }


}
