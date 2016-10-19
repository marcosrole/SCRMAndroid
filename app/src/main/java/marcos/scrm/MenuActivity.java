package marcos.scrm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import devazt.networking.ConfigServidor;
import devazt.networking.HttpClient;
import devazt.networking.OnHttpRequestComplete;
import devazt.networking.Response;
import util.General;

public class MenuActivity extends AppCompatActivity {
    private Button btnSalir;
    private Button btnCalibrar;
    private Button btnAsignar;
    private Button btnDispositivo;
    private Button btnAlarma;
    private Button btnActa;
    private final String TAG_LOG = "test";
    General archivoTXT = new General();
    String id_ins;
    boolean generarNotificacion = false;


    // Variables de la notificacion
    private NotificationManager nm;
    private static final int ID_NOTIFICACION_CREAR = 1;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        btnSalir = (Button) findViewById(R.id.btnSalir);
        btnCalibrar = (Button) findViewById(R.id.btnCalibrar);
        //btnAsignar = (Button) findViewById(R.id.btnAsignar);
        btnDispositivo = (Button) findViewById(R.id.btnDispositivo);
        btnAlarma = (Button) findViewById(R.id.btnAlarma);
        btnActa = (Button) findViewById(R.id.btnActas);

       /* btnAsignar.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(MenuActivity.this, AsignarActivity.class);
                startActivity(i);
            }
        });*/
        btnAlarma.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(MenuActivity.this, AlarmaActivity.class);
                startActivity(i);
            }
        });
        btnActa.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(MenuActivity.this, pickActaActivity.class);
                startActivity(i);
            }
        });


        DescargarImagenesDeInternetEnOtroHilo miTareaAsincrona = new DescargarImagenesDeInternetEnOtroHilo(true);
        miTareaAsincrona.execute();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Menu Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        AppIndex.AppIndexApi.start(client2, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client2, getIndexApiAction());
        client2.disconnect();
    }

    private class DescargarImagenesDeInternetEnOtroHilo extends AsyncTask<String, Float, Integer> {
        private boolean cancelarSiHayMas100Archivos;
        private ProgressBar miBarraDeProgreso;

        /**
         * Contructor de ejemplo que podemos crear en el AsyncTask
         *
         * @param //en este ejemplo le pasamos un booleano que indica si hay más de 100 archivos o no. Si le pasas true se cancela por la mitad del progreso, si le pasas false seguirá hasta el final sin cancelar la descarga simulada
         */
        public DescargarImagenesDeInternetEnOtroHilo(boolean cancelarSiHayMas100Archivos) {
            this.cancelarSiHayMas100Archivos = cancelarSiHayMas100Archivos;
        }

        /**
         * Se ejecuta antes de empezar el hilo en segundo plano. Después de este se ejecuta el método "doInBackground" en Segundo Plano
         * <p>
         * Se ejecuta en el hilo: PRINCIPAL
         */
        @Override
        protected void onPreExecute() {
            Log.v(TAG_LOG, "onPreExecute: Obtengo el ID del inspector");
        }

        /**
         * Se ejecuta después de "onPreExecute". Se puede llamar al hilo Principal con el método "publishProgress" que ejecuta el método "onProgressUpdate" en hilo Principal
         * <p>
         * Se ejecuta en el hilo: EN SEGUNDO PLANO
         *
         * @param //array con los valores pasados en "execute"
         * @return devuelve un valor al terminar de ejecutar este segundo plano. Se lo envía y ejecuta "onPostExecute" si ha termiado, o a "onCancelled" si se ha cancelado con "cancel"
         */
        @Override
        protected Integer doInBackground(String... variableNoUsada) {

            archivoTXT.recuperar_id_ins("attributes_usr.txt");
            id_ins = archivoTXT.getId_ins();

            //Verifico si existe alarma. SI EXISTE: Muestro la notificacion y espero 5 minutos
            //Si la alarma no ha sido tomada vuelvo a mostrar una notificacion.

            while (!isCancelled()) {
                Log.v(TAG_LOG, "doInBackgorund: SEGUNDO PLANO");
                try {
                    int seconds = 3;
                    //Simula el tiempo aleatorio de descargar una imagen, al dormir unos milisegundos aleatorios al hilo en segundo plano
                    Thread.sleep((long) seconds * 1000);
                } catch (InterruptedException e) {
                    cancel(true); //Cancelamos si entramos al catch porque algo ha ido mal
                    e.printStackTrace();
                }
                verificarAlarma();

                if (generarNotificacion) {
                    //Genero una notifiacion.
                    publishProgress(1.0f); //1.0 = SI ; 0.0 = NO

                    int seconds2 = 60*5;
                    //Simula el tiempo aleatorio de descargar una imagen, al dormir unos milisegundos aleatorios al hilo en segundo plano
                    try {
                        Thread.sleep((long) seconds2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }


            /*
            int cantidadImagenesDescargadas = 0;
            float progreso = 0.0f;

            //Suponemos que tenemos 200 imágenes en algún lado de Internet. isCancelled() comprueba si hemos cancelado con cancel() el hilo en segundo plano.
            while (!isCancelled() && cantidadImagenesDescargadas<200){
                cantidadImagenesDescargadas++;
                Log.v(TAG_LOG, "Imagen descargada número "+cantidadImagenesDescargadas+". Hilo en SEGUNDO PLANO");

                //Simulamos la descarga de una imagen. Iría aquí el código........................
                try {
                    //Simula el tiempo aleatorio de descargar una imagen, al dormir unos milisegundos aleatorios al hilo en segundo plano
                    Thread.sleep((long) (Math.random()*100));
                } catch (InterruptedException e) {
                    cancel(true); //Cancelamos si entramos al catch porque algo ha ido mal
                    e.printStackTrace();
                }
                //Simulamos la descarga de una imagen. Iría aquí el código........................

                progreso+=0.5;

                //Enviamos el progreso a "onProgressUpdate" para que se lo muestre al usuario, pues en el hilo principal no podemos llamar a nada de la interfaz
                publishProgress(progreso);

                //Si hemos decidido cancelar al pasar de 100 imágenes descargadas entramos aquí.
                if (cancelarSiHayMas100Archivos && cantidadImagenesDescargadas>100){
                    cancel(true);
                }
            }

            return cantidadImagenesDescargadas;
            */
            return 1;
        }

        /**
         * Se ejecuta después de que en "doInBackground" ejecute el método "publishProgress".
         * <p>
         * Se ejecuta en el hilo: PRINCIPAL
         *
         * @param //array con los valores pasados en "publishProgress"
         */
        @Override
        protected void onProgressUpdate(Float... generarNotificacion) {

            //Log.v(TAG_LOG, "Progreso descarga: "+porcentajeProgreso[0]+"%. Hilo PRINCIPAL");
            Log.v(TAG_LOG, "Genero NOtifiacion por alarma");

            if (generarNotificacion[0] > 0) {
                try{
                    Log.v(TAG_LOG, "Renderizando Notificacion");

                    Intent i = new Intent(MenuActivity.this, AlarmaActivity.class);
                    i.putExtra("notificationID", ID_NOTIFICACION_CREAR);

                    PendingIntent pendingIntent = PendingIntent.getActivity(MenuActivity.this, 0, i, 0);
                    NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    CharSequence ticker ="SCRM: Nueva Alarma Asignada";
                    CharSequence contentTitle = "SCRM";
                    CharSequence contentText = "Ver alarma";
                    Notification noti = new NotificationCompat.Builder(MenuActivity.this)
                            .setContentIntent(pendingIntent)
                            .setTicker(ticker)
                            .setContentTitle(contentTitle)
                            .setContentText(contentText)
                            .setSmallIcon(R.drawable.iconoscrm)
                            .setSound(soundUri) //This sets the sound to play
                            .addAction(R.drawable.buttoninfo, ticker, pendingIntent)
                            .setVibrate(new long[] {100, 250, 100, 500})
                            .build();
                    nm.notify(ID_NOTIFICACION_CREAR, noti);


                }catch (Exception e){
                    Log.v(TAG_LOG, "Exception: " + e.toString());
                    cancel(true);
                }

            }

            // miBarraDeProgreso.setProgress( Math.round(porcentajeProgreso[0]) );
        }

        /**
         * Se ejecuta después de terminar "doInBackground".
         * <p>
         * Se ejecuta en el hilo: PRINCIPAL
         *
         * @param //array con los valores pasados por el return de "doInBackground".
         */
        @Override
        protected void onPostExecute(Integer cantidadProcesados) {
            //  TV_mensaje.setText("DESPUÉS de TERMINAR la descarga. Se han descarcado "+cantidadProcesados+" imágenes. Hilo PRINCIPAL");
            Log.v(TAG_LOG, "onPostExecute: DESPUÉS de TERMINAR la descarga. Se han descarcado " + cantidadProcesados + " imágenes. Hilo PRINCIPAL");

            //TV_mensaje.setTextColor(Color.GREEN);
        }

        /**
         * Se ejecuta si se ha llamado al método "cancel" y después de terminar "doInBackground". Por lo que se ejecuta en vez de "onPostExecute"
         * Nota: Este onCancelled solo funciona a partir de Android 3.0 (Api Level 11 en adelante). En versiones anteriores onCancelled no funciona
         * <p>
         * Se ejecuta en el hilo: PRINCIPAL
         * <p>
         * // @param array con los valores pasados por el return de "doInBackground".
         */
        @Override
        protected void onCancelled(Integer cantidadProcesados) {
            // TV_mensaje.setText("DESPUÉS de CANCELAR la descarga. Se han descarcado "+cantidadProcesados+" imágenes. Hilo PRINCIPAL");
            Log.v(TAG_LOG, "Servicio Cancelado");

            //TV_mensaje.setTextColor(Color.RED);
        }

    }



    public void verificarAlarma() {

        HttpClient client = new HttpClient(new OnHttpRequestComplete() {
            @Override
            public void onComplete(Response status) {
                if (status.isSuccess()) {
                    Gson gson = new GsonBuilder().create();
                    try {
                        JSONObject jsono = new JSONObject(status.getResult());
                        // JSONArray jsonarray = jsono.getJSONArray("rows");
                        String cantidad = jsono.getString("total");
                        if (jsono.getString("total").equals("0")) {
                            //tvError.setText(jsono.getString("descripcion"));
                            //pDialog.hide();
                            Log.v(TAG_LOG, "NO EXISTE ALARMA");
                            generarNotificacion = false;
                        } else {
                            //tvError.setText("");
                            Log.v(TAG_LOG, "EXISTE ALARMA");
                            generarNotificacion = true;

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //tvError.setText("Error al intentar acceder a servidor");
                    //pDialog.hide();
                }
            }
        });


        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority(ConfigServidor.IP)
                .appendPath(ConfigServidor.URL)
                .appendPath("alarma")
                .appendPath("alarmaInspector.php")
                .appendQueryParameter("id_ins", id_ins);
        String myUrl = builder.build().toString();
        client.excecute(myUrl);
    }

    public void salir(View view) {
        System.runFinalization();
        System.exit(0);
    }

    public void irA_Calibracion(View view) {
        Intent i = new Intent(this, AsignarActivity.class);
        startActivity(i);
    }

    public void irA_Dispositivo(View view) {
        Intent i = new Intent(this, DetalleDispoActivity.class);
        startActivity(i);
    }
}
