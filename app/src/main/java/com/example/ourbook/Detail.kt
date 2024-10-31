package com.example.ourbook

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ourbook.databinding.ActivityDetailBinding

class Detail : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        val bookId = intent.getIntExtra("book_id", -1)

        if (bookId != -1) {
            val book = db.getBookById(bookId)

            // Menggunakan let untuk memastikan `book` tidak null sebelum mengakses propertinya
            book?.let {
                binding.Nama.text = it.name
                binding.NP.text = it.nickname
                binding.email.text = it.email
                binding.alamat.text = it.address
                binding.tglLahir.text = it.birthdate
                binding.telp.text = it.phone

                if (it.photoUri != null) {
                    binding.foto.setImageURI(book.photoUri?.let { Uri.parse(book.photoUri) })
                } else {
                    binding.foto.setImageResource(R.drawable.baseline_person_24)
                }
            } ?: run {
                // Jika `book` null, tampilkan pesan atau lakukan tindakan lain sesuai kebutuhan
                binding.Nama.text = "Data tidak ditemukan"
            }
        }

        binding.adios.setOnClickListener {
            finish() // Menggunakan finish() untuk kembali ke activity sebelumnya tanpa Intent
        }
    }
}
