package fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.tessoft.mykaraoke.KaraokeApplication;
import com.tessoft.mykaraoke.TransactionDelegate;

/**
 * Created by Daeyong on 2017-09-01.
 */
public class BaseFragment extends Fragment implements TransactionDelegate {

    KaraokeApplication application = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (KaraokeApplication) getActivity().getApplication();
    }

    @Override
    public void doPostTransaction(int requestCode, Object result) {

    }

    public void showOKDialog(String message, final Object param) {
        showOKDialog("확인", message, param);
    }

    public void showOKDialog(String title, String message, final Object param) {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

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

    public void showYesNoDialog( String title, String message, final int requestCode, final Object param )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        yesClicked(requestCode, param);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        noClicked(requestCode, param);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void yesClicked( int requestCode, Object param )
    {

    }

    public void noClicked( int requestCode, Object param )
    {

    }
}
