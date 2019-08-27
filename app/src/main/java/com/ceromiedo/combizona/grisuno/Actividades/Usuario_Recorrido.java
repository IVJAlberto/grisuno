package com.ceromiedo.combizona.grisuno.Actividades;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ceromiedo.combizona.grisuno.Constantes;
import com.ceromiedo.combizona.grisuno.Noticias.Adaptador.AdaptadorNoticias;
import com.ceromiedo.combizona.grisuno.Noticias.Objeto.ObjetoNoticia;
import com.ceromiedo.combizona.grisuno.R;
import com.ceromiedo.combizona.grisuno.Recibidores.AlarmaNotificacion;
import com.ceromiedo.combizona.grisuno.Recibidores.Apagado;
import com.ceromiedo.combizona.grisuno.Recibidores.Cambio_Hora;
import com.ceromiedo.combizona.grisuno.Servicio.ServicioUbicacion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Usuario_Recorrido extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //------ALARMA------//
    AlarmManager manejadorAlarma;
    boolean alarmaActiva;
    Intent iAlarma, iServicio;
    PendingIntent piAlarma;

    //------ARREGLOS------//
    String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};

    //------BOOLEANS------//
    Boolean hayInternet = false, inicioNormal = true, politicasPrivacidad = false, primeraVez = true, servicio;

    //------BROADCASTER------//
    public BroadcastReceiver recibirCoordenadas, recibir_Apagado, recibidor_CambioHora;
    private BroadcastReceiver recibidor_Notificaciones;

    //------CONSTANTES------//
    Constantes constantes = new Constantes();

    //------CONTADORES------//
    int  c_CreacionRecorrido = 0, c_Quemados = 0, c_Suma = 0;

    //------COORDENADAS------//
    private LocationManager manejadorUbicacion;
    String hora, parada;

    //------ENTEROS------//
    int picker_Hora=0, picker_Minutos=0;

    //------FIRESTORE------//
    FirebaseFirestore ff_BaseDatos;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference pathRef;

    //------HANDLERS------//
    Handler ha_Destino = new Handler();
    public Handler ha_Actualizar_UI = new Handler();

    //------HORA------//
    DateFormat formatoFecha_d = new SimpleDateFormat("dd");
    DateFormat formatoFecha_m = new SimpleDateFormat("MM");
    DateFormat formatoFecha_md = new SimpleDateFormat("MM-dd");
    DateFormat formatoFecha_ymd = new SimpleDateFormat("yyy-MM-dd");
    String anio, fecha;

    //------MENU------//
    private DrawerLayout drawer_Menu;
    private ImageButton ibtn_Menu;
    private ImageView iv_Foto;
    private NavigationView nv_Menu;
    private String usuario_activo, correo_activo;
    private TextView tv_Nombre, tv_Correo;
    private View encabezado_menu;

    //------PREFERENCIAS------//
    public SharedPreferences preferencias_Variables;
    public SharedPreferences.Editor editor_Variables;
    String TAG = "Usuario_Recorrido";

    //------RUTA------//
    int[] Tiempos_OSo_DT = {6,15,18,30,45,55,62,70};
    int[] Tiempos_OSo_DE = {6,15,18,30,45,55,62,72};
    int[] Tiempos_OSa_DT = {10,19,22,34,49,59,66,74};
    int[] Tiempos_OSa_DE = {10,19,22,34,49,59,66,76};
    int[] Tiempos_OT_DSo = {8, 15, 23, 38, 50, 60, 66, 70};
    int[] Tiempos_OT_DSa = {8, 15, 23, 38, 50, 60, 66, 75};
    int[] Tiempos_OE_DSo = {8, 15, 23, 38, 50, 60, 66, 70};
    int[] Tiempos_OE_DSa = {8, 15, 23, 38, 50, 60, 66, 75};

    String unidad, origen, destino, str_HoraI, s_unidad_Activa;
    String[] Unidad = {"UNIDAD", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32",
            "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65","66","67","68", "69"};
    String[] Origen = {"ORIGEN", "Soledad", "Satélite", "Trincheras", "Encinos"};
    String[] DestinoInicial = {"DESTINO","",""};
    String[] Destino = {"DESTINO", "Soledad", "Satélite"};
    String[] Destino2 = {"DESTINO", "Trincheras", "Encinos"};
    String[] Paradas_D_T = {"Educadores","Tecnológico", "Lomas","Bosque","Ocampo","Lágrima","Santa Cecilia"};
    String[] Paradas_D_E = {"Educadores","Tecnológico", "Lomas","Bosque","Ocampo","Lágrima","Santa Cecilia"};
    String[] Paradas_O_T = {"Santa Cecilia","Lágrima","Ocampo","Bosque","Lomas","Sec. 65","Educadores"};
    String[] Paradas_O_E = {"Santa Cecilia","Lágrima","Ocampo","Bosque","Lomas","Sec. 65","Educadores"};

    //------SERVICIO------//
    public Intent intentoServicio = new Intent();

    //------STRINGS------//
    String  llave_TR = "" ,s_Hora = "", s_Minuto = "", uid = "";

    //------XML------//
    Button  btn_SHora;
    CardView cv_PrepararRuta, cv_CancelarRuta;
    TextView tv_PrepararRuta, tv_CancelarRuta;
    ProgressBar pb_PrepararRuta, pb_CancelarRuta;
    ImageView iv_Flecha, iv_Flecha2, iv_Hora;
    LinearLayout fondo_iv_flecha, fondo_iv_flecha2, fondo_iv_hora;
    Spinner spOrigen, spUnidad, spDestino;
    String sImagen, caminoCache;
    TextView tvParada1, tvP1Tiempo, tvP1Difiempo, tvP1DT,
             tvParada2, tvP2Tiempo, tvP2Difiempo, tvP2DT,
             tvParada3, tvP3Tiempo, tvP3Difiempo, tvP3DT,
             tvParada4, tvP4Tiempo, tvP4Difiempo, tvP4DT,
             tvParada5, tvP5Tiempo, tvP5Difiempo, tvP5DT,
             tvParada6, tvP6Tiempo, tvP6Difiempo, tvP6DT,
             tvParada7, tvP7Tiempo, tvP7Difiempo, tvP7DT,
//             tvParada8, tvP8Tiempo, tvP8Difiempo, tvP8DT,
//             tvParada9, tvP9Tiempo, tvP9Difiempo, tvP9DT,
//             tvParada10, tvP10Tiempo, tvP10Difiempo, tvP10DT,
//             tvParada11, tvP11Tiempo, tvP11Difiempo, tvP11DT,
             tvDestino, tvTiempoDestino, tvDifTDestino, tvPDDT,
             tv_UnidadSiguiendo, tv_TiempoUnidadSiguiendo;


    @Override
    public void onBackPressed() {
        if (drawer_Menu.isDrawerOpen(GravityCompat.START))
            drawer_Menu.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activitidad_usuario);

        preferencias_Variables = getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE);
        editor_Variables = getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE).edit();
        ff_BaseDatos = FirebaseFirestore.getInstance();
        caminoCache = getApplicationContext().getCacheDir().getAbsolutePath() + "/";
        intentoServicio = new Intent(getApplicationContext(), ServicioUbicacion.class);
        manejadorUbicacion = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        manejadorAlarma = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

//        recibidor_Notificaciones = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Bundle extras = intent.getExtras();
//
//                if (extras != null && extras.containsKey("titulo")){
//                    String id_Notificacion = extras.getString("titulo");
//                    Toast.makeText(getApplicationContext(), "ID: " + id_Notificacion, Toast.LENGTH_LONG).show();
//                    f_Obtener_Recorrido(id_Notificacion);
//                    editor_Variables.putString("ID_Notificacion",id_Notificacion+"");
//                    editor_Variables.commit();
//                }
//            }
//        };

        recibirCoordenadas = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                if(intent.getAction()!=null && intent.getAction().equals("COORDENADAS") && c_Suma ==0)
                    f_ActualizarCoordenadas(intent);
            }
        };


        nv_Menu = findViewById(R.id.nav_view);
        nv_Menu.setNavigationItemSelectedListener(this);
        nv_Menu.setItemIconTintList(null);
        encabezado_menu = nv_Menu.getHeaderView(0);
        iv_Foto = encabezado_menu.findViewById(R.id.iv_Foto);
        tv_Nombre = encabezado_menu.findViewById(R.id.tv_Nombre);
        tv_Correo = encabezado_menu.findViewById(R.id.tv_Correo);
        tv_Nombre.setText(preferencias_Variables.getString("nombre","CombiZona Oro Verde"));
        tv_Correo.setText(preferencias_Variables.getString("correo","CombiZona_OV@gmail.com"));
        f_Asignar_Foto(iv_Foto);

        editor_Variables.putString("btn_Cancelar", "Cancelar");
        editor_Variables.commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.nav_ubicacion:
                f_Configuracion_GPS();
                break;
            case R.id.nav_r_datos:
                f_Refrescar_Datones();
                break;
            case R.id.nav_avisos:
                new asyt_Noticias().execute();
                break;
            case R.id.nav_salir:
                f_Salir();
                break;
            case R.id.nav_privacidad:
                f_Politicas_Privacidad("Menu_Extendible");
                break;
            case R.id.nav_librerias:
                f_Librerias();
                break;
            case R.id.nav_version:
//                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
//                String horaInicial = s_Hora + "-" + s_Minuto;
//                String nombreRecorrido = /*fechaRecorrido + "_" +*/ horaInicial + "_" + origen + "-" + destino;
//                f_AgregarTexto(nombreRecorrido,"VERSION" ,"");
                break;
            default:
                break;
        }

        //cierra el drawer al presionar botón
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor_Variables.putString("origen", spOrigen.getSelectedItem().toString());
        editor_Variables.putString("destino", spDestino.getSelectedItem().toString());
        editor_Variables.putString("unidad", spUnidad.getSelectedItem().toString());
        editor_Variables.putInt("picker_Hora", picker_Hora);
        editor_Variables.putInt("picker_Minutos", picker_Minutos);
        editor_Variables.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        f_XmlToAndroid();
        f_Botones();
        uid = preferencias_Variables.getString("usuario_ID","");
        politicasPrivacidad = preferencias_Variables.getBoolean("pp",false);
        primeraVez = preferencias_Variables.getBoolean("primeraVez",true);
        usuario_activo = preferencias_Variables.getString("nombre", "");
        correo_activo = preferencias_Variables.getString("correo", "");

        inicioNormal = preferencias_Variables.getBoolean("inicioNormal",true);

