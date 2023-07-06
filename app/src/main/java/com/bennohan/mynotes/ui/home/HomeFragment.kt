package com.bennohan.mynotes.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.doOnTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseFragment
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.database.Note
import com.bennohan.mynotes.database.UserDao
import com.bennohan.mynotes.databinding.FragmentHomeBinding
import com.bennohan.mynotes.databinding.ItemNoteBinding
import com.bennohan.mynotes.ui.addNote.NoteActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.adapter.ReactiveListAdapter
import com.crocodic.core.extension.openActivity
import com.crocodic.core.extension.snacked
import com.crocodic.core.extension.tos
import com.crocodic.core.helper.ImagePreviewHelper
import com.crocodic.core.helper.log.Log
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
//TODO GAMBAR DI ITEM NOTE
//TODO PINDAH GAMBAR
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val viewModel by activityViewModels<HomeViewModel>()

    @Inject
    lateinit var userDao: UserDao
    private var categories: String? = null
    private var dataNote = ArrayList<Note?>()


//    //Adapter for Note List
//    private val adapterNote by lazy {
//        ReactiveListAdapter<ItemNoteBinding, Note>(R.layout.item_note).initItem { position, data ->
//            val intent = Intent(requireContext(), NoteActivity::class.java)
//            intent.putExtra(Const.NOTE.ID_NOTE, data.id)
//            (requireActivity() as NavigationActivity).activityLauncher.launch(intent) {
//                // IF Result
//                if (it.resultCode == 6100) {
//                    getNote()
//                    observe()
//                }
//            }
//        }
//
//
//    }

    private val adapterNote by lazy {
        object : ReactiveListAdapter<ItemNoteBinding, Note>(R.layout.item_note) {
            override fun onBindViewHolder(
                holder: ItemViewHolder<ItemNoteBinding, Note>,
                position: Int
            ) {
                super.onBindViewHolder(holder, position)
                val item = getItem(position)

                item?.let { itm ->
                    holder.binding.data = itm
                    holder.bind(itm)

                    item.categoriesId?.let { viewModel.getCategoriesById(it) }
                    android.util.Log.d("cek item", item.categoriesId.toString())

                    holder.binding.tvCategory.setText(categories)

                    holder.binding.cardView.setOnClickListener {
                        val intent = Intent(requireContext(), NoteActivity::class.java)
                        intent.putExtra(Const.NOTE.ID_NOTE, itm.id)
                        (requireActivity() as NavigationActivity).activityLauncher.launch(intent) {
                            // IF Result
                            if (it.resultCode == 6100) {
                                getNote()
                                observe()
                            }
                        }
                    }

                    if (itm.photo.isEmpty()) {
                        holder.binding.imageNote.visibility = View.GONE
                    } else {
                        holder.binding.imageNote.visibility = View.VISIBLE
                    }

                    holder.binding.cardView.setOnClickListener {
                        val intent = Intent(requireContext(), NoteActivity::class.java)
                        intent.putExtra(Const.NOTE.ID_NOTE, item.id)
//                        startActivity(intent)
                        startActivityForResult(intent, -1)

                    }


                }

            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getNote()
        observe()
        search()
        binding?.rvNote?.adapter = adapterNote


        binding?.btnMenu?.setOnClickListener {
            val activity = requireActivity() as NavigationActivity
            activity.sideMenu()
        }

    }

    //Search function for Notes
    private fun search() {
        binding?.etSearch?.doOnTextChanged { text, _, _, _ ->
            if (text!!.isNotEmpty()) {
                val filter = dataNote.filter {
                    it?.title?.contains(
                        "$text",
                        true
                    ) == true || it?.content?.contains("$text", true) == true
                }
                adapterNote.submitList(filter)

//                if (filter.isEmpty()) {
//                    binding.tvDataKosong.visibility = View.VISIBLE
//                } else {
//                    binding.tvDataKosong.visibility = View.GONE
//                }
            } else {
                adapterNote.submitList(dataNote)
            }
        }
    }


    private fun getNote() {
        viewModel.getNote()
    }

    fun logout() {
        viewModel.logout()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> {}
                            ApiStatus.SUCCESS -> {}
                            ApiStatus.ERROR -> {
//                                disconnect(it)
//                                loadingDialog.setResponse(it.message ?: return@collect)

                            }
                            else -> {}
                        }

                    }
                }
                launch {
                    //TODO The different between collect and collect latest
                    viewModel.listNote.collectLatest { listNote ->
                        android.util.Log.d("cek list observe", listNote.toString())
                        adapterNote.submitList(listNote)
                        dataNote.clear()
                        dataNote.addAll(listNote)


                    }
                }
                launch {
                    viewModel.categoriesName.collect { data ->
                        categories = data.toString()
                        android.util.Log.d("cek categories", categories!!)
                    }
                }
            }
        }
    }

}