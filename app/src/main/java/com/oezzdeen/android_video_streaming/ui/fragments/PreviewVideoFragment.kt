package com.oezzdeen.android_video_streaming.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.oezzdeen.android_video_streaming.databinding.FragmentPreviewVideoBinding

class PreviewVideoFragment : Fragment(), Player.Listener {


    private lateinit var exoPlayer: ExoPlayer

    private val args: PreviewVideoFragmentArgs by navArgs()

    private val mTAG: String = "_PreviewVideoFragment"
    private var _binding: FragmentPreviewVideoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPreviewVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExoPlayer()

        args.videoUrl?.let {
            Log.d(mTAG, "onViewCreated: URL = $it")
            setMP4File(it)
        }
    }

    private fun setupExoPlayer() {
        exoPlayer = ExoPlayer.Builder(requireContext())
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()
        exoPlayer.playWhenReady = true
        binding.playerView.player = exoPlayer
        exoPlayer.addListener(this)
    }

    private fun setMP4File(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        when (playbackState) {
            Player.STATE_BUFFERING -> {
                binding.Progressbar.isVisible = true
                binding.playerView.useController = false
            }
            Player.STATE_READY -> {
                binding.Progressbar.isVisible = false
                binding.playerView.useController = true
            }
            /*Player.STATE_ENDED -> {
                binding.Progressbar.isVisible = false
            }
            Player.STATE_IDLE -> {
                //
            }*/
        }
    }
}