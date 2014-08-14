package ua.pp.dimoshka.jwp;

import ua.pp.dimoshka.adapter.news_adapter;
import ua.pp.dimoshka.classes.my_ListFragment;

public class news extends my_ListFragment {

    public void setadapter_list() {
        mAdapter = new news_adapter(
                getActivity(), new String[]{}, new int[]{}, main.get_database(), main.get_funct());
        sqlite_rawQuery = "select * from news where news.id_lang='" + main.get_funct().get_id_lng() + "' order by news._id DESC;";
    }
}