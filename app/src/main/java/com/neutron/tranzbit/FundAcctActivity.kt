package com.neutron.tranzbit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.neutron.tranzbit.data.Statics

class FundAcctActivity : AppCompatActivity() {
    lateinit var trfBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_acct)

        findViewById<Button>(R.id.trsf_btn).setOnClickListener {
            Statics.callWoven()
            startActivity(Intent(this, MapsActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.pay_thru_card).setOnClickListener {
            Statics.callWoven()
            val str = findViewById<EditText>(R.id.amt).text.toString()
            val amount = if(str.isNotEmpty()){
                str.toDouble()
            } else{
                0.0
            }

            if(amount > 0.0) {
                Statics.currentUser?.balance = Statics.currentUser?.balance?.plus(amount)
                Toast.makeText(this, "Account Funded Successsfully", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this,"Invalid Amount Entered",Toast.LENGTH_LONG).show()
            }
        }
    }
}