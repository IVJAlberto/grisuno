package com.ceromiedo.combizona.grisuno.Servicio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ceromiedo.combizona.grisuno.Constantes;
import com.ceromiedo.combizona.grisuno.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED;
import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED;
import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED;

public class ServicioUbicacion extends Service implements GpsStatus.Listener {

//    ListenerRegistration lr;

    //------ACCIONES------//
    public final static String ACCION_COORDENADAS = "COORDENADAS";
    public final static String ACCION_DIF_HORA = "DIF_HORA";
    public final static String ACCION_HORA = "HORA";
    public final static String ACCION_PUNTO = "PUNTO";
    public final static String ACCION_UNIDAD_SIGUIENDO= "DIF_US";
    public final static String ACCION_TIEMPO_UNIDAD_SIGUIENDO = "DIF_TUS";

    //------ARCHIVOS------//
    String nombreRecorrido;
    int valor_GPS = 2;

    //------ARREGLOS------//
    double[] array_Punto1 = {0.0, 0.0, 0.0, 0.0}, array_Punto2 = {0.0, 0.0, 0.0, 0.0}, array_Punto3 = {0.0, 0.0, 0.0, 0.0},
            array_Punto4 = {0.0, 0.0, 0.0, 0.0}, array_Punto5 = {0.0, 0.0, 0.0, 0.0}, array_Punto6 = {0.0, 0.0, 0.0, 0.0},
            array_Punto7 = {0.0, 0.0, 0.0, 0.0}, array_PuntoDestino = {0.0, 0.0, 0.0, 0.0};
//    int[] Tiempos_OSo = {6,15,18,30,45,55,62,72};
//    int[] Tiempos_OSa = {10,19,22,34,49,59,66,76};
//    int[] Tiempos_DSo = {8,15,23,38,50,60,66,70};
//    int[] Tiempos_DSa = {8,15,23,38,50,60,66,74};
    int[] Tiempos_OSo_DT = {6,15,18,30,45,55,62,70};
    int[] Tiempos_OSo_DE = {6,15,18,30,45,55,62,72};
    int[] Tiempos_OSa_DT = {10,19,22,34,49,59,66,74};
    int[] Tiempos_OSa_DE = {10,19,22,34,49,59,66,76};
    int[] Tiempos_OT_DSo = {8, 15, 23, 38, 50, 60, 66, 70};
    int[] Tiempos_OT_DSa = {8, 15, 23, 38, 50, 60, 66, 75};
    int[] Tiempos_OE_DSo = {8, 15, 23, 38, 50, 60, 66, 70};
    int[] Tiempos_OE_DSa = {8, 15, 23, 38, 50, 60, 66, 75};
    final String[] arr_NombreColision = {"Punto_1","Punto_2","Punto_3","Punto_4","Punto_5","Punto_6","Punto_7","Punto_Destino"};
    final String[] arr_DifNombreColision = {"Punto_1_Dif","Punto_2_Dif","Punto_3_Dif",
            "Punto_4_Dif","Punto_5_Dif","Punto_6_Dif","Punto_7_Dif","Punto_Destino_Dif"};

    final String[] Paradas_O_SS = {"Educadores","Tecnológico", "Lomas","Bosque","Ocampo","Lágrima","Santa Cecilia"};
    final String[] Paradas_O_TE = {"Santa Cecilia","Lágrima","Ocampo","Bosque","Lomas","Sec. 65","Educadores"};

    String[] Paradas_RECORRIDO = {"","","","","","",""};

    //------BASEDATOS------//
    FirebaseFirestore miBDRegistrosTR_FS;
    String llave = "";

    //------BOOLEANOS------//
    boolean bool_Punto1 = false, bool_Punto2 = false, bool_Punto3 = false,
            bool_Punto4 = false, bool_Punto5 = false, bool_Punto6 = false,
            bool_Punto7 = false, bool_PuntoDestino = false;
    boolean hayInternet = false;
    boolean ahorro_energia = false;

