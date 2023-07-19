package com.bennohan.mynotes.ui.favouriteNote

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseActivity
import com.bennohan.mynotes.database.constant.Const
import com.bennohan.mynotes.database.note.Note
import com.bennohan.mynotes.databinding.ActivityFavouriteBinding
import com.bennohan.mynotes.databinding.ItemNoteBinding
import com.bennohan.mynotes.ui.addNote.NoteActivity
import com.crocodic.core.base.adapter.ReactiveListAdapter
import com.crocodic.core.extension.createIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavouriteActivity :
    BaseActivity<ActivityFavouriteBinding, FavouriteViewModel>(R.layout.activity_favourite) {

    //Adapter for Note List
    private val adapterNote by lazy {
        ReactiveListAdapter<ItemNoteBinding, Note>(R.layout.item_note).initItem { position, data ->
            val intent = Intent(this, NoteActivity::class.java)
            intent.putExtra(Const.NOTE.ID_NOTE, data.id)
            activityLauncher.launch(createIntent<NoteActivity>()) {
                intent.putExtra(Const.NOTE.ID_NOTE, data.id)
                if (it.resultCode == 6100) {
                    getNoteFavourite()
                    observe()
                }
            }


        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getNoteFavourite()
        observe()
        binding.rvNote.adapter = adapterNote

        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    private fun getNoteFavourite() {
        viewModel.getNote()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.listNote.collectLatest { listNote ->
                        val filterNote = listNote.filter { it?.favorite == true }
                        adapterNote.submitList(filterNote)
                    }
                }
            }
        }
    }

}