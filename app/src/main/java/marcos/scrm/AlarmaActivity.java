package marcos.scrm;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
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

public class AlarmaActivity extends AppCompatActivity {
    ListView lv;
    ArrayList<HashMap<String, String>> AlarmaList = new ArrayList<HashMap<String, String>>();
    TextView tvError;
    String id_ins, id_AsiIns;
    Button btnAtras;
    General archivoTXT = new General();
    //final Intent modIntent = new Intent(super.getIntent());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);
        lv = (ListView) findViewById(R.id.listViewAlarma);
        tvError = (TextView)findViewById(R.id.tvError);
        btnAtras = (Button)findViewById(R.id.btnAtras);

        View v;


        Bundle extras = getIntent().getExtras();
        Intent modIntent = new Intent();
        if (extras != null) {
            if(extras.get("notificationID") != null){
                 //Eliminar Notificacion de la alarma
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Cancelamos la Notificacion que hemos comenzado
                //nm.cancel(Integer.parseInt(getIntent().getExtras().getString("notificationID")));
                nm.cancel(1);
            }
        }





        archivoTXT.recuperar_id_ins("attributes_usr.txt");
        id_ins=archivoTXT.getId_ins();

        getVistas();

        final ProgressDialog pDialog = new ProgressDialog(AlarmaActivity.this);
        pDialog.setMessage("Buscando alarmas...");
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
                        if (jsono.getString("total").equals("0")) {
                            tvError.setText(jsono.getString("descripcion"));
                            pDialog.hide();
                        } else {
                            tvError.setText("");
                            JSONArray jsonarray = jsono.getJSONArray("rows");
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                String alarma = jsonobject.getString("descripcion");
                                String sucursal = jsonobject.getString("sucursal");
                                String empresa = jsonobject.getString("empresa");
                                String direccion = jsonobject.getString("direccion");
                                String observacion = jsonobject.getString("observacion");
                                if(observacion==null)observacion="";
                                String id_dis = jsonobject.getString("id_dis");
                                String hs = jsonobject.getString("hs") + "hs";
                                String id_AsignarInspector = jsonobject.getString("id_AsignarInspector");
                                id_AsiIns = jsonobject.getString("id_AsignarInspector");


                                HashMap<String, String> item = new HashMap<String, String>();
                                item.put("alarma", alarma);
                                item.put("sucursal", sucursal);
                                item.put("empresa", empresa);
                                item.put("direccion", direccion);
                                item.put("observacion", hs + " - " + observacion);
                                item.put("id_dis", id_dis);
                                item.put("id_AsignarInspector", id_AsignarInspector);
                                AlarmaList.add(item);
                            }

                            final SimpleAdapter adapter_alarma = new SimpleAdapter(AlarmaActivity.this, AlarmaList,
                                    R.layout.five_line, new String[]{"alarma","sucursal","empresa","direccion","observacion"},
                                    new int[]{
                                            R.id.linea_a_5,
                                            R.id.linea_b_5,
                                            R.id.linea_c_5,
                                            R.id.linea_d_5,
                                            R.id.linea_e_5,
                                            });
                            lv.setAdapter(adapter_alarma);

