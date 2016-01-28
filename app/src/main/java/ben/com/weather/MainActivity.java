package ben.com.weather;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String DEBUG_TAG = "WeatherAppLog";
    private static final List<String> columns = Arrays.asList(
		"obs-datetime",
		"obs-temp",
		"obs-apptemp",
		"obs-dewpoint",
		"obs-relhum",
		"obs-delta-t",
		"obs-wind obs-wind-dir",
		"obs-wind obs-wind-spd-kph",
		"obs-wind obs-wind-gust-kph",
		"obs-wind obs-wind-spd-kts",
		"obs-wind obs-wind-gust-kts",
		"obs-press",
		"obs-rainsince9am",
		"obs-lowtemp",
		"obs-hightemp",
		"obs-highwind obs-highwind-dir",
		"obs-highwind obs-highwind-gust-kph",
		"obs-highwind obs-highwind-gust-kts"
	);

	private static final Map<String,String> displayColumns;
	static {
		displayColumns = new LinkedHashMap<String, String>();
		displayColumns.put("obs-temp", "Temp");
		displayColumns.put("obs-wind obs-wind-spd-kph", "Wind");
	}

	private SwipeRefreshLayout swipeLayout;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);

		main();

    }

	@Override
	public void onRefresh() {
		swipeLayout.setRefreshing(true);
		main();
	}

	public void main() {
		String stringUrl = "http://www.bom.gov.au/nsw/observations/sydney.shtml";
		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		TableLayout tl = (TableLayout) findViewById(R.id.regiontable);
		tl.removeAllViews();

		if (networkInfo != null && networkInfo.isConnected()) {
			new DownloadRegionObsTask().execute(stringUrl);
		} else {
			Context context = getApplicationContext();
			CharSequence errorText = "No network connection available.";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, errorText, duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			swipeLayout.setRefreshing(false);
		}
	}

    private void displayData(ArrayList<HashMap<String,String>> region) {
	    Resources r = getResources();
	    int locationWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, r.getDisplayMetrics());
	    int cellWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
		TableLayout tl = (TableLayout) findViewById(R.id.regiontable);

	    TableRow header = new TableRow(this);
	    TextView locationHeading = new TextView(this);
	    locationHeading.setText("Station");
	    header.addView(locationHeading);
	    for (String column : displayColumns.keySet()) {
		    TextView columnHeading = new TextView(this);
		    columnHeading.setText(displayColumns.get(column));
		    header.addView(columnHeading);
	    }
	    tl.addView(header);

	    int row = 0;

	    for (HashMap<String,String> observations : region) {
		    TableRow tr = new TableRow(this);

		    if (row % 2 == 0) {
			    tr.setBackgroundResource(R.color.teal50);
		    } else {
			    tr.setBackgroundResource(R.color.teal100);
		    }

		    Log.d(DEBUG_TAG, "observations: " + observations.toString());
		    TextView location = new TextView(this);
		    location.setText(observations.get("station"));
		    location.setWidth(locationWidth);
		    tr.addView(location);

		    Log.d(DEBUG_TAG, "station: " + observations.get("station"));
		    for (String column : displayColumns.keySet()) {
			    Log.d(DEBUG_TAG, "station: " + column + " = " + observations.get(column));
			    TextView cell = new TextView(this);
			    cell.setText(observations.get(column));
			    cell.setWidth(cellWidth);
			    tr.addView(cell);
		    }

		    tl.addView(tr);

		    row++;
	    }
    }

    private class DownloadRegionObsTask extends AsyncTask<String, Void, ArrayList<HashMap<String,String>>> {
        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(String... urls) {

            ArrayList region = new ArrayList<HashMap<String,String>>();

            // params comes from the execute() call: params[0] is the url.
            try {
                Document document = Jsoup.connect(urls[0]).get();
                //Elements tempElement = document.select("td[headers=\"obs-temp obs-station-sydney-observatory-hill\"]");
                Elements rows = document.select("tr.rowleftcolumn");
                for (Element stationRow : rows) {
                    HashMap<String,String> observations = new HashMap<String,String>();
                    
                    observations.put("station", stationRow.select("a").html());

                    for (String column : columns) {
                        String observation = stationRow.select("[headers^=\"" + column + "\"]").html();
                        observations.put(column, observation);
                    }

                    region.add(observations);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return region;
        }
	    
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<HashMap<String,String>> region) {
            Log.d(DEBUG_TAG, "DATA: " + region.toString());
            displayData(region);
	        swipeLayout.setRefreshing(false);
        }
    }
}