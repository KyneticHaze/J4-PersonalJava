package com.furkanharmanci.personaljava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.furkanharmanci.personaljava.databinding.ActivityPersonalRegisterBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class PersonalRegister extends AppCompatActivity {

    private ActivityPersonalRegisterBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    //İzinlerde String ile uğraşacağız
    ActivityResultLauncher<String> permissionResultLauncher;
    Bitmap uriImageToBitmap;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPersonalRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // program başlamadan önce bu aktivite sonucu başlatıcılarını kayıt etmeliyiz.
        registerLauncher();
        // veri görüntülemek için database oluşturmayı onCreate içinde tanımlamak daha iyi
        database = this.openOrCreateDatabase("personal", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        // bilgi new ise menüden gelmiştir
        if (info.equals("new")) {
            // yeni item
            binding.nameText.setText("");
            binding.jobText.setText("");
            binding.lastNameText.setText("");
            binding.save.setVisibility(View.VISIBLE);
            binding.personImage.setImageResource(R.drawable.person);
        } else {
            // değil ise recyclerView'den gelmiştir.
            //id yolladı.
            int personId = intent.getIntExtra("personId",1);
            // defaultValue 1 => Çünkü veri çekmede her bir veri 1'den başlayan id'lere kaydedildiğinden, veri gelmezse varsayılan olarak ilk id yani 1 id'li veri gelsin diyoruz. Adapter'den id gönderdik çünkü obje halinde kaydedilen veriler id ile farklılıkları tanımlanıyor.
            binding.save.setVisibility(View.INVISIBLE);

            Cursor cursor = database.rawQuery("SELECT * FROM personal WHERE id = ?", new String[] {String.valueOf(personId)});
            // Soru işaretli kısma gelecek değeri selection args belirliyor.
            // selection args bizden String listesi istiyor o sebeple yeni bir String listesi tanımladık tek elemanımız olmasına rağmen ve int tipindeki id elemanımızı String sınıfndaki valueOf methodumuz ile String'e dönüştürdük.
            int personNameIx = cursor.getColumnIndex("personName");
            int personLastNameIx = cursor.getColumnIndex("personLastName");
            int personJobNameIx = cursor.getColumnIndex("personJobName");
            int personImageIx = cursor.getColumnIndex("image");

            while(cursor.moveToNext()) {
                // View'daki yerlere veritabanında kaydedilen ilgili verileri iliştirmek(bind'lamak)
                binding.nameText.setText(cursor.getString(personNameIx));
                binding.lastNameText.setText(cursor.getString(personLastNameIx));
                binding.jobText.setText(cursor.getString(personJobNameIx));

                // image, byte dizisi halinde tutuluyor veritabanında
                byte[] bytes = cursor.getBlob(personImageIx);
                // byte dizisini image'a koyamıyoruz o sebeple bitmap'e dönüştürücez
                Bitmap byteArrayToBitmapImage = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                // sonra bağlıyoruz
                binding.personImage.setImageBitmap(byteArrayToBitmapImage);
            }
            cursor.close();
        }
    }


    public void save(View view) {
        String name = binding.nameText.getText().toString();
        String lastName = binding.lastNameText.getText().toString();
        String jobName = binding.jobText.getText().toString();

        // bitmap şeklinde olan veriyi boyutlandırdık
        Bitmap smallImage = makeSmallerImage(uriImageToBitmap, 100);

        // Bitmap tipiindeki görseli veriye çevirmemiz lazım. Byte dizisine çevirelim
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Verileri veritabanına kaydetmek için bir method
        sqlDataRegister(name,lastName, jobName, byteArray);

        // Diğer aktiviteye geçmek için
        Intent intent = new Intent(PersonalRegister.this, MainActivity.class);
        // Tüm aktiviteleri kapat sadece şu an gideceğimi aç
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    public void sqlDataRegister(String name, String lastName, String jobName, byte[] byteArray) {
        database.execSQL("CREATE TABLE IF NOT EXISTS personal (id INTEGER PRIMARY KEY, personName VARCHAR, personLastName VARCHAR, personJobName VARCHAR, image BLOB)");
        String sql = "INSERT INTO personal (personName, personLastName, personjobName, image) VALUES (?, ?, ?, ?)";
        SQLiteStatement sqLiteStatement = database.compileStatement(sql);

        sqLiteStatement.bindString(1,name);
        sqLiteStatement.bindString(2,lastName);
        sqLiteStatement.bindString(3, jobName);
        sqLiteStatement.bindBlob(4, byteArray);

        sqLiteStatement.execute();
    }


    // image'ı boyutlandırdık
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            //landscape image
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            // portrait image
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return image.createScaledBitmap(image,width, height, true);
    }


    public void changeImage(View view) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // İzin verilmemişse
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // request permission
                        permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                // request permission
                permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            // İzin verilmişse
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // MediaStore.Images.Media.EXTERNAL_CONTENT_URI => Galeriye gidicem
            // Intent.ACTION_PICK => seçip alıcam
            activityResultLauncher.launch(intentToGallery);
        }
    }


    public void registerLauncher() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = result.getData();

                    if(intentFromResult != null) {
                        // URI kullanıcının seçtiği görselin nerde olduğunu gösterir.
                       Uri uriImageData = intentFromResult.getData();

                       try {
                           ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uriImageData);
                           uriImageToBitmap = ImageDecoder.decodeBitmap(source);
                           binding.personImage.setImageBitmap(uriImageToBitmap);
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                    }
                }
            }
        });

        permissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            // ActivityResultContracts.RequestPermission() => Activitenin tipi

            @Override
            public void onActivityResult(Boolean result) {
                // Boolean result => doğru ise izin verildi yanlış ise verilmedi
                if(result) {
                    // permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    activityResultLauncher.launch(intentToGallery);
                } else {
                    // permission denied
                    Toast.makeText(PersonalRegister.this, "Permission Needed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}