package com.bennohan.mynotes.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseFragment
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.database.Note
import com.bennohan.mynotes.database.User
import com.bennohan.mynotes.database.UserDao
import com.bennohan.mynotes.databinding.FragmentProfileBinding
import com.bennohan.mynotes.helper.ViewBindingHelper.Companion.writeBitmap
import com.bennohan.mynotes.ui.addNote.NoteActivity
import com.bennohan.mynotes.ui.home.HomeViewModel
import com.bennohan.mynotes.ui.home.NavigationActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.*
import com.crocodic.core.helper.BitmapHelper
import com.crocodic.core.helper.ImagePreviewHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(R.layout.fragment_profile) {

    @Inject
    lateinit var userDao: UserDao
    private val viewModel by activityViewModels<ProfileViewModel>()
    private var dataUser: User? = null
    private var filePhoto: File? = null
    private var userName: String? = null

    private val myArray: Array<String> = arrayOf("take from camera 1", "insert from gallery 2")
    private var isTextChangedByUser = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe()
        binding?.tvName?.let { setupTextWatcher(it) }

        binding?.btnOpenPhoto?.setOnClickListener {
            openPictureDialog()
        }

        binding?.btnEditProfile?.setOnClickListener {
            editProfile()
        }
        binding?.ivUserPhoto?.setOnClickListener {
            ImagePreviewHelper(requireContext()).show(binding!!.ivUserPhoto, binding?.user?.photo)
        }


    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            //TODO Loading dialog at fragment
                            ApiStatus.LOADING -> {}
                            ApiStatus.SUCCESS -> {
                                //TODO Replace it with TOAST
                                binding?.root?.snacked("Profile Edited")
                            }
                            ApiStatus.ERROR -> {
//                                loadingDialog.setResponse(it.message ?: return@collect)

                            }
                            else -> binding?.root?.snacked("error")
                        }

                    }
                }
                launch {
                    userDao.getUser().collectLatest { user ->
                        binding?.user = user
                        dataUser = user
                        userName = user.name
                        isTextChangedByUser = false

                        Log.d("cek user", user.photo.toString())

                    }
                }
            }
        }

    }

    private fun editProfile() {
        val name = binding?.tvName?.textOf()
        val photo = filePhoto

        if (photo != null) {
            viewModel.editProfilePhoto(name, photo)
        } else {
            viewModel.editProfile(name)
        }

    }


    private fun openPictureDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setItems(myArray) { _, which ->
                // The 'which' argument contains the index position of the selected item
                when (which) {
                    0 -> (requireActivity() as NavigationActivity).activityLauncher.openCamera(
                        requireActivity(),
                        "${requireActivity().packageName}.fileprovider") { file, _ ->
                        uploadAvatar(file)
                    }
                    1 -> (requireActivity() as NavigationActivity).activityLauncher.openGallery(
                        requireContext()) { file, _ ->
                        uploadAvatar(file)
                    }
                }
            }
        }.create().show()
    }

    private fun uploadAvatar(file: File?) {
        if (file == null) {
            binding?.root?.snacked("error")
            return
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val resizeBitmap = BitmapHelper.resizeBitmap(bitmap, 512f)

        file.delete()

        //Result Photo
        val uploadFile = requireContext().createImageFile().also { it.writeBitmap(resizeBitmap) }

        //Processing the photo result
        filePhoto = uploadFile
        binding?.btnEditProfile?.visibility = View.VISIBLE
        Log.d("cek isi photo", uploadFile.toString())

        if (uploadFile != null) {
            binding?.ivUserEditedView?.visibility = View.VISIBLE
            binding?.ivUserEditedView?.let {
                Glide
                    .with(requireContext())
                    .load(uploadFile)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .apply(RequestOptions.circleCropTransform())
                    .into(it)
            }
        } else {
            binding?.ivUserEditedView?.visibility = View.GONE
        }


    }


    // Function to initialize the text watcher
    private fun setupTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isTextChangedByUser) {
                    // Show a toast message with the updated text
                    val updatedText = s.toString()
                    binding?.btnEditProfile?.visibility = View.VISIBLE
                } else {
                    isTextChangedByUser = true
                    binding?.btnEditProfile?.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
    }


}