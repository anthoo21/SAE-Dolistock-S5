/**
 * Package de la SAE
 */
package com.example.saedolistocks5.pagemodifliste;

import static com.example.saedolistocks5.outilapi.RequetesApi.getArticles;
import static com.example.saedolistocks5.outilapi.RequetesApi.getListeEntrepot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saedolistocks5.R;
import com.example.saedolistocks5.outilapi.RequetesApi;
import com.example.saedolistocks5.outilsdivers.OutilDivers;
import com.example.saedolistocks5.pageliste.ListeActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import com.example.saedolistocks5.outilsdivers.Quartet;

/**
 * Classe ModifListeActivity qui permet de modifier une liste sur l'application Dolistock
 * en fonction du nom de la liste, du nom de l'entrepôt et de modifier des lignes à la liste
 * en fonction du code de l'article et de la quantité à ajouter ou à modifier
 * @author BONNET, FROMENT et ENJALBERT
 * @version 2.0
 */
public class ModifListeActivity extends AppCompatActivity {

    /**
     * Editext pour la saisie du Nom de la liste.
     */
    private EditText saisieNomListe;
    /**
     * Editext pour la saisie du nom de l'entrepot.
     */
    private EditText saisieNomEntrepot;
    /**
     * Editext pour saisir la quantite.
     */
    private EditText saisieQuantite;
    /**
     * AutoCompleteTextView pour la saisie du code article
     */
    private AutoCompleteTextView saisieCodeArticle;
    /**
     * Textview du libelle du stock.
     */
    private TextView libelleStock;
    /**
     * Textview du texte d'erreur
     */
    private TextView texteErreur;
    /**
     * textView des erreurs de saisies.
     */
    private TextView erreurSaisies;
    /**
     * Spinner du choix du mode.
     */
    private Spinner choixMode;
    /**
     * RecyclerView modif article.
     */
    private RecyclerView modifArticleRecyclerView;

    /**
     * Liste des entrepots.
     */
    public static ArrayList<String> listeEntrepots;
    /**
     * Listes des articles.
     */
    public static ArrayList<String> listeArticles;
    /**
     * Listes des stocks.
     */
    public static ArrayList<String> listeStock;
    /**
     * Liste du choix du mode.
     */
    private ArrayList<String> listeChoixMode;
    /**
     * Liste pour la réference.
     */
    public static ArrayList<String> listeRef;
    /**
     * Liste avant les stocks.
     */
    private ArrayList<Integer> listeStockAvant;

    /**
     * Liste pour la quantite saisie.
     */
    private ArrayList<Integer> listeQuantiteSaisie;

    /**
     * Liste des ids des articles
     */
    private ArrayList<String> listeIdArticle;

    /**
     * Liste de paire contenant les articles et leurs id associés
     */
    public static ArrayList<Pair<String, String>> listeArticlesIdEtNom;

    /**
     * Liste de quartet (quatres valeurs) contenant l'id, le libellé, sa référence et son stock
     */
    public static ArrayList<Quartet<String, String, String, String>> listeInfosArticle;

    /**
     * Liste de paire contenant les entrepôts et leurs id associés
     */
    public static ArrayList<Pair<String, String>> listeEntrepotIdEtNom;

    /**
     * Liste des articles à modifier.
     */
    private ArrayList<ModifListe> articlesAModifier;

    /**
     * Adapter pour la modif d'une liste.
     */
    private ModifListeAdapter adaptateurModifListe;

    /**
     * Adpater pour la liste du choix du mode.
     */
    private ArrayAdapter<String> adaptateurListeChoixMode;
    /**
     * String por le token de l'api.
     */
    private String token;
    /**
     * String pour le user de l'api.
     */
    private String user;
    /**
     * String pour l'URL de l'API.
     */
    private String urlApi;
    /**
     * Pour controler si l'entrepot existe.
     */
    private boolean entrepotOk;

