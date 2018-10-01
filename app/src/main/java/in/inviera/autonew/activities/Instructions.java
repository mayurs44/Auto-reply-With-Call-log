package in.inviera.autonew.activities;

import in.inviera.autonew.R;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

public class Instructions extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);
		
		TextView instructionsTextView = (TextView)findViewById(R.id.instructions_instructions);
		instructionsTextView.setText(Html.fromHtml(getString(R.string.instructions)));
	}
}
