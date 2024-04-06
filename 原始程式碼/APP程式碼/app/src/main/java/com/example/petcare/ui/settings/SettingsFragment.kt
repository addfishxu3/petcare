package com.example.petcare.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.petcare.MainActivity
import com.example.petcare.R
import com.example.petcare.databinding.FragmentSettingsBinding
import com.example.petcare.ui.settings.machineSetting.MachineFragment
import com.example.petcare.ui.settings.personalInfo.PersonalInfoFragment
import com.example.petcare.ui.settings.petsInfo.PetsInfoFragment
import javax.crypto.Mac


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val settingsViewModel =
//            ViewModelProvider(this)[SettingsViewModel::class.java]

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

//        val textView: TextView = binding.textNotifications
//        settingsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.personalInfo.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            requireActivity().actionBar.
            transaction?.replace(R.id.SettingsFragment, PersonalInfoFragment())?.commit()
        }

        binding.petsInfo.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.SettingsFragment, PetsInfoFragment())?.commit()
        }



        binding.machineSetting.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.SettingsFragment, MachineFragment())?.commit()
        }

        binding.logout.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

}