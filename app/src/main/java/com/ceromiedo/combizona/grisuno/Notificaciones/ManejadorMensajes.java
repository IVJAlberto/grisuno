package com.ceromiedo.combizona.grisuno.Notificaciones;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ManejadorMensajes extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage == null)
            return;

        String titulo = remoteMessage.getNotification().getTitle();
        String cuerpo = remoteMessage.getNotification().getBody();

        ManejadorNotificaciones.getMiInstancia(getApplicationContext())
                .mostrar_Notificacion(titulo,cuerpo);

//        if (remoteMessage.getNotification() != null){
//            Intent empujar_Notificacion = new Intent(this, Usuario_Recorrido.class);
//            empujar_Notificacion.setAction(Constantes.INTENT_NOTIFICACION);
//            empujar_Notificacion.putExtra("titulo",titulo);
//            LocalBroadcastManager.getInstance(this).sendBroadcast(empujar_Notificacion);
//        }
    }

}