//        LocalBroadcastManager.getInstance(this).registerReceiver(recibidor_Notificaciones, new IntentFilter(Constantes.INTENT_NOTIFICACION));

        ha_Actualizar_UI.post(act_UI);

        if (primeraVez)
            f_PrimeraVez();

        if (inicioNormal)
            ha_Destino.post(act_Destino);

        llave_TR = preferencias_Variables.getString("llave_TR", "");
        unidad = preferencias_Variables.getString("unidad", "UNIDAD");
        picker_Hora = preferencias_Variables.getInt("picker_Hora", 0);
        picker_Minutos = preferencias_Variables.getInt("picker_Minutos", 0);
        origen = preferencias_Variables.getString("origen", "ORIGEN");
        destino = preferencias_Variables.getString("destino", "DESTINO");

        tv_PrepararRuta.setText(preferencias_Variables.getString("tv_PrepararRuta", "Preparar Ruta"));

        if (!unidad.equals("UNIDAD"))
            spUnidad.setSelection(Integer.parseInt(unidad));
        else
            spUnidad.setSelection(0);

        if (origen.equals("Soledad")) {
            spDestino.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_diseno_spinner, Destino2));
            spOrigen.setSelection(1);
        } else if (origen.equals("Satélite")) {
            spDestino.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_diseno_spinner, Destino2));
            spOrigen.setSelection(2);
        } else if (origen.equals("Trincheras")) {
            spDestino.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_diseno_spinner, Destino));
            spOrigen.setSelection(3);
        } else if (origen.equals("Encinos")) {
            spDestino.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_diseno_spinner, Destino));
            spOrigen.setSelection(4);
        }

        switch (destino){
            case "Soledad":
                spDestino.setSelection(1);
                break;
            case "Satélite":
                spDestino.setSelection(2);
                break;
            case "Trincheras":
                spDestino.setSelection(1);
                break;
            case "Encinos":
                spDestino.setSelection(2);
                break;
        }
        s_Hora = f_AgregarDecena(picker_Hora);
        s_Minuto = f_AgregarDecena(picker_Minutos);
        str_HoraI = s_Hora + ":" + s_Minuto;
        if (!str_HoraI.equals("00:00"))
            btn_SHora.setText(str_HoraI);
        else
            btn_SHora.setText("HORA");

        cv_CancelarRuta.setEnabled(preferencias_Variables.getBoolean("bool_btnCancelar", false));

        if (!tv_PrepararRuta.getText().toString().equals("Preparar Ruta")) {
            fecha = preferencias_Variables.getString("fecha", "");
            str_HoraI = preferencias_Variables.getString("hora_inicial", "00:00");

            s_unidad_Activa = preferencias_Variables.getString("unidadActiva", "true");
            tvParada1.setText(preferencias_Variables.getString("parada1", ""));
            tvParada2.setText(preferencias_Variables.getString("parada2", ""));
            tvParada3.setText(preferencias_Variables.getString("parada3", ""));
            tvParada4.setText(preferencias_Variables.getString("parada4", ""));
            tvParada5.setText(preferencias_Variables.getString("parada5", ""));
            tvParada6.setText(preferencias_Variables.getString("parada6", ""));
            tvParada7.setText(preferencias_Variables.getString("parada7", ""));
//            tvParada8.setText(preferencias_Variables.getString("parada8", ""));
//            tvParada9.setText(preferencias_Variables.getString("parada9", ""));
//            tvParada10.setText(preferencias_Variables.getString("parada10", ""));
//            tvParada11.setText(preferencias_Variables.getString("parada11", ""));
            tvDestino.setText(preferencias_Variables.getString("paradaDestino", ""));
            tvP1Tiempo.setText(preferencias_Variables.getString("Punto_1", ""));
            tvP2Tiempo.setText(preferencias_Variables.getString("Punto_2", ""));
            tvP3Tiempo.setText(preferencias_Variables.getString("Punto_3", ""));
            tvP4Tiempo.setText(preferencias_Variables.getString("Punto_4", ""));
            tvP5Tiempo.setText(preferencias_Variables.getString("Punto_5", ""));
            tvP6Tiempo.setText(preferencias_Variables.getString("Punto_6", ""));
            tvP7Tiempo.setText(preferencias_Variables.getString("Punto_7", ""));
//            tvP8Tiempo.setText(preferencias_Variables.getString("Punto_8", ""));
//            tvP9Tiempo.setText(preferencias_Variables.getString("Punto_9", ""));
//            tvP10Tiempo.setText(preferencias_Variables.getString("Punto_10", ""));
//            tvP11Tiempo.setText(preferencias_Variables.getString("Punto_11", ""));
            tvTiempoDestino.setText(preferencias_Variables.getString("Punto_Destino", ""));
            tvP1Difiempo.setText(preferencias_Variables.getString("Punto_1_Dif", "0"));
            f_Agregar_Color(tvP1Difiempo,Integer.parseInt(tvP1Difiempo.getText().toString()));
            tvP2Difiempo.setText(preferencias_Variables.getString("Punto_2_Dif", "0"));
            f_Agregar_Color(tvP2Difiempo,Integer.parseInt(tvP2Difiempo.getText().toString()));
            tvP3Difiempo.setText(preferencias_Variables.getString("Punto_3_Dif", "0"));
            f_Agregar_Color(tvP3Difiempo,Integer.parseInt(tvP3Difiempo.getText().toString()));
            tvP4Difiempo.setText(preferencias_Variables.getString("Punto_4_Dif", "0"));
            f_Agregar_Color(tvP4Difiempo,Integer.parseInt(tvP4Difiempo.getText().toString()));
            tvP5Difiempo.setText(preferencias_Variables.getString("Punto_5_Dif", "0"));
            f_Agregar_Color(tvP5Difiempo,Integer.parseInt(tvP5Difiempo.getText().toString()));
            tvP6Difiempo.setText(preferencias_Variables.getString("Punto_6_Dif", "0"));
            f_Agregar_Color(tvP6Difiempo,Integer.parseInt(tvP6Difiempo.getText().toString()));
            tvP7Difiempo.setText(preferencias_Variables.getString("Punto_7_Dif", "0"));
            f_Agregar_Color(tvP7Difiempo,Integer.parseInt(tvP7Difiempo.getText().toString()));
//            tvP8Difiempo.setText(preferencias_Variables.getString("Punto_8_Dif", "0"));
//            f_Agregar_Color(tvP8Difiempo,Integer.parseInt(tvP8Difiempo.getText().toString()));
//            tvP9Difiempo.setText(preferencias_Variables.getString("Punto_9_Dif", "0"));
//            f_Agregar_Color(tvP9Difiempo,Integer.parseInt(tvP9Difiempo.getText().toString()));
//            tvP10Difiempo.setText(preferencias_Variables.getString("Punto_10_Dif", "0"));
//            f_Agregar_Color(tvP10Difiempo,Integer.parseInt(tvP10Difiempo.getText().toString()));
//            tvP11Difiempo.setText(preferencias_Variables.getString("Punto_11_Dif", "0"));
//            f_Agregar_Color(tvP11Difiempo,Integer.parseInt(tvP11Difiempo.getText().toString()));
            tvDifTDestino.setText(preferencias_Variables.getString("Punto_Destino_Dif", "0"));
            f_Agregar_Color(tvDifTDestino,Integer.parseInt(tvDifTDestino.getText().toString()));
            tvP1DT.setText(preferencias_Variables.getString("dift1",""));
            tvP2DT.setText(preferencias_Variables.getString("dift2",""));
            tvP3DT.setText(preferencias_Variables.getString("dift3",""));
            tvP4DT.setText(preferencias_Variables.getString("dift4",""));
            tvP5DT.setText(preferencias_Variables.getString("dift5",""));
            tvP6DT.setText(preferencias_Variables.getString("dift6",""));
            tvP7DT.setText(preferencias_Variables.getString("dift7",""));
//            tvP8DT.setText(preferencias_Variables.getString("dift8",""));
//            tvP9DT.setText(preferencias_Variables.getString("dift9",""));
//            tvP10DT.setText(preferencias_Variables.getString("dift10",""));
//            tvP11DT.setText(preferencias_Variables.getString("dift11",""));
            tvPDDT.setText(preferencias_Variables.getString("diftD",""));
            tv_CancelarRuta.setText(preferencias_Variables.getString("btn_Cancelar", "Cancelar"));
            if(cv_CancelarRuta.isEnabled())
                cv_CancelarRuta.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
            else
                cv_CancelarRuta.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));

            if (tvTiempoDestino.getText().toString() != ""){
                tv_PrepararRuta.setText("Ruta Finalizada");
                tv_CancelarRuta.setText("Limpiar");
            }

            spUnidad.setEnabled(false);
            spOrigen.setEnabled(false);
            spDestino.setEnabled(false);
            btn_SHora.setEnabled(false);
            iv_Flecha.setEnabled(false);
            iv_Flecha2.setEnabled(false);
            iv_Hora.setEnabled(false);
            spUnidad.setAlpha(0.5f);
            spOrigen.setAlpha(0.5f);
            spDestino.setAlpha(0.5f);
            btn_SHora.setAlpha(0.5f);
            iv_Flecha.setAlpha(0.5f);
            iv_Flecha2.setAlpha(0.5f);
            iv_Hora.setAlpha(0.5f);
            fondo_iv_flecha.setAlpha(0.5f);
            fondo_iv_flecha2.setAlpha(0.5f);
            fondo_iv_hora.setAlpha(0.5f);

            if(!tv_PrepararRuta.getText().toString().equals("Iniciar Ruta")){
                cv_PrepararRuta.setEnabled(false);
                cv_PrepararRuta.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));

                if (!tv_PrepararRuta.getText().toString().equals("Ruta Finalizada")){
                    boolean bool_Servicio = constantes.servicioActivo(getApplicationContext());
                    if (!bool_Servicio) {
                        f_IniciarServicio();
                        String nombreArchivo = preferencias_Variables.getString("nombreArchivo","");
                        f_Iniciar_Recibidor_Apagado(nombreArchivo);
                        f_Iniciar_Recibidor_CambioHora(nombreArchivo);
                    }
                }
            }else{
                cv_PrepararRuta.setEnabled(true);
                cv_PrepararRuta.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(recibir_Apagado);
            unregisterReceiver(recibidor_CambioHora);
        }catch (Exception e){}

    }

    //*************************ASYNCTASK************************//

    private class asyt_Cancelar extends AsyncTask<String, String, String>{
        ProgressDialog dialogo;
        boolean esLimpia;

        asyt_Cancelar(boolean esLimpia){
            this.esLimpia = esLimpia;
        }

        @Override
        protected void onPreExecute() {
//            if(!((Usuario_Recorrido.this).isFinishing())){
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//                    dialogo = ProgressDialog.show(Usuario_Recorrido.this,"¡Finalizando!" , "Limpiando recorrido...");
//                }else {
//                    dialogo = new ProgressDialog(Usuario_Recorrido.this, R.style.DialogoProgresivo);
//                    f_Dialogo(dialogo, "¡Finalizando!", "Limpiando recorrido...");
//                }
//            }
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                Thread.sleep(500);
            }catch (Exception e){}
            return null;
        }

        @Override
        protected void onPostExecute(String bool) {
            if(!((Usuario_Recorrido.this).isFinishing())  && dialogo!=null)
                dialogo.dismiss();
            f_ReiniciarVariables(false,esLimpia,false);
        }
    }
    private class asyt_Conexion extends AsyncTask<String,String, Boolean> {
        ProgressDialog dialogo;
        String str;
        HttpURLConnection urlc = null;

        @Override
        protected void onPreExecute() {
//            if(!((Usuario_Recorrido.this).isFinishing())){
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//                    dialogo = ProgressDialog.show(Usuario_Recorrido.this,"¡Analizando!" , "Comprobando conexión a internet...");
//                }else{
//                    dialogo = new ProgressDialog(Usuario_Recorrido.this, R.style.DialogoProgresivo);
//                    f_Dialogo(dialogo,"¡Analizando!","Comprobando conexión a internet...");
//                }
//            }
        }
        @Override
        protected Boolean doInBackground(String... params) {
            str = params[0];
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ConnectivityManager conMan = (ConnectivityManager)getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
            NetworkInfo ni_Activa = conMan.getActiveNetworkInfo();
            if(ni_Activa != null && ni_Activa.isConnected()){
                try {
                    urlc = (HttpURLConnection)(new URL("http://www.google.com").openConnection());
                    urlc.setRequestProperty("User-Agent","Android");
                    urlc.setRequestProperty("Connection","close");
                    urlc.setConnectTimeout(1000);
                    urlc.connect();
                    return (urlc.getResponseCode()==200);
                }catch (Exception e){

                }finally {
                    urlc.disconnect();
                }
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean bool) {
            if(!((Usuario_Recorrido.this).isFinishing()) && dialogo!=null)
                dialogo.dismiss();
            if (urlc != null)
                urlc.disconnect();
            if(bool)
                hayInternet = true;
            else{
                hayInternet = false;
                f_Toast("No hay conexión a internet", "error", 0);
            }

        }
    }
    private class asyt_IniciarRecorrido extends AsyncTask<DocumentReference, String, String> {
        ProgressDialog dialogo;
        String nombreRecorrido;

        @Override
        protected void onPreExecute() {
            c_CreacionRecorrido++;
//            if(!((Usuario_Recorrido.this).isFinishing())){
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//                    dialogo = ProgressDialog.show(Usuario_Recorrido.this,"¡Iniciando!" , "Creando recorrido...");
//                }else {
//                    dialogo = new ProgressDialog(Usuario_Recorrido.this, R.style.DialogoProgresivo);
//                    f_Dialogo(dialogo, "¡Iniciando!", "Creando recorrido...");
//                }
//            }
        }

        @Override
        protected String doInBackground(DocumentReference... params) {
            try {
                llave_TR = params[0].getId() + "";
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!((Usuario_Recorrido.this).isFinishing())  && dialogo!=null)
                dialogo.dismiss();

            f_IniciarServicio();

            String fechaRecorrido = formatoFecha_ymd.format(Calendar.getInstance().getTime());
            String horaInicial = s_Hora + "-" + s_Minuto;
            nombreRecorrido = /*fechaRecorrido + "_" +*/ horaInicial + "_" + origen + "-" + destino;
            f_GenerarArchivoRecorrido(nombreRecorrido, "R",  fechaRecorrido + "/" + horaInicial + "/" + origen.substring(0,2) + "-" + destino.substring(0,2) + "/" + getString(R.string.version));
            DateFormat formatoIniciar = new SimpleDateFormat("HH:mm:ss");
            hora = formatoIniciar.format(Calendar.getInstance().getTime());
            f_AgregarTexto(nombreRecorrido,"BI" ,hora);
            editor_Variables.putString("boton_iniciar",hora);

            f_Iniciar_Recibidor_Apagado(nombreRecorrido);
            f_Iniciar_Recibidor_CambioHora(nombreRecorrido);

            Map<String, Object> datones_Unidad = new HashMap<>();
            datones_Unidad.put("Activo", true);
            datones_Unidad.put("Conductor", usuario_activo);

            ff_BaseDatos.collection("unidades").document(unidad).set(datones_Unidad, SetOptions.merge());

            editor_Variables.putString("llave_TR", llave_TR);
            editor_Variables.putString("nombreArchivo", nombreRecorrido);

            tvP1Difiempo.setText("0");
            tvP1Difiempo.setTextColor(Color.GREEN);
            tvP2Difiempo.setText("0");
            tvP2Difiempo.setTextColor(Color.GREEN);
            tvP3Difiempo.setText("0");
            tvP3Difiempo.setTextColor(Color.GREEN);
            tvP4Difiempo.setText("0");
            tvP4Difiempo.setTextColor(Color.GREEN);
            tvP5Difiempo.setText("0");
            tvP5Difiempo.setTextColor(Color.GREEN);
            tvP6Difiempo.setText("0");
            tvP6Difiempo.setTextColor(Color.GREEN);
            tvP7Difiempo.setText("0");
            tvP7Difiempo.setTextColor(Color.GREEN);
//            tvP8Difiempo.setText("0");
//            tvP8Difiempo.setTextColor(Color.GREEN);
//            tvP9Difiempo.setText("0");
//            tvP9Difiempo.setTextColor(Color.GREEN);
//            tvP10Difiempo.setText("0");
//            tvP10Difiempo.setTextColor(Color.GREEN);
//            tvP11Difiempo.setText("0");
//            tvP11Difiempo.setTextColor(Color.GREEN);
            tvDifTDestino.setText("0");
            tvDifTDestino.setTextColor(Color.GREEN);
            editor_Variables.putString("Punto_1_Dif", tvP1Difiempo.getText().toString());
            editor_Variables.putString("Punto_2_Dif", tvP2Difiempo.getText().toString());
            editor_Variables.putString("Punto_3_Dif", tvP3Difiempo.getText().toString());
            editor_Variables.putString("Punto_4_Dif", tvP4Difiempo.getText().toString());
            editor_Variables.putString("Punto_5_Dif", tvP5Difiempo.getText().toString());
            editor_Variables.putString("Punto_6_Dif", tvP6Difiempo.getText().toString());
            editor_Variables.putString("Punto_7_Dif", tvP7Difiempo.getText().toString());
//            editor_Variables.putString("Punto_8_Dif", tvP8Difiempo.getText().toString());
//            editor_Variables.putString("Punto_9_Dif", tvP9Difiempo.getText().toString());
//            editor_Variables.putString("Punto_10_Dif", tvP10Difiempo.getText().toString());
//            editor_Variables.putString("Punto_11_Dif", tvP11Difiempo.getText().toString());
            editor_Variables.putString("Punto_Destino_Dif", tvDifTDestino.getText().toString());
            editor_Variables.commit();
            tv_PrepararRuta.setVisibility(View.VISIBLE);
            pb_PrepararRuta.setVisibility(View.GONE);
        }

    }
    private class asyt_Noticias extends AsyncTask<String, String, String>{
        ProgressDialog dialogo;

        @Override
        protected void onPreExecute() {
            if(!((Usuario_Recorrido.this).isFinishing())){
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                    dialogo = ProgressDialog.show(Usuario_Recorrido.this,"¡Noticias!" , "Descargando noticias...");
                }else {
                    dialogo = new ProgressDialog(Usuario_Recorrido.this, R.style.DialogoProgresivo);
                    f_Dialogo(dialogo, "¡Noticias!", "Descargando noticias...");
                }
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!((Usuario_Recorrido.this).isFinishing())  && dialogo!=null)
                dialogo.dismiss();

            f_Noticias();
        }
    }
    private class asyt_Salir extends AsyncTask<String,String ,String>{
        ProgressDialog dialogo;
        @Override
        protected void onPreExecute() {
            if (drawer_Menu.isDrawerOpen(GravityCompat.START))
                drawer_Menu.closeDrawer(GravityCompat.START);
            if(!((Usuario_Recorrido.this).isFinishing())){
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                    dialogo = ProgressDialog.show(Usuario_Recorrido.this,"¡Salir!" , "Cerrando sesión...");
                }else {
                    dialogo = new ProgressDialog(Usuario_Recorrido.this, R.style.DialogoProgresivo);
                    f_Dialogo(dialogo, "¡Salir!", "Cerrando sesión...");
                }
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            dialogo.dismiss();
            f_Toast("¡Gracias por utilizar CombiZona: Oro Verde!","combi",0);
            f_ReiniciarVariables(true,false,false);
        }
    }
    private class asyt_Suma extends AsyncTask<String, String, String> {
        Map<String, Object> datones_Registro = new HashMap<>();
        int suma = 0;
        int vacio = 0;
        @Override
        protected void onPreExecute() {
            int c = 1;
            do {
                int v;
                if (c<8){
                    v = Integer.parseInt(preferencias_Variables.getString("Punto_"+c+"_Dif","0"));
                    String s = preferencias_Variables.getString("Punto_"+c,"0");
                    Log.e(TAG,s );
                    if (f_ComprobarVacio(s) == null)
                        vacio++;
                } else
                    v = Integer.parseInt(preferencias_Variables.getString("Punto_Destino_Dif","0"));

                if ( v > 0)
                    suma += v;
                c++;
            }while (c<9);

            Log.e(TAG,"Vacio " + vacio );

            datones_Registro.put("f", fecha);
            datones_Registro.put("c", usuario_activo);
            datones_Registro.put("u", unidad);
            datones_Registro.put("lo", origen);
            datones_Registro.put("ld", destino);
            datones_Registro.put("hi", str_HoraI);
            datones_Registro.put("p1", preferencias_Variables.getString("Punto_1", ""));
            datones_Registro.put("p2", preferencias_Variables.getString("Punto_2", ""));
            datones_Registro.put("p3", preferencias_Variables.getString("Punto_3", ""));
            datones_Registro.put("p4", preferencias_Variables.getString("Punto_4", ""));
            datones_Registro.put("p5", preferencias_Variables.getString("Punto_5", ""));
            datones_Registro.put("p6", preferencias_Variables.getString("Punto_6", ""));
            datones_Registro.put("p7", preferencias_Variables.getString("Punto_7", ""));
            /*datones_Registro.put("p8", preferencias_Variables.getString("Punto_8", ""));
            datones_Registro.put("p9", preferencias_Variables.getString("Punto_9", ""));
            datones_Registro.put("p10", preferencias_Variables.getString("Punto_10", ""));
            datones_Registro.put("p11", preferencias_Variables.getString("Punto_11", ""));*/
            if(preferencias_Variables.getString("Punto_Destino", "") == "")
                datones_Registro.put("pD","CANCELADO");
            else
                datones_Registro.put("pD", preferencias_Variables.getString("Punto_Destino", ""));
            datones_Registro.put("d1", preferencias_Variables.getString("Punto_1_Dif", "0"));
            datones_Registro.put("d2", preferencias_Variables.getString("Punto_2_Dif", "0"));
            datones_Registro.put("d3", preferencias_Variables.getString("Punto_3_Dif", "0"));
            datones_Registro.put("d4", preferencias_Variables.getString("Punto_4_Dif", "0"));
            datones_Registro.put("d5", preferencias_Variables.getString("Punto_5_Dif", "0"));
            datones_Registro.put("d6", preferencias_Variables.getString("Punto_6_Dif", "0"));
            datones_Registro.put("d7", preferencias_Variables.getString("Punto_7_Dif", "0"));
            /*datones_Registro.put("d8", preferencias_Variables.getString("Punto_8_Dif", "0"));
            datones_Registro.put("d9", preferencias_Variables.getString("Punto_9_Dif", "0"));
            datones_Registro.put("d10", preferencias_Variables.getString("Punto_10_Dif", "0"));
            datones_Registro.put("d11", preferencias_Variables.getString("Punto_11_Dif", "0"));*/
            datones_Registro.put("dD", preferencias_Variables.getString("Punto_Destino_Dif", "0"));
            datones_Registro.put("s", suma);
            datones_Registro.put("si", preferencias_Variables.getString("boton_iniciar","00:00"));
            datones_Registro.put("sd", preferencias_Variables.getString("boton_detener","00:00"));
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(c_Suma == 1){
                ff_BaseDatos.collection("registros").add(datones_Registro).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if(llave_TR!="" && llave_TR!=null){
                            ff_BaseDatos.collection("registros_TR").document(llave_TR)
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documento) {
                                    if(documento.exists()){
                                        ff_BaseDatos.collection("registros_TR").document(llave_TR).delete();
                                        llave_TR = "";
                                        editor_Variables.putString("llave_TR", llave_TR);
                                        editor_Variables.commit();
                                    }
                                }
                            });
                        }
                    }
                });
                if(c_Quemados == 0){
                    c_Quemados++;
                    f_Tiempo_Quemados(suma);
                    if (vacio >= 3) f_Subir_Incidencias();
                }
                c_Suma = 0;
            }
        }
    }


    //*************************FUNCIONES************************//

    private void f_ActualizarCoordenadas(Intent intent) {
        parada = intent.getStringExtra(ServicioUbicacion.ACCION_PUNTO);
        hora = intent.getStringExtra(ServicioUbicacion.ACCION_HORA);
        String st_DiferenciaHor = intent.getStringExtra(ServicioUbicacion.ACCION_DIF_HORA);

        String st_UnidadSiguiendo = intent.getStringExtra(ServicioUbicacion.ACCION_UNIDAD_SIGUIENDO);
        String st_TiempoUnidadSiguiendo = intent.getStringExtra(ServicioUbicacion.ACCION_TIEMPO_UNIDAD_SIGUIENDO);

        if (parada != null) {
            int color = Color.BLACK;
            if (parada !=  "Recorrido_I"){
                if (Integer.parseInt(st_DiferenciaHor) > 0)
                    color = Color.RED;
                else if (Integer.parseInt(st_DiferenciaHor) < 0)
                    color = Color.BLUE;
                else
                    color = Color.GREEN;
            }

            /*tv_UnidadSiguiendo.setText(st_UnidadSiguiendo);
            tv_TiempoUnidadSiguiendo.setText(st_TiempoUnidadSiguiendo);
            editor_Variables.putString("tv_UnidadSiguiendo", st_UnidadSiguiendo);
            editor_Variables.putString("tv_TiempoUnidadSiguiendo", st_TiempoUnidadSiguiendo);*/

            switch (parada) {
                case "Recorrido_I":
                    tv_PrepararRuta.setText("Ruta en Curso");
                    editor_Variables.putString("tv_PrepararRuta", tv_PrepararRuta.getText().toString());
                    editor_Variables.commit();
                    break;
                case "Punto_1":
                    tvP1Difiempo.setText(st_DiferenciaHor);
                    tvP1Difiempo.setTextColor(color);
                    tvP1Tiempo.setText(hora);
                    break;
                case "Punto_2":
                    tvP2Difiempo.setText(st_DiferenciaHor);
                    tvP2Difiempo.setTextColor(color);
                    tvP2Tiempo.setText(hora);
                    break;
                case "Punto_3":
                    tvP3Difiempo.setText(st_DiferenciaHor);
                    tvP3Difiempo.setTextColor(color);
                    tvP3Tiempo.setText(hora);
                    break;
                case "Punto_4":
                    tvP4Difiempo.setText(st_DiferenciaHor);
                    tvP4Difiempo.setTextColor(color);
                    tvP4Tiempo.setText(hora);
                    break;
                case "Punto_5":
                    tvP5Difiempo.setText(st_DiferenciaHor);
                    tvP5Difiempo.setTextColor(color);
                    tvP5Tiempo.setText(hora);
                    break;
                case "Punto_6":
                    tvP6Difiempo.setText(st_DiferenciaHor);
                    tvP6Difiempo.setTextColor(color);
                    tvP6Tiempo.setText(hora);
                    break;
                case "Punto_7":
                    tvP7Difiempo.setText(st_DiferenciaHor);
                    tvP7Difiempo.setTextColor(color);
                    tvP7Tiempo.setText(hora);
                    break;
//                case "Punto_8":
//                    tvP8Difiempo.setText(st_DiferenciaHor);
//                    tvP8Difiempo.setTextColor(color);
//                    tvP8Tiempo.setText(hora);
//                    break;
//                case "Punto_9":
//                    tvP9Difiempo.setText(st_DiferenciaHor);
//                    tvP9Difiempo.setTextColor(color);
//                    tvP9Tiempo.setText(hora);
//                    break;
//                case "Punto_10":
//                    tvP10Difiempo.setText(st_DiferenciaHor);
//                    tvP10Difiempo.setTextColor(color);
//                    tvP10Tiempo.setText(hora);
//                    break;
//                case "Punto_11":
//                    tvP11Difiempo.setText(st_DiferenciaHor);
//                    tvP11Difiempo.setTextColor(color);
//                    tvP11Tiempo.setText(hora);
//                    break;
                case "Punto_Destino":
                    tv_CancelarRuta.setText("Limpiar");
                    tv_PrepararRuta.setText("Ruta Finalizada");
                    editor_Variables.putString("btn_IniciarRuta", "Ruta Finalizada");
                    editor_Variables.putString("btn_Cancelar", "Limpiar");
                    editor_Variables.putBoolean("servicio_activo", false);
                    tvDifTDestino.setText(st_DiferenciaHor);
                    tvDifTDestino.setTextColor(color);
                    tvTiempoDestino.setText(hora);
                    f_DetenerAlarma();
                    stopService(intentoServicio);
                    break;
            }
            editor_Variables.commit();
        }
    }
    private String f_AgregarDecena(int i) {
        String s = i + "";
        if (i < 10)
            s = "0" + i;
        return s;
    }
    private void f_AgregarHora(){
        DateFormat for_Hora = new SimpleDateFormat("HH");
        DateFormat for_Minutos = new SimpleDateFormat("mm");
        String s_hora = for_Hora.format(Calendar.getInstance().getTime());
        String s_minutos = for_Minutos.format(Calendar.getInstance().getTime());
        int i_hora = Integer.parseInt(s_hora);
        int i_minutos = Integer.parseInt(s_minutos);

        TimePickerDialog.OnTimeSetListener onTimeSetListener  = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                picker_Hora = hourOfDay;
                picker_Minutos = minute;
                s_Hora = f_AgregarDecena(picker_Hora);
                s_Minuto = f_AgregarDecena(picker_Minutos);
                str_HoraI = s_Hora + ":" + s_Minuto;
                btn_SHora.setText(str_HoraI);
            }
        };


        TimePickerDialog timePickerDialog = new TimePickerDialog(Usuario_Recorrido.this ,onTimeSetListener, i_hora, i_minutos, false);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            timePickerDialog = new TimePickerDialog(Usuario_Recorrido.this, R.style.DialogoRelojTransparente, onTimeSetListener, i_hora, i_minutos, true);
        else
            timePickerDialog = new TimePickerDialog(Usuario_Recorrido.this, R.style.DialogoReloj, onTimeSetListener, i_hora, i_minutos, true);

        timePickerDialog.setTitle(" Formato: \n 24 horas");
        timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE,"Aceptar",timePickerDialog);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            timePickerDialog.setIcon(R.mipmap.ic_launcher_foreground);
        else
            timePickerDialog.setIcon(R.mipmap.ic_launcher_foreground);
        if(!((Usuario_Recorrido.this).isFinishing()))
            timePickerDialog.show();
    }
    private void f_AgregarTexto(String nombreArchivo, String n_Atributo, String v_Atributo){
        File archivo = new File( System.getProperty("java.io.tmpdir")+"/"+nombreArchivo+".json");
        try {
            FileOutputStream foutputstream = new FileOutputStream(archivo,true);
            OutputStreamWriter escritor = new OutputStreamWriter(foutputstream);
//            if(n_Atributo.equals("}"))
//                escritor.append("\n\"R\":\"F\"  \n"+ n_Atributo);
//            else
                escritor.append(/*"\n\""+ */ "\n"+ n_Atributo + "_" + /*"\":" + "\""+*/ v_Atributo/* + "\","*/);

            escritor.close();
            foutputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void f_Agregar_Color(TextView tv_Punto, int v){
        int color = Color.BLACK;
        if (v > 0)
            color = Color.RED;
        else if (v < 0)
            color = Color.BLUE;
        else
            color = Color.GREEN;
        tv_Punto.setTextColor(color);
    }
    private void f_Asignar_Foto(ImageView iv){
        sImagen = preferencias_Variables.getString("imagen", "");
        if (!sImagen.equals("")){
            File file = new File(caminoCache + sImagen);
            if (file.exists())
                Glide.with(getApplicationContext()).load(caminoCache+sImagen).apply(RequestOptions.centerCropTransform()).apply(RequestOptions.overrideOf(254,254)).apply(RequestOptions.circleCropTransform()).into(iv);
            else
                Glide.with(getApplicationContext()).load(R.drawable.ic_account).apply(RequestOptions.centerCropTransform()).apply(RequestOptions.overrideOf(254,254)).apply(RequestOptions.circleCropTransform()).into(iv);
//            if (!file.exists())
//                f_DescargarImagen(sImagen);
//            else
//                Glide.with(getApplicationContext()).load(caminoCache+sImagen).apply(RequestOptions.centerCropTransform()).apply(RequestOptions.overrideOf(254,254)).apply(RequestOptions.circleCropTransform()).into(iv);
        }
    }
    private void f_Asignar_TextView(String[] checadas, int[] tiempos){
        tvParada1.setText(checadas[0]);
        tvParada2.setText(checadas[1]);
        tvParada3.setText(checadas[2]);
        tvParada4.setText(checadas[3]);
        tvParada5.setText(checadas[4]);
        tvParada6.setText(checadas[5]);
        tvParada7.setText(checadas[6]);
//        tvParada8.setText(checadas[7]);
//        tvParada9.setText(checadas[8]);
//        tvParada10.setText(checadas[9]);
//        tvParada11.setText(checadas[10]);
        tvP1DT.setText(String.valueOf(tiempos[0]));
        tvP2DT.setText(String.valueOf(tiempos[1]));
        tvP3DT.setText(String.valueOf(tiempos[2]));
        tvP4DT.setText(String.valueOf(tiempos[3]));
        tvP5DT.setText(String.valueOf(tiempos[4]));
        tvP6DT.setText(String.valueOf(tiempos[5]));
        tvP7DT.setText(String.valueOf(tiempos[6]));
//        tvP8DT.setText(String.valueOf(tiempos[7]));
//        tvP9DT.setText(String.valueOf(tiempos[8]));
//        tvP10DT.setText(String.valueOf(tiempos[9]));
//        tvP11DT.setText(String.valueOf(tiempos[10]));
        tvPDDT.setText(String.valueOf(tiempos[7]));
        tvP1Difiempo.setText("0");tvP1Difiempo.setTextColor(Color.BLACK);
        tvP2Difiempo.setText("0");tvP2Difiempo.setTextColor(Color.BLACK);
        tvP3Difiempo.setText("0");tvP3Difiempo.setTextColor(Color.BLACK);
        tvP4Difiempo.setText("0");tvP4Difiempo.setTextColor(Color.BLACK);
        tvP5Difiempo.setText("0");tvP5Difiempo.setTextColor(Color.BLACK);
        tvP6Difiempo.setText("0");tvP6Difiempo.setTextColor(Color.BLACK);
        tvP7Difiempo.setText("0");tvP7Difiempo.setTextColor(Color.BLACK);
//        tvP8Difiempo.setText("0");tvP8Difiempo.setTextColor(Color.BLACK);
//        tvP9Difiempo.setText("0");tvP9Difiempo.setTextColor(Color.BLACK);
//        tvP10Difiempo.setText("0");tvP10Difiempo.setTextColor(Color.BLACK);
//        tvP11Difiempo.setText("0");tvP11Difiempo.setTextColor(Color.BLACK);
        tvDifTDestino.setText("0");tvDifTDestino.setTextColor(Color.BLACK);
        editor_Variables.putString("parada1", tvParada1.getText().toString());
        editor_Variables.putString("parada2", tvParada2.getText().toString());
        editor_Variables.putString("parada3", tvParada3.getText().toString());
        editor_Variables.putString("parada4", tvParada4.getText().toString());
        editor_Variables.putString("parada5", tvParada5.getText().toString());
        editor_Variables.putString("parada6", tvParada6.getText().toString());
        editor_Variables.putString("parada7", tvParada7.getText().toString());
//        editor_Variables.putString("parada8", tvParada8.getText().toString());
//        editor_Variables.putString("parada9", tvParada9.getText().toString());
//        editor_Variables.putString("parada10", tvParada10.getText().toString());
//        editor_Variables.putString("parada11", tvParada11.getText().toString());
        editor_Variables.putString("dift1",tvP1DT.getText().toString());
        editor_Variables.putString("dift2",tvP2DT.getText().toString());
        editor_Variables.putString("dift3",tvP3DT.getText().toString());
        editor_Variables.putString("dift4",tvP4DT.getText().toString());
        editor_Variables.putString("dift5",tvP5DT.getText().toString());
        editor_Variables.putString("dift6",tvP6DT.getText().toString());
        editor_Variables.putString("dift7",tvP7DT.getText().toString());
//        editor_Variables.putString("dift8",tvP8DT.getText().toString());
//        editor_Variables.putString("dift9",tvP9DT.getText().toString());
//        editor_Variables.putString("dift10",tvP10DT.getText().toString());
//        editor_Variables.putString("dift11",tvP11DT.getText().toString());
        editor_Variables.putString("diftD",tvPDDT.getText().toString());
        editor_Variables.commit();
    }
    private void f_Botones(){

        cv_CancelarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_CancelarRuta.setVisibility(View.GONE);
                pb_CancelarRuta.setVisibility(View.VISIBLE);
                final boolean esLimpia;
                final String nombreArchivo = preferencias_Variables.getString("nombreArchivo","");
                AlertDialog.Builder alertaCancelar;
                DateFormat formatoCancelar = new SimpleDateFormat("HH:mm:ss");
                hora = formatoCancelar.format(Calendar.getInstance().getTime());

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                    alertaCancelar = new AlertDialog.Builder(Usuario_Recorrido.this, R.style.DialogoEleccion);
                else
                    alertaCancelar = new AlertDialog.Builder(Usuario_Recorrido.this, R.style.DialogoEleccion_Transparente);
                alertaCancelar.setCancelable(true);
                if (tv_CancelarRuta.getText().equals("Cancelar")) {
                    alertaCancelar.setTitle("Cancelar");
                    alertaCancelar.setMessage("¿Desea cancelar la ruta?");
                    esLimpia = false;
                } else {
                    alertaCancelar.setTitle("Limpiar");
                    alertaCancelar.setMessage("¿Desea limpiar la ruta?");
                    esLimpia = true;
                }
                alertaCancelar.setCancelable(false);
                alertaCancelar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopService(intentoServicio);
                        LocalBroadcastManager.getInstance(Usuario_Recorrido.this).unregisterReceiver(recibirCoordenadas);
                        f_DetenerAlarma();
                        f_AgregarTexto(nombreArchivo,"BC", hora);
                        editor_Variables.putString("boton_detener",hora);
                        editor_Variables.putBoolean("servicio_activo", false);
                        editor_Variables.commit();
                        if (!unidad.equals("")) {
                            Map<String, Object> datones_Unidad = new HashMap<>();
                            datones_Unidad.put("Activo", false);
                            datones_Unidad.put("Conductor", "");
                            datones_Unidad.put("ID_Vuelta", "");
                            ff_BaseDatos.collection("unidades").document(unidad).set(datones_Unidad, SetOptions.merge());
                        }

                        if (!tv_PrepararRuta.getText().toString().equals("Iniciar Ruta")){
                            c_Suma++;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new asyt_Suma().execute();
                                }
                            },1000);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    f_SubirArchivoRecorrido();
                                    new asyt_Cancelar(esLimpia).execute();
                                }
                            },2000);
                        }else
                            new asyt_Cancelar(false).execute();
                    }
                });
                alertaCancelar.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        tv_CancelarRuta.setVisibility(View.VISIBLE);
                        pb_CancelarRuta.setVisibility(View.GONE);
                    }
                });
                if(!((Usuario_Recorrido.this).isFinishing())){
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                        alertaCancelar.show();
                    else{
                        Dialog dialogo = alertaCancelar.show();
                        int lineaID = dialogo.getContext().getResources().getIdentifier("android:id/titleDivider",null,null);
                        View linea = dialogo.findViewById(lineaID);
                        Resources recursos = dialogo.getContext().getResources();
                        int color = recursos.getColor(R.color.colorAccent);
                        linea.setBackgroundColor(color);
                    }
                }
            }
        });


        cv_PrepararRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    f_Toast("Acepte los permisos de ubicacion! ", "gps", 1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
                        }
                    }, 1000);
                    return;
                }
                if (manejadorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if ((tv_PrepararRuta.getText().toString().equals("Preparar Ruta")) && spUnidad.getSelectedItem().toString() != "UNIDAD"
                            && spOrigen.getSelectedItem().toString() != "ORIGEN" && spDestino.getSelectedItem().toString() != "DESTINO"
                            && picker_Hora!=0) {
                        tv_PrepararRuta.setVisibility(View.GONE);
                        pb_PrepararRuta.setVisibility(View.VISIBLE);
                        ha_Destino.removeCallbacks(act_Destino);
                        unidad = spUnidad.getSelectedItem().toString();
                        LocalBroadcastManager.getInstance(Usuario_Recorrido.this).unregisterReceiver(recibirCoordenadas);
                        f_DetenerAlarma();
                        boolean servicioActivo = constantes.servicioActivo(getApplicationContext());
                        if (servicioActivo)
                            stopService(intentoServicio);
                        c_CreacionRecorrido = 0;

                        DocumentReference ref_Unidad = ff_BaseDatos.collection("unidades").document(unidad);
                        ref_Unidad.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documento = task.getResult();
                                    if (documento.getBoolean("Activo") == false) {
                                        tv_PrepararRuta.setText("Iniciar Ruta");
                                        cv_CancelarRuta.setEnabled(true);
                                        cv_CancelarRuta.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                        inicioNormal = false;
                                        s_unidad_Activa = "false";
                                        origen = spOrigen.getSelectedItem().toString();
                                        destino = spDestino.getSelectedItem().toString();


                                        if (origen.equals("Trincheras")){
                                            if (destino.equals("Soledad"))
                                                f_Asignar_TextView(Paradas_O_T, Tiempos_OT_DSo);
                                            else
                                                f_Asignar_TextView(Paradas_O_T, Tiempos_OT_DSa);
                                        }else if (origen.equals("Encinos")){
                                            if (destino.equals("Soledad"))
                                                f_Asignar_TextView(Paradas_O_E, Tiempos_OE_DSo);
                                            else
                                                f_Asignar_TextView(Paradas_O_E, Tiempos_OE_DSa);
                                        }else if (destino.equals("Trincheras")){
                                            if (origen.equals("Soledad"))
                                                f_Asignar_TextView(Paradas_D_T, Tiempos_OSo_DT);
                                            else
                                                f_Asignar_TextView(Paradas_D_T, Tiempos_OSa_DT);
                                        }else{
                                            if (origen.equals("Soledad"))
                                                f_Asignar_TextView(Paradas_D_E, Tiempos_OSo_DE);
                                            else
                                                f_Asignar_TextView(Paradas_D_E, Tiempos_OSa_DE);
                                        }

                                        int ano = Calendar.getInstance().get(Calendar.YEAR);
                                        tvDestino.setText(destino);
                                        fecha = formatoFecha_ymd.format(Calendar.getInstance().getTime());
                                        String fechaRecorrido = formatoFecha_md.format(Calendar.getInstance().getTime());
                                        String dia = formatoFecha_d.format(Calendar.getInstance().getTime());
                                        String mes = formatoFecha_m.format(Calendar.getInstance().getTime());
                                        editor_Variables.putString("dia-mes",fechaRecorrido);
                                        editor_Variables.putInt("ano",ano);
                                        editor_Variables.putString("dia",dia);
                                        editor_Variables.putString("mes",mes);
                                        editor_Variables.putString("unidad", unidad);
                                        editor_Variables.putString("paradaDestino", tvDestino.getText().toString());
                                        editor_Variables.putString("origen", origen);
                                        editor_Variables.putString("destino", destino);
                                        editor_Variables.putBoolean("inicioNormal", inicioNormal);
                                        editor_Variables.putString("unidadActiva", s_unidad_Activa);
                                        editor_Variables.putBoolean("bool_btnCancelar", true);
                                        editor_Variables.putString("fecha", fecha);
                                        editor_Variables.putString("hora_inicial", str_HoraI);
                                        editor_Variables.putString("tv_PrepararRuta", tv_PrepararRuta.getText().toString());
                                        editor_Variables.putInt("picker_Hora", picker_Hora);
                                        editor_Variables.putInt("picker_Minutos", picker_Minutos);
                                        editor_Variables.commit();
                                        spOrigen.setEnabled(false);
                                        spDestino.setEnabled(false);
                                        spUnidad.setEnabled(false);
                                        btn_SHora.setEnabled(false);
                                        iv_Flecha.setEnabled(false);
                                        iv_Flecha2.setEnabled(false);
                                        iv_Hora.setEnabled(false);
                                        spUnidad.setAlpha(0.5f);
                                        spOrigen.setAlpha(0.5f);
                                        spDestino.setAlpha(0.5f);
                                        btn_SHora.setAlpha(0.5f);
                                        iv_Flecha.setAlpha(0.5f);
                                        iv_Flecha2.setAlpha(0.5f);
                                        iv_Hora.setAlpha(0.5f);
                                        fondo_iv_flecha.setAlpha(0.5f);
                                        fondo_iv_flecha2.setAlpha(0.5f);
                                        fondo_iv_hora.setAlpha(0.5f);
                                    } else {
                                        s_unidad_Activa = "true";
                                        f_Toast("Unidad en ruta!", "combi", 0);
                                    }
                                }
                            }
                        });
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tv_PrepararRuta.setVisibility(View.VISIBLE);
                                pb_PrepararRuta.setVisibility(View.GONE);
                            }
                        }, 1000);
                    }else if((tv_PrepararRuta.getText().toString().equals("Preparar Ruta"))){
                        f_Toast("Comprueba los campos", "error", 0);
                        tv_PrepararRuta.setText("Preparar Ruta");
                    }

                    if (tv_PrepararRuta.getText().toString().equals("Iniciar Ruta")) {
                        tv_PrepararRuta.setVisibility(View.GONE);
                        pb_PrepararRuta.setVisibility(View.VISIBLE);
                        new asyt_Conexion().execute("null");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(hayInternet){
                                    c_Suma = 0;
                                    cv_PrepararRuta.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
                                    cv_CancelarRuta.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));

                                    Map<String, Object> datones_Registro_TR = new HashMap<>();
                                    datones_Registro_TR.put("f", fecha);
                                    datones_Registro_TR.put("c", usuario_activo);
                                    datones_Registro_TR.put("u", unidad);
                                    datones_Registro_TR.put("lo", origen);
                                    datones_Registro_TR.put("ld", destino);
                                    datones_Registro_TR.put("hi", str_HoraI);
                                    datones_Registro_TR.put("p1", "");
                                    datones_Registro_TR.put("p2", "");
                                    datones_Registro_TR.put("p3", "");
                                    datones_Registro_TR.put("p4", "");
                                    datones_Registro_TR.put("p5", "");
                                    datones_Registro_TR.put("p6", "");
                                    datones_Registro_TR.put("p7", "");
                                    datones_Registro_TR.put("pD", "");
                                    datones_Registro_TR.put("d1", "0");
                                    datones_Registro_TR.put("d2", "0");
                                    datones_Registro_TR.put("d3", "0");
                                    datones_Registro_TR.put("d4", "0");
                                    datones_Registro_TR.put("d5", "0");
                                    datones_Registro_TR.put("d6", "0");
                                    datones_Registro_TR.put("d7", "0");
                                    datones_Registro_TR.put("dD", "0");
                                    ff_BaseDatos.collection("registros_TR").add(datones_Registro_TR).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            if(c_CreacionRecorrido==0)
                                                new asyt_IniciarRecorrido().execute(documentReference);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                        }
                                    });
                                }else{
                                    tv_PrepararRuta.setVisibility(View.VISIBLE);
                                    pb_PrepararRuta.setVisibility(View.GONE);
                                }
                            }
                        }, 3000);
                    }

                } else {
                    f_Toast("Habilite la ubicacion!\n Seleccione modo: \n Alta precisión. ", "gps", 1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i_llamarConfiguracionGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i_llamarConfiguracionGPS);
                        }
                    },2500);
                }
            }
        });

        btn_SHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f_AgregarHora();
            }
        });

        ibtn_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { drawer_Menu.openDrawer(GravityCompat.START); }
        });

        iv_Flecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iv_Flecha.getAlpha()==1.0)
                    spUnidad.performClick();
            }
        });

        iv_Flecha2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iv_Flecha2.getAlpha()==1.0){
                    if (spOrigen.getSelectedItem().equals("ORIGEN"))
                        spOrigen.performClick();
                    else
                        spDestino.performClick();
                }
            }
        });

        iv_Hora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(iv_Hora.getAlpha()==1.0)
                    f_AgregarHora();
            }
        });

    }
    private void f_Configuracion_GPS(){
        Intent i_llamarConfiguracionGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i_llamarConfiguracionGPS);
    }
    private String f_ComprobarVacio(String valor){
        if (valor == "" || valor.equals("") || valor == null)
            return null;

        return "si";
    }

