package com.tessoft.mykaraoke;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

/**
 * Created by Daeyong on 2017-08-22.
 */
public class BaseActivity extends AppCompatActivity implements TransactionDelegate {

    public void showOKDialog(String message, final Object param) {
        showOKDialog("확인", message, param);
    }

    public void showOKDialog(String title, String message, final Object param) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        okClicked(param);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void okClicked(Object param) {

    }

    @Override
    public void doPostTransaction(int requestCode, Object result) {

    }

    public HashMap getUserInfoMap() throws Exception {
        String userInfoString = getMetaInfoString("userInfo");
        if (!Util.isEmptyString(userInfoString)) {
            ObjectMapper mapper = new ObjectMapper();
            HashMap userInfo = mapper.readValue(userInfoString, new TypeReference<HashMap>() {
            });
            return userInfo;
        }

        return new HashMap();
    }

    public HashMap getDefaultHashMap() throws Exception
    {
        HashMap param = getUserInfoMap();
        if ( param == null || param.size() == 0 )
        {
            param = new HashMap();
            param.put("OSVersion", getOSVersion());
            param.put("appVersion", getPackageVersion());
        }

        return param;
    }

    public String getOSVersion()
    {
        return Build.VERSION.RELEASE;
    }

    public String getPackageVersion()
    {
        PackageInfo pInfo;
        try {

            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }

    public String getMetaInfoString( String key )
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if ( settings.contains(key) )
            return settings.getString(key, "");
        else return "";
    }

    public void setMetaInfo( String key, String value )
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString( key, value );
        editor.commit();
    }

    public void showToastMessage( String message )
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
