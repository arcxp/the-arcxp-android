package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentHomeBinding
import com.arcxp.thearcxp.utils.spinner

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spin.setImageDrawable(spinner(requireContext()))

        vm.sectionsListEvent.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Success -> {
                    var id = 0
                    it.success.forEach {
                        vm.sectionsIndexMap[id] = it.navigation.nav_title
                        vm.indexSectionMap[it.navigation.nav_title] = id++
                    }

                    binding.pager.adapter = SectionsAdapter()
                    binding.tabLayout.setupWithViewPager(binding.pager)
                }
                is Failure -> onError(it.failure)
            }
            binding.spin.visibility = GONE
        }

        vm.sectionEvent.observe(viewLifecycleOwner) {
            binding.pager.currentItem = vm.indexSectionMap[it.navigation.nav_title]!!
        }
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            ArcXPContentError(error.type!!, error.localizedMessage),
            binding.collectionViewFragment,
            R.id.collection_view_fragment,
            true
        )
    }

    private inner class SectionsAdapter() :
        FragmentPagerAdapter(childFragmentManager) {
        override fun getPageTitle(position: Int): CharSequence {
            return vm.sectionsIndexMap[position].toString()
        }

        override fun getCount(): Int {
            return vm.sections.size
        }

        override fun getItem(position: Int): Fragment {
            val fragment = vm.getFragment(position)
            if (fragment !== null) {
                return fragment
            }

            val sectionTitle = vm.sectionsIndexMap[position]
            val section = vm.sections[sectionTitle]
            return vm.createFragment(section!!)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
