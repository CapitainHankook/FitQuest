package ru.tzhack.facegame.common.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Holder для использования в RecyclerView.Adapter с поддержкой databinding.
 */
open class ViewDataBindingHolder<VDB : ViewDataBinding>(val dataBinding: VDB) : RecyclerView.ViewHolder(dataBinding.root)
