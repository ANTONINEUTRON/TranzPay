package com.neutron.tranzbit

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.neutron.tranzbit.data.Statics
import com.neutron.tranzbit.data.model.Transporter
import com.neutron.tranzbit.data.model.User

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var isProfileDisplayed: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_REQUEST_CODE: Int = 1111
    private var isLocationAccessGranted: Boolean = false
    private lateinit var mMap: GoogleMap
    private lateinit var payBtn: Button
    private lateinit var tripsBtn: Button
    private lateinit var paytransporterBtn: Button
    private lateinit var amountEditText: EditText
    private lateinit var transporterDetailsRV: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val db = FirebaseFirestore.getInstance()
    private lateinit var transporterDetails: Transporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setSupportActionBar(findViewById(R.id.toolbar))

        if(!Statics.IS_CURRENT_USER_LOGGED_IN){
            startActivity(Intent(this, LoginActivity::class.java))
            this.finish()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        payBtn = findViewById(R.id.pay)
        payBtn.setOnClickListener{
            scanQrCode()
        }

        val bottomSheet: View = findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        progressBar = findViewById(R.id.progress_bar)

        amountEditText = findViewById(R.id.amount_edit)
        paytransporterBtn = findViewById(R.id.pay_transporter)

        paytransporterBtn.setOnClickListener {
            val amtEntered = amountEditText.text.toString()
            val amount: Double = if(amtEntered.isNotEmpty()) amtEntered.toDouble() else 0.0

            if(amount > 0) {
                //Connect to user account and debit

                ////Credit transporter account
                creditTransporterAccount(amount)
                ////SEND RESPONSE TO SuccessActivity to be displayed
            }else{
                Toast.makeText(this,"You entered an invalid amount",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(isProfileDisplayed){
            displayUserProfile()
        }
    }

    private fun creditTransporterAccount(amount: Double) {
        db.collection(Statics.USERS_COLL).document(transporterDetails.userDetails.id!!)
            .update(mapOf("balance" to transporterDetails.userDetails.balance!! + amount))
            .addOnSuccessListener {
                startActivity(Intent(this, SuccessActivity::class.java))
                //Deduct from current user account
                Statics.currentUser?.balance = Statics.currentUser?.balance?.minus(amount)
                amountEditText.setText("0.0")
                findViewById<EditText>(R.id.start_location).setText("")
                findViewById<EditText>(R.id.end_location).setText("")
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            .addOnFailureListener { exception ->
                //Pass message to Failure activity and show
                val intent = Intent(this, FailureActivity::class.java)
                startActivity(intent)
            }
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if(isLocationAccessGranted){
            //Get user location
            try {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true

                getCurrentUserLocation()
            } catch (e: SecurityException){
                Log.e("TranzPay Exception: ", e.message, e)
            }
        }else{
            getLocationAccessPermission()
        }

    }

    private fun getCurrentUserLocation(){
        //check permission
        try{
            requestLocationUpdates()

            //Get and display last location
            val locationResult = fusedLocationClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    //set map camera to current position
                    val lastKnownLocation: Location? = task.result
                    showLocation(lastKnownLocation)
                }else{
                    showDefaultLocation()

                    val alert = AlertDialog.Builder(this)
                    alert.apply {
                        setMessage("It seems your device GPS is off \n Please turn it on in order to get your current location")
                        setNeutralButton("Ok",null)
                        show()
                    }
                }

            }
        }catch (e: SecurityException){
            Log.e("TranzPay Exception: ", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        /**
         * Helps incase google doesn't have user location record*/
        val locationRequest = LocationRequest.create()
        locationRequest.apply {
            interval = 60000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        showLocation(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun showLocation(lastKnownLocation: Location?) {
        if (lastKnownLocation != null) {
            val userLocation = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
            mMap.addMarker(MarkerOptions().position(userLocation).title("Your Current Location"))
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    userLocation, 16.0.toFloat()
                )
            )
        } else {
            showDefaultLocation()
        }
    }

    private fun showDefaultLocation() {
        // Add a marker in Nigeria and move the camera
        val defaultLocation: LatLng = LatLng(9.0820, 8.6753)
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation))
    }

    private fun getLocationAccessPermission() {
        if(ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            isLocationAccessGranted = true
            getCurrentUserLocation()
        }else{
            ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    isLocationAccessGranted = true
                    getCurrentUserLocation()
                }else{
                    getLocationAccessPermission()
                }
            }
        }
    }

    fun scanQrCode() {
        val scanIntegrator = IntentIntegrator(this)
        scanIntegrator.setPrompt("Pay For A Trip")
            .setBeepEnabled(true)
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
            .setCaptureActivity(CaptureActivity::class.java)
            .setOrientationLocked(true)
            .setBarcodeImageEnabled(true)
            .initiateScan()

        progressBar.visibility = View.VISIBLE
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        lateinit var scanResponse:String
        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (!scanningResult.contents.isNullOrEmpty()) {
            scanResponse = scanningResult.contents.toString()
            //TODO
            //Get db referencce
            //And verify driver details on database
            lateinit var userDetails: User
            db.collection(Statics.TRANSPORTERS_COLL).document(scanResponse)
                .get()
                .addOnSuccessListener { document ->
                    progressBar.visibility = View.GONE
                    db.collection(Statics.USERS_COLL).document(scanResponse)
                        .get()
                        .addOnSuccessListener { details ->
                            val user: User? = details.toObject(User::class.java)
                            if(user != null) {
                                userDetails = user

                                //Instantiate transporter object
                                val vDesc = document["vehicle"].toString()
                                transporterDetails = Transporter(userDetails, vDesc)


                                //Show Transporters Details with payment availability
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                                val details = mapOf<String,String>("name" to transporterDetails.userDetails.name!!,
                                    "Phone Number" to transporterDetails.userDetails.phoneNumber!!,
                                    "email" to transporterDetails.userDetails.email!!,
                                    "Vehicle Description" to transporterDetails.vehicleDesc)
                                transporterDetailsRV = findViewById(R.id.transporter_det_rv)
                                transporterDetailsRV.adapter = TransporterDetailsAdapter(details)
                                transporterDetailsRV.setHasFixedSize(true)
                                transporterDetailsRV.layoutManager = GridLayoutManager(this,2)

                                progressBar.visibility = View.GONE
                            }
                        }

                }
                .addOnFailureListener { exception ->
                    progressBar.visibility = View.GONE
                    //Alert user to the failure
                    val alert = AlertDialog.Builder(this)
                    alert.apply {
                        setMessage(exception.message+" ''' ${exception.toString()}")
                        setTitle("Error Getting Driver Details")
                        setNeutralButton("Ok", null)
                        show()
                    }
                }
        } else {
            Toast.makeText(
                this,
                "Nothing Found \n Make sure you place the Barcode correctly",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.account -> {
                if(isProfileDisplayed){
                    hideUserProfile()
                }else {
                    displayUserProfile()
                }
                return true
            }
            R.id.f_acct -> {
                startActivity(Intent(this, FundAcctActivity::class.java))
                return true
            }
            R.id.log_out -> {
                startActivity(Intent(this, LoginActivity::class.java))
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayUserProfile() {
        isProfileDisplayed = true
        findViewById<LinearLayout>(R.id.user_profile).visibility = View.VISIBLE
        findViewById<TextView>(R.id.username).text = Statics.currentUser?.name
        findViewById<TextView>(R.id.balance).text = "NGN" + Statics.currentUser?.balance.toString()
    }

    private fun hideUserProfile() {
        isProfileDisplayed = false
        findViewById<LinearLayout>(R.id.user_profile).visibility = View.GONE
    }
}