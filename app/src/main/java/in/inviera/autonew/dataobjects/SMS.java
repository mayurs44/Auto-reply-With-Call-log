package in.inviera.autonew.dataobjects;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class SMS {

	private long time;
	private String text;
	private String to;
	private String rule;
	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

	/**
	 * 
	 * @param time Time the SMS was sent
	 * @param text Text of the SMS
	 * @param to To whom the SMS was sent
	 * @param rule Name of the Rule the SMS was sent by
	 */
	public SMS(long time, String text, String to, String rule) {
		/*this.time = time;
		this.text = text;
		this.to = to;
		this.rule = rule;*/
		this.time = time;
		this.text = text;
		this.to = to;
		this.rule = rule;
	}

	/**
	 * 
	 * @return The 'time' field of this SMS in milliseconds
	 */
	public long getTimeInMilli() {
		return time;
	}
	
	/**
	 * 
	 * @return The 'time' field of this SMS as a locally formatted String
	 */
	public String getTimeAsDate() {
		return dateFormat.format(new Date(time));
	}

	public String getText() {
		return text;
	}

	public String getTo() {
		return to;
	}

	public String getRule() {
		return rule;
	}

	public String toString() {
		
		return getTimeAsDate() + ", <b>To:</b> " + to + "<br>" +
				text + "<br>" +
				"<i>by Rule " + rule + "</i>";
	}
}