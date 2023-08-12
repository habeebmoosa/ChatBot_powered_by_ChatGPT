package com.generativeaichatbot.gaichatbot;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //Declarations
    EditText inputMessage;
    TextView dialog_input_box_textview;
    EditText export_input_box;
    ImageButton imageButton , speechBtn;
    Button copy_response_content , pdf_response_content , doc_response_content;
    Button export_input_ok_button, export_input_cancel_button;
    List<Message> messageList;
    chatAdapter chatAdapt;
    RecyclerView recyclerView;
    AlertDialog.Builder builder;
    TextToSpeech textToSpeech;
    boolean speechCheckerVar = false;
    ApiFetch apiFetch;


    //This onDestroy() method is used to stop the voice of the bot
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }


    //This creates main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        apiFetch = new ApiFetch(MainActivity.this);

        //Defination for the export dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.export_options_dialog);

        inputMessage = findViewById(R.id.inputMessage);
        imageButton = findViewById(R.id.imageButton);
        speechBtn = findViewById(R.id.speechBtn);

        copy_response_content = dialog.findViewById(R.id.copy_response_content);
        pdf_response_content = dialog.findViewById(R.id.pdf_response_content);
        doc_response_content = dialog.findViewById(R.id.doc_response_content);

        messageList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        chatAdapt = new chatAdapter(messageList); //chatAdapter

        //This method is used to call the dialog with options by long press on the response msg
        chatAdapt.setOnItemLongClickListener(new chatAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(int position) {
                Message clickedMessage = messageList.get(position);
                String messageText = clickedMessage.getMessage();

                if(position%2 !=0){

                    //copy response msg
                    copy_response_content.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("copy",messageText);
                            clipboardManager.setPrimaryClip(clipData);

                            Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });

                    //export response msg as pdf
                    pdf_response_content.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            InputTextDialog(MainActivity.this,
                                    "Don't write name with extension (.pdf)",
                                    "Enter file name",
                                    "",
                                    new InputTextDialogCallback() {
                                        @Override
                                        public void onInputReceived(String userInput) {
                                            ExportsMessage.createPdf(MainActivity.this, messageText, userInput);
                                        }
                                    });

                            dialog.dismiss();
                        }
                    });

                    //export response msg as docx
                    doc_response_content.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            InputTextDialog(MainActivity.this,
                                    "Don't write name with extension (.docx)",
                                    "Enter file name",
                                    "",
                                    new InputTextDialogCallback() {
                                        @Override
                                        public void onInputReceived(String userInput) {
                                            ExportsMessage.createDoc(MainActivity.this, messageText, userInput);
                                        }
                                    });
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
                return true; 
            }
        });

        builder = new AlertDialog.Builder(this);
        recyclerView.setAdapter(chatAdapt);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        //Text to speech initialization
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }else{
                    Toast.makeText(MainActivity.this, "Language is not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //This method is used to swap between audio and send button
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String inputTempMsg = inputMessage.getText().toString();

                if(!inputTempMsg.isEmpty()){
                    imageButton.setVisibility(View.VISIBLE);
                    speechBtn.setVisibility(View.INVISIBLE);
                }
                else{
                    imageButton.setVisibility(View.INVISIBLE);
                    speechBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        //This calls when send button is clicked
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg;

                if(TextUtils.isEmpty(inputMessage.getText().toString())){
                    msg="";
                }else {
                    msg = inputMessage.getText().toString(); //.trim()
                    setTheUserSendMsg(msg);
                    Log.d("api work key sys","its callednaturee");
                }
            }
        });


        //This calls when speech button is clicked
        speechBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("itscalled","its called nature6");
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start Specking");
                Log.d("itscalled","its called nature7");

                if(intent.resolveActivity(getPackageManager()) != null){
                    Log.d("itscalled","its called nature8");
                    startActivityForResult(intent,100);
                }
            }
        });

    }


    //This method is used by the speech recognition system
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("itscalled","its called nature3");
        String msg;

        if(requestCode == 100 && resultCode == RESULT_OK){
            Log.d("itscalled","its called nature4");
            msg = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            Log.d("itscalled","its called nature5");
            setTheUserSendMsg(msg);
        }
    }


    //This function is used to send the query from the user to API_call method
    void setTheUserSendMsg(String msg){
        addChatMessage(msg,Message.SEND_BY_USER);
        Log.d("itscalled","its called nature1");
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        Log.d("itscalled","its called nature2");
        inputMessage.setText("");
        API_call(msg);
        Log.d("itscalled","its called nature11");
    }


   // This method is used for taking the input from the user through dialog box
    private void InputTextDialog(Context context, String textViewText, String editTextHint, String editTextText, InputTextDialogCallback callback) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.export_name_input);

        export_input_ok_button = dialog.findViewById(R.id.export_input_ok_button);
        export_input_cancel_button = dialog.findViewById(R.id.export_input_cancel_button);
        dialog_input_box_textview = dialog.findViewById(R.id.dialog_input_box_textview);

        export_input_box = dialog.findViewById(R.id.export_input_box);

        export_input_box.setHint(editTextHint);
        export_input_box.setText(editTextText);

        dialog_input_box_textview.setText(textViewText);

        export_input_ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInput = export_input_box.getText().toString();
                if (!userInput.isEmpty()) {
                    callback.onInputReceived(userInput);
                } else {
                    Toast.makeText(MainActivity.this, "It is required", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
                if(textViewText.equals("Write the api key from OpenAI website")) restart();
            }
        });

        export_input_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    //It is used for returning the input from the user of the above dialog
    interface InputTextDialogCallback {
        void onInputReceived(String userInput);
    }


    //This method is used to create the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_page_menu,menu);
        return true;
    }


    //This method is used to create the options on option menu bar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.turnOnOf:
                if(speechCheckerVar) {
                    speechCheckerVar = false;
                    item.setTitle("Speech On");
                    Toast.makeText(this, "Text to Speech Off", Toast.LENGTH_SHORT).show();
                    textToSpeech.stop();
                }
                else{
                    speechCheckerVar = true;
                    item.setTitle("Speech Off");
                    Toast.makeText(this, "Text to Speech On", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.insertApiKey:

                InputTextDialog(MainActivity.this,
                        "Write the api key from OpenAI website",
                        "Enter Api Key",
                        getApiKey(),
                        new InputTextDialogCallback() {
                            @Override
                            public void onInputReceived(String userInput) {
                                // Handle the user input here
                                saveApiKey(userInput);
                            }
                        });
                break;

            default: break;
        }
       return true;
    }


    //It is used to save the api key
    private void saveApiKey(String apiKey) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("api_key", apiKey);
        editor.apply();
    }


    //It is used to return the api key
    private String getApiKey() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("api_key", "");
    }


    //This method is used to add the chat to list with recycler view
    void addChatMessage(String message,String sendBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sendBy));
                chatAdapt.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(chatAdapt.getItemCount());

                imageButton.setEnabled(true);
                speechBtn.setEnabled(true);
            }
        });
    }


    //This method send the response to the addChatMessage() method
    void addResponse(String response){
        messageList.remove(messageList.size()-1);

        if(speechCheckerVar){
            textToSpeech.speak(response,TextToSpeech.QUEUE_FLUSH,null);
        }

        addChatMessage(response,Message.SEND_BY_BOT);
    }


    //This method is used for API calls from the ApiFetch class
    void API_call(String msg) {
        Log.d("itscalled","its called nature12");
        messageList.add(new Message("...", Message.SEND_BY_BOT));
        imageButton.setEnabled(false);
        speechBtn.setEnabled(false);

        Log.d("itscalled","its called nature13");
        apiFetch.fetchResponse(msg, new ApiFetch.ApiCallback() {
            @Override
            public void onResponse(String response) {
                addResponse(response);
            }
        });
    }


    private void restartMainActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    private void restart() {
        restartMainActivity();
    }

}