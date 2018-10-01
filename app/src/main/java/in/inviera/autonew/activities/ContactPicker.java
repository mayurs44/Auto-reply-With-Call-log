package in.inviera.autonew.activities;

import java.util.ArrayList;

import android.net.Uri;
import android.service.carrier.CarrierService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.RadioGroup;
import in.inviera.autonew.R;


public class ContactPicker extends AppCompatActivity {

    static ContentResolver cr;
    ArrayList<String> phoneNos; //array holding phone nos, from which the selected ones go into selectedContacts
    String[] listContacts; //array holding Strings that will be displayed in the listview
    Button selectButton;
    Activity thisActivity;
    String logTag = "ContactPicker";


    SparseBooleanArray checked;
    ListView listView;
    RadioGroup radioFilterType;

    private static String incomingExtraTag = "selected_contacts";
    private static String outgoingExtraTag = "selected_contacts_string";
    private static String outgoingFilterTypeTag = "contact_filter_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_picker);
        thisActivity = this;

        // Set result the canceled incase user bails
        setResult(RESULT_CANCELED);

        listView = (ListView)findViewById(R.id.contactpicker_contactsList);
        radioFilterType = (RadioGroup) findViewById(R.id.radioGroup_filterType);

        // TODO progress bar

        long startTime = System.nanoTime();
        // store contacts + nos in listContacts
        getContactList();

        // populate the listview with listContacts
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, listContacts);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        if (getIntent().hasExtra(incomingExtraTag)) { // if opened from an edit
            String[] savedNos = getIntent().getStringExtra(incomingExtraTag).split(","); // get already existing nos
            for (int i = 0; i < savedNos.length; i++) { //  look thru already existing nos
                int n = phoneNos.indexOf(savedNos[i]);
                if (n != -1) { //                           if they exist in the current contacts list
                    listView.setItemChecked(n, true);//     check their corresponding checkbox
                }
            }

            long endTime = System.nanoTime();
            Log.i(logTag, "population took " + (endTime - startTime) + " secs"); // TODO delete
        }
    }

    /**
     * Stores a list of contactName + no into listContacts
     */
    private void getContactList(){
        Uri myContacts = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(myContacts,
                projection, // only number and name columns
                null, null, // all rows
                ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC"  // sort by ascending name
        );
        phoneNos = new ArrayList<String>(cursor.getCount());
        listContacts = new String[cursor.getCount()];

        int i =0;
        if(cursor.moveToFirst())
        {
            do
            {
                String phoneNo = cursor.getString(cursor
                        .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)); // get phone no in raw format
                phoneNos.add(i, phoneNo.replaceAll("[()\\-\\s]", "")); // Trim from extra chars then add into phone no arraylist
                listContacts[i] = cursor.getString(cursor
                        .getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)) + "\n" +
                        phoneNo; // name \n raw phone no
                i++;
            }
            while(cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Called when the done actionbar button is selected.
     * Finishes the activity with the selected phoneNos as the extra of the result intent
     */
    private void doneSelected(){

        // 0 = include / 1 = exclude
        int filterType = radioFilterType.indexOfChild(findViewById(radioFilterType.getCheckedRadioButtonId()));

        checked = listView.getCheckedItemPositions();
        ArrayList<String> selectedContacts = new ArrayList<String>();
        String selectedContactsString = "";
        for (int i = 0; i < listView.getCount(); i++)
            if (checked.get(i)) {
                selectedContactsString += phoneNos.get(i) + ",";
            }
        // Put the array as an extra and finish activity
        /*Intent contactIntent = new Intent();
        contactIntent.putExtra(outgoingFilterTypeTag, filterType);
        contactIntent.putExtra(outgoingExtraTag, selectedContactsString);
        setResult(RESULT_OK, contactIntent);
        thisActivity.finish();*/

        Intent contactIntent = new Intent();
        contactIntent.putExtra(outgoingFilterTypeTag, filterType);
        contactIntent.putExtra(outgoingExtraTag, selectedContactsString);
        setResult(RESULT_OK, contactIntent);
        thisActivity.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contactpicker_action_done:
                doneSelected();
                return true;
            case R.id.contactpicker_action_selectAll:
                setAll(true);
                return true;
            case R.id.contactpicker_action_deselectAll:
                setAll(false);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Sets all the checkboxes in the listView to the given value
     * @param value Value to set
     */
    private void setAll(boolean value) {
        for (int i = 0; i < listView.getCount(); i++) {
            listView.setItemChecked(i, value);
        }
    }
}