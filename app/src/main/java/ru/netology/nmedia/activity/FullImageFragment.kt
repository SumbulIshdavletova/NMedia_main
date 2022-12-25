package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentFullImageBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class FullImageFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }
    private var _binding: FragmentFullImageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullImageBinding.inflate(
            inflater, container, false
        )

        viewModel.data.observe(viewLifecycleOwner) { post ->
            val name= arguments?.textArg
            val url = "${BuildConfig.BASE_URL}/media/${name}"
            Glide.with(binding.fullImage)
                .load(url)
                .placeholder(R.drawable.ic_baseline_rotate_right_24)
                .error(R.drawable.ic_baseline_error_24)
                .timeout(10_000)
                .into(binding.fullImage)
        }


        return binding.root
    }
}