    /**
     * Méthode on create
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modif_liste_activity);

        // Initialse les composants de base
        initialiserComposants();

        try {
            // Permet d'initialiser les variables à utiliser pour contacter l'API
            initialiserVariableAPI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Configure les différents listener
        configurerListeners();

        // Initialise tous les adaptateurs
        initialiserAdapteurs();

        // Charge les données initiales de la vue
        chargerDonneesInitiales();
    }

    /**
     * Initialisation des composants de l'interface utilisateur.
     */
    private void initialiserComposants() {
        saisieNomListe = findViewById(R.id.saisieNomListe);
        saisieNomEntrepot = findViewById(R.id.saisieNomEntrepot);
        saisieCodeArticle = findViewById(R.id.saisieCodeArticle);
        libelleStock = findViewById(R.id.libelleStock);
        saisieQuantite = findViewById(R.id.saisieQuantite);
        texteErreur = findViewById(R.id.erreurSaisieEntrepot);
        erreurSaisies = findViewById(R.id.erreurSaisies);
        choixMode = findViewById(R.id.ddlModeMaj);
        modifArticleRecyclerView = findViewById(R.id.ajout_article_recycler);

        listeEntrepots = new ArrayList<>();
        listeArticles = new ArrayList<>();
        listeStock = new ArrayList<>();
        listeChoixMode = new ArrayList<>();
        listeRef = new ArrayList<>();
        articlesAModifier = new ArrayList<>();
        listeStockAvant = new ArrayList<>();
        listeQuantiteSaisie = new ArrayList<>();
        listeArticlesIdEtNom = new ArrayList<>();
        listeEntrepotIdEtNom = new ArrayList<>();
        listeInfosArticle = new ArrayList<>();
        listeIdArticle = new ArrayList<>();

        entrepotOk = false;
    }

    /**
     * Méthode pour initialiser l'API.
     * @throws Exception exception.
     */
    private void initialiserVariableAPI() throws Exception {
        // On ouvre le fichier infouser pour récupérer les infos à propos de l'utilisateur
        InputStreamReader infosUser = new InputStreamReader(openFileInput("infouser.txt"));

        // On appelle une méthode pour récupérer les infos de l'utilisateur
        Quartet<String, String, String, String> infos = OutilDivers.getInfosUserAndApi(infosUser);

        // Récupère le token
        token = infos.first();

        // Récupère l'URL de l'API
        urlApi = infos.second();

        // Récupère le nom de l'utilisateur
        user = infos.third();
    }

    /**
     * Configuration des listeners pour les composants interactifs.
     */
    private void configurerListeners() {
        saisieNomEntrepot.addTextChangedListener(new VerificationEntrepotTextWatcher());
        saisieCodeArticle.addTextChangedListener(new FiltreArticleTextWatcher());
    }

    /**
     * Initialisation et configuration des adapteurs.
     */
    private void initialiserAdapteurs() {
        // On ajoute "Ajout" et "Modification à la liste permettant de choisir le mode
        listeChoixMode.add(getString(R.string.Ajout));
        listeChoixMode.add(getString(R.string.Modification));

        adaptateurListeChoixMode = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, listeChoixMode);
        choixMode.setAdapter(adaptateurListeChoixMode);

