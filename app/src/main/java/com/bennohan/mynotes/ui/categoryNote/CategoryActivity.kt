package com.bennohan.mynotes.ui.categoryNote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseActivity
import com.bennohan.mynotes.database.Categories
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.database.Note
import com.bennohan.mynotes.databinding.ActivityCategoryBinding
import com.bennohan.mynotes.databinding.ItemNoteBinding
import com.bennohan.mynotes.ui.addNote.NoteActivity
import com.bennohan.mynotes.ui.home.NavigationActivity
import com.crocodic.core.base.adapter.ReactiveListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryActivity :
    BaseActivity<ActivityCategoryBinding, CategoryViewModel>(R.layout.activity_category) {

    private var dataCategories = ArrayList<Categories?>()

    private val adapterCategory by lazy {
        ReactiveListAdapter<ItemNoteBinding, Categories>(R.layout.item_category).initItem { position, data ->
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observe()

        binding.rvCategory.adapter = adapterCategory

    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
}