                            pDialog.hide();
                        }
                        pDialog.hide();
                    } catch (Exception e) {
                        tvError.setText("Error jSON");
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
                .appendPath("asignarInspector")
                .appendPath("listarAlarma.php")
                .appendQueryParameter("id_ins", id_ins);
        String myUrl = builder.build().toString();
        client.excecute(myUrl);




        seleccionarItem();
    }
    private void getVistas() {
        lv = (ListView) findViewById(R.id.listViewAlarma);
        // Obtengo los datos para el adaptador de la lista.

        final SimpleAdapter adapter_alarma = new SimpleAdapter(AlarmaActivity.this, AlarmaList,
                R.layout.five_line, new String[]{"alarma","sucursal","empresa","direccion","observacion"},
                new int[]{
                        R.id.linea_a_5,
                        R.id.linea_b_5,
                        R.id.linea_c_5,
                        R.id.linea_d_5,
                        R.id.linea_e_5,
                });
        lv.setAdapter(adapter_alarma);

        // Creo el listener para cuando se hace click en un item de la lista.
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lst, View vistafila,
                                    int posicion, long id) {

            }
        });
        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(AlarmaActivity.this, MenuActivity.class);
                startActivity(i);
            }
        });

        // Registro el ListView para que tenga menú contextual.
        registerForContextMenu(lv);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        // Si se ha hecho LongClick sobre la lista.
        if (v.getId() == R.id.listViewAlarma) {
            // Obtengo la posición de la lista que se ha pulsado
            int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
            // Inflo el menú.
            this.getMenuInflater().inflate(R.menu.menu_contextual, menu);
            // Cambio el título de los menús para incluir el nombre del alumno.
            //menu.findItem(R.id.mnuEditar).setTitle(getString(R.string.editar) +
              //      lstAlumnos.getItemAtPosition(position));
            //menu.findItem(R.id.mnuEliminar).setTitle(getString(R.string.eliminar) +
              //      lstAlumnos.getItemAtPosition(position));

            // Establezco el título que se muestra en el encabezado del menú.
            menu.setHeaderTitle(R.string.elija_una_opcion);
        }
        // Llamo al OnCreateContextMenu del padre por si quiere
        // añadir algún elemento.
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        // Obtengo la posición de la lista que se ha pulsado
        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        // Dependiendo del menú sobre el que se ha pulsado informo al usuario.
        switch (item.getItemId()) {
            case R.id.mcVerDetalle:
                HashMap<String, String> item_seleccionado = new HashMap<String, String>();
                item_seleccionado=(HashMap<String, String>) AlarmaList.get(position);
                String id_dis=item_seleccionado.get("id_dis");
                Intent i = new Intent(AlarmaActivity.this, DetalleDispoActivity.class);
                i.putExtra("id_dis", id_dis);
                startActivity(i);
                mostrarTostada("Detalles del Dipositivo: " + id_dis);
                break;
            case R.id.mcMultar:
                item_seleccionado = new HashMap<String, String>();
                item_seleccionado=(HashMap<String, String>) AlarmaList.get(position);
                String id_AsiIns=item_seleccionado.get("id_AsignarInspector");
                i = new Intent(AlarmaActivity.this, multaActivity.class);
                i.putExtra("id_AsiIns", id_AsiIns);
                startActivity(i);
                break;
            default:
                // Retorno lo que retorne el padre.
                return super.onContextItemSelected(item);
        }
        // Retorno que he gestionado yo el evento.
        return true;
    }

    // Muestra una tostada.
    private void mostrarTostada(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    public void seleccionarItem(){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int posicion,
                                    long arg3) {
                //Log.i("Selected Item in list", arg1.toString());
                HashMap<String, String> item_seleccionado = new HashMap<String, String>();
                System.out.print(AlarmaList.get(posicion));
                item_seleccionado=(HashMap<String, String>) AlarmaList.get(posicion);
                String id_AsignarInspector=item_seleccionado.get("id_AsignarInspector");

                new AlertDialog.Builder(AlarmaActivity.this)
                .setTitle("Alarma verificada")
                .setMessage("¿Desea marcar como solucionado el inconveniente?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog pDialog = new ProgressDialog(AlarmaActivity.this);
                        pDialog.setMessage("Buscando alarma...");
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
                                        if (jsono.getString("total").equals("0")) {
                                            tvError.setText(jsono.getString("descripcion"));
                                            pDialog.hide();
                                        } else {
                                            tvError.setText("");
                                            mostrarTostada("Alarma solucionada");
                                            pDialog.hide();
                                        }
                                        pDialog.hide();
                                    } catch (Exception e) {
                                        tvError.setText("Error jSON");
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
                                .appendPath("asignarInspector")
                                .appendPath("alarmaSolucionada.php")
                                .appendQueryParameter("id_AsiIns", id_AsiIns);
                        String myUrl = builder.build().toString();
                        client.excecute(myUrl);

                        Intent i = new Intent(AlarmaActivity.this, AlarmaActivity.class);
                        startActivity(i);
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

}
