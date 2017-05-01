package com.lowlightstudios.watlo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.lowlightstudios.watlo.Custom.DottedRadius;
import com.lowlightstudios.watlo.Custom.InfoCardsAdapter;
import com.lowlightstudios.watlo.core.Utils;
import com.lowlightstudios.watlo.models.InfoCard;
import com.lowlightstudios.watlo.services.WaterService;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener, WaterService.ServiceResponse {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location currentLocation;
    private View mapView;
    private EditText searchBar;

    public final static int REQUEST_PERMISSION_GPS = 1;
    public final static int REQUEST_PERMISSION_LOCATION = 2;
    public final static String TAG = "style";
    private DottedRadius dottedRadius;

    private RecyclerView infoCardsRecycler;
    private InfoCardsAdapter infoCardsAdapter;

    private WaterService waterService;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing googleApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mapView = mapFragment.getView();
        dottedRadius = (DottedRadius) findViewById(R.id.dotted_circle);

        searchBar = (EditText) findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    onMapSearch();
                    return true;
                }
                return false;
            }
        });

        //RecyclerView
        infoCardsRecycler = (RecyclerView) findViewById(R.id.info_cards_recycler);
        infoCardsRecycler.setHasFixedSize(true);

        infoCardsAdapter = new InfoCardsAdapter(this);
        infoCardsRecycler.setAdapter(infoCardsAdapter);

        //Layout manager for the Recycler View
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        infoCardsRecycler.setLayoutManager(mLayoutManager);

        progressBar = (ProgressBar) findViewById(R.id.loading_water);

        waterService = new WaterService(this, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_GPS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMap();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.permission_gps_denied))
                            .setCancelable(true)
                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
                break;
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getMyLocation();
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onCameraIdle() {
        getPoints();
    }

    @Override
    public void onResponseComplete(int codeResult) {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoadedNews(InfoCard infoCard) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_GPS);
            return;
        }

        initMap();
    }

    @SuppressWarnings("MissingPermission")
    private void initMap() {
        if (mMap != null) {
            mMap.clear();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);

            // Default location to New York.
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.825774, -73.858294), 8));
            mMap.setOnCameraIdleListener(this);

            styleReady(mMap);
        }
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 160);
        }
    }

    public void styleReady(GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

    }

    private void getMyLocation() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_GPS);
            return;
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    public void onMapSearch() {
        String location = searchBar.getText().toString();
        List<Address> addressList = null;

        if (!location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker").icon(BitmapDescriptorFactory.
                    fromBitmap(Utils.getBitmapFromVectorDrawable(this, R.drawable.ic_waterdrop))));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

            InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
        }
    }

    public void getPoints() {
        progressBar.setVisibility(View.VISIBLE);
        float radius = dottedRadius.getCircleRadius();
        LatLng circlePoint = mMap.getCameraPosition().target;
        Point screenLoc = mMap.getProjection().toScreenLocation(circlePoint);

        int startXPosition = screenLoc.x;
        int positiveXCoordenate = screenLoc.x + (int) radius;
        int negativeXCoordenate = screenLoc.x - (int) radius;
        int positiveYCoordenate = screenLoc.y - (int) radius;
        int negativeYCoordenate = screenLoc.y + (int) radius;

        screenLoc.x = positiveXCoordenate;
        LatLng positiveX = mMap.getProjection().fromScreenLocation(screenLoc);
        screenLoc.x = negativeXCoordenate;
        LatLng negativeX = mMap.getProjection().fromScreenLocation(screenLoc);
        screenLoc.x = startXPosition;
        screenLoc.y = positiveYCoordenate;
        LatLng positiveY = mMap.getProjection().fromScreenLocation(screenLoc);
        screenLoc.y = negativeYCoordenate;
        LatLng negativeY = mMap.getProjection().fromScreenLocation(screenLoc);

        waterService.requestGroundWater(negativeX.longitude, negativeX.latitude, positiveY.longitude, positiveY.latitude);
    }
}
