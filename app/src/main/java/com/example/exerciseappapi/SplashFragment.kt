package com.example.exerciseappapi

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.exerciseappapi.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val splashScreenDuration = 3000L // 3 seconds
    private val updateInterval = 100L // 0.1 second
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        progressBar = binding.progressBar
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle the back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Allow the back press to navigate back and exit the app
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        )

        startProgressBar()

        // Navigate to MainFragment after splash screen duration
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
            }
        }, splashScreenDuration)
    }

    private fun startProgressBar() {
        progressBar?.max = (splashScreenDuration / updateInterval).toInt()
        val handler = Handler(Looper.getMainLooper())
        var progress = 0

        handler.post(object : Runnable {
            override fun run() {
                if (progress <= progressBar?.max ?: 0) {
                    progressBar?.progress = progress
                    progress++
                    handler.postDelayed(this, updateInterval)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
