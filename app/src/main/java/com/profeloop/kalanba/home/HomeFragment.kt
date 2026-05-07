package com.profeloop.kalanba.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.profeloop.kalanba.R
import com.profeloop.kalanba.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
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

        val items: MutableList<Any> = mutableListOf()
        items.add("PRIMARIA")
        items.addAll(1..5)
        items.add("BACHILLERATO")
        items.addAll(6..9)

        val adapter = GradeListAdapter(items) { grade ->
            val bundle = Bundle().apply {
                putInt("extra_grado", grade)
            }
            findNavController().navigate(R.id.action_home_to_subjectList, bundle)
        }

        binding.rvGrades.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGrades.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
