package com.example.TeamExpense;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by dinesh on 15/2/16.
 */
public class Util {

    public static Uri takeScreenshot(View v, String file_name, Activity t) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            File sdCardDirectory = Environment.getExternalStorageDirectory();

            // create bitmap screen capture
            v.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);

            File imageFile = new File(sdCardDirectory, file_name);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            Uri uri = Uri.fromFile(imageFile);

            // send broadcast that an image is added to sd card
            // so that apps like gallery will list this new image
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            t.sendBroadcast(mediaScanIntent);

            return uri;

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            Toast.makeText(t, "Error capturing image", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return null;
    }
}
