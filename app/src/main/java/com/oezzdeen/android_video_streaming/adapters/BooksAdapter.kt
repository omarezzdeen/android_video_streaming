package com.oezzdeen.android_video_streaming.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oezzdeen.android_video_streaming.databinding.CellBookItemBinding
import com.oezzdeen.android_video_streaming.listener_event.OnEditBookListener
import com.oezzdeen.android_video_streaming.listener_event.OnPreviewVideoListener
import com.oezzdeen.android_video_streaming.model.Book
import java.text.SimpleDateFormat
import java.util.*

class BooksAdapter(
    private val onEditBookListener: OnEditBookListener,
    private val onPreviewVideoListener: OnPreviewVideoListener
) :
    RecyclerView.Adapter<BooksAdapter.BooksViewHolder>() {

    private var booksList = ArrayList<Book>()

    fun setBooksList(list: ArrayList<Book>) {
        booksList = list
        notifyDataSetChanged()
    }

    inner class BooksViewHolder(val binding: CellBookItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val root = CellBookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BooksViewHolder(root)
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        val book = booksList[position]
        holder.binding.tvBookID.text = "${holder.adapterPosition + 1}"
        holder.binding.tvBookName.text = book.bookName
        holder.binding.tvBookAuthor.text = book.author

        val milliseconds = book.launchYear?.time!!
        val formatter = SimpleDateFormat("yyyy", Locale.ENGLISH)
        val mLaunchYear = formatter.format(milliseconds)
        holder.binding.tvBookLaunchYear.text = mLaunchYear
        holder.binding.tvBookPrice.text = "$ ${book.price}"
        holder.binding.rbRatingBookBar.rating = book.rating
        holder.binding.tvRatingBook.text = book.rating.toString()

        holder.binding.btnEditBook.setOnClickListener {
            onEditBookListener.onEditBookListener(book = book)
        }

        holder.binding.btnPreview.setOnClickListener {
            onPreviewVideoListener.onPreviewVideoListener(url = book.videoUrl)
        }
    }

    override fun getItemCount(): Int {
        return booksList.size
    }
}