//    private File f_CrearTempFile(String prefix, String suffix) {
//        String tempDir = System.getProperty("java.io.tmpdir");
//        String fileName = (prefix != null ? prefix : "") + (suffix != null ? suffix : "");
//        return new File(tempDir, fileName);
//    }
//    private void f_Datones_Usuario() {
////        final Dialog dialogo = new Dialog(Usuario_Recorrido.this);
////        dialogo.setContentView(R.layout.ly_informacion_usuario);
////        DisplayMetrics metricas = new DisplayMetrics();
////        getWindowManager().getDefaultDisplay().getMetrics(metricas);
////        int ancho = metricas.widthPixels;
////        int largo = metricas.heightPixels;
////        dialogo.getWindow().setLayout((int)(ancho*0.95),(int)(largo*0.95));
////        dialogo.setTitle("Software de terceros. ");
////
////        ImageView iv_foto_usuario = dialogo.findViewById(R.id.iv_usuario_foto);
////        ImageButton ibtn_atras = dialogo.findViewById(R.id.ib_usuario_atras);
////        TextView tv_nombre = dialogo.findViewById(R.id.tv_usuario_nombre);
////        TextView tv_correo = dialogo.findViewById(R.id.tv_usuario_correo);
////
////        tv_nombre.setText(usuario_activo);
////        tv_correo.setText(correo_activo+"@gmail.com");
////
//////        if (tv_nombre.getText().equals("Fluke Ario"))
//////            f_Prueba();
////
////
//////        Glide.with(Usuario_Recorrido.this).load(caminoCache + sImagen).apply(RequestOptions.centerCropTransform()).apply(RequestOptions.overrideOf(254, 254)).apply(RequestOptions.circleCropTransform()).into(iv_foto_usuario);
////
////        ibtn_atras.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                dialogo.dismiss();
////            }
////        });
////
////        if(!((Usuario_Recorrido.this).isFinishing()))
////            dialogo.show();
//    }

    private void f_DetenerAlarma() {
        alarmaActiva = f_PiActivo(26);
        if (alarmaActiva) {
            iAlarma = new Intent(Usuario_Recorrido.this, AlarmaNotificacion.class);
            piAlarma = PendingIntent.getBroadcast(this, 26, iAlarma, PendingIntent.FLAG_CANCEL_CURRENT);
            manejadorAlarma.cancel(piAlarma);
            piAlarma.cancel();
        }
        boolean notificacionActiva = f_PiActivo(93);
        if (notificacionActiva){
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
            Intent i = getPackageManager()
                    .getLaunchIntentForPackage(getPackageName())
                    .setPackage(null)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            PendingIntent pi =PendingIntent.getActivity(getApplicationContext(),93,i,0);
            pi.cancel();
        }
    }
    private void f_Dialogo(ProgressDialog dialogo, String str_titulo, String str_cuerpo){
        dialogo.setTitle(str_titulo);
        dialogo.setMessage(str_cuerpo);
        if(!((Usuario_Recorrido.this).isFinishing()))
            dialogo.show();
        int lineaID = dialogo.getContext().getResources().getIdentifier("android:id/titleDivider",null,null);
        int tituloID = dialogo.getContext().getResources().getIdentifier("android:id/alertTitle",null,null);
        View linea = dialogo.findViewById(lineaID);
        View titulo = dialogo.findViewById(tituloID);
        Resources recursos = dialogo.getContext().getResources();
        int color = recursos.getColor(R.color.colorAccent);
        linea.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            titulo.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dialogo.setProgressDrawable(recursos.getDrawable(R.drawable.barra_progreso));
    }
    private void f_GenerarArchivoRecorrido(String nombreArchivo, String n_Atributo ,String v_Atributo){
        File recorrido = new File(System.getProperty("java.io.tmpdir"),nombreArchivo + ".json");
        FileWriter escritor = null;
        try {
            escritor = new FileWriter(recorrido);
//            escritor.append("{\n");
            escritor.append(/*"\"" + */n_Atributo + "_" /*+ "\":"*/);
            escritor.append(/*"\"" +*/ v_Atributo  /*+ "\","*/);
            escritor.flush();
            escritor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void f_IniciarAlarma() {
        alarmaActiva = f_PiActivo(26);
        if(!alarmaActiva){
            iAlarma = new Intent(Usuario_Recorrido.this, AlarmaNotificacion.class);
            iAlarma.putExtra("ENVIAR_ORIGEN", origen);
            iAlarma.putExtra("ENVIAR_DESTINO", destino);
            iAlarma.putExtra("ENVIAR_LLAVE", llave_TR);
            iAlarma.putExtra("ENVIAR_HORAI", picker_Hora);
            iAlarma.putExtra("ENVIAR_MINUTOSI", picker_Minutos);
            piAlarma = PendingIntent.getBroadcast(Usuario_Recorrido.this, 26, iAlarma, PendingIntent.FLAG_CANCEL_CURRENT);
            manejadorAlarma.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), 300000, piAlarma);
        }
    }
    private void f_IniciarServicio(){
        iServicio = new Intent(Usuario_Recorrido.this, ServicioUbicacion.class);
        iServicio.putExtra("ENVIAR_ORIGEN", origen);
        iServicio.putExtra("ENVIAR_DESTINO", destino);
        iServicio.putExtra("ENVIAR_LLAVE", llave_TR);
        iServicio.putExtra("ENVIAR_HORAI", picker_Hora);
        iServicio.putExtra("ENVIAR_MINUTOSI", picker_Minutos);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
            this.startForegroundService(iServicio);
        else
            this.startService(iServicio);

        f_IniciarAlarma();
        LocalBroadcastManager.getInstance(Usuario_Recorrido.this).registerReceiver(recibirCoordenadas,
                new IntentFilter(ServicioUbicacion.ACCION_COORDENADAS));

        editor_Variables.putBoolean("servicio_activo",true);
        editor_Variables.commit();
    }
    private void f_Iniciar_Recibidor_Apagado(String nombre){
        IntentFilter filtro = new IntentFilter(Intent.ACTION_SHUTDOWN);

        recibir_Apagado = new Apagado(nombre);
        registerReceiver(recibir_Apagado, filtro);
    }
    private void f_Iniciar_Recibidor_CambioHora(String nombre){
        IntentFilter filtro = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        recibidor_CambioHora = new Cambio_Hora(nombre);
        registerReceiver(recibidor_CambioHora, filtro);
    }

    private void f_Librerias(){
        final Dialog dialogo = new Dialog(Usuario_Recorrido.this);
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogo.setContentView(R.layout.ly_librerias);
        DisplayMetrics metricas = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metricas);
        int ancho = metricas.widthPixels;
        int largo = metricas.heightPixels;
        dialogo.getWindow().setLayout((int)(ancho*0.95),(int)(largo*0.95));
        dialogo.setTitle("Software de terceros. ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TextView tv_google = dialogo.findViewById(R.id.tv_google_play_d);
            TextView tv_okhttp = dialogo.findViewById(R.id.tv_ok_http_d);
            TextView tv_glide = dialogo.findViewById(R.id.tv_glide_d);
            tv_google.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            tv_okhttp.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            tv_glide.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
        ImageButton ibtn_atras = dialogo.findViewById(R.id.ib_librerias_atras);

        ibtn_atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogo.dismiss();
            }
        });

        if(!((Usuario_Recorrido.this).isFinishing()))
            dialogo.show();

    }
    private void f_Noticias(){
        final Dialog dialogo = new Dialog(Usuario_Recorrido.this);
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogo.setContentView(R.layout.ly_noticias);
        DisplayMetrics metricas = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metricas);
        int ancho = metricas.widthPixels;
        int largo = metricas.heightPixels;
        dialogo.getWindow().setLayout((int)(ancho*0.95),(int)(largo*0.95));
        dialogo.setTitle("Noticias ");

        ImageButton ibtn_atras = dialogo.findViewById(R.id.ib_noticias_atras);

        ibtn_atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        final RecyclerView rv_ListaNoticias = dialogo.findViewById(R.id.rv_ListaNoticias);
        FirebaseFirestore bd_Noticias = FirebaseFirestore.getInstance();
        final List<ObjetoNoticia> objetos = new ArrayList<>();
        final AdaptadorNoticias[] adaptador = new AdaptadorNoticias[1];

        rv_ListaNoticias.setLayoutManager(new LinearLayoutManager(dialogo.getContext()));
        objetos.clear();

        bd_Noticias.collection("avisos").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documento : task.getResult()){
                        ObjetoNoticia obj = new ObjetoNoticia(documento.get("Titulo").toString(),documento.get("Cuerpo").toString(),documento.get("Fecha").toString());
                        objetos.add(obj);
                    }
                    f_Ordenar_Noticias(objetos);
                    adaptador[0] = new AdaptadorNoticias(rv_ListaNoticias,Usuario_Recorrido.this ,objetos);
                    rv_ListaNoticias.setAdapter(adaptador[0]);
                    adaptador[0].notifyDataSetChanged();
                }
            }
        });

        if(!((Usuario_Recorrido.this).isFinishing()))
            dialogo.show();

    }
    private void f_Ordenar_Noticias(List<ObjetoNoticia> objetos){
        Collections.sort(objetos, new Comparator<ObjetoNoticia>() {
            @Override
            public int compare(ObjetoNoticia v1, ObjetoNoticia v2) {
                return v2.getFecha().compareTo(v1.getFecha());
            }
        });
    }
    private boolean f_PiActivo(int id) {
        boolean b = false;
        Intent i;
        if (id == 26) {
            i = new Intent(this, AlarmaNotificacion.class);
            b = PendingIntent.getBroadcast(this, id, i, PendingIntent.FLAG_NO_CREATE) != null;
        } else if (id == 93) {
            i = getPackageManager()
                    .getLaunchIntentForPackage(getPackageName())
                    .setPackage(null)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            b = PendingIntent.getActivity(getApplicationContext(), 93, i, PendingIntent.FLAG_NO_CREATE) != null;
        }
        return b;
    }
    private void f_Politicas_Privacidad(String actividad){
        final Dialog dialogo = new Dialog(Usuario_Recorrido.this);
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogo.setContentView(R.layout.ly_politicas_privacidad);
        DisplayMetrics metricas = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metricas);
        int ancho = metricas.widthPixels;
        int largo = metricas.heightPixels;
        dialogo.getWindow().setLayout((int)(ancho*0.95),(int)(largo*0.95));
        dialogo.setTitle("Políticas de privacidad ");
        TextView tv_Texto = dialogo.findViewById(R.id.tv_PP_Cuerpo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tv_Texto.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
        Button btn_PP_Si = dialogo.findViewById(R.id.btn_PP_Si);
        Button btn_PP_No = dialogo.findViewById(R.id.btn_PP_No);
        ImageButton ibtn_atras = dialogo.findViewById(R.id.ib_pp_atras);

        if (actividad.equals("Usuario_Recorrido")){
            btn_PP_Si.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogo.dismiss();
                    Map<String, Object> datones_Usuario = new HashMap<>();
                    datones_Usuario.put("PP", true);
                    ff_BaseDatos.collection("usuarios").document(uid).set(datones_Usuario, SetOptions.merge());
                    editor_Variables.putBoolean("pp",true);
                    editor_Variables.commit();
                }
            });

            btn_PP_No.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogo.dismiss();
                    f_Toast("¡Se cerrará sesión en breve!","error",0);
                    editor_Variables.putBoolean("pp",false);
                    editor_Variables.commit();
                    f_ReiniciarVariables(true,false, false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new asyt_Salir().execute();
                        }
                    },3000);
                }
            });
            ibtn_atras.setVisibility(View.INVISIBLE);
        }else{
            ibtn_atras.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogo.dismiss();
                }
            });
                btn_PP_No.setVisibility(View.INVISIBLE);
                btn_PP_Si.setVisibility(View.INVISIBLE);
        }

        tv_Texto.setText("Política de privacidad\n" +
                "0Miedo construyó la aplicación CombiZona: Oro Verde como una aplicación comercial. Este servicio es proporcionado por 0Miedo y está diseñado para ser utilizado tal como está.\n" +
                "\n" +
                "Esta página se utiliza para informar a los visitantes sobre nuestras políticas con la recopilación, el uso y la divulgación de Información personal si alguien decide utilizar nuestro Servicio.\n" +
                "\n" +
                "Si elige usar nuestro Servicio, acepta la recopilación y el uso de la información en relación con esta política. La información personal que recopilamos se usa para proporcionar y mejorar el servicio. No usaremos ni compartiremos su información con nadie, excepto según se describe en esta Política de privacidad.\n" +
                "\n" +
                "Los términos utilizados en esta Política de privacidad tienen los mismos significados que en nuestros Términos y condiciones, que se encuentran disponibles en CombiZona: Oro Verde, a menos que se defina lo contrario en esta Política de privacidad.\n" +
                "\n" +
                "Recopilación y uso de información\n" +
                "\n" +
                "Para una mejor experiencia, mientras usamos nuestro Servicio, es posible que le solicitemos que nos brinde cierta información de identificación personal, que incluye, entre otros, el nombre de usuario, la contraseña, la ubicación, la unidad de transporte público, la base, el destino, la información del sistema y el almacenamiento local de datos. Nosotros retendremos la información que solicitamos y la utilizaremos como se describe en esta política de privacidad.\n" +
                "\n" +
                "La aplicación utiliza servicios de terceros que pueden recopilar información utilizada para identificarlo.\n" +
                "\n" +
                "Enlace a la política de privacidad de proveedores de servicios de terceros utilizados por la aplicación\n" +
                "\n" +
                "Servicios de Google Play\n" +
                "Firebase Analytics\n" +
                "Crashlytics\n" +
                "\n" +
                "Dato de registro\n" +
                "\n" +
                "Queremos informarle que cada vez que utiliza nuestro Servicio, en caso de error en la aplicación, recopilamos datos e información (a través de productos de terceros) en su teléfono, llamados Datos de registro. Este registro de datos puede incluir información como la dirección del protocolo de Internet (\"IP\"), el nombre del dispositivo, la versión del sistema operativo, la configuración de la aplicación al utilizar nuestro servicio, la hora y la fecha de uso del servicio y otras estadísticas .\n" +
                "\n" +
                "Cookies\n" +
                "\n" +
                "Las cookies son archivos con una pequeña cantidad de datos que se utilizan comúnmente como identificadores únicos anónimos. Estos se envían a su navegador desde los sitios web que visita y se almacenan en la memoria interna de su dispositivo.\n" +
                "\n" +
                "Este Servicio no usa estas \"cookies\" explícitamente. Sin embargo, la aplicación puede usar código de terceros y bibliotecas que usan \"cookies\" para recopilar información y mejorar sus servicios. Usted tiene la opción de aceptar o rechazar estas cookies y saber cuándo se está enviando una cookie a su dispositivo. Si elige rechazar nuestras cookies, es posible que no pueda usar algunas partes de este Servicio.\n" +
                "\n" +
                "Proveedores de servicio\n" +
                "\n" +
                "Podemos emplear compañías e individuos de terceros debido a las siguientes razones:\n" +
                "\n" +
                "Para facilitar nuestro servicio;\n" +
                "Para proporcionar el Servicio en nuestro nombre;\n" +
                "Para realizar servicios relacionados con el servicio; o\n" +
                "Para ayudarnos a analizar cómo se utiliza nuestro Servicio.\n" +
                "Queremos informar a los usuarios de este Servicio que estos terceros tienen acceso a su Información personal. El motivo es realizar las tareas que se les asignaron en nuestro nombre. Sin embargo, están obligados a no divulgar ni utilizar la información para ningún otro fin.\n" +
                "\n" +
                "Seguridad\n" +
                "\n" +
                "Valoramos su confianza al proporcionarnos su Información personal, por lo tanto, nos esforzamos por utilizar medios comercialmente aceptables para protegerla. Pero recuerde que ningún método de transmisión a través de Internet o método de almacenamiento electrónico es 100% seguro y confiable, y no podemos garantizar su seguridad absoluta.\n" +
                "\n" +
                "Enlaces a otros sitios\n" +
                "\n" +
                "Este Servicio puede contener enlaces a otros sitios. Si hace clic en un enlace de un tercero, se lo dirigirá a ese sitio. Tenga en cuenta que estos sitios externos no son operados por nosotros. Por lo tanto, le recomendamos encarecidamente que revise la Política de privacidad de estos sitios web. No tenemos control ni asumimos ninguna responsabilidad por el contenido, las políticas de privacidad o las prácticas de sitios o servicios de terceros.\n" +
                "\n" +
                "Privacidad de los niños\n" +
                "\n" +
                "Estos Servicios no se dirigen a personas menores de 13 años. No recopilamos, a sabiendas, información de identificación personal de niños menores de 13 años. En el caso de que descubramos que un niño menor de 13 años nos ha proporcionado información personal, lo eliminamos de inmediato de nuestros servidores. Si usted es un padre o tutor y sabe que su hijo nos ha proporcionado información personal, comuníquese con nosotros para que podamos hacer las acciones necesarias.\n" +
                "\n" +
                "Cambios a esta política de privacidad\n" +
                "\n" +
                "Es posible que actualicemos nuestra Política de privacidad de vez en cuando. Por lo tanto, se recomienda revisar esta página periódicamente para cualquier cambio. Estos cambios entran en vigencia inmediatamente después de que se publiquen en esta página.\n" +
                "\n" +
                "Contáctenos\n" +
                "\n" +
                "Si tiene alguna pregunta o sugerencia sobre nuestra Política de privacidad, no dude en contactarnos. \n" +
                "contacto@0miedo.com.mx");

        if(!((Usuario_Recorrido.this).isFinishing()))
            dialogo.show();

    }
    private void f_PrimeraVez() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        int versionAPI = Build.VERSION.SDK_INT;
        if (!politicasPrivacidad)
            f_Politicas_Privacidad("Usuario_Recorrido");
        Map<String, Object> datones_Usuario = new HashMap<>();
        datones_Usuario.put("Activo", true);
        datones_Usuario.put("VersionAPK", preferencias_Variables.getString("versionAPK",""));
        datones_Usuario.put("VersionAPI", versionAPI);
        datones_Usuario.put("Modelo_Celular", brand + " - " + model);
        ff_BaseDatos.collection("usuarios").document(uid).set(datones_Usuario, SetOptions.merge());