    //------COORDENADAS------//
    Intent iCoordenadas;
    private GpsStatus estadoGPS;
    private LocationManager manejadorUbicacion;
    private LocationListener listenerUbicacion;
    private Location ubicacion;
    // {Lat ARRIBA , Lat ABAJO, Lon DERECHA , Lon IZQUIERDA
    double[] C_Educadores = {19.730650814477784, 19.730390766417827, -101.17373922070324, -101.17407986127812};
    double[] C_Bosque = {19.69760851171307, 19.695741165132233, -101.18148398096393, -101.18297824017274};
    double[] C_65 = {19.722236688519462, 19.721643343042164, -101.17829516530037, -101.17881014943123};
    double[] C_Lagrima = {19.671406446211513, 19.67040021538972, -101.19665335659909, -101.19787107949185};
    double[] C_Ocampo = {19.686520, 19.686058, -101.196891, -101.197521};
    double[] C_SantaCecilia = {19.668486938677184, 19.667663922533936, -101.18332769204892, -101.18448617129121};
    double[] C_Tecnologico = {19.721805702616674, 19.72052709967644, -101.18730961705779, -101.18857561971282};
    double[] C_Lomas = {19.716207009331313, 19.715469212885395, -101.17440316434863, -101.17516222949985};
    double[] C_SantaMaria = {19.672540229328447, 19.67170650892416, -101.18994378739241, -101.19084568018819};
    double[] B_Trincheras = {19.66402785496262, 19.66315898155512, -101.19150906801224, -101.19249612092972};
    double[] B_Encinos = {19.653545492410494, 19.652645240239853, -101.17182701826096, -101.17312520742416};
    double[] B_Soledad = {19.73271354431679, 19.731627917850606, -101.18106454610825, -101.18205159902573};
    double[] B_Satelite = {19.73670757631652, 19.735212981852108, -101.16447776556015, -101.16529315710068};

    //------DOBLES------//
    double latitud, longitud;

    //------ENTEROS------//
    int i_HoraI, i_MinutosI;

    //------FORMATOS------//
    Date d_HFinal, d_HInicial;
    DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat formato_HMS = new SimpleDateFormat("HH:mm:ss");
    DateFormat formato_HM = new SimpleDateFormat("HH:mm");
    DateFormat formato_Hora = new SimpleDateFormat("HH");
    DateFormat formato_Minutos = new SimpleDateFormat("mm");

    //------PREFERENCIAS------//
    SharedPreferences preferencias_Variables;
    SharedPreferences.Editor editor_Variables;
    String TAG = "Servicio_Ubicacion";

    //------STRINGS------//
    String destino, hora, if_Hora, if_Minutos,origen, st_DiferenciaHor;
    String s_Hora, s_Minutos;

    //------MANEJADORES------//
    Handler ha_AhorroE = new Handler();
    Handler ha_Datones = new Handler();
    LocalBroadcastManager manejador_Coordenadas;
    PowerManager manejador_Poder;

    //------NOTIFICACIONES------//
    NotificationManager mNotificationManager;

    //------VOZ------//
    TextToSpeech tts_Tiempos, tts_Diferencia_Unidades;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        manejadorUbicacion = (LocationManager) ServicioUbicacion.this.getSystemService(Context.LOCATION_SERVICE);
        manejadorUbicacion.addGpsStatusListener(this);
        manejador_Coordenadas = LocalBroadcastManager.getInstance(this);
        manejador_Poder = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
        iCoordenadas = new Intent(ACCION_COORDENADAS);
        d_HInicial = new Date();
        preferencias_Variables = getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE);
        editor_Variables = getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE).edit();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        miBDRegistrosTR_FS = FirebaseFirestore.getInstance();
        editor_Variables.putInt("valor_GPS",valor_GPS);
        editor_Variables.putBoolean("ahorro_energia",false);
        editor_Variables.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hora = formato_HMS.format(Calendar.getInstance().getTime());
                f_AgregarTexto(nombreRecorrido,"SD" ,hora + " / " + origen.substring(0,2) + "-" + destino.substring(0,2));
                origen = "";
                destino = "";
                llave = "";
                i_HoraI = 0;
                i_MinutosI = 0;
                manejadorUbicacion.removeUpdates(listenerUbicacion);
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1);
                Intent i = getPackageManager()
                        .getLaunchIntentForPackage(getPackageName())
                        .setPackage(null)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 93, i, 0);
                pi.cancel();
                f_Detener_Handlers();
                editor_Variables.putBoolean("p1",false);
                editor_Variables.putBoolean("p2",false);
                editor_Variables.putBoolean("p3",false);
                editor_Variables.putBoolean("p4",false);
                editor_Variables.putBoolean("p5",false);
                editor_Variables.putBoolean("p6",false);
                editor_Variables.putBoolean("p7",false);
                editor_Variables.putBoolean("pD",false);
                editor_Variables.commit();

                if (tts_Tiempos != null){
                    tts_Tiempos.stop();
                    tts_Tiempos.shutdown();
                }
                if (tts_Diferencia_Unidades != null){
                    tts_Diferencia_Unidades.stop();
                    tts_Diferencia_Unidades.shutdown();
                }
