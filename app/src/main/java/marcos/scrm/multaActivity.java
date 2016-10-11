package marcos.scrm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import devazt.networking.ConfigServidor;
import devazt.networking.HttpClient;
import devazt.networking.OnHttpRequestComplete;
import devazt.networking.Response;

public class multaActivity extends AppCompatActivity {
    TextView tvError, tvFecha, tvHs, tvEmpresa, tvCUIT, tvSucursal, tvDireccion, tvAlarma;
    EditText etNombreInfractor, etDNIInf, etNombreIns, etDNIIns, etObservacion;
    Button btnAtras, btnGuardar;
    String id_AsiIns;
    String id_ins,id_dis,cuit_emp, id_suc,id_ala;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multa);


        tvError = (TextView) findViewById(R.id.tvError);
        tvFecha = (TextView) findViewById(R.id.tvFecha);
        tvHs = (TextView) findViewById(R.id.tvHora);
        tvCUIT = (TextView) findViewById(R.id.tvCUIT);
        tvSucursal = (TextView) findViewById(R.id.tvSucursal);
        tvFecha = (TextView) findViewById(R.id.tvFecha);
        tvEmpresa = (TextView) findViewById(R.id.tvEmpresa);
        tvFecha = (TextView) findViewById(R.id.tvFecha);
        tvDireccion = (TextView) findViewById(R.id.tvDireccion);
        tvAlarma = (TextView) findViewById(R.id.tvAlarma);

        etDNIInf = (EditText) findViewById(R.id.etDNI);
        etNombreInfractor = (EditText) findViewById(R.id.etNombre);
        etNombreIns = (EditText) findViewById(R.id.etNombreInspector);
        etDNIIns = (EditText) findViewById(R.id.etDNIInspector);
        etObservacion = (EditText) findViewById(R.id.etObservacion);

        btnAtras = (Button) findViewById(R.id.btnAtras);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        final Toast msj = new Toast(multaActivity.this);

        Intent startingIntent = getIntent();
        if (startingIntent != null) {
            Bundle b = startingIntent
                    .getBundleExtra("android.intent.extra.INTENT");
            id_AsiIns = (String) getIntent().getSerializableExtra("id_AsiIns");
        }

        setDatos();

        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(multaActivity.this, AlarmaActivity.class);
                startActivity(i);
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            Boolean flag=true;
            public void onClick(final View view) {

                if (!etDNIIns.getText().toString().equals("")) {
                    if (!etNombreIns.getText().toString().equals("") && flag) {
                        final ProgressDialog pDialog = new ProgressDialog(multaActivity.this);
                        pDialog.setMessage("Almacenando acta de infraccion...");
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
                                                    multaActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            pDialog.hide();
                                                        }
                                                    });
                                                } else {
                                                    showToast("Acta de Infraccion almacenada con exito");
                                                    multaActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            setAsignacionInspector();
                                                            new AlertDialog.Builder(multaActivity.this)
                                                                    .setTitle("Imprimir Acta")
                                                                    .setMessage("¿Desea imprimir el acta generada?")
                                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            //IMPRIMIR ACTA
                                                                        }
                                                                    })
                                                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            Intent i = new Intent(multaActivity.this, AlarmaActivity.class);
                                                                            startActivity(i);
                                                                        }
                                                                    })
                                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                                    .show();
                                                            pDialog.hide();
                                                        }
                                                    });
                                                }
                                            } catch (final Exception e) {
                                                showToast("Error JSON");
                                                multaActivity.this.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        e.printStackTrace();
                                                        pDialog.hide();
                                                    }
                                                });

                                            }
                                        } else {
                                            showToast("Error al intentar acceder a servidor");
                                            multaActivity.this.runOnUiThread(new Runnable() {
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
                                        .appendPath("acta")
                                        .appendPath("insertarMulta.php")
                                        .appendQueryParameter("observacion", etObservacion.getText().toString())
                                        .appendQueryParameter("id_ins", id_ins)
                                        .appendQueryParameter("id_suc", id_suc)
                                        .appendQueryParameter("id_dis", id_dis)
                                        .appendQueryParameter("id_ala", id_ala);

                                String myUrl = builder.build().toString();
                                client.excecute(myUrl);
                            }
                        }).start();
                    } else {
                        etNombreIns.setFocusable(true);
                        msj.makeText(multaActivity.this, "Ingrese el nombre del Inspector", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    etDNIIns.setFocusable(true);
                    msj.makeText(multaActivity.this, "Ingrese el DNI del inspector", Toast.LENGTH_SHORT).show();
                    flag=false;
                }
            }


    });



}


    public void setDatos(){
        final ProgressDialog pDialog = new ProgressDialog(multaActivity.this);
        pDialog.setMessage("Generando acta de infraccion...");
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
                                if (jsono.getString("total").equals("0")) {
                                    showToast(jsono.getString("descripcion"));
                                    multaActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });
                                } else {

                                    JSONArray jsonarray = jsono.getJSONArray("rows");

                                    final JSONObject jsonobject = jsonarray.getJSONObject(0);
                                    multaActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                tvFecha.setText(jsonobject.getString("fecha"));
                                                tvHs.setText(jsonobject.getString("hs"));
                                                etDNIInf.setText(jsonobject.getString("dni_due"));
                                                etNombreInfractor.setText(jsonobject.getString("nombre_due") + " " + jsonobject.getString("apellido_due"));
                                                etDNIInf.setEnabled(false);
                                                etNombreInfractor.setEnabled(false);
                                                tvEmpresa.setText(jsonobject.getString("razonsocial"));
                                                tvCUIT.setText(jsonobject.getString("cuit"));
                                                tvSucursal.setText(jsonobject.getString("nombre_suc"));
                                                tvDireccion.setText(jsonobject.getString("direccion"));
                                                etNombreIns.setText(jsonobject.getString("nombre_ins") + " " + jsonobject.getString("apellido_ins"));
                                                etDNIIns.setText(jsonobject.getString("dni_ins"));
                                                tvAlarma.setText(jsonobject.getString("descripcion"));
                                                etObservacion.setText("");

                                                id_ins = jsonobject.getString("id_ins");
                                                cuit_emp = jsonobject.getString("cuit");
                                                id_dis = jsonobject.getString("id_dis");
                                                id_suc = jsonobject.getString("id_suc");
                                                id_ala = jsonobject.getString("id_ala");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            pDialog.hide();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                showToast("Error jSON");
                                multaActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        pDialog.hide();
                                    }
                                });
                            }
                        } else {
                            showToast("Error al intentar acceder a servidor");
                            multaActivity.this.runOnUiThread(new Runnable() {
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
                        .appendPath("acta")
                        .appendPath("detallesMulta.php")
                        .appendQueryParameter("id_AsiIns", id_AsiIns);
                String myUrl = builder.build().toString();
                client.excecute(myUrl);
            }
        }).start();
    }

    public void setAsignacionInspector(){
        final ProgressDialog pDialog = new ProgressDialog(multaActivity.this);
        pDialog.setMessage("Configurando datos...");
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
                                if (jsono.getString("total").equals("0")) {
                                    showToast(jsono.getString("descripcion"));
                                    multaActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });
                                } else {

                                    multaActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Intent i = new Intent(multaActivity.this, pickActaActivity.class);
                                            startActivity(i);
                                            pDialog.hide();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                showToast("Error jSON");
                                multaActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });
                            }
                        } else {
                            showToast("Error al intentar acceder a servidor");
                            multaActivity.this.runOnUiThread(new Runnable() {
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
                        .appendPath("asignarInspector")
                        .appendPath("alarmaSolucionada.php")
                        .appendQueryParameter("id_AsiIns", id_AsiIns);
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
                Toast.makeText(multaActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
