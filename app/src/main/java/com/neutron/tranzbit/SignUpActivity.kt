package com.neutron.tranzbit

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.neutron.tranzbit.data.Statics
import com.neutron.tranzbit.data.model.User

class SignUpActivity : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private lateinit var password: EditText
    private lateinit var signUp: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var nameStr: String
    private lateinit var emailStr: String
    private lateinit var phoneStr: String
    private lateinit var passwordStr: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        setSupportActionBar(findViewById(R.id.toolbar))

        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        phone = findViewById(R.id.phone)
        password = findViewById(R.id.password)
        signUp = findViewById(R.id.sign_up)
        progressBar = findViewById(R.id.progress_bar)


        signUp.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            nameStr = name.text.toString()
            emailStr = email.text.toString()
            phoneStr = phone.text.toString()
            passwordStr = password.text.toString()

            if(nameStr.isNotEmpty() && emailStr.isNotEmpty()
                && phoneStr.isNotEmpty() && passwordStr.isNotEmpty()){
                //Upload user data
                val id = "${System.currentTimeMillis()}"

                val newUser = User(
                    id,
                    nameStr,
                    phoneStr,
                    2000.0,
                    emailStr,
                    listOf<Double>(23.33, 4.44),
                    passwordStr
                )

                Statics.currentUser = newUser
                Statics.IS_CURRENT_USER_LOGGED_IN = true

                val alert = AlertDialog.Builder(this)
                alert.apply {
                    setMessage("Account Created Successfully \n And Your account will be funded with a signup bonus of NGN2000")
                    setPositiveButton("continue", DialogInterface.OnClickListener { dialogInterface, i ->
                        startActivity(Intent(this@SignUpActivity, MapsActivity::class.java))
                        this@SignUpActivity.finish()
                    })
                    setNeutralButton("FUND ACCOUNT", DialogInterface.OnClickListener { dialogInterface, i ->
                        startActivity(Intent(this@SignUpActivity, FundAcctActivity::class.java))
                    })
                    show()
                }
//
//                FirebaseFirestore.getInstance()
//                    .collection(Statics.USERS_COLL)
//                    .document(id)
//                    .set(newUser)
//                    .addOnSuccessListener {
//
//                    }
                //TODO USE API TO GIVE user Nuban
            }
        }
    }
}