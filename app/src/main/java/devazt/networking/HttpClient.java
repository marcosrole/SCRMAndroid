package devazt.networking;

import android.content.Context;
import android.net.ConnectivityManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Neksze on 30/09/2015.
 */
public class HttpClient {
    private Context context;
    private ConnectivityManager connManager;
    private ArrayList<OnHttpRequestComplete> httpRequestComplete;

    public HttpClient(OnHttpRequestComplete o) {
        httpRequestComplete = new ArrayList<>();
        httpRequestComplete.add(o);
    }

    /**
     * Este método hace una solicitud web a la url enviada como parametro, y al terminar, se ejecuta el
     * listener OnHttpRequestComplete [seteado al instanciar clase]
     * @param urlHttp url para request
     */
    public void excecute(String urlHttp){

        Response r = getResponse(urlHttp);
        for (OnHttpRequestComplete http : httpRequestComplete) {
            http.onComplete(r);
        }

    }

    /**
     * Este método hace una solicitud web a la url enviada como parametro, y al terminar, se ejecuta el
     * listener OnHttpRequestComplete [seteado al instanciar clase]
     * @param urlHttp url para request
     * @param idResponse id para la respuesta
     */
    public void excecute(String urlHttp, int idResponse){
        Response r = getResponse(urlHttp);
        Response response = new Response(r.getResult(),r.isSuccess(),idResponse,"");
        for (OnHttpRequestComplete http : httpRequestComplete) {
            http.onComplete(response);
        }
    }

    private Response getResponse(final String urlHttp){
        boolean a = isConnectedToServer(urlHttp,15000);

        AndroidHelper.AddStrictMode(); // Para versiones superiores a SDK 9
        String text = "";
        String error_description = "";
        boolean success = false;
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urlHttp);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader isw = new InputStreamReader(in);
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                text += String.valueOf(current);
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            error_description="Error al intentar acceder a " + urlHttp;
        } finally {
            try {
                urlConnection.disconnect();
            } catch (Exception e) {
                success = false;
            }
        }

        return new Response(text,success,0,error_description);
    }

    public boolean isConnectedToServer(String url, int timeout) {
        try{
            URL testUrl = new URL("http://google.com");
            StringBuilder answer = new StringBuilder(100000);

            long start = System.nanoTime();

            URLConnection testConnection = testUrl.openConnection();
            testConnection.setConnectTimeout(timeout);
            testConnection.setReadTimeout(timeout);
            BufferedReader in = new BufferedReader(new InputStreamReader(testConnection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                answer.append(inputLine);
                answer.append("");
            }
            in.close();

            long elapsed = System.nanoTime() - start;
            System.out.println("Elapsed (ms): " + elapsed / 1000000);
            System.out.println("Answer:");
            System.out.println(answer);
            return true;
        } catch (Exception e) {
            // Handle your exceptions
            return false;
        }
    }

}
