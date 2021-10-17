package com.neutron.tranzbit.data.repository

import android.view.View

class TransporterRepository() {
    fun getTransporterDetails(transporterId: String, successView: View, failureView: View){
        //Will display appropriete view according to response received from firebase
    }

    fun payTransporter(transporterId: String, amount: Double, payerId: String){

    }
}