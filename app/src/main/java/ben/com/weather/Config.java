package ben.com.weather;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Config {

	// TODO - these three methods are largely duplicated - refactor
	public String getRegionUrl(Activity activity) {
		String locationTag = activity.getIntent().getExtras().getString("locationTag");
		try {
			JSONObject jo = getJSONConfig(activity, R.raw.regions);
			String url = (String) jo.getJSONObject(locationTag).get("url");
			Log.d("....", "url = " + url);
			return url;
		} catch (Exception e) {
			Log.d("....", "got an error");
			return "";
		}
	}

	public String getRegionFormat(Activity activity) {
		String locationTag = activity.getIntent().getExtras().getString("locationTag");
		try {
			JSONObject jo = getJSONConfig(activity, R.raw.regions);
			String htmlFormat = (String) jo.getJSONObject(locationTag).get("htmlFormat");
			Log.d("....", "format = " + htmlFormat);
			return htmlFormat;
		} catch (Exception e) {
			Log.d("....", "got an error");
			return "";
		}
	}

	public String getRegionColumns(Activity activity) {
		try {
			JSONObject jo = getJSONConfig(activity, R.raw.regionhtmlformat);
			JSONArray ja = jo.getJSONArray("columns");
			String htmlFormat = (String) jo.get("columns");
			Log.d("....", "format = " + htmlFormat);
			return htmlFormat;
		} catch (Exception e) {
			Log.d("....", "got an error");
			return "";
		}
	}

	public String getRegionFormat2(Activity activity) {
		try {
			JSONObject jo = getJSONConfig(activity, R.raw.regionhtmlformat);
			String htmlFormat = (String) jo.get("columns");
			Log.d("....", "format = " + htmlFormat);
			return htmlFormat;
		} catch (Exception e) {
			Log.d("....", "got an error");
			return "";
		}
	}

	private JSONObject getJSONConfig(Context context, int configId) {

		JSONObject config;

		try {
			// Stupid boilerplate to read file to string because Java is shite.
			InputStream stream = context.getApplicationContext().getResources().openRawResource(configId);
			InputStreamReader isReader = new InputStreamReader(stream, Charset.forName("utf-8"));
			BufferedReader br = new BufferedReader(isReader);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			String jsonString = sb.toString();
			Log.d("Config", jsonString);
			config = new JSONObject(jsonString);
			return config;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new JSONObject();
	}
}
