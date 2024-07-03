package com.gece.scanapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ProductListActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView productListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        dbHelper = new DBHelper(this);
        productListView = findViewById(R.id.product_list_view);

        loadProducts();
    }

    private void loadProducts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("Products", null, null, null, null, null, null);

        ArrayList<String> products = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
            String barcode = cursor.getString(cursor.getColumnIndexOrThrow("barcode"));
            products.add("Name: " + name + ", Price: " + price + ", Barcode: " + barcode);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, products);
        productListView.setAdapter(adapter);
    }
}
