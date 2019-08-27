package com.ceromiedo.combizona.grisuno.Recibidores;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.ceromiedo.combizona.grisuno.Constantes;
import com.ceromiedo.combizona.grisuno.Servicio.ServicioUbicacion;

import static android.content.Context.MODE_PRIVATE;

public class AlarmaNotificacion extends BroadcastReceiver {

    static String TAG = "Alarma";
    String origen,destino,llave, hora;
    Context contexto;
    Intent iServicio;
    boolean bool_Servicio, servicio_activo;
    int i_HoraI, i_MinutosI;
    Constantes constantes = new Constantes();
    public SharedPreferences preferencias_Variables;
    public SharedPreferences.Editor editor_Variables;

    @Override
    public void onReceive(Context context, Intent intent) {
        contexto = context;
        preferencias_Variables = context.getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE);
        editor_Variables = context.getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE).edit();

        origen = intent.getStringExtra("ENVIAR_ORIGEN");
        destino = intent.getStringExtra("ENVIAR_DESTINO");
        llave = intent.getStringExtra("ENVIAR_LLAVE");
        i_HoraI = intent.getIntExtra("ENVIAR_HORAI",0);
        i_MinutosI = intent.getIntExtra("ENVIAR_MINUTOSI",0);

        servicio_activo = preferencias_Variables.getBoolean("servicio_activo",false);

//        Log.e(TAG,servicio_activo + "");
        //SERVICIO
        if (servicio_activo){
            bool_Servicio = constantes.servicioActivo(contexto);
            if (!bool_Servicio) {
                iServicio = new Intent(context, ServicioUbicacion.class);
                iServicio.putExtra("ENVIAR_ORIGEN", origen);
                iServicio.putExtra("ENVIAR_DESTINO", destino);
                iServicio.putExtra("ENVIAR_LLAVE", llave);
                iServicio.putExtra("ENVIAR_HORAI", i_HoraI);
                iServicio.putExtra("ENVIAR_MINUTOSI", i_MinutosI);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
                    context.startForegroundService(iServicio);
                else
                    context.startService(iServicio);
            }

        }

    }

}
