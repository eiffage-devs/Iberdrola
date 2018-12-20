package com.eiffage.companias.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eiffage.companias.R;

import java.util.ArrayList;

public class ListaDocsAdapter extends ArrayAdapter<String> {

    Context context;
    ArrayList<String> values;

    public ListaDocsAdapter(@NonNull Context context, ArrayList<String> values) {
        super(context, 0, values);
        this.values = values;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.custom_list_item_documentacion, parent, false);

        try{

            TextView txtDoc = convertView.findViewById(R.id.nomDocumento);
            txtDoc.setText(values.get(position));
        }catch (NullPointerException e){
            e.printStackTrace();
        }



        return rowView;

    }
}
