package marcos.scrm;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import devazt.networking.ConfigServidor;
import devazt.networking.HttpClient;
import devazt.networking.OnHttpRequestComplete;
import devazt.networking.Response;

public class DetalleDispoActivity extends AppCompatActivity {
    Handler mHandler = new Handler();
    private Button btnSalir, btnAtras;
    private ImageButton btnHsIni, btnHsFin,btnBuscar, btnLimpiar;
    private TextView tvError, tvHsIni, tvHsFin, tvFechaIni, tvFechaFin, tvFechaCensado, tvHsCensado;
    private DatePickerDialog dialogDate = null;
    private EditText etID_dis;
    ArrayList<Entry> entries = new ArrayList<>();
    ArrayList<Entry> entriesLimit = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<String>();
    private String fechaSelecIni;
    private Integer fechaSelecDiaIni=-1;
    private Integer fechaSelecMesIni=-1;
    private Integer fechaSelecAnioIni=-1;
    private Integer hsIni=-1;
    private Integer minutosIni=-1;
    private Integer fechaSelecDiaFin=-1,fechaSelecMesFin=-1,fechaSelecAnioFin=-1, hsFin=-1,minutosFin=-1;

    private String hsIniLink, hsFinLink, fechaLink;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_dispo);
        btnBuscar = (ImageButton) findViewById(R.id.btnBuscar);
        btnLimpiar = (ImageButton) findViewById(R.id.btnLimpiar);
        btnSalir = (Button) findViewById(R.id.btnSalir);
        btnAtras = (Button) findViewById(R.id.btnAtras);
        tvError = (TextView) findViewById(R.id.tvError);
        tvFechaIni = (TextView) findViewById(R.id.tvFechaIni);
        tvFechaFin = (TextView) findViewById(R.id.tvFechaFin);
        tvFechaCensado=(TextView) findViewById(R.id.tvFechaCensado);
        tvHsCensado=(TextView) findViewById(R.id.tvhsCensado);
        tvHsIni = (TextView) findViewById(R.id.tvHsIni);
        tvHsFin = (TextView) findViewById(R.id.tvHsFin);
        etID_dis = (EditText) findViewById(R.id.etID_dis);

        btnHsIni = (ImageButton)findViewById(R.id.btnHsIni);
        btnHsFin = (ImageButton)findViewById(R.id.btnHsFin);

        Intent startingIntent = getIntent();
        if (startingIntent != null) {
            Bundle b=getIntent().getExtras();
            if (b!=null){
                String id_dis = b.getString("id_dis").toString();
                if(id_dis!=null){
                    etID_dis.setText(id_dis);
                    mostrarDetalles(id_dis);
                }
            }



        }

        //Actuazliar la tabla cada cierto tiempo
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    try {
                        Thread.sleep(1000*60);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                final String id_dis = etID_dis.getText().toString();
                                final Toast msj = new Toast(DetalleDispoActivity.this);
                                if (!id_dis.equals("")) {
                                    final ProgressDialog pDialog = new ProgressDialog(DetalleDispoActivity.this);
                                    pDialog.setMessage("Actulizando datos...");
                                    pDialog.show();

                                    HttpClient client = new HttpClient(new OnHttpRequestComplete() {
                                        @Override
                                        public void onComplete(Response status) {
                                            if (status.isSuccess()) {
                                                Gson gson = new GsonBuilder().create();
                                                try {
                                                    JSONObject jsono = new JSONObject(status.getResult());
                                                    if (!jsono.getString("error").equals("0")) {
                                                        tvError.setText(jsono.getString("descripcion"));
                                                        pDialog.hide();
                                                    } else {
                                                        tvError.setText("");
                                                        JSONArray jsonarray = jsono.getJSONArray("rows");
                                                        for (int i = 0; i < jsonarray.length()-1; i++) {
                                                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                                                            Integer db = Integer.parseInt(jsonobject.getString("db"));
                                                            String hs = jsonobject.getString("fechahs");
                                                            entries.add(new Entry(db, i));
                                                            entriesLimit.add(new Entry(20, i));
                                                            labels.add(hs);
                                                        }

                                                        determinardBlimite(id_dis);
                                                        pDialog.hide();
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
                                            .appendPath("detalle_dispo")
                                            .appendPath("listarDetalleDispo.php")
                                            .appendQueryParameter("id_dis", id_dis);
                                    String myUrl = builder.build().toString();
                                    client.excecute(myUrl);

                                }
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
        */

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                final String id_dis = etID_dis.getText().toString();
                final Toast msj = new Toast(DetalleDispoActivity.this);
                if(id_dis.equals("")){
                    etID_dis.requestFocus();
                    msj.makeText(DetalleDispoActivity.this,"Ingrese un ID de dispositivo a buscar", Toast.LENGTH_SHORT).show();
                }else{
                    limpiarGrafico();
                    entries.clear();
                    labels.clear();

                    if(!tvFechaIni.getText().toString().equals("") && !tvFechaFin.getText().toString().equals("")){
                        try {
                            if(verificarRango()){
                                final ProgressDialog pDialog = new ProgressDialog(DetalleDispoActivity.this);
                                pDialog.setMessage("Buscando Dispositivo...");
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
                                                        if (!jsono.getString("error").equals("0")) {
                                                            showToast(jsono.getString("descripcion"));
                                                            DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    pDialog.hide();
                                                                }
                                                            });
                                                        } else {
                                                            JSONArray jsonarray = jsono.getJSONArray("rows");
                                                            for (int i = 0; i < jsonarray.length(); i++) {
                                                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                                                Integer db = Integer.parseInt(jsonobject.getString("db"));
                                                                String hs = jsonobject.getString("hs");
                                                                entries.add(new Entry(db, i));
                                                                entriesLimit.add(new Entry(20, i));
                                                                labels.add(hs);
                                                            }
                                                            final JSONObject jsonobjectPRIMERO = jsonarray.getJSONObject(0);
                                                            final JSONObject jsonobjectULTIMO = jsonarray.getJSONObject(jsonarray.length() - 1);


                                                            DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    try {
                                                                        tvFechaCensado.setText(jsonobjectULTIMO.getString("fecha") + " ");
                                                                        tvHsCensado.setText(jsonobjectPRIMERO.getString("hs") + " - " + jsonobjectULTIMO.getString("hs"));
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    determinardBlimite(id_dis);
                                                                    pDialog.hide();
                                                                }
                                                            });
                                                        }
                                                    } catch (Exception e) {
                                                        showToast("Error JSON: listarDetalleDispoRango ");
                                                        DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                pDialog.hide();
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    showToast("Error al intentar acceder a servidor");
                                                    DetalleDispoActivity.this.runOnUiThread(new Runnable() {
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
                                                .appendPath("detalle_dispo")
                                                .appendPath("listarDetalleDispoRango.php")
                                                .appendQueryParameter("id_dis", id_dis)
                                                //  .appendQueryParameter("hsIni", String.valueOf(hsIni)+":"+String.valueOf(minutosIni)+":00")
                                                //  .appendQueryParameter("hsFin", String.valueOf(hsFin)+":"+String.valueOf(minutosFin)+":00")
                                                //  .appendQueryParameter("fecha", String.valueOf(fechaSelecDiaIni)+"/"+String.valueOf(fechaSelecMesIni)+"/"+String.valueOf(fechaSelecAnioFin));

                                                .appendQueryParameter("hsIni", hsIniLink)
                                                .appendQueryParameter("hsFin", hsFinLink)
                                                .appendQueryParameter("fecha", fechaLink);
                                        String myUrl = builder.build().toString();
                                        client.excecute(myUrl);
                                    }
                                }).start();
                            }
                        } catch (ParseException e) {
                            showToast("Error funcion: validarDatos()");
                        }
                    }else {
                        final ProgressDialog pDialog = new ProgressDialog(DetalleDispoActivity.this);
                        pDialog.setMessage("Generando gráfico...");
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
                                                if (!jsono.getString("error").equals("0")) {
                                                    showToast(jsono.getString("descripcion"));
                                                    DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            pDialog.hide();
                                                        }
                                                    });
                                                } else {
                                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                                    for (int i = 0; i < jsonarray.length(); i++) {
                                                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                                                        Integer db = Integer.parseInt(jsonobject.getString("db"));
                                                        String hs = jsonobject.getString("hs");
                                                        entries.add(new Entry(db, i));
                                                        entriesLimit.add(new Entry(20, i));
                                                        labels.add(hs);
                                                    }
                                                    final JSONObject jsonobjectPRIMERO = jsonarray.getJSONObject(0);
                                                    final JSONObject jsonobjectULTIMO = jsonarray.getJSONObject(jsonarray.length() - 1);

                                                    DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            try {
                                                                tvFechaCensado.setText(jsonobjectULTIMO.getString("fecha") + " ");
                                                                tvHsCensado.setText(jsonobjectPRIMERO.getString("hs") + " - " + jsonobjectULTIMO.getString("hs"));
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                            determinardBlimite(id_dis);
                                                            pDialog.hide();
                                                        }
                                                    });
                                                }
                                            } catch (final Exception e) {
                                                showToast("Error JSON: listarDetalleDispo");
                                                DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        e.printStackTrace();
                                                        pDialog.hide();
                                                    }
                                                });

                                            }
                                        } else {
                                            showToast("Error al intentar acceder a servidor");
                                            DetalleDispoActivity.this.runOnUiThread(new Runnable() {
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
                                        .appendPath("detalle_dispo")
                                        .appendPath("listarDetalleDispo.php")
                                        .appendQueryParameter("id_dis", id_dis);
                                String myUrl = builder.build().toString();
                                client.excecute(myUrl);

                            }
                        }).start();
                    }
                }
            }
        });
        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                limpiarGrafico();
                tvFechaIni.setText("");
                tvFechaFin.setText("");
                tvHsIni.setText("");
                tvHsFin.setText("");
                tvFechaCensado.setText("");
                tvHsCensado.setText("");
            }
        });

        btnHsIni.setOnClickListener(new View.OnClickListener() {
            public DatePickerDialog dialogDate;

            public void onClick(final View view) {
                showDate(true);
                limpiarGrafico();
            }
        });

        btnHsFin.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                showDate(false);
                limpiarGrafico();
            }
        });

        btnAtras.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {

                Intent i = new Intent(DetalleDispoActivity.this, MenuActivity.class);
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

    public void determinardBlimite(final String id_dis){
        final ProgressDialog pDialog = new ProgressDialog(DetalleDispoActivity.this);
        pDialog.setMessage("Determinando Limite de aceptacion...");
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
                                if(jsono.getString("error").equals("-3")){
                                    showToast(jsono.getString("descripcion"));
                                    DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });
                                }else{
                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                    JSONObject jsonobject = jsonarray.getJSONObject(0);
                                    final Integer db_permitido= Integer.parseInt(jsonobject.getString("db"));

                                    DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            graficar(id_dis,db_permitido);
                                            pDialog.hide();
                                        }
                                    });

                                }
                            }catch (final Exception e){
                                showToast("Error JSON: dispositivoCalibrado");
                                DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });
                            }
                        }else {
                            showToast("Error al intentar acceder a servidor");
                            DetalleDispoActivity.this.runOnUiThread(new Runnable() {
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

    public void graficar(String id_dis, int db_permitido){

        LineChart lineChart = (LineChart) findViewById(R.id.chart);
        // creating list of entry
        LineDataSet dataset = new LineDataSet(entries, "dB del Dispositivo: " + id_dis);


        // creating labels

        LineData data = new LineData(labels, dataset);

        LimitLine line = new LimitLine(10);
        lineChart.setData(data); // set the data and list of lables into chart
        lineChart.setDescription("dB vs Tiempo");  // set the description

        YAxis yl = lineChart.getAxisLeft();


        LimitLine ll = new LimitLine(db_permitido, "Limite");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(2f);
        ll.setTextColor(Color.RED);
        ll.setTextSize(9f);

        yl.addLimitLine(ll);

        lineChart.animateX(2000);
    }

    public void mostrarDetalles(final String id_dis){
        final ProgressDialog pDialog = new ProgressDialog(DetalleDispoActivity.this);
        pDialog.setMessage("Buscando Dispositivo...");
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
                                if (!jsono.getString("error").equals("0")) {
                                    showToast(jsono.getString("descripcion"));
                                    DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            pDialog.hide();
                                        }
                                    });
                                } else {
                                    JSONArray jsonarray = jsono.getJSONArray("rows");
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                                        Integer db = Integer.parseInt(jsonobject.getString("db"));
                                        String hs = jsonobject.getString("hs");
                                        entries.add(new Entry(db, i));
                                        entriesLimit.add(new Entry(20, i));
                                        labels.add(hs);
                                    }
                                    final JSONObject jsonobjectPRIMERO = jsonarray.getJSONObject(0);
                                    final JSONObject jsonobjectULTIMO = jsonarray.getJSONObject(jsonarray.length() - 1);

                                    showToast(jsono.getString("descripcion"));
                                    DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                tvFechaCensado.setText(jsonobjectULTIMO.getString("fecha") + " ");
                                                tvHsCensado.setText(jsonobjectPRIMERO.getString("hs") + " - " + jsonobjectULTIMO.getString("hs"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            determinardBlimite(id_dis);
                                            pDialog.hide();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                showToast("Error JSON");
                                DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        e.printStackTrace();
                                        pDialog.hide();
                                    }
                                });
                            }
                        } else {
                            showToast("Error al intentar acceder a servidor");
                            DetalleDispoActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    pDialog.hide();
                                }
                            });
                            tvError.setText("Error al intentar acceder a servidor");
                            pDialog.hide();
                        }
                    }
                });


                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority(ConfigServidor.IP)
                        .appendPath(ConfigServidor.URL)
                        .appendPath("detalle_dispo")
                        .appendPath("listarDetalleDispo.php")
                        .appendQueryParameter("id_dis", id_dis);
                String myUrl = builder.build().toString();
                client.excecute(myUrl);
            }
        }).start();
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

    public String updateDate(Integer day, Integer month, Integer year) {
        month = month + 1;
        String fecha="";
        if (day < 10 && month < 10) {
            fecha="0" + day + "/" + "0" + month + "/" + year;
        } else if (month < 10) {
            fecha=day + "/" + "0" + month + "/" + year;
        } else if (day < 10) {
            fecha="0" + day + "/" + month + "/" + year;
        } else {
            fecha=day + "/" + month + "/" + year;
        }
        return fecha;
    }

    public void showDate(final boolean inicio){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String hoy = sdf.format(c.getTime()).toString();


        //String hoy = f_cts_fec.getText().toString();
        final String[] dma = hoy.split("/");
        final int dia = Integer.valueOf(dma[0]);
        final int mes = Integer.valueOf(dma[1]) - 1;
        final int anio = Integer.valueOf(dma[2]);



        this.dialogDate = new DatePickerDialog(DetalleDispoActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        view.updateDate(year, monthOfYear, dayOfMonth);
                        int mescam = monthOfYear + 1;
                        if(inicio){
                            fechaSelecDiaIni=dayOfMonth;
                            fechaSelecMesIni=mescam;
                            fechaSelecAnioIni=year;
                            tvFechaIni.setText(fechaSelecDiaIni+"/"+fechaSelecMesIni+"/"+fechaSelecAnioIni+"  ");
                            dialogDate.hide();
                        }else{
                            fechaSelecDiaFin=dayOfMonth;
                            fechaSelecMesFin=mescam;
                            fechaSelecAnioFin=year;
                            tvFechaFin.setText(fechaSelecDiaFin+"/"+fechaSelecMesFin+"/"+fechaSelecAnioFin+"  ");
                            dialogDate.hide();
                        }

                    }
                }, anio, mes, dia);
        dialogDate.show();
        if(dialogDate.isShowing()){
            showTime(inicio);
        }

    }

    public void showTime(final boolean inicio){
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int  mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog tpd = new TimePickerDialog(DetalleDispoActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        // Display Selected time in textbox
                        //Valido:
                        String hora,mintuos = "";
                        if(hourOfDay<10) hora="0"+hourOfDay;
                        else hora=""+hourOfDay;
                        if(minute<10) mintuos="0"+minute;
                        else mintuos=""+minute;

                        String hs=hora + ":" + mintuos + ":00";
                        boolean valido = true;
                        //if(tvHsIni.getText().toString().equals("")) valido=false;
                        // if(valido) if(restarHs(tvHsIni.getText().toString(),HsFin)<0) valido=false; tvError.setText("Fecha de Fin debe ser mayor a Fecha de Inicio");
                        if(inicio){
                            hsIni=Integer.parseInt(hora);
                            minutosIni=Integer.parseInt(mintuos);
                            tvHsIni.setText(hora+":"+mintuos+":00");
                        }else{
                            hsFin=Integer.parseInt(hora);
                            minutosFin=Integer.parseInt(mintuos);
                            tvHsFin.setText(hora+":"+mintuos+":00");
                        }

                        //if(valido) tvHsFin.setText(fecha + HsFin); tvError.setText("");
                    }
                }, mHour, mMinute, false);
        tpd.show();
    }

    public void limpiarGrafico(){
        tvFechaCensado.setText("");
        tvHsCensado.setText("");
        LineChart lineChart = (LineChart) findViewById(R.id.chart);
        // creating list of entry
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> entriesLimit = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();

        LineDataSet dataset = new LineDataSet(entries, "No existen datos a mostrar");


        // creating labels

        LineData data = new LineData(labels, dataset);

        LimitLine line = new LimitLine(10);
        lineChart.setData(data); // set the data and list of lables into chart
        lineChart.setDescription("No existen datos a mostrar");  // set the description
        lineChart.animateX(2000);
        etID_dis.requestFocus();
    }


    public boolean verificarRango() throws ParseException {

        boolean flag = true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        Date date1 = sdf.parse(fechaSelecAnioIni+"-"+fechaSelecMesIni+"-"+fechaSelecDiaIni);
        Date date2 = sdf.parse(fechaSelecAnioFin+"-"+fechaSelecMesFin+"-"+fechaSelecDiaFin);
        if(date1.compareTo(date2)>0) {
            flag=false;
            showToast("Fecha de Incio no debe ser mayor a la Fecha de Fin");
        }

        if(date1.compareTo(date2)==0){//Comparo hs
            if(flag){
                if((hsFin*3600+minutosFin*60)-(hsIni*3600+minutosIni*60)<0){
                    flag=false;
                    showToast("La Hora de Incio no debe ser mayor a la Hora de Fin");
                }
            }

        }

        if(flag){//Armo los links
            hsIniLink=String.valueOf(hsIni)+":"+String.valueOf(minutosIni)+":00";
            hsFinLink=String.valueOf(hsFin)+":"+String.valueOf(minutosFin)+":00";
            fechaLink=String.valueOf(fechaSelecDiaIni)+"/"+String.valueOf(fechaSelecMesIni)+"/"+String.valueOf(fechaSelecAnioFin);
        }

        return flag;
    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(DetalleDispoActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }




}