        adaptateurModifListe = new ModifListeAdapter(articlesAModifier);
        modifArticleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        modifArticleRecyclerView.setAdapter(adaptateurModifListe);
    }

    /**
     * Chargement des données initiales pour les entrepôts et les articles.
     */
    private void chargerDonneesInitiales() {
        // Récupère la liste des entrepôts
        getListeEntrepot(urlApi, token, getApplicationContext(), "ModifListe");

        // Récupère la liste des articles
        getArticles(urlApi, token, getApplicationContext(), "ModifListe");
    }

    /**
     * Lors d'un click pour modifier un article à la liste
     * @param view la vue
     */
    public void clickAjouter(View view) {
        // On vérifie si les champs saisies sont valides
        Pair<Boolean, String> verif = verificationChamp(false);
        // Si ce n'est pas valide, alors on affiche une ereur
        if(Boolean.FALSE.equals(verif.first)) {
            erreurSaisies.setText(String.format("%s : %s", getString(R.string.ErreurSaisie),
                    verif.second));
        // Sinon, on traite normalement l'article
        } else {
            erreurSaisies.setText("");

            String libelleArticle = "";
            int quantite = 0;
            int iteration = 0;
            String idArticle = "";
            // On récupère le code article saisie
            String valeurSaisieCodeArticle = saisieCodeArticle.getText().toString();
            // On récupère la quantité saisie
            int quantiteSaisie = Integer.parseInt(saisieQuantite.getText().toString());

            // On boucle sur la liste des références de tous les articles dans l'entrepôt saisi
            for(String valLabel : listeRef) {
                // Si l'article saisie est égal à valLabel
                if(valeurSaisieCodeArticle.equals(valLabel)) {
                    // On met à jour la quantité de l'article dans l'entrepôt
                    quantite = Integer.parseInt(listeStock.get(iteration));
                    // On ajoute le stock avant
                    listeStockAvant.add(Integer.valueOf(listeStock.get(iteration)));
                }
                iteration++;
            }

            // On remet l'itération à 0
            iteration = 0;
            // On boucle sur les infos de tous article
            for(Quartet<String, String, String, String> pair : listeInfosArticle) {
                if(valeurSaisieCodeArticle.equals(pair.third())) {
                    // On récupère le libellé de l'article
                    libelleArticle = pair.second();
                    // On récupère l'ID de l'article
                    idArticle = pair.first();
                }
                iteration++;
            }

            // On désactive la saisie d'un entrepot car il peut y en avoir seulement un par liste
            saisieNomEntrepot.setEnabled(false);

            // Si le choix du mode est "Ajout
            if(choixMode.getSelectedItem().toString().equals(getString(R.string.Ajout))) {
                // On ajoute à la quantité la quantite saisie
                quantite += quantiteSaisie;
                // On verouille la possibilité de choisir un autre mode
                // Car il n'y a qu'un mode par liste
                if(listeChoixMode.size() > 1) {
                    listeChoixMode.remove(1);
                    adaptateurListeChoixMode.notifyDataSetChanged();
                }
            // Sinon le choix du mode est "Modification"
            } else {
                // On remplace la quantité par la quantité saisie
                quantite = quantiteSaisie;
                // On verouille la possibilité de choisir un autre mode
                // Car il n'y a qu'un mode par liste
                if(listeChoixMode.size() > 1) {
                    listeChoixMode.remove(0);
                    adaptateurListeChoixMode.notifyDataSetChanged();
                }
            }
            // On récupère la quantité saisie pour la mettre dans une liste
            listeQuantiteSaisie.add(quantiteSaisie);
            // On récupère l'ID de l'article pour le mettre dans une liste
            listeIdArticle.add(idArticle);
            // On ajoute au recycler view l'article
            articlesAModifier.add(new ModifListe(libelleArticle,  valeurSaisieCodeArticle, String.valueOf(quantite)));
            // On met à jour l'adaptateur pour qu'il mette à jour la vue
            adaptateurModifListe.notifyDataSetChanged(); // Mise à jour de l'adaptateur après l'ajout
        }
    }

    /**
     *  Permet de vérifier si l'entrepôt, l'article ou autre sont correct
     * @param verifValiderFichier un boolean verifiant le fichier
     * @return une pair
     */
    public Pair<Boolean, String> verificationChamp(boolean verifValiderFichier) {
        // On récupère le code article saisie
        String valeurSaisieArticle = saisieCodeArticle.getText().toString();
        // On récupère la quantité saisie
        String valeurSaisieQuantite = saisieQuantite.getText().toString();

        // Si on fait la vérif au moment de valider la liste
        if (verifValiderFichier) {
            String valeurSaisieNomListe = saisieNomListe.getText().toString().trim();
            if (valeurSaisieNomListe.isEmpty()) {
                return new Pair<>(false, getString(R.string.veuillez_saisir_nom_liste));
            }

            if (!entrepotOk) {
                return new Pair<>(false, getString(R.string.entrepot_incorrect));
            }
            return new Pair<>(true, "");
        }

        // Si l'utilisateur n'a pas saisi d'article
        if (valeurSaisieArticle.isEmpty()) {
            return new Pair<>(false, getString(R.string.veuillez_saisir_code_article));
        }

        // Si l'utilisateur n'a pas saisi de quantité
        if(valeurSaisieQuantite.isEmpty()) {
            return new Pair<>(false, getString(R.string.veuillez_saisir_quantite));
        }

        // Si l'article est incorrect
        if (!listeRef.contains(valeurSaisieArticle)) {
            return new Pair<>(false, getString(R.string.code_article_incorrect));
        }

        for (ModifListe modifListe : articlesAModifier) {
            if (modifListe.getCodeArticle().equals(valeurSaisieArticle)) {
                return new Pair<>(false, getString(R.string.article_deja_present));
            }
        }

        // Si il n'y a aucun problème, on renvoie true
        return new Pair<>(true, "");
    }

    /**
     * Suppression de l'article
     * @param view la vue
     */
    public void supprimerArticle(View view) {
        // On récupère la position de la ligne à supprimer
        int position = (int) view.getTag();
        // Si ce n'est pas le dernier article de la liste
        if (position >= 0 && position < articlesAModifier.size()) {
            listeQuantiteSaisie.remove(position);
            articlesAModifier.remove(position);
            adaptateurModifListe.notifyItemRemoved(position);
        }
        // Cas où il n'y a plus d'article ajouter,
        // On remet à jour le choix du mode
        if(articlesAModifier.isEmpty()) {
            listeChoixMode.clear();
            listeChoixMode.add(getString(R.string.Ajout));
            listeChoixMode.add(getString(R.string.Modification));
            saisieNomEntrepot.setEnabled(true);
        }
    }

    /**
     * Réinitialisation des champs
     * @param view la vue
     */
    public void reinitialiserChamp(View view) {
        saisieNomListe.setText("");
        saisieNomEntrepot.setText("");
        saisieCodeArticle.setText("");
        saisieQuantite.setText("");
        libelleStock.setText("");
        texteErreur.setText("");
        erreurSaisies.setText("");
        listeChoixMode.clear();
        listeChoixMode.add(getString(R.string.Ajout));
        listeChoixMode.add(getString(R.string.Modification));
        saisieNomEntrepot.setEnabled(true);
        adaptateurListeChoixMode.notifyDataSetChanged();
        articlesAModifier.clear();
        adaptateurModifListe.notifyDataSetChanged();
    }

    /**
     * Validation de la liste
     * @param view la vue
     * @throws IOException l'exception
     */
    public void validerListe(View view) throws IOException {
        // On vérifie si les champs saisies sont valides
        Pair<Boolean, String> verif = verificationChamp(true);

        // Si ce n'est pas valide, alors on retourne une erreur
        if(Boolean.FALSE.equals(verif.first)) {
            erreurSaisies.setText(String.format("%s %s", getString(R.string.ErreurSaisie), verif.second));
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

            // On met à jour le format pour la date
            simpleDateFormat.applyPattern("yyyy-dd-MM");
            String date = simpleDateFormat.format(Calendar.getInstance().getTime());

            // On met à jour le format pour l'heure
            simpleDateFormat.applyPattern("HH:mm");
            String heure = simpleDateFormat.format(Calendar.getInstance().getTime());
            // On récupère le nom de la liste, de l'entrepôt et le choix du mode
            String nomListe = saisieNomListe.getText().toString();
            String nomEntrepot = saisieNomEntrepot.getText().toString();
            String modeMaj = choixMode.getSelectedItem().toString();

            String refArticle;
            String libelleArticle;
            String quantiteSaisie;
            String stockAvant;
            String stockApres;
            String idArticle;
            String ligneFichier;

            // On récupère l'ID de l'entrepôt
            String idEntrepot = "";
            for(Pair<String, String> pair : listeEntrepotIdEtNom) {
                if(pair.second.equals(nomEntrepot)) {
                    idEntrepot = pair.first;
                }
            }

            erreurSaisies.setText("");

            // On construit le nom du fichier
            String nomFichier = user + nomListe.toLowerCase().replace(" ", "")
                    + date + heure;

            // On crée un nouveau fichier pour écrire la liste
            FileOutputStream fichier = openFileOutput(nomFichier + ".txt",
                    Context.MODE_PRIVATE);
            // On boucle sur l'ensemble des articles de la liste
            for(int i = 0; i < articlesAModifier.size(); i++) {
                refArticle = articlesAModifier.get(i).getCodeArticle();
                libelleArticle = articlesAModifier.get(i).getLibelleArticle();
                quantiteSaisie = listeQuantiteSaisie.get(i).toString();
                stockAvant = listeStockAvant.get(i).toString();
                stockApres = articlesAModifier.get(i).getQuantite();
                idArticle = listeIdArticle.get(i);

                // On constitue la ligne du fichier
                ligneFichier = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%s",
                        user, nomListe, refArticle, libelleArticle, nomEntrepot, quantiteSaisie,
                        modeMaj, stockAvant, stockApres, date, heure, idArticle, idEntrepot, "\n");
                // On écrit la ligne dans le fichier
                fichier.write(ligneFichier.getBytes());
            }

            // On retourne sur la page de visualisation de toutes les listes
            Intent intention = new Intent(ModifListeActivity.this, ListeActivity.class);
            startActivity(intention);

        }
    }

    /**
     * Verification de l'entrepot.
     */
    private class VerificationEntrepotTextWatcher implements TextWatcher {
        /**
         * Avant la modification du texte.
         * @param s charSequence.
         * @param start int.
         * @param count int.
         * @param after int.
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Avant la modification du texte, pas d'action nécessaire
        }

        /**
         * Lorsque que le texte change.
         * @param s charSequence.
         * @param start int.
         * @param before int.
         * @param count int.
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // A chaque modification du texte
            String saisie = s.toString();
            Pair<String, String> idEtNomEntrepotActuel = new Pair<>("", "");
            listeEntrepots = new ArrayList<>();
            for(Pair<String, String> pair : listeEntrepotIdEtNom)
            {
                listeEntrepots.add(pair.second);
                // On vérifie si le nom de l'entrepôt est celui saisi par l'utilisateur
                if(pair.second.contains(saisie)) {
                    idEtNomEntrepotActuel = new Pair<>(pair.first, pair.second);
                }
            }

            // Si l'entrepôt existe
            if (listeEntrepots.contains(saisie)) {
                // On indique à l'utilisateur que l'entrepôt est correct
                texteErreur.setText(R.string.EntrepotOk); // Pas d'erreur
                texteErreur.setTextColor(getResources().getColor(R.color.green, getTheme()));
                entrepotOk = true;
                // On appel verifArticle pour mettre à jour la liste des articles
                // en fonction de l'entrepôt
                VerifArticles(idEtNomEntrepotActuel.first);
            } else {
                // On vide la liste des noms et codes articles et de leur stock
                listeArticles.clear();
                listeRef.clear();
                listeStock.clear();
                // On indique à l'utilisateur que l'entrepôt est incorrect
                texteErreur.setText(R.string.EntrepotIncorrect);
                texteErreur.setTextColor(getResources().getColor(R.color.red, getTheme())); // Utilisez une couleur appropriée
                entrepotOk = false;
            }
        }

        /**
         * Pas d'action.
         * @param s un editable.
         */
        @Override
        public void afterTextChanged(Editable s) {
            // Après la modification du texte, pas d'action nécessaire
        }
    }

    /**
     * Permet de vérifier si un article se trouve dans un entrepot
     * @param idEntrepot id de l'entrepôt
     */
    public void VerifArticles(String idEntrepot) {
        for(Quartet<String, String, String, String> quartet : listeInfosArticle) {
            RequetesApi.GetArticlesByEntrepot(urlApi, token, getApplicationContext(),
                    "AjoutListe", quartet.first(), idEntrepot,quartet, null);
        }
    }


    /**
     * Vérifie à chaque saisie de l'utilisateur sur le champ saisie article
     */
    private class FiltreArticleTextWatcher implements TextWatcher {
        /**
         * Avant qu'un texte change.
         * @param s charsequence.
         * @param start int.
         * @param count int.
         * @param after int.
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Avant la modification du texte, pas d'action nécessaire
        }

        /**
         * Quand l'utilisateur saisie du texte sur le champ saisieArticle
         * @param s charsequence.
         * @param start int.
         * @param before int.
         * @param count int.
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // A chaque modification du texte
            String saisie = s.toString();
            // Lorsque l'utilisateur saisi 3 caractères ou plus
            if (saisie.length() >= 3) {
                // ArrayList pour proposer à l'utilisateur des suggestions de saisies en fonction
                // du code article
                ArrayList<String> suggestions = new ArrayList<>();
                for (String ref : listeRef) {
                    if (ref.toLowerCase().contains(saisie.toLowerCase())) {
                        suggestions.add(ref);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ModifListeActivity.this,
                        android.R.layout.simple_dropdown_item_1line, suggestions);
                saisieCodeArticle.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            // Mettre à jour le stock disponible si l'article est dans la liste
            if (listeRef.contains(saisie)) {
                int index = listeRef.indexOf(saisie);
                libelleStock.setText(String.format("%s en stock", listeStock.get(index)));
            } else {
                libelleStock.setText("");
            }
        }

        /**
         * Apres que le texte change
         * @param s editable
         */
        @Override
        public void afterTextChanged(Editable s) {
            // Après la modification du texte, pas d'action nécessaire
        }
    }

    /**
     * Méthode invoquée automatiquement lors d'un clic sur l'image bouton
     * de retour vers l'activité principale
     * @param view  source du clic
     */
    public void onClickRetour(View view) {

        // création d'une intention pour informer l'activté parente
        Intent intentionRetour = new Intent();

        // retour à l'activité parente et destruction de l'activité fille
        setResult(Activity.RESULT_OK, intentionRetour);
        finish(); // destruction de l'activité courante
    }

}