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

public class pickEmpresaActivity extends AppCompatActivity {

    private ListView lv;
    private TextView tvError;
    private Button btnAtras;
    ArrayList<HashMap<String, String>> EmpresaList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_empresa);
        tvError = (TextView) findViewById(R.id.tvError);
        lv = (ListView) findViewById(R.id.listView);
        btnAtras = (Button) findViewById(R.id.btnAtras);


        final SimpleAdapter adapter_empresa = new SimpleAdapter(pickEmpresaActivity.this, EmpresaList,
                R.layout.two_line, new String[]{"empresa_nombre","empresa_cuit"},
                new int[]{
                        R.id.linea_a_2,
                        R.id.linea_b_2,});
        lv.setAdapter(adapter_empresa);

        cargarEmpresaList();
        seleccionarItem();

        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(pickEmpresaActivity.this, AsignarActivity.class);
                startActivity(i);
            }
        });

    }

    public void cargarEmpresaList() {
        final ProgressDialog pDialog = new ProgressDialog(pickEmpresaActivity.this);
        pDialog.setMessage("Buscando empresas...");
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
                                    pickEmpresaActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });
                                } else {
                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                                        String empresa_cuit = jsonobject.getString("cuit");
                                        String empresa_nombre = jsonobject.getString("razonsocial");


                                        HashMap<String, String> item = new HashMap<String, String>();
                                        item.put("empresa_nombre", empresa_nombre);
                                        item.put("empresa_cuit", empresa_cuit);
                                        EmpresaList.add(item);
                                    }

                                    final SimpleAdapter adapter_empresa = new SimpleAdapter(pickEmpresaActivity.this, EmpresaList,
                                            R.layout.two_line, new String[]{"empresa_nombre","empresa_cuit"},
                                            new int[]{
                                                    R.id.linea_a_2,
                                                    R.id.linea_b_2,});
                                    pickEmpresaActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            lv.setAdapter(adapter_empresa);
                                            pDialog.hide();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                pickEmpresaActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });


                            }
                        } else {
                            showToast("Error al intentar acceder al Servidor");
                            tvError.setText("Error al intentar acceder a servidor");
                            pickEmpresaActivity.this.runOnUiThread(new Runnable() {
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
                        .appendPath("empresa")
                        .appendPath("listarEmpresa.php");
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
                System.out.print(EmpresaList.get(posicion));
                item_seleccionado=(HashMap<String, String>) EmpresaList.get(posicion);
               String empresa_cuit=item_seleccionado.get("empresa_cuit");
               String empresa_nombre=item_seleccionado.get("empresa_nombre");

               Intent i = new Intent(pickEmpresaActivity.this, pickSucursalActivity.class);
                i.putExtra("cuit_emp",empresa_cuit);
                i.putExtra("nombreEmpresa",empresa_nombre);
                startActivity(i);

            }
        });
    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(pickEmpresaActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

