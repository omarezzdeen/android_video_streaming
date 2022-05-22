package com.oezzdeen.android_video_streaming.ui.fragments
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dev_fawzi.cc_assignment4.utils.Utils
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.oezzdeen.android_video_streaming.R
import com.oezzdeen.android_video_streaming.databinding.FragmentAddBookBinding
import com.oezzdeen.android_video_streaming.fcm.api.RetrofitInstance
import com.oezzdeen.android_video_streaming.fcm.notif_model.NotificationData
import com.oezzdeen.android_video_streaming.fcm.notif_model.PushNotification
import com.oezzdeen.android_video_streaming.listener_event.OnVideoDownloadedUrl
import com.oezzdeen.android_video_streaming.listener_event.OnVideoPickedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class AddBookFragment : Fragment(), OnVideoPickedListener, OnVideoDownloadedUrl {

    /*----------------------------------*/

    private val database by lazy { Firebase.database.reference }
    private val storageRef by lazy { Firebase.storage.reference }

    /*----------------------------------*/

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var videoUri: Uri? = null

    /*----------------------------------*/

    private val mTAG: String = "_AddBookFragment"
    private var _binding: FragmentAddBookBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*----------------------------------*/

        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*----------------------------------*/

        setupPickVideo(this)

        /*----------------------------------*/

        binding.edBookVideoDemo.setOnClickListener {
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            activityResultLauncher.launch(Intent.createChooser(intent, "Select a file"))
        }

        binding.btnAddBook.setOnClickListener {
            checkFieldsThenAddNewBook()
        }
    }

    private fun checkFieldsThenAddNewBook() {
        val bookName = binding.edBookName.text.toString()
        val author = binding.edBookAuthor.text.toString()
        val launchYear = binding.edLaunchYear.text.toString()
        val price = binding.edPrice.text.toString()
        val rating = binding.RatingBook.rating

        when {
            bookName.isEmpty() -> {
                binding.edBookName.error = "add book name"
            }
            author.isEmpty() -> {
                binding.edBookAuthor.error = "add author"
            }
            launchYear.isEmpty() -> {
                binding.edLaunchYear.error = "add launch year"
            }
            price.isEmpty() -> {
                binding.edPrice.error = "add price"
            }
            rating == 0f -> {
                Toast.makeText(requireActivity(), "Rate The com.oezzdeen.android_video_streaming.model.Book", Toast.LENGTH_SHORT).show()
            }
            videoUri == null -> {
                Toast.makeText(requireActivity(), "Please Select com.oezzdeen.android_video_streaming.model.Book Cover", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                Log.d(mTAG, "checkFieldsThenAddNewBook: imageUri =>$videoUri")

                uploadBookVideoToStorage(this, videoUri!!)
            }
        }
    }

    private fun uploadBookVideoToStorage(
        onVideoDownloadedUrl: OnVideoDownloadedUrl,
        imgUri: Uri
    ) {
        storageRef.child(Utils.Books_Covers_Storage)
            .child("${UUID.randomUUID()}--${imgUri.lastPathSegment}")
            .putFile(imgUri)
            .addOnProgressListener {
                val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                Log.d(mTAG, "Upload is $progress% done")
            }.addOnPausedListener {
                Log.d(mTAG, "Upload is paused")
            }.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    onVideoDownloadedUrl.onVideoDownloadedUrl(it)
                    Log.d(mTAG, "uploadBookCoverToStorage: $it")
                }.addOnFailureListener {
                    Log.d(mTAG, "uploadBookCoverToStorage: exception => ${it.message}")

                    Toast.makeText(requireActivity(), "${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            }
    }

    private fun addNewBook(
        bookName: String,
        author: String,
        launchYear: Int,
        price: Double,
        rating: Float,
        videoUri: Uri
    ) {
        binding.ProgressBar.visibility = View.VISIBLE

        val dateString = launchYear.toString()
        val formatter = SimpleDateFormat("yyyy", Locale.ENGLISH)
        val mLaunchYear = formatter.parse(dateString) as Date

        val bookID = database.child(Utils.Book_Collection).push().key
        Log.d(mTAG, "addNewBook: bookID2 => $bookID")

        val book = hashMapOf(
            "bookID" to bookID,
            "bookName" to bookName,
            "author" to author,
            "launchYear" to mLaunchYear,
            "price" to price,
            "rating" to rating,
            "videoUrl" to videoUri.toString()
        )

        database.child("${Utils.Book_Collection}/$bookID").setValue(book)
            .addOnSuccessListener {
                binding.ProgressBar.visibility = View.GONE
                Toast.makeText(requireActivity(), "com.oezzdeen.android_video_streaming.model.Book Added Successfully", Toast.LENGTH_SHORT)
                    .show()

                val notificationData =
                    NotificationData(title = "Add New com.oezzdeen.android_video_streaming.model.Book", body = "$bookName Added Successfully")
                val pushNotification =
                    PushNotification(data = notificationData, to = Utils.TOPIC)

                sendNotification(pushNotification)

                Handler(Looper.getMainLooper()).postDelayed({

                    findNavController().popBackStack(
                        destinationId = R.id.homeFragment,
                        inclusive = false
                    )
                }, 1000)

            }
            .addOnFailureListener {
                Toast.makeText(requireActivity(), "${it.message}", Toast.LENGTH_SHORT).show()
                binding.ProgressBar.visibility = View.GONE
            }
    }

    // region Pick Video

    private fun setupPickVideo(onVideoPickedListener: OnVideoPickedListener) {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val intent: Intent? = result.data
                    val uri = intent?.data  //The uri with the location of the file
                    Log.d(mTAG, "uri => ${uri.toString()}")
                    val file = getFile(requireActivity(), uri!!)
                    val newUri = Uri.fromFile(file)
                    Log.d(mTAG, "newUri => $newUri")

                    onVideoPickedListener.onVideoPickedListener(newUri)
                }
            }

    }

    private fun getFile(context: Context, uri: Uri): File {
        val destinationFilename: File =
            File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                createFileFromStream(
                    inputStream!!,
                    destinationFilename
                )
            }
        } catch (ex: Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
        return destinationFilename
    }

    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { fileOutputStream ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    fileOutputStream.write(buffer, 0, length)
                }
                fileOutputStream.flush()
            }
        } catch (ex: Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    // endregion

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    RetrofitInstance.api.postNotification(pushNotification = notification)

                if (response.isSuccessful) {
//                    Log.d(mTAG, "sendNotification: ${Gson().toJson(response)}")
                    Log.d(mTAG, "sendNotification: Success")

                } else {
//                    Log.d(mTAG, "sendNotification: ${response.errorBody().toString()}")
                    Log.d(mTAG, "sendNotification: Failure")
                }
            } catch (exception: Exception) {
//                Log.d(mTAG, "sendNotification: ${exception.message}")
                Log.d(mTAG, "sendNotification: Error")

            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            findNavController().popBackStack(
                destinationId = R.id.homeFragment,
                inclusive = false
            )
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val callBack = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                findNavController().popBackStack(
                    destinationId = R.id.homeFragment,
                    inclusive = false
                )
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callBack)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onVideoPickedListener(uri: Uri?) {
        videoUri = uri
        Log.d(mTAG, "onImagePickedListener: $uri")
    }

    override fun onVideoDownloadedUrl(uri: Uri?) {
        val bookName = binding.edBookName.text.toString()
        val author = binding.edBookAuthor.text.toString()
        val launchYear = binding.edLaunchYear.text.toString()
        val price = binding.edPrice.text.toString()
        val rating = binding.RatingBook.rating

        addNewBook(
            bookName,
            author,
            launchYear.toInt(),
            price.toDouble(),
            rating,
            uri!!
        )
    }
}