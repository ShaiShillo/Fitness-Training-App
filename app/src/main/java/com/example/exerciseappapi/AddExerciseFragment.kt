package com.example.exerciseappapi

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.exerciseappapi.databinding.FragmentAddExerciseBinding
import com.example.exerciseappapi.databinding.BottomSheetImageOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class AddExerciseFragment : Fragment() {

    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var binding: FragmentAddExerciseBinding
    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback { uri ->
            uri?.let {
                imageUri = it
                binding.exerciseImageView.setImageURI(it)
            }
        }
    )

    private val captureImageLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
        ActivityResultCallback { bitmap ->
            bitmap?.let {
                val uri = saveImage(requireContext(), it)
                imageUri = uri
                binding.exerciseImageView.setImageURI(uri)
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddExerciseBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupSpinners()
        setupObservers()
        setupNameEditText()

        binding.exerciseImageView.setOnClickListener {
            showImageOptionsBottomSheet()
        }

        binding.saveExerciseButton.setOnClickListener {
            saveExercise()
        }

        return binding.root
    }

    private fun setupNameEditText() {
        binding.exerciseNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    setVisibilityForFields(View.GONE)
                } else {
                    setVisibilityForFields(View.VISIBLE)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setVisibilityForFields(visibility: Int) {
        binding.bodyPartTitle.visibility = visibility
        binding.bodyPartSpinner.visibility = visibility
        binding.targetTitle.visibility = visibility
        binding.targetSpinner.visibility = visibility
        binding.equipmentTitle.visibility = visibility
        binding.equipmentSpinner.visibility = visibility
        binding.secondaryMusclesTitle.visibility = visibility
        binding.secondaryMusclesEditText.visibility = visibility
        binding.instructionsTitle.visibility = visibility
        binding.instructionsEditText.visibility = visibility

        // Ensure proper state management for spinners
        if (visibility == View.VISIBLE) {
            binding.targetSpinner.isEnabled = false
            binding.targetSpinner.alpha = 0.5f
            binding.equipmentSpinner.isEnabled = false
            binding.equipmentSpinner.alpha = 0.5f
        }
    }

    private fun setupSpinners() {
        viewModel.bodyParts.observe(viewLifecycleOwner) { bodyParts ->
            val bodyPartList = listOf(getString(R.string.select_body_part)) + bodyParts
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bodyPartList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.bodyPartSpinner.adapter = adapter
        }

        binding.bodyPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedBodyPart = parent.getItemAtPosition(position) as String
                if (selectedBodyPart != getString(R.string.select_body_part)) {
                    viewModel.loadTargets(selectedBodyPart)
                    binding.targetSpinner.isEnabled = true
                    binding.targetSpinner.alpha = 1.0f
                } else {
                    binding.targetSpinner.isEnabled = false
                    binding.targetSpinner.alpha = 0.5f
                    binding.equipmentSpinner.isEnabled = false
                    binding.equipmentSpinner.alpha = 0.5f
                    clearSpinner(binding.targetSpinner)
                    clearSpinner(binding.equipmentSpinner)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.targets.observe(viewLifecycleOwner) { targets ->
            val targetList = listOf(getString(R.string.select_target)) + targets
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, targetList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.targetSpinner.adapter = adapter
        }

        binding.targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTarget = parent.getItemAtPosition(position) as String
                if (selectedTarget != getString(R.string.select_target)) {
                    binding.equipmentSpinner.isEnabled = true
                    binding.equipmentSpinner.alpha = 1.0f
                    viewModel.fetchAllEquipment()
                } else {
                    binding.equipmentSpinner.isEnabled = false
                    binding.equipmentSpinner.alpha = 0.5f
                    clearSpinner(binding.equipmentSpinner)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.equipment.observe(viewLifecycleOwner) { equipment ->
            val equipmentList = listOf(getString(R.string.select_equipment)) + equipment
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, equipmentList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.equipmentSpinner.adapter = adapter
        }
    }

    private fun setupObservers() {
        viewModel.bodyParts.observe(viewLifecycleOwner) { bodyParts ->
            val bodyPartList = listOf(getString(R.string.select_body_part)) + bodyParts
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bodyPartList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.bodyPartSpinner.adapter = adapter
        }
    }

    private fun showImageOptionsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetImageOptionsBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.chooseImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.captureImageButton.setOnClickListener {
            captureImageLauncher.launch(null)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun saveImage(context: Context, bitmap: Bitmap): Uri? {
        val filename = "${System.currentTimeMillis()}.jpg"
        var imageUri: Uri? = null

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyAppImages")
            }

            val contentResolver = context.contentResolver
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let { uri ->
                contentResolver.openOutputStream(uri)?.use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return imageUri
    }

    private fun saveExercise() {
        val name = binding.exerciseNameEditText.text.toString()
        val bodyPart = binding.bodyPartSpinner.selectedItem as? String
        val equipment = binding.equipmentSpinner.selectedItem as? String
        val target = binding.targetSpinner.selectedItem as? String
        val secondaryMuscles = binding.secondaryMusclesEditText.text.toString().split(",").filter { it.isNotEmpty() }
        val instructions = binding.instructionsEditText.text.toString().split(",").filter { it.isNotEmpty() }

        val missingFields = listOf(
            "name" to name.isEmpty(),
            "body part" to (bodyPart.isNullOrEmpty() || bodyPart == getString(R.string.select_body_part)),
            "equipment" to (equipment.isNullOrEmpty() || equipment == getString(R.string.select_equipment)),
            "target" to (target.isNullOrEmpty() || target == getString(R.string.select_target))
        ).mapNotNull { if (it.second) it.first else null }

        if (missingFields.isNotEmpty()) {
            val message = "Please fill the following fields: ${missingFields.joinToString(", ")}"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return
        }

        val gifUrl = imageUri?.toString() ?: getPlaceholderUri()

        val exercise = Exercise(
            id = System.currentTimeMillis().toString(),
            name = name,
            bodyPart = bodyPart ?: "",
            equipment = equipment ?: "",
            target = target ?: "",
            gifUrl = gifUrl,
            secondaryMuscles = secondaryMuscles,
            instructions = instructions
        )

        lifecycleScope.launch {
            viewModel.addExercise(exercise)
            setFragmentResult("addExerciseResult", bundleOf("shouldReset" to true))
            findNavController().navigate(R.id.action_addExerciseFragment_to_mainFragment)
        }
    }

    private fun getPlaceholderUri(): String {
        return Uri.parse("android.resource://${requireContext().packageName}/drawable/placeholder").toString()
    }

    private fun clearSpinner(spinner: Spinner) {
        val emptyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf<String>())
        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = emptyAdapter
    }
}