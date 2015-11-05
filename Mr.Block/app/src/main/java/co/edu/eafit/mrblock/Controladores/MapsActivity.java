package co.edu.eafit.mrblock.Controladores;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;

import co.edu.eafit.mrblock.Entidades.SimpleGeofence;
import co.edu.eafit.mrblock.Entidades.Ubicacion;
import co.edu.eafit.mrblock.Helper.UbicationHelper;
import co.edu.eafit.mrblock.R;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mapa; // Might be null if Google Play services APK is not available.
    public GeofencingEvent EVENT ;
    private ArrayList<Ubicacion> array;
    private UbicationHelper ubicationHelper;
    public static LatLng storeubic;
    private ArrayList<Geofence> mGeofenceList = new ArrayList<Geofence>();
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private SimpleGeofence geofence;
    private ArrayList<Ubicacion> ubicacionblock = new ArrayList<Ubicacion>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                storeubic = latLng;
                finish();
                Intent i = new Intent(getApplicationContext(), LockBlockActivity.class);
                startActivity(i);
            }
        });
        buildGoogleApiClient();
        CheckUbicationsforDraw();
//        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        CheckUbicationsforDraw();
    }

    private void setUpMapIfNeeded() {
        if (mapa == null) {
            mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
        if (mapa != null) {
            mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mapa.setMyLocationEnabled(true);
        }
    }

    private void CheckUbicationsforDraw(){
        ubicationHelper = new UbicationHelper(getApplicationContext());
        array = ubicationHelper.getAllUbication();
        if(!array.isEmpty()){
            for(int i = 0 ; i< array.size();i++){
                Ubicacion ubicacion = new Ubicacion();
                ubicacion = array.get(i);
                Toast.makeText(getApplicationContext(),"Creando GeoFence" + ubicacion.getName(),Toast.LENGTH_LONG).show();
                double rad = ubicacion.getRadio();
                float radio = (float)rad;
                geofence = new SimpleGeofence(ubicacion.getName(),ubicacion.getLatitud(),ubicacion.getLongitud(),radio);
                addMarkerForFence(geofence);
                mGeofenceList.add(i,geofence.toGeofence());
                Toast.makeText(getApplicationContext(),"Esperando prueba",Toast.LENGTH_LONG).show();
            }
        }
    }

    public void addMarkerForFence(SimpleGeofence fence){
        if(fence == null){
            return;
        }
        mapa.addMarker(new MarkerOptions()
                .position(new LatLng(fence.getLatitude(), fence.getLongitude()))
                .title("Fence " + fence.getId())
                .snippet("Radius: " + fence.getRadius())).showInfoWindow();

        CircleOptions circleOptions = new CircleOptions()
                .center( new LatLng(fence.getLatitude(), fence.getLongitude()) )
                .radius( fence.getRadius() )
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);

        Circle circle = mapa.addCircle(circleOptions);

    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        boolean conectado = mGoogleApiClient.isConnected();
        if(conectado){
            Toast.makeText(getApplicationContext(),"Conectado",Toast.LENGTH_LONG).show();
        }else if(!conectado){
            Toast.makeText(getApplicationContext(),"No Conectado",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            Intent intent = new Intent(getApplicationContext(),MainFragmentActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
