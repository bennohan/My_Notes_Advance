package com.bennohan.mynotes.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseFragment
import com.bennohan.mynotes.database.OpenNavigation
import com.bennohan.mynotes.database.constant.Const
import com.bennohan.mynotes.database.note.Note
import com.bennohan.mynotes.database.user.UserDao
import com.bennohan.mynotes.databinding.FragmentHomeBinding
import com.bennohan.mynotes.databinding.ItemNoteBinding
import com.bennohan.mynotes.ui.addNote.NoteActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.adapter.ReactiveListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val viewModel by activityViewModels<HomeViewModel>()

    @Inject
    lateinit var userDao: UserDao

    private var dataNote = ArrayList<Note?>()



    private val adapterNote by lazy {
        object : ReactiveListAdapter<ItemNoteBinding, Note>(R.layout.item_note) {
            override fun onBindViewHolder(
                holder: ItemViewHolder<ItemNoteBinding, Note>,
                position: Int
            ) {
                super.onBindViewHolder(holder, position)
                val item = getItem(position)

                val colorList = listOf(R.color.red_note, R.color.greenNote,  R.color.purple_note)


                item?.let { itm ->
                    holder.binding.data = itm
                    holder.bind(itm)

                    val colorIndex = position % colorList.size
                    val color = colorList[colorIndex]

                    holder.binding.constraint.setBackgroundResource(color)


                    holder.binding.constraint.setOnClickListener {
                        val intent = Intent(requireContext(), NoteActivity::class.java)
                        intent.putExtra(Const.NOTE.ID_NOTE, itm.id)

                        //Send Intent Background Color
                        intent.putExtra("COLOR_EXTRA",color)
                        (requireActivity() as NavigationActivity).activityLauncher.launch(intent) {
                            // IF Result
                            if (it.resultCode == 6100) {
                                getNote()
                                observe()
                            }
                        }
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



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getNote()
        observe()
        search()
        binding?.rvNote?.adapter = adapterNote


        binding?.btnMenu?.setOnClickListener {
            EventBus.getDefault().post(OpenNavigation())
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

                if (filter.isEmpty()) {
                    binding!!.tvNoteNotFound.visibility = View.VISIBLE
                } else {
                    binding!!.tvNoteNotFound.visibility = View.GONE
                }
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
                        adapterNote.submitList(listNote)
                        dataNote.clear()
                        dataNote.addAll(listNote)
                    }
                }
            }
        }
    }

}