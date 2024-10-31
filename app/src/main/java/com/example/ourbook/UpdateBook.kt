package com.example.ourbook

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.ourbook.databinding.ActivityUpdateBookBinding
import java.io.ByteArrayOutputStream
import java.util.Calendar

class UpdateBook : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBookBinding
    private lateinit var db: DatabaseHelper
    private var bookId: Int = -1
    private var photoUri: Uri? = null
    private val CAMERA_REQUEST_CODE = 0
    private val GALLERY_REQUEST_CODE = 1

    // Registering for crop image result
    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri: Uri? = result.uriContent
            uri?.let {
                binding.UpdateFoto.setImageURI(it)
                photoUri = it // Set URI of the cropped photo
            }
        } else {
            Toast.makeText(this, "Image cropping failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        // Get the book ID from Intent
        bookId = intent.getIntExtra("book_id", -1)
        if (bookId == -1) {
            finish()
            return
        }

        // Get the book data based on ID and fill in the fields
        val book = db.getAllBooks().find { it.id == bookId }
        book?.let {
            binding.UpdateFoto.setImageURI(it.photoUri?.let { Uri.parse(it) }) // Set existing photo URI
            binding.UpdateNama.setText(it.name)
            binding.UpdateNP.setText(it.nickname)
            binding.UpdateEmail.setText(it.email)
            binding.UpdateAlamat.setText(it.address)
            binding.UpdateTgLahir.setText(it.birthdate)
            binding.UpdateHp.setText(it.phone)
            photoUri = it.photoUri?.let { Uri.parse(it) } // Set existing photo URI
        }

        // Set up the photo update button
        binding.UpdateFoto.setOnClickListener {
            selectPhoto()
        }

        // Save button listener
        binding.saveButton.setOnClickListener {
            saveData()
        }

        binding.UpdateTgLahir.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    binding.UpdateTgLahir.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun saveData() {
        val newName = binding.UpdateNama.text.toString()
        val newNickname = binding.UpdateNP.text.toString()
        val newEmail = binding.UpdateEmail.text.toString()
        val newAddress = binding.UpdateAlamat.text.toString()
        val newBirthdate = binding.UpdateTgLahir.text.toString()
        val newPhone = binding.UpdateHp.text.toString()

        // Update data in the database
        val updated = db.updateData(
            bookId,
            newName,
            newNickname,
            newEmail,
            newAddress,
            newBirthdate,
            newPhone,
            photoUri.toString() // Convert URI to string for database
        )

        if (updated) {
            Toast.makeText(this, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectPhoto() {
        val options = arrayOf("Camera", "Gallery")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Photo")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera() // Camera option
                1 -> openGallery() // Gallery option
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
                    selectedImageUri?.let {
                        launchCropImage(it)
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
