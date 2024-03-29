package com.example.saedolistocks5.popup;

import static com.example.saedolistocks5.pageliste.ListeActivity.URLApi;
import static com.example.saedolistocks5.pageliste.ListeActivity.idArticleVerif;
import static com.example.saedolistocks5.pageliste.ListeActivity.idEntrepotVerif;
import static com.example.saedolistocks5.pageliste.ListeActivity.libelleArticleVerif;
import static com.example.saedolistocks5.pageliste.ListeActivity.listeCodeArticleVerif;
import static com.example.saedolistocks5.pageliste.ListeActivity.listeCodeBarreVerif;
import static com.example.saedolistocks5.pageliste.ListeActivity.listeEntrepotsVerif;
import static com.example.saedolistocks5.pageliste.ListeActivity.listeFichierUser;
import static com.example.saedolistocks5.pageliste.ListeActivity.mode;
import static com.example.saedolistocks5.pageliste.ListeActivity.positionItemListe;
import static com.example.saedolistocks5.pageliste.ListeActivity.nombreLignes;
import static com.example.saedolistocks5.pageliste.ListeActivity.idEntrepot;
import static com.example.saedolistocks5.pageliste.ListeActivity.token;
import static com.example.saedolistocks5.pageliste.ListeActivity.utilisateurCourant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.saedolistocks5.R;
import com.example.saedolistocks5.outilapi.OutilAPI;
import com.example.saedolistocks5.outilapi.RequetesApi;
import com.example.saedolistocks5.pageliste.ListeActivity;
import com.example.saedolistocks5.pagemodifliste.ModifListeActivity;
import com.example.saedolistocks5.pagevisualisation.Visualisation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class CustomPopupAfficherMenu extends AlertDialog {

    private final ListeActivity listeActivity;

    public static ArrayList<String> listeLibelleArticles;

    public static ArrayList<String> listeIdsArticles;

    /**
     * Index de parcours de ligne (cas mode déconnecté)
     */
    public static int indexParcoursListe;

    /**
     * Index de parcours de ligne (cas mode connecté)
     */
    public static int indexParcoursListeConnecte;

    /**
     * Nom du fichier
     */
    public static String nomFichier;

    protected CustomPopupAfficherMenu(Context context, ListeActivity listeActivity) {
        super(context);
        this.listeActivity = listeActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the dialog content view
        setContentView(R.layout.popup_afficher_menu);

        listeIdsArticles = new ArrayList<>();
        listeLibelleArticles = new ArrayList<>();

        Button visualiser = findViewById(R.id.btnVisualiser);
        Button modifier = findViewById(R.id.btnModifier);
        Button envoyer = findViewById(R.id.btnEnvoyer);
        Button supprimer = findViewById(R.id.btnSupprimer);

        if(mode.equals("deconnecte")) {
            envoyer.setEnabled(false);
            envoyer.setBackground(listeActivity.getDrawable(R.drawable.base_bouton_disable));
        }

        visualiser.setOnClickListener(this::onClickVisualiser);
        modifier.setOnClickListener(this::onClickModifier);
        envoyer.setOnClickListener(this::onClickEnvoyer);
        supprimer.setOnClickListener(this::onClickSupprimer);
    }

    public static CustomPopupAfficherMenu createDialog(Context context, ListeActivity listeActivity) {
        CustomPopupAfficherMenu dialog = new CustomPopupAfficherMenu(context, listeActivity);
        return dialog;
    }

    public void onClickVisualiser(View view) {
        Intent intention = new Intent(listeActivity, Visualisation.class);
        intention.putExtra("positionItem", positionItemListe);
        listeActivity.startActivity(intention);
        dismiss(); // Fermer le dialogue après le lancement de l'activité
    }

    public void onClickModifier(View view) {
        Intent intention = new Intent(listeActivity, ModifListeActivity.class);
        intention.putExtra("positionItem", positionItemListe);
        listeActivity.startActivityForResult(intention, 0);
        dismiss(); // Fermer le dialogue après le lancement de l'activité
    }

    public void onClickEnvoyer(View view) {
        EnvoyerListe();
    }

    public void onClickSupprimer(View view) {
        CustomPopupConfirmationSupp dialog = CustomPopupConfirmationSupp.createDialog(listeActivity,
                listeActivity);
        dismiss();
        dialog.show();
    }

    /**
     * Envoyer liste.
     */
    public void EnvoyerListe() {

        nomFichier = listeFichierUser.get(positionItemListe);
        try {
            InputStreamReader liste = new InputStreamReader(listeActivity.openFileInput(nomFichier));
            BufferedReader listeText = new BufferedReader(liste);

            indexParcoursListe = 0;
            indexParcoursListeConnecte = 1;
            nombreLignes = 0;

            // Compter le nombre de lignes
            while (listeText.readLine() != null) {
                nombreLignes++;
            }

            // Réinitialiser la position de lecture du flux
            listeText.close();
            liste = new InputStreamReader(listeActivity.openFileInput(nomFichier));
            listeText = new BufferedReader(liste);

            String[] elementListe;
            String ligne;


            // Index pour savoir si on est sur le dernier traitement du fichier ou nom

            while ((ligne = listeText.readLine()) != null) {
                elementListe = ligne.split(";");
                String[] finalElementListe = elementListe;
                int maj;

                String codeArticle = "";
                String codeBarre = "";

                if (listeCodeArticleVerif.contains(finalElementListe[2])) {
                    codeArticle = finalElementListe[2];
                } else if (listeCodeBarreVerif.contains(finalElementListe[2])) {
                    codeBarre = finalElementListe[2];
                } else {
                    codeArticle = finalElementListe[2];
                }

                maj = VerifArticlesEtEntrepots(codeArticle, codeBarre, finalElementListe[4]);
                int finalMaj = maj;
                // Si la mise à jour est "Oui", donc que tout s'est bien passé, on modifie le stock
                if (maj == 0) {
                    String finalCodeArticle = codeArticle;
                    String finalCodeBarre = codeBarre;
                    String idArticle = listeIdsArticles.get(indexParcoursListe);
                    String libelleArticle = listeLibelleArticles.get(indexParcoursListe);
                    RequetesApi.GetArticlesByEntrepot(URLApi, token, listeActivity.getApplicationContext(),
                            "Liste", idArticle,
                            idEntrepot, null, new RequetesApi.QuantiteCallback() {
                                @Override
                                public void onQuantiteRecuperee(int quantiteAvantEnvoieListe)
                                        throws FileNotFoundException, JSONException {
                                    JSONObject bodyJSON = new JSONObject();

                                    int quantiteApres;
                                    int quantiteSaisie;

                                    quantiteSaisie = Integer.parseInt(finalElementListe[5]);

                                    if (finalElementListe[6].equals(listeActivity.getString(R.string.Ajout))) {
                                        finalElementListe[6] = "0";
                                        quantiteApres = quantiteAvantEnvoieListe + quantiteSaisie;
                                    } else {
                                        finalElementListe[6] = "1";
                                        quantiteApres = Integer.parseInt(finalElementListe[5]);
                                        quantiteSaisie = quantiteApres - quantiteAvantEnvoieListe;
                                    }


                                    bodyJSON.put("ref", "");
                                    bodyJSON.put("status", 1);
                                    bodyJSON.put("Libelle", finalElementListe[1]);
                                    bodyJSON.put("CodeArticle", finalCodeArticle);
                                    bodyJSON.put("CodeBarre", finalCodeBarre);
                                    bodyJSON.put("Designation", libelleArticle);
                                    bodyJSON.put("Entrepot", finalElementListe[4]);
                                    bodyJSON.put("Quantite", quantiteSaisie);
                                    bodyJSON.put("Mode", Integer.parseInt(finalElementListe[6]));
                                    bodyJSON.put("Maj", finalMaj);
                                    bodyJSON.put("StockAvant", quantiteAvantEnvoieListe);
                                    bodyJSON.put("StockApres", quantiteApres);
                                    bodyJSON.put("date", finalElementListe[7]);
                                    bodyJSON.put("Utilisateur", utilisateurCourant);

                                    EnvoyerListeSurDolibarr(nomFichier, bodyJSON,
                                            indexParcoursListeConnecte, true);

                                    JSONObject bodyJSONMvtStock = new JSONObject();
                                    bodyJSONMvtStock.put("product_id", idArticle);
                                    bodyJSONMvtStock.put("warehouse_id", idEntrepot);
                                    bodyJSONMvtStock.put("qty", quantiteSaisie);
                                    bodyJSONMvtStock.put("datem", finalElementListe[7]);
                                    bodyJSONMvtStock.put("movementlabel",
                                            String.format("%s : %s", utilisateurCourant, finalElementListe[1]));
                                    ModifierMouvementStock(bodyJSONMvtStock);
                                    indexParcoursListeConnecte++;
                                }
                            });
                } else {
                    JSONObject bodyJSON = new JSONObject();
                    if (finalElementListe[6].equals("Ajout")) {
                        finalElementListe[6] = "0";
                    } else {
                        finalElementListe[6] = "1";
                    }

                    bodyJSON.put("ref", "");
                    bodyJSON.put("status", 1);
                    bodyJSON.put("Libelle", finalElementListe[1]);
                    bodyJSON.put("CodeArticle", codeArticle);
                    bodyJSON.put("CodeBarre", codeBarre);
                    bodyJSON.put("Designation", finalElementListe[3]);
                    bodyJSON.put("Entrepot", finalElementListe[4]);
                    bodyJSON.put("Quantite", finalElementListe[5]);
                    bodyJSON.put("Mode", Integer.parseInt(finalElementListe[6]));
                    bodyJSON.put("Maj", finalMaj);
                    bodyJSON.put("StockAvant", "");
                    bodyJSON.put("StockApres", "");
                    bodyJSON.put("date", finalElementListe[7]);
                    bodyJSON.put("Utilisateur", utilisateurCourant);

                    EnvoyerListeSurDolibarr(nomFichier, bodyJSON, indexParcoursListe+1,
                            false);
                }
                indexParcoursListe++;
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verif articles et entrepots int.
     *
     * @param codeArticle the code article
     * @param nomEntrepot the nom entrepot
     * @return the int
     */
    public static int VerifArticlesEtEntrepots(String codeArticle, String codeBarre, String nomEntrepot) {
        boolean verifArticle = false;
        boolean verifEntrepot = false;


        int index = 0;
        // Si le code article n'est pas vide,
        // cela veut dire qu'on peut faire la vérification dessus
        if(!codeArticle.equals("")) {
            for(String codeArticleVerif : listeCodeArticleVerif) {
                if(codeArticle.equals(codeArticleVerif)) {
                    verifArticle = true;
                    listeIdsArticles.add(idArticleVerif.get(index));
                    listeLibelleArticles.add(libelleArticleVerif.get(index));
                    break;
                }
                index++;
            }
        } else {
            for(String codeBarreVerif : listeCodeBarreVerif) {
                if(codeBarre.equals(codeBarreVerif)) {
                    verifArticle = true;
                    listeIdsArticles.add(idArticleVerif.get(index));
                    listeLibelleArticles.add(libelleArticleVerif.get(index));
                    break;
                }
                index++;
            }
        }


        index = 0;
        for(String nomEntrepotVerif : listeEntrepotsVerif) {
            if(nomEntrepot.equals(nomEntrepotVerif)) {
                verifEntrepot = true;
                idEntrepot = idEntrepotVerif.get(index);
            }
            index++;
        }

        if(verifArticle && verifEntrepot) {
            return 0;
        }

        return 1;
    }

    /**
     * Envoyer liste sur dolibarr.
     *
     * @param nomFichier the nom fichier
     * @param bodyJson   the body json
     * @throws JSONException         the json exception
     * @throws FileNotFoundException the file not found exception
     */
    public void EnvoyerListeSurDolibarr(String nomFichier, JSONObject bodyJson,
                                        int index, boolean validation)
            throws JSONException, FileNotFoundException {
        String urlAPI = String.format("http://%s/htdocs/api/index.php/dolistockapi/listess?api_key=%s",
                URLApi, token);

        OutilAPI.PostApiJson(listeActivity.getApplicationContext(), urlAPI, bodyJson, new OutilAPI.ApiCallback() {

            @Override
            public void onSuccess(JSONObject result) {
                // BLC
            }

            @Override
            public void onSuccess(JSONArray result) {
                if(nombreLignes == index) {
                    traiterResultatOK(nomFichier);
                }
            }

            @Override
            public void onError(VolleyError error) {
                if(error.networkResponse != null ) {
                    if(error.networkResponse.statusCode != 503) {
                        if(Objects.requireNonNull(error.getMessage()).contains("JSONException")) {
                            // Si on traite la dernière ligne du fichier
                            if(nombreLignes == index) {
                                if(!validation) {
                                    Toast.makeText(listeActivity.getApplicationContext(),
                                                    listeActivity.getString(R.string.liste_invalide),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                }
                                traiterResultatOK(nomFichier);
                            }
                        } else {
                            Toast.makeText(listeActivity, "Problème d'insertion",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    // Si on traite la dernière ligne du fichier
                    if(nombreLignes == index) {
                        if(!validation) {
                            Toast.makeText(listeActivity.getApplicationContext(),
                                            listeActivity.getString(R.string.liste_invalide),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                        traiterResultatOK(nomFichier);
                    }
                }
            }
        });

    }

    /**
     * Modifier mouvement stock.
     *
     * @param bodyJson the body json
     */
    public void ModifierMouvementStock(JSONObject bodyJson) {
        String urlAPI = String.format("http://%s/htdocs/api/index.php/stockmovements?api_key=%s",
                URLApi, token);

        OutilAPI.PostApiJson(listeActivity.getApplicationContext(), urlAPI, bodyJson, new OutilAPI.ApiCallback() {

            @Override
            public void onSuccess(JSONObject result) {
                Toast.makeText(listeActivity.getApplicationContext(), "Insertion OK",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(JSONArray result) {
                Toast.makeText(listeActivity.getApplicationContext(), "Insertion OK",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(VolleyError error) {
                if(error.getMessage() != null) {
                    if(!error.getMessage().contains("JSONException")) {
                        Toast.makeText(listeActivity.getApplicationContext(), "Erreur : " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    /**
     * Traiter resultat ok.
     *
     * @param nomFichier the nom fichier
     */
    public void traiterResultatOK(String nomFichier) {
        CustomPopupEnvoyer dialog = CustomPopupEnvoyer.createDialog(listeActivity, listeActivity);
        dismiss();
        dialog.show();
    }


}