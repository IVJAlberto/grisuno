package com.ceromiedo.combizona.grisuno.Actividades;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.ceromiedo.combizona.grisuno.Constantes;
import com.ceromiedo.combizona.grisuno.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Inicio_Sesion  extends AppCompatActivity {

    //PREFERENCIAS
    public SharedPreferences preferencias_Variables;
    public SharedPreferences.Editor editor_Variables;
    private String TAG = "Inicio Sesión";

    //XML
    TextView tv_IniciarSesion, tv_Version;
    CardView cv_IniciarSesion, cv_Version;
    ProgressBar pb_IniciarSesion, pb_Version;
    private ImageView ibtn_MostrarPass;
    EditText et_Usuario, et_Contrasena;
    String uid,bd_Usuario, bd_Contrasena, bool_Recordar = "false";
    Boolean hayInternet = false, b_MostrarPass = false;

    //BASE DATOS
    FirebaseFirestore ff_BaseDatos;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference pathRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Inicio_Sesion);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_actividad_ingreso);

        f_Verificar_Actividad_Usuario();

        preferencias_Variables = getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE);
        editor_Variables = getSharedPreferences(Constantes.PREFERENCIAS_S, MODE_PRIVATE).edit();
        ff_BaseDatos = FirebaseFirestore.getInstance();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Inicio_Sesion.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
            }
        }

        if(!((Inicio_Sesion.this).isFinishing()))
            f_Comprobar_Intents();
        f_XmlToAndroid();
        f_Botones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        et_Usuario.setText(preferencias_Variables.getString("user",""));
        et_Contrasena.setText(preferencias_Variables.getString("contrasena",""));
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor_Variables.putString("user", et_Usuario.getText().toString());
        editor_Variables.putString("contrasena", et_Contrasena.getText().toString());
        editor_Variables.commit();
    }

    @Override
    public void onBackPressed() {
        //
    }

    //ASYNCTASK

    private class asyt_Conexion extends AsyncTask<String,String, Boolean> {
        ProgressDialog dialogo;
        String str;
        HttpURLConnection urlc;
        @Override
        protected void onPreExecute() {
//            if(!((Inicio_Sesion.this).isFinishing())){
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//                    dialogo = ProgressDialog.show(Inicio_Sesion.this,"¡Analizando!" , "Comprobando conexión a internet...");
//                }else{
//                    dialogo = new ProgressDialog(Inicio_Sesion.this, R.style.DialogoProgresivo);
//                    f_Dialogo(dialogo,"¡Analizando!","Comprobando conexión a internet...");
//                }
//            }
        }
        @Override
        protected Boolean doInBackground(String... params) {
            str = params[0];
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
                }catch (Exception e){}finally {
                    urlc.disconnect();
                }
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean bool) {
            if(!((Inicio_Sesion.this).isFinishing()) && dialogo!=null)
                dialogo.dismiss();

            if (urlc != null)
                urlc.disconnect();

            if(bool){
                hayInternet = true;
                if(str.equals("version"))
                    new asyt_Version().execute();
                else if(str.equals("ingresar"))
                    new asyt_ComprobandoDatones().execute();
            }else {
                hayInternet = false;
                f_Toast("No hay conexión a internet", "error", 0);
                if (!str.equals("null"))
                    cv_Version.setClickable(true);
                pb_IniciarSesion.setVisibility(View.GONE);
                tv_IniciarSesion.setVisibility(View.VISIBLE);
                pb_Version.setVisibility(View.GONE);
                tv_Version.setVisibility(View.VISIBLE);
            }
        }
    }

    private class asyt_ComprobandoDatones extends AsyncTask<String, String, String>{
        ProgressDialog dialogo;
        boolean bool;

        @Override
        protected void onPreExecute() {
//            if(!((Inicio_Sesion.this).isFinishing())){
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//                    dialogo = ProgressDialog.show(Inicio_Sesion.this,"¡Comprobando!" , "Analizando datos de ingreso...");
//                }else {
//                    dialogo = new ProgressDialog(Inicio_Sesion.this, R.style.DialogoProgresivo);
//                    f_Dialogo(dialogo, "¡Comprobando!", "Analizando datos de ingreso...");
//                }
//            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if ((et_Usuario.getText().toString().trim().length() == 0) || et_Contrasena.getText().toString().trim().length() == 0) {
                f_Toast("Ingrese los campos correctamente!", "error", 0);
                pb_IniciarSesion.setVisibility(View.GONE);
                tv_IniciarSesion.setVisibility(View.VISIBLE);
                cv_Version.setClickable(true);
            }else
                f_Verificar_Ingreso(et_Usuario.getText().toString().toLowerCase(), et_Contrasena.getText().toString());
            if(!((Inicio_Sesion.this).isFinishing())  && dialogo!=null)
                dialogo.dismiss();
        }
    }

    private class asyt_Ingreso extends AsyncTask<String, String, String> {
        ProgressDialog dialogo;
        boolean val_Bool;
        String val_Str;

        public asyt_Ingreso(boolean bool, String str) {
            this.val_Bool = bool;
            this.val_Str = str;
        }

        @Override
        protected void onPreExecute() {
//            if(!((Inicio_Sesion.this).isFinishing())){
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//                    dialogo = ProgressDialog.show(Inicio_Sesion.this,"¡Iniciando sesión!" , "Conectando a CombiZona database...");
//                }else {
//                    dialogo = new ProgressDialog(Inicio_Sesion.this, R.style.DialogoProgresivo);
//                    f_Dialogo(dialogo, "¡Iniciando sesión!", "Conectando a CombiZona database...");
//                }
//            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(500);
                f_DescargarImagen(val_Str);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!((Inicio_Sesion.this).isFinishing())  && dialogo!=null)
                dialogo.dismiss();
            if (val_Bool) {
                FirebaseMessaging.getInstance().subscribeToTopic(bd_Usuario);
                f_Toast("Sesión iniciada correctamente.", "user", 0);
                cv_IniciarSesion.setEnabled(false);
                Handler esperar_Dialogo = new Handler();
                esperar_Dialogo.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cv_IniciarSesion.setEnabled(true);
                        Intent i_Vista_Usuario = new Intent(Inicio_Sesion.this, Usuario_Recorrido.class);
                        startActivity(i_Vista_Usuario);
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
                            overridePendingTransition(R.anim.anim_mover_arriba,R.anim.anim_mover_abajo);
                    }
                }, 2300);
            } else {
                if (val_Str.equals("activo"))
                    f_Toast("Usuario actualmente activo", "activo", 0);
                else if (val_Str.equals("error"))
                    f_Toast("¡Compruebe usuario/contraseña!", "error", 0);
            }
            pb_IniciarSesion.setVisibility(View.GONE);
            tv_IniciarSesion.setVisibility(View.VISIBLE);
            cv_Version.setClickable(true);
        }

    }

    private class asyt_Version extends AsyncTask<String, String, String>{
        ProgressDialog dialogo;

        @Override
        protected void onPreExecute() {
//            if(!((Inicio_Sesion.this).isFinishing())){
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//                    dialogo = ProgressDialog.show(Inicio_Sesion.this,"¡Versión!" , "Comprobando última versión...");
//                }else {
//                    dialogo = new ProgressDialog(Inicio_Sesion.this, R.style.DialogoProgresivo);
//                    f_Dialogo(dialogo, "Versión", "Comprobando última versión...");
//                }
//            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
//            if(!((Inicio_Sesion.this).isFinishing())  && dialogo!=null)
//                dialogo.dismiss();
            DocumentReference ref_Version = ff_BaseDatos.collection("version").document("version");
            ref_Version.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot documento = task.getResult();
                        if (documento.get("Version").equals(tv_Version.getText().toString()))
                            f_Toast("Aplicación actualizada!", "actualizado", 0);
                        else {
                            f_Toast("Aplicación desactualizada!", "descargar", 0);
                            Handler esperar = new Handler();
                            esperar.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder alertaActualizar;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                        alertaActualizar = new AlertDialog.Builder(Inicio_Sesion.this, R.style.DialogoEleccion);
                                    else
                                        alertaActualizar = new AlertDialog.Builder(Inicio_Sesion.this, R.style.DialogoEleccion_Transparente);
                                    alertaActualizar.setCancelable(true);
                                    alertaActualizar.setTitle("Nueva versión " + documento.get("Version"));
                                    alertaActualizar.setMessage("¿Desea actualizar la aplicación?");
                                    alertaActualizar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Handler esperarDescarga = new Handler();
                                            esperarDescarga.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Intent iAppStore = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constantes.PACKAGE_NAME));
                                                        iAppStore.setPackage("com.android.vending");
                                                        startActivity(iAppStore);
                                                    } catch (android.content.ActivityNotFoundException e) {
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Constantes.PACKAGE_NAME)));
                                                    }

                                                }
                                            }, 2000);
                                        }
                                    });
                                    alertaActualizar.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });

                                    if (!((Inicio_Sesion.this).isFinishing())) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                            alertaActualizar.show();
                                        else {
                                            Dialog dialogo = alertaActualizar.show();
                                            int lineaID = dialogo.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
                                            View linea = dialogo.findViewById(lineaID);
                                            Resources recursos = dialogo.getContext().getResources();
                                            int color = recursos.getColor(R.color.colorAccent);
                                            linea.setBackgroundColor(color);
                                        }
                                    }
                                }
                            }, 3000);
                        }
                        pb_Version.setVisibility(View.GONE);
                        tv_Version.setVisibility(View.VISIBLE);
                    }
