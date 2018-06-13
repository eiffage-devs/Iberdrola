package com.example.jsancho.pedidos.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.jsancho.pedidos.Activities.MisTareas;
import com.example.jsancho.pedidos.Objetos.Pedido;
import com.example.jsancho.pedidos.R;

import java.util.ArrayList;

public class MisPedidosAdapter extends ArrayAdapter<Pedido> {

    private final Context context;
    private ArrayList<Pedido> values;

    public MisPedidosAdapter(Context context, ArrayList<Pedido> values){

        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.custom_list_item_pedido, parent, false);

        final int id = position;
        //----------Identificamos los elementos de cada item de la list----------\\
        TextView numPedido =  rowView.findViewById(R.id.misPedidosNumPedido);
        TextView descPedido = rowView.findViewById(R.id.misPedidosDescPedido);
        Button tareas = rowView.findViewById(R.id.tareasDeUnPedido);

        numPedido.setText(values.get(position).getCodigo());
        descPedido.setText(values.get(position).getDescripcion());

        tareas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MisTareas.class);
                intent.putExtra("filtro_tareas", values.get(id).getCodigo());
                context.startActivity(intent);
            }
        });


        return rowView;
    }
}

