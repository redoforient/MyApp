package com.ips.highlightsearch;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.xmlpull.v1.XmlPullParserException;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HighlightSearchCityActivity extends AppCompatActivity {
    private static final String TAG = "高亮显示搜索城市";
    public static final String KEY_ID = "id"; // value: String
    public static final String KEY_DISPLAYNAME = "name"; // value: String
    private static final String KEY_GMT = "gmt"; // value: String
    private static final String KEY_OFFSET = "offset"; // value: int (Integer)
    public static final String KEY_CITYINDEX = "cityindex"; // value: int
    private static final String XMLTAG_TIMEZONE = "timezone";

    private static final int HOURS_1 = 60 * 60000;

    private EditText input_tv;
    private ListView listview;
    private TimeZone mTimeZone;
    private ArrayList<HashMap<String, Object>> data;
    private TextView mHint;

    static int mCityIndex = 0;
    static List<HashMap<String, Object>> mData = null;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.world_city_list);
        mTimeZone = TimeZone.getDefault();
        data = (ArrayList<HashMap<String, Object>>) getZones(this);
        Collections.sort(data, new zoneComparator(KEY_DISPLAYNAME));
        init();
        new UpdateListTask().execute(getKeyWord());

        input_tv.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mHint.setVisibility(View.GONE);
                return false;
            }
        });

        input_tv.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 0) {
                    mHint.setVisibility(View.GONE);
                } else {
                    mHint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                new UpdateListTask().execute(getKeyWord());
            }
        });


        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Map<?, ?> map = (Map<?, ?>) parent.getItemAtPosition(position);
                String tzId = map.get(KEY_ID).toString();
                String city = map.get(KEY_DISPLAYNAME).toString();
                String index = map.get(KEY_CITYINDEX).toString();
                Log.i(TAG, map.toString());
            }
        });
    }

    protected void onRestart() {
        new UpdateListTask().execute(getKeyWord());
        super.onRestart();
    }

    public void init() {
        input_tv = (EditText) findViewById(R.id.search_box);
        mHint = (TextView) findViewById(R.id.hint);
        listview = (ListView) findViewById(android.R.id.list);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                CharSequence str = "";
                str = input_tv.getText();
                if (str != null && str.length() > 0) {
                    String temp = str.toString();
                    input_tv.setTextKeepState(temp.substring(0, temp.length()));
                }
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    /**
     * Customize Adapter, so that realize highlighted keywords
     */
    private class HighLightKeywordsAdapter extends BaseAdapter {
        private List<HashMap<String, String>> list;
        private Context context;
        private String[] from;
        private int[] to;
        private int layoutid;
        LayoutInflater myInflater;
        HashMap<String, String> ZoneCityNameGmt;

        public ArrayList<String> IndexToName(ArrayList<String> beChooseCitys) {
            ArrayList<String> beChooseNames = new ArrayList<String>();
            for (int i = 0; i < beChooseCitys.size(); i++) {
                beChooseNames.add(City.cityNames[Integer.valueOf(beChooseCitys.get(i))]);
            }
            return beChooseNames;
        }

        public HighLightKeywordsAdapter(Context context, List<HashMap<String, String>> list,
                                        int layoutid, String[] from, int[] to) {
            this.context = context;
            this.list = list;
            this.from = from;
            this.to = to;
            this.layoutid = layoutid;
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            myInflater = LayoutInflater.from(context);
            try {
                ZoneCityNameGmt = (HashMap<String, String>) list.get(position);
                convertView = myInflater.inflate(layoutid, null);
                convertView.setTag(ZoneCityNameGmt);
            } catch (Exception e) {
                Log.e(TAG, "Hight Key Error! ");
            }

            View item = convertView.findViewById(to[0]);
            View description = convertView.findViewById(to[1]);
            if (item instanceof TextView && description instanceof TextView) {
                TextView item_tv = (TextView) item;
                String item_value = "";
                item_value = ZoneCityNameGmt.get(from[0]).toString();

                String input = input_tv.getText().toString();

                String lower_item_value = item_value.toLowerCase();
                String lower_input_value = input.toLowerCase();
                if (lower_item_value.contains(lower_input_value)) {
                    int start = lower_item_value.indexOf(lower_input_value);
                    SpannableStringBuilder style_string = new SpannableStringBuilder(item_value);
                    style_string.setSpan(new ForegroundColorSpan(Color.RED), start,
                            start + input.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    item_tv.setText(style_string);
                }

                TextView description_tv = (TextView) description;
                String description_value = ZoneCityNameGmt.get(from[1]).toString();
                description_tv.setText(description_value);

                String beShowedCity = item_tv.getText().toString();
                Log.e(TAG, "显示的时区(城市):" + beShowedCity);
            }
            return convertView;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private synchronized List<HashMap<String, String>> updateNumberslist(String input) {
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (input == null) {
            for (int i = 0; i < data.size(); i++) {
                HashMap<String, String> zone_info = (HashMap) data.get(i);
                list.add(zone_info);
            }
        } else {
            for (int i = 0; i < data.size(); i++) {
                String zoneCityName = data.get(i).get(KEY_DISPLAYNAME)
                        .toString();
                String zoneGmt = data.get(i).get(KEY_GMT).toString();
                String zoneId = data.get(i).get(KEY_ID).toString();

                if (zoneCityName.toLowerCase().contains(input.toLowerCase())) {
                    HashMap<String, String> zone_info = new HashMap<>();
                    zone_info.put(KEY_DISPLAYNAME, zoneCityName);
                    zone_info.put(KEY_GMT, zoneGmt);
                    zone_info.put(KEY_ID, zoneId);
                    list.add(zone_info);
                }
            }
        }

        return list;
    }

    private String getKeyWord() {
        if (input_tv.getText() != null && input_tv.getText().length() > 0) {
            return input_tv.getText().toString();
        } else {
            return null;
        }
    }

    private class UpdateListTask extends AsyncTask<String, Integer, BaseAdapter> {
        protected BaseAdapter doInBackground(String... params) {

            BaseAdapter listAdapter = new HighLightKeywordsAdapter(HighlightSearchCityActivity.this,
                    updateNumberslist(getKeyWord()), R.layout.custom_row, new String[]{
                    KEY_DISPLAYNAME, KEY_GMT
            }, new int[]{
                    R.id.item, R.id.description
            });

            return listAdapter;
        }

        protected void onPostExecute(BaseAdapter result) {
            listview.setAdapter(result);

            // put system zone on listview first item
            for (int i = 0; i < data.size(); i++) {
                if (mTimeZone.getID().equals(data.get(i).get(KEY_ID))) {
                    listview.setSelection(i);
                }
            }
        }

    }

    protected static List<HashMap<String, Object>> getZones(Context context) {
        mData = new ArrayList<HashMap<String, Object>>();
        mCityIndex = 0;
        final long date = Calendar.getInstance().getTimeInMillis();
        try {
            XmlResourceParser xrp = context.getResources().getXml(R.xml.timezones);
            if (xrp != null) {
                while (xrp.next() != XmlResourceParser.START_TAG)
                    continue;
                xrp.next();
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                        if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                            return mData;
                        }
                        xrp.next();
                    }
                    if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                        String id = xrp.getAttributeValue(0);
                        String displayName = xrp.nextText();
                        addItem(mData, id, displayName, date, mCityIndex);
                        mCityIndex++;
                    }
                    while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                        xrp.next();
                    }
                    xrp.next();
                }
                xrp.close();
            }
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Ill-formatted timezones.xml file");
        } catch (java.io.IOException ioe) {
            Log.e(TAG, "Unable to read timezones.xml file");
        }
        City.setCityNames(mData);
        return mData;
    }

    private static class zoneComparator implements Comparator<HashMap<?, ?>> {
        private String mSortingKey;
        RuleBasedCollator collator;

        public zoneComparator(String sortingKey) {
            mSortingKey = sortingKey;
            collator = (RuleBasedCollator) Collator.getInstance(Locale.getDefault());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public int compare(HashMap<?, ?> map1, HashMap<?, ?> map2) {
            Object value1 = map1.get(mSortingKey);
            Object value2 = map2.get(mSortingKey);

            /*
             * This should never happen, but just in-case, put non-comparable
             * items at the end.
             */
            return collator.compare(value1.toString(), value2.toString()) < 0 ? -1 : 1;
        }
    }

    private static void addItem(List<HashMap<String, Object>> myData, String id,
                                String displayName, long date, int cityIndex) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_ID, id);
        map.put(KEY_DISPLAYNAME, displayName);
        map.put(KEY_CITYINDEX, cityIndex);
        final TimeZone tz = TimeZone.getTimeZone(id);
        final int offset = tz.getOffset(date);
        final int p = Math.abs(offset);
        final StringBuilder name = new StringBuilder();
        name.append(KEY_GMT);

        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }

        name.append(p / (HOURS_1));
        name.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);

        map.put(KEY_GMT, name.toString());
        map.put(KEY_OFFSET, offset);

        myData.add(map);
    }

}
