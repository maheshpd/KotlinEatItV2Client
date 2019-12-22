package com.createsapp.kotlineatitv2client

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.createsapp.kotlineatitv2client.Remote.ICloudFunction
import com.createsapp.kotlineatitv2client.Remote.RetrofitCloudClient
import com.facebook.accountkit.AccessToken
import com.facebook.accountkit.AccountKit
import com.facebook.accountkit.AccountKitLoginResult
import com.facebook.accountkit.ui.AccountKitActivity
import com.facebook.accountkit.ui.AccountKitConfiguration
import com.facebook.accountkit.ui.LoginType
import com.google.firebase.auth.FirebaseAuth
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private val compositeDisposable = CompositeDisposable()
    private lateinit var cloudFunction: ICloudFunction

    companion object {
        private val APP_REQUEST_CODE = 7171 //Any number
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener)
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        cloudFunction = RetrofitCloudClient.getInstance().create(ICloudFunction::class.java)
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                //Already login
                Toast.makeText(this@MainActivity, "Already login", Toast.LENGTH_SHORT).show()
            } else {
                //Not login
                val accessToken = AccountKit.getCurrentAccessToken()
                if (accessToken != null)
                    getCustomeToken(accessToken)
                else
                    phoneLogin()
            }
        }
    }

    private fun phoneLogin() {
        val intent = Intent(this@MainActivity, AccountKitActivity::class.java)
        val configurationBuilConfig = AccountKitConfiguration.AccountKitConfigurationBuilder(
            LoginType.PHONE,
            AccountKitActivity.ResponseType.TOKEN
        )
        intent.putExtra(
            AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
            configurationBuilConfig.build()
        )
        startActivityForResult(intent, APP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == APP_REQUEST_CODE)
            handleFacebookLoginResult(resultCode, data)
    }

    private fun handleFacebookLoginResult(resultCode: Int, data: Intent?) {
        val result =
            data!!.getParcelableExtra<AccountKitLoginResult>(AccountKitLoginResult.RESULT_KEY)
        if (result!!.error != null) {
            Toast.makeText(
                this@MainActivity,
                "" + result!!.error!!.userFacingMessage,
                Toast.LENGTH_SHORT
            ).show()
        } else if (result.wasCancelled() || resultCode == Activity.RESULT_CANCELED) {
            finish()
        } else {
            if (result.accessToken != null) {
                getCustomeToken(result.accessToken!!)
                Toast.makeText(this@MainActivity, "Login OK!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCustomeToken(accessToken: AccessToken) {
        dialog!!.show()
        compositeDisposable.add(cloudFunction!!.getCustomToken(accessToken.token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ responseBody ->
                val customeToken = responseBody.string()
                signInWithCustomToken(customeToken)
            }, {t: Throwable? ->
                dialog!!.dismiss()
                Toast.makeText(this@MainActivity,""+t!!.message,Toast.LENGTH_SHORT).show()
            }))
    }

    private fun signInWithCustomToken(customeToken: String) {
        dialog!!.dismiss()
        firebaseAuth!!.signInWithCustomToken(customeToken)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful)
                    Toast.makeText(this@MainActivity,"Authentication Failed!",Toast.LENGTH_SHORT).show()

            }
    }

}