//                if (tts_GPS != null) {
//                    tts_GPS.stop();
//                    tts_GPS.shutdown();
//                }

            }
        }, 5000);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        origen = intent.getStringExtra("ENVIAR_ORIGEN");
        destino = intent.getStringExtra("ENVIAR_DESTINO");
        llave = intent.getStringExtra("ENVIAR_LLAVE");
        i_HoraI = intent.getIntExtra("ENVIAR_HORAI", preferencias_Variables.getInt("picker_Hora", 0));
        i_MinutosI = intent.getIntExtra("ENVIAR_MINUTOSI", preferencias_Variables.getInt("picker_Minutos", 0));
        d_HInicial.setHours(i_HoraI);
        d_HInicial.setMinutes(i_MinutosI);
        d_HInicial.setSeconds(0);

        iCoordenadas.putExtra("PUNTO","Recorrido_I");
        manejador_Coordenadas.sendBroadcast(iCoordenadas);

        nombreRecorrido = preferencias_Variables.getString("nombreArchivo","");

        if (origen == null || origen.equals(""))
            origen = preferencias_Variables.getString("origen","");
        if (destino == null ||  destino.equals(""))
            destino = preferencias_Variables.getString("destino","");
        if( llave==null || llave.equals(""))
            llave = preferencias_Variables.getString("llave_TR", "");
        if (i_HoraI==0)
            i_HoraI = preferencias_Variables.getInt("picker_Hora", i_HoraI);
        if (i_MinutosI==0)
            i_MinutosI = preferencias_Variables.getInt("picker_Minutos", i_MinutosI);

        hora = formato_HMS.format(Calendar.getInstance().getTime());
        f_AgregarTexto(nombreRecorrido, "SI" ,hora + " / " + origen.substring(0,2) + "-" + destino.substring(0,2));
        editor_Variables.putString("servicio_iniciado",hora);
        editor_Variables.commit();

        bool_Punto1 = preferencias_Variables.getBoolean("p1",false);
        bool_Punto2 = preferencias_Variables.getBoolean("p2",false);
        bool_Punto3 = preferencias_Variables.getBoolean("p3",false);
        bool_Punto4 = preferencias_Variables.getBoolean("p4",false);
        bool_Punto5 = preferencias_Variables.getBoolean("p5",false);
        bool_Punto6 = preferencias_Variables.getBoolean("p6",false);
        bool_Punto7 = preferencias_Variables.getBoolean("p7",false);
        bool_PuntoDestino = preferencias_Variables.getBoolean("pD",false);

        if (origen.equals("Soledad") || origen.equals("Satélite")) {
            f_AsignarCoordenadas(array_Punto1, C_Educadores);
            f_AsignarCoordenadas(array_Punto2, C_Tecnologico);
            f_AsignarCoordenadas(array_Punto3, C_Lomas);
            f_AsignarCoordenadas(array_Punto4, C_Bosque);
            f_AsignarCoordenadas(array_Punto5, C_Ocampo);
            f_AsignarCoordenadas(array_Punto6, C_Lagrima);
            f_AsignarCoordenadas(array_Punto7, C_SantaCecilia);
            f_AsignarPuntos(Paradas_RECORRIDO,Paradas_O_SS);
        }else if(origen.equals("Trincheras") || origen.equals("Encinos")){
            f_AsignarCoordenadas(array_Punto1, C_SantaCecilia);
            f_AsignarCoordenadas(array_Punto2, C_Lagrima);
            f_AsignarCoordenadas(array_Punto3, C_Ocampo);
            f_AsignarCoordenadas(array_Punto4, C_Bosque);
            f_AsignarCoordenadas(array_Punto5, C_Lomas);
            f_AsignarCoordenadas(array_Punto6, C_65);
            f_AsignarCoordenadas(array_Punto7, C_Educadores);
            f_AsignarPuntos(Paradas_RECORRIDO,Paradas_O_TE);
        }
        if (destino.equals("Trincheras"))
            f_AsignarCoordenadas(array_PuntoDestino, B_Trincheras);
        else if (destino.equals("Encinos"))
            f_AsignarCoordenadas(array_PuntoDestino, B_Encinos);
        else if (destino.equals("Soledad"))
            f_AsignarCoordenadas(array_PuntoDestino, B_Soledad);
        else
            f_AsignarCoordenadas(array_PuntoDestino, B_Satelite);


        if (tts_Tiempos != null){
            tts_Tiempos.stop();
            tts_Tiempos.shutdown();
        }
        if (tts_Diferencia_Unidades != null){
            tts_Diferencia_Unidades.stop();
            tts_Diferencia_Unidades.shutdown();
        }
