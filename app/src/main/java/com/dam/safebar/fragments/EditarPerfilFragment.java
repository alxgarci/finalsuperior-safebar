package com.dam.safebar.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dam.safebar.R;
import com.dam.safebar.javabeans.Usuario;
import com.dam.safebar.listeners.CuentaListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class EditarPerfilFragment extends Fragment {

    public static final int RC_PHOTO_ADJ = 1;

    FirebaseAuth fba;
    FirebaseUser user;

    DatabaseReference dbRef;
    ValueEventListener vel;
    Uri selectedUri;
    StorageReference mFotoStorageRef;

    Usuario usuLoged;
    Usuario usuEditado;
    CuentaListener listener;

    String nombre;
    String email;
    String password;
    String direc;
    boolean fotoCambiada;

    ImageView imageView;
    ImageButton imbEditarImagen;
    EditText etNom;
    EditText etEmail;
    EditText etPw;
    EditText etDirec;
    Button btnGuardar;


    public EditarPerfilFragment() {
        // Required empty public constructor
    }


    public EditarPerfilFragment newInstance() {
        EditarPerfilFragment fragment = new EditarPerfilFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_perfil, container, false);

        imageView = view.findViewById(R.id.imgEditPerfFrag);
        imbEditarImagen = view.findViewById(R.id.imbEditPerfFrag);
        etNom = view.findViewById(R.id.etEditPerfFragNom);
        etEmail = view.findViewById(R.id.etEditPerfFragEmail);
        etPw = view.findViewById(R.id.etEditPerfFragPW);
        etDirec = view.findViewById(R.id.etEditPerfFragDirec);

        fba = FirebaseAuth.getInstance();
        user = fba.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("datos/usuarios");
        mFotoStorageRef = FirebaseStorage.getInstance().getReference().child("fotos");

        //BTN GUARDAR BARRA DE TAREAS
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar();

        fotoCambiada = false;

        addListener();

        imbEditarImagen.setOnClickListener(v -> {
            Toast.makeText(getContext(), R.string.selecciona_imagen, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete la acci??n usando"), RC_PHOTO_ADJ);
            fotoCambiada = true;

        });


        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.editar_perfil_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.i("EDITARPERFILFRAGMENT", "ONOPTIONSITEMSELECTED");
        if (item.getItemId() == R.id.itemGuardarPerfil) {

            nombre = etNom.getText().toString().trim();
            email = etEmail.getText().toString().trim();
            password = etPw.getText().toString().trim();
            direc = etDirec.getText().toString().trim();

            if (fotoCambiada) {
                final StorageReference fotoRef = mFotoStorageRef.child(selectedUri.getEncodedPath());
                UploadTask ut = fotoRef.putFile(selectedUri);
                Task<Uri> urlTask = ut.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fotoRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        usuEditado = new Usuario(downloadUri.toString() ,  nombre, email, password, direc);

                        user.updateEmail(email);
                        user.updatePassword(password);

                        //dbRef.child(user.getUid()).setValue(usuEditado);
                        dbRef.child(user.getUid()).child("direccion").setValue(usuEditado.getDireccion());
                        dbRef.child(user.getUid()).child("email").setValue(usuEditado.getEmail());
                        dbRef.child(user.getUid()).child("nombre").setValue(usuEditado.getNombre());
                        dbRef.child(user.getUid()).child("password").setValue(usuEditado.getPassword());
                        dbRef.child(user.getUid()).child("urlFoto").setValue(downloadUri.toString());

                        Snackbar snackbar = Snackbar
                                .make(getActivity().getWindow().getDecorView().getRootView(), R.string.perfil_modificado_ok, Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.green_dark));


                        //View para introducir margen por encima del BottomBar
                        View snackBarView = snackbar.getView();
                        snackBarView.setTranslationY(-(convertDpToPixel(56, getActivity())));
                        snackbar.show();

                        listener.abrirConfiguracion();

                    }
                });
            } else {
                usuEditado = new Usuario(usuLoged.getUrlFoto() , nombre, email, password, direc);

                user.updateEmail(email);
                user.updatePassword(password);

                //dbRef.child(user.getUid()).setValue(usuEditado);
                dbRef.child(user.getUid()).child("direccion").setValue(usuEditado.getDireccion());
                dbRef.child(user.getUid()).child("email").setValue(usuEditado.getEmail());
                dbRef.child(user.getUid()).child("nombre").setValue(usuEditado.getNombre());
                dbRef.child(user.getUid()).child("password").setValue(usuEditado.getPassword());
                dbRef.child(user.getUid()).child("urlFoto").setValue(usuLoged.getUrlFoto());

                Snackbar snackbar = Snackbar
                        .make(getActivity().getWindow().getDecorView().getRootView(), R.string.perfil_modificado_ok, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.orange_dark));


                //View para introducir margen por encima del BottomBar
                View snackBarView = snackbar.getView();
                snackBarView.setTranslationY(-(convertDpToPixel(56, getActivity())));
                snackbar.show();

                listener.abrirConfiguracion();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_ADJ && resultCode == Activity.RESULT_OK) {
            selectedUri = data.getData();
            Glide.with(this).load(selectedUri).circleCrop()
                    .into(imageView);
        }
    }


    //    @Override
//    public void onResume() {
//        super.onResume();
//        addListener();
//    }

    private void addListener() {
        if (vel == null) {
            vel = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    usuLoged = dataSnapshot.getValue(Usuario.class);

                    cargarDatosUsuario();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().getWindow().getDecorView().getRootView(), R.string.error_carga_datos, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.orange_dark));
                    snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                    snackbar.setAnchorView(R.id.bottomNavigationBar);
                    snackbar.show();
                }
            };
            dbRef.child(user.getUid()).addValueEventListener(vel);
        }
    }

    private void cargarDatosUsuario() {

        removeListener();

        Glide.with(this)
                .load(usuLoged.getUrlFoto())
                .placeholder(null)
                .circleCrop()
                .into(imageView);

        etNom.setText(usuLoged.getNombre());
        etEmail.setText(usuLoged.getEmail());
        etPw.setText(usuLoged.getPassword());
        etDirec.setText(usuLoged.getDireccion());

    }

    @Override
    public void onPause() {
        super.onPause();
        removeListener();
    }

    private void removeListener() {
        if (vel != null) {
            dbRef.removeEventListener(vel);
            vel = null;
        }

    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CuentaListener) {
            listener = (CuentaListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}