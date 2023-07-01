package com.bennohan.mynotes.ui.favouriteNote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseActivity
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.database.Note
import com.bennohan.mynotes.databinding.ActivityFavouriteBinding
import com.bennohan.mynotes.databinding.ItemNoteBinding
import com.bennohan.mynotes.ui.addNote.NoteActivity
import com.bennohan.mynotes.ui.home.NavigationActivity
import com.crocodic.core.base.adapter.ReactiveListAdapter
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
//            (requireActivity() as NavigationActivity).activityLauncher.launch(intent) {
//                // IF Result
//                if (it.resultCode == 6100) {
//                    getNote()
//                    observe()
//                }
//            }
        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getNoteFavourite()
        observe()
        binding.rvNote.adapter = adapterNote

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