package com.example.admir.maps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.example.admir.maps.ClientUtils.AsyncResponce;
import com.example.admir.maps.ClientUtils.RouteFinder;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


import static com.example.admir.maps.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AsyncResponce {

    private final int receivingPort = 6000;
    private GoogleMap mMap;
    private String to;
    private String from;
    private String mode;
    private String destinationIp;
    private int destinationPort;
    private ArrayList<Polyline> allPolylines = new ArrayList<>();
    //private ArrayList<LatLng> currentLocation = new ArrayList<>();
    private LatLng currentLocation;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        Context context = getApplicationContext();

        Properties properties = new Properties();
        InputStream is = context.getResources().openRawResource(R.raw.config);
        try {
            properties.loadFromXML(is);
            System.out.println("GATE 1:" + properties.getProperty("GATE1"));
            String[] gateInfo = properties.getProperty("GATE1").split(":");
            destinationIp = gateInfo[0];
            destinationPort = Integer.parseInt(gateInfo[1]);
            // gate = new Pair<>(InetAddress.getByName(gateInfo[0]), Integer.parseInt(gateInfo[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (mMap != null) {


            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                @Override
                public void onMyLocationChange(Location arg0) {
                    // TODO Auto-generated method stub

                  //  mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));
                    currentLocation=new LatLng(arg0.getLatitude(),arg0.getLongitude());
                }
            });

        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.980848, 23.7297373), 12.0f));
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            List<Marker> listOfMarkers = new ArrayList<Marker>();

            @Override
            public void onMapClick(LatLng latLng) {
                RadioButton radioButtonSource = (RadioButton) findViewById(R.id.source);
                RadioButton radioButtonDestination = (RadioButton) findViewById(R.id.destination);
                RadioButton radioButtonCurrentLocation=(RadioButton) findViewById(R.id.current_location_btn);
                //Source Actions
                if (radioButtonSource.isChecked()) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title("source");
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    if (!listOfMarkers.isEmpty()) {
                        for (int i = 0; i < listOfMarkers.size(); i++) {
                            if (listOfMarkers.get(i).getTitle().equals("source")) {
                                listOfMarkers.get(i).remove();
                                listOfMarkers.remove(i);
                            }
                        }
                    }


                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    Marker marker = mMap.addMarker(markerOptions);
                    listOfMarkers.add(marker);
                    from = lowerPrecission(latLng.latitude, latLng.longitude);
                    }
                //put marker in users location
                else if(radioButtonCurrentLocation.isChecked()){
                    if(currentLocation!=null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.title("source");
                        markerOptions.position(currentLocation);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        if (!listOfMarkers.isEmpty()) {
                            for (int i = 0; i < listOfMarkers.size(); i++) {
                                if (listOfMarkers.get(i).getTitle().equals("source")) {
                                    listOfMarkers.get(i).remove();
                                    listOfMarkers.remove(i);
                                }
                            }
                        }


                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
                        Marker marker = mMap.addMarker(markerOptions);
                        listOfMarkers.add(marker);
                        from = lowerPrecission(currentLocation.latitude, currentLocation.longitude);
                    }
                }

                //Destination Actions
                if (radioButtonDestination.isChecked()) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title("destination");
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    if (!listOfMarkers.isEmpty()) {
                        for (int i = 0; i < listOfMarkers.size(); i++) {
                            if (listOfMarkers.get(i).getTitle().equals("destination")) {
                                listOfMarkers.get(i).remove();
                                listOfMarkers.remove(i);
                            }
                        }
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    Marker marker = mMap.addMarker(markerOptions.draggable(true));
                    listOfMarkers.add(marker);
                    to = lowerPrecission(latLng.latitude, latLng.longitude);
                }

                Button findRouteBtn = (Button) findViewById(R.id.find_route_btn);
                buttonClicked(findRouteBtn);
            }
        });

    }


    private void buttonClicked(Button findRoute) {
        RadioButton modeDriving = (RadioButton) findViewById(R.id.driving);
        RadioButton modeWalking = (RadioButton) findViewById(R.id.walking);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        final String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());


        if (modeDriving.isChecked()) {
            mode = modeDriving.getText().toString();
        } else if (modeWalking.isChecked()) {
            mode = modeWalking.getText().toString();
        } else {
            mode = "walking";
        }
        final RouteFinder routeFinder = new RouteFinder(from, to, mode, ipAddress, destinationIp, destinationPort);
        routeFinder.delegate = this;


        findRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (from != null && to != null) {
                        routeFinder.execute();
                    }

                } catch (Exception e) {
                    System.out.println("deligate exception" + from + to + mode + ipAddress);

                }

            }
        });
    }

    @Override
    public void processFinish(String output) {
        if (output != null) {
            System.out.println(output);
            drawPath(output);
        }

    }


    public void drawPath(String result) {

        try {

            if (allPolylines != null) {
                for (Polyline poly : allPolylines) {
                    poly.remove();
                }
                allPolylines.clear();
            }
            //Tranform the string into a json object
            System.out.println("LIst of polyline:");
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");

            List<LatLng> list = decodePoly(encodedString);
            System.out.println("LIst of polyline:" + list.get(0));
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)

            );
            allPolylines.add(line);
            for (int z = 0; z < list.size() - 1; z++) {
                LatLng src = list.get(z);
                LatLng dest = list.get(z + 1);
                Polyline line2 = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(2)
                        .color(Color.BLUE).geodesic(true));
                allPolylines.add(line2);
            }

        } catch (JSONException e) {

        }

    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public String lowerPrecission(double coordinateLat, double coordinateLong) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        decimalFormatSymbols.setDecimalSeparator('.');

        String lowerCoordinatePrecisionLat = new DecimalFormat("#.####", decimalFormatSymbols).format(coordinateLat);
        String lowerCoordinatePrecisionLong = new DecimalFormat("#.####", decimalFormatSymbols).format(coordinateLong);
        System.out.println("Precission is:" + lowerCoordinatePrecisionLat + "LOng" + lowerCoordinatePrecisionLong);
        return lowerCoordinatePrecisionLat + "," + lowerCoordinatePrecisionLong;

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }






}
