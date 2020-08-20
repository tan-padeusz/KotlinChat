package com.mygamecompany.kotlinchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.databinding.FragmentStartBinding
import com.mygamecompany.kotlinchat.utilities.PermissionHandler
import java.util.Timer
import java.util.TimerTask

const val DELAY_TIME: Long = 2250

class StartFragment: Fragment() {
    //VALUES
    private val timer: Timer = Timer("delay_start")
    private val showViews: MutableLiveData<Boolean> = MutableLiveData(false)

    //VARIABLES
    private lateinit var binding: FragmentStartBinding

    //FUNCTIONS
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStartBinding.inflate(inflater, container, false)
        setListeners()
        setTimer()
        return binding.root
    }

    private fun setTimer() {
        timer.schedule(object: TimerTask() {
            override fun run() {
                showViews.postValue(true)
            }
        }, DELAY_TIME)
    }

    private fun showViews(show: Boolean) {
        binding.infoLabelUpper.visibility = if (show) View.VISIBLE else View.GONE
        binding.infoLabelLower.visibility = if (show) View.VISIBLE else View.GONE
        binding.requestButton.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setListeners() {
        binding.requestButton.setOnClickListener {
            PermissionHandler.setFlags()
            if (PermissionHandler.status.value!!) findNavController().navigate(R.id.action_startFragment_to_menuFragment)
            else PermissionHandler.requestPermissions()
        }

        showViews.observe(viewLifecycleOwner, Observer {
            showViews(it)
        })
    }
}