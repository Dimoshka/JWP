package com.dimoshka.ua.classes;

import com.dimoshka.ua.jwp.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class class_simplecursoradapter_player extends SimpleCursorAdapter {
	private int layout;
	private String[] from;
	private int[] to;

	@SuppressWarnings("deprecation")
	public class_simplecursoradapter_player(Context context, int layout,
			Cursor c, @NotNull String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.layout = layout;
		this.from = from;
		this.to = to;
	}

	@Override
	public void bindView(@NotNull View v, @NotNull Context context, @NotNull Cursor c) {

		for (int i = 0; i < from.length; i++) {
			TextView t = (TextView) v.findViewById(to[i]);
			t.setText(c.getString(c.getColumnIndex(from[i])));
			t.setTextSize(12);

			if (c.getInt(c.getColumnIndex("file")) == 1) {
				t.setTextColor(context.getResources().getColor(
						R.color.text_dark_blue));
			} else {
				t.setTextColor(context.getResources().getColor(
						R.color.text_yellowgreen));
			}
		}

	}

	@Override
	public View newView(@NotNull Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		return inflater.inflate(layout, parent, false);
	}
}
