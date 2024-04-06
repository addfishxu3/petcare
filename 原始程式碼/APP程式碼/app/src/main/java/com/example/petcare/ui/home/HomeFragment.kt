package com.example.petcare.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.petcare.R
import com.example.petcare.databinding.FragmentHomeBinding
import com.example.petcare.ui.home.pet.HomePetFragment
import com.example.petcare.ui.home.record.HomeRecordFragment
import com.example.petcare.ui.home.video.HomeVideoFragment
import com.example.petcare.ui.settings.petsInfo.PetDetailFragment
import com.example.petcare.ui.settings.petsInfo.PetViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private var spinnerList = ArrayList<String>()
    private lateinit var selectedPet: String
    lateinit var model: PetViewModel
    private val fragmentMap = HashMap<String, Fragment>()


    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchFragment(HomePetFragment())
        model = ViewModelProvider(requireActivity())[PetViewModel::class.java]

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")

        if(uid.isNotEmpty()){
            getUserData()
        }

        binding.infoBtn.setOnClickListener {
            switchFragment(HomePetFragment())
        }
        binding.videoBtn.setOnClickListener {
            switchFragment(HomeVideoFragment())
        }
        binding.recordBtn.setOnClickListener {
            switchFragment(HomeRecordFragment())
        }
    }

    private fun switchFragment(fragment: Fragment) {
        val manager = childFragmentManager
        val transaction = manager.beginTransaction()
        val fragmentTag = fragment.javaClass.simpleName

        // 判斷該 Fragment 是否已加入
        if (!fragmentMap.containsKey(fragmentTag)) {
            transaction.add(R.id.child_fragment_container, fragment, fragmentTag)
            fragmentMap[fragmentTag] = fragment
            Log.d("fragmentTag", "$fragmentTag")
        } else {
            // 這邊要更新資料
            transaction.show(fragmentMap[fragmentTag]!!)
        }

        // 隱藏其他 Fragment
        for (existingFragment in fragmentMap) {
            if (existingFragment.key != fragmentTag) {
                transaction.hide(existingFragment.value)
            }
        }

        transaction.commitAllowingStateLoss()
    }


    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object: ValueEventListener {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            override fun onDataChange(snapshot: DataSnapshot) {
                spinnerList.clear()
                for (ds in snapshot.child("petList").children) {
                    val name =snapshot.child("petList").child(ds.key.toString()).child("name").value.toString()
                    spinnerList.add(name)
                    Log.d("spinnerList", "$spinnerList ")
                }
                val spinnerAdapter = context?.let {

                    ArrayAdapter(
                        it,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        spinnerList
                    )

                }

                binding.choosePetSpinner.adapter = spinnerAdapter

                binding.choosePetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedPet = binding.choosePetSpinner.selectedItem.toString()
                        model.sendMsg(selectedPet)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
                if(spinnerList.size > 0){
                    selectedPet = spinnerList[0]
                    binding.choosePetSpinner.setSelection(spinnerList.indexOf(selectedPet))
                }


            }
            // Failed to read value
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "資料讀取錯誤", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}