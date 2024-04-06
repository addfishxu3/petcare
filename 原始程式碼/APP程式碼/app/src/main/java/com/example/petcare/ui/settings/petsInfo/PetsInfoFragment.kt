package com.example.petcare.ui.settings.petsInfo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcare.data.PetData
import com.example.petcare.databinding.FragmentPetsInfoBinding
import com.example.petcare.R
import com.example.petcare.ui.settings.SettingsFragment

private lateinit var viewModel: PetViewModel

class PetsInfoFragment : Fragment(), PetAdapter.IItemClickListener {

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewAdapter: PetAdapter
    private var _binding: FragmentPetsInfoBinding? = null
    lateinit var model: PetViewModel

    //只能用在onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetsInfoBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val manager = activity?.supportFragmentManager
        val transaction = manager?.beginTransaction()

        model = ViewModelProvider(requireActivity())[PetViewModel::class.java]

        // 定義 LayoutManager 為 LinearLayoutManager
        viewManager = LinearLayoutManager(context)

        // 自定義 Adapter 為 PetAdapter，稍後再定義 PetAdapter 這個類別
        viewAdapter = PetAdapter(this)

        viewModel = ViewModelProvider(this)[PetViewModel::class.java]


        // 定義從佈局當中，拿到 recycler_view 元件，
        binding.petsRecyclerView.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = viewManager
            // set the custom adapter to the RecyclerView
            adapter = viewAdapter

            viewModel.allPets.observe(viewLifecycleOwner, Observer {
                viewAdapter.updateList(it)
            })

            // 讓我們item有新增或減少時，item的size不會改變，讓RecyclerView不用重新計算大小
            setHasFixedSize(true)

            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        binding.addPetBtn.setOnClickListener {
            transaction?.replace(R.id.PetsInfoFragment, AddPetFragment())?.commit()
        }
        binding.cancelBtn.setOnClickListener {
            transaction?.replace(R.id.PetsInfoFragment, SettingsFragment())?.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClickListener(data: PetData) {
        val manager = activity?.supportFragmentManager
        val transaction = manager?.beginTransaction()
        model.sendMsg(data.name.toString())
        transaction?.replace(R.id.PetsInfoFragment, PetDetailFragment())?.commit()
    }
}