//        if (tts_GPS != null) {
//            tts_GPS.stop();
//            tts_GPS.shutdown();
//        }

        s_Hora = f_AgregarDecena(i_HoraI);
        s_Minutos = f_AgregarDecena(i_MinutosI);

        f_NotificacionRecorrido(origen + " - " + destino,"Servicio iniciado", s_Hora +":"+ s_Minutos);

        listenerUbicacion = new LocationListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location) {
                ubicacion = location;
                if (manejadorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER) || manejadorUbicacion.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                    if (manejadorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
                        ubicacion = manejadorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);

//                    Log.e(TAG,manejadorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString());
                        if (!ubicacion.equals(null) && ubicacion != null) {
                            if (ubicacion.getLatitude() != 0)
                                latitud = ubicacion.getLatitude();
                            if (ubicacion.getLongitude() != 0)
                                longitud = ubicacion.getLongitude();
                        }

                    if (!bool_Punto1)
                        bool_Punto1 = f_Colision(array_Punto1, longitud, latitud, "p1", 0, "d1");
                    if (!bool_Punto2)
                        bool_Punto2 = f_Colision(array_Punto2, longitud, latitud, "p2", 1, "d2");
                    if (!bool_Punto3)
                        bool_Punto3 = f_Colision(array_Punto3, longitud, latitud, "p3", 2, "d3");
                    if (!bool_Punto4)
                        bool_Punto4 = f_Colision(array_Punto4, longitud, latitud, "p4", 3, "d4");
                    if (!bool_Punto5)
                        bool_Punto5 = f_Colision(array_Punto5, longitud, latitud, "p5", 4, "d5");
                    if (!bool_Punto6)
                        bool_Punto6 = f_Colision(array_Punto6, longitud, latitud, "p6", 5, "d6");
                    if (!bool_Punto7)
                        bool_Punto7 = f_Colision(array_Punto7, longitud, latitud, "p7", 6, "d7");
                    if (!bool_PuntoDestino)
                        bool_PuntoDestino = f_Colision(array_PuntoDestino, longitud, latitud, "pD", 7, "dD");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                hora = formato_HMS.format(Calendar.getInstance().getTime());
                int valor_GPS_adentro = status;
                valor_GPS = preferencias_Variables.getInt("valor_GPS",2);
                editor_Variables.putInt("valor_GPS",valor_GPS_adentro);
                editor_Variables.commit();
                if (valor_GPS != valor_GPS_adentro){
                    if (valor_GPS_adentro == 1)
                        f_AgregarTexto(nombreRecorrido, "GPSab" , hora);
                    else
                        f_AgregarTexto(nombreRecorrido, "GPSap" , hora);
                }
        }

            @Override
            public void onProviderEnabled(String provider) {
//                f_AgregarTexto(nombreRecorrido, "GPS ON" , hora);
            }

            @Override
            public void onProviderDisabled(String provider) {
//                f_AgregarTexto(nombreRecorrido, "GPS OFF" , hora);
                Log.e(TAG,"Desactivado");
                f_AgregarTexto(nombreRecorrido, "GPSoff" , hora);
            }
        };

