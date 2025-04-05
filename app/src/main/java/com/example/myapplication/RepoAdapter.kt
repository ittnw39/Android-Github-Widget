package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemRepoBinding
import com.example.myapplication.model.Repository

// Repository 데이터를 표시하는 Adapter
class RepoAdapter : RecyclerView.Adapter<RepoAdapter.RepoViewHolder>() {

    private var repoList: List<Repository> = listOf()

    // Repo 데이터를 전달받아 어댑터에 세팅
    fun submitList(repos: List<Repository>) {
        repoList = repos
        notifyDataSetChanged()  // 데이터가 변경되었음을 알려 RecyclerView에 반영
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val binding = ItemRepoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RepoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        holder.bind(repoList[position])
    }

    override fun getItemCount(): Int = repoList.size

    // ViewHolder 클래스
    inner class RepoViewHolder(private val binding: ItemRepoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(repository: Repository) {
            binding.repository = repository
            binding.executePendingBindings()
        }
    }
}
