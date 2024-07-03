package com.gece.scanapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private static final String BARCODE_KEY = "BARCODE";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;

    private String scanResult;
    private EditText productNameEditText;
    private EditText productPriceEditText;
    private DBHelper dbHelper;

    private TextView resultTextView;
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    scanResult = result.getContents();
                    resultTextView.setText(scanResult);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        Button scanButton = findViewById(R.id.scan_button);
        resultTextView = findViewById(R.id.result_text_view);
        productNameEditText = findViewById(R.id.product_name);
        productPriceEditText = findViewById(R.id.product_price);
        Button saveButton = findViewById(R.id.save_button);
        Button searchButton = findViewById(R.id.search_button);

        scanButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                startScanning();
            }
        });

        saveButton.setOnClickListener(v -> saveProductToDatabase());

        searchButton.setOnClickListener(v -> searchProductOnline());
    }



    private void startScanning() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Escaneando...");
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        barcodeLauncher.launch(options);
    }

    private void saveProductToDatabase() {
        String name = productNameEditText.getText().toString().trim();
        String priceText = productPriceEditText.getText().toString().trim();

        if (scanResult == null) {
            Toast.makeText(this, "Primero escanea un código de barras", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || priceText.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingrese un precio válido", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("barcode", scanResult);
        values.put("name", name);
        values.put("price", price);

        long newRowId = db.insert("products", null, values);
        if (newRowId == -1) {
            Toast.makeText(this, "Error al guardar el producto", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Producto guardado: " + name + " - " + price, Toast.LENGTH_SHORT).show();
            resultTextView.setText("Producto guardado: " + name + " - " + price);
        }
    }

    private void searchProductOnline() {
        if (scanResult != null) {
            Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
            intent.putExtra(BARCODE_KEY, scanResult);
            startActivity(intent);
        } else {
            resultTextView.setText("Primero escanea un código de barras");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
