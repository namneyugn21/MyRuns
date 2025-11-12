package sfu.namnguyen.myruns

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import java.io.File
import androidx.core.content.edit

class ProfileActivity : AppCompatActivity() {
    // variable storing the profile photo
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var tempImgUri: Uri
    private lateinit var myViewModel: MyViewModel
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>

    // variables to store form input fields
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var classEditText: EditText
    private lateinit var majorEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private val tempImgFileName = "temporary_image.jpg"
    private val profileImgFileName = "profile_image.jpg"

    companion object {
        private const val PREFS_NAME = "MyRunsProfilePref"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_GENDER = "gender"
        private const val KEY_CLASS = "class"
        private const val KEY_MAJOR = "major"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // initialize variables
        imageView = findViewById(R.id.profile_photo_imageView)
        nameEditText = findViewById(R.id.name_editText)
        emailEditText = findViewById(R.id.email_editText)
        phoneEditText = findViewById(R.id.phone_editText)
        genderRadioGroup = findViewById(R.id.gender_radioGroup)
        classEditText = findViewById(R.id.class_editText)
        majorEditText = findViewById(R.id.major_editText)

        // load profile
        loadProfile()

        // set up camera
        setImageSelection(savedInstanceState)

        // set up button
        button = findViewById(R.id.profile_photo_button)
        button.setOnClickListener { showPhotoSelectionDialog() }

        saveButton = findViewById(R.id.save_button)
        saveButton.setOnClickListener { saveProfile() }

        cancelButton = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
            if (tempImgFile.exists()) {
                tempImgFile.delete()
            }
            finish()
        }
    }

    private fun loadProfile() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        nameEditText.setText(sharedPreferences.getString(KEY_NAME, ""))
        emailEditText.setText(sharedPreferences.getString(KEY_EMAIL, ""))
        phoneEditText.setText(sharedPreferences.getString(KEY_PHONE, ""))
        majorEditText.setText(sharedPreferences.getString(KEY_MAJOR, ""))

        val savedClassOf = sharedPreferences.getInt(KEY_CLASS, -1)
        if (savedClassOf != -1) {
            classEditText.setText(savedClassOf.toString())
        } else {
            classEditText.setText("")
        }

        val savedGenderId = sharedPreferences.getInt(KEY_GENDER, -1)
        if (savedGenderId != -1) {
            genderRadioGroup.check(savedGenderId)
        }
    }

    private fun saveProfile() {
        // validation Check
        if (nameEditText.text.isNullOrBlank() ||
            emailEditText.text.isNullOrBlank() ||
            phoneEditText.text.isNullOrBlank() ||
            classEditText.text.isNullOrBlank() ||
            majorEditText.text.isNullOrBlank() ||
            genderRadioGroup.checkedRadioButtonId == -1
        ) {
            Toast.makeText(this, "Please fill in all profile fields.", Toast.LENGTH_LONG).show()
            return
        }

        val classYear = classEditText.text.toString().toIntOrNull()
        if (classYear == null || classYear < 1900 || classYear > 2100) {
            Toast.makeText(this, "Please enter a valid class year.", Toast.LENGTH_LONG).show()
            return
        }

        val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
        val permanentImgFile = File(getExternalFilesDir(null), profileImgFileName)

        if (tempImgFile.exists()) {
            tempImgFile.copyTo(permanentImgFile, overwrite = true)
            tempImgFile.delete()
        }

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
            putString(KEY_NAME, nameEditText.text.toString())
            putString(KEY_EMAIL, emailEditText.text.toString())
            putString(KEY_PHONE, phoneEditText.text.toString())
            putString(KEY_MAJOR, majorEditText.text.toString())
            putInt(KEY_CLASS, classYear)
            putInt(KEY_GENDER, genderRadioGroup.checkedRadioButtonId)
        }

        Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show()

        finish()
    }

    private fun setImageSelection(savedInstanceState: Bundle?) {
        Util.checkPermissions(this)

        myViewModel = ViewModelProvider(this)[MyViewModel::class.java]

        val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
        tempImgUri = FileProvider.getUriForFile(this, "sfu.namnguyen.myruns", tempImgFile)

        cameraResult = registerForActivityResult(StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = Util.getBitmap(this, tempImgUri)
                myViewModel.userImage.value = bitmap
            }
        }

        galleryResult = registerForActivityResult(StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    contentResolver.openInputStream(selectedImageUri)?.use { inputStream ->
                        contentResolver.openOutputStream(tempImgUri)?.use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    val bitmap = Util.getBitmap(this, tempImgUri)
                    myViewModel.userImage.value = bitmap
                }
            }
        }

        myViewModel.userImage.observe(this) { it ->
            imageView.setImageBitmap(it)
        }

        val permanentImgFile = File(getExternalFilesDir(null), profileImgFileName)

        if (savedInstanceState == null && tempImgFile.exists()) {
            tempImgFile.delete()
        }

        if (tempImgFile.exists()) {
            val bitmap = Util.getBitmap(this, tempImgUri)
            imageView.setImageBitmap(bitmap)
        } else if (permanentImgFile.exists()) {
            permanentImgFile.copyTo(tempImgFile, overwrite = true)
            val bitmap = Util.getBitmap(this, tempImgUri)
            myViewModel.userImage.value = bitmap
        }
    }

    private fun launchCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
    }

    private fun launchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }

    private fun showPhotoSelectionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Change Profile Photo")
        builder.setItems(options) { dialog: DialogInterface, which: Int ->
            when (which) {
                0 -> launchCameraIntent()
                1 -> launchGalleryIntent()
            }
            dialog.dismiss()
        }
        builder.show()
    }

    // Reference: https://developer.android.com/guide/fragments/appbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        return when (item.itemId) {
            R.id.action_settings -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}