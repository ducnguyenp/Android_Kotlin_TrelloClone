package com.example.trelloclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityMyProfileBinding
import com.example.trelloclone.firebase.FirebaseStore
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {
    private var mSlectedImageUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetail: User

    private var binding: ActivityMyProfileBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        FirebaseStore().signInUser(this)
        setUpNavigationBar()

    binding?.ivUserImage?.setOnClickListener {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Constants.showImageChoose(this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_STORAGE_PERMISSION_CODE
            )
        }
    }
    binding?.btnUpdate?.setOnClickListener {
        if (mSlectedImageUri != null) {
            uploadUserImage()
        } else {
            showProgressDialog(resources.getString(R.string.please_wait))
            updateUserProfile()
        }
    }
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChoose(this)
            } else {
                Toast.makeText(this, "You denied the permission for storage", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSlectedImageUri = data.data
            try {
                Glide
                    .with(this)
                    .load(mSlectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_holder)
                    .into(binding?.ivUserImage!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setUpNavigationBar() {
        setSupportActionBar(binding?.toolbarMyProfileActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.my_profile)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_icon)
        binding?.toolbarMyProfileActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun updateUserProfile() {
        val userHashap = HashMap<String, Any>()
        var anyChangeMade = false

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetail.image) {
            userHashap[Constants.IMAGE] = mProfileImageURL
            anyChangeMade = true
        }
        if (binding?.etName?.text?.toString() != mUserDetail.email) {
            userHashap[Constants.NAME] = binding?.etName?.text.toString()
            anyChangeMade = true
        }
        if (binding?.etMobile?.text.toString() != mUserDetail.mobileNumber.toString()) {
            userHashap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
            anyChangeMade = true
        }
        if (anyChangeMade) {
            FirebaseStore().updateProfileData(this, userHashap)
        }
    }

    fun setUpProfile(user: User) {
        mUserDetail = user

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_holder)
            .into(binding?.ivUserImage!!)
        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)
        if (user.mobileNumber != 0L) {
            binding?.etMobile?.setText(user.mobileNumber.toString())
        }


    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSlectedImageUri != null) {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                        mSlectedImageUri!!, this
                    )
                )
            sRef.putFile(mSlectedImageUri!!).addOnSuccessListener {
                it.metadata!!.reference!!.downloadUrl!!.addOnSuccessListener { uri ->
                    mProfileImageURL = uri.toString()
                    updateUserProfile()
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    fun uploadProfileSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}