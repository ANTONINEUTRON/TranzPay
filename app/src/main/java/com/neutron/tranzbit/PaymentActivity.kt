package com.neutron.tranzbit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/**
 * Receives The Scanned Transporter Details and show it
 * With the Pre-Payment View*/
class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_activity)
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, PrePaymentFragment.newInstance())
//                .commitNow()
//        }
    }
}