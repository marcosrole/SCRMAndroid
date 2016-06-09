package marcos.scrm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import clases.Usuario;
import devazt.networking.ConfigServidor;
import devazt.networking.HttpClient;
import devazt.networking.OnHttpRequestComplete;
import devazt.networking.Response;
import util.General;

public class MainActivity extends AppCompatActivity {

    LinearLayout stackContent;
    TextView tvError;
    EditText etName, etPass;
    Button btnEntrar;
    Usuario usr = new Usuario();
    String id_ins;
    General archivoTXT = new General();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stackContent = (LinearLayout) findViewById(R.id.StackContent);

        tvError = (TextView) findViewById(R.id.tvError);

        etName = (EditText) findViewById(R.id.etName);
        etPass = (EditText) findViewById(R.id.etPass);

        btnEntrar = (Button) findViewById(R.id.btnEntrar);

        /*new AlertDialog.Builder(MainActivity.this)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();*/

    }

    public void validarUsuario(final View view){
        if(validarDatos()){

            final ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage("Validando datos...");
            pDialog.show();

            new Thread(new Runnable() {
                public void run() {
                    HttpClient client = new HttpClient(new OnHttpRequestComplete() {

                        public void onComplete(Response status) {
                            if(status.isSuccess()){
                                Gson gson = new GsonBuilder().create();
                                try{
                                    JSONObject jsono = new JSONObject(status.getResult());
                                    // JSONArray jsonarray = jsono.getJSONArray("rows");
                                    String cantidad = jsono.getString("total");
                                    if(cantidad.equals("1")){

                                        JSONArray jsonarray = jsono.getJSONArray("rows");
                                        JSONObject jsonobject = jsonarray.getJSONObject(0);

                                        id_ins=jsonobject.getString("id_ins");
                                        archivoTXT.grabar(view, "attributes_usr.txt","id_ins=" + id_ins);
                                        showToast("Hola " + jsonobject.getString("nombre") + "!");

                                        //Modificar Intervaz de Usuario
                                        tvError.post(new Runnable() {
                                            public void run() {
                                                tvError.setText("");
                                                Intent i = new Intent(MainActivity.this, MenuActivity.class);
                                                startActivity(i);
                                            }
                                        });



                                        //Intent i = new Intent(MainActivity.this, MenuActivity.class);
                                       // startActivity(i);

                                    }else{
                                        showToast("Usuario o contraseña Incorrecto");
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                pDialog.hide();
                                            }
                                        });
                                    }

                            /*ArrayList<Usuario> usuarios= new ArrayList<Usuario>();
                            for(int i = 0; i < jsonarray.length(); i++) {
                                String usuario = jsonarray.getString(i);
                                //System.out.println(usuario);
                                Usuario usr = gson.fromJson(usuario,Usuario.class);
                                usuarios.add(usr);
                                //System.err.println(p.getName());
                                TextView t = new TextView(getBaseContext());
                                t.setText(usr.getName());
                                stackContent.addView(t);
                            }*/

                                }catch (final Exception e){
                                    tvError.post(new Runnable() {
                                        public void run() {
                                            e.printStackTrace();
                                            pDialog.hide();
                                        }
                                    });

                                }
                            }else {
                                showToast("Error al intentar acceder al Servidor");
                                tvError.post(new Runnable() {
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
                            .appendPath("usuario")
                            .appendPath("listarUsuario.php")
                            .appendQueryParameter("name", usr.getName())
                            .appendQueryParameter("pass", usr.getPass());
                    String myUrl = builder.build().toString();
                    client.excecute(myUrl);
                }
            }).start();
        }

    }
    public boolean validarDatos(){
        boolean validado=true;

        String name = etName.getText().toString();
        String pass = etPass.getText().toString();

        if(name.equals("") || pass.equals("")){
            Toast msj = new Toast(MainActivity.this);
            msj.makeText(MainActivity.this,"Ingrese usuario y contraseña", Toast.LENGTH_LONG).show();
            validado=false;
        }else{

            usr.setName(name);
            usr.setPass(pass);
        }

        return validado;
    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }



}
