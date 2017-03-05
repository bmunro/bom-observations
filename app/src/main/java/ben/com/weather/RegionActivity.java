package ben.com.weather;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RegionActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
	private static final String DEBUG_TAG = "WeatherAppLog";
	private static final List<String> columns = Arrays.asList(
		"-datetime",
		"-tmp",
		"-apptmp",
		"-dewpoint",
		"-relhum",
		"-delta-t",
		"-wind-dir",
		"-wind-spd-kmh",
		"-wind-gust-kmh",
		"-wind-spd-kts",
		"-wind-gust-kts",
		"-press",
		"-rainsince9am",
		"-lowtmp",
		"-hightmp",
		"-highwind-dir",
		"-highwind-gust-kmh",
		"-highwind-gust-kts"
	);

	// TODO: make these configurable
	private static final Map<String,String> displayColumns;
	static {
		displayColumns = new LinkedHashMap<String, String>();
		displayColumns.put("-tmp", "Temp");
		displayColumns.put("-dewpoint", "DP");
		displayColumns.put("-wind-spd-kmh", "Wind");
		displayColumns.put("-lowtmp", "Low");
		displayColumns.put("-hightmp", "High");
		displayColumns.put("-rainsince9am", "Rain");
	}

	private SwipeRefreshLayout swipeLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_region);

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
		// TODO don't create a new object each time.
		String url = new Config().getRegionUrl(this);
		String htmlFormat = new Config().getRegionFormat(this);

		TableLayout tl = (TableLayout) findViewById(R.id.regiontable);
		tl.removeAllViews();

		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			new DownloadRegionObsTask(url, htmlFormat).execute();
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

	private class DownloadRegionObsTask extends AsyncTask<Void, Void, ArrayList<HashMap<String,String>>> {
		String url;
		String htmlFormat;

		public DownloadRegionObsTask(String url, String htmlFormat) {
			this.url = url;
			this.htmlFormat = htmlFormat;
		}

		@Override
		protected ArrayList<HashMap<String,String>> doInBackground(Void... params) {

			ArrayList region = new ArrayList<HashMap<String,String>>();
			Config config = new Config();
			//String

			try {
				Document document = Jsoup.connect(this.url).get();
				Elements rows = document.select("tr.rowleftcolumn");
				for (Element stationRow : rows) {
					HashMap<String,String> observations = new HashMap<String,String>();
					Log.d("Row", stationRow.select("a").html());
					observations.put("station", stationRow.select("a").html());

					//loadConfig(R.raw.regionhtmlformat);

					for (String column : columns) {
						String observation = stationRow.select("[headers*=\"" + column + "\"]").html();

						// Throw away the time of the observation
						// e.g. 21.5<br><small>09:20pm</small> becomes 21.5
						observation = observation.split("<")[0];
						observations.put(column, observation);
						Log.d("col", column + " = " + observation);
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