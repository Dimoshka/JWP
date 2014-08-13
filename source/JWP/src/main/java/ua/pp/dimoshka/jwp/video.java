package ua.pp.dimoshka.jwp;

import ua.pp.dimoshka.adapter.video_adapter;
import ua.pp.dimoshka.classes.my_ListFragment;

public class video extends my_ListFragment {

    public void setadapter_list() {
        mAdapter = new video_adapter(
                getActivity(),
                new String[]{"_id"}, new int[]{R.id.title},
                main.get_database(), main.get_funct());
        sqlite_rawQuery = "select magazine._id as _id, magazine.name as name, magazine.title as title, magazine.img as img, " +
                "language.code as code_lng, " +
                "publication.code as code_pub, publication._id as cur_pub, date, " +
                "files.id_type as id_type_files, files.file as file_files " +
                "from magazine " +
                "left join language on magazine.id_lang=language._id " +
                "left join publication on magazine.id_pub=publication._id " +
                "left join (select id_magazine, GROUP_CONCAT(id_type) as id_type, GROUP_CONCAT(file) as file from files group by id_magazine) as files on magazine._id=files.id_magazine " +
                "where magazine.id_lang='" + main.get_funct().get_id_lng() + "' and magazine.id_pub='5' order by magazine.date desc, magazine._id asc;";
    }
}