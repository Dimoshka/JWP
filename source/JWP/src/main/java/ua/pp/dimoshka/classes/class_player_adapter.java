package ua.pp.dimoshka.classes;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.pp.dimoshka.jwp.R;

public class class_player_adapter extends SimpleCursorAdapter {
	private int layout;
	private String[] from;
	private int[] to;

	@SuppressWarnings("deprecation")
	public class_player_adapter(Context context,
                                String[] from, int[] to) {
		super(context, android.R.layout.simple_list_item_single_choice, null, from, to);
		this.layout = android.R.layout.simple_list_item_single_choice;
		this.from = from;
		this.to = to;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {

		for (int i = 0; i < from.length; i++) {
			TextView t = (TextView) v.findViewById(to[i]);
			t.setText(c.getString(c.getColumnIndex(from[i])));
			t.setTextSize(12);

			if (c.getInt(c.getColumnIndex("file")) == 1) {
				t.setTextColor(context.getResources().getColor(
						R.color.text_dark_blue));
			} else {
				t.setTextColor(context.getResources().getColor(
						R.color.text_green));
			}
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		return inflater.inflate(layout, parent, false);
	}
}
