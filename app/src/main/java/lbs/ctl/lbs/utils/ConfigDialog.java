package lbs.ctl.lbs.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import lbs.ctl.lbs.R;


/**
 * Created by admin on 2016/8/12.
 */
public class ConfigDialog {

    EditText ipEdit;
    RelativeLayout layout;

    public  void showConfig(final Context context)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        layout = (RelativeLayout) inflater.inflate(R.layout.config_dialog, null);
        final Dialog dialog = new Dialog(new ContextThemeWrapper(context, R.style.customDialog));
        dialog.setCancelable(true);
        dialog.setTitle("配置");
        dialog.show();
        dialog.getWindow().setContentView(layout);

        ipEdit = (EditText)layout.findViewById(R.id.ip);


        Button setIpBtn = (Button) layout.findViewById(R.id.seting);
        setIpBtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                String ipstr=ipEdit.getText().toString();
                SharedPreferences.Editor editor= context.getSharedPreferences("AIRINTERFACE", Activity.MODE_PRIVATE).edit();
                editor.putString("IP",ipstr);
                editor.commit();
                dialog.dismiss();
            }
        });
    }

}
