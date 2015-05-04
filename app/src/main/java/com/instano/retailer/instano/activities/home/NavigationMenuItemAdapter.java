package com.instano.retailer.instano.activities.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.instano.retailer.instano.R;

/**
 * Created by Rohit on 2/5/15.
 */
public class NavigationMenuItemAdapter extends ArrayAdapter<String>{

    private final Context context;
    private final String[] values;

    public NavigationMenuItemAdapter(Context context, String[] objects) {
        super(context, R.layout.list_item_navigation_drawer_home, objects);
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_navigation_drawer_home, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.navigation_drawer_menu_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.navigation_drawer_menu_image);
        textView.setText(values[position]);
        // Change the icon for Windows and iPhone
        String s = values[position];
        if (s.startsWith("Offers")) {
            imageView.setImageResource(R.drawable.home_unselected);
        }
        else if(s.startsWith("Map")) {
            imageView.setImageResource(R.drawable.chat_unselected);
        }
        else if(s.startsWith("Stores")) {
            imageView.setImageResource(R.drawable.settings_unselected);
        }
        return rowView;
    }
}
