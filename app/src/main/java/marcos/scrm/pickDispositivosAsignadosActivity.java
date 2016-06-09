package marcos.scrm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

public class pickDispositivosAsignadosActivity extends AppCompatActivity {
    private String sucursal_selec, empresa_selec, direccion_selec, sucursal_idSelec;
    private TextView tvError, tvNombreEmpresa;
    private ListView lv;
    private Button btnAtras;
    ArrayList<HashMap<String, String>> DispositivosList = new ArrayList<HashMap<String, String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_dispositivos_asignados);
        tvError = (TextView) findViewById(R.id.tvError);
        tvNombreEmpresa = (TextView) findViewById(R.id.tvNombreEmpresa);
        lv = (ListView) findViewById(R.id.listView);
        btnAtras = (Button) findViewById(R.id.btnAtras);

        final SimpleAdapter adapter_empresa = new SimpleAdapter(pickDispositivosAsignadosActivity.this, DispositivosList,
                R.layout.two_line, new String[]{"id_dis", "fechahs_asig"},
                new int[]{
                        R.id.linea_a_2,
                        R.id.linea_b_2,});
        lv.setAdapter(adapter_empresa);

        Intent startingIntent = getIntent();
        if (startingIntent != null) {
            Bundle b = startingIntent
                    .getBundleExtra("android.intent.extra.INTENT");
            sucursal_selec = (String) getIntent().getSerializableExtra("sucursal_nombre");
            empresa_selec = (String) getIntent().getSerializableExtra("empresa_nombre");
            direccion_selec = (String) getIntent().getSerializableExtra("sucursal_direccion");
            sucursal_idSelec = (String) getIntent().getSerializableExtra("sucursal_id");
        }

        cargarDispositivosList();
        seleccionarItem();

        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(pickDispositivosAsignadosActivity.this, AsignarActivity.class);
                startActivity(i);
            }
        });


    }

    public void cargarDispositivosList() {
        DispositivosList.clear();
        final ProgressDialog pDialog = new ProgressDialog(pickDispositivosAsignadosActivity.this);
        pDialog.setMessage("Buscando Dispositivos...");
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

                                    HashMap<String, String> item = new HashMap<String, String>();
                                    item.put("id_dis", "Agregar nuevo dispositivo");
                                    item.put("fechaAlta", " ");
                                    DispositivosList.add(item);

                                    final SimpleAdapter adapter_empresa = new SimpleAdapter(pickDispositivosAsignadosActivity.this, DispositivosList,
                                            R.layout.two_line, new String[]{"id_dis","fechaAlta"},
                                            new int[]{
                                                    R.id.linea_a_2,
                                                    R.id.linea_b_2,});
                                    pickDispositivosAsignadosActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            lv.setAdapter(adapter_empresa);
                                            pDialog.hide();
                                        }
                                    });
                                } else {
                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                    HashMap<String, String> item = new HashMap<String, String>();
                                    item.put("id_dis", "Agregar nuevo dispositivo");
                                    item.put("fechaAlta", " ");
                                    DispositivosList.add(item);
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                                        String id_dis = jsonobject.getString("id_dis");
                                        String fechaAlta = jsonobject.getString("fechaAlta");

                                        item = new HashMap<String, String>();
                                        item.put("id_dis", id_dis);
                                        item.put("fechaAlta", "Fecha de Alta: " +  fechaAlta);
                                        DispositivosList.add(item);
                                    }


                                    final SimpleAdapter adapter_empresa = new SimpleAdapter(pickDispositivosAsignadosActivity.this, DispositivosList,
                                            R.layout.two_line, new String[]{"id_dis","fechaAlta"},
                                            new int[]{
                                                    R.id.linea_a_2,
                                                    R.id.linea_b_2,});

                                    pickDispositivosAsignadosActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            lv.setAdapter(adapter_empresa);
                                            pDialog.hide();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                tvError.setText("Error JSON");
                                pickDispositivosAsignadosActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });

                            }
                        } else {
                            showToast("Error al intentar acceder al Servidor");
                            tvError.setText("Error al intentar acceder a servidor");
                            pickDispositivosAsignadosActivity.this.runOnUiThread(new Runnable() {
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
                        .appendPath("listarDispositivos.php")
                        .appendQueryParameter("id_suc", sucursal_idSelec);
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
                if(posicion==0){
                    Intent i = new Intent(pickDispositivosAsignadosActivity.this, AsignarActivity.class);
                    i.putExtra("sucursal_id", sucursal_idSelec);
                    i.putExtra("sucursal_nombre", sucursal_selec);
                    i.putExtra("sucursal_direccion", direccion_selec);
                    i.putExtra("empresa_nombre", empresa_selec);
                    startActivity(i);
                }else{
                    System.out.print(DispositivosList.get(posicion));
                    item_seleccionado=(HashMap<String, String>) DispositivosList.get(posicion);
                    final String id_dis=item_seleccionado.get("id_dis");

                    new AlertDialog.Builder(pickDispositivosAsignadosActivity.this)
                            .setTitle("Eliminar")
                            .setMessage("¿Desea quitar el dispositivo Nro: " +id_dis + " de la sucursal " + sucursal_selec + "?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            final ProgressDialog pDialog = new ProgressDialog(pickDispositivosAsignadosActivity.this);
                                            pDialog.setMessage("Eliminando asignacion...");
                                            pDialog.show();

                                            HttpClient client = new HttpClient(new OnHttpRequestComplete() {
                                                @Override
                                                public void onComplete(Response status) {
                                                    if (status.isSuccess()) {
                                                        Gson gson = new GsonBuilder().create();
                                                        try {
                                                            JSONObject jsono = new JSONObject(status.getResult());
                                                            // JSONArray jsonarray = jsono.getJSONArray("rows");
                                                            String cantidad = jsono.getString("total");
                                                            if (jsono.getString("error").equals("-1")) {
                                                                tvError.setText(jsono.getString("descripcion"));
                                                                pDialog.hide();
                                                            }else{
                                                                Toast msj = new Toast(pickDispositivosAsignadosActivity.this);
                                                                msj.makeText(pickDispositivosAsignadosActivity.this, "Dispositivo eliminado con exito", Toast.LENGTH_SHORT).show();
                                                                cargarDispositivosList();
                                                            }
                                                            pDialog.hide();
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
                                                    .appendPath("hisoAsign")
                                                    .appendPath("EliminarAsigancion.php")
                                                    .appendQueryParameter("id_dis", id_dis);
                                            String myUrl = builder.build().toString();
                                            client.excecute(myUrl);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                }


            }
        });
    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(pickDispositivosAsignadosActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
