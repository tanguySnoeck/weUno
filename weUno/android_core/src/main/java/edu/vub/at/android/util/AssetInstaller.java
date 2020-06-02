/**
 * AmbientTalk/2 Project
 * (c) Software Languages Lab, 2006 - 2011
 * Authors: Software Languages Lab - Ambient Group
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package edu.vub.at.android.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/* This class copies everything under 'assets/_ENV_AT_ASSETS_BASE_' in a package to the SD card.
 * The development flag indicates that the assets should be copied every time. Turn this off for release!
 */
public class AssetInstaller extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final class CopyAsyncTask extends AsyncTask<Void, String, Void> {
        final ProgressDialog pd = ProgressDialog.show(AssetInstaller.this, "Copying assets", "Please wait");

        @Override
        protected Void doInBackground(Void... params) {

            long then = System.currentTimeMillis();
            if (copyDefaultAssets && needToCopyDefaultAssets(getResources())) {
                Log.i("AssetInstaller", "Copying AmbientTalk assets ");
                publishProgress("Copying AmbientTalk assets");
                copyDefaultAssets(AssetInstaller.this, false);
            }

            Log.i("AssetInstaller", "Copying project assets ");
            publishProgress("Copying project assets");
            copyAssets(AssetInstaller.this);

            long now = System.currentTimeMillis();
            Log.i("AssetInstaller", "Copying assets took " + (now - then) + "ms");

            try {
                marker_.createNewFile();
            } catch (IOException e) {
                Log.e("ATLibInstaller","Could not create marker file", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pd.cancel();
            setResult(RESULT_OK);
            finish();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pd.setMessage(values[0]);
        }
    }

    private static final File _ASSET_ROOT_ = IATSettings._ENV_AT_HOME_;
    private File marker_;
    protected boolean development;
    protected boolean copyDefaultAssets;

    public AssetInstaller(boolean defaultAssets) {
        super();
        //TODO develop
        marker_ = new File(_ASSET_ROOT_, "." + getClass().getName());
        development = false;
        copyDefaultAssets = defaultAssets;
    }

    private static boolean needToCopyDefaultAssets(Resources resources) {

        File defaultMarker = new File(_ASSET_ROOT_, "version.at");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(defaultMarker)));
            int installed_version = Integer.parseInt(br.readLine());

            br.close();

            InputStream is = resources.openRawResource(R.raw.atlib);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().equals("version.at")) {
                    br = new BufferedReader(new InputStreamReader(zis));
                    int packaged_version = Integer.parseInt(br.readLine());
                    br.close();

                    return packaged_version > installed_version;
                }
            }

            Log.e("AssetInstaller", "No version.at file in included atlib zipfile!");
            return true;
        } catch (FileNotFoundException e) {
            return true;
        } catch (IOException e) {
            return true;
        }
    }

    public AssetInstaller() {
        this(true);
    }

    private static void copyAssets(AssetManager am, String path, File destRoot) {
        try {
            //TODO develop
            //System.out.println("copyAssets was called");
            String[] contents = am.list(Constants._ENV_AT_ASSETS_BASE_ + path);
            for (String f : contents) {
                String newPath = path + "/" + f;
                Log.i("AssetInstaller", newPath);
                if (f.endsWith(".at")) {
                    try {
                        copyFile(am, newPath, destRoot);
                    } catch (IOException e) {
                        Log.e("AssetInstaller", "Could not copy file " + newPath, e);
                    }
                } else {
                    copyAssets(am, newPath, destRoot);
                }
            }
        } catch (IOException e) {
            //TODO develop
            //System.out.println("copyAssets failed");
            Log.i("AssetInstaller","Could not get path " + path, e);
        }
    }





    /* When this activity is started, copy all the assets in the users' package to the SD card.
     * If the SD card is not present or not writable, the activity exits with an _RESULT_INSTALLATION_FAILED_
     * result code. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.installer);


        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            AlertDialog alert =
                    new AlertDialog.Builder(this)
                            .setTitle("SD card required")
                            .setMessage("An SD card is required to run AmbientTalk")
                            .setCancelable(false)
                            .setIcon(R.drawable.at_icon)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    setResult(Constants._RESULT_FAIL_);
                                    finish();
                                }})
                            .create();
            alert.show();
            return;
        }


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        } else {
            startCopyTask();
        }
    }

    public void startCopyTask() {
        // creating the tmp directory for storing the files which the
        // user edit but didn't explicitly save.
        File dest = new File( Environment.getExternalStorageDirectory(), Constants._AT_TEMP_FILES_PATH);
        dest.mkdirs();

        if (!(copyDefaultAssets && needToCopyDefaultAssets(getResources())) && marker_.exists() && !development) {
            setResult(RESULT_OK);
            finish();
            return;
        }

        new CopyAsyncTask().execute((Void)null);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCopyTask();
        } else {
            Log.e("AssetInstaller", "I don't have permissions to write assets!");
            Toast.makeText(this, "Cannot launch application without permissions!", Toast.LENGTH_SHORT).show();
        }
    }

    // Allows copying default assets (atlib) synchronously.
    // Also creates the AmbientTalk.app tmp directory, since it is added to the path.
    // Returns whether they were copied.
    public static boolean copyDefaultAssets(Activity a, boolean check) {
        File tmpdir = new File(Constants._ENV_AT_BASE_ + Constants._AT_TEMP_FILES_PATH);
        if (!tmpdir.exists())
            tmpdir.mkdirs();

        Resources r = a.getResources();
        AssetManager am = a.getAssets();
        if (check && !needToCopyDefaultAssets(r))
            return false;

        copyATLibFile(r, am, _ASSET_ROOT_);
        return true;
    }

    public static boolean copyDefaultAssets(Activity a) {
        return copyDefaultAssets(a, true);
    }

    // Allows copying assets synchronously.
    public static void copyAssets(Activity a) {
        copyAssets(a.getAssets(), "", _ASSET_ROOT_);
    }

    private static void copyATLibFile(Resources r, AssetManager am, File destRoot) {
        InputStream is = r.openRawResource(R.raw.atlib);

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        try {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                String filename = ze.getName();
                byte[] bytes = baos.toByteArray();

                File dest = new File(destRoot, filename);
                // it could be that the file was there from the default atlib. Override it with the one of the project.
                if (dest.exists()){
                    dest.delete();
                }
                if (filename.endsWith(".at") && !(filename.contains("MACOSX"))) {
                    try {
                        Log.v("AssetInstaller", filename);
                        dest.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(dest);
                        fos.write(bytes);
                    } catch (IOException e) {
                        Log.e("AssetInstaller", "Error while copying " + filename + " to " + destRoot);
                    }
                }


                //  copyFile(am, filename, destRoot);
            }
        } catch (IOException e) {
            Log.e("AssetInstaller", "Error while copying atlib zip file ", e);
        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                Log.e("AssetInstaller", "Error while closing atlib zip file ", e);
            }
        }

    }

    private static void copyFile(AssetManager am, String path, File destRoot) throws IOException {
        InputStream is;
        int size;
        byte buf[] = new byte[4096];
        try {
            is = am.open(Constants._ENV_AT_ASSETS_BASE_ + path);
        } catch (IOException e) {
            throw e;
        }

        File dest = new File(destRoot, path);
        // it could be that the file was there from the default atlib. Override it with the one of the project.
        if (dest.exists()) {
            dest.delete();
        }
        try {
            dest.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(dest);

            while (-1 != (size = is.read(buf)))
                fos.write(buf, 0, size);
        } catch (IOException e) {
            throw e;
        } finally {
            is.close();
        }
    }

}