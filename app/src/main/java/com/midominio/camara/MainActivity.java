package com.midominio.camara;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    Button btnCamara;
    ImageView imgView;
    String rutaImagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCamara = findViewById(R.id.btnCamara);
        imgView = findViewById(R.id.imageView);

        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamara();
            }
        });

    }
    private void abrirCamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //if(intent.resolveActivity(getPackageManager())!=null){
            File imagenArchivo = null;
            try {
                imagenArchivo = crearImagen();
            }catch (IOException ex){
                Log.e("Error",ex.toString());
            }
            if(imagenArchivo!=null){
                Uri fotoUri = FileProvider.getUriForFile(this,"com.midominio.camara.fileprovider",imagenArchivo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,fotoUri);
                startActivityForResult(intent,1);
            }

        //}

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1 && resultCode == RESULT_OK){
            Bitmap imgBitmap = BitmapFactory.decodeFile(rutaImagen);
            // Rotar la imagen si es necesario
            imgBitmap = rotarImagenSiEsNecesario(imgBitmap);
            imgView.setImageBitmap(imgBitmap);
            // Guardar la imagen en la galería
            guardarEnGaleria(imgBitmap);
        }
    }
    private File crearImagen() throws IOException {
        String nombreImgen ="foto_";
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreImgen,".jpg",directorio);

        rutaImagen = imagen.getAbsolutePath();
        return  imagen;
    }

    private void guardarEnGaleria(Bitmap bitmap) {
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "foto.jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.WIDTH, bitmap.getWidth());
        contentValues.put(MediaStore.Images.Media.HEIGHT, bitmap.getHeight());

        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            try {
                OutputStream outputStream = contentResolver.openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Bitmap rotarImagenSiEsNecesario(Bitmap bitmap) {
        try {
            ExifInterface exifInterface = new ExifInterface(rutaImagen);
            int orientacion = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            int rotacion = 0;
            switch (orientacion) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotacion = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotacion = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotacion = 270;
                    break;
            }
            if (rotacion != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotacion);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}