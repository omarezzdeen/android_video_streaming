package com.oezzdeen.android_video_streaming.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev_fawzi.cc_assignment4.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.oezzdeen.android_video_streaming.R
import com.oezzdeen.android_video_streaming.adapters.BooksAdapter
import com.oezzdeen.android_video_streaming.databinding.FragmentHomeBinding
import com.oezzdeen.android_video_streaming.listener_event.OnEditBookListener
import com.oezzdeen.android_video_streaming.listener_event.OnPreviewVideoListener
import com.oezzdeen.android_video_streaming.model.Book

class HomeFragment : Fragment(), OnEditBookListener, OnPreviewVideoListener {

    /*----------------------------------*/

    private val database by lazy { Firebase.database.reference }

    /*----------------------------------*/

    private lateinit var booksAdapter: BooksAdapter
    private var booksList = ArrayList<Book>()

    /*----------------------------------*/

    private val mTAG: String = "_HomeFragment"
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*----------------------------------*/

        booksAdapter = BooksAdapter(onEditBookListener = this, onPreviewVideoListener = this)

        /*----------------------------------*/

        getAllBooks()

        /*----------------------------------*/

        binding.BooksRecyclerView.apply {
            this.adapter = booksAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            val decoration =
                DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
            addItemDecoration(decoration)
        }

        binding.btnAddBook.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addBookFragment)
        }
    }

    private fun getAllBooks() {
        database.child(Utils.Book_Collection).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                booksList.clear()

                for (doc in snapshot.children) {
                    val book = doc.getValue<Book>()
                    book?.let {
                        booksList.add(it)
                    }
                }

                Log.d(mTAG, "onDataChange: booksList => $booksList")
                booksAdapter.setBooksList(booksList)

                binding.ProgressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(mTAG, "getAllBooks: onCancelled => ${error.toException().message}")
                binding.ProgressBar.visibility = View.GONE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onEditBookListener(book: Book) {
        // This line for navigating and sending data
        HomeFragmentDirections.actionHomeFragmentToEditBookFragment(
            book
        ).also {
            findNavController().navigate(it)
        }
    }

    override fun onPreviewVideoListener(url: String) {
        // This line for navigating and sending data
        HomeFragmentDirections.actionHomeFragmentToPreviewVideoFragment(
            url
        ).also {
            findNavController().navigate(it)
        }
    }
}