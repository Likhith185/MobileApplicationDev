package com.example.likhi.hwork08;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTrip extends AppCompatActivity {

    EditText editTrip, addDay;
    int check=0;
    Button addTrip;
    ProgressDialog pd;
    double latitute, longitude;
    ListView listViewPlaces;
    ListObject listObj=new ListObject();
    DatabaseReference mRootRef ;
    AutoCompleteTextView addCity;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    ArrayList<ListObject> placeNameList=new ArrayList<ListObject>();
    ArrayList<Places> arrayListPlacestoTrip=new ArrayList<Places>();
    ArrayList<Places> placesArrayList = new ArrayList<Places>();
    Trip trip=new Trip();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_add_trip);
        setTitle("Add Your Trip");


        addDay = findViewById(R.id.addDay);
        listViewPlaces=findViewById(R.id.placesList);
        editTrip=findViewById(R.id.addTrip);
        addDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH);
                int dd = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePicker = new DatePickerDialog(AddTrip.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String date = String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear + 1)
                                + "/" + String.valueOf(year);
                        addDay.setText(date);
                        trip.setDay(date);
                        trip.setTripName(editTrip.getText()+"");
                        trip.setTripId("id");
                    }
                }, yy, mm, dd);
                datePicker.show();
            }


        });


        addCity = findViewById(R.id.addCity);


        addCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("US").setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES).build();
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(typeFilter).build(AddTrip.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });


        listViewPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListObject listObj=placeNameList.get(position);
                placeNameList.add(position,listObj);
                Adapterclass adapterclass = new Adapterclass(AddTrip.this, placeNameList);
                listViewPlaces.setAdapter(adapterclass);

            }
        });
        addTrip=findViewById(R.id.btnAddTrip);
        addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(editTrip.getText().toString())){
                    editTrip.setError("Please Enter Some Name!");
                }
                else if(TextUtils.isEmpty(addDay.getText().toString())){
                    addDay.setError("Please Select a Date!");
                }
                else if(TextUtils.isEmpty(addCity.getText().toString())){
                    addCity.setError("Please Select a City!");
                }
                else if(check==0){
                    Toast.makeText(AddTrip.this,"Select atleast one Place!",Toast.LENGTH_SHORT).show();
                }
                else if (check < 16) {
                    for (ListObject listObj : placeNameList) {
                        if ((listObj.getStatus().equals("checked"))) {
                            int index = placeNameList.indexOf(listObj);
                            Places place = placesArrayList.get(index);
                            arrayListPlacestoTrip.add(place);
                        }
                    }
                    trip.setPlaces(arrayListPlacestoTrip);
                    writeNewUser(trip);
                    int resultCode = 1;
                    setResult(resultCode);
                    finish();
                }
                else
                    Toast.makeText(AddTrip.this,"More than 15 places is not allowed!",Toast.LENGTH_SHORT).show();
            }

        });

    }

    private void writeNewUser(Trip trip) {
        String id = mRootRef.child("Trip").push().getKey();
        trip.setTripId(id);
        mRootRef.child("Trip").child(trip.getTripId()).setValue(trip);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(AddTrip.this, data);
                latitute = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                addCity.setText(place.getName());
                trip.setCity(place.getName()+"");
                String url;

                url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=restaurants&location=+" + latitute + "," + longitude + "&radius=1500&key=AIzaSyC9EgErT0j5pf2677fWPA7dfYXMsAnDJcU";
                new JsonTask().execute(url);


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(AddTrip.this, data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        String temp;

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(AddTrip.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuffer buffer = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            temp = buffer.toString();
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
            placeNameList.clear();
            placesArrayList.clear();
            ArrayList<Places> temporary=(ArrayList) parseGoogleParse(temp);
            for(int index=1;index<temporary.size();index++){
                Places obj=temporary.get(index);
                ListObject listObj=new ListObject();
                listObj.setName(obj.name);
                listObj.setStatus("unchecked");
                placeNameList.add(listObj);
                placesArrayList.add(obj);
            }

            Adapterclass adapterclass = new Adapterclass(AddTrip.this, placeNameList);
            listViewPlaces.setAdapter(adapterclass);
        }
    }


    private static ArrayList<Places> parseGoogleParse(final String response) {
        ArrayList placesList = new ArrayList();
        try {
            JSONObject root = new JSONObject(response);
            JSONArray JSONresults = root.getJSONArray("results");

            for (int i = 0; i < JSONresults.length(); i++) {
                Places places = new Places();
                JSONObject placeJSON = JSONresults.getJSONObject(i);
                places.setName(placeJSON.getString("name"));
                JSONObject geometry = placeJSON.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                places.setLat(location.getString("lat"));
                places.setLng(location.getString("lng"));
                placesList.add(places);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placesList;
    }

    public class Adapterclass extends ArrayAdapter<ListObject> {
        private Activity context;
        private ArrayList<ListObject> list;
        CheckBox checkBox;

        public Adapterclass(Activity context, List<ListObject> list) {
            super(context, R.layout.list_adapter, list);
            this.context = context;
            this.list = (ArrayList<ListObject>) list;
        }

        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            listObj=getItem(position);
            View listViewItem = LayoutInflater.from(getContext()).inflate(R.layout.list_adapter, null, true);
            checkBox=listViewItem.findViewById(R.id.checkBox);
            TextView textViewName = listViewItem.findViewById(R.id.placeName);
            textViewName.setText(listObj.getName()+"");
            String status = listObj.getStatus();

            if (status.equals("unchecked")) {
                checkBox.setChecked(false);
            }

            else if (status.equals("checked")) {
                checkBox.setChecked(true);
            }

            checkBox.setTag(position);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = (Integer) buttonView.getTag();
                    if(isChecked){
                        check++;
                        placeNameList.get(position).setStatus("checked");
                    }
                    else {
                        check--;
                        placeNameList.get(position).setStatus("unchecked");
                    }
                }
            });

            return listViewItem;
        }
    }
}
