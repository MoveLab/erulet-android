package net.movelab.sudeau;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends FragmentActivity {

    private LinearLayout username_area;
    private LinearLayout login_or_register_area;
    private TextView username;
    private Button login_button;
    private Button logout_button;
    private Button register_button;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);

        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);

        username_area = (LinearLayout) findViewById(R.id.username_area);
        username = (TextView) findViewById(R.id.tv_username);
        login_or_register_area = (LinearLayout) findViewById(R.id.login_or_register_area);
        login_button = (Button) findViewById(R.id.login_button);
        logout_button = (Button) findViewById(R.id.logout_button);
        register_button = (Button) findViewById(R.id.register_button);


        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToLogin();
            }
        });
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToRegister();
            }
        });
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PropertyHolder.setUserName(UtilLocal.SERVULET_ANONYMOUS_USERNAME);
                PropertyHolder.setUserKey(UtilLocal.SERVULET_ANONYMOUS_API_KEY);
                username_area.setVisibility(View.GONE);
                login_or_register_area.setVisibility(View.VISIBLE);
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(PropertyHolder.getUserName().equals(UtilLocal.SERVULET_ANONYMOUS_USERNAME)){
            username_area.setVisibility(View.GONE);
            login_or_register_area.setVisibility(View.VISIBLE);
        } else{
            username_area.setVisibility(View.VISIBLE);
            username.setText(PropertyHolder.getUserName());
            login_or_register_area.setVisibility(View.GONE);
        }
        super.onResume();

    }



    private void tryToLogin() {
        if (Util.isOnline(getBaseContext())) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.no_data_access), Toast.LENGTH_SHORT).show();
        }
    }


    private void tryToRegister() {

            if (Util.isOnline(getBaseContext())) {
                Intent intent = new Intent(this, RegistrationActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.no_data_access), Toast.LENGTH_SHORT).show();
            }

    }


}
