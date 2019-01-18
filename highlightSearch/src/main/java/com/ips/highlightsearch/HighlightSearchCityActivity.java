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
import android.widget.Toast;

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
    private static final String TAG = "高亮显示搜索时区";
    public static final String KEY_ID = "id"; // value: String
    public static final String KEY_DISPLAYNAME = "name"; // value: String
    private static final String KEY_GMT = "gmt"; // value: String
    private static final String KEY_OFFSET = "offset"; // value: int (Integer)
    public static final String KEY_INDEX = "index"; // value: int
    private static final String XMLTAG_TIMEZONE = "timezone";

    private static final int HOURS_1 = 60 * 60000;

    private EditText mEtInput;
    private ListView listview;
    private TimeZone mTimeZone;
    private ArrayList<HashMap<String, Object>> data;
    private TextView mHint;

    static int mCityIndex = 0;
    static List<HashMap<String, Object>> mData = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.world_city_list);
        mTimeZone = TimeZone.getDefault();
        data = (ArrayList<HashMap<String, Object>>) getZones(this);
        //按时区名称进行排序
        Collections.sort(data, new ZoneComparator(KEY_DISPLAYNAME));
        init();
        new UpdateListTask().execute(getKeyWord());

        mEtInput.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mHint.setVisibility(View.GONE);
                return false;
            }
        });

        mEtInput.addTextChangedListener(new TextWatcher() {

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
                //第一种获取列表选项值的方法:通过parent
                final Map<?, ?> map = (Map<?, ?>) parent.getItemAtPosition(position);
                String tzId = map.get(KEY_ID).toString();
                String city = map.get(KEY_DISPLAYNAME).toString();
                String gmt = map.get(KEY_GMT).toString();
                String index = "空";
                if(map.containsKey(KEY_INDEX)){
                    index = map.get(KEY_INDEX).toString();
                }
                Toast.makeText(HighlightSearchCityActivity.this,
                        "时区ID:" + tzId + " 时区名称:" + city + " 格林威治时间:" + gmt
                        +" index:"+index,
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG, map.toString());


                //第二种获取列表选项值的方法：通过view
                TextView tvTimeZoneId = (TextView) view.findViewById(R.id.item);
                TextView tvTimeZoneDesc = (TextView) view.findViewById(R.id.description);
                String timeZoneItem = tvTimeZoneId.getText().toString();
                String timeZoneDesc = tvTimeZoneDesc.getText().toString();
                Toast.makeText(HighlightSearchCityActivity.this, "时区:" + timeZoneItem + " " +
                                "格林威治时间:" + timeZoneDesc,
                        Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onRestart() {
        new UpdateListTask().execute(getKeyWord());
        super.onRestart();
    }

    public void init() {
        mEtInput = (EditText) findViewById(R.id.search_box);
        mHint = (TextView) findViewById(R.id.hint);
        listview = (ListView) findViewById(android.R.id.list);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                CharSequence str = "";
                str = mEtInput.getText();
                if (str != null && str.length() > 0) {
                    String temp = str.toString();
                    mEtInput.setTextKeepState(temp);
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
        HashMap<String, String> timeZoneCityNameGmt;

        //public ArrayList<String> indexToName(ArrayList<String> beChooseCitys) {
        //    ArrayList<String> beChooseNames = new ArrayList<>();
        //    for (int i = 0; i < beChooseCitys.size(); i++) {
        //        beChooseNames.add(City.cityNames[Integer.valueOf(beChooseCitys.get(i))]);
        //    }
        //    return beChooseNames;
        //}

        public HighLightKeywordsAdapter(Context context, List<HashMap<String, String>> list,
                                        int layoutid, String[] from, int[] to) {
            this.context = context;
            this.list = list;
            this.from = from;
            this.to = to;
            this.layoutid = layoutid;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            myInflater = LayoutInflater.from(context);
            try {
                timeZoneCityNameGmt = list.get(position);
                //convertView = myInflater.inflate(layoutid, null);//此view加载方式会导致item样式不生效
                convertView = myInflater.inflate(layoutid, parent, false);
                convertView.setTag(timeZoneCityNameGmt);
            } catch (Exception e) {
                Log.e(TAG, "Hight Key Error! ");
            }

            View item = convertView.findViewById(to[0]);
            View description = convertView.findViewById(to[1]);
            if (item instanceof TextView && description instanceof TextView) {
                TextView TvItem = (TextView) item;
                String itemValue = "";
                itemValue = timeZoneCityNameGmt.get(from[0]);

                String input = mEtInput.getText().toString();

                String lowerItemValue = itemValue.toLowerCase();
                String lower_input_value = input.toLowerCase();
                if (lowerItemValue.contains(lower_input_value)) {
                    int start = lowerItemValue.indexOf(lower_input_value);
                    SpannableStringBuilder style_string = new SpannableStringBuilder(itemValue);
                    style_string.setSpan(new ForegroundColorSpan(Color.RED), start,
                            start + input.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    TvItem.setText(style_string);
                }

                TextView description_tv = (TextView) description;
                String description_value = timeZoneCityNameGmt.get(from[1]).toString();
                description_tv.setText(description_value);

                String beShowedCity = TvItem.getText().toString();
                Log.e(TAG, "显示的时区(城市):" + beShowedCity);
            }
            return convertView;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private synchronized List<HashMap<String, String>> updateNumberslist(String input) {
        List<HashMap<String, String>> list = new ArrayList<>();
        if (input == null) {
            for (int i = 0; i < data.size(); i++) {
                HashMap<String, String> zone_info = (HashMap) data.get(i);
                list.add(zone_info);
            }
        } else {
            for (int i = 0; i < data.size(); i++) {
                String zoneCityName = data.get(i).get(KEY_DISPLAYNAME).toString();
                String zoneGmt = data.get(i).get(KEY_GMT).toString();
                String zoneId = data.get(i).get(KEY_ID).toString();

                if (zoneCityName.toLowerCase().contains(input.toLowerCase())) {
                    HashMap<String, String> zoneInfo = new HashMap<>();
                    zoneInfo.put(KEY_DISPLAYNAME, zoneCityName);
                    zoneInfo.put(KEY_GMT, zoneGmt);
                    zoneInfo.put(KEY_ID, zoneId);
                    list.add(zoneInfo);
                }
            }
        }

        return list;
    }

    private String getKeyWord() {
        if (mEtInput.getText() != null && mEtInput.getText().length() > 0) {
            return mEtInput.getText().toString();
        } else {
            return null;
        }
    }

    private class UpdateListTask extends AsyncTask<String, Integer, BaseAdapter> {
        @Override
        protected BaseAdapter doInBackground(String... params) {

            BaseAdapter listAdapter = new HighLightKeywordsAdapter(HighlightSearchCityActivity.this,
                    updateNumberslist(getKeyWord()), R.layout.custom_row,
                    new String[]{KEY_DISPLAYNAME, KEY_GMT},
                    new int[]{R.id.item, R.id.description});

            return listAdapter;
        }

        @Override
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
        mData = new ArrayList<>();
        mCityIndex = 0;
        final long date = Calendar.getInstance().getTimeInMillis();
        try {
            XmlResourceParser xrp = context.getResources().getXml(R.xml.timezones);
            if (xrp != null) {
                while (xrp.next() != XmlResourceParser.START_TAG){
                    continue;
                }
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

    private static class ZoneComparator implements Comparator<HashMap<?, ?>> {
        private String mSortingKey;
        RuleBasedCollator collator;

        public ZoneComparator(String sortingKey) {
            mSortingKey = sortingKey;
            collator = (RuleBasedCollator) Collator.getInstance(Locale.getDefault());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
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
        final HashMap<String, Object> map = new HashMap<>(6);
        map.put(KEY_ID, id);
        map.put(KEY_DISPLAYNAME, displayName);
        map.put(KEY_INDEX, cityIndex);
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