//                    }else{
//                        pb_Version.setVisibility(View.GONE);
//                        tv_Version.setVisibility(View.VISIBLE);
//                    }
                }
            });
            cv_Version.setClickable(true);
        }
    }

    //FUNCIONES

    public void f_Botones(){

        cv_IniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_IniciarSesion.setVisibility(View.GONE);
                pb_IniciarSesion.setVisibility(View.VISIBLE);
                new asyt_Conexion().execute("ingresar");
            }
        });


        cv_Version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb_Version.setVisibility(View.VISIBLE);
                tv_Version.setVisibility(View.GONE);
                new asyt_Conexion().execute("version");
            }
        });

        ibtn_MostrarPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (b_MostrarPass) {
                    et_Contrasena.setTransformationMethod(new PasswordTransformationMethod());
                    ibtn_MostrarPass.setImageResource(R.drawable.ic_turn_visibility_off);
                }else{
                    et_Contrasena.setTransformationMethod(null);
                    ibtn_MostrarPass.setImageResource(R.drawable.ic_turn_visibility_on);
                }
                b_MostrarPass ^= true;
            }
        });

    }
    private void f_Comprobar_Intents(){
        boolean b_PasarMensaje = preferencias_Variables.getBoolean("Pasar_Apps_Protegidas",false);
        AlertDialog.Builder alerta_AppsProtect;

        if (!b_PasarMensaje){
            boolean b_Encontrado_Intent_Correcto = false;
            for (final Intent i : Constantes.POWERMANAGER_INTENTS){
                if (f_SePuedeLlamar(this,i)){
                    b_Encontrado_Intent_Correcto = true;

                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                        alerta_AppsProtect = new AlertDialog.Builder(this, R.style.DialogoEleccion);
                    else
                        alerta_AppsProtect = new AlertDialog.Builder(this, R.style.DialogoEleccion_Transparente);
                    alerta_AppsProtect.setCancelable(true);
                    alerta_AppsProtect.setTitle(Build.MANUFACTURER + " Apps Protegidas");
                    alerta_AppsProtect.setMessage("CombiZona: OroVerde requiere ser habilitada para trabajar de forma correcta.");

                    alerta_AppsProtect.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int c) {
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(i);
                            editor_Variables.putBoolean("Pasar_Apps_Protegidas", true);
                            editor_Variables.apply();
                        }
                    });
                    alerta_AppsProtect.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    if(!((this).isFinishing())){
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                            alerta_AppsProtect.show();
                        else{
                            Dialog dialogo = alerta_AppsProtect.show();
                            int lineaID = dialogo.getContext().getResources().getIdentifier("android:id/titleDivider",null,null);
                            View linea = dialogo.findViewById(lineaID);
                            Resources recursos = dialogo.getContext().getResources();
                            int color = recursos.getColor(R.color.colorAccent);
                            linea.setBackgroundColor(color);
                        }
                    }
                }
            }
            if (!b_Encontrado_Intent_Correcto){
                editor_Variables.putBoolean("Pasar_Apps_Protegidas", true);
                editor_Variables.apply();
            }
        }
    }

    private File f_CrearTempFile(String prefix, String suffix) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = (prefix != null ? prefix : "") + (suffix != null ? suffix : "");
        return new File(tempDir, fileName);
    }
    private void f_DescargarImagen(String imagen) {
        pathRef = storageRef.child("/Usuarios/Imagenes/"+imagen);
        String camino_cache = getApplicationContext().getCacheDir().getAbsolutePath() + "/";
        File file = null;
        if (!imagen.equals(""))
            file = new File(camino_cache + imagen);

        if (!file.exists()){
            String si = imagen;
            String[] separar = si.split("\\.");
            try {
                pathRef.getFile(f_CrearTempFile(separar[0], "." + separar[1])).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }catch (Exception e){}
        }
    }
    public void f_Dialogo(ProgressDialog dialogo, String str_titulo, String str_cuerpo){
        dialogo.setTitle(str_titulo);
        dialogo.setMessage(str_cuerpo);
        if(!((Inicio_Sesion.this).isFinishing()))
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
    private static boolean f_SePuedeLlamar(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    public void f_Toast(String mensaje, String nombreImagen, int duracion) {
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
        if(!((Inicio_Sesion.this).isFinishing()))
            toast.show();
    }
    private void f_Verificar_Actividad_Usuario(){
        String usuario_ID = FirebaseAuth.getInstance().getUid();
        if(usuario_ID != null)
            startActivity( new Intent(this, Usuario_Recorrido.class));

    }
    private void f_Verificar_Ingreso(final String usuario, final String contrasena) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(usuario + "@gmail.com", contrasena)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    uid = FirebaseAuth.getInstance().getUid();
                    editor_Variables.putString("usuario_ID",uid);
                    editor_Variables.commit();

                    //CHECAR SI ESTA DISPONIBLE EL USUARIO
                    ff_BaseDatos.collection("usuarios").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documento) {
                            if (documento.exists()) {
                                if (!documento.getBoolean("Activo")) {
                                    bd_Usuario = et_Usuario.getText().toString();
                                    bd_Contrasena = et_Contrasena.getText().toString();
                                    editor_Variables.putBoolean("pp",documento.getBoolean("PP"));
                                    editor_Variables.putString("nombre", documento.getString("Nombre") + "");
                                    editor_Variables.putString("correo", documento.getString("Correo"));
                                    editor_Variables.putString("imagen", documento.getString("Foto"));
                                    editor_Variables.putString("user", et_Usuario.getText().toString());
                                    editor_Variables.putString("contrasena", et_Contrasena.getText().toString());
                                    editor_Variables.putString("usuario_ID", uid);
                                    editor_Variables.putString("versionAPK", tv_Version.getText().toString());
                                    editor_Variables.putBoolean("primeraVez",true);

                                    editor_Variables.commit();

                                    new asyt_Ingreso(true, documento.getString("Foto")).execute();

                                } else
                                    new asyt_Ingreso(false, "activo").execute();

                            }
                        }
                    });
                } else new asyt_Ingreso(false, "error").execute();
            }
        });
    }
    public void f_XmlToAndroid(){
        cv_IniciarSesion = findViewById(R.id.cv_IniciarSesion);
        tv_IniciarSesion = findViewById(R.id.tv_IniciarSesion);
        pb_IniciarSesion = findViewById(R.id.pb_IniciarSesion);
        cv_Version = findViewById(R.id.cv_Version);
        tv_Version = findViewById(R.id.tv_Version);
        pb_Version = findViewById(R.id.pb_Version);
        et_Usuario =  findViewById(R.id.etUsuario);
        et_Contrasena =  findViewById(R.id.etContrasena);
        ibtn_MostrarPass = findViewById(R.id.ibtn_MostrarPass);
    }
}
