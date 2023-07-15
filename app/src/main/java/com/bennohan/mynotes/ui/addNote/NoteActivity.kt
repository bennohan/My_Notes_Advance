package com.bennohan.mynotes.ui.addNote

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseActivity
import com.bennohan.mynotes.database.Categories
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.database.Note
import com.bennohan.mynotes.databinding.ActivityNoteBinding
import com.bennohan.mynotes.helper.ViewBindingHelper.Companion.writeBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.*
import com.crocodic.core.helper.BitmapHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
@AndroidEntryPoint
class NoteActivity : BaseActivity<ActivityNoteBinding, NoteViewModel>(R.layout.activity_note) {

    //Data
    private var dataCategories = ArrayList<Categories?>()
    private var dataNote: Note? = null
    private var idCategories: String? = null
    private var filePhoto: File? = null

    //View for Keyboard
    private lateinit var rootView: View

    //For Redo Undo Button
    private lateinit var textWatcher: TextWatcher
    private lateinit var textHistory: MutableList<CharSequence>
    private var currentPosition = 0

    private val cameraOption: Array<String> = arrayOf("take from camera 1", "insert from gallery 2")
    private var isTextChangedByUser = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = findViewById(android.R.id.content)


        listCategory()
        observe()
        getNote()
//        keyboardTop()
        setDateTime()
        autocompleteSpinner()
        undoRedo()



        setupTextWatcher(binding.etTitle,binding.etContent)

        binding.btnShareNote.setOnClickListener {
            shareText(dataNote?.title, dataNote?.content)
        }

        binding.btnFavourite.setOnClickListener {
            favourite()
        }

        binding.btnPhoto.setOnClickListener {
            openPictureDialog()

        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnCheck.setOnClickListener {
            createUpdateNote()
        }

        binding.btnDelete.setOnClickListener {
            deleteNote()
        }

    }


    private fun listCategory() {
        viewModel.getCategory()
    }


