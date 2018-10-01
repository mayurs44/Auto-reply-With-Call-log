package in.inviera.autonew.activities;

import in.inviera.autonew.R;
import in.inviera.autonew.database.DatabaseManager;
import in.inviera.autonew.dataobjects.Rule;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;


public class AddEditRule extends AppCompatActivity {

	static final int PICK_INCLUDE_CONTACT_REQUEST = 0;
	static final int PICK_EXCLUDE_CONTACT_REQUEST = 1;

	static final int CONTACT_FILTER_REQUEST = 2;

	private String logTag = "AddEditRule";

	private EditText editTextName;
	private EditText editTextDescription;
	private EditText editTextText;
	private CheckBox checkBoxContacts;
	private RadioGroup radioReplyTo;
	private ProgressBar progressBar;
	private LinearLayout fields;
	private Switch switchContactsFilter;

	private DatabaseManager dbManager;

	private boolean edit;
	private String oldRuleName;

	String includeString;
	String excludeString;

	String filterString;

	private static String outgoingExtraTag = "selected_contacts";
	private static String incomingExtraTag = "selected_contacts_string";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_edit_rule);

		// initiate
		if (includeString == null)
			includeString = "";
		if (excludeString == null)
			excludeString = "";

		// Get views

		/*editTextName = (EditText) findViewById(R.id.editText_name);
		editTextDescription = (EditText) findViewById(R.id.editText_description);
		editTextText = (EditText) findViewById(R.id.editText_text);
		checkBoxContacts = (CheckBox) findViewById(R.id.checkBox_contactsOnly);
		radioReplyTo = (RadioGroup) findViewById(R.id.radio_replyTo);
		progressBar = (ProgressBar) findViewById(R.id.addedit_progress_bar);
		switchContactsFilter = (Switch) findViewById(R.id.switch_contactsFilter);
		fields = (LinearLayout) findViewById(R.id.addRule_fields);*/


		editTextName = (EditText) findViewById(R.id.editText_name);
		editTextDescription = (EditText)findViewById(R.id.editText_description);
		editTextText =(EditText) findViewById(R.id.editText_text);
		checkBoxContacts = (CheckBox) findViewById(R.id.checkBox_contactsOnly);
		radioReplyTo = (RadioGroup) findViewById(R.id.radio_replyTo);
		progressBar = (ProgressBar) findViewById(R.id.addedit_progress_bar);
		switchContactsFilter = (Switch) findViewById(R.id.switch_contactsFilter);
		fields = (LinearLayout) findViewById(R.id.addRule_fields);

		// For up navigation thru the action bar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		/*	Set onCheckedChangeListener to the ContactFilter switch
		/	when turned on, launches contact picker
		*/
		switchContactsFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					Intent intent = new Intent(getApplicationContext(), ContactPicker.class);
					if (edit) {
						intent.putExtra(filterString, outgoingExtraTag);
					}
					startActivityForResult(intent, CONTACT_FILTER_REQUEST);
				}
			}
		});


		// If this activity is launched with an editing intent, start the asynctask to populate the fields
		Intent intent = getIntent();
		if (intent.hasExtra("ruleName")) {
			Log.i(logTag, "AddEdit launched with intent that contains rulename extra: \n" + intent.toString());
			setTitle("Edit Rule");
			edit = true;
			oldRuleName = intent.getStringExtra("ruleName");
			new PopulateFieldsTask().execute(new String[] {oldRuleName});
		}
	}

	public void launchIncludeContactPicker(View view) {
		// Pass nos that are already selected to the contact picker
		Intent intent = new Intent(this, ContactPicker.class);
		intent.putExtra(outgoingExtraTag, includeString);
		startActivityForResult(intent, PICK_INCLUDE_CONTACT_REQUEST);
	}

	public void launchExcludeContactPicker(View view) {
		// Pass nos that are already selected to the contact picker
		Intent intent = new Intent(this, ContactPicker.class);
		intent.putExtra(outgoingExtraTag, excludeString);
		startActivityForResult(intent, PICK_EXCLUDE_CONTACT_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == CONTACT_FILTER_REQUEST) {
				Log.i(logTag, "Returned with contact filter request as requestCode");
				filterString = data.getStringExtra(incomingExtraTag);
			}
			else if (requestCode == PICK_INCLUDE_CONTACT_REQUEST) {
				Log.i(logTag, "Returned with include requestCode");
				includeString = data.getStringExtra(incomingExtraTag);
			}
			else if(requestCode == PICK_EXCLUDE_CONTACT_REQUEST){
				Log.i(logTag, "Returned with exlude requestcode");
				excludeString = data.getStringExtra(incomingExtraTag);
			} else
				Log.e(logTag, "requestCode doesnt match any predefined one");
		} else
			Log.i(logTag, "resultCode is not OK!");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.addedit_menu, menu);
		return true;
	}

	/**
	 * If the save button is clicked, call hte saveButtonClicked() method
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_action_save:
			saveButtonClicked();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Called when the save button on the AddEditRule Activity is clicked
	 * 
	 */
	public void saveButtonClicked() {
		Log.i(logTag, "Save button clicked with edit as " + edit);

		String newRuleName = editTextName.getText().toString().trim(); //get the new rule name
		String ruleText = editTextText.getText().toString(); //get the text

		// Validate input
		if (newRuleName.length() == 0){
			Toast.makeText(getApplicationContext(), "Name field cannot be empty", Toast.LENGTH_SHORT).show();
		}
		else if(ruleText.length() == 0){
			Toast.makeText(getApplicationContext(), "Text field cannot be empty", Toast.LENGTH_SHORT).show();
		}
		else { //Input is valid
			dbManager = new DatabaseManager(getApplicationContext()); //get a DB
			if (edit) { //Edit functionality
				try {
					// If the name of the rule is changed request the wID from the DB.
					// Then call for the widgets update if there's one
					if (! oldRuleName.equals(newRuleName)) { //changed
						int wID = dbManager.editRule(true, oldRuleName, new Rule(newRuleName,
								editTextDescription.getText().toString().trim(),
								ruleText,
								checkBoxContacts.isChecked(),
								radioReplyTo.indexOfChild(findViewById(radioReplyTo.getCheckedRadioButtonId())),
								includeString,
								excludeString));
						// If the rule has a widget, call to update it
						if (wID != AppWidgetManager.INVALID_APPWIDGET_ID) {
							callForWidgetUpdate(wID);
						}
					}
					else { //if the name hasn't changed, don't request a wID
						dbManager.editRule(false, oldRuleName, new Rule(newRuleName,
								editTextDescription.getText().toString().trim(),
								ruleText,
								checkBoxContacts.isChecked(),
								radioReplyTo.indexOfChild(findViewById(radioReplyTo.getCheckedRadioButtonId())),
								includeString,
								excludeString));
						Toast.makeText(getApplicationContext(), "Rule edited.", Toast.LENGTH_SHORT).show();
					}
					Log.i(logTag, "Rule edited");
					//return to homepage
					super.onBackPressed();
				}
				catch(SQLiteConstraintException ex){ //catch constraint exceptions, and give error feedback to user
					Toast.makeText(getApplicationContext(), "Rule NOT saved: name must be unique!", Toast.LENGTH_SHORT).show();
					Log.i(logTag, "Rule not added, cought " + ex);
				}
			}
			else { //Add functionality
				//add Rule to DB
				try {
					if (dbManager.addRule(new Rule(editTextName.getText().toString().trim(),
							editTextDescription.getText().toString().trim(),
							ruleText,
							checkBoxContacts.isChecked(),
							radioReplyTo.indexOfChild(findViewById(radioReplyTo.getCheckedRadioButtonId())),
							includeString,
							excludeString))
							!= -1) // -1 means error
					{
						Log.i(logTag, "Rule added successfully");
						Toast.makeText(getApplicationContext(), "Rule succesfully added.", Toast.LENGTH_SHORT).show();
						super.onBackPressed(); //return to homepage
					} else {
						Log.i(logTag, "Problem with adding the rule.");
						Toast.makeText(getApplicationContext(), "A problem has occured. Try restarting the app.", Toast.LENGTH_SHORT).show();
					}
					
				}
				catch(SQLiteConstraintException ex){ //catch constraint exceptions, and give error feedback to user
					Toast.makeText(getApplicationContext(), "Rule NOT added, name must be unique!", Toast.LENGTH_SHORT).show();
					Log.i(logTag, "Rule not added, cought " + ex);
				}
			}
		}
	}

	private void callForWidgetUpdate(int widgetID) {
		Intent updateWidgetIntent = new Intent();
		updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetID} ).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		this.sendBroadcast(updateWidgetIntent);
		Log.i(logTag, "Broadcasted " + updateWidgetIntent.toString());
		Toast.makeText(getApplicationContext(), "Rule edited, its widget will update automatically.", Toast.LENGTH_SHORT).show();
	}

	/**
	 * AsyncTask to populate the fields if the activity was launched by an edit intent
	 * 
	 *
	 */
	private class PopulateFieldsTask extends AsyncTask <String,Void,Rule>{
		@Override
		protected void onPreExecute(){
			fields.setVisibility(View.INVISIBLE);
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Rule doInBackground(String... ruleName) {
			// Return the rule matching the rule name from the Database

			dbManager = new DatabaseManager(getApplicationContext());
			return dbManager.getRule(ruleName[0]);
		}

		@Override
		protected void onPostExecute(Rule rule) {
			// Populate the views

			/*editTextName.setText(rule.getName());
			editTextDescription.setText(rule.getDescription());
			editTextText.setText(rule.getText());
			checkBoxContacts.setChecked(rule.getOnlyContacts() == 1);
			((RadioButton) radioReplyTo.getChildAt(rule.getReplyTo())).setChecked(true);*/
			editTextName.setText(rule.getName());
			editTextDescription.setText(rule.getDescription());
			editTextText.setText(rule.getText());
			checkBoxContacts.setChecked(rule.getOnlyContacts() == 1);
			((RadioButton) radioReplyTo.getChildAt(rule.getReplyTo())).setChecked(true);

			// get the inc/exc fields
			includeString = rule.getInclude();
			excludeString = rule.getExclude();


			// Progress bar disappears
			progressBar.setVisibility(View.GONE);

			// The fields are shown
			fields.setVisibility(View.VISIBLE);
		}
	}
}