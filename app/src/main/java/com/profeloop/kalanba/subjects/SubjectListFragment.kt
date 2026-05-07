package com.profeloop.kalanba.subjects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.profeloop.kalanba.databinding.FragmentSubjectListBinding
import com.profeloop.kalanba.utils.Constants
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch

class SubjectListFragment : Fragment() {

    private var _binding: FragmentSubjectListBinding? = null
    private val binding get() = _binding!!
    private val args: SubjectListFragmentArgs by navArgs()

    private var currentPeriodo = 1
    private lateinit var adapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val grado = args.extraGrado

        // Setup tabs
        for (i in 1..4) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Período $i"))
        }

        adapter = SubjectAdapter(emptyList()) { subject ->
            val action = SubjectListFragmentDirections
                .actionSubjectListToTaskList(grado, subject, currentPeriodo)
            findNavController().navigate(action)
        }

        binding.rvSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubjects.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentPeriodo = tab.position + 1
                loadSubjects(grado)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        loadSubjects(grado)
    }

    private fun loadSubjects(grado: Int) {
        lifecycleScope.launch {
            val subjectsWithTeachers = Constants.SUBJECTS.map { subject ->
                val teacher = FirebaseUtils.checkTeacherInSubject(grado, subject)
                Pair(subject, teacher)
            }
            adapter.updateData(subjectsWithTeachers)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
