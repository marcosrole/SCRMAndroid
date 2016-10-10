package marcos.scrm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import clases.Dispositivo;
import clases.HistoAsignacion;
import devazt.networking.ConfigServidor;
import devazt.networking.HttpClient;
import devazt.networking.OnHttpRequestComplete;
import devazt.networking.Response;

public class CalibrarActivity extends AppCompatActivity {
    Button btnVerificar, btnSalir, btnAtras, btnGuardar;
    EditText etId_dis, edDB, edDistancia;
    TextView tvLocalidad, tvSucursal, tvDireccion, tvError;
    String id_dis;
    ImageButton btnCalibrar;
    final HistoAsignacion histoAsigObj = new HistoAsignacion();
    private ProgressDialog progress, progressPromediar;
    private Handler mHandler = new Handler();
    private String hsHOY;

    int tiempoEspera = 2;//minutos
    int envioDatosDispo = 20; //seg
    int cantRegistros = (tiempoEspera*60)/envioDatosDispo;

    double dbProm=0.0;
    double distProm=0.0;
    private boolean VerificarDetalle=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrar);

        Dispositivo dispoObj = new Dispositivo();

        btnVerificar = (Button) findViewById(R.id.btnVerificar);
        btnSalir = (Button) findViewById(R.id.btnSalir);
        btnAtras = (Button) findViewById(R.id.btnAtras);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        btnCalibrar = (ImageButton) findViewById(R.id.btnCalibrar);

        etId_dis = (EditText) findViewById(R.id.etId_dis);
        edDB = (EditText) findViewById(R.id.edDB);
        edDistancia = (EditText) findViewById(R.id.edDistancia);

        tvError = (TextView) findViewById(R.id.tvError);
        tvLocalidad = (TextView) findViewById(R.id.tvLocalidad);
        tvSucursal = (TextView) findViewById(R.id.tvSucursal);
        tvDireccion = (TextView) findViewById(R.id.tvDireccion);
        final Toast msj = new Toast(CalibrarActivity.this);



        Intent startingIntent = getIntent();
        if (startingIntent != null) {
            Bundle b = startingIntent
                    .getBundleExtra("android.intent.extra.INTENT");
            id_dis = (String) getIntent().getSerializableExtra("id_dis");
            etId_dis.setText(id_dis);
            if(!etId_dis.getText().toString().equals(""))validarDispositivo();
        }


        //Obtener Hora de AHORA
        SimpleDateFormat readDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        readDate.setTimeZone(TimeZone.getTimeZone("GMT")); // missing line

        SimpleDateFormat writeDate = new SimpleDateFormat("HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT-03:00"));

        hsHOY = writeDate.format(new Date());
        final String Hshoy[] = hsHOY.split(":");

        //Paso todo a segundos
        int horaHOY = Integer.parseInt(Hshoy[0].toString())*3600;
        int minutoHOY = Integer.parseInt(Hshoy[1].toString())*60;
        int segundoHOY = Integer.parseInt(Hshoy[2].toString());
        long tiempoHOY = horaHOY+minutoHOY+segundoHOY;

       btnCalibrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(!etId_dis.getText().toString().equals("")) {
                    if(!tvSucursal.getText().toString().equals("")){
                        progress = new ProgressDialog(CalibrarActivity.this);
                        progress.setTitle("Calibrando Dispositivo");
                        progress.setMessage("Espere aproximadamente " + tiempoEspera + " minutos");
                        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progress.setIndeterminate(false);
                        progress.setButton("Cancelar", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                VerificarDetalle = true;

                            }
                        });

                        progress.setCancelable(false);
                        progress.setMax(100);
                        progress.setProgress(0);
                        progress.show();

                        final int totalProgressTime = 100;
                        final Thread t = new Thread() {
                            @Override
                            public void run() {
                                int jumpTime = 0;

                                while (jumpTime < totalProgressTime) {
                                    try {
                                        sleep(((tiempoEspera * 60) / 20) * 1000);
                                        jumpTime += 5;
                                        progress.setProgress(jumpTime);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                                progress.dismiss();
                            }
                        };
                        t.start();



                        progressPromediar = new ProgressDialog(CalibrarActivity.this);
                        progressPromediar.setTitle("Calibrando Dispositivo");
                        progressPromediar.setMessage("Promediando datos");
                        progressPromediar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressPromediar.setIndeterminate(true);

                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                if(VerificarDetalle){
                                    VrificarDetalleDatos();
                                }
                            }
                        }, tiempoEspera*60*1000+500);

                    }else{
                        msj.makeText(CalibrarActivity.this, "Valide el dispositivo", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    msj.makeText(CalibrarActivity.this, "Ingrese un dispositivo", Toast.LENGTH_SHORT).show();
                    etId_dis.requestFocus();
                }
                }
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });

        btnVerificar.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                validarDispositivo();
            }
        });


        btnGuardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                String id_dis = etId_dis.getText().toString();
                final String db = edDB.getText().toString();
                final String distancia = edDistancia.getText().toString();
                String sucursal = tvSucursal.getText().toString();
                if(id_dis.equals("")){
                    etId_dis.requestFocus();
                    msj.makeText(CalibrarActivity.this,"Ingrese un ID de dispositivo a buscar",Toast.LENGTH_SHORT).show();
                }else{
                    if(db.equals("")){
                        edDB.requestFocus();
                        msj.makeText(CalibrarActivity.this,"Ingrese el valor de dB",Toast.LENGTH_SHORT).show();
                    }else{
                        if(distancia.equals("")){
                            edDistancia.requestFocus();
                            msj.makeText(CalibrarActivity.this,"Ingrese el valor de Distancia",Toast.LENGTH_SHORT).show();
                        }else{
                            if(sucursal.equals("")){
                                etId_dis.requestFocus();
                                msj.makeText(CalibrarActivity.this,"Debe validar el ID del Dispositivo",Toast.LENGTH_SHORT).show();
                            }else{

                                final ProgressDialog pDialog = new ProgressDialog(CalibrarActivity.this);
                                pDialog.setMessage("Guardando calibracion...");
                                pDialog.show();

                                new Thread(new Runnable() {
                                    public void run() {
                                        HttpClient client = new HttpClient(new OnHttpRequestComplete() {
                                            @Override
                                            public void onComplete(Response status) {
                                                if(status.isSuccess()){
                                                    Gson gson = new GsonBuilder().create();
                                                    try{
                                                        JSONObject jsono = new JSONObject(status.getResult());
                                                        if(!jsono.getString("error").equals("0")){
                                                            showToast(jsono.getString("descripcion"));
                                                            CalibrarActivity.this.runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    pDialog.hide();
                                                                }
                                                            });
                                                        }else{
                                                            showToast("Datos guardados con Exito");
                                                            CalibrarActivity.this.runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    Intent i = new Intent(CalibrarActivity.this, MenuActivity.class);
                                                                    startActivity(i);
                                                                    pDialog.hide();
                                                                }
                                                            });
                                                        }
                                                    }catch (final Exception e){
                                                        showToast("Error JSON");
                                                        CalibrarActivity.this.runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                e.printStackTrace();
                                                                pDialog.hide();
                                                            }
                                                        });

                                                    }
                                                }else {
                                                    showToast("Error al intentar acceder al Servidor");
                                                    CalibrarActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            pDialog.hide();
                                                        }
                                                    });
                                                }
                                            }
                                        });


                                        Uri.Builder builder = new Uri.Builder();
                                        builder.scheme("http")
                                                .authority(ConfigServidor.IP)
                                                .appendPath(ConfigServidor.URL)
                                                .appendPath("calibracion")
                                                .appendPath("guardarCalibracion.php")
                                                .appendQueryParameter("id_AsiDis", histoAsigObj.getId())
                                                .appendQueryParameter("db_permitido", db)
                                                .appendQueryParameter("dist_permitido", distancia);
                                        String myUrl = builder.build().toString();
                                        client.excecute(myUrl);
                                    }
                                }).start();
                            }
                        }
                    }
                }
            }
        });

        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(CalibrarActivity.this, MenuActivity.class);
                startActivity(i);
            }
        });
        btnSalir.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                finish();
                System.exit(0);
            }
        });
    }



    public void verificarCalibracion(){
        final String id_dis = etId_dis.getText().toString();
        final ProgressDialog pDialog = new ProgressDialog(CalibrarActivity.this);
        pDialog.setMessage("Verificando datos de calibracion...");
        pDialog.show();

        new Thread(new Runnable() {
            public void run() {
                HttpClient client = new HttpClient(new OnHttpRequestComplete() {
                    @Override
                    public void onComplete(Response status) {
                        if(status.isSuccess()){
                            Gson gson = new GsonBuilder().create();
                            try{
                                JSONObject jsono = new JSONObject(status.getResult());
                                if(jsono.getString("total").equals("0")){
                                    showToast(jsono.getString("descripcion"));
                                    CalibrarActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            edDB.setText("");
                                            edDistancia.setText("");
                                            pDialog.hide();
                                        }
                                    });
                                }else{
                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                    final JSONObject jsonobject = jsonarray.getJSONObject(0);

                                    showToast(jsono.getString("descripcion"));
                                    CalibrarActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                edDB.setText(jsonobject.getString("db"));
                                                edDistancia.setText(jsonobject.getString("distancia"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            pDialog.hide();
                                        }
                                    });
                             }
                            }catch (final Exception e){
                                showToast("Error JSON");
                                CalibrarActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });

                            }
                        }else {
                            showToast("Error al intentar acceder al Servidor");
                            CalibrarActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    pDialog.hide();
                                }
                            });

                        }
                    }
                });


                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority(ConfigServidor.IP)
                        .appendPath(ConfigServidor.URL)
                        .appendPath("calibracion")
                        .appendPath("dispositivoCalibrado.php")
                        .appendQueryParameter("id_dis", id_dis);
                String myUrl = builder.build().toString();
                client.excecute(myUrl);
            }
        }).start();
    }


    public void VrificarDetalleDatos(){


        HttpClient client = new HttpClient(new OnHttpRequestComplete() {
            @Override
            public void onComplete(Response status) {
                if(status.isSuccess()){
                    Gson gson = new GsonBuilder().create();
                    try{
                        JSONObject jsono = new JSONObject(status.getResult());
                        if(!jsono.getString("error").equals("0")){
                            tvError.setText(jsono.getString("descripcion"));
                            progressPromediar.dismiss();
                        }else{
                            tvError.setText("");
                            JSONArray jsonarray = jsono.getJSONArray("rows");

                            //Dos pasos:
                            //1 : Verificar diferencia para cada json (o cada registro)

                            boolean bandera = false;
                            for (int i = 0; (i < jsonarray.length() && !bandera); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                String db = jsonobject.getString("db");
                                String distancia = jsonobject.getString("distancia");
                                String diferencia = jsonobject.getString("diferencia");

                                int diferenciaEntero=Integer.parseInt(diferencia.toString());
                                int tolerancia = envioDatosDispo*4*(i+1);

                                if(diferenciaEntero < tolerancia){
                                    dbProm+=Integer.parseInt(db.toString());
                                    distProm+=Integer.parseInt(distancia.toString());
                                }else{
                                    bandera=true;
                                    Toast msj = new Toast(CalibrarActivity.this);
                                    msj.makeText(CalibrarActivity.this,"Error: Los datos censados no se encuentran actualizados",Toast.LENGTH_SHORT).show();
                                    //tvError.setText("Conecte el dispositivo y espere " + tiempoEspera + " minutos aproximadamente");
                                }
                            }

                            if(!bandera){
                                dbProm=dbProm/cantRegistros;
                                distProm=distProm/cantRegistros;

                                DecimalFormat twoDForm = new DecimalFormat("#");
                                dbProm= Double.valueOf(twoDForm.format(dbProm));
                                distProm= Double.valueOf(twoDForm.format(distProm));

                                edDB.setText(String.valueOf(dbProm));
                                edDistancia.setText(String.valueOf(distProm));
                            }




                            progressPromediar.dismiss();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        progressPromediar.dismiss();
                    }
                }else {
                    tvError.setText("Error al intentar acceder a servidor");
                    progressPromediar.dismiss();
                }
            }
        });


        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority(ConfigServidor.IP)
                .appendPath(ConfigServidor.URL)
                .appendPath("calibracion")
                .appendPath("datosDeCalibracion.php")
                .appendQueryParameter("id_dis", etId_dis.getText().toString())
                .appendQueryParameter("cantidad", (String.valueOf(cantRegistros)));
        String myUrl = builder.build().toString();
        client.excecute(myUrl);

    }
    public long restarHs(String HsIni, String HsFin){
        String Hs0[] = HsIni.split(":");
        String Hs1[] = HsFin.split(":");

        //Paso todo a segundos
        int hora0 = Integer.parseInt(Hs0[0].toString())*3600;
        int minuto0 = Integer.parseInt(Hs0[1].toString())*60;
        int segundo0 = Integer.parseInt(Hs0[2].toString());

        int hora1 = Integer.parseInt(Hs1[0].toString())*3600;
        int minuto1 = Integer.parseInt(Hs1[1].toString())*60;
        int segundo1 = Integer.parseInt(Hs1[2].toString());

        long tiempoIni = hora0+minuto0+segundo0;
        long tiempoFin = hora1+minuto1+segundo1;

        return tiempoFin-tiempoIni;
    }

    public void validarDispositivo(){
        final String id_dis = etId_dis.getText().toString();
        if(id_dis.equals("")){
            Toast msj = new Toast(this);
            msj.makeText(CalibrarActivity.this,"Ingrese un ID de dispositivo a buscar",Toast.LENGTH_SHORT).show();
        }else{
            final ProgressDialog pDialog = new ProgressDialog(CalibrarActivity.this);
            pDialog.setMessage("Validando dispositivo...");
            pDialog.show();

            new Thread(new Runnable() {
                public void run() {
                    HttpClient client = new HttpClient(new OnHttpRequestComplete() {
                        @Override
                        public void onComplete(Response status) {
                            if(status.isSuccess()){
                                Gson gson = new GsonBuilder().create();
                                try{
                                    JSONObject jsono = new JSONObject(status.getResult());
                                    // JSONArray jsonarray = jsono.getJSONArray("rows");
                                    String cantidad = jsono.getString("total");
                                    if(jsono.getString("total").equals("0")){
                                        //showToast(jsono.getString("descripcion"));
                                        CalibrarActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                tvSucursal.setText("");
                                                tvDireccion.setText("");
                                                tvLocalidad.setText("");
                                                edDB.setText("");
                                                edDistancia.setText("");
                                                pDialog.hide();
                                            }
                                        });

                                    }else{
                                        JSONArray jsonarray = jsono.getJSONArray("rows");
                                        final JSONObject jsonobject = jsonarray.getJSONObject(0);

                                        //showToast(jsono.getString("descripcion"));
                                        CalibrarActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                histoAsigObj.setId_dis(id_dis);
                                                try {
                                                    histoAsigObj.setId(jsonobject.getString("id"));
                                                    tvSucursal.setText(jsonobject.getString("nombre"));
                                                    tvDireccion.setText(jsonobject.getString("direccion"));
                                                    tvLocalidad.setText(jsonobject.getString("localidad"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                verificarCalibracion();
                                                pDialog.hide();
                                            }
                                        });


                                    }
                                }catch (final Exception e){
                                    showToast("Error JSON");
                                    CalibrarActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            e.printStackTrace();
                                            pDialog.hide();
                                        }
                                    });

                                }
                            }else {
                                showToast("Error al intentar acceder al Servidor");
                                CalibrarActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        pDialog.hide();
                                    }
                                });
                            }
                        }
                    });


                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority(ConfigServidor.IP)
                            .appendPath(ConfigServidor.URL)
                            .appendPath("hisoAsign")
                            .appendPath("buscarDispositivo.php")
                            .appendQueryParameter("id_dis", id_dis);
                    String myUrl = builder.build().toString();
                    client.excecute(myUrl);
                }
            }).start();
        }


    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(CalibrarActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
