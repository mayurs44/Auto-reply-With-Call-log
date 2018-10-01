package in.inviera.autonew.activities;

import java.util.ArrayList;

import in.inviera.autonew.R;
import in.inviera.autonew.arrayadapters.RuleListViewAdapter;
import in.inviera.autonew.database.DatabaseManager;
import in.inviera.autonew.dataobjects.Rule;
import in.inviera.autonew.widget.RuleWidgetProvider;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


public class Main extends AppCompatActivity {

	private ListView ruleListView;
	private ProgressBar progressBar;
	private DatabaseManager dbManager;
	private ArrayList<Rule> ruleArray;
	private String logTag = "Main";
	private RuleListViewAdapter mListAdapter;
	private boolean runResume;
	private boolean listLoaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(logTag, "onCreate called");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		//Instantiate view(s)
		ruleListView = (ListView) findViewById(R.id.main_list);
		progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);

		runResume = false;

		// Call for ListView population in a background thread

		Log.i(logTag,"Oncreate is going to populate the list");
		new PopulateListTask().execute(true);
	}

	@Override
	public void onResume(){
		Log.i(logTag, "onResume called");
		super.onResume();
		if(runResume) {
			Log.i(logTag, "onResume is going to populate the list");
			new PopulateListTask().execute(false);
		}
		else
			runResume = true;
	}

	private class PopulateListTask extends AsyncTask <Boolean,Void,ArrayList<Rule>>{
		@Override
		protected void onPreExecute(){
			listLoaded = false; // The list is not loaded
			Log.i(logTag, "PopulateListTask is started.");
			ruleListView.setVisibility(View.INVISIBLE);
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected ArrayList<Rule> doInBackground(Boolean... getDB) {
			Log.i(logTag, "PopulateListTask background task started with runResume = " + runResume);
			// Should I get a DBmanager?
			if (getDB[0]) { 
				Log.i(logTag, "PopulateListTask is gettign a new DBManager.");
				dbManager = new DatabaseManager(getApplicationContext());
			}
			//Get data from DB
			ruleArray = dbManager.getRulesArray();
			Log.i(logTag, "Retreived rule array from the DB.");
			return ruleArray;
		}

		@Override
		protected void onPostExecute(ArrayList<Rule> ruleArray) {
			Log.i(logTag, "Background part of populateListTask has ended");
			// Populate the ListView before completing the task
			populateListView(ruleArray);
			// Hide the progress bar
			progressBar.setVisibility(View.GONE);
			// Show the list
			ruleListView.setVisibility(View.VISIBLE);
			listLoaded = true; //The list is now loaded
			Log.i(logTag, "PopulateListTask complete.");
		}
	}

	/**
	 * Populates the listView with the data of the given ruleArray by
	 * settings the views adapter as a RuleListViewAdapter
	 * 
	 * @param ruleArray An ArrayList of rules to be used as data
	 */
	private void populateListView(ArrayList<Rule> ruleArray){
		Log.i(logTag, "populateListView called.");

		if(ruleArray.isEmpty()) //if the loaded rule array is empty
			Toast.makeText(this, "You have no saved rules to view!", Toast.LENGTH_SHORT).show();

		//pass the adapter with the array to the list view
		mListAdapter = new RuleListViewAdapter(this, ruleArray, getResources());
		ruleListView.setAdapter(mListAdapter);
		Log.i(logTag, "Adapter to rulelistview set");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			launchAddEditRuleActivity(null); //launch addEdit rule in add mode
			return true;
		case R.id.action_outbox:
			startActivity(new Intent(this, Outbox.class));
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.action_about:
			// Show about dialog
			new AlertDialog.Builder(this)
			.setTitle("About")
			.setPositiveButton(R.string.main_about_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setMessage(Html.fromHtml(getString(R.string.main_about_dialog_message)))
			.show();
			return true;
		case R.id.action_instructions:
			startActivity(new Intent(this, Instructions.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * onClick method for Add button from the action bar, launches the AddEditRule activity.
	 * Will only launch the activity if the list is populated.
	 */
	private void launchAddEditRuleActivity(String ruleName) {
		if (ruleName == null) {
			if (listLoaded)
				startActivity(new Intent (this, AddEditRule.class));
			else
				Toast.makeText(this, "Please wait for the list to load before adding another rule.", Toast.LENGTH_SHORT).show();			
		}
		else{
			Intent editIntent = new Intent(this, AddEditRule.class);
			editIntent.putExtra("ruleName", ruleName);
			startActivity(editIntent);	
		}
	}

	/**
	 * onToggle method for the toggle button in each row of the listView, 
	 * called thru the RuleListViewAdapter.
	 * 
	 * Creates a new thread to run in the background that queries the DB to
	 * change the rule's status and then broadcasts a widget update if needed.
	 * 
	 * @param name the name of the rule whose toggle item is clicked
	 * @param position the position of the toggle's item on the list, 0 indexed
	 * @param status True if toggle is on, false otherwise
	 */
	public void onItemToggleClicked(final String name, int position, final boolean status) {

		Log.i(logTag, "onItemToggleClicked for " + name + " at " + position + ", settign status to " + status);

		// Get the old rule to make it easier to construct the new one
		Rule cRule = ruleArray.get(position);

		// Change the rule in the rule list
		ruleArray.set(position, new Rule(name, cRule.getDescription(), cRule.getText(),
				cRule.getOnlyContacts(), cRule.getReplyTo(), ((status) ? 1 : 0)));

		// ruleArray.get(position).setStatus(status); // Alternate method

		mListAdapter.notifyDataSetChanged();

		// Change the rule in the DB
		new Runnable() {
			@Override
			public void run() {
				int wID = dbManager.setRuleStatus(name, status);
				Log.i(logTag, name + " set to " + status);
				if (wID != AppWidgetManager.INVALID_APPWIDGET_ID) {
					//Send a broadcast for the widget to update itself
					Intent updateWidgetIntent = new Intent(getApplicationContext(), RuleWidgetProvider.class);
					updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{wID} ).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
					getApplicationContext().sendBroadcast(updateWidgetIntent);
					Log.i(logTag, "Broadcasted to update the widget of " + name + " with wID " + wID);
				}
				else
					Log.i(logTag, "Did not broadcast widget update b/c " + name + " has no widget");
			}
		}.run();
	}

	/**
	 * onLongClick of each row of the listView, called thru the RuleListViewAdapter.
	 * 
	 * Launches a dialog with the rule name, text, and delete and edit options.
	 * @param position position in the array
	 * 
	 * @param ruleName The name of the rule long clicked on
	 * @param text The text of the rule long clicked on
	 */
	public void onLongItemClick(final String ruleName, final int position, String text) {
		Log.i(logTag, "Long click detected at " + ruleName);

		new AlertDialog.Builder(this)
		.setTitle(ruleName)
		.setPositiveButton(R.string.main_dialog_edit, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				launchAddEditRuleActivity(ruleName);
			}
		})
		.setNegativeButton(R.string.main_dialog_delete, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				deleteRule(ruleName, position);
			}
		})
		.setMessage(text)
		.show();
	}

	/**
	 * Queries to delete the rule with the given name.
	 * If there is a widget associated with the rule, prompts the user for its removal and
	 * broadcasts the widget to update itself.
	 * 
	 * Then refreshes the view thru notifying the ListView adapter
	 * 
	 * @param ruleName Name of the rule to be deleted
	 * @param position position in the array
	 */
	public void deleteRule(final String ruleName, int position){

		Log.i(logTag, "Delete rule requested for Rule: " + ruleName);

		new Runnable() {
			@Override
			public void run() {
				int wID = dbManager.deleteRule(ruleName);
				if (wID != AppWidgetManager.INVALID_APPWIDGET_ID) { //if there is a widget associated with the rule
					// Prompt the user to remove it manually
					Toast.makeText(getApplicationContext(), "Remember to remove the widget associated with the deleted rule: " + ruleName, Toast.LENGTH_SHORT).show();

					// Broadcast widget Update so the text sets to ERROR
					Intent updateWidgetIntent = new Intent();
					updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{wID} ).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
					getApplicationContext().sendBroadcast(updateWidgetIntent);
					Log.i(logTag, "Broadcasted " + updateWidgetIntent.toString());	
				}
			}
		}.run();

		// Remove the rule from the rule array and notify the list adapter that the data has changed so the view refreshes
		ruleArray.remove(position);
		mListAdapter.notifyDataSetChanged();
	}
}