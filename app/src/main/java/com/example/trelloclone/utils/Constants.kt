package com.example.trelloclone.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.trelloclone.activities.MyProfileActivity
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val USERS: String = "users"
    const val NAME: String = "name"
    const val IMAGE: String = "image"
    const val MOBILE: String = "mobileNumber"
    const val BOARDS: String = "boards"
    const val ASSSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"
    const val BOARD_DETAIL: String = "boardDetail"
    const val ID: String = "id"
    const val EMAIL: String = "email"
    const val ASSIGNED_TO: String = "assignedTo"
    const val TASK_LIST_POSITION: String = "taskListPosition"
    const val CARD_POSITION: String = "cardPosition"
    const val BOARD_MEMBER_LIST: String = "boardMemberList"
    const val UN_SELECT: String = "unSelect"
    const val SELECT: String = "select"

    const val PICK_IMAGE_REQUEST_CODE: Int = 2
    const val READ_STORAGE_PERMISSION_CODE: Int = 1

    fun showImageChoose(activity: Activity) {
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(uri: Uri, activity: Activity): String {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))!!
    }
}