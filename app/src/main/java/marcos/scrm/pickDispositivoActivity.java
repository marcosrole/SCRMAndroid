package marcos.scrm;

import android.app.ProgressDialog;
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

public class pickDispositivoActivity extends AppCompatActivity {

    private ListView lv;
    private TextView tvError;
    private Button btnAtras;
    ArrayList<HashMap<String, String>> DispositivoList = new ArrayList<HashMap<String, String>>();

    String sucursal_nombre, direccion_sucursal, empresa_nombre, id_sucursal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_dispositivo);

        tvError = (TextView) findViewById(R.id.tvError);
        lv = (ListView) findViewById(R.id.listView);
        btnAtras = (Button) findViewById(R.id.btnAtras);

        Intent startingIntent = getIntent();
        if (startingIntent != null) {
            Bundle b = startingIntent
                    .getBundleExtra("android.intent.extra.INTENT");
            sucursal_nombre=(String) getIntent().getSerializableExtra("sucursal_nombre");
            empresa_nombre=(String) getIntent().getSerializableExtra("empresa_nombre");
            direccion_sucursal=(String) getIntent().getSerializableExtra("sucursal_direccion");
            id_sucursal = (String) getIntent().getSerializableExtra("sucursal_id");
        }

        final SimpleAdapter adapter_empresa = new SimpleAdapter(pickDispositivoActivity.this, DispositivoList,
                R.layout.two_line, new String[]{"id_dis","mac"},
                new int[]{
                        R.id.linea_a_2,
                        R.id.linea_b_2,});
        lv.setAdapter(adapter_empresa);

        cargarDispositivoList();
        seleccionarItem();

        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(pickDispositivoActivity.this, AsignarActivity.class);
                startActivity(i);
            }
        });

    }

    public void cargarDispositivoList() {
        final ProgressDialog pDialog = new ProgressDialog(pickDispositivoActivity.this);
        pDialog.setMessage("Listando dispositivos...");
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
                                    pickDispositivoActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });
                                } else {
                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                                        String id_dis = jsonobject.getString("id");
                                        String mac = jsonobject.getString("mac");


                                        HashMap<String, String> item = new HashMap<String, String>();
                                        item.put("id_dis", id_dis);
                                        item.put("mac", mac);
                                        DispositivoList.add(item);
                                    }

                                    final SimpleAdapter adapter_empresa = new SimpleAdapter(pickDispositivoActivity.this, DispositivoList,
                                            R.layout.two_line, new String[]{"id_dis","mac"},
                                            new int[]{
                                                    R.id.linea_a_2,
                                                    R.id.linea_b_2,});

                                    pickDispositivoActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            lv.setAdapter(adapter_empresa);
                                            pDialog.hide();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                showToast("Error JSON");
                                pickDispositivoActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });

                            }
                        } else {
                            showToast("Error al intentar acceder al Servidor");
                            pickDispositivoActivity.this.runOnUiThread(new Runnable() {
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
                        .appendPath("dispositivo")
                        .appendPath("listarDispositivo.php");
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
                System.out.print(DispositivoList.get(posicion));
                item_seleccionado=(HashMap<String, String>) DispositivoList.get(posicion);
                String id_dis=item_seleccionado.get("id_dis");
                String mac=item_seleccionado.get("mac");

                Intent i = new Intent(pickDispositivoActivity.this, AsignarActivity.class);
                i.putExtra("id_dis",id_dis);
                i.putExtra("mac",mac);
                i.putExtra("sucursal_nombre", sucursal_nombre);
                i.putExtra("sucursal_direccion", direccion_sucursal);
                i.putExtra("empresa_nombre", empresa_nombre);
                i.putExtra("sucursal_id", id_sucursal);
                startActivity(i);

            }
        });
    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(pickDispositivoActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
