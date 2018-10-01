package in.inviera.autonew.widget;

import in.inviera.autonew.database.DatabaseManager;
import in.inviera.autonew.dataobjects.Rule;
import in.inviera.autonew.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;

public class RuleWidgetProvider extends AppWidgetProvider {

	private static String WIDGET_ONCLICK_ACTION = "AUTO_REPLY_MATE.WIGDET_ONCLICK_ACTION";
	private String logTag = "WidgetProvider";
	private DatabaseManager dbManager; 

	@Override
	public void onEnabled(Context context){
		super.onEnabled(context);
		Log.i(logTag, "onEnabled was called");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(logTag, "onUpdate called");
		for (int i=0; i<appWidgetIds.length; i++) {
			int appWidgetId = appWidgetIds[i];

			Log.i(logTag, "Widget onUpdate called for " + appWidgetId);

			//Get a dbManager
			dbManager = new DatabaseManager(context);
			Rule rule = dbManager.getRule(appWidgetId);
			
			RemoteViews rm = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
			
			if (rule != null) { //if there's a rule associated with the widgetID
				//Update the background image to match the status of the rule in the DB
				rm.setImageViewResource(R.id.widget_backgroundImage, 
						((rule.getStatus()==1) ? R.drawable.widget_button_green : R.drawable.widget_button_red));
				//Update the widget text (useful after relaunch)
				rm.setTextViewText(R.id.widget_button, rule.getName());
				Intent onClickIntent = new Intent(context, RuleWidgetProvider.class);

				onClickIntent.setAction(WIDGET_ONCLICK_ACTION);
				onClickIntent.putExtra("rule_name", rule.getName());
				onClickIntent.putExtra("widget_ID", appWidgetId);
				PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			}
			else {
				Log.w(logTag, "No rule associated with wID " + appWidgetId);
				rm.setTextViewText(R.id.widget_button, "ERROR");
			}
			appWidgetManager.updateAppWidget(appWidgetId, rm);				
			Log.i(logTag, "Updated " + appWidgetId);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(logTag, "Widget received " + intent.getAction());
		super.onReceive(context, intent);

		if (WIDGET_ONCLICK_ACTION.equals(intent.getAction())) {
			Log.i(logTag, "The broadcast matches the widget onClick action");

			//Make DB manager
			dbManager = new DatabaseManager(context);

			//Get the rule name and widget ID from the intent
			String ruleName = intent.getStringExtra("rule_name");
			int widgetID = intent.getIntExtra("widget_ID", AppWidgetManager.INVALID_APPWIDGET_ID);
			
			//Change the status of the rule in the database
			dbManager.toggleRuleStatus(ruleName);

			//documentation and feedback
			Log.i(logTag, "Rule: " + ruleName + ", wID: " + widgetID + " status toggled.");

//			//Get a dbManager
//			dbManager = new DatabaseManager(context);
//			Rule rule = dbManager.getRule(widgetID);
//
//			RemoteViews rm = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
//
//			Intent onClickIntent = new Intent(context, RuleWidgetProvider.class);
//			onClickIntent.setAction(WIDGET_ONCLICK_ACTION);
//			onClickIntent.putExtra("rule_name", rule.getName());
//			onClickIntent.putExtra("widget_ID", widgetID);
//			PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, widgetID, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//			if (rule != null) { //if there's a rule associated with the widgetID
//				//Update the background image to match the status of the rule in the DB
//				rm.setImageViewResource(R.id.widget_backgroundImage,
//						((rule.getStatus()==1) ? R.drawable.widget_button_green : R.drawable.widget_button_red));
//				//Update the widget text (useful after relaunch)
//				rm.setTextViewText(R.id.widget_button,
//						rule.getName());
//
//			}
//			else {
//				Log.w(logTag, "No rule associated with wID " + widgetID);
//				rm.setTextViewText(R.id.widget_button, "ERROR");
//			}
//			AppWidgetManager.getInstance(context).updateAppWidget(widgetID, rm);
//			Log.i(logTag, "Updated " + widgetID);

			//Call for a widget update thru the onUpdate method (faster than broadcasting)
			onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetID});
		}
	}
	
	/**
	 * Calls its super method, then also removes the deleted widget's ID from the DB
	 */
	@Override
	public void onDeleted(Context context, final int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		dbManager = new DatabaseManager(context);

		Log.i(logTag, "Deleting widget(s) " + Arrays.toString(appWidgetIds));

		// Delete widget IDs from DB
		new Runnable() {
			@Override
			public void run() {
				dbManager.resetWidgetIDs(appWidgetIds);
				Log.i(logTag, "Widgets deleted from DB.");
			}
		}.run();

	}
}