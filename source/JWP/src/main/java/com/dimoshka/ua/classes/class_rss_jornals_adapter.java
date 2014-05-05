package com.dimoshka.ua.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dimoshka.ua.jwp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class class_rss_jornals_adapter extends BaseExpandableListAdapter {
    private Context context;

    public class_functions funct = new class_functions();
    private SQLiteDatabase database;
    private LayoutInflater inflater;

    ArrayList<Map<String, String>> groupData;
    ArrayList<ArrayList<Map<String, String>>> childData;

    public class_rss_jornals_adapter(Context context,
                                     ArrayList<Map<String, String>> groupData,
                                     ArrayList<ArrayList<Map<String, String>>> childData,
                                     SQLiteDatabase database) {
        this.childData = childData;
        this.groupData = groupData;
        this.context = context;
        this.database = database;
    }

    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        try {

            inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.list_items_jornals, null);

            Map<String, String> m = getChild(groupPosition, childPosition);
            String name = m.get("name");
            String code_lng = m.get("code_lng");
            String code_pub = m.get("code_pub");
            Date date = funct
                    .get_jwp_jornals_rss_date(name, code_pub, code_lng);
            Integer img = Integer.parseInt(m.get("img"));
            Integer _id = Integer.parseInt(m.get("_id"));
            SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");
            String files = m.get("id_type");
            String[] id_types = files.split(",");

            TextView title = (TextView) v.findViewById(R.id.title);
            title.setText(format.format(date));

            TextView text = (TextView) v.findViewById(R.id.text);
            text.setText(name);

            ImageView myImage = (ImageView) v.findViewById(R.id.img);
            if (img == 1) {
                File imgFile = new File(funct.get_dir_app(context) + "/img/"
                        + name + ".jpg");
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
                            .getAbsolutePath());
                    myImage.setImageBitmap(myBitmap);
                } else {
                    myImage.setImageResource(R.drawable.ic_noimages);
                    ContentValues initialValues = new ContentValues();
                    initialValues.put("img", "0");
                    database.update("magazine", initialValues, "_id=?",
                            new String[]{_id.toString()});
                }
            } else {
                myImage.setImageResource(R.drawable.ic_noimages);
            }

            ImageView epub = (ImageView) v.findViewById(R.id.epub);
            ImageView pdf = (ImageView) v.findViewById(R.id.pdf);
            ImageView mp3 = (ImageView) v.findViewById(R.id.mp3);
            ImageView aac = (ImageView) v.findViewById(R.id.aac);
            epub.setImageResource(R.drawable.ic_none_type);
            pdf.setImageResource(R.drawable.ic_none_type);
            mp3.setImageResource(R.drawable.ic_none_type);
            aac.setImageResource(R.drawable.ic_none_type);

            for (int i = 0; i < id_types.length; i++) {

                String[] f = id_types[i].split("=");
                int file_isn = Integer.parseInt(f[1]);

                switch (Integer.parseInt(f[0])) {
                    case 1:
                        if (file_isn == 1)
                            epub.setImageResource(R.drawable.ic_epub_1);
                        else
                            epub.setImageResource(R.drawable.ic_epub_0);
                        break;
                    case 2:
                        if (file_isn == 1)
                            pdf.setImageResource(R.drawable.ic_pdf_1);
                        else
                            pdf.setImageResource(R.drawable.ic_pdf_0);
                        break;
                    case 3:
                        if (file_isn == 1)
                            mp3.setImageResource(R.drawable.ic_mp3_1);
                        else
                            mp3.setImageResource(R.drawable.ic_mp3_0);
                        break;
                    case 4:
                        if (file_isn == 1)
                            aac.setImageResource(R.drawable.ic_aac_0);
                        else
                            aac.setImageResource(R.drawable.ic_aac_0);
                        break;
                    default:
                        break;
                }
            }
            return v;
        } catch (Exception e) {
            funct.send_bug_report(context, e, getClass().getName(), 132);
            return null;
        }
    }

    @Override
    public Map<String, String> getChild(int groupPosition, int childPosition) {
        return childData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childData.get(groupPosition).size();
    }

    @Override
    public Map<String, String> getGroup(int groupPosition) {
        return groupData.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groupData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Map<String, String> m = getGroup(groupPosition);
        LayoutInflater infalInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.list_items_section, null);

        TextView grouptxt = (TextView) convertView.findViewById(R.id.text1);
        grouptxt.setText(m.get("groupName"));
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
