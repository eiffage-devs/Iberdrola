package com.eiffage.companias.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eiffage.companias.Objetos.TouchImageView;
import com.eiffage.companias.R;

public class FotoPantallaCompleta extends AppCompatActivity {

    TouchImageView fotoPantallaCompleta;

    //
    //      Método para usar flecha de atrás en Action Bar
    //
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_pantalla_completa);

        fotoPantallaCompleta = findViewById(R.id.fotoPantallaCompleta);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Detalle de foto");

        Intent i = getIntent();
        String url = i.getStringExtra("urlImagen");
        String token = i.getStringExtra("token");

        if(token.equals("-")){
            Glide.with(this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    fotoPantallaCompleta.setImageBitmap(resource);
                }
            });
        }
        else {
            GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build());

            Glide.with(this).asBitmap().load(glideUrl).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    fotoPantallaCompleta.setImageBitmap(resource);
                }
            });
        }
    }
}
