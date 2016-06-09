package util;

import android.os.Environment;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by marco_000 on 10/05/2016.
 */
public class General {
    private String id_ins;
    public General() {};

    public void grabar(View v, String nameFile, String txt) {
        String nomarchivo = nameFile;
        String contenido = txt;
        try {
            File tarjeta = Environment.getExternalStorageDirectory();
            File file = new File(tarjeta.getAbsolutePath(), nomarchivo);
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(file));
            osw.write(contenido);
            osw.flush();
            osw.close();
        } catch (IOException ioe) {

        }
    }

    public void recuperar_id_ins(String filename) {
        //PRIMER LINEA: id_ins
        String nomarchivo = filename;
        File tarjeta = Environment.getExternalStorageDirectory();
        File file = new File(tarjeta.getAbsolutePath(), nomarchivo);
        try {
            FileInputStream fIn = new FileInputStream(file);
            InputStreamReader archivo = new InputStreamReader(fIn);
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
           /* String todo = "";
           while (linea != null) {
                todo = todo + linea + " ";
                linea = br.readLine();
            }*/
            String[] split = linea.split("=");

            br.close();
            archivo.close();
            this.id_ins = split[1];
            getId_ins();

        } catch (IOException e) {

        }
    }


    public String getId_ins() {
        return id_ins;
    }
}
