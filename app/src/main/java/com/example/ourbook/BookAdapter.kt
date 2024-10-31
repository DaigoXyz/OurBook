package com.example.ourbook

import Profile
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.metrics.Event
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView

class BooksAdapter(private var book: List<Profile>, context: Context): RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {

    private val db: DatabaseHelper = DatabaseHelper(context)

    class BookViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nama : TextView = itemView.findViewById(R.id.nama)
        val foto : ImageView = itemView.findViewById(R.id.profile_image)
        val updateButton : ImageView = itemView.findViewById(R.id.btnedit)
        val deleteButton : ImageView = itemView.findViewById(R.id.btndelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = book.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = book[position]
        holder.nama.text = book.name

        if(book.photoUri != null){
            holder.foto.setImageURI(book.photoUri?.let { Uri.parse(book.photoUri) })
        }else{
            holder.foto.setImageResource(R.drawable.baseline_insert_photo_24)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, Detail::class.java).apply {
                putExtra("book_id", book.id)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.updateButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateBook::class.java).apply {
                putExtra("book_id", book.id)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            delete(holder, position)
        }
    }

    fun refreshData(newBooks : List<Profile>) {
        book = newBooks
        notifyDataSetChanged()
    }

    private fun delete(holder: BookViewHolder, position: Int) {
        val book = book[position]
        var builder = AlertDialog.Builder(holder.itemView.context)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure want to delete this?")
        builder.setPositiveButton("YES", DialogInterface.OnClickListener{ dialog, id ->
            db.deleteData(book.id)
            refreshData(db.getAllBooks())
            dialog.cancel()
            Toast.makeText(holder.itemView.context, "Book Deleted", Toast.LENGTH_SHORT).show()
        })
        builder.setNegativeButton("NO", DialogInterface.OnClickListener{ dialog, id ->
            dialog.cancel()
        })
        var alert : AlertDialog = builder.create()
        alert.show()
    }
}