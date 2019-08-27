package com.ceromiedo.combizona.grisuno.Noticias;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ceromiedo.combizona.grisuno.Actividades.Usuario_Recorrido;
import com.ceromiedo.combizona.grisuno.Constantes;
import com.ceromiedo.combizona.grisuno.Noticias.Adaptador.AdaptadorNoticias;
import com.ceromiedo.combizona.grisuno.Noticias.Objeto.ObjetoNoticia;
import com.ceromiedo.combizona.grisuno.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Noticias extends Activity {

    //PREFERENCIAS
    public SharedPreferences preferencias_Variables;
    public SharedPreferences.Editor editor_Variables;

    RecyclerView rv_ListaNoticias;
    FirebaseFirestore bd_Noticias;

    List<ObjetoNoticia> objetos = new ArrayList<>();
    AdaptadorNoticias adaptador;

    Button btn_atras;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_activitidad_noticias);

        preferencias_Variables = getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE);
        editor_Variables = getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE).edit();
        //XML
        rv_ListaNoticias = findViewById(R.id.rv_ListaNoticias);
        btn_atras = findViewById(R.id.btn_avisos_atras);

        btn_atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),Usuario_Recorrido.class);
                startActivity(i);
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
                    overridePendingTransition(R.anim.anim_entrada_izquierda,R.anim.anim_salida_izquierda);
            }
        });

        //DATABASE
        bd_Noticias = FirebaseFirestore.getInstance();

        rv_ListaNoticias.setLayoutManager(new LinearLayoutManager(Noticias.this));
        objetos.clear();
        obtenerAvisos();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(),Usuario_Recorrido.class);
        startActivity(i);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            overridePendingTransition(R.anim.anim_entrada_izquierda,R.anim.anim_salida_izquierda);

    }

    //----- FUNCIONES
    private void obtenerAvisos(){
        bd_Noticias.collection("avisos").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documento : task.getResult()){
                        ObjetoNoticia obj = new ObjetoNoticia(documento.get("Titulo").toString(),documento.get("Cuerpo").toString(),documento.get("Fecha").toString());
                        objetos.add(obj);
                    }
                    ordenarPorFecha();
                    adaptador = new AdaptadorNoticias(rv_ListaNoticias,Noticias.this,objetos);
                    rv_ListaNoticias.setAdapter(adaptador);
                    adaptador.notifyDataSetChanged();
                }
            }
        });
    }

    private void ordenarPorFecha(){
        Collections.sort(objetos, new Comparator<ObjetoNoticia>() {
            @Override
            public int compare(ObjetoNoticia v1, ObjetoNoticia v2) {
                return v2.getFecha().compareTo(v1.getFecha());
            }
        });
    }
}

