package com.example.jsancho.pedidos.Adapters;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jsancho.pedidos.Activities.DetalleTarea;
import com.example.jsancho.pedidos.Objetos.Foto;
import com.example.jsancho.pedidos.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ListaFotosAdapter extends ArrayAdapter<Foto> {

    //Del constructor...
    private final Context context;
    private ArrayList<Foto> values;

    //Views de cada row...
    private ImageView miFoto;
    private Spinner spinnerCategorias, spinnerSubcategorias;
    private TextView desc;
    private Button borrar, descripcion;

    //GPS...
    private LocationManager locationManager;

    //Ha cambiado...
    private boolean haCambiado;

    //Objeto Foto
    private String imagen = "-";
    private String descFoto = "-";
    private String categoria="-";
    private String subcategoria = "-";
    private String fecha = "-";
    private String hora = "-";
    private String lattitude = "-";
    private String longitude = "-";
    private String idTarea = "-";
    private Bitmap fotoFormatoBitmap;

    Foto temp;


    public ListaFotosAdapter(Context context, ArrayList<Foto> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.haCambiado = false;
    }
    public View getView(final int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.custom_list_item_photo, parent, false);

        //----------Identificamos los elementos de cada item de la list----------\\

        ImageView miFoto = rowView.findViewById(R.id.imagenItem);
        miFoto.setImageBitmap(values.get(position).getFoto());
        borrar = rowView.findViewById(R.id.btnBorrar);
        descripcion = rowView.findViewById(R.id.btnDescripcion);

        final Spinner categorias = rowView.findViewById(R.id.spinner);
        final Spinner subcategorias = rowView.findViewById(R.id.spinner2);
        final TextView desc = rowView.findViewById(R.id.txtdesc);

        fotoFormatoBitmap = values.get(position).getFoto();
        descFoto = values.get(position).getDescripcion();
        categoria = values.get(position).getCategoria();
        subcategoria = values.get(position).getSubcategoria();
        desc.setText(descFoto);



        //----------Inflamos Spinner categorías----------\\

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.categorias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorias.setAdapter(adapter);

        if(!categoria.equals("-")){
            ArrayList cats = new ArrayList(Arrays.asList(context.getResources().getStringArray(R.array.categorias)));
            categorias.setSelection(cats.indexOf(categoria));
        }
        //----------Inflamos Spinner subcategorías en función de la categoría elegida----------\\

        categorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                if (pos == 0) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.sub1, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categoria = categorias.getItemAtPosition(pos).toString();
                    values.get(position).setCategoria(categoria);

                    subcategorias.setAdapter(adapter);

                    if(!subcategoria.equals("-")){
                        ArrayList subcats = new ArrayList(Arrays.asList(context.getResources().getStringArray(R.array.sub1)));
                        subcategorias.setSelection(subcats.indexOf(subcategoria));
                    }

                } else if (pos == 1) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.sub2, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categoria = categorias.getItemAtPosition(pos).toString();
                    values.get(position).setCategoria(categoria);

                    subcategorias.setAdapter(adapter);

                    if(!subcategoria.equals("-")){
                        ArrayList subcats = new ArrayList(Arrays.asList(context.getResources().getStringArray(R.array.sub2)));
                        subcategorias.setSelection(subcats.indexOf(subcategoria));
                    }
                } else if (pos == 2) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.sub3, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categoria = categorias.getItemAtPosition(pos).toString();
                    values.get(position).setCategoria(categoria);

                    subcategorias.setAdapter(adapter);

                    if(!subcategoria.equals("-")){
                        ArrayList subcats = new ArrayList(Arrays.asList(context.getResources().getStringArray(R.array.sub3)));
                        subcategorias.setSelection(subcats.indexOf(subcategoria));
                    }
                } else {
                    subcategorias.setAdapter(null);
                    categoria = categorias.getItemAtPosition(pos).toString();
                    values.get(position).setCategoria(categoria);
                    values.get(position).setSubcategoria("-");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.sub1, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categoria = categorias.getSelectedItem().toString();

                subcategorias.setAdapter(adapter);
            }
        });



        subcategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int po, long id) {

                subcategoria = subcategorias.getItemAtPosition(po).toString();
                values.get(position).setSubcategoria(subcategoria);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //----------Funcionalidad botones borrar y descripción----------\\

        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                haCambiado = true;
                remove(getItem(position));
                notifyDataSetChanged();
            }
        });

        descripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                haCambiado = true;
                final EditText e = new EditText(context);
                e.setHint("Descripción aquí...");

                AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Descripción")
                        .setView(e)
                        .setCancelable(true)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                descFoto = e.getText().toString();
                                desc.setText(descFoto);
                                values.get(position).setDescripcion(descFoto);
                            }
                        })
                        .create();
                alertdialogobuilder.show();

            }
        });

        //----------Fecha y hora de la foto----------\\
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM", Locale.getDefault());
        Date date = new Date();
        fecha = dateFormat.format(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("CET"));
        String tempHora = "" + calendar.get(Calendar.HOUR_OF_DAY);
        if (tempHora.length() == 1) {
            tempHora = "0" + tempHora;
        }
        String tempMin = "" + calendar.get(Calendar.MINUTE);
        if (tempMin.length() == 1) {
            tempMin = "0" + tempMin;
        }
        String tempSeg = "" + calendar.get(Calendar.SECOND);
        if (tempSeg.length() == 1) {
            tempSeg = "0" + tempSeg;
        }

        hora = tempHora + ":" + tempMin + ":" + tempSeg;

        //----------Recoger coordenadas GPS----------\\
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mensajeDeAlertaGPS();

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                //ActivityCompat.requestPermissions(, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                if (location != null) {
                    double latti = location.getLatitude();
                    double longi = location.getLongitude();
                    lattitude = String.valueOf(latti);
                    longitude = String.valueOf(longi);

                } else if (location1 != null) {
                    double latti = location1.getLatitude();
                    double longi = location1.getLongitude();
                    lattitude = String.valueOf(latti);
                    longitude = String.valueOf(longi);

                } else if (location2 != null) {
                    double latti = location2.getLatitude();
                    double longi = location2.getLongitude();
                    lattitude = String.valueOf(latti);
                    longitude = String.valueOf(longi);

                } else {
                    //Toast.makeText(context, "Imposible localizar tu posición", Toast.LENGTH_SHORT).show();

                }
            }
        }

        //----------INSERTAR INFORMACIÓN EN OBJETO FOTO ANTES DE VOLVER----------\\

        Foto temp = values.get(position);

        temp.setDescripcion(descFoto);
        temp.setCategoria(categoria);
        temp.setSubcategoria(subcategoria);
        temp.setFecha(fecha);
        temp.setHora(hora);
        temp.setCoordenadasFoto(longitude + "," + lattitude);
        temp.setId(fecha+hora);
        values.set(position, temp);
        Log.d("Mi foto...", values.get(position).toString());
        return rowView;

    }

    protected void mensajeDeAlertaGPS() {

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setMessage("Por favor activa el GPS de tu dispositivo")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isHaCambiado() {
        return haCambiado;
    }
    public void setHaCambiado(boolean haCambiado){
        this.haCambiado = haCambiado;
    }
}
