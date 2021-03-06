package com.dam.safebar.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dam.safebar.R;
import com.dam.safebar.adapters.InicioAdapter;
import com.dam.safebar.javabeans.Restaurante;
import com.dam.safebar.listeners.BuscarListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;


public class FiltrosFragment extends Fragment {

    RecyclerView rv;
    ArrayList<Restaurante> listaRestaurantes;
    BuscarListener listener;

    String nombreRest;

    EditText etNomRest;

    public FiltrosFragment() {
        // Required empty public constructor
    }


    public FiltrosFragment newInstance(ArrayList<Restaurante> listaRestaurantes) {
        FiltrosFragment fragment = new FiltrosFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("LISTA_RF", listaRestaurantes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listaRestaurantes = getArguments().getParcelableArrayList("LISTA_RF");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtros, container, false);

        etNomRest = view.findViewById(R.id.etEditFiltFragFil);
        TextInputLayout textLayout = view.findViewById(R.id.lEditFiltFragFil);


        rv = view.findViewById(R.id.rvFiltros);

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        InicioAdapter inicAdap = new InicioAdapter(listaRestaurantes);



        inicAdap.setListener(v -> {
            int i = rv.getChildAdapterPosition(v);
            Restaurante restaurante = listaRestaurantes.get(i);
            listener.abrirRestaurante(restaurante.getRestUID(), restaurante.getNombreRest());
        });

        rv.setAdapter(inicAdap);

        textLayout.setEndIconOnClickListener(v -> {
            nombreRest = etNomRest.getText().toString().trim();

            if (nombreRest.isEmpty()) {
                Snackbar snackbar = Snackbar
                        .make(getActivity().getWindow().getDecorView().getRootView(), R.string.debes_introducir_nombre, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.green_dark));
                snackbar.setAction(getResources().getString(R.string.aceptar), v1 -> snackbar.dismiss());
                snackbar.setAnchorView(R.id.bottomNavigationBar);
                snackbar.show();
            } else {
                listener.buscarRestaurantes(nombreRest);
            }
        });

        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BuscarListener) {
            listener = (BuscarListener) context;
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