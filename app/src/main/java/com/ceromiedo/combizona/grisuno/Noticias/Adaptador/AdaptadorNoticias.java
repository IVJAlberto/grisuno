package com.ceromiedo.combizona.grisuno.Noticias.Adaptador;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ceromiedo.combizona.grisuno.Noticias.Objeto.ObjetoNoticia;
import com.ceromiedo.combizona.grisuno.R;

import java.util.List;

class ObjetoViewHolder extends RecyclerView.ViewHolder{

    public TextView titulo, fecha;
    public ImageView icono;

    public ObjetoViewHolder(View itemView) {
        super(itemView);
        titulo = (TextView)itemView.findViewById(R.id.tv_NotiTitulo);
        titulo.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        fecha = (TextView)itemView.findViewById(R.id.tv_DiaNotiFecha);
        icono = (ImageView)itemView.findViewById(R.id.iv_DiaNotiIcono);
    }
}

public class AdaptadorNoticias extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_OBJETO=0, VIEW_TYPE_CARGAR=1;
    Activity actividad;
    List<ObjetoNoticia> objetos;
    int verUltimosObjetos, totalObjetos;

    public AdaptadorNoticias(RecyclerView recyclerView, Activity activity, List<ObjetoNoticia> items){
        this.actividad = activity;
        this.objetos = items;
        final LinearLayoutManager llManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalObjetos = llManager.getItemCount();
                verUltimosObjetos = llManager.findLastVisibleItemPosition();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return objetos.get(position) == null ? VIEW_TYPE_CARGAR:VIEW_TYPE_OBJETO;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_OBJETO){
            View view = LayoutInflater.from(actividad).inflate(R.layout.layout_diseno_objetonoticia,parent,false);
            return new ObjetoViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if(holder instanceof ObjetoViewHolder){
            ObjetoViewHolder obj_ViewHolder = (ObjetoViewHolder) holder;
            obj_ViewHolder.titulo.setText(objetos.get(position).getTitulo());
            obj_ViewHolder.fecha.setText(objetos.get(position).getFecha());
            if(objetos.get(position).getTitulo().contains("Actualización")||objetos.get(position).getTitulo().contains("actualización"))
                obj_ViewHolder.icono.setImageResource(R.drawable.ic_descargar);
            else
                obj_ViewHolder.icono.setImageResource(R.drawable.ic_news);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertaAtras;
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                    alertaAtras = new AlertDialog.Builder(actividad,R.style.DialogoReloj);
                else
                    alertaAtras = new AlertDialog.Builder(actividad,R.style.DialogoRelojTransparente);
                alertaAtras.setCancelable(true);
                alertaAtras.setTitle(objetos.get(position).getTitulo()+"          "+objetos.get(position).getFecha());
                alertaAtras.setMessage("\n"+objetos.get(position).getCuerpo());
                alertaAtras.setNegativeButton("Atras", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                    alertaAtras.show();
                else{
                    Dialog dialogo = alertaAtras.show();
                    int lineaID = dialogo.getContext().getResources().getIdentifier("android:id/titleDivider",null,null);
                    View linea = dialogo.findViewById(lineaID);
                    Resources recursos = dialogo.getContext().getResources();
                    int color = recursos.getColor(R.color.colorAccent);
                    linea.setBackgroundColor(color);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return objetos.size();
    }

}
