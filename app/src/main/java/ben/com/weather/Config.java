package ben.com.weather;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Config extends Application {

	private static Config instance = null;
	private Context context;

	public Config() {
		Log.d("Config", "Constructor");
	}

	public void onCreate() {
		Log.d("Config", "onCreate");
		super.onCreate();
		context = getApplicationContext();
	}

	public Context getAppContext() {
		Log.d("Config", "context" + context);
		return context;
	}

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
			instance.loadConfig();
		}
		return instance;
	}

	public JSONObject loadConfig() {

		JSONObject config;

		try {
			// Stupid boilerplate to read file to string because Java is shite.
			InputStream stream = getAppContext().getResources().openRawResource(R.raw.regions);
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
