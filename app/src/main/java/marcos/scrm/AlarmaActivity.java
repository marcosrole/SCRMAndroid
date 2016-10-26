package marcos.scrm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

import clases.ItemAlarma;
import devazt.networking.ConfigServidor;
import devazt.networking.HttpClient;
import devazt.networking.OnHttpRequestComplete;
import devazt.networking.Response;
import util.General;



class ItemAlarmaAdapter extends BaseAdapter{
    protected Activity activity;
    protected ArrayList<ItemAlarma> items;


    public ItemAlarmaAdapter(Activity activity, ArrayList<ItemAlarma> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent) {
        View vi=contentView;

        if(contentView == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.six_line_alarma, null);
        }

        ItemAlarma item = items.get(position);

        ImageView image = (ImageView) vi.findViewById(R.id.imgIzquierdaAlarma);


        int imageResource = activity.getResources()
                .getIdentifier(item.getRutaImagen(), null,
                        activity.getPackageName());

        image.setImageDrawable(activity.getResources().getDrawable(
                imageResource));



        TextView lineaA = (TextView) vi.findViewById(R.id.linea_a_6);
        lineaA.setText(item.getLinea_a());

        TextView lineaB = (TextView) vi.findViewById(R.id.linea_b_6);
        lineaB.setText(item.getLinea_b());

        TextView lineaC = (TextView) vi.findViewById(R.id.linea_c_6);
        lineaC.setText(item.getLinea_c());

        TextView lineaD = (TextView) vi.findViewById(R.id.linea_d_6);
        lineaD.setText(item.getLinea_d());

        TextView lineaE = (TextView) vi.findViewById(R.id.linea_e_6);
        lineaE.setText(item.getLinea_e());

        return vi;
    }
}


public class AlarmaActivity extends AppCompatActivity {
    ListView lv;
    ArrayList<ItemAlarma> items = new ArrayList<ItemAlarma>();
    TextView tvError;
    String id_ins, id_AsiIns;
    ImageView imgAlarma;
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
       // imgAlarma = (ImageView)findViewById(R.id.imgAlarma);

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
                                String hs = jsonobject.getString("hs") + "hs";
                                String observacion = jsonobject.getString("observacion");
                                if(observacion==null)observacion=hs;
                                else observacion = observacion + " - " + hs;
                                String id_dis = jsonobject.getString("id_dis");
                                String alarmaTomada = jsonobject.getString("alarmaTomada");
                                id_AsiIns = jsonobject.getString("id_AsignarInspector");


                                if(alarmaTomada.equals("0")){
                                    items.add(new ItemAlarma(i,alarma,sucursal,empresa,direccion,observacion, "drawable/alert46",id_dis,id_AsiIns));
                                }else items.add(new ItemAlarma(i,alarma,sucursal,empresa,direccion,observacion, "drawable/ok46",id_dis,id_AsiIns, alarmaTomada));

                            }

                            ItemAlarmaAdapter adapter = new ItemAlarmaAdapter(AlarmaActivity.this, items);
                            lv.setAdapter(adapter);

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
            case R.id.mcSolucionada:

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
                                    Toast.makeText(AlarmaActivity.this, "La alarma ha sido solucionada", Toast.LENGTH_SHORT).show();
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


                finish();
                startActivity(getIntent());

                break;
            case R.id.mcVerDetalle:
                String id_dis=items.get(position).getId_dis();
                Intent i = new Intent(AlarmaActivity.this, DetalleDispoActivity.class);
                i.putExtra("id_dis", id_dis);
                startActivity(i);
                Toast.makeText(this, "Detalles del Dipositivo: " + id_dis, Toast.LENGTH_SHORT).show();
                break;
            case R.id.mcMultar:
                String id_AsiIns=items.get(position).getId_AsiIns();
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


    public void seleccionarItem(){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(final AdapterView<?> arg0, View arg1, int posicion,
                                    long arg3) {
                //Log.i("Selected Item in list", arg1.toString());

                System.out.print(items.get(posicion));
                ItemAlarma item_seleccionado = new ItemAlarma();
                item_seleccionado.setId(items.get(posicion).getId());
                item_seleccionado.setLinea_a(items.get(posicion).getLinea_a());
                item_seleccionado.setLinea_b(items.get(posicion).getLinea_b());
                item_seleccionado.setLinea_c(items.get(posicion).getLinea_c());
                item_seleccionado.setLinea_d(items.get(posicion).getLinea_d());
                item_seleccionado.setLinea_e(items.get(posicion).getLinea_e());
                item_seleccionado.setRutaImagen(items.get(posicion).getRutaImagen());

                String id_AsignarInspector= String.valueOf(items.get(posicion).getId());
                Log.v("Insepctro seleccionado",id_AsignarInspector);

                if(!items.get(posicion).getAlarmaTomada()){
                         //Muestro el msj
                        new AlertDialog.Builder(AlarmaActivity.this)
                                .setTitle("Tomar alarma")
                                .setMessage("¿Desea tomar la alarma?")
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
                                                            Toast.makeText(AlarmaActivity.this, "Alarma asiganda",Toast.LENGTH_SHORT).show();
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
                                                .appendPath("alarmaTomada.php")
                                                .appendQueryParameter("id_AsiIns", id_AsiIns);
                                        String myUrl = builder.build().toString();
                                        client.excecute(myUrl);

                                        finish();
                                        startActivity(getIntent());
                                    }

                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                }else{
                    Toast.makeText(AlarmaActivity.this, "Mantenga presionado para mas opciones", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }


}
