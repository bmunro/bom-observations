package ben.com.weather;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		setContentView(R.layout.activity_home);
	}

	public void buttonClick (View v) {
		getWindow().setExitTransition(new Explode());
		Intent intent = new Intent(HomeActivity.this, RegionActivity.class);
		String locationTag = (String) v.getTag();
		intent.putExtra("locationTag", locationTag);
		startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
	}

}
