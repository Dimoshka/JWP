package com.dimoshka.ua.jwp;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class main extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TabHost tabHost = getTabHost();
		TabHost.TabSpec tabSpec;

		tabSpec = tabHost.newTabSpec("tag2");
		tabSpec.setIndicator(getString(R.string.news));
		tabSpec.setContent(new Intent(this, news.class));
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tag1");
		tabSpec.setIndicator(getString(R.string.jornals));
		tabSpec.setContent(new Intent(this, jornals.class));
		tabHost.addTab(tabSpec);

	}

}