//        try {
//            String dos = caminoCache + sImagen;
//            File file = new File(dos);
//            if (!file.exists())
//                f_DescargarImagen(sImagen);
//        } catch (Exception e) { }
        primeraVez = false;
        editor_Variables.putBoolean("primeraVez",false);
        editor_Variables.commit();
    }
    private void f_Refrescar_Datones(){
        AlertDialog.Builder alertaRefrescar;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            alertaRefrescar = new AlertDialog.Builder(Usuario_Recorrido.this, R.style.DialogoEleccion);
        else
            alertaRefrescar = new AlertDialog.Builder(Usuario_Recorrido.this, R.style.DialogoEleccion_Transparente);
        alertaRefrescar.setCancelable(true);
        alertaRefrescar.setTitle("Refrescar");
        alertaRefrescar.setMessage("¿Desea refrescar los datos?");
        alertaRefrescar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ha_Destino.removeCallbacks(act_Destino);
                f_ReiniciarVariables(false,false,true);
            }
        });
        alertaRefrescar.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        if(!((Usuario_Recorrido.this).isFinishing())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                alertaRefrescar.show();
            else {
                Dialog dialogo = alertaRefrescar.show();
                int lineaID = dialogo.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
                View linea = dialogo.findViewById(lineaID);
                Resources recursos = dialogo.getContext().getResources();
                int color = recursos.getColor(R.color.colorAccent);
                linea.setBackgroundColor(color);
            }
        }
    }
    private void f_ReiniciarVariables(boolean salir,final boolean esLimpia, boolean refrescar){
        inicioNormal = true;
        if (esLimpia){
            origen = destino;
            switch (destino){
                case "Soledad":
                    spOrigen.setSelection(1);
                    spDestino.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_diseno_spinner, Destino2));
                    break;
                case "Satélite":
                    spOrigen.setSelection(2);
                    spDestino.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_diseno_spinner, Destino2));
                    break;
                case "Trincheras":
                    spOrigen.setSelection(3);
                    spDestino.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_diseno_spinner, Destino));
                    break;
                case "Encinos":
                    spOrigen.setSelection(4);
                    spDestino.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_diseno_spinner, Destino));
                    break;
                default:
                    spOrigen.setSelection(0);
                    break;
            }
        }else if(salir || refrescar){
            if (!unidad.equals("")) {
                Map<String, Object> datones_Unidad = new HashMap<>();
                datones_Unidad.put("Activo", false);
                datones_Unidad.put("Conductor", "");
                datones_Unidad.put("ID_Vuelta", "");
                ff_BaseDatos.collection("unidades").document(unidad).set(datones_Unidad, SetOptions.merge());
            }
            final String nombreArchivo = preferencias_Variables.getString("nombreArchivo","");
            DateFormat formatoCancelar = new SimpleDateFormat("HH:mm:ss");
            hora = formatoCancelar.format(Calendar.getInstance().getTime());
            f_AgregarTexto(nombreArchivo,"BC", hora);
            editor_Variables.putString("boton_detener",hora);
            editor_Variables.putBoolean("servicio_activo", false);
            editor_Variables.commit();
            f_DetenerAlarma();
            boolean servicioActivo = constantes.servicioActivo(getApplicationContext());
//            Log.e(TAG,servicioActivo+" //");
            if (servicioActivo){
                stopService(intentoServicio);
                c_Suma++;
//                Log.e(TAG,"SUMA: " + c_Suma);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new asyt_Suma().execute();
                        f_SubirArchivoRecorrido();
                    }
                },1000);
            }
            if(refrescar){
                spOrigen.setSelection(0);
                spUnidad.setSelection(0);
                btn_SHora.setText("HORA");
                ha_Destino.post(act_Destino);
            }else if (salir){
                spOrigen.setSelection(0);
                spUnidad.setSelection(0);
                ha_Destino.removeCallbacks(act_Destino);
                Map<String, Object> datones_Usuario = new HashMap<>();
                datones_Usuario.put("Activo", false);
                ff_BaseDatos.collection("usuarios").document(uid).set(datones_Usuario, SetOptions.merge());
                primeraVez = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseAuth.getInstance().signOut();
                        Intent i_Vista_Usuario = new Intent(Usuario_Recorrido.this, Inicio_Sesion.class);
                        startActivity(i_Vista_Usuario);
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
                            overridePendingTransition(R.anim.anim_mover_arriba,R.anim.anim_mover_abajo);
                    }
                },4000);
            }
