package com.example.jsancho.pedidos.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.example.jsancho.pedidos.R;

import java.util.ArrayList;

public class DocumentosAdapter extends ArrayAdapter<String>{
    private final Context context;
    private ArrayList<String> values;

    public DocumentosAdapter(Context context, ArrayList<String> values){
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.custom_list_item_documentacion, parent, false);

        CheckBox c = rowView.findViewById(R.id.checkboxDocumento);
        c.setText(values.get(position));

        return rowView;
    }
}