//        for (int i = 0; i < Paradas_RECORRIDO.length;i++){
////            Log.e(TAG,Paradas_RECORRIDO[i]);
////        }

        if (manejadorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER))
            manejadorUbicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listenerUbicacion);
        else
            manejadorUbicacion.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listenerUbicacion);



        ha_AhorroE.post(lector_bateria);
        ha_Datones.post(lector_Datones);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onGpsStatusChanged(int i){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        estadoGPS = manejadorUbicacion.getGpsStatus(estadoGPS);
        hora = formato_HMS.format(Calendar.getInstance().getTime());
        switch (i){
            case GpsStatus.GPS_EVENT_STARTED:
                f_AgregarTexto(nombreRecorrido, "GPSon" , hora);
                f_NotificacionRecorrido(origen + " - " + destino,"GPS Encendido.",s_Hora + ":" + s_Minutos);
//                f_TTS_GPS(tts_GPS_Encendido,"encendido");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
//                f_AgregarTexto(nombreRecorrido, "GPSoff" , hora);
                f_NotificacionRecorrido(origen + " - " + destino,"GPS Apagado.",s_Hora + ":" + s_Minutos);
//                f_TTS_GPS(tts_GPS_Apagado,"apagado");
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                f_AgregarTexto(nombreRecorrido, "GPSfix" , hora);
                f_NotificacionRecorrido(origen + " - " + destino,"GPS Conectado.",s_Hora + ":" + s_Minutos);
//                f_TTS_GPS(tts_GPS_Conectado, "conectado");
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                break;
        }
    }

    public ServicioUbicacion() {
    }


    //*************************ASYNCTASK************************//

    public class asyt_Conexion extends AsyncTask<String,String, Boolean> {
        HttpURLConnection urlc = null;
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Thread.sleep(500);
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
                    urlc.setConnectTimeout(5000);
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
            if (urlc != null)
                urlc.disconnect();
            if(bool)
                hayInternet = true;
            else
                hayInternet = false;
        }
    }

    //*************************FUNCIONES************************//

    private void f_Actualizar_Recorrido(String r, String d, String h){

        String u = preferencias_Variables.getString("unidad","0");

        Map<String, Object> datones_Recorrido = new HashMap<>();
        datones_Recorrido.put("T",h);
        datones_Recorrido.put("U",u);
        miBDRegistrosTR_FS.collection("pruebas").document("r").collection(r)
                .document(d).set(datones_Recorrido, SetOptions.merge());
    }
    private String f_AgregarDecena(int i){
        String s =""+i;
        if(i<10)
            s = "0" + i;
        return s;
    }
    private void f_AgregarTexto(String nombreArchivo, String n_Atributo, String v_Atributo){
        File archivo = new File( System.getProperty("java.io.tmpdir")+"/"+nombreArchivo+".json");
        try {
            FileOutputStream foutputstream = new FileOutputStream(archivo,true);
            OutputStreamWriter escritor = new OutputStreamWriter(foutputstream);
//            escritor.append("\n\""+ n_Atributo + "\":" + "\""+ v_Atributo + "\",");
            escritor.append(/*"\n\""+ */ "\n"+ n_Atributo + "_" + /*"\":" + "\""+*/ v_Atributo/* + "\","*/);
            escritor.close();
            foutputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void f_AsignarCoordenadas(double[] punto, double[] parada){
        for(int i=0;i<4;i++){
            punto[i] = parada[i];
        }
    }
    private void f_AsignarPuntos(String[] punto, String[] parada){
        for(int i=0;i<7;i++){
            punto[i] = parada[i];
        }
    }
    private boolean f_Colision(final double[] arrayPColision, double longitud, double latitud, final String nombrePColision, final int n, final String nombreDifTiempo){
        if(latitud < arrayPColision[0] && latitud > arrayPColision[1] && longitud < arrayPColision[2] &&  longitud > arrayPColision[3]){
            d_HFinal = new Date();
            hora = formato_HM.format(Calendar.getInstance().getTime());
            if_Hora = formato_Hora.format(Calendar.getInstance().getTime());
            if_Minutos = formato_Minutos.format(Calendar.getInstance().getTime());

            st_DiferenciaHor = f_DifHora(i_HoraI,Integer.parseInt(if_Hora),i_MinutosI,Integer.parseInt(if_Minutos),n);

            new asyt_Conexion().execute();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run(){
                    if(hayInternet && !llave.equals("")){
                        Map<String, Object> datones_Registro_TR = new HashMap<>();
                        datones_Registro_TR.put(nombrePColision,hora);
                        datones_Registro_TR.put(nombreDifTiempo,st_DiferenciaHor);
                        miBDRegistrosTR_FS.collection("registros_TR").document(llave).set(datones_Registro_TR, SetOptions.merge());

                        String r;
                        if (origen.equals("Trincheras") || origen.equals("Encinos"))
                            r = "sur_norte";
                        else
                            r = "norte_sur";

                        if (!arr_NombreColision[n].equals("Punto_Destino"))
                            f_Obtener_Checada_Recorrido(r,arr_NombreColision[n], hora, if_Hora, if_Minutos);

                    }
                }
            }, 2000);

            if (arr_NombreColision[n].equals("Punto_Destino")) {
                f_AgregarTexto(nombreRecorrido, "PD" , hora);
                tts_Tiempos  = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i!=TextToSpeech.ERROR){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                tts_Tiempos.setLanguage(Locale.forLanguageTag("spa"));
                            }
                            String texto;
                            if (Integer.parseInt(st_DiferenciaHor)>0)
                                texto = "¡Llegaste a " + destino + " con" + st_DiferenciaHor + " minutos de atraso.";
                            else if (Integer.parseInt(st_DiferenciaHor)<0)
                                texto = "¡Llegaste a " + destino + " con" + st_DiferenciaHor + " minutos de adelanto.";
                            else
                                texto = "¡Llegaste a " + destino + " en el tiempo acordado.";
                            tts_Tiempos.speak(texto,TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        iCoordenadas.putExtra("PUNTO", arr_NombreColision[n]);
                        iCoordenadas.putExtra("HORA",hora);
                        iCoordenadas.putExtra("DIF_HORA",st_DiferenciaHor);
                        manejador_Coordenadas.sendBroadcast(iCoordenadas);
                    }
                },3000);
            }else{
                f_AgregarTexto(nombreRecorrido, "P"+(n+1) , hora);

                tts_Tiempos  = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i!=TextToSpeech.ERROR){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                tts_Tiempos.setLanguage(Locale.forLanguageTag("spa"));
                            }
                            String texto;
                            if (Integer.parseInt(st_DiferenciaHor)>0)
                                texto = "¡Llegaste a " + Paradas_RECORRIDO[n] + " con" + st_DiferenciaHor + " minutos de atraso.";
                            else if (Integer.parseInt(st_DiferenciaHor)<0)
                                texto = "¡Llegaste a " + Paradas_RECORRIDO[n] + " con" + st_DiferenciaHor + " minutos de adelanto.";
                            else
                                texto = "¡Llegaste a " + Paradas_RECORRIDO[n] + " en el tiempo acordado.";
                            tts_Tiempos.speak(texto,TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                });

                iCoordenadas.putExtra("PUNTO", arr_NombreColision[n]);
                iCoordenadas.putExtra("HORA",hora);
                iCoordenadas.putExtra("DIF_HORA",st_DiferenciaHor);
                manejador_Coordenadas.sendBroadcast(iCoordenadas);
            }

            editor_Variables.putBoolean(nombrePColision,true);
            editor_Variables.putString(arr_NombreColision[n],hora);
            editor_Variables.putString(arr_DifNombreColision[n],st_DiferenciaHor);
            editor_Variables.commit();

            return true;
        }
        return false;
    }
    public boolean f_Comprobar_Ahorro_Datones() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            switch (connMgr.getRestrictBackgroundStatus()){
                case RESTRICT_BACKGROUND_STATUS_ENABLED:
//                    Log.e(TAG,"AHORRO DATOS ACTIVADO");
                    return true;
                case RESTRICT_BACKGROUND_STATUS_DISABLED:
//                    Log.e(TAG,"AHORRO DATOS DESACTIVADO");
                    return false;
                case RESTRICT_BACKGROUND_STATUS_WHITELISTED:
//                    Log.e(TAG,"App en lista blanca.");
            }
        }
        return false;
    }
    private boolean f_Comprobar_Datones(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return tm.isDataEnabled();
        }else{
            return tm.getSimState() == TelephonyManager.SIM_STATE_READY && tm.getDataState() != TelephonyManager.DATA_DISCONNECTED;
        }
    }
    private void f_Detener_Handlers(){
        ha_AhorroE.removeCallbacks(lector_bateria);
        ha_AhorroE.removeMessages(0);
//        ha_AhorroE.removeCallbacksAndMessages(null);
        ha_Datones.removeCallbacks(lector_Datones);
        ha_Datones.removeMessages(0);
//        ha_Datones.removeCallbacksAndMessages(null);
    }
    private String f_DifHora_Unidades(int hu, int mu, String hc){
        int resta = 0;

        if (!hc.equals("") && !hc.isEmpty() && hc!= null){
            String[] tiempo = hc.split("[:]");
            String h = tiempo[0].replace("\"","");
            String m = tiempo[1].replace("\"","");

            int hh = hu - Integer.parseInt(h);
            int mm = mu - Integer.parseInt(m) ;
            if(hh>0)
                mm+=(hh*60);
            else if(hh<0)
                mm-=((Math.abs(hh))*60);

            resta = mm;
        }
        return String.valueOf(resta);
    }
    private String f_DifHora(int hi, int hf, int mi, int mf,int n){
        //Validar si hi y mi son iguales a 0, entonces inicializarlos como preferencias_Variables.getInt(picker_Hora y picker_Minutos)
        if(hi==0 && mi==0) {
            hi = preferencias_Variables.getInt("picker_Hora", hi);
            mi = preferencias_Variables.getInt("picker_Minutos", mi);
        }

        int hh = hf - hi;
        int mm = mf - mi;
        int resta = 0;
        if(hh>0)
            mm+=(hh*60);
        else if(hh<0)
            mm-=((Math.abs(hh))*60);

//        if (origen.equals("Soledad")){
//            resta = mm - Tiempos_OSo[n];
//        }else if (origen.equals("Sátelite")){
//            resta = mm - Tiempos_OSa[n];
//        }else if (destino.equals("Soledad")){
//            resta = mm - Tiempos_DSo[n];
//        }else if (destino.equals("Sátelite")){
//            resta = mm - Tiempos_DSa[n];
//        }

        if (origen.equals("Trincheras")){
            if (destino.equals("Soledad"))
                resta = mm - Tiempos_OT_DSo[n];
            else
                resta = mm - Tiempos_OT_DSa[n];
        }else if (origen.equals("Encinos")){
            if (destino.equals("Soledad"))
                resta = mm - Tiempos_OE_DSo[n];
            else
                resta = mm - Tiempos_OE_DSa[n];
        }else if (destino.equals("Trincheras")){
            if (origen.equals("Soledad"))
                resta = mm - Tiempos_OSo_DT[n];
            else
                resta = mm - Tiempos_OSa_DT[n];
        }else{
            if (origen.equals("Soledad"))
                resta = mm - Tiempos_OSo_DE[n];
            else
                resta = mm - Tiempos_OSa_DE[n];
        }

        return String.valueOf(resta);
    }
    private void f_NotificacionRecorrido(String titulo, String cuerpo, String tiempo){
        //NOTIFICACION
        Intent i = getPackageManager()
                .getLaunchIntentForPackage(getPackageName())
                .setPackage(null)
                .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pi =PendingIntent.getActivity(getApplicationContext(),93,i,0);

        Bitmap iconoLargo = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            RemoteViews notificacionView1 = new RemoteViews(getPackageName(), R.layout.layout_diseno_notificacion_xml);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel canal = new NotificationChannel("canal-01",
                        "Canal O",
                        NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(canal);
            }
            notificacionView1.setTextViewText(R.id.content_title, titulo);
            notificacionView1.setTextViewText(R.id.content_text,cuerpo);
            notificacionView1.setTextViewText(R.id.timestamp,tiempo);

            Notification notificacion = new NotificationCompat.Builder(getApplicationContext(),"canal-01")
                    .setSmallIcon(R.drawable.icono_combizona)
                    .setLargeIcon(iconoLargo)
                    .setCustomContentView(notificacionView1)
                    .setContentIntent(pi)
                    .build();

            startForeground(1,notificacion);
        }else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
            Notification.Builder notiBuilder = new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.icono_combizona_48)
                    .setLargeIcon(iconoLargo)
                    .setContentTitle(titulo +  " - " + tiempo)
                    .setContentText(cuerpo)
                    .setContentIntent(pi);
            startForeground(1,notiBuilder.build());
        }else{
            Notification.Builder notiBuilder = new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.icono_combizona_48)
                    .setLargeIcon(iconoLargo)
                    .setContentTitle(titulo + " - " + tiempo)
                    .setContentText(cuerpo)
                    .setContentIntent(pi);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                startForeground(1,notiBuilder.build());
            }
        }
    }
    private void f_Obtener_Checada_Recorrido(final String r, final String d, final String h, final String ifhora, final String ifminutos){
//        Query q = miBDRegistrosTR_FS.collection("pruebas").document("r").collection(r);
//        lr = ((CollectionReference) q).document(d).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documento, @Nullable FirebaseFirestoreException e) {
//                if (documento.exists()){
//                    final String ti = documento.get("T").toString();
//                    final String ui = documento.get("U").toString();
//                    try{
//                        if (ti != "00:00" && !ti.equals("00:00")){
//                            final String diferencia_Unidades = f_DifHora_Unidades(Integer.parseInt(ifhora),Integer.parseInt(ifminutos),ti);
//                            tts_Diferencia_Unidades  = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//                                @Override
//                                public void onInit(int i) {
//                                    if (i!=TextToSpeech.ERROR){
//                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//                                            tts_Diferencia_Unidades.setLanguage(Locale.forLanguageTag("spa"));
//                                        }
//                                        String texto = "¡Vas a " + diferencia_Unidades + " minutos de la unidad " + ui + "!";
//                                        tts_Diferencia_Unidades.speak(texto,TextToSpeech.QUEUE_FLUSH,null);
//                                    }
//                                }
//                            });
//                            String u = ui.replace("\"","");
//                            iCoordenadas.putExtra("DIF_US",u);
//                            iCoordenadas.putExtra("DIF_TUS",diferencia_Unidades);
//                            manejador_Coordenadas.sendBroadcast(iCoordenadas);
//                            lr.remove();
//                        }
//                    }catch (Exception io){Log.e(TAG, e.getMessage()); lr.remove();}finally {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                f_Actualizar_Recorrido(r,d,h);
//                            }
//                        },15000);
//                    }
//                }
//            }
//        });


        Source source = Source.SERVER;
        miBDRegistrosTR_FS.collection("pruebas").document("r").collection(r)
                .document(d).get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot documento = task.getResult();
                    final String ti = documento.get("T").toString();
                    final String ui = documento.get("U").toString();
                    try{
                        if (ti != "00:00" && !ti.equals("00:00")){
                            final String diferencia_Unidades = f_DifHora_Unidades(Integer.parseInt(ifhora),Integer.parseInt(ifminutos),ti);
                            tts_Diferencia_Unidades  = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int i) {
                                    if (i!=TextToSpeech.ERROR){
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                            tts_Diferencia_Unidades.setLanguage(Locale.forLanguageTag("spa"));
                                        }
                                        String texto = "¡Vas a " + diferencia_Unidades + " minutos de la unidad " + ui + "!";
                                        tts_Diferencia_Unidades.speak(texto,TextToSpeech.QUEUE_FLUSH,null);
                                    }
                                }
                            });
                            String u = ui.replace("\"","");
                            iCoordenadas.putExtra("DIF_US",u);
                            iCoordenadas.putExtra("DIF_TUS",diferencia_Unidades);
                            manejador_Coordenadas.sendBroadcast(iCoordenadas);
                        }
                    }catch (Exception io){}finally {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                f_Actualizar_Recorrido(r,d,h);
                            }
                        },15000);
                    }
                }
            }
        });
    }

