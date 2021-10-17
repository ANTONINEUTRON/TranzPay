package com.neutron.tranzbit

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.neutron.tranzbit.data.Statics

class LoginActivity : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var loginBtn: Button
    lateinit var altSignIn: TextView
    lateinit var pb: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginBtn = findViewById(R.id.login)
        altSignIn = findViewById(R.id.sign_up_msg)

        pb = findViewById(R.id.progress_bar)

        loginBtn.setOnClickListener {
            pb.visibility = View.VISIBLE
            val emailStr = email.text.toString()
            val passwordStr = password.text.toString()
            if(emailStr.isNotEmpty() && passwordStr.isNotEmpty()){
                validateUserLogin(emailStr,passwordStr)
            }else{
                Toast.makeText(this, "You have an empty field",Toast.LENGTH_LONG).show()
            }
        }

        altSignIn.setOnClickListener {
            takeToSignInActivity()
        }
    }

    private fun validateUserLogin(emailStr: String, passwordStr: String) {
        if(emailStr.toLowerCase().equals(Statics.currentUser?.email) &&
                passwordStr.toLowerCase().equals(Statics.currentUser?.password)){
            pb.visibility = View.GONE
            Statics.IS_CURRENT_USER_LOGGED_IN = true
            startActivity(Intent(this, MapsActivity::class.java))
            finish()
        }else{
            pb.visibility = View.GONE
            val alertDialog = AlertDialog.Builder(this)
                alertDialog.apply {
                    setMessage("Wrong email/password")
                    setPositiveButton("Forgot Password", DialogInterface.OnClickListener { dialogInterface, i ->
                        val alert = AlertDialog.Builder(this@LoginActivity)
                        alert.apply {
                            setMessage("To access demo account\n Use test@fsi.ng for email and 12345 as password")
                            setNeutralButton("OK",null)
                            show()
                        }
                    })
                    setNeutralButton("Sign Up", DialogInterface.OnClickListener { dialogInterface, i ->
                        takeToSignInActivity()
                    })
                    setCancelable(false)
                    show()
                }
        }

//        val db = FirebaseFirestore.getInstance()
//        db.collection(Statics.USERS_COLL)
//            .whereEqualTo("email",email.text.toString())
//            .get()
//            .addOnSuccessListener { document ->
//                if(document != null) {
//                    if(document.get(0).email.equals(email.text.toString()) &&
//                        user.get(0).password.equals(password.text.toString())){
//                        pb.visibility = View.GONE
//                        Statics.IS_CURRENT_USER_LOGGED_IN = true
//                        startActivity(Intent(this, MapsActivity::class.java))
//                        finish()
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                val alertDialog = AlertDialog.Builder(this)
//                alertDialog.apply {
//                    setMessage("${exception.message}")
//                    setPositiveButton("Ok", null)
//                    setNeutralButton("Sign Up", DialogInterface.OnClickListener { dialogInterface, i ->
//                        takeToSignInActivity()
//                    })
//                    setCancelable(false)
//                    show()
//                }
//            }
    }

    private fun takeToSignInActivity() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
}