//            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!esLimpia)
                    origen = "ORIGEN";

                destino = "DESTINO";
                s_unidad_Activa = "";
                anio = "";
                fecha = "";
                str_HoraI = "00:00";
                picker_Hora = 0;
                picker_Minutos = 0;
                c_Quemados = 0;
                c_CreacionRecorrido = 0;

                tv_PrepararRuta.setText("Preparar Ruta");
                cv_PrepararRuta.setEnabled(true);
                tv_CancelarRuta.setText("Cancelar");
                cv_CancelarRuta.setEnabled(false);
                btn_SHora.setText("Hora");
                btn_SHora.setEnabled(true);

                cv_PrepararRuta.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                cv_CancelarRuta.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));

                spDestino.setSelection(0);
                tvParada1.setText("");
                tvParada2.setText("");
                tvParada3.setText("");
                tvParada4.setText("");
                tvParada5.setText("");
                tvParada6.setText("");
                tvParada7.setText("");
//                tvParada8.setText("");
//                tvParada9.setText("");
//                tvParada10.setText("");
//                tvParada11.setText("");
                tvDestino.setText("");
                tvP1Tiempo.setText("");
                tvP1Difiempo.setText("");
                tvP2Tiempo.setText("");
                tvP2Difiempo.setText("");
                tvP3Tiempo.setText("");
                tvP3Difiempo.setText("");
                tvP4Tiempo.setText("");
                tvP4Difiempo.setText("");
                tvP5Tiempo.setText("");
                tvP5Difiempo.setText("");
                tvP6Tiempo.setText("");
                tvP6Difiempo.setText("");
                tvP7Tiempo.setText("");
                tvP7Difiempo.setText("");
