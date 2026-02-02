package com.example.playlist_maker_main.media.ui.playlists

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentNewPlaylistBinding
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class NewPlaylistFragment : Fragment(R.layout.fragment_new_playlist) {

    private val viewModel: NewPlaylistViewModel by viewModel()
    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewPlaylistBinding.bind(view)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imageUri = uri
                binding.ivPlaylistCover.setImageURI(uri)
                binding.ivPlaylistCover.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }
        }

        binding.ivPlaylistCover.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnCreate.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etName.addTextChangedListener(textWatcher)

        binding.btnCreate.setOnClickListener {
            val name = binding.etName.text.toString()
            val description = binding.etDescription.text.toString()
            val savedImagePath = imageUri?.let { saveImageToInternalStorage(it) }

            viewModel.createPlaylist(name, description, savedImagePath)

            Toast.makeText(requireContext(), "Плейлист $name создан", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.backBtn.setOnClickListener {
            handleBackNavigation()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })
    }

    private fun handleBackNavigation() {
        val name = binding.etName.text
        val desc = binding.etDescription.text

        if (imageUri != null || !name.isNullOrBlank() || !desc.isNullOrBlank()) {
            showConfirmDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все несохраненные данные будут потеряны")
            .setNegativeButton("Отмена") { _, _ -> }
            .setPositiveButton("Завершить") { _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val filePath = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "playlist_covers")
        if (!filePath.exists()) filePath.mkdirs()

        val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)

        BitmapFactory
            .decodeStream(inputStream)
            .compress(Bitmap.CompressFormat.JPEG, 30, outputStream)

        return file.absolutePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}