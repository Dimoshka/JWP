package ua.pp.dimoshka.classes;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class class_widget_service extends RemoteViewsService {

    public class_widget_service() {
    }

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new class_widget_factory(getApplicationContext());
    }

}