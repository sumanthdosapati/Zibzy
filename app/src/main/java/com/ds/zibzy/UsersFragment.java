package com.ds.zibzy;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ds.zibzy.adapters.AdapterUsers;
import com.ds.zibzy.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;
    FirebaseAuth firebaseAuth;


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        firebaseAuth= FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.users_RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        usersList = new ArrayList<>();

        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    if(!modelUsers.getUid().equals(fUser.getUid())){
                        usersList.add(modelUsers);
                    }

                    adapterUsers = new AdapterUsers(getActivity(),usersList);
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    if(!modelUsers.getUid().equals(fUser.getUid())){

                        if(modelUsers.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase()) ){
                            usersList.add(modelUsers);

                        }

                    }

                    adapterUsers = new AdapterUsers(getActivity(),usersList);
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null){
            //  mProfileTv.setText(user.getEmail());
        }
        else{
            Intent ma= new Intent(getActivity(),MainActivity.class);
            startActivity(ma);
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }
                else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }
                else {
                    getAllUsers();
                }
                return false;
            }
        });

         super.onCreateOptionsMenu(menu,inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);

    }

}
