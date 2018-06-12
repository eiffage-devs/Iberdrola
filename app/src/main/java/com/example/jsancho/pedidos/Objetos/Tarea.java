package com.example.jsancho.pedidos.Clases_Auxiliares;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.jsancho.pedidos.R;

public class Tarea {

    private String pedido, desc_pedido, desc_tarea;
    public Tarea(String pedido, String desc_pedido, String desc_tarea){
        this.pedido = pedido;
        this.desc_pedido = desc_pedido;
        this.desc_tarea = desc_tarea;
    }

    public String getPedido() {
        return pedido;
    }

    public String getDesc_pedido() {
        return desc_pedido;
    }

    public String getDesc_tarea() {
        return desc_tarea;
    }
}
