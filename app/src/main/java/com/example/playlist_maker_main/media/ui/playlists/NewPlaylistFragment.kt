package com.example.playlist_maker_main.media.ui.playlists

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentNewPlaylistBinding
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment(R.layout.fragment_new_playlist) {

    private val viewModel: NewPlaylistViewModel by viewModel()
    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private var playlistToEdit: Playlist? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivPlaylistCover.setImageURI(uri)
            binding.ivPlaylistCover.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistToEdit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("playlist", Playlist::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("playlist")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewPlaylistBinding.bind(view)

        setupUI()

        if (playlistToEdit != null) {
            setupEditMode(playlistToEdit!!)
        }

        binding.backBtn.setOnClickListener { handleBackNavigation() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { handleBackNavigation() }
        })
    }

    private fun setupUI() {
        binding.ivPlaylistCover.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.etName.doOnTextChanged { text, _, _, _ ->
            binding.btnCreate.isEnabled = !text.isNullOrBlank()
        }

        binding.btnCreate.setOnClickListener {
            val name = binding.etName.text.toString()
            val description = binding.etDescription.text.toString()

            val imagePath = imageUri?.let { viewModel.saveImage(it) }

            if (playlistToEdit == null) {
                viewModel.createPlaylist(name, description, imagePath)
                Toast.makeText(requireContext(), getString(R.string.playlist_created, name), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.updatePlaylist(playlistToEdit!!, name, description, imagePath)
            }
            findNavController().popBackStack()
        }
    }

    private fun setupEditMode(playlist: Playlist) {
        binding.tvHeader.text = getString(R.string.edit)
        binding.btnCreate.text = getString(R.string.save)

        binding.etName.setText(playlist.name)
        binding.etDescription.setText(playlist.description)

        if (playlist.imagePath != null) {
            Glide.with(this)
                .load(playlist.imagePath)
                .transform(CenterCrop(), RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_8)))
                .into(binding.ivPlaylistCover)
        }
    }

    private fun handleBackNavigation() {
        if (playlistToEdit != null) {
            findNavController().popBackStack()
            return
        }

        val hasContent = imageUri != null || !binding.etName.text.isNullOrBlank() || !binding.etDescription.text.isNullOrBlank()
        if (hasContent) {
            showExitDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.stop_creating_playlist))
            .setMessage(getString(R.string.unsaved_data_will_be_lost))
            .setNeutralButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.finish)) { _, _ -> findNavController().popBackStack() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}