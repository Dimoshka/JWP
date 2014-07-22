package ua.pp.dimoshka.classes;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class class_widget_service extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new class_widget_factory(getApplicationContext());
    }

}