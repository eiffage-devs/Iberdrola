package com.example.jsancho.pedidos.Objetos;

import android.graphics.Bitmap;

public class Foto {

    private Bitmap foto;
    private String descripcion;
    private String categoria, subcategoria;
    private String fecha, hora;

    private String coordenadasPedido;
    private String coordenadasFoto;

    String idTarea;

    private String id;


    public Foto(Bitmap foto, String descripcion, String categoria, String subcategoria, String fecha, String hora, String coordenadasFoto, String idTarea, String id){
        this.foto = foto;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.subcategoria = subcategoria;
        this.fecha = fecha;
        this.hora = hora;
        this.coordenadasFoto = coordenadasFoto;
        this.idTarea = idTarea;
        this.id = id;

    }

    public Bitmap getFoto() {
        return foto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getSubcategoria() {
        return subcategoria;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getCoordenadasFoto() {
        return coordenadasFoto;
    }

    public String getCoordenadasPedido() {
        return coordenadasPedido;
    }

    public String getIdTarea() {
        return idTarea;
    }

    public String getId() { return id;}

    public void setFoto(Bitmap foto) {
        this.foto = foto;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setSubcategoria(String subcategoria) {
        this.subcategoria = subcategoria;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setCoordenadasFoto(String coordenadasFoto) {
        this.coordenadasFoto = coordenadasFoto;
    }

    public void setCoordenadasPedido(String coordenadasPedido) {
        this.coordenadasPedido = coordenadasPedido;
    }

    public void setId(String id){this.id = id;}

    @Override
    public String toString() {
        String s =
                this.getFoto().toString() + "," +
                        this.getDescripcion() + "," +
                        this.getCategoria() + "," +
                        this.getSubcategoria() + "," +
                        this.getFecha() + "," +
                        this.getHora() + "," +
                        this.getCoordenadasFoto() + "," +
                        this.getId();
        return s;
    }
}
