package com.example.saedolistocks5.pagemain;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.saedolistocks5.pageliste.ListeActivity;
import com.example.saedolistocks5.pageconnexion.LoginActivity;
import com.example.saedolistocks5.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logo = findViewById(R.id.logo);

        // Masquer l'image au début
        logo.setAlpha(0f);

        // Créer une animation d'opacité pour faire apparaître l'image progressivement
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        fadeIn.setDuration(2000); // 2 secondes

        // Créer une animation de rotation
        ObjectAnimator rotate = ObjectAnimator.ofFloat(logo, "rotation", 0f, 360f);
        rotate.setDuration(2000); // 2 secondes

        // Créer un ensemble d'animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeIn, rotate);

        animatorSet.start();

    }
    public boolean ReadFichier() {
        boolean ok;
        int compteur = 0;
        try {
            InputStreamReader fichier = new InputStreamReader(openFileInput("infouser.txt"));
            BufferedReader fichiertexte = new BufferedReader(fichier);
            while(fichiertexte.readLine() != null) {
                compteur += 1;
            }
            if (compteur == 3) {
                ok = true;
            } else {
                ok = false;
            }
            fichier.close();
        } catch (FileNotFoundException e) {
            ok = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ok;
    }
    public void onClickModeCo(View Button){

        ConnectivityManager connManager =
                (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(connManager.getActiveNetwork());

        if(mWifi != null && ReadFichier() && mWifi.isConnectedOrConnecting()) {
            Intent intention = new Intent(MainActivity.this, ListeActivity.class);
            startActivity(intention);
            return;
        } else if (mWifi != null && !mWifi.isConnectedOrConnecting() && ReadFichier()) {
            Toast.makeText(this,R.string.messageModeConnecte,Toast.LENGTH_LONG).show();
            // Lorsqu'on est en mode avion
        } else if(mWifi == null && ReadFichier()) {
            Toast.makeText(this,R.string.messageModeConnecte,Toast.LENGTH_LONG).show();
        }
        if(!ReadFichier()) {
            Intent intention = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intention);
        }
    }

    public void onClickModeDeco(View Button){
        if (ReadFichier()) {
            Intent intention = new Intent(MainActivity.this, ListeActivity.class);
            startActivity(intention);
        } else {
            Intent intention = new Intent(MainActivity.this, LoginActivity.class);
            // Toat d'information, pas de mode déconnecté si ReadFichier() renvoie faux
            Toast.makeText(this,R.string.messageRedirection,Toast.LENGTH_LONG).show();
            startActivity(intention);
        }

    }
}