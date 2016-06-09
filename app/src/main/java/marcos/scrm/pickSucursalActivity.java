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

public class pickSucursalActivity extends AppCompatActivity {
    private ListView lv;
    private TextView tvError, tvNombreEmpresa;
    private String cuit_empSelec;
    private Button btnAtras;
    ArrayList<HashMap<String, String>> SucursalList = new ArrayList<HashMap<String, String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_sucursal);

        tvError = (TextView) findViewById(R.id.tvError);
        tvNombreEmpresa = (TextView) findViewById(R.id.tvNombreEmpresa);
        lv = (ListView) findViewById(R.id.listView);
        btnAtras = (Button) findViewById(R.id.btnAtras);

        final SimpleAdapter adapter_empresa = new SimpleAdapter(pickSucursalActivity.this, SucursalList,
                R.layout.two_line, new String[]{"sucursal_nombre", "sucursal_direccion"},
                new int[]{
                        R.id.linea_a_2,
                        R.id.linea_b_2,});
        lv.setAdapter(adapter_empresa);



        Intent startingIntent = getIntent();
        if (startingIntent != null) {
            Bundle b = startingIntent
                    .getBundleExtra("android.intent.extra.INTENT");
            cuit_empSelec = (String) getIntent().getSerializableExtra("cuit_emp");
            tvNombreEmpresa.setText((String) getIntent().getSerializableExtra("nombreEmpresa"));
        }

        cargarSucursalList();
        seleccionarItem();


        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(pickSucursalActivity.this, pickEmpresaActivity.class);
                startActivity(i);
            }
        });



    }

    public void cargarSucursalList() {
        final ProgressDialog pDialog = new ProgressDialog(pickSucursalActivity.this);
        pDialog.setMessage("Buscando sucursales...");
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
                                    pickSucursalActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });
                                } else {
                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                                        String sucursal_nombre = jsonobject.getString("nombre");
                                        String sucursal_direccion = jsonobject.getString("direccion");
                                        String sucursal_id = jsonobject.getString("id");


                                        HashMap<String, String> item = new HashMap<String, String>();
                                        item.put("sucursal_nombre", sucursal_nombre);
                                        item.put("sucursal_id", sucursal_id);
                                        item.put("sucursal_direccion", sucursal_direccion);
                                        SucursalList.add(item);
                                    }

                                    final SimpleAdapter adapter_empresa = new SimpleAdapter(pickSucursalActivity.this, SucursalList,
                                            R.layout.two_line, new String[]{"sucursal_nombre","sucursal_direccion"},
                                            new int[]{
                                                    R.id.linea_a_2,
                                                    R.id.linea_b_2,});

                                    pickSucursalActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            lv.setAdapter(adapter_empresa);
                                            pDialog.hide();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                pickSucursalActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });
                            }
                        } else {
                            showToast("Error al intentar acceder al Servidor");
                            pickSucursalActivity.this.runOnUiThread(new Runnable() {
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
                        .appendPath("sucursal")
                        .appendPath("listarSucursal.php")
                        .appendQueryParameter("cuit_emp", cuit_empSelec);
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
                System.out.print(SucursalList.get(posicion));
                item_seleccionado=(HashMap<String, String>) SucursalList.get(posicion);
                String sucursal_nombre=item_seleccionado.get("sucursal_nombre");
                String sucursal_direccion=item_seleccionado.get("sucursal_direccion");
                String sucursal_id=item_seleccionado.get("sucursal_id");

                Intent i = new Intent(pickSucursalActivity.this, pickDispositivosAsignadosActivity.class);
                i.putExtra("sucursal_id", sucursal_id);
                i.putExtra("sucursal_nombre", sucursal_nombre);
                i.putExtra("sucursal_direccion", sucursal_direccion);
                i.putExtra("empresa_nombre", tvNombreEmpresa.getText().toString());
                startActivity(i);

            }
        });
    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(pickSucursalActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
