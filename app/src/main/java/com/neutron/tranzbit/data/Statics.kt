package com.neutron.tranzbit.data

import com.neutron.tranzbit.data.model.User
import com.squareup.okhttp.*


class Statics {
    companion object{
        val USERS_COLL = "user"
        val TRANSPORTERS_COLL = "transporters"
        var currentUser: User? = User(
            "antoniId", "James Balogun", "08125260125", 1200.10, "test@fsi.ng",
            listOf<Double>(34.0111, -3.055), "12345"
        )
        var IS_CURRENT_USER_LOGGED_IN: Boolean = false

        fun callWoven(){
            Thread(Runnable {
                val client: OkHttpClient = OkHttpClient()
                val mediaType: MediaType = MediaType.parse("application/json")
                val body = RequestBody.create(
                    mediaType,
                    "{\n    \"customer_reference\":${Statics.currentUser?.id} \"\",\n    \"name\": \"${Statics.currentUser?.name}\",\n  " +
                            "  \"email\": \"${Statics.currentUser?.email}\",\n    \"mobile_number\": \"${Statics.currentUser?.phoneNumber}\",\n   " +
                            " \"expires_on\": \"2029-11-01\",\n    \"use_frequency\": \"500000\",\n    \"min_amount\": 100,\n    \"max_amount\": 120000\n}"
                )
                val request: Request = Request.Builder()
                    .url("https://fsi.ng/v2/api/vnubans/create_customer")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("requestId", "7af44212-93a8-47d2-ba7a-391b0b088f5d")
                    .addHeader("api_secret", "vb_ls_bfac75fe54a952841971b6918d06aeb2659523dc92d6")
                    .addHeader("sandbox-key", "MHVBsI91Dd4kdJweNRVkzTU5vWUpHV2Y1634077937")
                    .build()
                client.newCall(request).execute()
            })
        }
    }
}