    private fun undoRedo() {
        textHistory = mutableListOf()
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Add the current text to the history
                if (s != null) {
                    textHistory.add(s.toString())
                    currentPosition = textHistory.size - 1
                }
            }
        }

        binding.etContent.addTextChangedListener(textWatcher)

        binding.btnUndo.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                binding.etContent.removeTextChangedListener(textWatcher)
                binding.etContent.text = SpannableStringBuilder.valueOf(textHistory[currentPosition])
                binding.etContent.addTextChangedListener(textWatcher)
            }
        }

        binding.btnRedo.setOnClickListener {
            if (currentPosition < textHistory.size - 1) {
                currentPosition++
                binding.etContent.removeTextChangedListener(textWatcher)
                binding.etContent.text = SpannableStringBuilder.valueOf(textHistory[currentPosition])
                binding.etContent.addTextChangedListener(textWatcher)
            }
        }
    }


    private fun deleteNote() {

            val builder = AlertDialog.Builder(this@NoteActivity)
            builder.setTitle("Delete Note")
            builder.setMessage("Apakah anda yakin akan menghapus Note ini")
                .setCancelable(false)
                .setPositiveButton("Delete") { _, _ ->
                    // Delete selected note from database
                    val idNote = dataNote?.id
                    viewModel.deleteNote(idNote)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            val dialog: AlertDialog = builder.create()

            // Set the color of the positive button text
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, com.crocodic.core.R.color.text_red))
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.black))
            }

            // Show the dialog
            dialog.show()

    }


    private fun keyboardTop() {
        val viewTreeObserver = rootView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            // Calculate the height difference between the root view and the visible display frame
            val heightDiff = rootView.height - rect.height()

            // Check if the height difference is above a certain threshold to determine if the keyboard is visible
            val keyboardVisible = heightDiff > (rootView.height * 0.15)

            if (keyboardVisible) {
                // Keyboard is visible, adjust the position of the id/WYSIWYG view here
                val layoutParams = binding.WYSIWYG.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                binding.WYSIWYG.layoutParams = layoutParams
            } else {
                // Keyboard is not visible, reset the position of the id/WYSIWYG view here
                val layoutParams = binding.WYSIWYG.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topToBottom =
                    R.id.etContent // Set the correct anchor for the top constraint
                binding.WYSIWYG.layoutParams = layoutParams
            }
        }

    }



    private fun shareText(text1: String?, text2: String?) {
        val space = "\n"
        val combinedText = text1 + space + text2

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, combinedText)
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun getNote() {
        val id = intent.getStringExtra(Const.NOTE.ID_NOTE)
        val colorNote = intent.getIntExtra("COLOR_EXTRA", R.color.white)
        binding.constraint.setBackgroundResource(colorNote)
        id?.let { viewModel.getNote(it) }
    }

    private fun getCategories() {
        val idCategories = dataNote?.categoriesId
        //for Get Categories by id Name
        idCategories?.let { viewModel.getCategoriesById(it) }

    }

    private fun favourite() {
        val idNote = dataNote?.id
        if (dataNote?.favorite == true) {
            viewModel.unFavouriteNote(idNote)
            setResult(6100)
        } else {
            viewModel.favouriteNote(idNote)
            setResult(6100)
        }

    }


    private fun createUpdateNote() {
        val idNote = dataNote?.id
        val title = binding.etTitle.textOf()
        val content = binding.etContent.textOf()
        val categories = idCategories
        val photo = filePhoto

        if (title.isEmpty() || content.isEmpty()) {
            //Return nothing if the title and the content is empty
            return
        } else {
            if (dataNote == null) {
                //If dataNote is null then it create new Note
                    if (photo != null) {
                        //Check if it contains photo or not
                        viewModel.createNotePhoto(title, content, categories,photo)
                        return
                    } else {
                        viewModel.createNote(title, content, categories)
                        return
                    }
            }else{
                //If dataNote is not empty then it update exist Note
                if (photo != null) {
                    //Check if it contains photo or not
                    viewModel.editNotePhoto(idNote,title, content, categories,photo)
                    return
                } else {
                    viewModel.editNote(idNote,title, content, categories)
                    return
                }
            }
        }

    }

    //Set the base Current Date
    private fun setDateTime() {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        // Define the date and time format
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Format the current date and time to strings
        val formattedDate = dateFormat.format(currentDate)
        val formattedTime = timeFormat.format(currentDate)
        val combinedText = "$formattedDate $formattedTime"

        // Display the date and time in your UI or perform any desired action
            binding.tvDate.text = combinedText

    }


    //TODO LOADING DIALOG
    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show()
                            ApiStatus.SUCCESS -> {
                                when (it.message) {
                                    "Note Created" -> {
                                        tos("Note Created")
                                    }
                                    "Note Edited" -> {
                                        tos("Note Edited")
                                    }
                                    "Favourite" -> {
                                        //Update Note later after note favoured
                                        dataNote?.id?.let { it1 -> viewModel.getNote(it1) }
                                        binding.root.snacked("Note Favoured")
                                    }
                                    "UnFavourite" -> {
                                        //Update Note later after note unfavoured
                                        dataNote?.id?.let { it1 -> viewModel.getNote(it1) }
                                    binding.root.snacked("Note UnFavoured")
                                    }
                                    "Note Deleted" -> {
                                        finish()
                                        setResult(6100)
                                        tos("Note Deleted")
                                    }
                                }
                                loadingDialog.dismiss()
                                setResult(6100)
                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                loadingDialog.setResponse(it.message ?: return@collect)
                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)
                        }
                    }
                }
                launch {
                    viewModel.listCategory.collect { data ->
                        dataCategories.addAll(data)

                    }
                }
                launch {
                    viewModel.dataNote.collect { data ->
                        binding.data = data
                        dataNote = data

                        //Set Note Date Base on Latest Note was Updated
                        binding.tvDate.text = data?.updatedAtFormatted

                        if (data?.photo == null){
                            //TODO TEST VISIBILITY KALO DI ADD IMAGE
                            binding.imageView.visibility = View.GONE
                        } else {
                            binding.imageView.visibility = View.VISIBLE
                        }


                    }
                }
                launch {
                    viewModel.categoriesName.collect { data ->
                        binding.dropdownCategories.setText(data?.category)
                    }
                }
            }
        }
    }

    private fun openPictureDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setItems(cameraOption) { _, which ->
                // The 'which' argument contains the index position of the selected item
                when (which) {
                    0 -> activityLauncher.openCamera(this@NoteActivity, "${packageName}.fileprovider") { file, _ ->
                        uploadAvatar(file)
                    }
                    1 -> activityLauncher.openGallery(this@NoteActivity) { file, _ ->
                        uploadAvatar(file)
                    }
                }
            }
        }.create().show()
    }

    private fun uploadAvatar(file: File?) {
        if (file == null) {
            binding.root.snacked("error")
            return
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val resizeBitmap = BitmapHelper.resizeBitmap(bitmap, 512f)

        file.delete()

        //Result Photo
        val uploadFile = createImageFile().also { it.writeBitmap(resizeBitmap) }

        //Processing the photo result
        filePhoto = uploadFile

        Glide
            .with(this@NoteActivity)
            .load(uploadFile)
                    .placeholder(R.drawable.ic_baseline_person_24)
            .apply(RequestOptions.centerCropTransform())
            .into(binding.imageView)


    }

    private fun autocompleteSpinner() {

        val autoCompleteSpinner = findViewById<AutoCompleteTextView>(R.id.dropdownCategories)
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dataCategories)
        autoCompleteSpinner.setAdapter(adapter)
//        binding.dropdownCategories.setTextColor(ContextCompat.getColor(this, R.color.black))


        // Show the dropdown list when the AutoCompleteTextView is clicked
        autoCompleteSpinner.setOnClickListener {
            autoCompleteSpinner.showDropDown()
            autoCompleteSpinner.dropDownVerticalOffset = -autoCompleteSpinner.height

        }

        autoCompleteSpinner.setOnItemClickListener { _, _, position, _ ->
            // Handle item selection here
            val selectedItem = dataCategories[position]
            idCategories = selectedItem?.id


        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        createUpdateNote()
        val resultIntent = Intent()
        resultIntent.putExtra("result_code", 6100)
        setResult(6100, resultIntent)
        finish()
    }

    // Function to initialize the text watcher
    private fun setupTextWatcher(editText: EditText ,editText2: EditText) {

        //Edit text Title
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isTextChangedByUser) {
                    // Show Button Check and Delete when user is typing something
                    val updatedText = s.toString()
                    binding.btnDelete.visibility = View.VISIBLE
                    binding.btnCheck.visibility = View.VISIBLE
                } else {
                    isTextChangedByUser = true
                    binding.btnDelete.visibility = View.GONE
                    binding.btnCheck.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })

        //Edit text Content
        editText2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isTextChangedByUser) {
                    // Show Button Check and Delete when user is typing something
                    val updatedText = s.toString()
                    binding.btnDelete.visibility = View.VISIBLE
                    binding.btnCheck.visibility = View.VISIBLE
                } else {
                    isTextChangedByUser = true
                    binding.btnDelete.visibility = View.GONE
                    binding.btnCheck.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })

    }


}