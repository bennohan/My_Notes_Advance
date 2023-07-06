package com.bennohan.mynotes.ui.categoryNote

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseActivity
import com.bennohan.mynotes.database.Categories
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.database.Note
import com.bennohan.mynotes.databinding.ActivityCategoryBinding
import com.bennohan.mynotes.databinding.ItemCategoryBinding
import com.bennohan.mynotes.databinding.ItemNoteBinding
import com.bennohan.mynotes.ui.addNote.NoteActivity
import com.bennohan.mynotes.ui.home.NavigationActivity
import com.bennohan.mynotes.ui.login.LoginActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.adapter.ReactiveListAdapter
import com.crocodic.core.extension.openActivity
import com.crocodic.core.extension.snacked
import com.crocodic.core.extension.textOf
import com.crocodic.core.extension.tos
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryActivity :
    BaseActivity<ActivityCategoryBinding, CategoryViewModel>(R.layout.activity_category) {

    private var dataCategories = ArrayList<Categories?>()
    private var idCategories: String? = null


    //TODO TEST GETCATEGORIES SUCCESS
    private val adapterCategory by lazy {
        object : ReactiveListAdapter<ItemCategoryBinding, Categories>(R.layout.item_category) {
            override fun onBindViewHolder(
                holder: ItemViewHolder<ItemCategoryBinding, Categories>,
                position: Int,
            ) {
                super.onBindViewHolder(holder, position)
                val item = getItem(position)

                item?.let { itm ->
                    holder.binding.data = itm
                    holder.bind(itm)

                    holder.binding.btnForward.setOnClickListener {
                        dialogEditCategories(itm.id)
                    }

                    holder.binding.btnDeleteCategories.setOnClickListener {
                        deleteCategories(itm.id)
                    }


                }

            }

        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observe()
        search()
        getCategories()

        binding.rvCategory.adapter = adapterCategory

        binding.btnAddCategory.setOnClickListener {
            createCategories()
        }
        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    private fun getCategories() {
        viewModel.getCategory()
    }

    private fun createCategories() {
        val categories = binding.etCategory.textOf()
        viewModel.createCategory(categories)
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show()
                            ApiStatus.SUCCESS -> {
                                loadingDialog.dismiss()
                                getCategories()
                                binding.root.snacked("Success")
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
                        adapterCategory.submitList(data)
                        Log.d("cek categories", dataCategories.toString())
                        Log.d("cek it", data.toString())

                    }
                }
            }
        }

    }

    private fun deleteCategories(categoriesId: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Category")
        builder.setMessage("Apakah Anda yakin ingin menghapus categori ini?")
            .setCancelable(false)
            .setPositiveButton("Delete") { _, _ ->
                // Delete selected note from database
                viewModel.deleteCategory(categoriesId)
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

    private fun dialogEditCategories(categoriesId: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Categories")

// Set the EditText field
        val inputEditText = EditText(this)
        builder.setView(inputEditText)

// Set the positive button ("Yes")
        builder.setPositiveButton("Yes") { dialog, _ ->
            val enteredText = inputEditText.text.toString()
            // Perform actions based on the entered text
            viewModel.editCategory(categoriesId, enteredText)
            dialog.dismiss()
        }

// Set the negative button ("No")
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, com.crocodic.core.R.color.text_green))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.black))
        }

    }

    private fun search() {
        binding.etCategory.doOnTextChanged { text, _, _, _ ->
            if (text!!.isNotEmpty()) {
                val filter = dataCategories.filter {
                    it?.category?.contains("$text",
                        true) == true
                }
                adapterCategory.submitList(filter)

//                if (filter == null){
//                    binding.btnAddCategory.visibility = View.VISIBLE
//                } else {
//                    binding.btnAddCategory.visibility = View.GONE
//                }

            } else {
                adapterCategory.submitList(dataCategories)
            }
        }
    }

}