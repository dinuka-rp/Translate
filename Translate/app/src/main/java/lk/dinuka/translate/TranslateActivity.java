package lk.dinuka.translate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;
import com.ibm.watson.language_translator.v3.util.Language;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;

import java.util.HashMap;
import java.util.List;

import lk.dinuka.translate.databases.foreign.ForeignLanguage;
import lk.dinuka.translate.databases.foreign.ForeignRepository;

import static com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions.Voice.EN_US_LISAVOICE;

public class TranslateActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private LanguageTranslator translationService;          // translation service
    private TextToSpeech textService;           // Text to speech service


    private String translationLanguageCode;             // used to pass in the translation code of the chosen language
    private String translationText;                     // The English text that is required to be translated
    private TextView displayTranslation;
    public String selectedSpinnerLanguage;         // holds the translation language Name chosen from the spinner

    private StreamPlayer player = new StreamPlayer();


    public static HashMap<String, String> languageCodes = new HashMap<>();        // holds all Foreign language names with language codes


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        displayTranslation = findViewById(R.id.translated_textView);        // TextView to display translation


        // import all foreign languages list and add to temporary HashMap(for this activity) - <name, code>
        getLangNamesCode();


        //---------Spinner

        // create the spinner
        Spinner spinner = findViewById(R.id.language_spinner);

        if (spinner != null) {
            spinner.setOnItemSelectedListener(this);
        }

        // Create ArrayAdapter using the string array and default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);


        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner.
        if (spinner != null) {
            spinner.setAdapter(adapter);
        }
    }


    public void translateChosenEnglish(View view) {         // Translates and displays text onClick of Translate button

        // get translation language from spinner and assign here
        // get the translation code of the chosen language
        translationLanguageCode = languageCodes.get(selectedSpinnerLanguage);


        // get English word/ phrase to be translated---------->>>>>>>>>>>>>>>>>>>
//        translationText = findViewById(R.id.);
        translationText = "Hello World";


        // translate using Watson Translator
        translationService = initLanguageTranslatorService();           // connect & initiate to the cloud translation service
        new TranslationTask().execute(translationText, translationLanguageCode);

    }

    public void pronounceTranslation(View view) {  // pronounce translated word/ phrase

        textService = initTextToSpeechService();           // connect & initiate to the cloud text to speech service

        // speak displayed translation
        new SynthesisTask().execute(displayTranslation.getText().toString(),EN_US_LISAVOICE);
    }


    // ---------------------
//    public void displayToast(String message) {
//        Toast.makeText(getApplicationContext(), message,
//                Toast.LENGTH_SHORT).show();
//    }

    public void getLangNamesCode() {
        // get all foreign languages from db. Extract language name & subscription status
        ForeignRepository foreignRepository = new ForeignRepository(getApplicationContext());

        foreignRepository.getLangsFromDB().observe(this, new Observer<List<ForeignLanguage>>() {
            @Override
            public void onChanged(@Nullable List<ForeignLanguage> allLangs) {
                for (ForeignLanguage language : allLangs) {
                    languageCodes.put(language.getLanguage(), language.getLanguageCode());     // get the subscription status of the language saved in the db
                }
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String spinnerLabel = adapterView.getItemAtPosition(i).toString();
        selectedSpinnerLanguage = spinnerLabel;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private LanguageTranslator initLanguageTranslatorService() {           // connect & initiate to the cloud translation service
        Authenticator authenticator = new IamAuthenticator("2daMreRDE8V5zPRO3enCVHGUCH1sQJs-Kdq8ryPn4-ij");

        LanguageTranslator service = new LanguageTranslator("2018-05-01", authenticator);

        service.setServiceUrl("https://api.us-south.language-translator.watson.cloud.ibm.com/instances/caf1b5bc-ff11-4271-96cf-93372088290d");

        return service;
    }


    private class TranslationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TranslateOptions translateOptions = new TranslateOptions.Builder()
                    .addText(params[0])
                    .source(Language.ENGLISH)
                    .target(params[1])    // pass in translationLanguage here, to get required language
                    .build();

            TranslationResult result = translationService.translate(translateOptions).execute().getResult();

            String firstTranslation = result.getTranslations().get(0).getTranslation();

            return firstTranslation;
        }

        @Override
        protected void onPostExecute(String translatedText) {
            super.onPostExecute(translatedText);
            displayTranslation.setText(translatedText);
        }
    }


    // ---------------------

    private TextToSpeech initTextToSpeechService() {
        Authenticator authenticator = new IamAuthenticator("fV_5OUIol2WXx_pXQ2oG9-PWJFsbBr2I6tiDKOrWUB7k");
        TextToSpeech service = new TextToSpeech(authenticator);
        service.setServiceUrl("https://api.us-south.text-to-speech.watson.cloud.ibm.com/instances/2aa0807f-fff7-4ea6-a9aa-5828fa2f2020");
        return service;
    }


    private class SynthesisTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
                    .text(params[0])            // only one String parameter is entered. This is the translated text.
                    .voice(EN_US_LISAVOICE)     // ------>>>>>>this will speak out only in English. params[1] can be used to get translated language
                    .accept(HttpMediaType.AUDIO_WAV)
                    .build();
            player.playStream(textService.synthesize(synthesizeOptions).execute().getResult());

            return "Did synthesize";
        }
    }

}
