package com.ceromiedo.combizona.grisuno;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ceromiedo.combizona.grisuno.Servicio.ServicioUbicacion;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Constantes {
    //NOTIFICACION O
    public static final String INTENT_NOTIFICACION = "empujar_Notificacion";
    public static final String INTENT_ACCIONES_MENU = "empujar_Accion";
    public static final String CANAL_ID = "canalID";
    public static final String CANAL_NOMBRE = "canalNombre";
    public static final String CANAL_DESCRIPCION= "canalDescripcion";

    //CACHE
    public static final String PREFERENCIAS_S = "MiArchivoPrefs";
    public static final String PACKAGE_NAME = "com.ceromiedo.combizona.oroverde";

    //GEOFENCE
    public static final float GEOFENCE_RADIUS_IN_METERS = 100;
    public static final int CODIGO_PERMISO = 2601;
    public static final int PLAY_SERVICES_RESPUESTA = 1993;
    public static int INTERVALO_ACTUALIZACION = 5000;
    public static int INTERVALO_ACTUALIZACION_RAPIDA = 3000;
    public static int INTERVALO_DESPLAZAMIENTO = 10;

    public Constantes(){
    }

    //FUNCIONES

        public boolean servicioActivo(Context contexto) {
        ActivityManager manager = (ActivityManager)contexto.getSystemService(contexto.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info_Servicio : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ServicioUbicacion.class.getName().equals(info_Servicio.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static final List<Intent> POWERMANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")).setData(Uri.fromParts("package", PACKAGE_NAME, null)),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"))
                    .setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart")),
            new Intent().setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.SHOW_APPSEC")).addCategory(Intent.CATEGORY_DEFAULT).putExtra("packageName", BuildConfig.APPLICATION_ID)
    );

    public static final HashMap<String, LatLng> PUNTOS_CHECADA = new HashMap<String, LatLng>();

    static {
        // stanford university.
        PUNTOS_CHECADA.put("0Miedo", new LatLng(19.68476206640669, -101.17747860155006));
        PUNTOS_CHECADA.put("La Estrella", new LatLng(19.686232, -101.181204));
    }

}