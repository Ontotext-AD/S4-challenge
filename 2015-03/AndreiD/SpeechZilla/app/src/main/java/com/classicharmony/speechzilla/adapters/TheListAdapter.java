package com.classicharmony.speechzilla.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.classicharmony.speechzilla.R;
import com.classicharmony.speechzilla.models.TheNote;

import java.util.List;


public class TheListAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {

    private List<TheNote> data;
    Context context;

    public TheListAdapter(Context ctx, List<TheNote> data) {
        this.data = data;
        this.context = ctx;


    }


    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }


    @Override
    public TheNote getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.row_the_list, null);
            holder = new ViewHolder();

            holder.textView_row_created_at = (TextView) convertView.findViewById(R.id.textView_row_created_at);
            holder.textView_row_keywords = (TextView) convertView.findViewById(R.id.textView_row_keywords);
            holder.textView_row_fulltext = (TextView) convertView.findViewById(R.id.textView_row_fulltext);
            holder.textView_row_locations = (TextView) convertView.findViewById(R.id.textView_row_locations);
            holder.textView_row_organizations = (TextView) convertView.findViewById(R.id.textView_row_organizations);

            convertView.setTag(holder);
            final ViewHolder finalHolder = holder;

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TheNote mNote = this.data.get(position);

        String created_at = mNote.getCreated_at();
        String keywords = mNote.getKeywords();
        String location_list = mNote.getLocation_list();
        String organization_list = mNote.getOrganization_list();
        String full_text = mNote.getFull_text();

        if (location_list.equals("[]")) holder.textView_row_locations.setVisibility(View.GONE);
        if (organization_list.equals("[]")) holder.textView_row_organizations.setVisibility(View.GONE);
        if (keywords.equals("[]")) holder.textView_row_keywords.setVisibility(View.GONE);



        holder.textView_row_created_at.setText(created_at);
        holder.textView_row_fulltext.setText(full_text);
        holder.textView_row_keywords.setText(keywords);
        holder.textView_row_locations.setText(location_list);
        holder.textView_row_organizations.setText(organization_list);


        return convertView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


    }

    public void clear_all() {
        this.data = null;
    }


    public static class ViewHolder {


        public TextView textView_row_created_at;
        public TextView textView_row_fulltext;
        public TextView textView_row_locations;
        public TextView textView_row_organizations;
        public TextView textView_row_keywords;


    }


}
