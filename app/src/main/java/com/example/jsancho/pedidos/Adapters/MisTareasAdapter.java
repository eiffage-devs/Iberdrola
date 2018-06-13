package com.example.jsancho.pedidos.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.jsancho.pedidos.DB.MySqliteOpenHelper;
import com.example.jsancho.pedidos.Objetos.Pedido;
import com.example.jsancho.pedidos.Objetos.Tarea;
import com.example.jsancho.pedidos.R;

import java.util.ArrayList;

public class MisTareasAdapter extends ArrayAdapter<Tarea>{

    private final Context context;
    private ArrayList<Tarea> values;
    SQLiteDatabase db;

    public MisTareasAdapter(Context context, ArrayList<Tarea> values){

        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.custom_list_item, parent, false);

        MySqliteOpenHelper mySqliteOpenHelper = new MySqliteOpenHelper(context);
        db = mySqliteOpenHelper.getReadableDatabase();

        Tarea t = values.get(position);
        String descripcionTarea = t.getDescripcion();
        String descripcionPedido = "Estamos en ello";
        String codigoPedido = t.getCod_pedido();

        Cursor c = db.rawQuery("SELECT descripcion FROM Pedido WHERE codigo LIKE '" + codigoPedido + "'", null);
        if(c.getCount() > 0) {
            c.moveToFirst();
            do {
                descripcionPedido = c.getString(0);
            } while (c.moveToNext());
        }


        //----------Identificamos los elementos de cada item de la list----------\\
        TextView numPedido =  rowView.findViewById(R.id.misTareasnumPedido);
        TextView descPedido = rowView.findViewById(R.id.misTareasDescPedido);
        TextView descTarea = rowView.findViewById(R.id.misTareasDescTarea);

        numPedido.setText(codigoPedido);
        descPedido.setText(descripcionPedido);
        descTarea.setText(descripcionTarea);

        return rowView;
    }
}
