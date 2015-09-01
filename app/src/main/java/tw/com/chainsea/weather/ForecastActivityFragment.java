package tw.com.chainsea.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastActivityFragment extends Fragment {

    private static final String TAG = "ForecastActivityFragment";
    private Spinner spinner;
    private TextView city, date,description,temp;
    private ImageView weather;
    private ArrayAdapter<CharSequence> adapter;
    private String[] citys = { "Chuzhou", "Nanjing" };

    public ForecastActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forcast, container, false);
        spinner = (Spinner) v.findViewById(R.id.spinner_city);
        city = (TextView) v.findViewById(R.id.tv_city);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.citys_array,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                city.setText(parent.getItemAtPosition(position).toString());
                getWeatherMsg(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        date = (TextView) v.findViewById(R.id.tv_date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        date.setText(sdf.format(new Date()));
        temp = (TextView) v.findViewById(R.id.tv_temp);
        weather = (ImageView) v.findViewById(R.id.iv_weather);
        description = (TextView) v.findViewById(R.id.tv_description);

        return v;
    }

    private void getWeatherMsg(int position) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q="
                + citys[position] + ",cn&units=metric";
        new LoadJsonAsyncTask().execute(url);
    }

    class LoadJsonAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null; // 连接对象
            InputStream is = null;
            String resultData = "";
            try {
                URL url = new URL(params[0]); // URL对象
                conn = (HttpURLConnection) url.openConnection(); // 使用URL打开一个链接
                conn.setDoInput(true); // 允许输入流，即允许下载
                conn.setDoOutput(true); // 允许输出流，即允许上传
                conn.setUseCaches(false); // 不使用缓冲
                conn.setRequestMethod("GET"); // 使用get请求
                is = conn.getInputStream(); // 获取输入流，此时才真正建立链接
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine = "";
                while ((inputLine = bufferReader.readLine()) != null) {
                    resultData += inputLine + "\n";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return resultData;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                JSONObject jsonObject, subJson;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(result);
                    subJson = new JSONObject(jsonObject.get("main").toString());
                    temp.setText(subJson.get("temp") + "℃");
                    jsonArray = jsonObject.getJSONArray("weather");
                    subJson = new JSONObject(jsonArray.get(0).toString());
                    Weather w = new Weather();
                    w.setId(subJson.getInt("id"));
                    weather.setBackgroundResource(Utility
                            .getArtResourceForWeatherCondition(w.getId()));
                    w.setDescription(subJson.getString("description"));
                    description.setText(w.getDescription());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            //刷新页面
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
