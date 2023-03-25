package com.side.project.video.ui.adapter.other

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class BaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

class BaseItemCallback<R : Any> : DiffUtil.ItemCallback<R>() {
    /**
     * 以整個Item為基準，如不同則觸發Item更新。
     * 如要以Item的Id為判斷基準，可自自行自定義。
     */
    override fun areItemsTheSame(oldItem: R, newItem: R): Boolean =
        oldItem.toString() == newItem.toString()

    /**
     * 判斷要變動的Item，這邊預設是變動兩個List間，Item不同的項目。
     * 通常不用需要更改。
     */
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: R, newItem: R): Boolean =
        oldItem == newItem
}

/**
 * RecyclerView Base Adapter
 * 使用方法；
 * 1. 設定 List：exampleAdapter.submitList("Your-List".toMutableList()) <-- list一定要轉換成MutableList。
 * 2. 取得 List：exampleAdapter.currentList.toMutableList() <-- 如果要對List變動，list也要轉換成MutableList。
 * 3. 取得 List 數量：exampleAdapter.itemCount
 *
 * @param layoutRes：ex.R.layout.item...
 * @param differItemCallback：預設為BaseItemCallback，有需要可以自定義替換。
 */
abstract class BaseRvListAdapter<VB : ViewBinding, R : Any>(
    @LayoutRes val layoutRes: Int,
    differItemCallback: DiffUtil.ItemCallback<R> = BaseItemCallback()
) : ListAdapter<R, BaseViewHolder<VB>>(differItemCallback) {

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

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        getAbsoluteAdapterPosition = holder.absoluteAdapterPosition
        getLayoutPosition = holder.layoutPosition
        bind(getItem(position), holder.binding, holder.absoluteAdapterPosition)
    }

    override fun getItemViewType(position: Int): Int = layoutRes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        binding = bindingInflater.invoke(LayoutInflater.from(parent.context), parent, false)
        initialize(binding)
        return BaseViewHolder(binding)
    }

    override fun getItemCount(): Int = currentList.size
}