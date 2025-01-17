package com.example.exerciseappapi.ui.fragments

import android.Manifest
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import com.example.exerciseappapi.models.Exercise
import com.example.exerciseappapi.viewmodels.ViewModel
import com.example.exerciseappapi.R
import com.example.exerciseappapi.databinding.FragmentAddExerciseBinding
import com.example.exerciseappapi.databinding.BottomSheetImageOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class AddExerciseFragment : Fragment() {

    private val viewModel: ViewModel by viewModels()
    private lateinit var binding: FragmentAddExerciseBinding
    private var imageUri: Uri? = null
    private var exerciseToEdit: Exercise? = null
    private var permissionsGranted = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imageUri = it
            binding.exerciseImageView.setImageURI(it)
        }
    }

    private val captureImageLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val uri = saveImage(requireContext(), it)
            imageUri = uri
            binding.exerciseImageView.setImageURI(uri)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            permissionsGranted = true
        } else {
            Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
            permissionsGranted = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Request permissions
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))

        binding = FragmentAddExerciseBinding.inflate(inflater, container, false).apply {
            viewModel = this@AddExerciseFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        val args: AddExerciseFragmentArgs by navArgs()
        exerciseToEdit = args.exercise

        setupSpinners()
        setupObservers()
        setupNameEditText()

        if (exerciseToEdit != null) {
            viewModel.setEditMode(true)
            populateFieldsForEdit(exerciseToEdit!!)
            binding.saveExerciseButton.text = getString(R.string.save_changes)
        } else {
            viewModel.setEditMode(false)
            binding.saveExerciseButton.text = getString(R.string.save_new_exercise)
        }

        binding.exerciseImageView.setOnClickListener {
            if (permissionsGranted) {
                showImageOptionsBottomSheet()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveExerciseButton.setOnClickListener {
            if (viewModel.isEditMode.value == true) {
                saveChanges()
            } else {
                saveExercise()
            }
        }

        return binding.root
    }

    private fun setupNameEditText() {
        binding.exerciseNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty() && viewModel.isEditMode.value != true) {
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
        }
    }

    private fun setupSpinners() {
        viewModel.bodyParts.observe(viewLifecycleOwner) { bodyParts ->
            val bodyPartList = listOf(getString(R.string.select_body_part)) + bodyParts
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bodyPartList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.bodyPartSpinner.adapter = adapter

            // If in edit mode, set the spinner to the exercise body part
            if (viewModel.isEditMode.value == true && exerciseToEdit != null) {
                val position = bodyPartList.indexOf(exerciseToEdit!!.bodyPart)
                if (position != -1) {
                    binding.bodyPartSpinner.setSelection(position)
                }
            }
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
                    clearSpinner(binding.targetSpinner)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.targets.observe(viewLifecycleOwner) { targets ->
            val targetList = listOf(getString(R.string.select_target)) + targets
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, targetList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.targetSpinner.adapter = adapter

            // If in edit mode, set the spinner to the exercise target
            if (viewModel.isEditMode.value == true && exerciseToEdit != null) {
                val position = targetList.indexOf(exerciseToEdit!!.target)
                if (position != -1) {
                    binding.targetSpinner.setSelection(position)
                }
            }
        }

        viewModel.fetchAllEquipment()
        viewModel.equipment.observe(viewLifecycleOwner) { equipment ->
            val equipmentList = listOf(getString(R.string.select_equipment)) + equipment
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, equipmentList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.equipmentSpinner.adapter = adapter

            // If in edit mode, set the spinner to the exercise equipment
            if (viewModel.isEditMode.value == true && exerciseToEdit != null) {
                val position = equipmentList.indexOf(exerciseToEdit!!.equipment)
                if (position != -1) {
                    binding.equipmentSpinner.setSelection(position)
                }
            }
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
            pickImageLauncher.launch(arrayOf("image/*"))
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
        if (!permissionsGranted) {
            Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
            return
        }

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
            instructions = instructions,
            createdByUser = true
        )

        lifecycleScope.launch {
            viewModel.addExercise(exercise)
            setFragmentResult("addExerciseResult", bundleOf("shouldReset" to true))
            findNavController().navigate(R.id.action_addExerciseFragment_to_mainFragment)
        }
    }

    private fun saveChanges() {
        if (!permissionsGranted) {
            Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
            return
        }

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

        val gifUrl = imageUri?.toString() ?: exerciseToEdit?.gifUrl ?: getPlaceholderUri()

        val exercise = exerciseToEdit?.copy(
            name = name,
            bodyPart = bodyPart ?: "",
            equipment = equipment ?: "",
            target = target ?: "",
            gifUrl = gifUrl,
            secondaryMuscles = secondaryMuscles,
            instructions = instructions
        )

        exercise?.let {
            lifecycleScope.launch {
                viewModel.updateExercise(it)
                setFragmentResult("addExerciseResult", bundleOf("shouldReset" to true))
                findNavController().navigate(R.id.action_addExerciseFragment_to_mainFragment)
            }
        }
    }

    private fun getPlaceholderUri(): String {
        return Uri.parse("android.resource://${requireContext().packageName}/drawable/img").toString()
    }

    private fun clearSpinner(spinner: Spinner) {
        val emptyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf<String>())
        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = emptyAdapter
    }

    private fun populateFieldsForEdit(exercise: Exercise) {
        binding.exerciseNameEditText.setText(exercise.name)
        binding.secondaryMusclesEditText.setText(exercise.secondaryMuscles.joinToString(","))
        binding.instructionsEditText.setText(exercise.instructions.joinToString(","))

        exercise.gifUrl.let {
            imageUri = Uri.parse(it)
            binding.exerciseImageView.setImageURI(imageUri)
        }

        setVisibilityForFields(View.VISIBLE)

        viewModel.bodyParts.observe(viewLifecycleOwner) { bodyParts ->
            val bodyPartList = listOf(getString(R.string.select_body_part)) + bodyParts
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bodyPartList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.bodyPartSpinner.adapter = adapter

            // Set the spinner to the exercise body part
            val position = bodyPartList.indexOf(exercise.bodyPart)
            if (position != -1) {
                binding.bodyPartSpinner.setSelection(position)
            }
        }

        viewModel.targets.observe(viewLifecycleOwner) { targets ->
            val targetList = listOf(getString(R.string.select_target)) + targets
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, targetList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.targetSpinner.adapter = adapter

            // Set the spinner to the exercise target
            val position = targetList.indexOf(exercise.target)
            if (position != -1) {
                binding.targetSpinner.setSelection(position)
            }
        }

        viewModel.fetchAllEquipment()
        viewModel.equipment.observe(viewLifecycleOwner) { equipment ->
            val equipmentList = listOf(getString(R.string.select_equipment)) + equipment
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, equipmentList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.equipmentSpinner.adapter = adapter

            // Set the spinner to the exercise equipment
            val position = equipmentList.indexOf(exercise.equipment)
            if (position != -1) {
                binding.equipmentSpinner.setSelection(position)
            }
        }
    }
}
