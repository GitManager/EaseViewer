package team.androidreader.theme;

import team.top.activity.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ChangeThemeActivity extends Activity {
	Button black_btn, white_btn;
	LinearLayout title_theme;
	RelativeLayout layout_theme;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changetheme);
		black_btn = (Button) findViewById(R.id.black_btn);
		white_btn = (Button) findViewById(R.id.white_btn);
		title_theme = (LinearLayout) findViewById(R.id.title_theme);
		layout_theme = (RelativeLayout) findViewById(R.id.layout_theme);
		SetBackgroundImage.setBackGround(ChangeThemeActivity.this, title_theme,
				layout_theme);
		initListener();
	}

	private void initListener() {

		black_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SetBackgroundImage.saveBackground(ChangeThemeActivity.this,
						"black");
				SetBackgroundImage.setBackGround(ChangeThemeActivity.this,
						title_theme, layout_theme);

			}
		});

		white_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SetBackgroundImage.saveBackground(ChangeThemeActivity.this,
						"white");
				SetBackgroundImage.setBackGround(ChangeThemeActivity.this,
						title_theme, layout_theme);

			}
		});
	}

}
