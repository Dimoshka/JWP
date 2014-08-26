package ua.pp.dimoshka.jwp;

import android.view.View;
import android.widget.ListView;

import ua.pp.dimoshka.adapter.books_brochures_adapter;
import ua.pp.dimoshka.classes.my_ListFragment;

public class books_brochures extends my_ListFragment {

    public void setadapter_list() {
        mAdapter = new books_brochures_adapter(
                getActivity(),
                new String[]{"_id"}, new int[]{R.id.title},
                main.get_database(), main.get_funct());
        sqlite_rawQuery = "select magazine._id as _id, magazine.name as name, magazine.title as title, magazine.img as img, magazine.favorite, " +
                "language.code as code_lng, " +
                "publication.code as code_pub, publication._id as cur_pub, date, " +
                "files.id_type as id_type_files, files.file as file_files " +
                "from magazine " +
                "left join language on magazine.id_lang=language._id " +
                "left join publication on magazine.id_pub=publication._id " +
                "left join (select id_magazine, GROUP_CONCAT(id_type) as id_type, GROUP_CONCAT(file) as file from files group by id_magazine) as files on magazine._id=files.id_magazine " +
                "where magazine.id_lang='" + main.get_funct().get_id_lng() + "' and magazine.id_pub='4' order by magazine.favorite desc, magazine.date desc, magazine._id asc;";
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        main.get_open_or_download().dialog_show(id);
    }
}