//    private void f_TTS_GPS(TextToSpeech tts_GPS ,final String estado){
//        if (tts_GPS == null){
//            tts_GPS  = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//                @Override
//                public void onInit(int i) {
//                    if (i!=TextToSpeech.ERROR){
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            tts_GPS.setLanguage(Locale.forLanguageTag("spa"));
//                        }
//                        String texto = "¡GPS " + estado;
//
//                        tts_GPS.speak(texto,TextToSpeech.QUEUE_FLUSH,null);
//                    }
//                }
//            });
//        }
//    }

    //*************************MANEJADORES************************//

    private Runnable lector_bateria = new Runnable() {
        @Override
        public void run() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                boolean ahorro = preferencias_Variables.getBoolean("ahorro_energia",false);
                if ( manejador_Poder.isPowerSaveMode()){
                    editor_Variables.putBoolean("ahorro_energia",true);
                    ahorro_energia = true;
                    if (ahorro_energia != ahorro)
                        f_AgregarTexto(nombreRecorrido, "MABon" , hora);
                }else{
                    editor_Variables.putBoolean("ahorro_energia",false);
                    ahorro_energia = false;
                    if (ahorro_energia != ahorro)
                        f_AgregarTexto(nombreRecorrido, "MABoff" , hora);
                }
                editor_Variables.commit();
            }

            ha_AhorroE.postDelayed(lector_bateria,10000);
        }
    };

    private Runnable lector_Datones = new Runnable() {
        @Override
        public void run() {
            boolean mobile = f_Comprobar_Datones(getApplicationContext());
            boolean datones = preferencias_Variables.getBoolean("datones",false);
            String h = formato_HMS.format(Calendar.getInstance().getTime());
            if (datones == mobile){
                //                Log.e(TAG,"Es igual");
            } else{
                if (mobile == false)
                    f_AgregarTexto(nombreRecorrido, "Doff" , h);
                else
                    f_AgregarTexto(nombreRecorrido, "Don" , h);
            }
            boolean ahorro_datones = f_Comprobar_Ahorro_Datones();
            boolean ahorro = preferencias_Variables.getBoolean("ahorro_datones",false);
            if (ahorro == ahorro_datones){
//                                Log.e(TAG,"Es igual");
            } else{
                if (ahorro_datones == false)
                    f_AgregarTexto(nombreRecorrido, "ADoff" , h);
                else
                    f_AgregarTexto(nombreRecorrido, "ADon" , h);
            }
            editor_Variables.putBoolean("datones",mobile);
            editor_Variables.putBoolean("ahorro_datones",ahorro_datones);
            editor_Variables.commit();
            ha_AhorroE.postDelayed(lector_Datones,10000);
        }
    };

}
