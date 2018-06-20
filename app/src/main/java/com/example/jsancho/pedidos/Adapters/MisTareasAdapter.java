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
    MySqliteOpenHelper mySqliteOpenHelper;

    public MisTareasAdapter(Context context, ArrayList<Tarea> values){

        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.custom_list_item, parent, false);

        mySqliteOpenHelper = new MySqliteOpenHelper(context);
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
        TextView hayFotos = rowView.findViewById(R.id.txtHayFotos);
        boolean b = hayFotos(values.get(position).getCod_tarea());
        numPedido.setText(codigoPedido);
        descPedido.setText(descripcionPedido);
        descTarea.setText(descripcionTarea);
        if(b){
            hayFotos.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
            hayFotos.setText("FOTOS PENDIENTES DE ENVIAR");
        }
        return rowView;
    }

    public boolean hayFotos(String idTarea){
        Cursor c = mySqliteOpenHelper.recuperarFotos(db, idTarea);
        if(c.getCount() > 0){
            return true;
        }
        return false;
    }
}
