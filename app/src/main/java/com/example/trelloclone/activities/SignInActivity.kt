package com.example.trelloclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivitySignInBinding
import com.example.trelloclone.firebase.FirebaseStore
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignInActivity : BaseActivity() {
    var binding: ActivitySignInBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()
        binding?.btnSignIn?.setOnClickListener {
            signInRegisteredUser()
        }
    }
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignInActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_icon)

        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun signInRegisteredUser() {
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FirebaseStore().signInUser(this)
                    } else {
                        hideProgressDialog()
                        Toast.makeText(
                            this@SignInActivity,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        Toast.makeText(
            this@SignInActivity,
            "You have successfully signed in.",
            Toast.LENGTH_LONG
        ).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    private fun validateForm(email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            showErrorSnackBar("Please enter email.")
            false
        } else if (TextUtils.isEmpty(password)) {
            showErrorSnackBar("Please enter password.")
            false
        } else {
            true
        }
    }
}