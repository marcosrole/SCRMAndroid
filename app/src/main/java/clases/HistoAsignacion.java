package clases;

/**
 * Created by marco_000 on 11/05/2016.
 */
public class HistoAsignacion {
    private String id;
    private String fechaAlta;
    private String fechaBaja;
    private String fechaModif;
    private String coordLat;
    private String coordLon;
    private String observacion;
    private String id_dis;
    private String id_suc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_dis() {
        return id_dis;
    }

    public void setId_dis(String id_dis) {
        this.id_dis = id_dis;
    }
}
