package com.dam.safebar.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dam.safebar.R;
import com.dam.safebar.javabeans.Restaurante;
import com.dam.safebar.listeners.PerfilRestListener;
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


public class PerfilRestFragment extends Fragment {

    PerfilRestListener listener;

    FirebaseAuth fba;
    FirebaseUser user;
    DatabaseReference dbRef;
    StorageReference mFotoStorageRef;
    ValueEventListener vel;

    Restaurante restLoged;

    ImageView img;
    TextView tvNom;
    TextView tvEmail;
    TextView tvDirec;
    TextView tvTelef;
    TextView tvPrecio;
    TextView tvAforo;
    TextView tvDescrip;
    Button btnLO;

    public PerfilRestFragment() {
        // Required empty public constructor
    }


    public PerfilRestFragment newInstance() {
        PerfilRestFragment fragment = new PerfilRestFragment();
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
        View view = inflater.inflate(R.layout.fragment_perfil_rest, container, false);

        //BTN GUARDAR EN ACTIONBAR
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar();

        img = view.findViewById(R.id.imgPerfRestFrag);
        tvNom = view.findViewById(R.id.tvNombrePerfRestFrag);
        tvEmail = view.findViewById(R.id.tvEmailPerfRestFrag);
        tvDirec = view.findViewById(R.id.tvDirecPerfRestFrag);
        tvTelef = view.findViewById(R.id.tvTelefPerfRestFrag);
        tvPrecio = view.findViewById(R.id.tvPrecioPerfRestFrag);
        tvAforo = view.findViewById(R.id.tvAforoPerfRestFrag);
        tvDescrip = view.findViewById(R.id.tvDescripPerfRestFrag);
        btnLO = view.findViewById(R.id.btnLogOutPerfRestFrag);

        fba = FirebaseAuth.getInstance();
        user = fba.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("datos/restaurantes");
        mFotoStorageRef = FirebaseStorage.getInstance().getReference().child("fotosR");

        addListener();

//        btnEC.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                listener.abrirEditCuenta();
//
//            }
//        });

        btnLO.setOnClickListener(v -> {
            fba.signOut();
            listener.salir();
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.configuracion_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.itemEditarConfiguracion) {
            listener.abrirEditCuenta();
        }
        return super.onOptionsItemSelected(item);
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

                    restLoged = dataSnapshot.getValue(Restaurante.class);
                    cargarDatosUsuario();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar snackbar = Snackbar
                            .make(getActivity().getWindow().getDecorView().getRootView(), R.string.error_carga_datos, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.orange_dark));
                    snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                    snackbar.setAnchorView(R.id.bottomNavigationBarRest);
                    snackbar.show();
                }
            };
            dbRef.child(user.getUid()).addValueEventListener(vel);
        }
    }

    private void cargarDatosUsuario() {

        removeListener();

        if (getActivity() != null) {
            Fragment f = getActivity().getSupportFragmentManager().findFragmentById(R.id.flPerfilRest);
            if (f instanceof PerfilRestFragment) {
                Glide.with(img)
                        .load(restLoged.getUrlFoto())
                        .placeholder(null)
                        .into(img);
            }
        }





        tvNom.setText(restLoged.getNombreRest());
        tvEmail.setText(restLoged.getEmail());
        tvDirec.setText(restLoged.getDireccion());
        tvTelef.setText(restLoged.getTelefono());
        tvPrecio.setText(String.valueOf(restLoged.getPrecioMedio()));
        tvAforo.setText(String.valueOf(restLoged.getAforo()));
        tvDescrip.setText(restLoged.getDescripcion());


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
        if (context instanceof PerfilRestListener) {
            listener = (PerfilRestListener) context;
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