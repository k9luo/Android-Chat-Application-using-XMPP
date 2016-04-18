package com.mycompany.chat.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mycompany.chat.R;
import com.mycompany.chat.xmpp.LocalBinder;
import com.mycompany.chat.xmpp.MyService;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private boolean mBounded;
    private MyService mService;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(final ComponentName name,
                                       final IBinder service) {
            mService = ((LocalBinder<MyService>) service).getService();
            mBounded = true;
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService = null;
            mBounded = false;
            Log.d(TAG, "onServiceDisconnected");
        }
    };
    private EditText editText, editText2;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        buttonLogin = (Button) findViewById(R.id.button);
        buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String user = editText.getText().toString();
                String pass = editText2.getText().toString();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("user", user);
                editor.putString("pass", pass);
                editor.commit();
                doBindService();
                    Intent intent = new Intent(LoginActivity.this, FriendsActivity.class);
                    startActivity(intent);
            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    void doBindService() {
        bindService(new Intent(this, MyService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    public MyService getmService() {
        return mService;
    }


}
