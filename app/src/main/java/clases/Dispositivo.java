package clases;

/**
 * Created by marco_000 on 10/05/2016.
 */
public class Dispositivo {
    private String id;
    private String MAC;
    private String modelo;
    private String version;
    private String funciona;
    private String tiempo;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getFunciona() {
        return funciona;
    }

    public String getMAC() {
        return MAC;
    }

    public String getModelo() {
        return modelo;
    }

    public String getTiempo() {
        return tiempo;
    }

    public String getVersion() {
        return version;
    }

    public void setFunciona(String funciona) {
        this.funciona = funciona;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
