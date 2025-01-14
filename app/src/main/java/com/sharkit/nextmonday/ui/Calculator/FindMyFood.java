package com.sharkit.nextmonday.ui.Calculator;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sharkit.nextmonday.MySQL.DataBasePFC;
import com.sharkit.nextmonday.MySQL.FavoriteFood;
import com.sharkit.nextmonday.MySQL.LinkRation;
import com.sharkit.nextmonday.R;
import com.sharkit.nextmonday.variables.DataPFC;
import com.sharkit.nextmonday.variables.LocalDataPFC;
import com.sharkit.nextmonday.variables.MealData;
import com.sharkit.nextmonday.variables.PFC_today;
import com.sharkit.nextmonday.variables.UserMeal;


import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;



public class FindMyFood extends Fragment {


    TextView potassium, salt, calcium, cellulose, watter, casein_protein, agg_protein, soy_protein, whey_protein,
            protein, complex_carbohydrate, simple_carbohydrate, carbohydrate, epa, dha, ala,
            omega3, omega6, omega9, trans_fat, saturated_fat, fat, name, portion, calorie,
            potassium_text, salt_text, calcium_text, cellulose_text, watter_text, casein_protein_text,
            agg_protein_text, soy_protein_text, whey_protein_text, protein_text, complex_carbohydrate_text,
            simple_carbohydrate_text, carbohydrate_text, epa_text, dha_text, ala_text, meals, num,
            omega3_text, omega6_text, omega9_text, trans_fat_text, saturated_fat_text, fat_text, calorie_text;

    Button change_food, save;

    SwitchCompat weight;

    Spinner spinner;
    LinkRation linkRation;
    SQLiteDatabase sdl;

    EditText number;
    DataBasePFC dataBasePFC;
    FavoriteFood favoriteFood;
    SQLiteDatabase fdb, ddb;

    ImageView favorite;

    Cursor query;
    List<String> meal = new ArrayList<>();
    ArrayList<Object> count;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference docRef = db.collection("DB Product");

    FirebaseDatabase fb_db = FirebaseDatabase.getInstance();
    DatabaseReference users = fb_db.getReference("Users/" + mAuth.getCurrentUser().getUid() + "/Setting/Calculator/Meal");


    final String TAG = "qwerty";

