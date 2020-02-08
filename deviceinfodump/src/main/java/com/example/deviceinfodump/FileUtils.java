package com.example.deviceinfodump;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FileUtils {

    private FileUtils(){}

    public static String writeToDownloads(Context context, String fileContents, String fileName, String mimeType) {
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outFile = new File(downloadsDirectory, fileName);

        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = context.getContentResolver();

                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (uri == null)
                    throw new RuntimeException("Failed to insert file record");

                try {
                    OutputStream outputStream = resolver.openOutputStream(uri);
                    if (outputStream == null)
                        throw new RuntimeException("Failed to open output stream for " + uri);

                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                        outputStreamWriter.write(fileContents);
                        outputStreamWriter.close();
                    } finally {
                        outputStream.close();
                    }
                } catch (Exception ex) {
                    // Don't leave an orphan entry in the MediaStore
                    resolver.delete(uri, null, null);
                    throw ex;
                }
            } else {
                FileWriter fileWriter = new FileWriter(outFile);
                try {
                    fileWriter.write(fileContents);
                    fileWriter.close();
                } catch (Exception ex) {
                    //noinspection ResultOfMethodCallIgnored
                    outFile.delete();
                    throw ex;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return outFile.getAbsolutePath();
    }
}
