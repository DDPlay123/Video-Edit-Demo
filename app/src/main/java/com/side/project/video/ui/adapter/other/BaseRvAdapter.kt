package com.side.project.video.ui.adapter.other

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseRvAdapter<VB : ViewBinding, R : Any>(val data: List<R>) : RecyclerView.Adapter<BaseRvAdapter.BaseViewHolder<VB>>() {

    protected lateinit var binding: VB
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    /**
     * 取得 position
     */
    var getAbsoluteAdapterPosition: Int = -1
    var getLayoutPosition: Int = -1

    /**
     * 初始化方法，只為執行在Adapter剛建立時。
     */
    open fun initialize(binding: VB) {}

    /**
     * 主程式。
     */
    open fun bind(item: R, binding: VB, position: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        binding = bindingInflater.invoke(LayoutInflater.from(parent.context), parent, false)
        initialize(binding)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int){
        getAbsoluteAdapterPosition = holder.absoluteAdapterPosition
        getLayoutPosition = holder.layoutPosition
        bind(data[position], holder.binding, holder.absoluteAdapterPosition)
    }

    override fun getItemCount(): Int = data.size

    class BaseViewHolder<VB: ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}