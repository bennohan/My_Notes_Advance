package com.bennohan.mynotes.ui.addNote

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
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
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.snacked
import com.crocodic.core.extension.textOf
import com.crocodic.core.extension.tos
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
//TODO BTN UNDO REDO
class NoteActivity : BaseActivity<ActivityNoteBinding, NoteViewModel>(R.layout.activity_note) {

    private var dataCategories = ArrayList<Categories?>()
    private var dataNote: Note? = null
    private var idCategories: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listCategory()
        observe()
        getNote()
//        autocompleteSpinner()

        binding.btnBack.setOnClickListener {
            finish()
        }

//        binding.btnShareNote.setOnClickListener {
//            shareText(dataNote?.title, dataNote?.content)
//        }
//
//        binding.btnFavourite.setOnClickListener {
//            favourite()
//        }
//
//        binding.btnUndo.setOnClickListener {
//            undo()
//        }
//        binding.btnRedo.setOnClickListener {
//            redo()
//        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnCheck.setOnClickListener {
            createUpdateNote()
        }

//        val editText = findViewById<EditText>()
        val undoStack = mutableListOf<String>()
        val redoStack = mutableListOf<String>()

//        editText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                // Save the current text to the undo stack
//                undoStack.add(s.toString())
//            }
//        })
    }

    private fun listCategory() {
        viewModel.getCategory()
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
        if (id != null) {
            Log.d("cek id receive", id)
            viewModel.getNote(id)
        }
    }

    private fun getCategories() {
        var idid = dataNote?.categoriesId
        if (idid != null) {
            viewModel.getCategoriesById(idid)
            Log.d("cek categories", idid)

        }
    }

    private fun favourite() {
        val idNote = dataNote?.id
        if (dataNote?.favorite == true) {
            viewModel.unFavouriteNote(idNote)
//            setResult(6100)
        } else {
            viewModel.favouriteNote(idNote)
//            setResult(6100)
        }

    }


    private fun createUpdateNote() {
        val idNote = dataNote?.id
//        val title = binding.etTitle.textOf()
//        val content = binding.etContent.textOf()
        val categories = idCategories

//        if (dataNote == null) {
//            if (title.isEmpty() || content.isEmpty()) {
//                return
//            } else {
//                viewModel.createNote(title, content, categories)
//            }
//        } else {
////            tos("$categories")
//            viewModel.editNote(idNote,title,content,categories)
//        }

    }

    private fun date() {
        val originalDateString = dataNote?.updatedAt
        if (originalDateString != null) {
            val originalFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            val date = originalFormat.parse(originalDateString)

            val targetFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = targetFormat.format(date)

//            binding.tvDate.text = formattedDate

        }
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
//                                when (it.message) {
//                                    "Favourite" -> tos("liked")
//                                    "UnFavourite" -> tos("UnFavourite")
//                                }
                                if (it.message == "Favourite") {
                                    viewModel.getNote(dataNote?.id)
                                    binding.root.snacked("Note Favourited")
                                }
                                if (it.message == "UnFavourite") {
                                    viewModel.getNote(dataNote?.id)
                                    binding.root.snacked("Note UnFavourited")
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
                        Log.d("cek categories", dataCategories.toString())
                        Log.d("cek it", data.toString())

                    }
                }
                launch {
                    viewModel.dataNote.collect { data ->
                        binding.data = data
                        dataNote = data
                        getCategories()
                        date()


//                        if (data?.photo == null){
//                            binding.imageView.visibility = View.GONE
//                        } else {
//                            binding.imageView.visibility = View.VISIBLE
//                        }


                        Log.d("cek note", dataNote.toString())

                    }
                }
                launch {
                    viewModel.categoriesName.collect { data ->
//                        binding.dropdownCategories.setText(data?.category)
                    }
                }
            }
        }
    }

//    private fun autocompleteSpinner() {
//
//        val autoCompleteSpinner = findViewById<AutoCompleteTextView>(R.id.dropdownCategories)
//        val adapter =
//            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dataCategories)
//        autoCompleteSpinner.setAdapter(adapter)
////        binding.dropdownCategories.setTextColor(ContextCompat.getColor(this, R.color.black))
//
//
//        // Show the dropdown list when the AutoCompleteTextView is clicked
//        autoCompleteSpinner.setOnClickListener {
//            autoCompleteSpinner.showDropDown()
//            autoCompleteSpinner.dropDownVerticalOffset = -autoCompleteSpinner.height
//
//        }
//
//        autoCompleteSpinner.setOnItemClickListener { _, _, position, _ ->
//            // Handle item selection here
//            val selectedItem = dataCategories[position]
//            idCategories = selectedItem?.id
//
//
//        }
//
//    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        createUpdateNote()
        val resultIntent = Intent()
        resultIntent.putExtra("result_code", 6100)
        setResult(6100, resultIntent)
        finish()
    }


}