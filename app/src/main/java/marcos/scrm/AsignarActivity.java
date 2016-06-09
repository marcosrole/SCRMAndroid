package marcos.scrm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.sax.TextElementListener;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import devazt.networking.ConfigServidor;
import devazt.networking.HttpClient;
import devazt.networking.OnHttpRequestComplete;
import devazt.networking.Response;


public class AsignarActivity extends AppCompatActivity {
    private TextView tvCoordLat, tvCoordLon, tvSucursal, tvEmpresa, tvDireccion, tvError, tvMAC, tvID_dis;
    private Button btnBuscarSuc, btnValidar, btnSiguiente, btnAtras, btnCalibrarDispo;
    ImageButton btnBuscarDispo;
    private LocationManager locationManager;
    private EditText etID_dis;
    private String provider;
    private String sucursal_idSelec, id_disSelec;
    boolean datosAlmacenados=false;
    boolean dispoNOcalibrado=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar);
        final Toast msj = new Toast(AsignarActivity.this);
        btnBuscarSuc = (Button) findViewById(R.id.btnBuscarSuc);
        btnValidar = (Button) findViewById(R.id.btnCheck);
        btnSiguiente = (Button)findViewById(R.id.btnSiguiente);
        btnAtras = (Button)findViewById(R.id.btnAtras);
        btnCalibrarDispo = (Button)findViewById(R.id.btnCalibrarDispo);
        btnBuscarDispo = (ImageButton)findViewById(R.id.btnBuscarDispo);

        tvCoordLat = (TextView) findViewById(R.id.tvCoorLat);
        tvCoordLon = (TextView) findViewById(R.id.tvCoorLon);
        tvSucursal = (TextView) findViewById(R.id.tvSucursal);
        tvEmpresa = (TextView) findViewById(R.id.tvEmpresa);
        tvDireccion = (TextView) findViewById(R.id.tvDireccion);
        tvError = (TextView) findViewById(R.id.tvError);
        tvMAC = (TextView) findViewById(R.id.tvMAC);
        tvID_dis = (TextView) findViewById(R.id.tvId_dis);

        etID_dis = (EditText) findViewById(R.id.etID_dis);

        Intent startingIntent = getIntent();
        if (startingIntent != null) {
            Bundle b = startingIntent
                    .getBundleExtra("android.intent.extra.INTENT");
            tvSucursal.setText((String) getIntent().getSerializableExtra("sucursal_nombre"));
            tvEmpresa.setText((String) getIntent().getSerializableExtra("empresa_nombre"));
            tvDireccion.setText((String) getIntent().getSerializableExtra("sucursal_direccion"));
            sucursal_idSelec = (String) getIntent().getSerializableExtra("sucursal_id");
            tvID_dis.setText((String) getIntent().getSerializableExtra("id_dis"));
            tvMAC.setText((String) getIntent().getSerializableExtra("mac"));
            etID_dis.setText((String) getIntent().getSerializableExtra("id_dis"));
           // validarDispositivo();

        }

        	/* Uso de la clase LocationManager para obtener la localizacion del GPS */
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(AsignarActivity.this);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                (LocationListener) Local);

        //mensaje1.setText("Localizacion agregada");
        //mensaje2.setText("");

        btnBuscarSuc.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(AsignarActivity.this, pickEmpresaActivity.class);
                startActivity(i);
            }
        });
        btnValidar.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                validarDispositivo();
            }
        });

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                final String id_dis = etID_dis.getText().toString();
                if (validarDatos_btnAsignar()) {
                    if(tvCoordLon.getText().toString().equals("")){
                        new AlertDialog.Builder(AsignarActivity.this)
                                .setTitle("Advertencia")
                                .setMessage("No se ha podido determinar las coordenadas geográficas. ¿Desea continuar?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        guardarDatos(id_dis);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }else guardarDatos(id_dis);
                }
            }
        });

        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(AsignarActivity.this, MenuActivity.class);
                startActivity(i);
            }
        });
        btnCalibrarDispo.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(AsignarActivity.this, CalibrarActivity.class);
                startActivity(i);
            }
        });
        btnBuscarDispo.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(AsignarActivity.this, pickDispositivoActivity.class);
                i.putExtra("sucursal_nombre", tvSucursal.getText().toString());
                i.putExtra("sucursal_direccion", tvDireccion.getText().toString());
                i.putExtra("empresa_nombre", tvEmpresa.getText().toString());
                i.putExtra("sucursal_id", sucursal_idSelec);

                startActivity(i);
            }
        });

        if(datosAlmacenados){
            msj.makeText(this,"Datos almacenados con exito", Toast.LENGTH_SHORT);
            Intent i = new Intent(AsignarActivity.this, MenuActivity.class);
            startActivity(i);
        }
    }


    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                   // mensaje2.setText("Mi direccion es: \n"
                          //  + DirCalle.getAddressLine(0));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        AsignarActivity mainActivity;

        public AsignarActivity getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(AsignarActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion

            loc.getLatitude();
            loc.getLongitude();
            String Text = ""+loc.getLatitude();
            tvCoordLat.setText(""+loc.getLatitude());
            tvCoordLon.setText(""+loc.getLongitude());

        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            //mensaje1.setText("GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            //mensaje1.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Este metodo se ejecuta cada vez que se detecta un cambio en el
            // status del proveedor de localizacion (GPS)
            // Los diferentes Status son:
            // OUT_OF_SERVICE -> Si el proveedor esta fuera de servicio
            // TEMPORARILY_UNAVAILABLE -> Temporalmente no disponible pero se
            // espera que este disponible en breve
            // AVAILABLE -> Disponible
        }

    }/* Fin de la clase localizacion */
    public  boolean validarDatos_btnAsignar(){
        String mac =tvMAC.getText().toString();
        String id_dis = tvID_dis.getText().toString();
        boolean valido=true;
        if((tvSucursal.getText().toString().equals("") || tvEmpresa.getText().toString().equals(""))){
            valido=false;
            tvError.setText("Seleccione una sucursal");
            btnBuscarSuc.requestFocus();
        }
            if((mac.equals("") || id_dis.equals(""))){
                valido=false;
                tvError.setText("Valide un dispositivo");
                etID_dis.requestFocus();
            }

        return valido;
    }

    public void validarDispositivo(){
        final String id_dis = etID_dis.getText().toString();
        if (id_dis.equals("")) {
            Toast msj = new Toast(this);
            msj.makeText(AsignarActivity.this, "Ingrese un ID valido", Toast.LENGTH_SHORT).show();
            etID_dis.requestFocus();
        } else {
            final ProgressDialog pDialog = new ProgressDialog(AsignarActivity.this);
            pDialog.setMessage("Validando dispositivo...");
            pDialog.show();

            new Thread(new Runnable() {
                public void run() {
                    HttpClient client = new HttpClient(new OnHttpRequestComplete() {
                        @Override
                        public void onComplete(Response status) {
                            if (status.isSuccess()) {
                                Gson gson = new GsonBuilder().create();
                                try {
                                    JSONObject jsono = new JSONObject(status.getResult());
                                    // JSONArray jsonarray = jsono.getJSONArray("rows");
                                    String cantidad = jsono.getString("total");
                                    if (jsono.getString("error").equals("-2")) {
                                        dispoNOcalibrado = true;
                                        //tvError.setText(jsono.getString("descripcion"));
                                        JSONArray jsonarray = jsono.getJSONArray("rows");
                                        final JSONObject jsonobject = jsonarray.getJSONObject(0);
                                        AsignarActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                try {
                                                    tvMAC.setText(jsonobject.getString("MAC"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                try {
                                                    tvID_dis.setText(jsonobject.getString("id_dis"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                pDialog.hide();
                                            }
                                        });

                                    } else {
                                        if (jsono.getString("error").equals("-3")) {
                                            showToast("El dispositivo no se existe");
                                            AsignarActivity.this.runOnUiThread(new Runnable() {
                                                public void run() {
                                                    etID_dis.requestFocus();
                                                    tvMAC.setText("");
                                                    tvID_dis.setText("");
                                                    pDialog.hide();
                                                }
                                            });
                                        } else {
                                            if (jsono.getString("error").equals("-1")) {
                                                showToast("El dispositivo ya ha sido asignado");
                                                AsignarActivity.this.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        etID_dis.requestFocus();
                                                        tvMAC.setText("");
                                                        tvID_dis.setText("");
                                                        pDialog.hide();
                                                    }
                                                });

                                            }else {
                                                JSONArray jsonarray = jsono.getJSONArray("rows");
                                                final JSONObject jsonobject = jsonarray.getJSONObject(0);

                                                AsignarActivity.this.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        try {
                                                            tvMAC.setText(jsonobject.getString("MAC"));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            tvID_dis.setText(jsonobject.getString("id_dis"));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        pDialog.hide();
                                                    }
                                                });

                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    pDialog.hide();
                                }
                            } else {
                                tvError.setText("Error al intentar acceder a servidor");
                                pDialog.hide();
                            }
                        }
                    });


                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority(ConfigServidor.IP)
                            .appendPath(ConfigServidor.URL)
                            .appendPath("calibracion")
                            .appendPath("dispositivoCalibrado.php")
                            .appendQueryParameter("id_dis", id_dis)
                            .appendQueryParameter("flagHistoAsig", "1");
                    String myUrl = builder.build().toString();
                    client.excecute(myUrl);
                }
            }).start();

        }
    }

    public void guardarDatos(final String id_dis){
        final ProgressDialog pDialog = new ProgressDialog(AsignarActivity.this);
        pDialog.setMessage("Generando asignacion...");
        pDialog.show();

        new Thread(new Runnable() {
            public void run() {
                HttpClient client = new HttpClient(new OnHttpRequestComplete() {
                    @Override
                    public void onComplete(Response status) {
                        if (status.isSuccess()) {
                            Gson gson = new GsonBuilder().create();
                            try {
                                JSONObject jsono = new JSONObject(status.getResult());
                                // JSONArray jsonarray = jsono.getJSONArray("rows");
                                if (jsono.getString("error").equals("-1")) {
                                    showToast(jsono.getString("descripcion"));
                                    AsignarActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });
                                }else{
                                    datosAlmacenados=true;
                                    AsignarActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Intent i = new Intent(AsignarActivity.this, CalibrarActivity.class);
                                            i.putExtra("id_dis",id_dis);
                                            startActivity(i);
                                            pDialog.hide();
                                        }
                                    });

                                }
                            } catch (final Exception e) {
                                showToast("Error JSON");
                                AsignarActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });

                            }
                        } else {
                            showToast("Error al intentar acceder al Servidor");
                            tvError.setText("Error al intentar acceder a servidor");
                            AsignarActivity.this.runOnUiThread(new Runnable() {
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
                        .appendPath("insertarAsigancion.php")
                        .appendQueryParameter("id_dis", id_dis)
                        .appendQueryParameter("id_suc",sucursal_idSelec)
                        .appendQueryParameter("coordLat", tvCoordLat.getText().toString())
                        .appendQueryParameter("coordLon", tvCoordLon.getText().toString());

                String myUrl = builder.build().toString();
                client.excecute(myUrl);
            }
        }).start();


    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(AsignarActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
