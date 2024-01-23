package com.example.saedolistocks5;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class LoginActivity extends AppCompatActivity {

    private String token;

    private TextView userView;
    private TextView passwordView;
    private TextView urlApiView;
    private TextView texteErreurView;

    private String user;
    private String password;
    private String urlApi;


    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        userView = findViewById(R.id.champsLogin);
        passwordView = findViewById(R.id.champsMdp);
        urlApiView = findViewById(R.id.champsURL);
        texteErreurView = findViewById(R.id.texteErreur);

        context = this;
    }

    public void seConnecter(View view) throws JSONException {
        user = userView.getText().toString();
        password = passwordView.getText().toString();
        urlApi = urlApiView.getText().toString();

        if(user.equals("") || password.equals("") || urlApi.equals("")) {
            texteErreurView.setText("Erreur : tous les champs de sont pas renseignés");
        } else {
            String urlApiEntiere = String.format("http://%s/htdocs/api/index.php/login?login=%s&password=%s", urlApi, user, password);
            OutilAPI.getApiRetour(getApplicationContext(),urlApiEntiere, new OutilAPI.ApiCallback() {
                //@RequiresApi(api = Build.VERSION_CODES.O)
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onSuccess(JSONObject result)  {
                    JSONObject success = null;
                    try {
                        success = result.getJSONObject("success");
                        String token = success.getString("token");

                        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
                        keyGen.init(56); // 56 bits pour DES
                        Key key = keyGen.generateKey();

                        // Clé secrète permettant d'encrypter et de décrypter un message
                        byte[] tokenEncrypt = EncryptAndDecrypteToken.encrypt(token, key);
                        String tokenDecrypt = EncryptAndDecrypteToken.decrypt(tokenEncrypt, key);
                        texteErreurView.setText(tokenDecrypt);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    texteErreurView.setText(error.getMessage());
                }
            });
        }
    }
}

