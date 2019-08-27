package com.ceromiedo.combizona.grisuno.Recibidores;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Cambio_Hora extends BroadcastReceiver {
    String nombreRecorrido, hora;
    DateFormat formato_HMS = new SimpleDateFormat("HH:mm:ss");

    public Cambio_Hora(String nombre){
        nombreRecorrido = nombre;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        hora = formato_HMS.format(Calendar.getInstance().getTime());
        f_AgregarTexto(nombreRecorrido, "CH" , hora);
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
}
