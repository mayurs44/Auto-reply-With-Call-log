package in.inviera.autonew.arrayadapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class HTMLTextArrayAdapter<T> extends ArrayAdapter<T> {

    int mResource;
    LayoutInflater mInflater;
    public HTMLTextArrayAdapter(Context context, int resource,
                              List<T> objects) {
        super(context, resource, objects);
        mResource = resource;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Override the getView method to use HTML formatting when setting the text
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView text;
        if (convertView == null) {

            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }
        try {
            text = (TextView) view;
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }
        T item = getItem(position);
        if (item instanceof CharSequence) {
            text.setText((CharSequence)item);
        } else {
            text.setText(Html.fromHtml(item.toString()));
        }
        return view;
    }
}
