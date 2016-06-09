package marcos.scrm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {
    private Button btnSalir;
    private Button btnCalibrar;
    private Button btnAsignar;
    private Button btnDispositivo;
    private Button btnAlarma;
    private Button btnActa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnSalir = (Button) findViewById(R.id.btnSalir);
        btnCalibrar = (Button) findViewById(R.id.btnCalibrar);
        //btnAsignar = (Button) findViewById(R.id.btnAsignar);
        btnDispositivo = (Button) findViewById(R.id.btnDispositivo);
        btnAlarma = (Button) findViewById(R.id.btnAlarma);
        btnActa = (Button)findViewById(R.id.btnActas);

       /* btnAsignar.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(MenuActivity.this, AsignarActivity.class);
                startActivity(i);
            }
        });*/
        btnAlarma.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(MenuActivity.this, AlarmaActivity.class);
                startActivity(i);
            }
        });
        btnActa.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                Intent i = new Intent(MenuActivity.this, pickActaActivity.class);
                startActivity(i);
            }
        });
    }


    public void salir(View view) {
        System.runFinalization();
        System.exit(0);
    }
    public void irA_Calibracion(View view) {
        Intent i = new Intent(this, AsignarActivity.class);
        startActivity(i);
    }
    public void irA_Dispositivo(View view) {
        Intent i = new Intent(this, DetalleDispoActivity.class);
        startActivity(i);
    }
}
