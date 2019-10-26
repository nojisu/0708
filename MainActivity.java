package com.example.studio1;


import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    GridView gridview;
    Button gallery_button;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gallery_button = (Button) findViewById(R.id.gallery_button);

        gallery_button.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);


        gridview=(GridView) findViewById(R.id.sbs);

        gridview.setAdapter(new MyImageAdapter(this,imgs));

       // myOriginalMemoryBundle=savedInstanceState;


        }

    // 사용자 이벤트가 발생할 때 갤러리 앱의 목록 화면을 띄우기 위한 Intent를 발생시킴.
    @Override
    public void onClick(View v) {

        if (v == gallery_button) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // 여러 파일 정보를 획득하는 ClipData가 Jelly Bean에서 제공해서
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(intent, 30);
            } else {
                // Jelly Bean 하위에서는 하나만 선택하게
                // Intent에 Action 정보와 Type, Data 정보를 주어 갤러리 앱의 목록 액티비티를 띄운다.
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // 이때 사용자가 사진 한 장을 선택하면 다시 화면으로 되돌아와야 하므로.
                startActivityForResult(intent, 20);
            }
        }
    }

    // startActivityForResult() 함수로 인하여 요청이 되돌아올 때 사후 처리를 위해 자동으로 호출.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 20 && resultCode == RESULT_OK) {
            // Jelly Bean 이전 버전.. 하나만 선택해서 들어오는 경우
            String[] projection = {MediaStore.Images.Media.DATA};

            // 외부 앱(갤러리)의 Content Provider를 이용하는 곳에서
            // 시스템에서 미리 생성해놓은 Content Provider 객체를 ContentResolver를 이용해 획득.
            // ContentResolver에는 모든 앱의 모든 Content Provider가 등록되어 있다. Uri 객체를 식별자로 객체를 식별.
            // Content Provider를 "Uri 모델로 식별되어 이용되는 컴포넌트"라고 표현.
            Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);

            cursor.moveToFirst();

            String filePath = cursor.getString(0);

            insertImageView(filePath);
        } else if (requestCode == 30 && resultCode == RESULT_OK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            ClipData clipData = data.getClipData();

            if (clipData != null || data.getData() != null) {

                if (clipData == null) {
                    Uri uri = data.getData();
                    String filePath = getFilePathFromDocumentUri(this, uri);

                    if (filePath != null) {
                        insertImageView(filePath);
                    }
                } else {
                    for (int i = 0; i < clipData.getItemCount(); i++) {

                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();

                        if ("com.android.providers.media.documents".equals(uri.getAuthority()) && Build.VERSION.SDK_INT >= 19) {

                            String filePath = getFilePathFromDocumentUri(this, uri);

                            if (filePath != null) {
                                insertImageView(filePath);
                            }
                        } else if ("external".equals(uri.getPathSegments().get(0))) {
                            String filePath = getFilePathFromUriSegment(uri);

                            if (filePath != null) {
                                insertImageView(filePath);
                            }
                        }
                    }
                }
            }
        } else {
            Uri uri = data.getData();
            String filePath = getFilePathFromDocumentUri(this, uri);

            if (filePath != null) {
                insertImageView(filePath);
            }
        }
    }

    // 갤러리 앱을 연동하여 획득한 사용자가 선택한 파일의 경로를 매개변수로 넘기면
    // 해당 파일을 ImageView로 만들어 화면에 출력.
    private void insertImageView(String filePath) {

        if (!filePath.equals("")) {
            File file = new File(filePath);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            try {
                InputStream in = new FileInputStream(filePath);
                BitmapFactory.decodeStream(in, null, options);
                in.close();
                in = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            final int width = options.outWidth;

            // reqWidth 동적으로 크기 조정하려면 얼마의 값이 필요할지 알아봐야함!!!
            int reqWidth = 500;

            int inSampleSize = 1;



            if (width > reqWidth) {
                int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = widthRatio;
            }


            BitmapFactory.Options imgOptions = new BitmapFactory.Options();
            imgOptions.inSampleSize = inSampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, imgOptions);


            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            LinearLayout mainContent = (LinearLayout) findViewById(R.id.mainContent);

            mainContent.addView(imageView);
        }
    }

    // Document식 Uri 값에서 사용자가 선택한 이미지 파일의 경로를 획득.
    private String getFilePathFromDocumentUri(Context context, Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // ex> ..../document/image:3A35260
            // 필요한 정보는 document 뒤에 단어로 이를 추출하여 ':' 앞 단어는 데이터의 타입, 뒷 단어는 데이터의 식별자로 추출 및 사용
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];
            Uri contentUri = null;

            if ("image".equals(type)) {     // Video, Audio type도 가능.
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }

            String selection = MediaStore.Images.Media._ID + "=?";
            String[] selectionArgs = new String[]{split[1]};

            String column = "_data";
            String[] projection = {column};
            Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null);

            String filePath = null;

            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                filePath = cursor.getString(column_index);
            }

            cursor.close();

            return filePath;
        } else {
            return null;
        }
    }

    // Segment식 Uri 값에서 사용자가 선택한 이미지의 파일 경로를 획득.
    private String getFilePathFromUriSegment(Uri uri) {

        String selection = MediaStore.Images.Media._ID + "=?";

        // Uri 값의 맨 마지막 세그먼트가 사용자가 선택한 이미지의 식별자. (식별자 값을 획득)
        String[] selectionArgs = new String[]{uri.getLastPathSegment()};

        String column = "_data";
        String[] projection = {column};

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

        String filePath = null;

        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(column);
            filePath = cursor.getString(column_index);
        }

        cursor.close();

        return filePath;
    }

}