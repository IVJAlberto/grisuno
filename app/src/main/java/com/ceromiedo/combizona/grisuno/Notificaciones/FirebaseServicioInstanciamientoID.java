package com.ceromiedo.combizona.grisuno.Notificaciones;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseServicioInstanciamientoID extends FirebaseInstanceIdService {
    String refreshedToken;

    @Override
    public void onTokenRefresh() {
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
    }

}
