package marcos.scrm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import devazt.networking.ConfigServidor;
import devazt.networking.HttpClient;
import devazt.networking.OnHttpRequestComplete;
import devazt.networking.Response;
import util.General;

public class pickActaActivity extends AppCompatActivity {
    ListView lv;
    ArrayList<HashMap<String, String>> ActasList = new ArrayList<HashMap<String, String>>();
    TextView tvError;
    String id_ins;
    Button btnAtras, btnSalir;
    General archivoTXT = new General();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_acta);
        lv = (ListView) findViewById(R.id.listViewActas);
        tvError = (TextView)findViewById(R.id.tvError);
        btnAtras = (Button)findViewById(R.id.btnAtras);
        btnSalir = (Button)findViewById(R.id.btnSalir);
        archivoTXT.recuperar_id_ins("attributes_usr.txt");
        id_ins=archivoTXT.getId_ins();

        mostrarActasList();
        seleccionarItem();

        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(pickActaActivity.this, MenuActivity.class);
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

    public void mostrarActasList(){
        final ProgressDialog pDialog = new ProgressDialog(pickActaActivity.this);
        pDialog.setMessage("Buscando actas realizadas...");
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
                                    pickActaActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });

                                } else {
                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                                        String alarma = jsonobject.getString("nom_TipAla");

                                        String sucursal = jsonobject.getString("nom_suc");
                                        String empresa = jsonobject.getString("emp_razsoc");
                                        String EmpSuc = empresa + " - " + sucursal;

                                        String direccion = jsonobject.getString("direccion");

                                        String observacion;
                                        if(jsonobject.getString("observacion").equals(""))observacion="";
                                        else observacion = jsonobject.getString("observacion");

                                        String hs = jsonobject.getString("hs");
                                        String fecha = jsonobject.getString("fecha");
                                        String fechahs = fecha + "    -    " + hs + " hs.";


                                        HashMap<String, String> item = new HashMap<String, String>();
                                        item.put("fechahs", fechahs);
                                        item.put("nom_TipAla", alarma);
                                        item.put("EmpSuc", EmpSuc);
                                        item.put("direccion", direccion);
                                        item.put("observacion", observacion);
                                        ActasList.add(item);
                                    }

                                    final SimpleAdapter adapter_alarma = new SimpleAdapter(pickActaActivity.this, ActasList,
                                            R.layout.five_line, new String[]{"fechahs","nom_TipAla","EmpSuc","direccion","observacion"},
                                            new int[]{
                                                    R.id.linea_a_5,
                                                    R.id.linea_b_5,
                                                    R.id.linea_c_5,
                                                    R.id.linea_d_5,
                                                    R.id.linea_e_5,
                                            });

                                    pickActaActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            lv.setAdapter(adapter_alarma);
                                            pDialog.hide();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                showToast("Error JSON");
                                pickActaActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        pDialog.hide();
                                    }
                                });
                            }
                        } else {
                            showToast("Error al intentar acceder al Servidor");
                            pickActaActivity.this.runOnUiThread(new Runnable() {
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
                        .appendPath("inspector")
                        .appendPath("verActasRealizadas.php")
                        .appendQueryParameter("id_ins", id_ins);
                String myUrl = builder.build().toString();
                client.excecute(myUrl);
            }
        }).start();
    }


    public void seleccionarItem(){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int posicion,
                                    long arg3) {
                //Log.i("Selected Item in list", arg1.toString());
                HashMap<String, String> item_seleccionado = new HashMap<String, String>();
                System.out.print(ActasList.get(posicion));
                item_seleccionado=(HashMap<String, String>) ActasList.get(posicion);
                final String id_AsignarInspector=item_seleccionado.get("id_AsignarInspector");

                new AlertDialog.Builder(pickActaActivity.this)
                        .setTitle("Imprimir acta")
                        .setMessage("¿Desea imprimir el acta seleccionada?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                //IMRPIMIR
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();



            }
        });
    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(pickActaActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
