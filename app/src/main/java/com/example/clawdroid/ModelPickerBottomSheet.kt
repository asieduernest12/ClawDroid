package com.example.clawdroid

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clawdroid.model.PROVIDER_COLORS
import com.example.clawdroid.model.PickerModel
import com.example.clawdroid.model.SortMode
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText

class ModelPickerBottomSheet(
    private val providerName: String,
    private val models: List<PickerModel>,
    private val onModelSelected: (modelId: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _models: List<PickerModel> = models
    private var _filtered: List<PickerModel> = models
    private var _sortMode: SortMode = SortMode.RECENT
    private var _recentIds: Set<String> = emptySet()
    private var _pinnedIds: MutableSet<String> = mutableSetOf()

    private var adapter: ModelAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_model_picker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<TextInputEditText>(R.id.search_input)
        val sortChip = view.findViewById<com.google.android.material.chip.Chip>(R.id.sort_chip)
        val recentSection = view.findViewById<View>(R.id.recent_section)
        val recentChipGroup = view.findViewById<com.google.android.material.chip.ChipGroup>(R.id.recent_chip_group)
        val sectionHeader = view.findViewById<TextView>(R.id.section_header)
        val modelList = view.findViewById<RecyclerView>(R.id.model_list)
        val footer = view.findViewById<TextView>(R.id.footer)

        view.findViewById<View>(R.id.btn_close).setOnClickListener { dismiss() }

        adapter = ModelAdapter { model ->
            _recentIds = setOf(model.modelId) + _recentIds.take(4)
            onModelSelected(model.modelId)
            dismiss()
        }
        modelList.layoutManager = LinearLayoutManager(requireContext())
        modelList.adapter = adapter

        applySort()

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.lowercase() ?: ""
                filterModels(query)
                recentSection.isVisible = query.isBlank() && _recentIds.isNotEmpty()
            }
        })

        sortChip.setOnClickListener {
            _sortMode = when (_sortMode) {
                SortMode.RECENT -> SortMode.ALPHABETICAL
                SortMode.ALPHABETICAL -> SortMode.CONTEXT_LENGTH
                SortMode.CONTEXT_LENGTH -> SortMode.RECENT
            }
            sortChip.text = when (_sortMode) {
                SortMode.RECENT -> getString(R.string.model_picker_sort_recent)
                SortMode.ALPHABETICAL -> getString(R.string.model_picker_sort_alpha)
                SortMode.CONTEXT_LENGTH -> getString(R.string.model_picker_sort_context)
            }
            applySort()
        }

        recentChipGroup.removeAllViews()
        for (recentId in _recentIds) {
            val model = _models.find { it.modelId == recentId } ?: continue
            val chip = layoutInflater.inflate(
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                recentChipGroup,
                false
            ) as Chip
            chip.text = model.displayName
            chip.isClickable = true
            chip.isCheckable = false
            chip.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.material_on_surface_emphasis_high_type))
            chip.setOnClickListener {
                onModelSelected(model.modelId)
                dismiss()
            }
            recentChipGroup.addView(chip)
        }
        recentSection.isVisible = _recentIds.isNotEmpty()

        sectionHeader.text = getString(R.string.model_picker_footer_template,
            _filtered.size, providerName)
        footer.text = sectionHeader.text
    }

    private fun filterModels(query: String) {
        _filtered = if (query.isBlank()) {
            _models.toList()
        } else {
            _models.filter {
                it.modelId.lowercase().contains(query) ||
                it.displayName.lowercase().contains(query) ||
                it.providerName.lowercase().contains(query)
            }
        }
        applySort()
    }

    private fun applySort() {
        val sorted = when (_sortMode) {
            SortMode.RECENT -> {
                val recent = _filtered.filter { it.modelId in _recentIds }
                val rest = _filtered.filter { it.modelId !in _recentIds }
                recent + rest.sortedBy { it.displayName.lowercase() }
            }
            SortMode.ALPHABETICAL -> _filtered.sortedBy { it.displayName.lowercase() }
            SortMode.CONTEXT_LENGTH -> _filtered.sortedByDescending { it.contextLength ?: 0 }
        }
        val pinnedFirst = if (_pinnedIds.isEmpty()) sorted
        else {
            val pinned = sorted.filter { it.modelId in _pinnedIds }
            val unpinned = sorted.filter { it.modelId !in _pinnedIds }
            pinned + unpinned
        }
        adapter?.submitList(pinnedFirst)
    }

    private inner class ModelAdapter(
        private val onClick: (PickerModel) -> Unit
    ) : RecyclerView.Adapter<ModelAdapter.ViewHolder>() {

        private var items: List<PickerModel> = emptyList()

        fun submitList(list: List<PickerModel>) {
            items = list
            notifyDataSetChanged()
        }

        override fun getItemCount() = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_model_picker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val modelName = itemView.findViewById<TextView>(R.id.model_name)
            private val providerChip = itemView.findViewById<Chip>(R.id.provider_chip)
            private val contextBadge = itemView.findViewById<TextView>(R.id.context_badge)
            private val btnStar = itemView.findViewById<View>(R.id.btn_star)
            private val btnSelect = itemView.findViewById<View>(R.id.btn_select)

            fun bind(model: PickerModel) {
                modelName.text = model.displayName

                providerChip.text = model.providerName
                val color = PROVIDER_COLORS[model.providerSlug] ?: PROVIDER_COLORS["default"]!!
                providerChip.setChipBackgroundColorResource(android.R.color.transparent)
                providerChip.background?.setTint(color)

                if (model.contextDisplay.isNotBlank()) {
                    contextBadge.text = model.contextDisplay
                    contextBadge.isVisible = true
                } else {
                    contextBadge.isVisible = false
                }

                val starRes = if (model.modelId in _pinnedIds)
                    R.drawable.ic_star_filled else R.drawable.ic_star_outline
                btnStar.setBackgroundResource(starRes)

                btnStar.setOnClickListener {
                    if (model.modelId in _pinnedIds) {
                        _pinnedIds.remove(model.modelId)
                    } else {
                        _pinnedIds.add(model.modelId)
                    }
                    applySort()
                }

                val clickTarget = itemView
                clickTarget.setOnClickListener { onClick(model) }
                btnSelect.setOnClickListener { onClick(model) }
            }
        }
    }
}
