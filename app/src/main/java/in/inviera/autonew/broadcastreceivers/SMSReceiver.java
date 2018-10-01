package in.inviera.autonew.broadcastreceivers;

import in.inviera.autonew.database.DatabaseManager;
import in.inviera.autonew.dataobjects.Rule;
import in.inviera.autonew.dataobjects.SMS;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class SMSReceiver extends BroadcastReceiver{

	private static long delay = 2000; // 2 secs delay before responding
	private String logTag = "SMSReceiver";
	private SmsManager smsManager;
	private Context context;
	private DatabaseManager dbManager;

	@Override
	public void onReceive(final Context c, Intent intent) {
		context = c;

		String phoneNo = "";

		Bundle bundle = intent.getExtras();
		SmsMessage[] msg;

		if (bundle != null) {
			Log.i(logTag, "Non-null intent received");
			
			dbManager = new DatabaseManager(c);
			
			Object[] pdus = (Object[]) bundle.get("pdus");
			msg = new SmsMessage[pdus.length];
			for (int i=0; i<msg.length; i++) {
				msg[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

				//get the phoneNo of the sender
				phoneNo = msg[i].getOriginatingAddress();

			}

			//REPLY			
			phoneNo = phoneNo.replaceAll("[()\\-\\s]", "");//re-create phone no string, to make it final

//			ArrayList<Rule> rules = dbManager.getEnabledSMSRules();
//			if (! inContacts(pn)) { 							// SMS received from a non-contact
//				for (int i = 0; i < rules.size(); i ++) {
//					if(rules.get(i).getOnlyContacts() == 1) { 	// remove contacts only rules
//						rules.remove(i);
//					}
//				}
//			}
/**
			ArrayList<Rule> allRules;
			if(! inContacts(phoneNo)){ //sender not in contacts
				// Get only non-OC rules
				allRules = dbManager.getEnabledSMSRules(new String[]{"0"});
			} else { // sender in contacts
				// Get all rules from DB
				allRules = dbManager.getEnabledSMSRules(new String[] {"0", "1"});

				HashMap<String, Integer> exHash = new HashMap<>();
				HashMap<String, Integer> inHash = new HashMap<>();
				ArrayList<Rule> incRules = new ArrayList<>();
				// Iterate over all rules
				for (int i=0; i<allRules.size(); i++) {
					exHash.clear();
					arrayToHash(exHash, allRules.get(i).getExclude().split(","));

					if (! exHash.containsKey(phoneNo)) { // rule doesn;t exclude senders no
						arrayToHash(inHash, allRules.get(i).getInclude().split(","));
						if(inHash.containsKey(phoneNo)) { // rule includes senders no
							incRules.add(allRules.get(i)); // add it to include array
						}
						if()
					}
				}
 **/
				// remove rules excluding sender's no
				// include > OC > all
				// priorities replyTo = SMS over replyTo = both
			}


//			new Handler().postDelayed(new Runnable() { //Handler/Runnable usage in order to delay the reply
//				public void run() {
//					smsManager = SmsManager.getDefault();
//					for (Rule r : dbManager.getEnabledSMSRules()) { //Reply for each rule
//						if (r.getOnlyContacts() == 1) { // Reply only if the sender no is in the contacts
//							if (inContacts(pn)) { // Check if the sender is in the contacts
//								sendSMS(r, pn);
//							}
//						}
//						else {
//							sendSMS(r, pn);
//						}
//					}
//				}
//			}, delay );
//		}
	}

	private void arrayToHash(HashMap hm, String[] array) {
		for (int i = 0; i < array.length; i++) {
			hm.put(array[i], 0);
		}
	}

	/**
	 * Sends out an SMS to phoneNo using Rule r, also logs this action to SMS table for outbox usage.
	 * @param r Rule calling for sending an SMS
	 * @param phoneNo phone number to send SMS to
	 */
	private void sendSMS(Rule r, String phoneNo) {
		// Reply
		String replyText = r.getText();
		smsManager.sendMultipartTextMessage(phoneNo, null, smsManager.divideMessage(replyText), null, null);
		
		// Add the reply to the Outbox DB
		dbManager.addSMS(new SMS(System.currentTimeMillis(), replyText, String.valueOf(phoneNo), r.getName()));
		
		//documentation & feedback
		Toast.makeText(context, "Replied to " + phoneNo + ": " + replyText.substring(0,80) + "...", Toast.LENGTH_SHORT).show();
		Log.i(logTag, "Sent out an SMS to " + String.valueOf(phoneNo));
	}
	
	/**
	 * Checks if the given no is in the contacts
	 * 
	 * @param no The phone no to check for
	 * @return True if the passed no is saved in the contacts, false otherwise 
	 */
	private boolean inContacts(String no) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(no));
		//	    String name = "?";

		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri,
				new String[] {BaseColumns._ID }, //ContactsContract.PhoneLookup.DISPLAY_NAME }
				null, null, null);

		if (contactLookup != null)
		{
			try {
				if (contactLookup.getCount() > 0) {
					Log.i(logTag, contactLookup.getCount() + " contact(s) found with the senders no");
					return true;
					//name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
					//String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
				}
			} finally {
					contactLookup.close();
			}
		}
		return false;
	}
}