package com.ceromiedo.combizona.grisuno.Noticias.Objeto;

public class ObjetoNoticia {
    private  String titulo, cuerpo, fecha;

    public ObjetoNoticia(String titulo, String cuerpo, String fecha){
        this.titulo = titulo;
        this.cuerpo = cuerpo;
        this.fecha = fecha;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