    CollectionReference collRef = db.collection("Users/" + mAuth.getCurrentUser().getUid() + "/FavoriteMeal");

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.calculator_find_my_food, container, false);
        FindView(root);

        dataBasePFC = new DataBasePFC(getApplicationContext());
        ddb = dataBasePFC.getReadableDatabase();
        dataBasePFC.onCreate(ddb);
        favoriteFood = new FavoriteFood(getApplicationContext());
        fdb = favoriteFood.getReadableDatabase();
        favoriteFood.onCreate(fdb);
        portion.setText(LocalDataPFC.getPortion() + " гр");
        num.setText(" Грамм");
        Adaptive();

        ReturnNumber();

        if (isExistSQLiteFavorite()){
            favorite.setImageResource(R.drawable.favorite_minus);
        }else{
            favorite.setImageResource(R.drawable.favorit_plus);
        }
        if (PFC_today.getPage().equals("MainMenu.LocalSQLite")) {
            WriteListFromSQL(PFC_today.getBar_code());
        }
        if (PFC_today.getPage().equals("Update")){
            name.setText(LocalDataPFC.getName());
            number.setText(String.format("%.0f" , LocalDataPFC.getNumber()));
            WeightProductGram(number.getText().toString());
            ShowView();
        }else {
            WriteList();
        }


        try {

            if (!PFC_today.getMealName().equals(null)) {
                spinner.setVisibility(View.GONE);
                meals.setVisibility(View.VISIBLE);
                meals.setText(PFC_today.getPage());
            }
        }catch (NullPointerException e){
            spinner.setVisibility(View.VISIBLE);
            meals.setVisibility(View.GONE);
        }


        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExistSQLiteFavorite()){
                    favorite.setImageResource(R.drawable.favorit_plus);
                    DropTargetFromSQLite();
                }else{
                    favorite.setImageResource(R.drawable.favorite_minus);
                    WriteFavoriteFood();
                }
            }
        });

        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (weight.isChecked()) {
                    WeightProductPortion(String.valueOf(s));

                }else{

                    WeightProductGram(String.valueOf(s));
                }
            }
        });



        weight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    WeightProductPortion(String.valueOf(1));
                    num.setText(" Порций");
                }else{
                    num.setText(" Грамм");
                    WeightProductGram(String.valueOf(1));
                }
            }
        });

        change_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PFC_today.setPage("Find_my_food");
                NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                navController.navigate(R.id.nav_cal_change_food);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(number.getText())){
                    Toast.makeText(getContext(),"Введите корректное количество грамм или порций", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (PFC_today.getPage().equals("Update")){
                    FoodUpdate();
                    return;
                }
                NewListFoodUsers();
            }
        });

        return root;
    }

    private void Adaptive() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int h = metrics.heightPixels;
        int w = metrics.widthPixels;
        int w_e = (int)(w / 1.6);
        LinearLayout.LayoutParams edit_params = new LinearLayout.LayoutParams(w_e, h/17 );
        edit_params.setMargins(h/32,h/82, h/32,h/82);

        number.setLayoutParams(edit_params);

        LinearLayout.LayoutParams but_params = new LinearLayout.LayoutParams(-1, h/17);
        but_params.setMargins(h/21,h/84, h/21, h/84);
        meals.setLayoutParams(but_params);
        save.setLayoutParams(but_params);
        change_food.setPadding(0,0,0,0);
        change_food.setLayoutParams(but_params);
        int w_n = (int)(w/1.2);
         LinearLayout.LayoutParams name_params = new LinearLayout.LayoutParams(w_n  , -2);
         name_params.setMargins(h/84, h/84,h/84,h/84);

         name.setLayoutParams(name_params);



    }

    private void DropTargetFromSQLite() {
        fdb = favoriteFood.getReadableDatabase();
        fdb.execSQL("DELETE FROM " + favoriteFood.TABLE + " WHERE " + favoriteFood.COLUMN_ID + " = '" + mAuth.getCurrentUser().getUid() +
                "' AND " + favoriteFood.COLUMN_BAR_CODE + " = '" + PFC_today.getBar_code() + "'");
    }

    private boolean isExistSQLiteFavorite() {
        query = fdb.rawQuery("SELECT * FROM " + favoriteFood.TABLE + " WHERE " + favoriteFood.COLUMN_ID + " = '" + mAuth.getCurrentUser().getUid() +
                "' AND " + favoriteFood.COLUMN_BAR_CODE + " = '" + PFC_today.getBar_code() + "'",null);
        return query.moveToNext();
    }

    private void WriteToMap() {
        count = new ArrayList<>();
        collRef.document("Link")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> l = documentSnapshot.getData();

                        try {
                            for (int i = 0; i < l.size(); i++) {
                                count.add(l.get(String.valueOf(i)));
                            }

                        }catch (NullPointerException e){
                        }
                    }
                });

    }


    @SuppressLint("SetTextI18n")
    private void WeightProductGram(String a) {

        try {
            portion.setText(a  + " гр");
            potassium.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getPotassium()) /
                    Float.parseFloat(LocalDataPFC.getPortion().trim()) * Float.parseFloat(a)) + " мг");
            salt.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getSalt()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            calcium.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getCalcium()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " мг");
            cellulose.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getCellulose()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            watter.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getWatter()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " мл");
            casein_protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getCasein_protein()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            agg_protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getAgg_protein()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            soy_protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getSoy_protein()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            whey_protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getWhey_protein()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            protein.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(LocalDataPFC.getProtein()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            complex_carbohydrate.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getComplex_carbohydrate()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            simple_carbohydrate.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getSimple_carbohydrates()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            carbohydrate.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getCarbohydrate()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            epa.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getEpa()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " мг");
            dha.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getDha()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " мг");
            ala.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getAla()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " мг");
            omega3.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getOmega_3()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " мг");
            omega6.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getOmega_6()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " мг");
            omega9.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getOmega_9()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " мг");
            trans_fat.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getTrans_fat()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            saturated_fat.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getSaturated_fat()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            fat.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(LocalDataPFC.getFat()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " гр");
            calorie.setText(String.format(Locale.ROOT,"%.0f", Float.parseFloat(LocalDataPFC.getCalorie()) /
                    Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(a)) + " Ккал");

        }catch (NumberFormatException e){}

    }

    @SuppressLint("SetTextI18n")
    private void WeightProductPortion(String a) {

       try {
          portion.setText(String.valueOf(Integer.parseInt(a) * Integer.parseInt(LocalDataPFC.getPortion())) + " гр");
          potassium.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getPotassium())) + " мг");
          salt.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getSalt())) + " гр");
          calcium.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getCalcium())) + " мг");
          cellulose.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getCellulose())) + " гр");
          watter.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getWatter())));
          casein_protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getCasein_protein())) + " гр");
          agg_protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getAgg_protein())) + " гр");
          soy_protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getSoy_protein())) + " гр");
          whey_protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getWhey_protein())) + " гр");
          protein.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getProtein())) + " гр");
          complex_carbohydrate.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getComplex_carbohydrate())) + " гр");
          simple_carbohydrate.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getSimple_carbohydrates())) + " гр");
          carbohydrate.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getCarbohydrate())) + " гр");
          epa.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getEpa())) + " мг");
          dha.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getDha())) + " мг");
          ala.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getAla())) + " мг");
          omega3.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getOmega_3())) + " мг");
          omega6.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getOmega_6())) + " мг");
          omega9.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getOmega_9())) + " мг");
          trans_fat.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getTrans_fat())) + " гр");
          saturated_fat.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getSaturated_fat())) + " гр");
          fat.setText(String.format(Locale.ROOT,"%.2f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getFat())) + " гр");
          calorie.setText(String.format(Locale.ROOT,"%.0f", Float.parseFloat(a) * Float.parseFloat(LocalDataPFC.getCalorie())) + " Ккал");

       }catch (NumberFormatException e){}

    }

    public void ReturnNumber(){
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MealList(snapshot.getChildrenCount());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void MealList(long a) {
        for (long i = 0; i < a; i++) {
            users.child(i+"").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        String k = snapshot.getValue(String.class);
                        meal.add(k);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_item, meal);
                        adapter.setDropDownViewResource(R.layout.smart_material_spinner_dropdown_item_layout);
                        adapter.notifyDataSetChanged();
                        spinner.setAdapter(adapter);
                    } catch (NullPointerException e) {
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }


    private void NewListFoodUsers() {
        Calendar calendar = Calendar.getInstance();
        long date  = calendar.getTimeInMillis();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String date_name = dateFormat.format(date);
        MealData mealData = new MealData();


        CollectionReference collRef = db.collection("Users/" +mAuth.getCurrentUser().getUid() + "/Meal");
        if (weight.isChecked()) {
            mealData.setNumber(Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(number.getText().toString()));
        }else{
            mealData.setNumber(Integer.parseInt(number.getText().toString()));
        }
        mealData.setDate_millis(date);
        mealData.setDate(date_name);
        mealData.setCode(PFC_today.getBar_code());
        if (!PFC_today.getPage().equals("")){
            mealData.setMeal(PFC_today.getPage());
        }else
        mealData.setMeal(spinner.getSelectedItem().toString());

        collRef.document(date+"").set(mealData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                        SynchronizedToSQL();
                        Toast.makeText(getContext(), "Продукт добавлен", Toast.LENGTH_SHORT).show();
                        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.nav_cal_ration);
            }
        });
    }
    private void WriteFavoriteFood(){
        fdb = favoriteFood.getReadableDatabase();
            try {
                fdb.execSQL("INSERT INTO " + favoriteFood.TABLE + " VALUES ('" + mAuth.getCurrentUser().getUid() + "','" +
                        name.getText().toString() + "','" +
                        LocalDataPFC.getPortion() + "','" +
                        LocalDataPFC.getCalorie() + "','" +
                        LocalDataPFC.getProtein() + "','" +
                        LocalDataPFC.getWhey_protein() + "','" +
                        LocalDataPFC.getSoy_protein() + "','" +
                        LocalDataPFC.getCasein_protein() + "','" +
                        LocalDataPFC.getAgg_protein() + "','" +
                        LocalDataPFC.getCarbohydrate() + "','" +
                        LocalDataPFC.getComplex_carbohydrate() + "','" +
                        LocalDataPFC.getSimple_carbohydrates() + "','" +
                        LocalDataPFC.getFat() + "','" +
                        LocalDataPFC.getSaturated_fat() + "','" +
                        LocalDataPFC.getTrans_fat() + "','" +
                        LocalDataPFC.getOmega_9() + "','" +
                        LocalDataPFC.getOmega_6() + "','" +
                        LocalDataPFC.getOmega_3() + "','" +
                        LocalDataPFC.getAla() + "','" +
                        LocalDataPFC.getDha() + "','" +
                        LocalDataPFC.getEpa() + "','" +
                        LocalDataPFC.getCellulose() + "','" +
                        LocalDataPFC.getSalt() + "','" +
                        LocalDataPFC.getWatter() + "','" +
                        LocalDataPFC.getCalcium() + "','" +
                        LocalDataPFC.getPotassium() + "','" +
                        PFC_today.getBar_code() + "');");
            } catch (SQLiteException e) {
            }
    }

    private void SynchronizedToSQL() {
        ddb = dataBasePFC.getReadableDatabase();


        if (isExistSQLite(PFC_today.getBar_code())) {
            try {
                ddb.execSQL("INSERT INTO " + dataBasePFC.TABLE + " VALUES ('" + mAuth.getCurrentUser().getUid() + "','" +
                        name.getText().toString() + "','" +
                        LocalDataPFC.getPortion() + "','" +
                        LocalDataPFC.getCalorie() + "','" +
                        LocalDataPFC.getProtein() + "','" +
                        LocalDataPFC.getWhey_protein() + "','" +
                        LocalDataPFC.getSoy_protein() + "','" +
                        LocalDataPFC.getCasein_protein() + "','" +
                        LocalDataPFC.getAgg_protein() + "','" +
                        LocalDataPFC.getCarbohydrate() + "','" +
                        LocalDataPFC.getComplex_carbohydrate() + "','" +
                        LocalDataPFC.getSimple_carbohydrates() + "','" +
                        LocalDataPFC.getFat() + "','" +
                        LocalDataPFC.getSaturated_fat() + "','" +
                        LocalDataPFC.getTrans_fat() + "','" +
                        LocalDataPFC.getOmega_9() + "','" +
                        LocalDataPFC.getOmega_6() + "','" +
                        LocalDataPFC.getOmega_3() + "','" +
                        LocalDataPFC.getAla() + "','" +
                        LocalDataPFC.getDha() + "','" +
                        LocalDataPFC.getEpa() + "','" +
                        LocalDataPFC.getCellulose() + "','" +
                        LocalDataPFC.getSalt() + "','" +
                        LocalDataPFC.getWatter() + "','" +
                        LocalDataPFC.getCalcium() + "','" +
                        LocalDataPFC.getPotassium() + "','" +
                        PFC_today.getBar_code() + "');");
            } catch (SQLiteException e) {

            }
        }
    }
    private boolean isExistSQLite(String code) {
        ddb = dataBasePFC.getReadableDatabase();


        query = ddb.rawQuery("SELECT * FROM " + dataBasePFC.TABLE + " WHERE " + dataBasePFC.COLUMN_ID + " = '" + mAuth.getCurrentUser().getUid() + "' AND "
                + dataBasePFC.COLUMN_BAR_CODE + " = '" + code + "'", null);

        return (!query.moveToNext());


    }

    @SuppressLint("SetTextI18n")
    private void WriteListNutrition() {
        potassium.setText(LocalDataPFC.getPotassium() + " мг");
        salt.setText(LocalDataPFC.getSalt() + " гр");
        calcium.setText(LocalDataPFC.getCalcium() + " мг");
        cellulose.setText(LocalDataPFC.getCellulose() + " гр");
        watter.setText(LocalDataPFC.getWatter() + " мл");
        casein_protein.setText(LocalDataPFC.getCasein_protein() + " гр");
        agg_protein.setText(LocalDataPFC.getAgg_protein() + " гр");
        soy_protein.setText(LocalDataPFC.getSoy_protein() + " гр");
        whey_protein.setText(LocalDataPFC.getWhey_protein() + " гр");
        protein.setText(LocalDataPFC.getProtein() + " гр");
        complex_carbohydrate.setText(LocalDataPFC.getComplex_carbohydrate() + " гр");
        simple_carbohydrate.setText(LocalDataPFC.getSimple_carbohydrates() + " гр");
        carbohydrate.setText(LocalDataPFC.getCarbohydrate() + " гр");
        epa.setText(LocalDataPFC.getEpa() + " мг");
        dha.setText(LocalDataPFC.getDha() + " мг");
        ala.setText(LocalDataPFC.getAla() + " мг");
        omega3.setText(LocalDataPFC.getOmega_3() + " мг");
        omega6.setText(LocalDataPFC.getOmega_6() + " мг");
        omega9.setText(LocalDataPFC.getOmega_9() + " мг");
        trans_fat.setText(LocalDataPFC.getTrans_fat() + " гр");
        saturated_fat.setText(LocalDataPFC.getSaturated_fat() + " гр");
        fat.setText(LocalDataPFC.getFat() + " гр");
        calorie.setText(LocalDataPFC.getCalorie() + " Ккал");
        name.setText(LocalDataPFC.getName());

        ShowView();
    }

    private void WriteListFromSQL(String code) {
        ddb = dataBasePFC.getReadableDatabase();

        query = ddb.rawQuery("SELECT * FROM " + dataBasePFC.TABLE + " WHERE " + dataBasePFC.COLUMN_ID + " = '" + mAuth.getCurrentUser().getUid()
                + "' AND " + dataBasePFC.COLUMN_BAR_CODE + " = '" + code + "'",null);

        while (query.moveToNext()){
            LocalDataPFC.setPotassium(query.getString(25));
            LocalDataPFC.setSalt(query.getString(22));
            LocalDataPFC.setCalcium(query.getString(24));
            LocalDataPFC.setCellulose(query.getString(21));
            LocalDataPFC.setWatter(query.getString(23));
            LocalDataPFC.setCasein_protein(query.getString(7));
            LocalDataPFC.setAgg_protein(query.getString(8));
            LocalDataPFC.setSoy_protein(query.getString(6));
            LocalDataPFC.setWhey_protein(query.getString(5));
            LocalDataPFC.setProtein(query.getString(4));
            LocalDataPFC.setComplex_carbohydrate(query.getString(10));
            LocalDataPFC.setSimple_carbohydrates(query.getString(11));
            LocalDataPFC.setCarbohydrate(query.getString(9));
            LocalDataPFC.setEpa(query.getString(20));
            LocalDataPFC.setDha(query.getString(19));
            LocalDataPFC.setAla(query.getString(18));
            LocalDataPFC.setOmega_3(query.getString(17));
            LocalDataPFC.setOmega_6(query.getString(16));
            LocalDataPFC.setOmega_9(query.getString(15));
            LocalDataPFC.setTrans_fat(query.getString(14));
            LocalDataPFC.setSaturated_fat(query.getString(13));
            LocalDataPFC.setFat(query.getString(12));
            LocalDataPFC.setCalorie(query.getString(3));
            LocalDataPFC.setName(query.getString(1));
            LocalDataPFC.setPortion(query.getString(2));
        }
        WriteListNutrition();

    }


    private void WriteList() {
        docRef.whereEqualTo("bar_code", PFC_today.getBar_code())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    DataPFC dataPFC = documentSnapshot.toObject(DataPFC.class);

                    LocalDataPFC.setPotassium(dataPFC.getPotassium());
                    LocalDataPFC.setSalt(dataPFC.getSalt());
                    LocalDataPFC.setCalcium(dataPFC.getCalcium());
                    LocalDataPFC.setCellulose(dataPFC.getCellulose());
                    LocalDataPFC.setWatter(dataPFC.getWatter());
                    LocalDataPFC.setCasein_protein(dataPFC.getCasein_protein());
                    LocalDataPFC.setAgg_protein(dataPFC.getAgg_protein());
                    LocalDataPFC.setSoy_protein(dataPFC.getSoy_protein());
                    LocalDataPFC.setWhey_protein(dataPFC.getWhey_protein());
                    LocalDataPFC.setProtein(dataPFC.getProtein());
                    LocalDataPFC.setComplex_carbohydrate(dataPFC.getComplex_carbohydrate());
                    LocalDataPFC.setSimple_carbohydrates(dataPFC.getSimple_carbohydrates());
                    LocalDataPFC.setCarbohydrate(dataPFC.getCarbohydrate());
                    LocalDataPFC.setEpa(dataPFC.getEpa());
                    LocalDataPFC.setDha(dataPFC.getDha());
                    LocalDataPFC.setAla(dataPFC.getAla());
                    LocalDataPFC.setOmega_3(dataPFC.getOmega_3());
                    LocalDataPFC.setOmega_6(dataPFC.getOmega_6());
                    LocalDataPFC.setOmega_9(dataPFC.getOmega_9());
                    LocalDataPFC.setTrans_fat(dataPFC.getTrans_fat());
                    LocalDataPFC.setSaturated_fat(dataPFC.getSaturated_fat());
                    LocalDataPFC.setFat(dataPFC.getFat());
                    LocalDataPFC.setCalorie(dataPFC.getCalorie());
                    LocalDataPFC.setName(dataPFC.getName());
                    LocalDataPFC.setPortion(dataPFC.getPortion());


                }
                WriteListNutrition();

                ShowView();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "feil");
            }
        });
    }


    private void ShowView() {

        if (TextUtils.isEmpty(watter.getText()) || watter.getText().equals(String.valueOf(0) + " мл") ||
                watter.getText().equals(String.format( Locale.ROOT,"%.2f",0.00)  + " мл")){
            watter.setVisibility(View.GONE);
            watter_text.setVisibility(View.GONE); }
        if (TextUtils.isEmpty(cellulose.getText()) || cellulose.getText().equals(String.valueOf(0) + " гр") ||
                cellulose.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            cellulose.setVisibility(View.GONE);
            cellulose_text.setVisibility(View.GONE); }
        if (TextUtils.isEmpty(potassium.getText()) || potassium.getText().equals(String.valueOf(0) + " мг") ||
                potassium.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " мг")){
            potassium.setVisibility(View.GONE);
            potassium_text.setVisibility(View.GONE); }
        if (TextUtils.isEmpty(salt.getText()) || salt.getText().equals(String.valueOf(0) + " гр") ||
                salt.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            salt.setVisibility(View.GONE);
            salt_text.setVisibility(View.GONE);  }
        if (TextUtils.isEmpty(calcium.getText()) || calcium.getText().equals(String.valueOf(0) + " мг") ||
                calcium.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " мг")){
            calcium.setVisibility(View.GONE);
            calcium_text.setVisibility(View.GONE);    }
        if (TextUtils.isEmpty(casein_protein.getText()) || casein_protein.getText().equals(String.valueOf(0) + " гр") ||
                casein_protein.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            casein_protein.setVisibility(View.GONE);
            casein_protein_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(agg_protein.getText()) || agg_protein.getText().equals(String.valueOf(0) + " гр") ||
                agg_protein.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            agg_protein.setVisibility(View.GONE);
            agg_protein_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(soy_protein.getText()) || soy_protein.getText().equals(String.valueOf(0) + " гр") ||
                soy_protein.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            soy_protein.setVisibility(View.GONE);
            soy_protein_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(whey_protein.getText()) || whey_protein.getText().equals(String.valueOf(0) + " гр") ||
                whey_protein.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            whey_protein.setVisibility(View.GONE);
            whey_protein_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(protein.getText()) || protein.getText().equals(String.valueOf(0) + " гр") ||
                protein.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            protein.setVisibility(View.GONE);
            protein_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(complex_carbohydrate.getText()) || complex_carbohydrate.getText().equals(String.valueOf(0) + " гр")
                || complex_carbohydrate.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            complex_carbohydrate.setVisibility(View.GONE);
            complex_carbohydrate_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(simple_carbohydrate.getText()) || simple_carbohydrate.getText().equals(String.valueOf(0) + " гр")
                || simple_carbohydrate.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            simple_carbohydrate.setVisibility(View.GONE);
            simple_carbohydrate_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(carbohydrate.getText()) || carbohydrate.getText().equals(String.valueOf(0) + " гр") ||
                carbohydrate.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            carbohydrate.setVisibility(View.GONE);
            carbohydrate_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(epa.getText()) || epa.getText().equals(String.valueOf(0) + " мг") ||
                epa.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " мг")){
            epa.setVisibility(View.GONE);
            epa_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(dha.getText()) || dha.getText().equals(String.valueOf(0) + " мг") ||
                dha.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " мг")){
            dha_text.setVisibility(View.GONE);
            dha.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(ala.getText()) || ala.getText().equals(String.valueOf(0) + " мг") ||
                ala.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " мг")){
            ala.setVisibility(View.GONE);
            ala_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(omega3.getText()) || omega3.getText().equals(String.valueOf(0) + " мг") ||
                omega3.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " мг")){
            omega3.setVisibility(View.GONE);
            omega3_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(omega6.getText()) || omega6.getText().equals(String.valueOf(0) + " мг") ||
                omega6.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " мг")){
            omega6.setVisibility(View.GONE);
            omega6_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(omega9.getText()) || omega9.getText().equals(String.valueOf(0) + " мг") ||
                omega9.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " мг")){
            omega9.setVisibility(View.GONE);
            omega9_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(saturated_fat.getText()) || saturated_fat.getText().equals(String.valueOf(0) + " гр") ||
                saturated_fat.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            saturated_fat.setVisibility(View.GONE);
            saturated_fat_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(trans_fat.getText()) || trans_fat.getText().equals(String.valueOf(0) + " гр") ||
                trans_fat.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            trans_fat.setVisibility(View.GONE);
            trans_fat_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(fat.getText()) || fat.getText().equals(String.valueOf(0) + " гр") ||
                fat.getText().equals(String.format( Locale.ROOT,"%.2f",0.00) + " гр")){
            fat.setVisibility(View.GONE);
            fat_text.setVisibility(View.GONE);        }
        if (TextUtils.isEmpty(calorie.getText()) || calorie.getText().equals(String.valueOf(0)) ||
                calorie.getText().equals(String.format( Locale.ROOT,"%.2f",0.00))){
            calorie.setVisibility(View.GONE);
            calorie_text.setVisibility(View.GONE);        }

    }

    private void FindView(View root) {
        num = root.findViewById(R.id.num_xml);

        portion = root.findViewById(R.id.gram);
        favorite = root.findViewById(R.id.favorite_xml);
        meals = root.findViewById(R.id.meal);
        spinner = root.findViewById(R.id.spinner_xml);
        number = root.findViewById(R.id.number_xml);
        weight = root.findViewById(R.id.weight_xml);
        save = root.findViewById(R.id.save_xml);
        potassium = root.findViewById(R.id.potassium);
        salt = root.findViewById(R.id.salt);
        calcium = root.findViewById(R.id.calcium);
        cellulose = root.findViewById(R.id.cellulose);
        watter = root.findViewById(R.id.watter);
        casein_protein = root.findViewById(R.id.casein_protein);
        agg_protein = root.findViewById(R.id.agg_protein);
        soy_protein = root.findViewById(R.id.soy_protein);
        whey_protein = root.findViewById(R.id.whey_protein);
        protein = root.findViewById(R.id.protein);
        complex_carbohydrate = root.findViewById(R.id.complex_carbohydrate);
        simple_carbohydrate = root.findViewById(R.id.simple_carbohydrates);
        carbohydrate = root.findViewById(R.id.carbohydrate);
        epa = root.findViewById(R.id.epa);
        dha = root.findViewById(R.id.dha);
        ala = root.findViewById(R.id.ala);
        omega3 = root.findViewById(R.id.omega3);
        omega6 = root.findViewById(R.id.omega6);
        omega9 = root.findViewById(R.id.omega9);
        trans_fat = root.findViewById(R.id.trans_fat);
        saturated_fat = root.findViewById(R.id.saturated_fat);
        fat = root.findViewById(R.id.fat);
        calorie = root.findViewById(R.id.calorie);
        name = root.findViewById(R.id.name);


        potassium_text = root.findViewById(R.id.potassium_text);
        salt_text = root.findViewById(R.id.salt_text);
        calcium_text = root.findViewById(R.id.calcium_text);
        cellulose_text = root.findViewById(R.id.cellulose_text);
        watter_text = root.findViewById(R.id.watter_text);
        casein_protein_text = root.findViewById(R.id.casein_protein_text);
        agg_protein_text = root.findViewById(R.id.agg_protein_text);
        soy_protein_text = root.findViewById(R.id.soy_protein_text);
        whey_protein_text = root.findViewById(R.id.whey_protein_text);
        protein_text = root.findViewById(R.id.protein_text);
        complex_carbohydrate_text = root.findViewById(R.id.complex_carbohydrate_text);
        simple_carbohydrate_text = root.findViewById(R.id.simple_carbohydrates_text);
        carbohydrate_text = root.findViewById(R.id.carbohydrate_text);
        epa_text = root.findViewById(R.id.epa_text);
        dha_text = root.findViewById(R.id.dha_text);
        ala_text = root.findViewById(R.id.ala_text);
        omega3_text = root.findViewById(R.id.omega3_text);
        omega6_text = root.findViewById(R.id.omega6_text);
        omega9_text = root.findViewById(R.id.omega9_text);
        trans_fat_text = root.findViewById(R.id.trans_fat_text);
        saturated_fat_text = root.findViewById(R.id.saturated_fat_text);
        fat_text = root.findViewById(R.id.fat_text);
        calorie_text = root.findViewById(R.id.calorie_text);

        change_food = root.findViewById(R.id.change_food);

    }

    public void FoodUpdate() {

        WriteListNutrition();

        linkRation = new LinkRation(getApplicationContext());
        sdl = linkRation.getReadableDatabase();
        linkRation.onCreate(sdl);

        CollectionReference docRef1 = db.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Meal");

        MealData mealData = new MealData();

        mealData.setMeal(spinner.getSelectedItem().toString());
        if (weight.isChecked()) {
            mealData.setNumber(Float.parseFloat(LocalDataPFC.getPortion()) * Float.parseFloat(number.getText().toString()));
        }else{
            mealData.setNumber(Integer.parseInt(number.getText().toString()));
        }

        sdl.execSQL("UPDATE " + linkRation.TABLE + " SET " +
                linkRation.COLUMN_NAME + " = '" + name.getText().toString() + "'," +
                linkRation.COLUMN_NUMBER + " = '" + number.getText().toString() + "'," +
                linkRation.COLUMN_MEAL + " = '" + spinner.getSelectedItem() + "' WHERE " +
                linkRation.COLUMN_DATE_MILLIS + " = '" + LocalDataPFC.getDate_millis() + "'");

        docRef1.document(String.valueOf(LocalDataPFC.getDate_millis())).update("number",mealData.getNumber());
        docRef1.document(String.valueOf(LocalDataPFC.getDate_millis())).update("meal",spinner.getSelectedItem()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Продукт обновлен", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_cal_ration);
            }
        });



    }
}