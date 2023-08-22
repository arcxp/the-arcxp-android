package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.content.extendedModels.ArcXPCollection
import com.arcxp.content.models.ArcXPSection
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentSectionBinding
import com.arcxp.thearcxp.utils.*


class SectionFragment : BaseSectionFragment() {

    private var _binding: FragmentSectionBinding? = null
    private val binding get() = _binding!!
    private var sectionName: String? = null
    private var path: String? = null
    private val items = mutableMapOf<Int, ArcXPCollection>()

    private var canRequestNextPagination = true
    private var lastLoaded = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAdapter()

        sectionName = arguments?.getString(SECTION_NAME_KEY)
        path = arguments?.getString(PATH_NAME_KEY)

        if (!path.isNullOrEmpty()) {
            path = path!!.replace("/", "")
        } else {
            path = "/"
        }

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (canRequestNextPagination) {
                    if ((binding.recycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == items.size - 1) {
                        loadData()
                        Toast.makeText(requireContext(), getString(R.string.pagination_toast_message_section_list), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })

        //we only want to trigger the initial load once, otherwise on revisit we are requesting more data without any scrolling
        if (lastLoaded == 0) {
            loadData()
        }
    }

    private fun loadData() {
        binding.spin.setImageDrawable(spinner(requireContext()))
        var from = 0
        if (lastLoaded > 0) from = lastLoaded + 1
        vm.getCollection(
            id = path!!,
            from = from,
            size = requireContext().resources.getInteger(R.integer.collection_page_size)
        ).observe(viewLifecycleOwner) {
            when (it) {
                is Success -> {
                    onGetCollectionSuccess(response = it.success)
                }
                is Failure -> {
                    onError(it.failure)
                }
            }
            binding.spin.visibility = GONE
        }
    }

    override fun getSectionName(): String? {
        return sectionName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onError(error: ArcXPException) {
        //to ignore inevitable empty list error at end of collections
        if (error.message != getString(R.string.empty_collection)) {
            showSnackBar(
                error = error,
                view = binding.recycler,
                viewId = R.id.collection_view_fragment
            )
        } else {
            canRequestNextPagination = false
        }
    }

    private fun addToMap(
        index: Int,
        collection: ArcXPCollection
    ) {
        items[index] = collection
    }

    private fun onGetCollectionSuccess(response: Map<Int, ArcXPCollection>) {
        response.forEach {
            addToMap(
                index = it.key,
                collection = it.value
            )
        }

        binding.recycler.adapter?.notifyItemRangeChanged(
            lastLoaded + 1,
            response.keys.size
        )
        lastLoaded = response.keys.last()
    }

    private fun initializeAdapter() {
        binding.recycler.recycledViewPool.setMaxRecycledViews(1, 0)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = SectionRecyclerAdapter(
            items = items,
            vm = vm
        )
    }

    companion object {

        private const val SECTION_NAME_KEY = "name"
        private const val PATH_NAME_KEY = "id"

        @JvmStatic
        fun create(section: ArcXPSection): SectionFragment {
            val frag = SectionFragment()
            val args = Bundle()
            args.putString(PATH_NAME_KEY, section.id)
            args.putString(SECTION_NAME_KEY, section.getNameToUseFromSection())
            frag.arguments = args
            return frag
        }
    }

}