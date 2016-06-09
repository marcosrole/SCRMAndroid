package devazt.networking;

/**
 * Created by Neksze on 01/10/2015.
 */
public class Response {

    private String result;
    private boolean success;
    private  int idResponse;
    private String description="";

    public Response(String result, boolean success, int idRespose, String description) {
        this.result = result;
        this.success = success;
        this.idResponse = idRespose;
        this.description = description;
    }

    public String getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getIdResponse() { return idResponse; }

    public String getDescription() {
        return description;
    }

}
