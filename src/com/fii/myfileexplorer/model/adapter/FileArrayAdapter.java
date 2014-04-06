package com.fii.myfileexplorer.model.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fii.myfileexplorer.R;
import com.fii.myfileexplorer.model.Item;

public class FileArrayAdapter extends ArrayAdapter<Item> {

    private Context context;
    private int id;
    private List<Item> items;

    public FileArrayAdapter(Context context, int textViewResourceId, List<Item> objects) {
	super(context, textViewResourceId, objects);
	this.context = context;
	id = textViewResourceId;
	items = objects;
    }

    public Item getItem(int i) {
	return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	View view = convertView;
	if (view == null) {
	    LayoutInflater viewInflater = (LayoutInflater) context
		    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    view = viewInflater.inflate(id, null);
	}

	final Item item = items.get(position);
	if (item != null) {
	    TextView titleTV = (TextView) view.findViewById(R.id.titleTV);
	    TextView subtitleTV = (TextView) view.findViewById(R.id.countTV);
	    TextView dateTV = (TextView) view.findViewById(R.id.dateTV);
	    ImageView iconIV = (ImageView) view.findViewById(R.id.iconIV);
	    Drawable image = null;

	    String uri = "drawable/" + item.getImage();
	    int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
	    image = context.getResources().getDrawable(imageResource);
	    iconIV.setImageDrawable(image);

	    if (titleTV != null) titleTV.setText(item.getName());
	    if (subtitleTV != null) subtitleTV.setText(item.getData());
	    if (dateTV != null) dateTV.setText(item.getDate());
	}
	return view;
    }
}
