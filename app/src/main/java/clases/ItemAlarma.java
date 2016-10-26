package clases;

/**
 * Created by Administrador on 20/10/2016.
 */

public class ItemAlarma {
    long id;
    String linea_a;
    String linea_b;
    String linea_c;
    String linea_d;
    String linea_e;
    String id_dis;
    String id_AsiIns;

    String rutaImagen;

    Boolean alarmaTomada=false;


    public String getId_dis() {
        return id_dis;
    }

    public void setId_dis(String id_dis) {
        this.id_dis = id_dis;
    }

    public ItemAlarma(){
        this.linea_a="";
        this.linea_b="";
        this.linea_c="";
        this.linea_d="";
        this.linea_e="";
        this.rutaImagen="";
    }

    public String getId_AsiIns() {
        return id_AsiIns;
    }

    public void setId_AsiIns(String id_AsiIns) {
        this.id_AsiIns = id_AsiIns;
    }

    public ItemAlarma(long id, String lineaA, String lineaB, String lineaC, String lineaD, String lineaE){
        this.linea_a=lineaA;
        this.linea_b=lineaB;
        this.linea_c=lineaC;
        this.linea_d=lineaD;
        this.linea_e=lineaE;
        this.id=id;


    }

    public ItemAlarma(long id, String lineaA, String lineaB, String lineaC, String lineaD, String lineaE, String rutaImgen, String id_dis,String id_AsiIns){
        this.linea_a=lineaA;
        this.linea_b=lineaB;
        this.linea_c=lineaC;
        this.linea_d=lineaD;
        this.linea_e=lineaE;
        this.id=id;
        this.rutaImagen=rutaImgen;
        this.id_dis=id_dis;
        this.id_AsiIns=id_AsiIns;
    }

    public ItemAlarma(long id, String lineaA, String lineaB, String lineaC, String lineaD, String lineaE, String rutaImgen, String id_dis,String id_AsiIns, String alarmaTomada){
        this.linea_a=lineaA;
        this.linea_b=lineaB;
        this.linea_c=lineaC;
        this.linea_d=lineaD;
        this.linea_e=lineaE;
        this.id=id;
        this.rutaImagen=rutaImgen;
        this.id_dis=id_dis;
        this.id_AsiIns=id_AsiIns;
        if(alarmaTomada.equals("1")){
            this.alarmaTomada=true;
        }else this.alarmaTomada=false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLinea_a() {
        return linea_a;
    }

    public void setLinea_a(String linea_a) {
        this.linea_a = linea_a;
    }

    public String getLinea_b() {
        return linea_b;
    }

    public void setLinea_b(String linea_b) {
        this.linea_b = linea_b;
    }

    public String getLinea_c() {
        return linea_c;
    }

    public void setLinea_c(String linea_c) {
        this.linea_c = linea_c;
    }

    public String getLinea_d() {
        return linea_d;
    }

    public void setLinea_d(String linea_d) {
        this.linea_d = linea_d;
    }

    public String getLinea_e() {
        return linea_e;
    }

    public void setLinea_e(String linea_e) {
        this.linea_e = linea_e;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public Boolean getAlarmaTomada() {
        return alarmaTomada;
    }

    public void setAlarmaTomada(Boolean alarmaTomada) {
        this.alarmaTomada = alarmaTomada;
    }
}
