package fr.miage.projet_m2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.miage.projet_m2.databinding.ActivityMapsBinding;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_ONE = 0;
    private static final int REQUEST_CODE_TWO = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private static final int REQUEST_LOCATION_AND_CAMERA_PERMISSION =1;
    private static final int CAMERA_REQUEST_CODE = 5;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Button buttonPlay;
    private FloatingActionButton floatingActionButton;

    /**
     M??thode ex??cut??e lors de la cr??ation de l'activit?? MapsActivity.
     Elle initialise la vue, demande les permissions n??cessaires et affiche la carte Google Maps.
     Si l'activit?? a ??t?? appel??e avec l'argument "capture" ?? 1, la vue de capture de sprites est affich??e, sinon la vue d'accueil est affich??e.
     @param savedInstanceState ??tat de l'instance de l'activit?? pr??c??demment enregistr??
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        floatingActionButton = findViewById(R.id.fab);
        super.onCreate(savedInstanceState);
        this.askPermission();
        Intent i=getIntent();
        int capture = i.getIntExtra("capture", 0);
        if(capture==1){
            this.deleteCoordinatesSpriteFile();
            binding = ActivityMapsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            this.createCoordinatesFile();
            setContentView(R.layout.home);
        }
        this.ReadJson();
    }

    /**
     M??thode qui lit le fichier JSON "coordinatesSprite.json" et r??cup??re son contenu en tant que cha??ne de caract??res.
     */
    public void ReadJson() {
        try {
            FileInputStream inputStream = openFileInput("coordinatesSprite.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     Fonction qui initialise le jeu en cr??ant le fichier de coordonn??es et en affichant la carte
     @param view La vue actuelle
     */
    public void initialzeGame(View view) {

        this.createCoordinatesFile();
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     Demande les permissions de localisation et de cam??ra ?? l'utilisateur s'il ne les a pas d??j?? accord??es.
     @return true si les permissions sont d??j?? accord??es, false sinon
     */
    private boolean askPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA
                    },
                    REQUEST_LOCATION_AND_CAMERA_PERMISSION);
        }
        return false;
    }

    /**
     M??thode appel??e lorsque la Google Map est pr??te ?? ??tre utilis??e.
     Affiche la position actuelle de l'utilisateur sur la carte et ajoute des marqueurs pour les coordonn??es de sprites contenues dans le fichier coordinatesSprite.json.
     Demande l'autorisation de localisation de l'appareil si elle n'est pas d??j?? accord??e.
     @param googleMap la Google Map qui vient d'??tre initialis??e
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //this.createCoordinatesFile();

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // La permission de localisation n'est pas accord??e, il faut la demander ?? l'utilisateur
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // La permission de localisation n'est pas accord??e, il faut la demander ?? l'utilisateur
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                // La permission de localisation est d??j?? accord??e, vous pouvez utiliser la localisation de l'appareil
            }
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Ma position actuelle"));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                            // J'affiche le marker correspondant aux coordonn??es dans coordinatesSprite.json sur la map
                            try {
                                FileInputStream inputStream = openFileInput("coordinatesSprite.json");
                                int size = inputStream.available();
                                byte[] buffer = new byte[size];
                                inputStream.read(buffer);
                                inputStream.close();
                                String json = new String(buffer, "UTF-8");

                                // Parser la cha??ne JSON pour obtenir les coordonn??es GPS
                                JSONObject jsonObject = new JSONObject(json);

                                double latitude = jsonObject.getDouble("latitude");
                                double longitude = jsonObject.getDouble("longitude");
                                LatLng position1 = new LatLng(latitude, longitude);


                                // Cr??er un marqueur avec une ic??ne par d??faut de couleur bleue
                                MarkerOptions markerOptions = new MarkerOptions().position(position1).title("Position du sprite").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                                // Ajouter le marqueur ?? la carte
                                googleMap.addMarker(markerOptions);

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }

                            // J'affiche le marker correspondant aux coordonn??es dans coordinatesSprite.json sur la map
                            try {
                                FileInputStream inputStream = openFileInput("coordinatesSprite.json");
                                int size = inputStream.available();
                                byte[] buffer = new byte[size];
                                inputStream.read(buffer);
                                inputStream.close();
                                String json = new String(buffer, "UTF-8");

                                // Parser la cha??ne JSON pour obtenir les coordonn??es GPS
                                JSONObject jsonObject = new JSONObject(json);


                                double latitude2 = jsonObject.getDouble("latitudeBis");
                                double longitude2 = jsonObject.getDouble("longitudeBis");
                                LatLng position2 = new LatLng(latitude2, longitude2);

                                // Cr??er un marqueur avec une ic??ne par d??faut de couleur bleue
                                MarkerOptions markerOptions2 = new MarkerOptions().position(position2).title("Position du sprite").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                                // Ajouter le marqueur ?? la carte
                                googleMap.addMarker(markerOptions2);

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }

                            // R??cup??rez la position actuelle de l'utilisateur
                            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                            FloatingActionButton fab = findViewById(R.id.fab);
                            // R??cup??rez la liste des coordonn??es pr??sentes dans coordinatesSprite.json
                            List<LatLng> spriteCoordinates = getSpriteCoordinates();
                            // Calculer la distance entre la position actuelle de l'utilisateur et chaque coordonn??e de spriteCoordinates
                            for (LatLng coord : spriteCoordinates) {
                                double distance = SphericalUtil.computeDistanceBetween(currentPosition, coord);

                                // Si la distance est inf??rieure ?? un certain seuil, activez le bouton flottant
                                if (distance < 100) {
                                    fab.setEnabled(true);
                                    break;
                                }
                                else {
                                    // Si aucune des coordonn??es n'est proche, d??sactivez le bouton flottant
                                    fab.setEnabled(false);
                                }
                            }


                        }
                    }
                });

        
        
    }

    /**
     Cette fonction lit le fichier JSON "coordinatesSprite.json" et retourne une liste de coordonn??es LatLng correspondant aux deux paires de latitude et longitude dans le fichier.
     @return Une liste de LatLng contenant les coordonn??es lues dans le fichier JSON.
     */
    private List<LatLng> getSpriteCoordinates() {
        List<LatLng> spriteCoordinates = new ArrayList<>();
        try {
            FileInputStream inputStream = openFileInput("coordinatesSprite.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, "UTF-8");
            // parse the JSON file
            JSONObject jsonObject = new JSONObject(json);
            double lat1 = jsonObject.getDouble("latitude");
            double lng1 = jsonObject.getDouble("longitude");
            spriteCoordinates.add(new LatLng(lat1, lng1));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        try {
            FileInputStream inputStream = openFileInput("coordinatesSprite.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, "UTF-8");
            // parse the JSON file
            JSONObject jsonObject = new JSONObject(json);
            double lat2 = jsonObject.getDouble("latitudeBis");
            double lng2 = jsonObject.getDouble("longitudeBis");
            spriteCoordinates.add(new LatLng(lat2, lng2));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        return spriteCoordinates;
    }


    /**
     Cr??e un fichier de coordonn??es GPS ?? partir de la position actuelle de l'utilisateur
     et de deux nouvelles coordonn??es situ??es ?? 10 m??tres de la position actuelle.
     Les coordonn??es sont stock??es dans un fichier JSON nomm?? "coordinatesSprite.json".
     */
    private void createCoordinatesFile() {
        // Obtenir la position actuelle de l'utilisateur
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // La permission de localisation n'est pas accord??e, il faut la demander ?? l'utilisateur
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // La permission de localisation n'est pas accord??e, il faut la demander ?? l'utilisateur
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                // La permission de localisation est d??j?? accord??e, vous pouvez utiliser la localisation de l'appareil
            }
        }
        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                // Cr??er des coordonn??es GPS situ??es ?? 10 m??tres de la position actuelle
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double newLatitude = latitude + 0.0004;
                double newLongitude = longitude + 0.0004;

                double latitude2 = location.getLatitude();
                double longitude2 = location.getLongitude();
                double newLatitude2 = latitude2 + 0.0015;
                double newLongitude2 = longitude2 + 0.0015;

                // Cr??er une cha??ne JSON ?? partir des coordonn??es GPS
                String json = "{\"latitude\": " + newLatitude + ", \"longitude\": " + newLongitude + ", \"latitudeBis\": " + newLatitude2+", \"longitudeBis\": " + newLongitude2+ "}";

                try {
                    FileOutputStream outputStream = openFileOutput("coordinatesSprite.json", Context.MODE_PRIVATE);
                    outputStream.write(json.getBytes());
                   // outputStream.write(json2.getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     M??thode appel??e lorsqu'une demande de permission a ??t?? effectu??e aupr??s de l'utilisateur.
     Cette m??thode est utilis??e pour v??rifier si la permission a ??t?? accord??e ou refus??e.
     @param requestCode code de la demande de permission
     @param permissions tableau des permissions demand??es
     @param grantResults tableau des r??sultats de la demande de permission (accord??e ou refus??e)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            switch (requestCode) {
                case REQUEST_LOCATION_AND_CAMERA_PERMISSION:
                    // Si la demande a ??t?? annul??e, le tableau de r??sultats est vide.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    } else {
                        // La permission a ??t?? refus??e, vous pouvez afficher un message d'erreur ou emp??cher l'op??ration de se poursuivre.
                        Toast.makeText(this, "La permission de localisation est n??cessaire pour cette op??ration", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    // Traitement des autres cas de demande de permission.
                    break;
            }
        }

    /**
     Supprime les coordonn??es du sprite du fichier "coordinatesSprite.json"
     */
    public void deleteCoordinatesSpriteFile() {
           try {
               FileInputStream inputStream = openFileInput("coordinatesSprite.json");
               int size = inputStream.available();
               byte[] buffer = new byte[size];
               inputStream.read(buffer);
               inputStream.close();
               String json = new String(buffer, "UTF-8");

               // Parser la cha??ne JSON pour obtenir les coordonn??es GPS
               JSONObject jsonObject = new JSONObject(json);
               jsonObject.remove("latitude");
               jsonObject.remove("longitude");

               // ??crire les donn??es modifi??es dans le fichier
               FileOutputStream outputStream = openFileOutput("coordinatesSprite.json", Context.MODE_PRIVATE);
               outputStream.write(jsonObject.toString().getBytes());
               outputStream.close();
           } catch (IOException | JSONException e) {
               e.printStackTrace();
           }
       }


    /**
     * Ouvre l'activity de la cam??ra lorsque le bouton "Ouvrir la cam??ra" est cliqu??.
     *
     * @param view La vue du bouton "Ouvrir la cam??ra"
     */
    public void OpenCamera(View view) {

        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}