package com.example.ourbook

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ourbook.databinding.ActivityAddBookBinding
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import java.io.ByteArrayOutputStream
import java.util.Calendar

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding
    private lateinit var myDb: DatabaseHelper
    private var photoUri: Uri? = null // Ganti variabel photo
    private var CAMERA_REQUEST_CODE = 0
    private var GALLERY_REQUEST_CODE = 1

    // Registering for crop image result
    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            photoUri = result.uriContent // Simpan URI hasil crop
            binding.addfoto.setImageURI(photoUri)
        } else {
            Toast.makeText(this, "Image cropping failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myDb = DatabaseHelper(this)

        // Save button listener
        binding.saveButton.setOnClickListener {
            showSaveConfirmationDialog()
        }

        // Button to select photo
        binding.addfoto.setOnClickListener {
            selectPhoto()
        }

        binding.addTglLahir.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    binding.addTglLahir.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun showSaveConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Simpan")
            .setMessage("Apakah Anda yakin ingin menyimpan data ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                saveData()
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveData() {
        val name = binding.addNama.text.toString()
        val nickname = binding.addNP.text.toString()
        val email = binding.addEmail.text.toString()
        val address = binding.addAlamat.text.toString()
        val birthdate = binding.addTglLahir.text.toString()
        val phone = binding.addHP.text.toString()

        // Simpan URI foto ke database
        val isInserted = myDb.insertData(name, nickname, email, address, birthdate, phone, photoUri.toString())
        if (isInserted) {
            Toast.makeText(this, "Data added successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@AddBookActivity, MainActivity::class.java))
        } else {
            Toast.makeText(this, "Error adding data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectPhoto() {
        val options = arrayOf("Camera", "Gallery")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Photo")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val selectedPhoto = data?.extras?.get("data") as Bitmap
                    val uri = getImageUri(selectedPhoto)
                    launchCropImage(uri)
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        launchCropImage(selectedImageUri)
                    }
                }
            }
        }
    }

    private fun launchCropImage(uri: Uri) {
        cropImageLauncher.launch(
            CropImageContractOptions(uri, CropImageOptions())
        )
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "TempImage", null)
        return Uri.parse(path)
    }
}
