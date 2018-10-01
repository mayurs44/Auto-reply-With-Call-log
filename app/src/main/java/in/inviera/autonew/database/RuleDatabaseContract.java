package in.inviera.autonew.database;

import android.provider.BaseColumns;

public class RuleDatabaseContract {
	//To prevent someone from accidentally instantiating the contract
	//give it an empty constructor

	public RuleDatabaseContract() {}

	/*Inner class defines table contents */
	public static abstract class RuleEntry implements BaseColumns{

		public static final String RULE_TABLE_NAME = "rules";
		public static final String RULE_COLUMN_NAME = "name";
		public static final String RULE_COLUMN_DESCRIPTION = "description";
		public static final String RULE_COLUMN_TEXT = "text";
		public static final String RULE_COLUMN_ONLYCONTACTS = "onlyContacts";
		public static final String RULE_COLUMN_REPLYTO = "replyTo";
		public static final String RULE_COLUMN_STATUS = "status";
		public static final String RULE_COLUMN_WIDGET_ID = "widgetID";
		public static final String RULE_COLUMN_INCLUDE = "include";
		public static final String RULE_COLUMN_EXCLUDE = "exclude";
	}
	
	public static abstract class SMSEntry implements BaseColumns{
		public static final String SMS_TABLE_NAME = "texts";
		public static final String SMS_COLUMN_TIME = "time";
		public static final String SMS_COLUMN_TEXT = "text";
		public static final String SMS_COLUMN_TO = "recipient";
		public static final String SMS_COLUMN_RULE = "rule";
	}

}
