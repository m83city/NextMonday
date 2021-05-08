package com.sharkit.nextmonday.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sharkit.nextmonday.MySQL.DataBasePFC;
import com.sharkit.nextmonday.MySQL.LinkRation;
import com.sharkit.nextmonday.R;
import com.sharkit.nextmonday.Users.DayOfWeek;
import com.sharkit.nextmonday.variables.DataPFC;
import com.sharkit.nextmonday.variables.MealData;
import com.sharkit.nextmonday.variables.PFC_today;
import com.sharkit.nextmonday.variables.UserMeal;

import java.security.acl.LastOwnerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;


public class RationExpList extends BaseExpandableListAdapter {
    private ArrayList<ArrayList<UserMeal>> mGroups;
    private ArrayList<String> mMeal;
    private Context mContext;

    DataPFC dataPFC;
    final String TAG = "qwerty";

    TextView calorie, weight, protein, carbohydrate, name_food, fat,
    all_calorie, all_protein, all_carbohydrate, all_fat, meal;
    ImageView plus;

    LinkRation linkRation;

    SQLiteDatabase sdb;
    Cursor query;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference colRef;
    ArrayList<UserMeal> list;

    public RationExpList(ArrayList<String> mMeal , ArrayList<ArrayList<UserMeal>> mGroups, Context mContext) {
        this.mGroups = mGroups;
        this.mContext = mContext;
        this.mMeal = mMeal;
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroups.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.calculator_food_parent_list, null);
        }
        FindView(convertView);

        UserMeal userMeal = new UserMeal();

        userMeal.setCalorie("0");
        userMeal.setProtein("0");
        userMeal.setWatter("0");
        userMeal.setFat("0");
        userMeal.setCarbohydrate("0");
        userMeal.setWhey_protein("0");
        userMeal.setSoy_protein("0");
        userMeal.setAgg_protein("0");
        userMeal.setCasein_protein("0");
        userMeal.setSaturated_fat("0");
        userMeal.setTrans_fat("0");
        userMeal.setOmega_9("0");
        userMeal.setOmega_6("0");
        userMeal.setOmega_3("0");
        userMeal.setAla("0");
        userMeal.setDha("0");
        userMeal.setEpa("0");
        userMeal.setSimple_carbohydrates("0");
        userMeal.setComplex_carbohydrate("0");
        userMeal.setCellulose("0");
        userMeal.setSalt("0");
        userMeal.setCalcium("0");
        userMeal.setPotassium("0");
        list = new ArrayList<>();

        for (int i = 0; i < mGroups.get(groupPosition).size(); i++){

            SumEatNutrition(i,groupPosition,userMeal);

        }
        all_calorie.setText(String.format("%.0f",Float.parseFloat(userMeal.getCalorie())));
        all_fat.setText(String.format("%.1f",Float.parseFloat(userMeal.getFat())));
        all_protein.setText(String.format("%.1f",Float.parseFloat(userMeal.getProtein())));
        all_carbohydrate.setText(String.format("%.1f",Float.parseFloat(userMeal.getCarbohydrate())));

        meal.setText(mMeal.get(groupPosition));

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PFC_today.setPage("Plus");
                OnClickPlus(plus, groupPosition);
            }
        });

        return convertView;
    }

    public void OnClickPlus(View v, int position){
        NavController navController = Navigation.findNavController((Activity) mContext, R.id.nav_host_fragment);
        PFC_today.setPage(mMeal.get(position));
        navController.navigate(R.id.nav_cal_find_food_by_name);
    }
    @SuppressLint("DefaultLocale")
    private void SumEatNutrition(int i, int groupPosition, UserMeal userMeal) {

        try {
            userMeal.setCalorie(String.valueOf ( Float.parseFloat(userMeal.getCalorie()) +  (Float.parseFloat( mGroups.get(groupPosition).get(i).getCalorie()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber())));

            userMeal.setProtein(String.valueOf ( Float.parseFloat(userMeal.getProtein()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getProtein()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setWatter(String.valueOf (Float.parseFloat(userMeal.getWatter()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getWatter()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setFat(String.valueOf (Float.parseFloat(userMeal.getFat()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getFat()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setCarbohydrate(String.valueOf (Float.parseFloat(userMeal.getCarbohydrate()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getCarbohydrate()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setWhey_protein(String.valueOf (Float.parseFloat(userMeal.getWhey_protein()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getWhey_protein()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setSoy_protein(String.valueOf (Float.parseFloat(userMeal.getSoy_protein()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getSoy_protein()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setAgg_protein(String.valueOf (Float.parseFloat(userMeal.getAgg_protein()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getAgg_protein()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setCasein_protein(String.valueOf (Float.parseFloat(userMeal.getCasein_protein()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getCasein_protein()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setSaturated_fat(String.valueOf (Float.parseFloat(userMeal.getSaturated_fat()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getSaturated_fat()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setTrans_fat(String.valueOf (Float.parseFloat(userMeal.getTrans_fat()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getTrans_fat()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setOmega_9(String.valueOf (Float.parseFloat(userMeal.getOmega_9()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getOmega_9()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setOmega_6(String.valueOf (Float.parseFloat(userMeal.getOmega_6()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getOmega_6()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setOmega_3(String.valueOf (Float.parseFloat(userMeal.getOmega_3()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getOmega_3()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setAla(String.valueOf (Float.parseFloat(userMeal.getAla()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getAla()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setDha(String.valueOf (Float.parseFloat(userMeal.getDha()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getDha()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setEpa(String.valueOf (Float.parseFloat(userMeal.getEpa()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getEpa()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setSimple_carbohydrates(String.valueOf (Float.parseFloat(userMeal.getSimple_carbohydrates()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getSimple_carbohydrates()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setComplex_carbohydrate(String.valueOf (Float.parseFloat(userMeal.getComplex_carbohydrate()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getComplex_carbohydrate()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setCellulose(String.valueOf (Float.parseFloat(userMeal.getCellulose()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getCellulose()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setSalt(String.valueOf (Float.parseFloat(userMeal.getSalt()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getSalt()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setCalcium(String.valueOf (Float.parseFloat(userMeal.getCalcium()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getCalcium()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

            userMeal.setPotassium(String.valueOf (Float.parseFloat(userMeal.getPotassium()) +  Float.parseFloat( mGroups.get(groupPosition).get(i).getPotassium()) /
                    Float.parseFloat(mGroups.get(groupPosition).get(i).getPortion()) * mGroups.get(groupPosition).get(i).getNumber()));

        }catch (NumberFormatException e){

        }
    }



    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.calculator_food_item_list, null);
        }

        FindView(convertView);

        name_food.setText(mGroups.get(groupPosition).get(childPosition).getName());
        weight.setText(mGroups.get(groupPosition).get(childPosition).getNumber() + " gram");

        carbohydrate.setText("Carbohydrate: " + String.format("%.1f", Float.parseFloat( mGroups.get(groupPosition).get(childPosition).getCarbohydrate()) /
                Float.parseFloat(mGroups.get(groupPosition).get(childPosition).getPortion()) * mGroups.get(groupPosition).get(childPosition).getNumber()));

        protein.setText("Protein: " + String.format("%.1f", Float.parseFloat( mGroups.get(groupPosition).get(childPosition).getProtein()) /
                Float.parseFloat(mGroups.get(groupPosition).get(childPosition).getPortion()) * mGroups.get(groupPosition).get(childPosition).getNumber()));

        fat.setText("Fat: " + String.format("%.1f", Float.parseFloat( mGroups.get(groupPosition).get(childPosition).getFat()) /
                Float.parseFloat(mGroups.get(groupPosition).get(childPosition).getPortion()) * mGroups.get(groupPosition).get(childPosition).getNumber()));

        calorie.setText((String.format("%.0f", Float.parseFloat( mGroups.get(groupPosition).get(childPosition).getCalorie()) /
                Float.parseFloat(mGroups.get(groupPosition).get(childPosition).getPortion()) * mGroups.get(groupPosition).get(childPosition).getNumber())) + " калл");

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void FindView(View convertView) {

        fat = convertView.findViewById(R.id.fat);
        calorie = convertView.findViewById(R.id.calorie);
        carbohydrate = convertView.findViewById(R.id.carbohydrate);
        protein = convertView.findViewById(R.id.protein);
        name_food = convertView.findViewById(R.id.name_product);
        weight = convertView.findViewById(R.id.weight_food);
        plus = convertView.findViewById(R.id.plus);

        all_fat = convertView.findViewById(R.id.all_fat);
        all_protein = convertView.findViewById(R.id.all_protein);
        all_calorie = convertView.findViewById(R.id.all_calorie);
        all_carbohydrate = convertView.findViewById(R.id.all_carbohydrate);
        meal = convertView.findViewById(R.id.meal_name);

    }
}