//                tvP8Tiempo.setText("");
//                tvP8Difiempo.setText("");
//                tvP9Tiempo.setText("");
//                tvP9Difiempo.setText("");
//                tvP10Tiempo.setText("");
//                tvP10Difiempo.setText("");
//                tvP11Tiempo.setText("");
//                tvP11Difiempo.setText("");
                tvTiempoDestino.setText("");
                tvDifTDestino.setText("");
                tvP1DT.setText("");
                tvP2DT.setText("");
                tvP3DT.setText("");
                tvP4DT.setText("");
                tvP5DT.setText("");
                tvP6DT.setText("");
                tvP7DT.setText("");
//                tvP8DT.setText("");
//                tvP9DT.setText("");
//                tvP10DT.setText("");
//                tvP11DT.setText("");
                tvPDDT.setText("");
                spUnidad.setEnabled(true);
                spOrigen.setEnabled(true);
                spDestino.setEnabled(true);
                iv_Flecha.setEnabled(true);
                iv_Flecha2.setEnabled(true);
                iv_Hora.setEnabled(true);
                spUnidad.setAlpha(1f);
                spOrigen.setAlpha(1f);
                spDestino.setAlpha(1f);
                btn_SHora.setAlpha(1f);
                iv_Flecha.setAlpha(1f);
                iv_Flecha2.setAlpha(1f);
                iv_Hora.setAlpha(1f);
                fondo_iv_flecha.setAlpha(1f);
                fondo_iv_flecha2.setAlpha(1f);
                fondo_iv_hora.setAlpha(1f);

                tv_UnidadSiguiendo.setText("0");
                tv_TiempoUnidadSiguiendo.setText("0");

                editor_Variables.putString("nombreArchivo","");
                editor_Variables.putString("btn_Cancelar", "Cancelar");
                editor_Variables.putBoolean("bool_btnCancelar", false);
                editor_Variables.putString("Punto_1", "");
                editor_Variables.putString("Punto_2", "");
                editor_Variables.putString("Punto_3", "");
                editor_Variables.putString("Punto_4", "");
                editor_Variables.putString("Punto_5", "");
                editor_Variables.putString("Punto_6", "");
                editor_Variables.putString("Punto_7", "");
                editor_Variables.putString("Punto_8", "");
                editor_Variables.putString("Punto_9", "");
                editor_Variables.putString("Punto_10", "");
                editor_Variables.putString("Punto_11", "");
                editor_Variables.putString("Punto_Destino", "");
                editor_Variables.putString("Punto_1_Dif", "0");
                editor_Variables.putString("Punto_2_Dif", "0");
                editor_Variables.putString("Punto_3_Dif", "0");
                editor_Variables.putString("Punto_4_Dif", "0");
                editor_Variables.putString("Punto_5_Dif", "0");
                editor_Variables.putString("Punto_6_Dif", "0");
                editor_Variables.putString("Punto_7_Dif", "0");
                editor_Variables.putString("Punto_8_Dif", "0");
                editor_Variables.putString("Punto_9_Dif", "0");
                editor_Variables.putString("Punto_10_Dif", "0");
                editor_Variables.putString("Punto_11_Dif", "0");
                editor_Variables.putString("Punto_Destino_Dif", "0");
                editor_Variables.putString("origen", origen);
                editor_Variables.putString("destino", destino);
                editor_Variables.putString("llave_TR", llave_TR);
                editor_Variables.putString("unidad", unidad);
                editor_Variables.putBoolean("inicioNormal", inicioNormal);
                editor_Variables.putBoolean("primeraVez",primeraVez);
                editor_Variables.putString("tv_PrepararRuta", tv_PrepararRuta.getText().toString());
                editor_Variables.putString("anio", anio);
                editor_Variables.putString("fecha", fecha);
                editor_Variables.putString("hora_inicial", str_HoraI);
                editor_Variables.putInt("picker_Hora", picker_Hora);
                editor_Variables.putInt("picker_Minutos", picker_Minutos);
                editor_Variables.putString("servicio_iniciado", "");
                editor_Variables.putString("servicio_detenido", "");
                editor_Variables.putString("tv_UnidadSiguiendo","0");
                editor_Variables.putString("tv_TiempoUnidadSiguiendo","0");
                editor_Variables.putBoolean("datones",false);
                editor_Variables.commit();
                tv_CancelarRuta.setVisibility(View.VISIBLE);
                pb_CancelarRuta.setVisibility(View.GONE);
            }
        },3000);

    }
    private void f_Salir(){
        AlertDialog.Builder alertaSalir;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            alertaSalir = new AlertDialog.Builder(Usuario_Recorrido.this, R.style.DialogoEleccion);
        else
            alertaSalir = new AlertDialog.Builder(Usuario_Recorrido.this, R.style.DialogoEleccion_Transparente);
        alertaSalir.setCancelable(true);
        alertaSalir.setTitle("Salir");
        alertaSalir.setMessage("¿Desea cerrar sesión?");
        alertaSalir.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ha_Destino.removeCallbacks(act_Destino);
                new asyt_Salir().execute();
            }
        });
        alertaSalir.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        if(!((Usuario_Recorrido.this).isFinishing())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                alertaSalir.show();
            else {
                Dialog dialogo = alertaSalir.show();
                int lineaID = dialogo.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
                View linea = dialogo.findViewById(lineaID);
                Resources recursos = dialogo.getContext().getResources();
                int color = recursos.getColor(R.color.colorAccent);
                linea.setBackgroundColor(color);
            }
        }
    }
    private void f_SubirArchivoRecorrido( ){
        int ano = preferencias_Variables.getInt("ano", 2019);
        int m = 0;
        String dia = preferencias_Variables.getString("dia", "1");
        String mes = preferencias_Variables.getString("mes", "1");
        final String nombreArchivo = preferencias_Variables.getString("nombreArchivo","");
        String urlArchivo = preferencias_Variables.getString("nombre","");
        final File archivo = new File(System.getProperty("java.io.tmpdir")+"/"+nombreArchivo+".json");

        m = Integer.parseInt(mes);

        JSONObject json = new JSONObject();
        try {
            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;
            while((linea = br.readLine())!=null){
//                Log.e(TAG,"Linea: " + linea);

                String llave = linea.split("_")[0];
                String valor = "";
                try{
                     valor = linea.split("_")[1];
                }catch (Exception e){}

//                    Log.e("TAG",llave + " - " + valor);
//                    Log.e("Valor",valor);
                try{
//                        Log.e("Tiene LLave",json.has(llave) + "/" + preferencias_Variables.getString(llave,"-"));
                    if (json.has(llave)){
//                        if (llave == "CH" || llave.equals("CH")){
//                            Log.e("CHINGUEN A SU MADRE","PERROS");
                        String anterior = preferencias_Variables.getString(llave,"anterior");
                        String nuevo = valor;
//                        Log.e("Anterior",anterior);
//                        Log.e("Nuevo",nuevo);
//                        Log.e("JSON",llave + ":" + anterior + "," + nuevo );
                        json.put(llave,anterior + "," + nuevo);
                        editor_Variables.putString(llave,anterior + "," + nuevo);
                    }else{
                        json.put(llave,valor);
                        editor_Variables.putString(llave,valor);
                    }
                    editor_Variables.commit();

                }catch (Exception e){}

            }
            editor_Variables.putString("R","");
            editor_Variables.putString("BI","");
            editor_Variables.putString("BC","");
            editor_Variables.putString("SI","");
            editor_Variables.putString("SD","");
            editor_Variables.putString("MABon","");
            editor_Variables.putString("MABoff","");
            editor_Variables.putString("GPSon","");
            editor_Variables.putString("GPSoff","");
            editor_Variables.putString("GPSfix","");
            editor_Variables.putString("GPSab","");
            editor_Variables.putString("GPSap","");
            editor_Variables.putString("CH","");
            editor_Variables.putString("Toff","");
            editor_Variables.putString("P1","");
            editor_Variables.putString("P2","");
            editor_Variables.putString("P3","");
            editor_Variables.putString("P4","");
            editor_Variables.putString("P5","");
            editor_Variables.putString("P6","");
            editor_Variables.putString("P7","");
            editor_Variables.putString("P8","");
            editor_Variables.putString("P9","");
            editor_Variables.putString("P10","");
            editor_Variables.putString("P11","");
            editor_Variables.putString("PD","");
            editor_Variables.commit();
//            Log.e(TAG,json.toString());

            PrintWriter pw = new PrintWriter(System.getProperty("java.io.tmpdir")+"/"+nombreArchivo+".json");
            pw.append(json.toString());
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageMetadata metadatos = new StorageMetadata.Builder().setContentType("application/json").build();
        storageRef.child("/Usuarios/Recorridos/GPS/"+urlArchivo+"/"+ ano +"/"+ meses[m-1] + "/" + dia +"/"+archivo.getName()).putFile(Uri.fromFile(archivo),metadatos).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            File a_eliminar = new File(System.getProperty("java.io.tmpdir")+"/"+nombreArchivo+".json");
            if (a_eliminar.exists())
                a_eliminar.delete();
        }}).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void f_Subir_Incidencias(){
        String fechaRecorrido = preferencias_Variables.getString("dia-mes", "01-01");
        int ano = preferencias_Variables.getInt("ano", 2019);

        FirebaseFirestore fbfs = FirebaseFirestore.getInstance();
        final CollectionReference cr;
        cr = fbfs.collection("incidencias").document(ano+"").collection(fechaRecorrido);

        if(usuario_activo==null)
            return;

        DocumentReference bd_Incidencia = cr.document(usuario_activo);
        final int[] c = {0};
        bd_Incidencia.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    c[0]++;
                    try {
                        DocumentSnapshot documento = task.getResult();
                        int t = Integer.parseInt(documento.get("total").toString());
                        t += 1;
                        Map<String, Object> daton_Suma = new HashMap<>();
                        daton_Suma.put("total", t);
                        cr.document(usuario_activo).set(daton_Suma);
                    }catch (Exception e){}
                }
            }
        });
    }
    private void f_Tiempo_Quemados(final int suma4){
        String fechaRecorrido = preferencias_Variables.getString("dia-mes", "01-01");
        int ano = preferencias_Variables.getInt("ano", 2019);
        String hi = preferencias_Variables.getString("hora_inicial", "00:00");
        String p2;
        int h = 0;
        if (!hi.equals(null)){
            p2 = hi.substring(0,2);
            h = Integer.parseInt(p2);
        }
        FirebaseFirestore fbfs = FirebaseFirestore.getInstance();
        final CollectionReference cr;
        if (h<13)
            cr = fbfs.collection("quemados").document(ano+"").collection(fechaRecorrido);
        else
            cr = fbfs.collection("quemados_tarde").document(ano+"").collection(fechaRecorrido);


        if(usuario_activo==null)
            return;

        /// SI VACIO ES MAYOR O IGUAL A 3 , SUMAR EN COLLECCIÓN
        /*
        *
        *
        *
        *
        * */


        DocumentReference bd_quemados = cr.document(usuario_activo);
        final int[] c = {0};
        bd_quemados.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    c[0]++;
                    try {
                        DocumentSnapshot documento = task.getResult();
                        int t = Integer.parseInt(documento.get("Suma4").toString());
                        t += suma4;
                        Map<String, Object> daton_Suma = new HashMap<>();
                        daton_Suma.put("Suma4", t);
                        cr.document(usuario_activo).set(daton_Suma);
                    }catch (Exception e){}
                }
            }
        });
    }
    private void f_Toast(String mensaje, String nombreImagen, int duracion) {
        LayoutInflater inflador = getLayoutInflater();
        View layout = inflador.inflate(R.layout.layout_diseno_toast, (ViewGroup) findViewById(R.id.toast_layout));
        ImageView imagen = (ImageView) layout.findViewById(R.id.toast_image);
        switch (nombreImagen) {
            case "error":
                imagen.setImageResource(R.drawable.ic_error);
                break;
            case "gps":
                imagen.setImageResource(R.drawable.ic_gps);
                break;
            case "user":
                imagen.setImageResource(R.drawable.ic_usuario);
                break;
            case "combi":
                imagen.setImageResource(R.drawable.ic_combi);
                break;
            case "activo":
                imagen.setImageResource(R.drawable.ic_activo);
                break;
            case "actualizado":
                imagen.setImageResource(R.drawable.ic_actualizado);
                break;
            case "descargar":
                imagen.setImageResource(R.drawable.ic_descargar);
                break;
            default:
                break;
        }

        TextView texto = (TextView) layout.findViewById(R.id.toast_text);
        texto.setText(mensaje);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        if (duracion == 1)
            toast.setDuration(Toast.LENGTH_LONG);
        else
            toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        if(!((Usuario_Recorrido.this).isFinishing()))
            toast.show();
    }
    private void f_XmlToAndroid(){
        tv_Nombre = findViewById(R.id.tv_Nombre);
        tv_Correo =  findViewById(R.id.tv_Correo);
        iv_Foto = findViewById(R.id.iv_Foto);
        drawer_Menu = findViewById(R.id.drawer_layout);
        ibtn_Menu = findViewById(R.id.ibtn_Menu);

        iv_Flecha =  findViewById(R.id.iv_Flecha);
        iv_Flecha2 =  findViewById(R.id.iv_Flecha2);
        iv_Hora = findViewById(R.id.iv_Hora);
        fondo_iv_flecha = findViewById(R.id.ll_fondo_flecha);
        fondo_iv_flecha2 = findViewById(R.id.ll_fondo_flecha2);
        fondo_iv_hora = findViewById(R.id.ll_fondo_hora);

        cv_PrepararRuta = findViewById(R.id.cv_PrepararRuta);
        tv_PrepararRuta = findViewById(R.id.tv_PrepararRuta);
        pb_PrepararRuta = findViewById(R.id.pb_PrepararRuta);
        cv_CancelarRuta = findViewById(R.id.cv_CancelarRuta);
        tv_CancelarRuta = findViewById(R.id.tv_CancelarRuta);
        pb_CancelarRuta = findViewById(R.id.pb_CancelarRuta);

        spOrigen =  findViewById(R.id.spOrigen);
        spUnidad =  findViewById(R.id.spUnidad);
        spDestino =  findViewById(R.id.spDestino);
        tvParada1 =  findViewById(R.id.tvParada1);
        tvP1Tiempo =  findViewById(R.id.tvP1Tiempo);
        tvP1Difiempo =  findViewById(R.id.tvP1DifTiempo);
        tvP1DT = findViewById(R.id.tvP1DT);
        tvParada2 =  findViewById(R.id.tvParada2);
        tvP2Tiempo =  findViewById(R.id.tvP2Tiempo);
        tvP2Difiempo =  findViewById(R.id.tvP2DifTiempo);
        tvP2DT = findViewById(R.id.tvP2DT);
        tvParada3 =  findViewById(R.id.tvParada3);
        tvP3Tiempo =  findViewById(R.id.tvP3Tiempo);
        tvP3Difiempo =  findViewById(R.id.tvP3DifTiempo);
        tvP3DT = findViewById(R.id.tvP3DT);
        tvParada4 =  findViewById(R.id.tvParada4);
        tvP4Tiempo =  findViewById(R.id.tvP4Tiempo);
        tvP4Difiempo =  findViewById(R.id.tvP4DifTiempo);
        tvP4DT = findViewById(R.id.tvP4DT);
        tvParada5 =  findViewById(R.id.tvParada5);
        tvP5Tiempo =  findViewById(R.id.tvP5Tiempo);
        tvP5Difiempo =  findViewById(R.id.tvP5DifTiempo);
        tvP5DT = findViewById(R.id.tvP5DT);
        tvParada6 =  findViewById(R.id.tvParada6);
        tvP6Tiempo =  findViewById(R.id.tvP6Tiempo);
        tvP6Difiempo =  findViewById(R.id.tvP6DifTiempo);
        tvP6DT = findViewById(R.id.tvP6DT);
        tvParada7 =  findViewById(R.id.tvParada7);
        tvP7Tiempo =  findViewById(R.id.tvP7Tiempo);
        tvP7Difiempo =  findViewById(R.id.tvP7DifTiempo);
        tvP7DT = findViewById(R.id.tvP7DT);
//        tvParada8 =  findViewById(R.id.tvParada8);
//        tvP8Tiempo =  findViewById(R.id.tvP8Tiempo);
//        tvP8Difiempo =  findViewById(R.id.tvP8DifTiempo);
//        tvP8DT = findViewById(R.id.tvP8DT);
//        tvParada9 =  findViewById(R.id.tvParada9);
//        tvP9Tiempo =  findViewById(R.id.tvP9Tiempo);
//        tvP9Difiempo =  findViewById(R.id.tvP9DifTiempo);
//        tvP9DT = findViewById(R.id.tvP9DT);
//        tvParada10 =  findViewById(R.id.tvParada10);
//        tvP10Tiempo =  findViewById(R.id.tvP10Tiempo);
//        tvP10Difiempo =  findViewById(R.id.tvP10DifTiempo);
//        tvP10DT = findViewById(R.id.tvP10DT);
//        tvParada11 =  findViewById(R.id.tvParada11);
//        tvP11Tiempo =  findViewById(R.id.tvP11Tiempo);
//        tvP11Difiempo =  findViewById(R.id.tvP11DifTiempo);
//        tvP11DT = findViewById(R.id.tvP11DT);
        tvDestino =  findViewById(R.id.tvDestino);
        tvTiempoDestino =  findViewById(R.id.tvTiempoDestino);
        tvDifTDestino =  findViewById(R.id.tvDifTDestino);
        tvPDDT = findViewById(R.id.tvPDDT);

        tv_UnidadSiguiendo = findViewById(R.id.tvUnidadSiguiendo);
        tv_TiempoUnidadSiguiendo = findViewById(R.id.tvTiempoUnidadSiguiendo);

        spOrigen.setAdapter(new ArrayAdapter<>(this, R.layout.layout_diseno_spinner, Origen));
        spDestino.setAdapter(new ArrayAdapter<>(this, R.layout.layout_diseno_spinner, DestinoInicial));
        spUnidad.setAdapter(new ArrayAdapter<>(this, R.layout.layout_diseno_spinner, Unidad));

        btn_SHora =  findViewById(R.id.btn_SHora);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            spOrigen.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            spDestino.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            spUnidad.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }

    }

    private void f_CrearCanalNotificacion() {
        NotificationManager manejadorNotificaciones = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel miCanal =
                    new NotificationChannel(Constantes.CANAL_ID, Constantes.CANAL_NOMBRE, NotificationManager.IMPORTANCE_HIGH);

            miCanal.setDescription(Constantes.CANAL_DESCRIPCION);
            miCanal.enableLights(true);
            miCanal.setLightColor(Color.BLUE);
            miCanal.enableVibration(true);

            miCanal.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 100});

            manejadorNotificaciones.createNotificationChannel(miCanal);
        }
    }
    private void f_Obtener_Recorrido(String id_Recorrido){
        ff_BaseDatos.collection("registros_TR").document(id_Recorrido).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documento) {
                if (documento.exists()){
                    String hi = documento.getString("hi");
                    int u = Integer.parseInt(documento.getString("u"));
                    String o = documento.getString("lo");
                    final String d = documento.getString("ld");

                    String[] separar = hi.split(":");

                    picker_Hora = Integer.parseInt(separar[0]);
                    picker_Minutos = Integer.parseInt(separar[1]);
                    s_Hora = f_AgregarDecena(picker_Hora);
                     s_Minuto = f_AgregarDecena(picker_Minutos);
                    str_HoraI = s_Hora + ":" + s_Minuto;

                    btn_SHora.setText(str_HoraI);
                    spUnidad.setSelection(u);

                    if (o.equals("Soledad")) {
                        spDestino.setAdapter(new ArrayAdapter<String>(Usuario_Recorrido.this, R.layout.layout_diseno_spinner, Destino2));
                        spOrigen.setSelection(1);
                    } else if (o.equals("Satélite")) {
                        spDestino.setAdapter(new ArrayAdapter<String>(Usuario_Recorrido.this, R.layout.layout_diseno_spinner, Destino2));
                        spOrigen.setSelection(2);
                    } else if (o.equals("Trincheras")) {
                        spDestino.setAdapter(new ArrayAdapter<String>(Usuario_Recorrido.this, R.layout.layout_diseno_spinner, Destino));
                        spOrigen.setSelection(3);
                    } else if (o.equals("Encinos")) {
                        spDestino.setAdapter(new ArrayAdapter<String>(Usuario_Recorrido.this, R.layout.layout_diseno_spinner, Destino));
                        spOrigen.setSelection(4);
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            switch (d){
                                case "Soledad":
                                    spDestino.setSelection(1);
                                    break;
                                case "Satélite":
                                    spDestino.setSelection(2);
                                    break;
                                case "Trincheras":
                                    spDestino.setSelection(1);
                                    break;
                                case "Encinos":
                                    spDestino.setSelection(2);
                                    break;
                            }
                        }
                    },1000 );
                }
            }
        });
    }
    private void f_MostrarDirectorio() {
        String tempDir = System.getProperty("java.io.tmpdir");
        File directorio = new File(tempDir);
        File[] archivos = directorio.listFiles();
        for (int i = 0; i < archivos.length; i++)
            Log.d("Archivo "+i,archivos[i]+"");
    }
    private void f_Prueba(){

    }

    private void f_DiferenteDeCero(TextView tvT, TextView tvTD){
        if (tvT.getText().toString() == "")
            tvT.setTextColor(Color.BLACK);
        else
            f_Agregar_Color(tvTD,Integer.parseInt(tvTD.getText().toString()));
    }

    private void f_DiferenteTiempoChecada(TextView tvT, String punto){
        if (preferencias_Variables.getString(punto, "") != "")
            tvT.setText(preferencias_Variables.getString(punto, "").substring(0,5));
        else
            tvT.setText(preferencias_Variables.getString(punto, ""));
    }

    //*************************MANEJADORES************************//

    private Runnable act_Destino = new Runnable() {
        @Override
        public void run() {
            spOrigen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getItemAtPosition(position).toString().equals("Soledad") || parent.getItemAtPosition(position).toString().equals("Satélite")) {
                        spDestino.setAdapter(new ArrayAdapter<String>(Usuario_Recorrido.this, R.layout.layout_diseno_spinner, Destino2));
                        switch (destino){
                            case "Trincheras":
                                spDestino.setSelection(1);
                                break;
                            case "Encinos":
                                spDestino.setSelection(2);
                                break;
                            default:
                                spDestino.setSelection(0);
                                break;
                        }
                        ha_Destino.postDelayed(act_Destino, 1000);
                    } else if (parent.getItemAtPosition(position).toString().equals("Trincheras") || parent.getItemAtPosition(position).toString().equals("Encinos")) {
                        spDestino.setAdapter(new ArrayAdapter<String>(Usuario_Recorrido.this, R.layout.layout_diseno_spinner, Destino));
                        switch (destino){
                            case "Soledad":
                                spDestino.setSelection(1);
                                break;
                            case "Satélite":
                                spDestino.setSelection(2);
                                break;
                            default:
                                spDestino.setSelection(0);
                                break;
                        }
                        ha_Destino.postDelayed(act_Destino, 1000);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    };

    public Runnable act_UI = new Runnable() {
        @Override
        public void run() {
            servicio = preferencias_Variables.getBoolean("servicio_activo",false);
            if (servicio){
//                tvP1Tiempo.setText(preferencias_Variables.getString("Punto_1", ""));
//                if (preferencias_Variables.getString("Punto_1", "") != "")
//                    tvP1Tiempo.setText(preferencias_Variables.getString("Punto_1", "").substring(0,5));
                f_DiferenteTiempoChecada(tvP1Tiempo,"Punto_1");
                f_DiferenteTiempoChecada(tvP2Tiempo,"Punto_2");
                f_DiferenteTiempoChecada(tvP3Tiempo,"Punto_3");
                f_DiferenteTiempoChecada(tvP4Tiempo,"Punto_4");
                f_DiferenteTiempoChecada(tvP5Tiempo,"Punto_5");
                f_DiferenteTiempoChecada(tvP6Tiempo,"Punto_6");
                f_DiferenteTiempoChecada(tvP7Tiempo,"Punto_7");
//                f_DiferenteTiempoChecada(tvP8Tiempo,"Punto_8");
//                f_DiferenteTiempoChecada(tvP9Tiempo,"Punto_9");
//                f_DiferenteTiempoChecada(tvP10Tiempo,"Punto_10");
//                f_DiferenteTiempoChecada(tvP11Tiempo,"Punto_11");
                f_DiferenteTiempoChecada(tvTiempoDestino,"Punto_Destino");
                tvTiempoDestino.setText(preferencias_Variables.getString("Punto_Destino", ""));
                tvP1Difiempo.setText(preferencias_Variables.getString("Punto_1_Dif", "0"));
                f_DiferenteDeCero(tvP1Tiempo,tvP1Difiempo);
                tvP2Difiempo.setText(preferencias_Variables.getString("Punto_2_Dif", "0"));
                f_DiferenteDeCero(tvP1Tiempo,tvP2Difiempo);
                tvP3Difiempo.setText(preferencias_Variables.getString("Punto_3_Dif", "0"));
                f_DiferenteDeCero(tvP3Tiempo,tvP3Difiempo);
                tvP4Difiempo.setText(preferencias_Variables.getString("Punto_4_Dif", "0"));
                f_DiferenteDeCero(tvP4Tiempo,tvP4Difiempo);
                tvP5Difiempo.setText(preferencias_Variables.getString("Punto_5_Dif", "0"));
                f_DiferenteDeCero(tvP5Tiempo,tvP5Difiempo);
                tvP6Difiempo.setText(preferencias_Variables.getString("Punto_6_Dif", "0"));
                f_DiferenteDeCero(tvP6Tiempo,tvP6Difiempo);
                tvP7Difiempo.setText(preferencias_Variables.getString("Punto_7_Dif", "0"));
                f_DiferenteDeCero(tvP7Tiempo,tvP7Difiempo);
//                tvP8Difiempo.setText(preferencias_Variables.getString("Punto_8_Dif", "0"));
//                f_DiferenteDeCero(tvP8Tiempo,tvP8Difiempo);
//                tvP9Difiempo.setText(preferencias_Variables.getString("Punto_9_Dif", "0"));
//                f_DiferenteDeCero(tvP9Tiempo,tvP9Difiempo);
//                tvP10Difiempo.setText(preferencias_Variables.getString("Punto_10_Dif", "0"));
//                f_DiferenteDeCero(tvP10Tiempo,tvP10Difiempo);
//                tvP11Difiempo.setText(preferencias_Variables.getString("Punto_11_Dif", "0"));
//                f_DiferenteDeCero(tvP11Tiempo,tvP11Difiempo);
                tvDifTDestino.setText(preferencias_Variables.getString("Punto_Destino_Dif", "0"));
                f_DiferenteDeCero(tvTiempoDestino,tvDifTDestino);

                tv_UnidadSiguiendo.setText(preferencias_Variables.getString("tv_UnidadSiguiendo","0"));
                tv_TiempoUnidadSiguiendo.setText(preferencias_Variables.getString("tv_TiempoUnidadSiguiendo","0"));


                if (tvTiempoDestino.getText().toString() != ""){
                    tv_PrepararRuta.setText("Ruta Finalizada");
                    tv_CancelarRuta.setText("Limpiar");
                    editor_Variables.putString("tv_PrepararRuta", "Ruta Finalizada");
                    editor_Variables.putString("btn_Cancelar", "Limpiar");
                    editor_Variables.putBoolean("servicio_activo", false);
                    f_DetenerAlarma();
                    stopService(intentoServicio);
                }
            }
            ha_Actualizar_UI.postDelayed(act_UI,5000);
        }
    };

}
