package com.dam.safebar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class LogInRest extends AppCompatActivity {

    private FirebaseAuth fba;
    private FirebaseUser user;

    String email;
    String password;

    TextInputLayout etUsuarioEmail;
    TextInputLayout etPassword;

    SharedPreferences loginData;
    SharedPreferences.Editor loginDataEditor;

    MaterialCheckBox chRememberRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_rest);
        ImageView background = findViewById(R.id.ivBackgroundLoginRest);
        Button btnLogin = findViewById(R.id.btnLoginRest);
        etUsuarioEmail = findViewById(R.id.lLoginUsuarioRest);
        etPassword = findViewById(R.id.lLoginPasswordRest);
        SwitchMaterial swRest = findViewById(R.id.switchLoginRest);
        chRememberRest = findViewById(R.id.chLoginRest);

        loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
        loginDataEditor = loginData.edit();

        Glide.with(this)
                .load(R.drawable.rest)
                .fitCenter()
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(6, 1)))
                .into(background);

        fba = FirebaseAuth.getInstance();
        user = fba.getCurrentUser();

        btnLogin.setOnClickListener(v -> {
            if (checkEmpty(etUsuarioEmail)) {

                etUsuarioEmail.setError(null);

                if (checkEmpty(etPassword)) {

                    etPassword.setError(null);

                    if (chRememberRest.isChecked()) {
                        loginDataEditor.putInt(LogIn.REMEMBER_ME_DATA, LogIn.REMEMBER_REST);
                        loginDataEditor.apply();
                    } else {
                        loginDataEditor.clear();
                        loginDataEditor.commit();
                    }

                    comprobarUsuario();

                } else { etPassword.setError("Obligatorio"); }

            } else { etUsuarioEmail.setError("Obligatorio"); }

        });

        swRest.setChecked(true);
        swRest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                Intent i = new Intent(LogInRest.this, LogIn.class);
                startActivity(i);
            }
        });
    }

    private void comprobarUsuario() {

        email = etUsuarioEmail.getEditText().getText().toString().trim();
        password = etPassword.getEditText().getText().toString().trim();

        fba.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user = fba.getCurrentUser();
                        acceder();

                    } else {
                        Snackbar snackbar = Snackbar
                                .make(getWindow().getDecorView().getRootView(), R.string.user_not_found, Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.orange_dark));
                        snackbar.setAnchorView(R.id.llSwitchRest);
                        snackbar.show();
                    }

                });

    }

    private void acceder() {
        Intent intent = new Intent(LogInRest.this, PerfilRest.class);
        startActivity(intent);
        finish();
    }

    private boolean checkEmpty(TextInputLayout et) {
        return !et.getEditText().getText().toString().isEmpty();
    }

    private String getInputString(TextInputLayout et) {
        return et.getEditText().getText().toString();
    }

    public void goSignUp(View view) {
        Intent i = new Intent(this, SignUpRest.class);
        startActivity(i);
    }
}