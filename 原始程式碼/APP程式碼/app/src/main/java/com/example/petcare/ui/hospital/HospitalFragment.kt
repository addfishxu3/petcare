package com.example.petcare.ui.hospital

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcare.PHARMACIES_DATA_URL
import com.example.petcare.R
import com.example.petcare.data.Feature
import com.example.petcare.data.HospitalInfo
import com.example.petcare.databinding.FragmentHospitalBinding
import com.example.petcare.ui.settings.petsInfo.PetViewModel
import com.example.petcare.util.CountyUtil
import com.google.gson.Gson
import com.thishkt.pharmacydemo.util.OkHttpUtil
import okhttp3.Response

class HospitalFragment : Fragment() , HospitalAdapter.IItemClickListener{

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewAdapter: HospitalAdapter
    private var _binding: FragmentHospitalBinding? = null

    private var currentCounty: String = "臺北市"
    private var currentTown: String = "大安區"
    private var hospitalInfo: HospitalInfo? = null
    lateinit var model: HospitalViewModel

    //只能用在onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val hospitalViewModel =
//            ViewModelProvider(this)[HospitalViewModel::class.java]

        _binding = FragmentHospitalBinding.inflate(inflater, container, false)

//        val textView: TextView = binding.textHospitals
//        hospitalViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProvider(requireActivity())[HospitalViewModel::class.java]

        val adapterCounty = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            CountyUtil.getAllCountiesName()
        )
        binding.spinnerCounty.adapter = adapterCounty
        binding.spinnerCounty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentCounty = binding.spinnerCounty.selectedItem.toString()
                setSpinnerTown()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.spinnerTown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentTown = binding.spinnerTown.selectedItem.toString()
                if (hospitalInfo != null) {
                    updateRecyclerView()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        setDefaultCountyWithTown()

        // 定義 LayoutManager 為 LinearLayoutManager
        viewManager = LinearLayoutManager(context)

        // 自定義 Adapter 為 HospitalAdapter，稍後再定義 HospitalAdapter 這個類別
        viewAdapter = HospitalAdapter(this)

        // 定義從佈局當中，拿到 recycler_view 元件，
        binding.recyclerView.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = viewManager
            // set the custom adapter to the RecyclerView
            adapter = viewAdapter

            // 讓我們item有新增或減少時，item的size不會改變，讓RecyclerView不用重新計算大小
            setHasFixedSize(true)

            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        //顯示忙碌圈圈
        binding.progressBar.visibility = View.VISIBLE

        OkHttpUtil.mOkHttpUtil.getAsync(PHARMACIES_DATA_URL, object : OkHttpUtil.ICallback {
            override fun onResponse(response: Response) {
                var hospitalsData = response.body?.string()
                //刪除空格
                hospitalsData = hospitalsData?.replace("\\s+".toRegex(), "")
                hospitalsData = "{\"features\":$hospitalsData}"

                hospitalInfo = Gson().fromJson(hospitalsData, HospitalInfo::class.java)

                activity?.runOnUiThread {
                    //將下載的資料，指定給 MainAdapter
                    viewAdapter.hospitalList = hospitalInfo!!.features
                    updateRecyclerView()

                    //關閉忙碌圈圈
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onFailure(e: okio.IOException) {
                Log.d("Okio", "onFailure: $e")

                //關閉忙碌圈圈
                binding.progressBar.visibility = View.GONE
            }
        })
    }
    private fun setDefaultCountyWithTown() {
        binding.spinnerCounty.setSelection(CountyUtil.getCountyIndexByName(currentCounty))
        setSpinnerTown()
    }

    private fun setSpinnerTown() {
        val adapterTown = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            CountyUtil.getTownsByCountyName(currentCounty)
        )
        binding.spinnerTown.adapter = adapterTown
        binding.spinnerTown.setSelection(CountyUtil.getTownIndexByName(currentCounty, currentTown))
    }

    private fun updateRecyclerView() {
        val filterData = hospitalInfo?.features?.filter {
            it.City == currentCounty && it.Address.substring(3..5) == currentTown
        }

        if (filterData != null) {
            viewAdapter.hospitalList = filterData
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClickListener(data: Feature) {
        val manager = requireActivity().supportFragmentManager
        val transaction = manager.beginTransaction()
        model.sendMsg("${data.Name},${data.Tel},${data.Address},${data.IsEmergencyDepartment},${data.EmergencyDepartment}")
        transaction.replace(R.id.FragmentHospital, HospitalDetailFragment()).commit()
    }
}