package com.ceromiedo.combizona.grisuno.Notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.ceromiedo.combizona.grisuno.Actividades.Usuario_Recorrido;
import com.ceromiedo.combizona.grisuno.R;

import java.util.Date;

public class ManejadorNotificaciones {

    private Context miCtx;
    private static ManejadorNotificaciones miInstancia;

    public ManejadorNotificaciones(Context contexto){
        miCtx = contexto;
    }

    public static synchronized ManejadorNotificaciones getMiInstancia(Context contexto){
        if(miInstancia == null){
            miInstancia = new ManejadorNotificaciones(contexto);
        }
        return miInstancia;
    }

    public void mostrar_Notificacion(String titulo, String cuerpo){

        NotificationManager manejadorNotificaciones = (NotificationManager) miCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(miCtx, Usuario_Recorrido.class);
        PendingIntent piNotificacion = PendingIntent.getActivity(miCtx,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap iconoLargo = BitmapFactory.decodeResource(Resources.getSystem(),R.mipmap.ic_launcher_round);
        RemoteViews notificacionView1 = new RemoteViews(R.class.getPackage().getName(), R.layout.layout_diseno_notificacion_xml);

        String canalID = "Canal Notificacion de Firebase";
        Uri sonidoNotificacion = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder miBuilder = new NotificationCompat.Builder(miCtx, canalID)
                .setSmallIcon(R.drawable.icono_combizona)
                    .setContentTitle(titulo)
                    .setContentText(cuerpo)
                    .setAutoCancel(true)
                    .setSound(sonidoNotificacion)
                    .setColor(ContextCompat.getColor(miCtx,R.color.colorPrimary))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(cuerpo))
                    .setContentIntent(piNotificacion);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel canal = new NotificationChannel(canalID, "Canal Oreos+", NotificationManager.IMPORTANCE_DEFAULT);
            manejadorNotificaciones.createNotificationChannel(canal);
        }

        manejadorNotificaciones.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE),miBuilder.build());


    }
}