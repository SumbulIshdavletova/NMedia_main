package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onDeleteLike(post: Post) {
                viewModel.unlikeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })

        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
            if (state.removeError) {
                Snackbar.make(binding.root, R.string.error_remove, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.removeById(id.toLong()) }
                    .show()
            }
            if (state.likeError) {
                Snackbar.make(binding.root, R.string.error_like, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.likeById(id.toLong())
                        viewModel.unlikeById(id.toLong())
                    }
                    .show()
            }
            if (state.saveError) {
                Snackbar.make(binding.root, R.string.error_save, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.save() }
                    .show()
            }
        }
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.empty
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.updateFab.setOnClickListener {
            viewModel.refreshPosts()
            binding.updateFab.isVisible = false
            viewModel.newerCount.value?.let { it1 -> binding.list.smoothScrollToPosition(it1) }

        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            if (state != null) {
                binding.updateFab.isVisible = true
            }
        }

        return binding.root
    }
}
