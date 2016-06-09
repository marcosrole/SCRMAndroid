package clases;

/**
 * Created by marco_000 on 10/05/2016.
 */
public class Usuario {
    private String id;
    private String name;
    private String pass;
    private String DNI_per;

    public String getName() {
        return name;
    }

    public String getDNI_per() {
        return DNI_per;
    }

    public String getId() {
        return id;
    }

    public String getPass() {
        return pass;
    }

    public void setDNI_per(String DNI_per) {
        this.DNI_per = DNI_per;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPass(String pass) {
        this.pass = pass;
    }
}
